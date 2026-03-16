package com.afterdark.financer.ui.screens.home

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import com.afterdark.financer.FinanceRApplication
import com.afterdark.financer.data.models.CategoryEntity
import com.afterdark.financer.data.models.TransactionEntity
import com.afterdark.financer.data.repositories.CategoryRepository
import com.afterdark.financer.data.repositories.PreferencesRepository
import com.afterdark.financer.data.repositories.ProfileRepository
import com.afterdark.financer.data.repositories.TransactionRepository
import com.afterdark.financer.ui.UiState
import com.afterdark.financer.ui.asUiState
import com.afterdark.financer.ui.screens.Home
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeScreenViewModel(
    val savedState: SavedStateHandle,
    val categoryRepo: CategoryRepository,
    val profileRepo: ProfileRepository,
    val preferenceStore: PreferencesRepository,
    val transactionRepository: TransactionRepository) : ViewModel() {

    val activeProfile = savedState.toRoute<Home>()

    val uiState = combine(
        categoryRepo.getProfileCategories(activeProfile.userId).asUiState(),
        profileRepo.getProfile(activeProfile.userId).asUiState(),
        preferenceStore.personalBudgetView.asUiState(),
        transactionRepository.getLatestProfileTransaction(activeProfile.userId).asUiState()
    )
    {
        categories,profile,preference,trans ->
        HomeScreenUI(
            categoryUi = categories,
            selectedUi = profile,
            itemizedBudget = preference,
            latestTransaction = trans
        )
    }.flowOn(Dispatchers.Default)
        .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeScreenUI()
    )

    private val _errorUi = MutableStateFlow(ErrorUI())
    val errorUi = _errorUi.asStateFlow()

    fun addCategory(nameNew: String) : Boolean{
        val exists = (uiState.value.categoryUi as UiState.Ok).data.firstOrNull { cat->cat.name== nameNew}
        if(exists!=null){
            _errorUi.update { old -> old.copy(categoryCreation = "No duplicate category name") }
            return false
        }
        var ok = true
        viewModelScope.launch {
            try {
                val newcat = CategoryEntity(
                    name = nameNew,
                    profileId = (uiState.value.selectedUi as UiState.Ok).data.id
                )
                categoryRepo.insertCategory(newcat)
                _errorUi.update { old -> old.copy(categoryCreation = null) }
            } catch (e : Exception){
                ok=false
                _errorUi.update { old -> old.copy(categoryCreation = "No duplicate category name") }
                Log.d("HOME-ViewModel",e.message?:"Not know")
            }
        }
        return ok
    }

    fun addExpense(category: CategoryEntity,addedExpense: Double,comment: String?){
        viewModelScope.launch {
            val updated = category.copy(currentExpense = category.currentExpense+addedExpense)
            categoryRepo.updateCategory(updated)
            val transaction = TransactionEntity(
                categoryId = category.id,
                valueMoved = addedExpense,
                comment = comment
            )
            transactionRepository.insertTransaction(transaction)
        }
    }

    fun setItemizedBudget(category: CategoryEntity,newBudget: Double){
        viewModelScope.launch {
            if(newBudget<=0) return@launch
            val updated = category.copy(personalizedBudget = newBudget)
            categoryRepo.updateCategory(updated)
        }
    }

    fun setItemizedView(){
        viewModelScope.launch {
            preferenceStore.togglePersonalizedBudgetView()
        }
    }

    fun clearAllExpenses(){
        viewModelScope.launch {
            (uiState.value.categoryUi as UiState.Ok).data
                .filter { elem -> elem.currentExpense>0.0 }
                .forEach { categoryElem ->
                    val updatedCat = categoryElem.copy(currentExpense = 0.0)
                    val transaction = TransactionEntity(categoryId = categoryElem.id, valueMoved = -categoryElem.currentExpense, comment = "System action triggered by user clear")
                    categoryRepo.updateCategory(updatedCat)
                    transactionRepository.insertTransaction(transaction)
            }
        }
    }

    fun removeExpense(category: CategoryEntity, removedExp: Double, comment: String?){
        viewModelScope.launch {
            val updated = category.copy(currentExpense = (category.currentExpense-removedExp).coerceAtLeast(0.0))
            categoryRepo.updateCategory(updated)
            val transaction = TransactionEntity(
                categoryId = category.id,
                valueMoved = -removedExp,
                comment = comment
            )
            transactionRepository.insertTransaction(transaction)
        }
    }

    fun renameCategory(category:CategoryEntity,nameNew: String) : Boolean{
        val exists = (uiState.value.categoryUi as UiState.Ok).data.firstOrNull { cat->cat.name== nameNew}
        if(exists!=null){
            _errorUi.update { old -> old.copy(categoryRename = "No duplicate category name") }
            return false
        }
        var ok = true
        viewModelScope.launch {
            try {
                val newcat = category.copy(name = nameNew)
                categoryRepo.updateCategory(newcat)
                _errorUi.update { old -> old.copy(categoryRename = null) }
            } catch (e : Exception){
                ok=false
                _errorUi.update { old -> old.copy(categoryRename = "No duplicate category name") }
                Log.d("HOME-ViewModel",e.message?:"Not know")
            }
        }
        return ok
    }

    fun deleteCategory(category: CategoryEntity){
        viewModelScope.launch {
            categoryRepo.deleteCategory(category)
        }
    }





    companion object{

        val Factory : ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[APPLICATION_KEY] as FinanceRApplication)
                val catRepo = app.container.categoryRepository
                val profRepo = app.container.profileRepository
                val prefRepo = app.container.preferencesRepository
                val savedHandle = createSavedStateHandle()
                val trans = app.container.transactionRepository
                HomeScreenViewModel(
                    savedState = savedHandle,
                    categoryRepo = catRepo,
                    profileRepo = profRepo,
                    preferenceStore = prefRepo,
                    transactionRepository = trans
                )
            }
        }
    }
}