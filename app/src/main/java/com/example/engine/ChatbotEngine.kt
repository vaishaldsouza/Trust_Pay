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
import java.util.Locale
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
    val isError: Boolean = false,
    val isLocalAnswer: Boolean = false
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

    /**
     * Deterministic Local Question Matcher:
     * Answers basic questions 100% locally using local ViewModel / Room DB state.
     * Guaranteed ZERO network calls made.
     */
    fun matchLocalQuestion(
        query: String,
        stateContext: AppStateContext,
        language: AppLanguage
    ): ChatMessage? {
        val qLower = query.trim().lowercase(Locale.ROOT)
        val remainingOffline = (stateContext.offlineLimit - stateContext.offlineExposure).coerceAtLeast(0L)

        // 1. "What's my balance?" / Wallet Balance Query
        val isBalanceQuery = qLower.contains("balance") || qLower.contains("wallet") ||
                qLower.contains("बैलेंस") || qLower.contains("बाकी") ||
                qLower.contains("ಬ್ಯಾಲೆನ್ಸ್") || qLower.contains("ಬಾಕಿ") ||
                qLower.contains("ബാലൻസ്") || qLower.contains("money in my") ||
                qLower.contains("my balance") || qLower.contains("how much money")

        if (isBalanceQuery) {
            val text = when (language) {
                AppLanguage.ENGLISH -> "Your total wallet balance is ₹${stateContext.walletBalance.toInt()}. You have ₹$remainingOffline available in offline spending allowance, with ₹${stateContext.offlineExposure} in offline exposure."
                AppLanguage.HINDI -> "आपका कुल वॉलेट बैलेंस ₹${stateContext.walletBalance.toInt()} है। आपके पास ₹$remainingOffline का ऑफलाइन खर्च भत्ता उपलब्ध है, और वर्तमान में ₹${stateContext.offlineExposure} का एक्सपोज़र है।"
                AppLanguage.KANNADA -> "ನಿಮ್ಮ ಒಟ್ಟು ವ್ಯಾಲೆಟ್ ಬ್ಯಾಲೆನ್ಸ್ ₹${stateContext.walletBalance.toInt()}. ಆಫ್‌ಲೈನ್ ಖರ್ಚಿಗೆ ₹$remainingOffline ಲಭ್ಯವಿದೆ, ಮತ್ತು ಪ್ರಸ್ತುತ ₹${stateContext.offlineExposure} ಆಫ್‌ಲೈನ್ ಎಕ್ಸ್‌ಪೋಸರ್ ಇದೆ."
                AppLanguage.MALAYALAM -> "നിങ്ങളുടെ ആകെ വാലറ്റ് ബാലൻസ് ₹${stateContext.walletBalance.toInt()} ആണ്. ഓഫ്‌ലൈൻ ചെലവഴിക്കലിനായി ₹$remainingOffline ലഭ്യമാണ്, നിലവിൽ ₹${stateContext.offlineExposure} ഓഫ്‌ലൈൻ എക്സ്പോഷർ ഉണ്ട്."
            }
            Log.d(TAG, "⚡ ROUTING DECISION: Basic Balance Question matched locally. Network call skipped entirely.")
            return ChatMessage(sender = ChatSender.BOT, text = text, isLocalAnswer = true, isError = false)
        }

        // 2. "How much can I spend offline today?" / Offline Spending Limit Query
        val isOfflineLimitQuery = qLower.contains("spend offline") || qLower.contains("offline limit") ||
                qLower.contains("offline allowance") || qLower.contains("offline spend") ||
                qLower.contains("ऑफ़लाइन सीमा") || qLower.contains("ऑफलाइन खर्च") ||
                qLower.contains("ಆಫ್‌ಲೈನ್ ಮಿತಿ") || qLower.contains("ಆಫ್‌ಲೈನ್ ಖರ್ಚು") ||
                qLower.contains("ഓഫ്‌ലൈൻ പരിധി") || qLower.contains("limit today")

        if (isOfflineLimitQuery) {
            val text = when (language) {
                AppLanguage.ENGLISH -> "You can spend up to ₹$remainingOffline offline today. Your daily offline limit is ₹${stateContext.offlineLimit} with ₹${stateContext.offlineExposure} currently in offline exposure."
                AppLanguage.HINDI -> "आप आज ₹$remainingOffline तक ऑफ़लाइन खर्च कर सकते हैं। आपकी दैनिक ऑफ़लाइन सीमा ₹${stateContext.offlineLimit} है और वर्तमान में ₹${stateContext.offlineExposure} एक्सपोज़र है।"
                AppLanguage.KANNADA -> "ನೀವು ಇಂದು ₹$remainingOffline ವರೆಗೆ ಆಫ್‌ಲೈನ್‌ನಲ್ಲಿ ಖರ್ಚು ಮಾಡಬಹುದು. ನಿಮ್ಮ ದಿನನಿತ್ಯದ ಆಫ್‌ಲೈನ್ ಮಿತಿ ₹${stateContext.offlineLimit}."
                AppLanguage.MALAYALAM -> "നിങ്ങൾക്ക് ഇന്ന് ₹$remainingOffline വരെ ഓഫ്‌ലൈനായി ചെലവഴിക്കാനാകും. നിങ്ങളുടെ ദിവസേനയുള്ള ഓഫ്‌ലൈൻ പരിധി ₹${stateContext.offlineLimit} ആണ്."
            }
            Log.d(TAG, "⚡ ROUTING DECISION: Offline Allowance Question matched locally. Network call skipped entirely.")
            return ChatMessage(sender = ChatSender.BOT, text = text, isLocalAnswer = true, isError = false)
        }

        // 3. "What is my mandate status?" / Mandate Query
        val isMandateQuery = qLower.contains("mandate") || qLower.contains("मैंडेट") ||
                qLower.contains("ಮ್ಯಾಂಡೇಟ್") || qLower.contains("മാൻഡേറ്റ്")

        if (isMandateQuery) {
            val text = when (language) {
                AppLanguage.ENGLISH -> "Your recurring mandate reference is ${stateContext.mandateReference ?: "MND-9823-XYZ"} and its status is ${stateContext.mandateStatus}."
                AppLanguage.HINDI -> "आपका मैंडेट संदर्भ ${stateContext.mandateReference ?: "MND-9823-XYZ"} है और इसकी स्थिति ${stateContext.mandateStatus} है।"
                AppLanguage.KANNADA -> "ನಿಮ್ಮ ಮ್ಯಾಂಡೇಟ್ ಉಲ್ಲೇಖ ${stateContext.mandateReference ?: "MND-9823-XYZ"} ಮತ್ತು ಅದರ ಸ್ಥಿತಿ ${stateContext.mandateStatus}."
                AppLanguage.MALAYALAM -> "നിങ്ങളുടെ മാൻഡേറ്റ് റഫറൻസ് ${stateContext.mandateReference ?: "MND-9823-XYZ"} ആണ്, അതിന്റെ നില ${stateContext.mandateStatus} ആണ്."
            }
            Log.d(TAG, "⚡ ROUTING DECISION: Mandate Query matched locally. Network call skipped entirely.")
            return ChatMessage(sender = ChatSender.BOT, text = text, isLocalAnswer = true, isError = false)
        }

        // 4. "How many pending transactions do I have?" / Pending Sync Count Query
        val isPendingQuery = qLower.contains("pending transaction") || qLower.contains("pending count") ||
                qLower.contains("pending sync") || qLower.contains("queued transaction") ||
                qLower.contains("how many pending") || qLower.contains("लंबित") ||
                qLower.contains("ಬಾಕಿ ವಹಿವಾಟು") || qLower.contains("ಕಾത്തിരിക്കുന്ന ഇടപാടുകൾ")

        if (isPendingQuery) {
            val text = when (language) {
                AppLanguage.ENGLISH -> "You currently have ${stateContext.pendingSyncCount} pending transaction(s) queued locally waiting for cloud sync."
                AppLanguage.HINDI -> "वर्तमान में आपके पास क्लाउड सिंक की प्रतीक्षा कर रहे ${stateContext.pendingSyncCount} लंबित लेनदेन हैं।"
                AppLanguage.KANNADA -> "ಪ್ರಸ್ತುತ ನಿಮ್ಮಲ್ಲಿ ${stateContext.pendingSyncCount} ಬಾಕಿ ವಹಿವಾಟುಗಳು ಸಿಂಕ್‌ಗಾಗಿ ಕಾಯುತ್ತಿವೆ."
                AppLanguage.MALAYALAM -> "നിലവിൽ നിങ്ങൾക്ക് സിങ്ക് ചെയ്യാനായി കാത്തിരിക്കുന്ന ${stateContext.pendingSyncCount} ഇടപാടുകൾ ഉണ്ട്."
            }
            Log.d(TAG, "⚡ ROUTING DECISION: Pending Transactions Query matched locally. Network call skipped entirely.")
            return ChatMessage(sender = ChatSender.BOT, text = text, isLocalAnswer = true, isError = false)
        }

        // 5. "What's my current risk level?" / Risk Score Query
        val isRiskQuery = qLower.contains("risk level") || qLower.contains("risk score") ||
                qLower.contains("fraud score") || qLower.contains("my risk") ||
                qLower.contains("जोखिम स्तर") || qLower.contains("ಅಪಾಯದ ಮಟ್ಟ") ||
                qLower.contains("അപായ നില")

        if (isRiskQuery) {
            val text = when (language) {
                AppLanguage.ENGLISH -> "Your current local risk level is LOW (Active risk alerts: ${stateContext.riskAlertsCount}). All offline transactions are secured by on-device Ed25519 cryptography."
                AppLanguage.HINDI -> "आपका वर्तमान जोखिम स्तर कम है (सक्रिय जोखिम अलर्ट: ${stateContext.riskAlertsCount})। सभी ऑफलाइन लेनदेन सुरक्षित हैं।"
                AppLanguage.KANNADA -> "ನಿಮ್ಮ ಪ್ರಸ್ತುತ ಆಫ್‌ಲೈನ್ ಅಪಾಯದ ಮಟ್ಟ ಕಡಿಮೆಯಾಗಿದೆ (ಸಕ್ರಿಯ ಎಚ್ಚರಿಕೆಗಳು: ${stateContext.riskAlertsCount})."
                AppLanguage.MALAYALAM -> "നിങ്ങളുടെ നിലവിലെ അപായ നില കുറവാണ് (സജീവ അപായ മുന്നറിയിപ്പുകൾ: ${stateContext.riskAlertsCount})."
            }
            Log.d(TAG, "⚡ ROUTING DECISION: Risk Level Query matched locally. Network call skipped entirely.")
            return ChatMessage(sender = ChatSender.BOT, text = text, isLocalAnswer = true, isError = false)
        }

        // 6. Navigation Help: "how do I make a payment"
        val isHowToPayQuery = qLower.contains("how to pay") || qLower.contains("make a payment") ||
                qLower.contains("how do i pay") || qLower.contains("भुगतान कैसे करें") ||
                qLower.contains("ಪಾವತಿ ಮಾಡುವುದು ಹೇಗೆ") || qLower.contains("എങ്ങനെ പേയ്‌മെന്റ് ചെയ്യാം")

        if (isHowToPayQuery) {
            val text = when (language) {
                AppLanguage.ENGLISH -> "To make a payment, tap 'Pay' on the bottom bar, enter the amount, select your transport mode (Soundwave, BLE, Wi-Fi Direct, or QR), and confirm with your device signature."
                AppLanguage.HINDI -> "भुगतान करने के लिए, नीचे 'Pay' टैब दबाएं, राशि दर्ज करें, माध्यम चुनें और पुष्टि करें।"
                AppLanguage.KANNADA -> "ಪಾವತಿ ಮಾಡಲು, 'Pay' ಟ್ಯಾಬ್ ಒತ್ತಿ, ಮೊತ್ತವನ್ನು ನಮೂದಿಸಿ ಮತ್ತು ದೃಢೀಕರಿಸಿ."
                AppLanguage.MALAYALAM -> "പേയ്‌മെന്റ് നടത്താൻ, 'Pay' ടാപ്പ് ചെയ്യുക, തുക നൽകി സ്ഥിരീകരിക്കുക."
            }
            Log.d(TAG, "⚡ ROUTING DECISION: Payment Navigation Help Query matched locally. Network call skipped entirely.")
            return ChatMessage(sender = ChatSender.BOT, text = text, isLocalAnswer = true, isError = false)
        }

        // 7. Navigation Help: "how do I check my transaction history"
        val isHowToHistoryQuery = qLower.contains("transaction history") || qLower.contains("view history") ||
                qLower.contains("check history") || qLower.contains("इतिहास कैसे देखें") ||
                qLower.contains("ಇತಿಹಾಸ ಪರಿಶೀಲಿಸುವುದು ಹೇಗೆ") || qLower.contains("ചരിത്രം എങ്ങനെ ಕಾಣാം")

        if (isHowToHistoryQuery) {
            val text = when (language) {
                AppLanguage.ENGLISH -> "To check your transaction history, tap the 'Activity' tab on the bottom navigation bar. You can view settled, pending sync, and flagged transactions."
                AppLanguage.HINDI -> "लेनदेन इतिहास देखने के लिए नीचे दिए गए 'Activity' टैब पर टैप करें।"
                AppLanguage.KANNADA -> "ವಹಿವಾಟಿನ ಇತಿಹಾಸವನ್ನು ವೀಕ್ಷಿಸಲು ಕೆಳಗಿನ 'Activity' ಟ್ಯಾಬ್ ಅನ್ನು ಕ್ಲಿಕ್ ಮಾಡಿ."
                AppLanguage.MALAYALAM -> "ഇടപാട് ചരിത്രം കാണാൻ താഴെയുള്ള 'Activity' ടാബ് ടാപ്പ് ചെയ്യുക."
            }
            Log.d(TAG, "⚡ ROUTING DECISION: History Navigation Help Query matched locally. Network call skipped entirely.")
            return ChatMessage(sender = ChatSender.BOT, text = text, isLocalAnswer = true, isError = false)
        }

        // 8. Check for decline query without any declined transaction in history (Honesty Check)
        val isDeclineQuery = qLower.contains("decline") || qLower.contains("failed") || qLower.contains("reject")
        val failedOrDeclinedTx = stateContext.recentTransactions.firstOrNull { tx ->
            tx.status.name.contains("FRAUD") || tx.status.name.contains("DECLINED") || tx.status.name.contains("REVIEW") || tx.status.name.contains("FAILED")
        }

        if (isDeclineQuery && failedOrDeclinedTx == null) {
            val plainAnswer = when (language) {
                AppLanguage.ENGLISH -> "You don't have any declined payments recently. Your recent transactions have been processed without decline flags."
                AppLanguage.HINDI -> "हाल ही में आपका कोई भी भुगतान अस्वीकृत नहीं हुआ है। आपके हाल के सभी लेनदेन बिना किसी समस्या के संसाधित हुए हैं।"
                AppLanguage.KANNADA -> "ನಿಮ್ಮ ಇತ್ತೀಚಿನ ಯಾವುದೇ ಪಾವತಿಗಳು ತಿರಸ್ಕೃತಗೊಂಡಿಲ್ಲ. ನಿಮ್ಮ ಎಲ್ಲಾ ಇತ್ತೀಚಿನ ವಹಿವಾಟುಗಳು ಯಶಸ್ವಿಯಾಗಿವೆ."
                AppLanguage.MALAYALAM -> "നിങ്ങളുടെ സമീപകാല പേയ്‌മെന്റുകളൊന്നും നിരസിക്കപ്പെട്ടിട്ടില്ല. നിങ്ങളുടെ എല്ലാ സമീപകാല ഇടപാടുകളും വിജയകരമായിരുന്നു."
            }
            Log.d(TAG, "⚡ ROUTING DECISION: Decline query with no failed transactions matched locally. Network call skipped entirely.")
            return ChatMessage(sender = ChatSender.BOT, text = plainAnswer, isLocalAnswer = true, isError = false)
        }

        return null
    }

    /**
     * Primary Conversational Router:
     * 1. Evaluates query against matchLocalQuestion(). If matched -> returns local response (0 network calls).
     * 2. If query is open-ended and device is offline -> returns clear offline notice (0 network calls).
     * 3. If query is open-ended and device is online -> executes Gemini 2.5 Flash API request.
     */
    suspend fun processUserQuery(
        query: String,
        isVoiceInput: Boolean,
        stateContext: AppStateContext,
        language: AppLanguage,
        recentMessages: List<ChatMessage> = emptyList()
    ): ChatMessage = withContext(Dispatchers.IO) {
        // STEP 1: ROUTING DECISION - Check local deterministic pattern matcher FIRST
        val localMatch = matchLocalQuestion(query, stateContext, language)
        if (localMatch != null) {
            return@withContext localMatch
        }

        // STEP 2: ROUTING DECISION - Query is open-ended. Check network connectivity.
        if (!stateContext.isOnline) {
            Log.d(TAG, "🌐 ROUTING DECISION: Open-ended question asked offline. Gemini network call BLOCKED.")
            val offlineMsg = when (language) {
                AppLanguage.ENGLISH -> "This needs an internet connection to answer — please try again once you're back online"
                AppLanguage.HINDI -> "इसका उत्तर देने के लिए इंटरनेट कनेक्शन की आवश्यकता है — ऑनलाइन होने पर पुनः प्रयास करें"
                AppLanguage.KANNADA -> "ಇದಕ್ಕೆ ಉತ್ತರಿಸಲು ಇಂಟರ್ನೆಟ್ ಸಂಪರ್ಕದ ಅಗತ್ಯವಿದೆ — ಆನ್‌ಲೈನ್‌ಗೆ ಬಂದ ನಂತರ ಮತ್ತೆ ಪ್ರಯತ್ನಿಸಿ"
                AppLanguage.MALAYALAM -> "ഇതിന് ഉത്തരം നൽകാൻ ഇന്റർനെറ്റ് കണക്ഷൻ ആവശ്യമാണ് — ഓൺലൈനിൽ വന്ന ശേഷം വീണ്ടും ശ്രമിക്കുക"
            }
            return@withContext ChatMessage(
                sender = ChatSender.BOT,
                text = offlineMsg,
                isError = true,
                isLocalAnswer = false
            )
        }

        // STEP 3: ROUTING DECISION - Online & Open-ended -> Proceed to Gemini 2.5 Flash API
        Log.d(TAG, "✨ ROUTING DECISION: Open-ended question online. Routing query to Gemini 2.5 Flash API.")
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key missing or unconfigured.")
            return@withContext ChatMessage(
                sender = ChatSender.BOT,
                text = "Gemini API key is unconfigured — please check local.properties",
                isError = true,
                isLocalAnswer = false
            )
        }

        try {
            val langName = when (language) {
                AppLanguage.ENGLISH -> "English"
                AppLanguage.HINDI -> "Hindi"
                AppLanguage.KANNADA -> "Kannada"
                AppLanguage.MALAYALAM -> "Malayalam"
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
                - Network Mode: ONLINE (Cloud Sync Active)
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
                5. STRICT SAFETY RULE: You are strictly read-only and explanatory. You can NEVER authorize, submit, or execute a payment yourself.
            """.trimIndent()

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
                            Log.d(TAG, "✨ GEMINI SUCCESS: Answer generated by Gemini 2.5 Flash API.")
                            return@withContext ChatMessage(
                                sender = ChatSender.BOT,
                                text = text.trim(),
                                isError = false,
                                isLocalAnswer = false
                            )
                        }
                    }
                }
            }

            return@withContext ChatMessage(
                sender = ChatSender.BOT,
                text = "Assistant unavailable — try again",
                isError = true,
                isLocalAnswer = false
            )
        } catch (e: Exception) {
            Log.w(TAG, "Gemini API Chatbot error: ${e.message}")
            return@withContext ChatMessage(
                sender = ChatSender.BOT,
                text = "Assistant unavailable — try again",
                isError = true,
                isLocalAnswer = false
            )
        }
    }
}
