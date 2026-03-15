package com.afterdark.financer.ui.screens.graphs

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
import com.afterdark.financer.data.repositories.CategoryRepository
import com.afterdark.financer.ui.screens.Graph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update


sealed interface UiState{
    object Loading : UiState
    data class Error(val message: String) : UiState
    data class Success(val data:List<CategoryEntity>) : UiState
}

enum class ViewTypes{
    BAR,
    DONUT
}

data class GraphsUiState(
    val categories: UiState = UiState.Loading,
    val view: ViewTypes = ViewTypes.DONUT
)

class GraphsViewModel(savedState: SavedStateHandle,categoryRepo: CategoryRepository) : ViewModel() {

    private val routeArgs = savedState.toRoute<Graph>()
    private val currentView = MutableStateFlow(ViewTypes.DONUT)

    val uiState = combine(
        categoryRepo.getProfileCategories(routeArgs.userId)
            .map<List<CategoryEntity>,UiState> { categ -> UiState.Success(categ) }
            .onStart { emit(UiState.Loading) }
            .catch { e -> emit(UiState.Error(e.message?:"Not know"))},
        currentView
    ) {categories,view ->
        GraphsUiState(categories=categories,view=view)
    }.flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GraphsUiState()
        )

    fun toogleGraphView(){
        currentView.update { prev -> if(prev == ViewTypes.BAR) ViewTypes.DONUT else ViewTypes.BAR }
    }



    companion object{
        val FACTORY : ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val saved = createSavedStateHandle()
                val app = (this[APPLICATION_KEY] as FinanceRApplication)
                GraphsViewModel(saved,app.container.categoryRepository)
            }
        }
    }
}