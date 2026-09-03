package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Transaction
import com.example.data.model.TransactionMode
import com.example.data.model.TransactionStatus

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val transactionId: String,
    val buyerId: String,
    val buyerName: String,
    val merchantId: String,
    val merchantName: String,
    val amount: Long,
    val currency: String,
    val mode: String,
    val timestamp: Long,
    val nonce: String,
    val signature: String,
    val status: String,
    val fraudProbability: Float,
    val anomalyScore: Float,
    val fraudReasonsJson: String,
    val createdAt: Long,
    val syncedAt: Long?,
    val settledAt: Long?,
    val settlementRef: String?,
    val isOfflineQueued: Boolean
) {
    fun toDomain(): Transaction {
        return Transaction(
            transactionId = transactionId,
            buyerId = buyerId,
            buyerName = buyerName,
            merchantId = merchantId,
            merchantName = merchantName,
            amount = amount,
            currency = currency,
            mode = try { TransactionMode.valueOf(mode) } catch (e: Exception) { TransactionMode.OFFLINE_VALUE },
            timestamp = timestamp,
            nonce = nonce,
            signature = signature,
            status = try { TransactionStatus.valueOf(status) } catch (e: Exception) { TransactionStatus.CREATED },
            fraudProbability = fraudProbability,
            anomalyScore = anomalyScore,
            fraudReasons = if (fraudReasonsJson.isBlank()) emptyList() else fraudReasonsJson.split(";;;"),
            createdAt = createdAt,
            syncedAt = syncedAt,
            settledAt = settledAt,
            settlementRef = settlementRef
        )
    }

    companion object {
        fun fromDomain(tx: Transaction, isOfflineQueued: Boolean = false): TransactionEntity {
            return TransactionEntity(
                transactionId = tx.transactionId,
                buyerId = tx.buyerId,
                buyerName = tx.buyerName,
                merchantId = tx.merchantId,
                merchantName = tx.merchantName,
                amount = tx.amount,
                currency = tx.currency,
                mode = tx.mode.name,
                timestamp = tx.timestamp,
                nonce = tx.nonce,
                signature = tx.signature,
                status = tx.status.name,
                fraudProbability = tx.fraudProbability,
                anomalyScore = tx.anomalyScore,
                fraudReasonsJson = tx.fraudReasons.joinToString(";;;"),
                createdAt = tx.createdAt,
                syncedAt = tx.syncedAt,
                settledAt = tx.settledAt,
                settlementRef = tx.settlementRef,
                isOfflineQueued = isOfflineQueued
            )
        }
    }
}

@Entity(tableName = "used_nonces")
data class UsedNonceEntity(
    @PrimaryKey val nonce: String,
    val usedAt: Long = System.currentTimeMillis()
)
