package com.afterdark.financer.ui.screens.history

import com.afterdark.financer.data.models.TransactionWithCategory


data class HistoryUiState(
    val transactions: UiState<List<TransactionWithCategory>> = UiState.Loading,
    val exportStatus : UiStateTriggered<String> = UiStateTriggered.NoStart
)


sealed interface UiState<out T>{
    object Loading : UiState<Nothing>
    data class Error(val errorMessage: String) : UiState<Nothing>
    data class Success<T>(val data:T) : UiState<T>
}

sealed interface UiStateTriggered<out T>{
    object Loading : UiStateTriggered<Nothing>
    data class Error(val errorMessage: String) : UiStateTriggered<Nothing>
    data class Success<T>(val data:T) : UiStateTriggered<T>
    object NoStart: UiStateTriggered<Nothing>
}