package com.afterdark.financer.ui.screens.home

import com.afterdark.financer.data.models.CategoryEntity
import com.afterdark.financer.data.models.ProfileEntity
import com.afterdark.financer.data.models.TransactionWithCategory
import com.afterdark.financer.ui.UiState


data class HomeScreenUI (
    val categoryUi : UiState<List<CategoryEntity>> = UiState.Loading,
    val selectedUi : UiState<ProfileEntity> = UiState.Loading,
    val itemizedBudget : UiState<Boolean> = UiState.Loading,
    val latestTransaction : UiState<TransactionWithCategory?> = UiState.Loading
)

data class ErrorUI(
    val categoryCreation: String? = null,
    val categoryRename: String?=null
)

val HomeScreenUI.isLoading: Boolean
    get() =
        categoryUi is UiState.Loading ||
                selectedUi is UiState.Loading ||
                itemizedBudget is UiState.Loading


