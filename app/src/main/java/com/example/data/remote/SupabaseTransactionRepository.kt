package com.example.data.remote

import android.util.Log
import com.example.crypto.CryptoEngine
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class SupabaseTransactionRepository {
    private val tag = "SupabaseTxRepo"

    suspend fun uploadTransaction(tx: Transaction): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (!SupabaseClient.isConfigured()) {
                return@withContext Result.success(true) // Graceful local fallback
            }

            val payloadStr = CryptoEngine.buildCanonicalPayload(
                buyerId = tx.buyerId,
                merchantId = tx.merchantId,
                amount = tx.amount,
                transactionId = tx.transactionId,
                nonce = tx.nonce,
                timestamp = tx.timestamp,
                mode = tx.mode.name,
                mandateReference = "MND-9823-XYZ"
            )

            val json = JSONObject().apply {
                put("id", tx.transactionId)
                put("buyer_id", tx.buyerId)
                put("merchant_id", tx.merchantId)
                put("amount", tx.amount)
                put("mode", tx.mode.name)
                put("status", tx.status.name)
                put("nonce", tx.nonce)
                put("payload", payloadStr)
                put("signature", tx.signature)
                put("fraud_score", tx.fraudProbability)
                put("anomaly_score", tx.anomalyScore)
                put("fraud_reasons", JSONArray(tx.fraudReasons))
                put("created_at", java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).format(java.util.Date(tx.timestamp)))
                if (tx.syncedAt != null) {
                    put("synced_at", java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).format(java.util.Date(tx.syncedAt)))
                }
                if (tx.settledAt != null) {
                    put("settled_at", java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).format(java.util.Date(tx.settledAt)))
                }
                if (tx.settlementRef != null) {
                    put("razorpay_settlement_id", tx.settlementRef)
                }
            }

            val body = json.toString().toRequestBody(SupabaseClient.JSON_MEDIA_TYPE)
            val request = SupabaseClient.buildRequest(
                path = "/rest/v1/transactions",
                method = "POST",
                body = body,
                extraHeaders = mapOf("Prefer" to "resolution=merge-duplicates")
            ) ?: return@withContext Result.success(true)

            val result = SupabaseClient.execute(request)
            if (result.isSuccess) {
                Log.d(tag, "Successfully synced transaction ${tx.transactionId} to Supabase")
                Result.success(true)
            } else {
                Log.w(tag, "Supabase transaction upload failed: ${result.exceptionOrNull()?.message}")
                Result.failure(result.exceptionOrNull() ?: Exception("Unknown upload error"))
            }
        } catch (e: Exception) {
            Log.e(tag, "Error uploading to Supabase: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateTransactionStatus(
        transactionId: String,
        status: TransactionStatus,
        settlementRef: String? = null,
        settledAt: Long? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (!SupabaseClient.isConfigured()) {
                return@withContext Result.success(true)
            }

            val json = JSONObject().apply {
                put("status", status.name)
                put("synced_at", java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).format(java.util.Date()))
                if (settlementRef != null) {
                    put("razorpay_settlement_id", settlementRef)
                }
                if (settledAt != null) {
                    put("settled_at", java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).format(java.util.Date(settledAt)))
                }
            }

            val body = json.toString().toRequestBody(SupabaseClient.JSON_MEDIA_TYPE)
            val request = SupabaseClient.buildRequest(
                path = "/rest/v1/transactions?id=eq.$transactionId",
                method = "PATCH",
                body = body
            ) ?: return@withContext Result.success(true)

            val result = SupabaseClient.execute(request)
            if (result.isSuccess) {
                Result.success(true)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to update status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadFraudResult(
        transactionId: String,
        score: Double,
        anomalyScore: Double,
        reasons: List<String>
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (!SupabaseClient.isConfigured()) {
                return@withContext Result.success(true)
            }

            val json = JSONObject().apply {
                put("fraud_score", score)
                put("anomaly_score", anomalyScore)
                put("fraud_reasons", JSONArray(reasons))
            }

            val body = json.toString().toRequestBody(SupabaseClient.JSON_MEDIA_TYPE)
            val request = SupabaseClient.buildRequest(
                path = "/rest/v1/transactions?id=eq.$transactionId",
                method = "PATCH",
                body = body
            ) ?: return@withContext Result.success(true)

            SupabaseClient.execute(request).map { true }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadFraudAlert(alert: RemoteFraudAlert): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (!SupabaseClient.isConfigured()) return@withContext Result.success(true)

            val json = JSONObject().apply {
                put("id", alert.id)
                put("transaction_id", alert.transactionId)
                put("severity", alert.severity)
                put("score", alert.score)
                put("reasons", JSONArray(alert.reasons))
                put("created_at", alert.createdAt)
                put("resolved", alert.resolved)
            }

            val body = json.toString().toRequestBody(SupabaseClient.JSON_MEDIA_TYPE)
            val request = SupabaseClient.buildRequest(
                path = "/rest/v1/fraud_alerts",
                method = "POST",
                body = body,
                extraHeaders = mapOf("Prefer" to "resolution=merge-duplicates")
            ) ?: return@withContext Result.success(true)

            SupabaseClient.execute(request).map { true }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFraudAlerts(): Result<List<RemoteFraudAlert>> = withContext(Dispatchers.IO) {
        try {
            if (!SupabaseClient.isConfigured()) {
                return@withContext Result.success(emptyList())
            }

            val request = SupabaseClient.buildRequest(
                path = "/rest/v1/fraud_alerts?order=created_at.desc&limit=20",
                method = "GET"
            ) ?: return@withContext Result.success(emptyList())

            val res = SupabaseClient.execute(request)
            if (res.isSuccess) {
                val array = JSONArray(res.getOrNull() ?: "[]")
                val alerts = mutableListOf<RemoteFraudAlert>()
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    val reasonsArray = item.optJSONArray("reasons") ?: JSONArray()
                    val reasonsList = mutableListOf<String>()
                    for (j in 0 until reasonsArray.length()) {
                        reasonsList.add(reasonsArray.getString(j))
                    }
                    alerts.add(
                        RemoteFraudAlert(
                            id = item.optString("id", java.util.UUID.randomUUID().toString()),
                            transactionId = item.optString("transaction_id", ""),
                            severity = item.optString("severity", "MEDIUM"),
                            score = item.optDouble("score", 0.0),
                            reasons = reasonsList,
                            createdAt = item.optString("created_at", ""),
                            resolved = item.optBoolean("resolved", false)
                        )
                    )
                }
                Result.success(alerts)
            } else {
                Result.failure(res.exceptionOrNull() ?: Exception("Failed to fetch fraud alerts"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recordUsedNonce(nonce: String, transactionId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (!SupabaseClient.isConfigured()) return@withContext Result.success(true)

            val json = JSONObject().apply {
                put("nonce", nonce)
                put("transaction_id", transactionId)
                put("created_at", java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).format(java.util.Date()))
            }

            val body = json.toString().toRequestBody(SupabaseClient.JSON_MEDIA_TYPE)
            val request = SupabaseClient.buildRequest(
                path = "/rest/v1/used_nonces",
                method = "POST",
                body = body
            ) ?: return@withContext Result.success(true)

            SupabaseClient.execute(request).map { true }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isNonceUsed(nonce: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (!SupabaseClient.isConfigured()) return@withContext Result.success(false)

            val request = SupabaseClient.buildRequest(
                path = "/rest/v1/used_nonces?nonce=eq.$nonce&select=nonce",
                method = "GET"
            ) ?: return@withContext Result.success(false)

            val res = SupabaseClient.execute(request)
            if (res.isSuccess) {
                val array = JSONArray(res.getOrNull() ?: "[]")
                Result.success(array.length() > 0)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.success(false)
        }
    }

    suspend fun getAdminMetrics(): Result<AdminMetrics> = withContext(Dispatchers.IO) {
        try {
            if (!SupabaseClient.isConfigured()) {
                return@withContext Result.success(AdminMetrics())
            }

            // Call Supabase PostgreSQL RPC function get_admin_metrics()
            val request = SupabaseClient.buildRequest(
                path = "/rest/v1/rpc/get_admin_metrics",
                method = "POST"
            ) ?: return@withContext Result.success(AdminMetrics())

            val res = SupabaseClient.execute(request)
            if (res.isSuccess) {
                val json = JSONObject(res.getOrNull() ?: "{}")
                Result.success(
                    AdminMetrics(
                        totalTransactions = json.optLong("total_transactions", 0),
                        totalVolume = json.optLong("total_volume", 0),
                        settledTransactions = json.optLong("settled_transactions", 0),
                        pendingTransactions = json.optLong("pending_transactions", 0),
                        fraudTransactions = json.optLong("fraud_transactions", 0),
                        fraudRate = json.optDouble("fraud_rate", 0.0),
                        offlineTransactions = json.optLong("offline_transactions", 0),
                        authorizationTransactions = json.optLong("authorization_transactions", 0)
                    )
                )
            } else {
                Result.failure(res.exceptionOrNull() ?: Exception("RPC get_admin_metrics failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
