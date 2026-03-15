package com.afterdark.financer.ui

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

//the base form of ui state
sealed interface UiState<out T>{
    object Loading : UiState<Nothing>
    data class Ok<T>(val data:T) : UiState<T>
    data class Error(val errorMessage: String) : UiState<Nothing>
}

// I can also can really use an easy way to have flows Wrap the UiState above.
fun<T> Flow<T>.asUiState() : Flow<UiState<T>> {
    return map<T, UiState<T>>{flow ->
        UiState.Ok(data = flow)
    }.onStart {
        emit(UiState.Loading)
    }.catch { e ->
        emit(UiState.Error(errorMessage = e.message ?: "Not know error"))
    }
}

sealed interface TriggeredUi<out T> : UiState<T>{
    object NotStarted : TriggeredUi<Nothing>
    object UnableToTrigger : TriggeredUi<Nothing>
}