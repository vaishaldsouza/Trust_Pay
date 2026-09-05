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

import com.example.util.AppLanguage

object GeminiExplainabilityService {
    private const val TAG = "TrustPayGemini"
    private const val GEMINI_MODEL = "gemini-2.5-flash"

    /**
     * Translates structured ML risk signals into plain language explainability for administrators.
     * Note: Gemini strictly never influences the deterministic accept/reject decision.
     */
    suspend fun explainFlaggedTransaction(
        transaction: Transaction,
        question: String = "Why was this transaction flagged and what actions are recommended?",
        isNetworkOnline: Boolean = true,
        language: AppLanguage = AppLanguage.ENGLISH
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }

        val structuredContext = buildContextPrompt(transaction, question, language)

        if (!isNetworkOnline || apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateOfflineDeterministicExplanation(transaction, question, language)
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

            val systemInstruction = "You are TrustPay AI Risk Investigator. You explain synthetic ML fraud risk scores and offline exposure anomalies to fintech compliance officers. Respond in ${language.displayName}. You do NOT make payment approvals or declines; those are determined deterministically by Trust Agent rules."

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
            return@withContext generateOfflineDeterministicExplanation(transaction, question, language)
        } catch (e: Exception) {
            Log.w(TAG, "Gemini API error, falling back to local engine: ${e.message}")
            return@withContext generateOfflineDeterministicExplanation(transaction, question, language)
        }
    }

    private fun buildContextPrompt(tx: Transaction, userQuery: String, language: AppLanguage): String {
        return """
            Language: ${language.displayName}
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

            Provide a clear, 3-4 sentence risk analysis in ${language.displayName} summarizing:
            1. The primary trigger (e.g. deviation from historical baseline or rapid burst)
            2. The exposure risk to the merchant and buyer
            3. Recommended operational step (e.g. restrict device or contact buyer to verify)
        """.trimIndent()
    }

    private fun generateOfflineDeterministicExplanation(tx: Transaction, userQuery: String, language: AppLanguage): String {
        val riskPercent = (tx.fraudProbability * 100).toInt()
        val reasonsText = tx.fraudReasons.joinToString(". ")

        return when (language) {
            AppLanguage.HINDI -> {
                "जोखिम मूल्यांकन (${riskPercent}% विश्वास):\n\n" +
                "लेनदेन ${tx.transactionId} ने उच्च विसंगति सीमा को ट्रिगर किया: $reasonsText। " +
                "₹${tx.amount} की लेनदेन राशि खरीदार के पैटर्न के सापेक्ष जोखिम पैदा करती है। " +
                "सिफारिश: इस लेनदेन को मैनुअल समीक्षा में रखें। पहचान सत्यापन तक डिवाइस ${tx.buyerId} पर आगे के ऑफ-लाइन लेनदेन को प्रतिबंधित करने पर विचार करें।"
            }
            AppLanguage.KANNADA -> {
                "ಅಪಾಯ ಮೌಲ್ಯಮಾಪನ (${riskPercent}% ನಂಬಿಕೆ):\n\n" +
                "ವ್ಯವಹಾರ ${tx.transactionId} ಹೆಚ್ಚಿನ ಅಸಂಗತತೆಯ ಮಿತಿಯನ್ನು ಪ್ರಚೋದಿಸಿದೆ: $reasonsText. " +
                "₹${tx.amount} ವ್ಯವಹಾರ ಮೊತ್ತವು ಖರೀದಿದಾರರ ಆಫ್‌ಲೈನ್ ಮಾದರಿಗೆ ಹೋಲಿಸಿದರೆ ಅಸಾಮಾನ್ಯ ಅಪಾಯವನ್ನು ಸೃಷ್ಟಿಸುತ್ತದೆ. " +
                "ಶಿಫಾರಸು: ಈ ವ್ಯವಹಾರವನ್ನು ಪರಿಶೀಲನೆಯಲ್ಲಿರಿಸಿ. ಗುರುತು ಪರಿಶೀಲನೆವರೆಗೆ ಸಾಧನ ${tx.buyerId} ನಲ್ಲಿ ವ್ಯವಹಾರಗಳನ್ನು ನಿರ್ಬಂಧಿಸಿ."
            }
            AppLanguage.MALAYALAM -> {
                "അപായ നിർണ്ണയം (${riskPercent}% വിശ്വാസ്യത):\n\n" +
                "ഇടപാട് ${tx.transactionId} ഉയർന്ന അപാകത പരിധി ലംഘിച്ചു: $reasonsText. " +
                "₹${tx.amount} തുക വാങ്ങുന്നയാളുടെ ചരിത്രപരമായ രീതിയുമായി താരതമ്യം ചെയ്യുമ്പോൾ അപകടസാധ്യത ഉണ്ടാക്കുന്നു. " +
                "ശുപാർശ: ഈ ഇടപാട് അവലോകനത്തിൽ വയ്ക്കുക. ഐഡന്റിറ്റി സ്ഥിരീകരണം വരെ ഉപകരണം ${tx.buyerId}-ൽ കൂടുതൽ ഇടപാടുകൾ പരിമിതപ്പെടുത്തുക."
            }
            else -> {
                "Risk Assessment (${riskPercent}% Confidence):\n\n" +
                "Transaction ${tx.transactionId} triggered high anomaly thresholds because: $reasonsText. " +
                "The transaction amount of ₹${tx.amount} creates an uncharacteristic exposure spike relative to the buyer's historical offline pattern. " +
                "Recommendation: Place this transaction under Manual Fraud Review. The compliance team should consider restricting further offline transactions on device ${tx.buyerId} until identity verification is completed."
            }
        }
    }

    /**
     * Answers open-ended user voice queries (e.g., "why was my last payment declined", "how much can I spend offline today")
     * using the user's real live context (wallet balance, offline exposure, offline limit, recent transactions, mandate status).
     *
     * STRICT DECISION ISOLATION: Gemini is strictly read-only and explanatory. It can NEVER execute payments.
     */
    suspend fun answerVoiceQueryWithContext(
        query: String,
        languageName: String = "English",
        walletBalance: Double,
        offlineExposure: Long,
        offlineLimit: Long,
        recentTransactions: List<Transaction>,
        mandateReference: String?,
        mandateStatus: String,
        isNetworkOnline: Boolean = true,
        pendingSyncCount: Int = 0
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }

        val remainingOfflineAllowance = (offlineLimit - offlineExposure).coerceAtLeast(0L)
        val lastTx = recentTransactions.firstOrNull()

        val contextPrompt = """
            User Spoken Query: "$query"
            User Language: $languageName
            
            Current Real User State:
            - Total Wallet Balance: ₹${walletBalance.toInt()}
            - Current Offline Exposure: ₹$offlineExposure
            - Daily Offline Limit: ₹$offlineLimit
            - Remaining Available Offline Allowance: ₹$remainingOfflineAllowance
            - Pending Sync Transactions Count: $pendingSyncCount
            - Network Mode: ${if (isNetworkOnline) "ONLINE (Cloud Sync Active)" else "OFFLINE (Local Device Allowance Active)"}
            - Mandate Reference: ${mandateReference ?: "MND-9823-XYZ"}
            - Mandate Status: $mandateStatus
            - Total Transactions Recorded: ${recentTransactions.size}
            - Latest Transaction ID: ${lastTx?.transactionId ?: "None"}
            - Latest Transaction Amount: ${if (lastTx != null) "₹${lastTx.amount}" else "N/A"}
            - Latest Transaction Status: ${lastTx?.status?.name ?: "N/A"}
            - Latest Transaction Merchant: ${lastTx?.merchantName ?: "N/A"}
            - Latest Transaction Flagged Reasons: ${lastTx?.fraudReasons?.joinToString("; ") ?: "None"}

            System Rules:
            1. You are TrustPay Voice Guide. Respond directly in $languageName.
            2. Keep your answer concise (2-3 sentences max) so it sounds natural when spoken aloud via TextToSpeech.
            3. Answer the user's question directly using the exact numbers and state above (e.g., explain why a payment was declined or how much they can spend).
            4. STRICT SAFETY RULE: You are strictly read-only and explanatory. You can NEVER authorize or submit a payment yourself. If the user asks to pay or transfer money, explain that for security they must review and sign the transaction on the display screen.
        """.trimIndent()

        if (!isNetworkOnline || apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateOfflineVoiceAnswer(query, walletBalance, offlineExposure, offlineLimit, lastTx, mandateStatus)
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

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", contextPrompt))
                        })
                    })
                })
            }

            OutputStreamWriter(conn.outputStream).use { writer ->
                writer.write(jsonBody.toString())
                writer.flush()
            }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
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
            return@withContext generateOfflineVoiceAnswer(query, walletBalance, offlineExposure, offlineLimit, lastTx, mandateStatus)
        } catch (e: Exception) {
            Log.w(TAG, "Gemini Voice API error: ${e.message}")
            return@withContext generateOfflineVoiceAnswer(query, walletBalance, offlineExposure, offlineLimit, lastTx, mandateStatus)
        }
    }

    private fun generateOfflineVoiceAnswer(
        query: String,
        walletBalance: Double,
        offlineExposure: Long,
        offlineLimit: Long,
        lastTx: Transaction?,
        mandateStatus: String
    ): String {
        val q = query.lowercase()
        val remaining = (offlineLimit - offlineExposure).coerceAtLeast(0L)

        return when {
            q.contains("decline") || q.contains("failed") || q.contains("reject") || q.contains("why") -> {
                if (lastTx != null && (lastTx.status.name.contains("FRAUD") || lastTx.status.name.contains("DECLINED") || lastTx.status.name.contains("REVIEW"))) {
                    "Your transaction ${lastTx.transactionId} for ₹${lastTx.amount} to ${lastTx.merchantName} was flagged under status ${lastTx.status.name.replace("_", " ")}. Reasons: ${lastTx.fraudReasons.joinToString("; ").ifEmpty { "High deviation from historical offline pattern" }}."
                } else {
                    "Your current wallet balance is ₹${walletBalance.toInt()} with ₹$remaining available in offline spending allowance. No recent payments have failed."
                }
            }
            q.contains("spend") || q.contains("allowance") || q.contains("limit") || q.contains("how much") -> {
                "You can still spend ₹$remaining offline today out of your total daily limit of ₹$offlineLimit. Your current offline exposure is ₹$offlineExposure."
            }
            q.contains("mandate") || q.contains("upi") || q.contains("recurring") -> {
                "Your Razorpay mandate MND-9823-XYZ status is currently $mandateStatus, authorizing automated background settlements."
            }
            else -> {
                "Your wallet balance is ₹${walletBalance.toInt()} with ₹$remaining available for offline payments. For your security, voice commands cannot authorize payments directly."
            }
        }
    }
}
