package com.afterdark.financer.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.afterdark.financer.FinanceRApplication
import com.afterdark.financer.data.repositories.PreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


sealed interface SimpleUi{
    object Loading: SimpleUi
    data class Done(val userId:Long) : SimpleUi
}
class AppViewModel(private val prefRepo : PreferencesRepository) : ViewModel() {

    val uiState: StateFlow<SimpleUi> = prefRepo.lastView
        .map { userFlow -> SimpleUi.Done(userFlow) }
        .stateIn(
        scope = viewModelScope,
        initialValue = SimpleUi.Loading,
        started = SharingStarted.WhileSubscribed(5000)
    )

    var startingValue: Long? = null

    init {
        viewModelScope.launch {
            startingValue = prefRepo.lastView.first()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as FinanceRApplication)
                val pref = application.container.preferencesRepository
                AppViewModel(prefRepo = pref)
            }
        }
    }
}