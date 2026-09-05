package com.example.engine

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.Transaction
import com.example.util.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

enum class ChatSender {
    USER_TEXT,
    USER_VOICE,
    BOT
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: ChatSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false
)

data class AppStateContext(
    val walletBalance: Double,
    val offlineExposure: Long,
    val offlineLimit: Long,
    val recentTransactions: List<Transaction>,
    val mandateReference: String?,
    val mandateStatus: String,
    val isOnline: Boolean,
    val pendingSyncCount: Int,
    val riskAlertsCount: Int
)

object ChatbotEngine {
    private const val TAG = "TrustPayChatbot"
    private const val GEMINI_MODEL = "gemini-2.5-flash"

    suspend fun processUserQuery(
        query: String,
        isVoiceInput: Boolean,
        stateContext: AppStateContext,
        language: AppLanguage,
        recentMessages: List<ChatMessage> = emptyList()
    ): ChatMessage = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }

        val langName = when (language) {
            AppLanguage.ENGLISH -> "English"
            AppLanguage.HINDI -> "Hindi"
            AppLanguage.KANNADA -> "Kannada"
            AppLanguage.MALAYALAM -> "Malayalam"
        }

        // Check for decline / failed query
        val qLower = query.lowercase()
        val isDeclineQuery = qLower.contains("decline") || qLower.contains("failed") || qLower.contains("reject") || qLower.contains("why")
        val failedOrDeclinedTx = stateContext.recentTransactions.firstOrNull { tx ->
            tx.status.name.contains("FRAUD") || tx.status.name.contains("DECLINED") || tx.status.name.contains("REVIEW") || tx.status.name.contains("FAILED")
        }

        // Strict Honesty Check: If user asks why payment was declined and NO declined/failed transaction exists in actual history:
        if (isDeclineQuery && failedOrDeclinedTx == null) {
            val plainAnswer = when (language) {
                AppLanguage.ENGLISH -> "You don't have any declined payments recently. Your recent transactions have been processed without decline flags."
                AppLanguage.HINDI -> "हाल ही में आपका कोई भी भुगतान अस्वीकृत नहीं हुआ है। आपके हाल के सभी लेनदेन बिना किसी समस्या के संसाधित हुए हैं।"
                AppLanguage.KANNADA -> "ನಿಮ್ಮ ಇತ್ತೀಚಿನ ಯಾವುದೇ ಪಾವತಿಗಳು ತಿರಸ್ಕೃತಗೊಂಡಿಲ್ಲ. ನಿಮ್ಮ ಎಲ್ಲಾ ಇತ್ತೀಚಿನ ವಹಿವಾಟುಗಳು ಯಶಸ್ವಿಯಾಗಿವೆ."
                AppLanguage.MALAYALAM -> "നിങ്ങളുടെ സമീപകാല പേയ്‌മെന്റുകളൊന്നും നിരസിക്കപ്പെട്ടിട്ടില്ല. നിങ്ങളുടെ എല്ലാ സമീപകാല ഇടപാടുകളും വിജയകരമായിരുന്നു."
            }
            return@withContext ChatMessage(
                sender = ChatSender.BOT,
                text = plainAnswer
            )
        }

        val remainingOffline = (stateContext.offlineLimit - stateContext.offlineExposure).coerceAtLeast(0L)
        val lastTx = stateContext.recentTransactions.firstOrNull()

        val historyText = recentMessages.takeLast(6).joinToString("\n") { msg ->
            val senderLabel = if (msg.sender == ChatSender.BOT) "Assistant" else "User"
            "$senderLabel: ${msg.text}"
        }

        val contextPrompt = """
            User Spoken/Typed Query: "$query"
            User Language: $langName
            Input Mode: ${if (isVoiceInput) "VOICE_TRANSCRIBED" else "TEXT_TYPED"}
            
            Real User App State Context:
            - Total Wallet Balance: ₹${stateContext.walletBalance.toInt()}
            - Current Offline Exposure: ₹${stateContext.offlineExposure}
            - Daily Offline Limit: ₹${stateContext.offlineLimit}
            - Remaining Available Offline Allowance: ₹$remainingOffline
            - Pending Sync Transactions Count: ${stateContext.pendingSyncCount}
            - Active Risk Flags Count: ${stateContext.riskAlertsCount}
            - Network Mode: ${if (stateContext.isOnline) "ONLINE (Cloud Sync Active)" else "OFFLINE (Local Device Allowance Active)"}
            - Mandate Reference: ${stateContext.mandateReference ?: "MND-9823-XYZ"}
            - Mandate Status: ${stateContext.mandateStatus}
            - Total Transactions Recorded: ${stateContext.recentTransactions.size}
            - Latest Transaction ID: ${lastTx?.transactionId ?: "None"}
            - Latest Transaction Amount: ${if (lastTx != null) "₹${lastTx.amount}" else "N/A"}
            - Latest Transaction Status: ${lastTx?.status?.name ?: "N/A"}
            - Latest Transaction Merchant: ${lastTx?.merchantName ?: "N/A"}
            - Latest Transaction Flagged Reasons: ${lastTx?.fraudReasons?.joinToString("; ") ?: "None"}

            Recent Conversation History:
            $historyText

            System Rules:
            1. You are TrustPay Conversational Guide. Respond directly in $langName.
            2. Keep your answer clear and concise (2-3 sentences max) so it sounds natural in text chat and TextToSpeech.
            3. Use the exact numbers from the real app state context above.
            4. ACCURACY MANDATE: If asked about declines or failures, ONLY refer to real failed transactions in the context. Never fabricate an event that didn't happen.
            5. STRICT SAFETY RULE: You are strictly read-only and explanatory. You can NEVER authorize, submit, or execute a payment yourself. If the user asks to pay or transfer money, explain that for security they must review and sign the transaction on the Payment screen display.
        """.trimIndent()

        if (!stateContext.isOnline || apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext ChatMessage(
                sender = ChatSender.BOT,
                text = "Assistant unavailable — try again",
                isError = true
            )
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
                            return@withContext ChatMessage(
                                sender = ChatSender.BOT,
                                text = text.trim()
                            )
                        }
                    }
                }
            }

            return@withContext ChatMessage(
                sender = ChatSender.BOT,
                text = "Assistant unavailable — try again",
                isError = true
            )
        } catch (e: Exception) {
            Log.w(TAG, "Gemini API Chatbot error: ${e.message}")
            return@withContext ChatMessage(
                sender = ChatSender.BOT,
                text = "Assistant unavailable — try again",
                isError = true
            )
        }
    }
}
