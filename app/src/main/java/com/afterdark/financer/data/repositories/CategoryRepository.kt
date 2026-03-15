package com.afterdark.financer.data.repositories

import com.afterdark.financer.data.daos.CategoryDao
import com.afterdark.financer.data.models.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {

    // Get all categories for a profile
    fun getProfileCategories(profileId: Long): Flow<List<CategoryEntity>> {
        return categoryDao.getProfileCategories(profileId)
    }

    suspend fun insertCategory(category: CategoryEntity) {
        categoryDao.insert(category)
    }

    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.update(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.delete(category)
    }
}