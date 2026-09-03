package com.example.data.remote

data class RemoteUser(
    val id: String,
    val name: String,
    val role: String,
    val createdAt: String? = null
)

data class RemoteDevice(
    val id: String,
    val userId: String,
    val publicKey: String,
    val keyAlgorithm: String = "Ed25519",
    val trustTier: String = "STANDARD",
    val createdAt: String? = null
)

data class RemoteTransaction(
    val id: String,
    val buyerId: String,
    val merchantId: String,
    val amount: Long,
    val mode: String,
    val status: String,
    val nonce: String,
    val payload: String,
    val signature: String,
    val fraudScore: Double = 0.0,
    val anomalyScore: Double = 0.0,
    val fraudReasons: List<String> = emptyList(),
    val createdAt: String,
    val syncedAt: String? = null,
    val settledAt: String? = null,
    val razorpayPaymentId: String? = null,
    val razorpaySettlementId: String? = null
)

data class RemoteUsedNonce(
    val nonce: String,
    val transactionId: String,
    val createdAt: String? = null
)

data class RemoteFraudAlert(
    val id: String,
    val transactionId: String,
    val severity: String,
    val score: Double,
    val reasons: List<String>,
    val createdAt: String,
    val resolved: Boolean = false
)

data class AdminMetrics(
    val totalTransactions: Long = 0,
    val totalVolume: Long = 0,
    val settledTransactions: Long = 0,
    val pendingTransactions: Long = 0,
    val fraudTransactions: Long = 0,
    val fraudRate: Double = 0.0,
    val offlineTransactions: Long = 0,
    val authorizationTransactions: Long = 0
)

data class AuthResponse(
    val accessToken: String?,
    val tokenType: String?,
    val user: RemoteUser?
)
