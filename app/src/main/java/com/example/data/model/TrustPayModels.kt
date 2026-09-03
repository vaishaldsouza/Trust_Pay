package com.example.data.model

enum class UserRole {
    BUYER,
    MERCHANT,
    ADMIN
}

enum class TransactionMode {
    OFFLINE_VALUE,
    AUTHORIZATION,
    ONLINE
}

enum class TransactionStatus {
    CREATED,
    SIGNED,
    OFFLINE_ACCEPTED,
    PENDING_SYNC,
    SYNCED,
    FRAUD_CHECKED,
    SETTLEMENT_PENDING,
    SETTLED,
    INVALID_SIGNATURE,
    DUPLICATE,
    REPLAY_DETECTED,
    FRAUD_REVIEW,
    SETTLEMENT_FAILED,
    CANCELLED
}

enum class RiskSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val createdAt: Long = System.currentTimeMillis(),
    val deviceId: String,
    val riskScore: Int = 12
)

data class Buyer(
    val userId: String,
    val offlineLimit: Long = 500L,
    val offlineExposure: Long = 180L,
    val mandateReference: String = "MND-9823-XYZ",
    val maxMandateMonthly: Long = 2000L,
    val successfulTransactions: Int = 38,
    val failedTransactions: Int = 1,
    val fraudFlags: Int = 0,
    val isRestricted: Boolean = false
) {
    val availableExposure: Long
        get() = (offlineLimit - offlineExposure).coerceAtLeast(0L)
}

data class Merchant(
    val merchantId: String,
    val businessName: String,
    val location: String,
    val category: String,
    val riskScore: Int = 8,
    val totalTransactions: Int = 1420,
    val logoUrl: String? = null
)

data class Transaction(
    val transactionId: String,
    val buyerId: String,
    val buyerName: String,
    val merchantId: String,
    val merchantName: String,
    val amount: Long,
    val currency: String = "INR",
    val mode: TransactionMode,
    val timestamp: Long = System.currentTimeMillis(),
    val nonce: String,
    val signature: String,
    val status: TransactionStatus,
    val fraudProbability: Float = 0.05f,
    val anomalyScore: Float = 0.02f,
    val fraudReasons: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null,
    val settledAt: Long? = null,
    val settlementRef: String? = null,
    val isTampered: Boolean = false
)

data class FraudAlert(
    val alertId: String,
    val transactionId: String,
    val buyerName: String,
    val merchantName: String,
    val amount: Long,
    val riskScore: Int,
    val severity: RiskSeverity,
    val reasons: List<String>,
    val status: String = "REVIEW",
    val createdAt: Long = System.currentTimeMillis()
)

data class Device(
    val deviceId: String,
    val userId: String,
    val publicKey: String,
    val keyCreatedAt: Long = System.currentTimeMillis(),
    val lastSync: Long = System.currentTimeMillis(),
    val deviceRisk: String = "LOW"
)
