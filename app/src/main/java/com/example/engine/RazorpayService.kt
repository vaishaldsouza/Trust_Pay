package com.example.engine

import android.util.Log
import com.example.data.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class MandateDetails(
    val mandateId: String = "MND-UNREACHABLE",
    val type: String = "Razorpay UPI Autopay Token",
    val status: String = "BACKEND_UNREACHABLE",
    val maxMonthlyLimit: Long = 0L,
    val authorizedAccount: String = "ganesh@okhdfcbank",
    val validUntil: String = "Dec 2028"
)

data class SettlementResult(
    val isSuccess: Boolean,
    val settlementRef: String,
    val paymentId: String,
    val amount: Long,
    val note: String,
    val failureReason: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

object RazorpayService {
    private const val TAG = "TrustPayRazorpay"
    const val LABEL = "Razorpay Real Test-Mode Proxy"

    const val RENDER_URL = "https://trust-pay-j0dh.onrender.com"
    const val LOCAL_EMULATOR_URL = "http://10.0.2.2:3000"

    // Default backend proxy endpoint (Deployed Render URL for physical devices & demo)
    private var backendBaseUrl = RENDER_URL

    // Increased timeout for Render cold-starts (35 seconds)
    private const val TIMEOUT_MS = 35000

    fun getBackendBaseUrl(): String = backendBaseUrl

    fun setBackendBaseUrl(url: String) {
        if (url.isNotBlank()) {
            backendBaseUrl = url.trimEnd('/')
        }
    }

    fun useRenderBackend() {
        backendBaseUrl = RENDER_URL
    }

    fun useLocalEmulatorBackend() {
        backendBaseUrl = LOCAL_EMULATOR_URL
    }

    fun getActiveMandate(): MandateDetails = MandateDetails()

    /**
     * Calls backend proxy POST /api/mandate/create to create a Razorpay token order.
     * Uses 35s timeout to allow Render free-tier cold starts to wake up cleanly.
     * Fails cleanly with BACKEND_UNREACHABLE if server is offline or times out.
     */
    suspend fun createMandate(buyerId: String = "dev_buyer_01", maxAmount: Long = 2000L): MandateDetails = withContext(Dispatchers.IO) {
        try {
            val url = URL("$backendBaseUrl/api/mandate/create")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                doOutput = true
            }

            val body = JSONObject().apply {
                put("buyerId", buyerId)
                put("maxAmount", maxAmount)
            }

            OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseStr = stream?.bufferedReader()?.use(BufferedReader::readText) ?: "{}"
            val json = JSONObject(responseStr)

            if (responseCode in 200..299 && json.optBoolean("success")) {
                val mndRef = json.optString("mandateReference", "MND-UNREACHABLE")
                val limit = json.optLong("maxMonthlyLimit", maxAmount)
                Log.i(TAG, "Successfully created Razorpay Mandate via Backend Proxy: $mndRef")
                return@withContext MandateDetails(
                    mandateId = mndRef,
                    type = json.optString("type", "Razorpay UPI Autopay Token"),
                    status = json.optString("status", "ACTIVE"),
                    maxMonthlyLimit = limit
                )
            } else {
                val errorMsg = json.optString("error", "Mandate creation failed")
                Log.e(TAG, "Mandate creation failed: $errorMsg")
                return@withContext MandateDetails(
                    mandateId = "MND-UNREACHABLE",
                    type = "Razorpay UPI Autopay Token",
                    status = "MANDATE_CREATION_FAILED",
                    maxMonthlyLimit = 0L
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Backend proxy unreachable for mandate creation: ${e.message}")
            return@withContext MandateDetails(
                mandateId = "MND-UNREACHABLE",
                type = "Razorpay UPI Autopay Token",
                status = "BACKEND_UNREACHABLE",
                maxMonthlyLimit = 0L
            )
        }
    }

    /**
     * Executes settlement draw-down against stored mandate via HTTPS backend proxy.
     * Uses 35s timeout to allow Render free-tier cold starts to wake up cleanly.
     * Re-validates Ed25519 signature server-side.
     * Returns SETTLEMENT_FAILED / BACKEND_UNREACHABLE when proxy is offline. Never fabricates success.
     */
    suspend fun executeSettlement(transaction: Transaction): SettlementResult = withContext(Dispatchers.IO) {
        try {
            val url = URL("$backendBaseUrl/api/settlement/execute")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                doOutput = true
            }

            val body = JSONObject().apply {
                put("transactionId", transaction.transactionId)
                put("buyerId", transaction.buyerId)
                put("merchantId", transaction.merchantId)
                put("amount", transaction.amount)
                put("nonce", transaction.nonce)
                put("timestamp", transaction.timestamp)
                put("mode", transaction.mode.name)
                put("mandateReference", "MND-9823-XYZ")
                put("signature", transaction.signature)
            }

            OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseStr = stream?.bufferedReader()?.use(BufferedReader::readText) ?: "{}"
            val json = JSONObject(responseStr)

            if (responseCode in 200..299 && json.optBoolean("success")) {
                val paymentId = json.optString("paymentId", "")
                val settlementRef = json.optString("settlementRef", "")
                Log.i(TAG, "[$LABEL] Settlement SETTLED for ${transaction.transactionId} -> Ref: $settlementRef")
                return@withContext SettlementResult(
                    isSuccess = true,
                    settlementRef = settlementRef,
                    paymentId = paymentId,
                    amount = transaction.amount,
                    note = "$LABEL | Razorpay Order Charge Executed"
                )
            } else {
                val reason = json.optString("reason", "SETTLEMENT_FAILED")
                val errorMsg = json.optString("error", "Mandate settlement declined by Razorpay API.")
                Log.e(TAG, "[$LABEL] Settlement FAILED for ${transaction.transactionId}: $reason - $errorMsg")
                return@withContext SettlementResult(
                    isSuccess = false,
                    settlementRef = "UNREACHABLE",
                    paymentId = "NONE",
                    amount = transaction.amount,
                    note = errorMsg,
                    failureReason = reason
                )
            }
        } catch (e: Exception) {
            val errorMsg = "Backend Proxy Unreachable: ${e.message ?: "Connection Refused / Host Offline"}"
            Log.e(TAG, "[$LABEL] Settlement FAILED for ${transaction.transactionId}: $errorMsg")
            return@withContext SettlementResult(
                isSuccess = false,
                settlementRef = "UNREACHABLE",
                paymentId = "NONE",
                amount = transaction.amount,
                note = errorMsg,
                failureReason = "BACKEND_UNREACHABLE"
            )
        }
    }
}
