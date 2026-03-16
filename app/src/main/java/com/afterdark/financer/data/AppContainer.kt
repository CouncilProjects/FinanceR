package com.afterdark.financer.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.afterdark.financer.data.repositories.CategoryRepository
import com.afterdark.financer.data.repositories.PreferencesRepository
import com.afterdark.financer.data.repositories.ProfileRepository
import com.afterdark.financer.data.repositories.TransactionRepository


private const val PREFERENCE_NAME = "preferences"
private val Context.datastore: DataStore<Preferences> by preferencesDataStore(
    name = PREFERENCE_NAME
)
interface IAppContainer{
    val profileRepository : ProfileRepository
    val categoryRepository : CategoryRepository
    val transactionRepository : TransactionRepository

    val preferencesRepository : PreferencesRepository
}

class AppContainer(context: Context) : IAppContainer{

    val database: FinancerDatabase by lazy {
        Room.databaseBuilder(context, FinancerDatabase::class.java,"financer_database").build()
    }



    override val profileRepository: ProfileRepository by lazy { ProfileRepository(database.profileDao()) }

    override val transactionRepository: TransactionRepository by lazy { TransactionRepository(database.transactionDao()) }

    override val categoryRepository: CategoryRepository by lazy { CategoryRepository(database.categoryDao()) }

    override val preferencesRepository: PreferencesRepository = PreferencesRepository(dataStore = context.datastore)
}