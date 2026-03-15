package com.afterdark.financer.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.afterdark.financer.data.daos.CategoryDao
import com.afterdark.financer.data.daos.ProfileDao
import com.afterdark.financer.data.daos.TransactionDao
import com.afterdark.financer.data.models.CategoryEntity
import com.afterdark.financer.data.models.ProfileEntity
import com.afterdark.financer.data.models.TransactionEntity

@Database(entities = [ProfileEntity::class, CategoryEntity::class, TransactionEntity::class], version = 1, exportSchema = false)
abstract class FinancerDatabase : RoomDatabase(){
    abstract fun profileDao() : ProfileDao
    abstract fun categoryDao() : CategoryDao
    abstract fun transactionDao() : TransactionDao
}