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
import com.afterdark.financer.ui.UiState
import com.afterdark.financer.ui.asUiState
import com.afterdark.financer.ui.screens.Graph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn


//maybe will add more in the future
enum class ViewTypes{
    DONUT
}

data class GraphsUiState(
    val categories: UiState<List<CategoryEntity>> = UiState.Loading,
    val view: ViewTypes = ViewTypes.DONUT
)

class GraphsViewModel(savedState: SavedStateHandle,categoryRepo: CategoryRepository) : ViewModel() {

    private val routeArgs = savedState.toRoute<Graph>()
    private val currentView = MutableStateFlow(ViewTypes.DONUT)

    val uiState = combine(
        categoryRepo.getProfileCategories(routeArgs.userId).asUiState(),
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
       //leave empy for now
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