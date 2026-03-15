package com.afterdark.financer.ui.screens.profile

import com.afterdark.financer.data.models.ProfileEntity
import com.afterdark.financer.ui.TriggeredUi
import com.afterdark.financer.ui.UiState


data class ProfileScreenUi(
    val deviceProfiles : UiState<List<ProfileEntity>> = UiState.Loading,
    val selectedProfile : ProfileEntity? = null,
    val creationErrors : UiState<String> = TriggeredUi.NotStarted
)

data class ProfileScreenUI2 (
    val deviceProfiles : DeviceProfiles = DeviceProfiles.Loading,
    val selectedProfile : ProfileEntity? = null
)

sealed interface CreationErrorUI{
    object Loading: CreationErrorUI
    object None: CreationErrorUI
    data class Error(val errorMessage: String) : CreationErrorUI
}

data class ErrorUi(
    val errorHappened: Boolean = false,
    val errorMessage: String = ""
)

sealed interface DeviceProfiles{
    object Loading : DeviceProfiles
    data class Success(val profiles:List<ProfileEntity>) : DeviceProfiles
    data class Error(val message: String) : DeviceProfiles
}