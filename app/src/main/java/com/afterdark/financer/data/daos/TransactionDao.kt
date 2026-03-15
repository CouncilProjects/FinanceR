package com.afterdark.financer.data.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.afterdark.financer.data.models.TransactionEntity
import com.afterdark.financer.data.models.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: TransactionEntity)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("select t.*,c.name " +
            "from transactions t join categories c on t.categoryId=c.id" +
            " where c.profileId=:id " +
            "order by t.createdAt desc"
    )
    fun getAllProfileTransactions(id:Long) : Flow<List<TransactionWithCategory>>

    @Query("delete " +
            "from transactions " +
            "where id in (" +
            "select t.id from transactions t join categories c on t.categoryId=c.id" +
            " where c.profileId=:profileId)"
    )
    suspend fun clearProfileTransactions(profileId:Long)

}