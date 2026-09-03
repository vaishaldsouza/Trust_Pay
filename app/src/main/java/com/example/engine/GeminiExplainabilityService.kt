package com.example.engine

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object GeminiExplainabilityService {
    private const val TAG = "TrustPayGemini"
    private const val GEMINI_MODEL = "gemini-2.5-flash"

    /**
     * Translates structured ML risk signals into plain English explainability for administrators.
     * Note: Gemini strictly never influences the deterministic accept/reject decision.
     */
    suspend fun explainFlaggedTransaction(
        transaction: Transaction,
        question: String = "Why was this transaction flagged and what actions are recommended?",
        isNetworkOnline: Boolean = true
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }

        val structuredContext = buildContextPrompt(transaction, question)

        if (!isNetworkOnline || apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateOfflineDeterministicExplanation(transaction, question)
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$GEMINI_MODEL:generateContent?key=$apiKey"
            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.connectTimeout = 8000
            conn.readTimeout = 10000
            conn.doOutput = true

            val systemInstruction = "You are TrustPay AI Risk Investigator. You explain synthetic ML fraud risk scores and offline exposure anomalies to fintech compliance officers. You do NOT make payment approvals or declines; those are determined deterministically by Trust Agent rules."

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", "$systemInstruction\n\n$structuredContext"))
                        })
                    })
                })
            }

            OutputStreamWriter(conn.outputStream).use { writer ->
                writer.write(jsonBody.toString())
                writer.flush()
            }

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                val root = JSONObject(responseText)
                val candidates = root.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text")
                        if (text.isNotBlank()) {
                            return@withContext text.trim()
                        }
                    }
                }
            }
            // If API returned error or empty, fall back gracefully
            return@withContext generateOfflineDeterministicExplanation(transaction, question)
        } catch (e: Exception) {
            Log.w(TAG, "Gemini API error, falling back to local engine: ${e.message}")
            return@withContext generateOfflineDeterministicExplanation(transaction, question)
        }
    }

    private fun buildContextPrompt(tx: Transaction, userQuery: String): String {
        return """
            Transaction ID: ${tx.transactionId}
            Amount: ₹${tx.amount} ${tx.currency}
            Buyer: ${tx.buyerName} (${tx.buyerId})
            Merchant: ${tx.merchantName} (${tx.merchantId})
            Mode: ${tx.mode}
            Status: ${tx.status}
            ML Risk Score: ${(tx.fraudProbability * 100).toInt()}%
            Anomaly Score: ${(tx.anomalyScore * 100).toInt()}%
            Model Signals / Flagged Reasons:
            ${tx.fraudReasons.joinToString("\n") { "- $it" }}

            Investigator Question: $userQuery

            Provide a clear, 3-4 sentence risk analysis summarizing:
            1. The primary trigger (e.g. deviation from historical baseline or rapid burst)
            2. The exposure risk to the merchant and buyer
            3. Recommended operational step (e.g. restrict device or contact buyer to verify)
        """.trimIndent()
    }

    private fun generateOfflineDeterministicExplanation(tx: Transaction, userQuery: String): String {
        val riskPercent = (tx.fraudProbability * 100).toInt()
        val reasonsText = tx.fraudReasons.joinToString(". ")

        return "Risk Assessment (${riskPercent}% Confidence):\n\n" +
                "Transaction ${tx.transactionId} triggered high anomaly thresholds because: $reasonsText. " +
                "The transaction amount of ₹${tx.amount} creates an uncharacteristic exposure spike relative to the buyer's historical offline pattern. " +
                "Recommendation: Place this transaction under Manual Fraud Review. The compliance team should consider restricting further offline transactions on device ${tx.buyerId} until identity verification is completed."
    }
}
