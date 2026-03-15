package com.afterdark.financer.data.repositories

import com.afterdark.financer.data.daos.TransactionDao
import com.afterdark.financer.data.models.TransactionEntity
import com.afterdark.financer.data.models.TransactionWithCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class TransactionRepository(private val transactionDao: TransactionDao) {
    // Get all transactions for a profile with category names
    fun getAllProfileTransactions(profileId: Long): Flow<List<TransactionWithCategory>> {
        return transactionDao.getAllProfileTransactions(profileId)
    }

    suspend fun clearProfileTransactions(prof:Long){
        transactionDao.clearProfileTransactions(prof)
    }

    fun getLatestProfileTransaction(profileId: Long): Flow<TransactionWithCategory?> {
        return transactionDao.getAllProfileTransactions(profileId).map { list -> list.firstOrNull() }
    }

    suspend fun insertTransaction(transaction: TransactionEntity) {
        transactionDao.insert(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.update(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.delete(transaction)
    }
}