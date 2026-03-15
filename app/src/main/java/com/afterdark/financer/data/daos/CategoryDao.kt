package com.afterdark.financer.data.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.afterdark.financer.data.models.CategoryEntity
import com.afterdark.financer.data.models.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert
    suspend fun insert(category : CategoryEntity)

    @Update
    suspend fun update(category : CategoryEntity)

    @Delete
    suspend fun delete(category : CategoryEntity)

    @Query("select * from categories where profileId = :id order by currentExpense desc")
    fun getProfileCategories(id:Long) : Flow<List<CategoryEntity>>
}