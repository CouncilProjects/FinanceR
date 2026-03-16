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
import com.afterdark.financer.ui.TriggeredUi
import com.afterdark.financer.ui.UiState
import com.afterdark.financer.ui.asUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileScreenViewModel(private val profileRepo: ProfileRepository,private val preferenceRepo: PreferencesRepository) : ViewModel() {
    
    
    private val _creationUi = MutableStateFlow<UiState<String>>(TriggeredUi.NotStarted)
    
    val uiState = combine(
        profileRepo.getAllProfiles().asUiState(),
        preferenceRepo.lastView,
        _creationUi
    ) { devProfiles,selected,creationUi ->
        ProfileScreenUi(
            deviceProfiles = devProfiles,
            selectedProfile = if(devProfiles is UiState.Ok){devProfiles.data.firstOrNull { profile -> profile.id==selected }} else null,
            creationErrors = creationUi
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileScreenUi()
    )

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

    //Assume the currently selected use will be executed.. i mean deleted
    fun deleteUser(){
        viewModelScope.launch {
            val profile = uiState.value.selectedProfile ?: return@launch
            preferenceRepo.setLastViewedProfile(-1)
            profileRepo.deleteProfile(profile)
        }
    }

    suspend fun createProfile(name: String,budget: Double) : Boolean{
        _creationUi.update { error ->
            UiState.Loading
        }
        val exists = (uiState.value.deviceProfiles as UiState.Ok).data.firstOrNull { prof->prof.name==name }
        if(exists!=null){
            _creationUi.update { error ->
                UiState.Error(errorMessage = "No duplicate profile name")
            }
            return false
        } else {
            profileRepo.insertProfile(ProfileEntity(name=name, budget = budget))
            _creationUi.update { error ->
                UiState.Ok(data = "User profile created you may selected it") //Note i wont mark it notStarted because i want a Ui confirmation that its done
            }
            return true
        }
    }

    fun ackSuccessfulCreation(){
        _creationUi.update { TriggeredUi.NotStarted }
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