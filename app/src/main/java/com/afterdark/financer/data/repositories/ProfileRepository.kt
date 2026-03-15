package com.afterdark.financer.data.repositories

import com.afterdark.financer.data.daos.ProfileDao
import com.afterdark.financer.data.models.ProfileEntity
import kotlinx.coroutines.flow.Flow

class ProfileRepository(private val profileDao: ProfileDao) {
    fun getProfile(id: Long): Flow<ProfileEntity> {
        return profileDao.getProfile(id)
    }

    // Get all profiles as a Flow
    fun getAllProfiles(): Flow<List<ProfileEntity>> {
        return profileDao.getAllProfiles()
    }

    suspend fun insertProfile(profile: ProfileEntity) {
        profileDao.insert(profile)
    }

    suspend fun updateProfile(profile: ProfileEntity) {
        profileDao.update(profile)
    }

    suspend fun deleteProfile(profile: ProfileEntity) {
        profileDao.delete(profile)
    }
}