package com.afterdark.financer.data.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.afterdark.financer.data.models.ProfileEntity
import com.afterdark.financer.ui.screens.Profile
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    @Insert
    suspend fun insert(profile : ProfileEntity)

    @Update
    suspend fun update(profile : ProfileEntity)

    @Delete
    suspend fun delete(profile : ProfileEntity)

    @Query("select * from profiles where id=:id limit 1")
    fun getProfile(id:Long) : Flow<ProfileEntity>

    @Query("select * from profiles")
    fun getAllProfiles() : Flow<List<ProfileEntity>>
}