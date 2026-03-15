package com.afterdark.financer.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.afterdark.financer.FinanceRApplication
import com.afterdark.financer.data.models.ProfileEntity
import com.afterdark.financer.data.repositories.PreferencesRepository
import com.afterdark.financer.data.repositories.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileScreenViewModel(val profileRepo: ProfileRepository,val preferenceRepo: PreferencesRepository) : ViewModel() {

    val uiState = combine(profileRepo.getAllProfiles(),preferenceRepo.lastView) { devProfiles,selected ->
        ProfileScreenUI(
            deviceProfiles = DeviceProfiles.Success(profiles = devProfiles),
            selectedProfile = devProfiles.firstOrNull { profile -> profile.id==selected })
    }.flowOn(Dispatchers.Default)
        .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileScreenUI(
            deviceProfiles = DeviceProfiles.Loading,
            selectedProfile = null)
    )

    private var _errorUi = MutableStateFlow<CreationErrorUI>(CreationErrorUI.None)
    val errorState = _errorUi.asStateFlow()

    fun changeProfileBudget(newbudget: Double){
        viewModelScope.launch {
            val newProfile = uiState.value.selectedProfile?.copy(budget = newbudget) ?: return@launch

            profileRepo.updateProfile(newProfile)
        }
    }

    fun changeActiveProfile(id:Long){
        viewModelScope.launch {
            preferenceRepo.setLastViewedProfile(id)
        }
    }

    fun deleteUser(id:Long){
        viewModelScope.launch {
            val profile = uiState.value.selectedProfile ?: return@launch
            preferenceRepo.setLastViewedProfile(-1)
            profileRepo.deleteProfile(profile)
        }
    }

    suspend fun createProfile(name: String,budget: Double) : Boolean{
        _errorUi.update { error ->
            CreationErrorUI.Loading
        }

            val exists = (uiState.value.deviceProfiles as DeviceProfiles.Success).profiles.firstOrNull { prof->prof.name==name }
            if(exists!=null){
                _errorUi.update { error ->
                    CreationErrorUI.Error(errorMessage = "Profile name taken")
                }
                return false
            } else {
                profileRepo.insertProfile(ProfileEntity(name=name, budget = budget))
                _errorUi.update { error ->
                    CreationErrorUI.None
                }
                return true
            }

    }

    companion object{
        val Factory : ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val appl = (this[APPLICATION_KEY] as FinanceRApplication)
                val repoProf = appl.container.profileRepository
                val repoPref = appl.container.preferencesRepository
                ProfileScreenViewModel(repoProf,repoPref)
            }
        }
    }
}