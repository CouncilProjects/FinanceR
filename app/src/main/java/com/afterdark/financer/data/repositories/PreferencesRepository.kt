package com.afterdark.financer.data.repositories

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class PreferencesRepository(private val dataStore: DataStore<Preferences>) {
    val lastView : Flow<Long> = dataStore.data
        .catch {
            if(it is IOException){
                Log.e(LOGTAG,"Error reading preference",it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences -> preferences[LAST_VIEWED_PROFILE] ?: -1 }

    val personalBudgetView : Flow<Boolean> = dataStore.data
        .catch {
            if(it is IOException){
                Log.e(LOGTAG,"Error reading preference",it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences -> preferences[PERSONALIZED_BUDGER_VIEW] ?: false }

    suspend fun setLastViewedProfile(profileId: Long) {
        dataStore.edit { preferences -> preferences[LAST_VIEWED_PROFILE] = profileId }
    }

    suspend fun togglePersonalizedBudgetView() {
        dataStore.edit { preferences ->
            val current = preferences[PERSONALIZED_BUDGER_VIEW] ?: false
            preferences[PERSONALIZED_BUDGER_VIEW] = !current
        }
    }




    companion object {
        const val LOGTAG = "Preferences store"
        val LAST_VIEWED_PROFILE = longPreferencesKey(name = "last_view")
        val PERSONALIZED_BUDGER_VIEW = booleanPreferencesKey(name = "show_personalized_budget")
    }
}