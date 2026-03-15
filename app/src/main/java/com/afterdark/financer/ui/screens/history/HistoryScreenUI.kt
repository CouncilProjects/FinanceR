package com.afterdark.financer.ui.screens.history

import com.afterdark.financer.data.models.TransactionWithCategory
import com.afterdark.financer.ui.TriggeredUi
import com.afterdark.financer.ui.UiState


data class HistoryUiState(
    val transactions: UiState<List<TransactionWithCategory>> = UiState.Loading,
    val exportStatus : UiState<String> = TriggeredUi.NotStarted,
    val deletionStatus : UiState<String> = TriggeredUi.NotStarted
)