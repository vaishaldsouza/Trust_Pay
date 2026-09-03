package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    suspend fun getAllTransactionsSync(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE isOfflineQueued = 1 ORDER BY timestamp ASC")
    suspend fun getPendingOfflineTransactions(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE transactionId = :id LIMIT 1")
    suspend fun getTransactionById(id: String): TransactionEntity?

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("UPDATE transactions SET isOfflineQueued = 0, status = :status, syncedAt = :syncedAt WHERE transactionId = :id")
    suspend fun markSynced(id: String, status: String, syncedAt: Long)

    @Query("DELETE FROM transactions WHERE transactionId = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}

@Dao
interface UsedNonceDao {
    @Query("SELECT COUNT(*) FROM used_nonces WHERE nonce = :nonce")
    suspend fun countNonce(nonce: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(nonce: UsedNonceEntity)

    @Query("DELETE FROM used_nonces")
    suspend fun clearAll()
}
