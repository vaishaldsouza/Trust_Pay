package com.example.engine

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.data.model.Merchant
import com.example.data.model.Transaction
import com.example.util.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed class VoiceActionResult {
    data class SpokenAnswer(val text: String, val suggestedAction: String? = null) : VoiceActionResult()
    data class GeminiSpokenAnswer(val spokenText: String, val suggestedAction: String? = null) : VoiceActionResult()
    data class NavigateToPayment(val amount: Double, val merchant: Merchant, val spokenText: String) : VoiceActionResult()
    data class NavigateToScreen(val screenTabName: String, val spokenText: String) : VoiceActionResult()
}

class VoiceAssistantEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _transcription = MutableStateFlow("")
    val transcription: StateFlow<String> = _transcription.asStateFlow()

    private val _lastResponse = MutableStateFlow("")
    val lastResponse: StateFlow<String> = _lastResponse.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("VoiceAssistantEngine", "Failed to initialize TTS: ${e.message}")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            tts?.setPitch(1.0f)
            tts?.setSpeechRate(0.95f)
        } else {
            isTtsReady = false
            Log.w("VoiceAssistantEngine", "TTS initialization failed with code $status")
        }
    }

    fun startListening(
        language: AppLanguage,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        _isListening.value = true
        _transcription.value = ""

        try {
            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            }

            val localeCode = when (language) {
                AppLanguage.ENGLISH -> "en-IN"
                AppLanguage.HINDI -> "hi-IN"
                AppLanguage.KANNADA -> "kn-IN"
                AppLanguage.MALAYALAM -> "ml-IN"
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeCode)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, localeCode)
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    _isListening.value = false
                }
                override fun onError(error: Int) {
                    _isListening.value = false
                    val errorMsg = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Tap to try again."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out."
                        else -> "Speech recognition error ($error)"
                    }
                    onError(errorMsg)
                }
                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    _transcription.value = text
                    if (text.isNotBlank()) {
                        onResult(text)
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val partialMatches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val partial = partialMatches?.firstOrNull() ?: ""
                    if (partial.isNotBlank()) {
                        _transcription.value = partial
                    }
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
            Log.e("VoiceAssistantEngine", "SpeechRecognizer error: ${e.message}")
            onError("Microphone error: ${e.message}")
        }
    }

    fun stopListening() {
        _isListening.value = false
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e("VoiceAssistantEngine", "Error stopping recognizer: ${e.message}")
        }
    }

    fun speak(text: String, language: AppLanguage) {
        if (!isTtsReady || tts == null) {
            Log.w("VoiceAssistantEngine", "TTS is not ready.")
            return
        }

        val locale = when (language) {
            AppLanguage.ENGLISH -> Locale("en", "IN")
            AppLanguage.HINDI -> Locale("hi", "IN")
            AppLanguage.KANNADA -> Locale("kn", "IN")
            AppLanguage.MALAYALAM -> Locale("ml", "IN")
        }

        try {
            val result = tts?.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.ENGLISH) // Fallback
            }
            _isSpeaking.value = true
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TrustPayTTS")
        } catch (e: Exception) {
            Log.e("VoiceAssistantEngine", "TTS speak error: ${e.message}")
        }
    }

    fun stopSpeaking() {
        _isSpeaking.value = false
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e("VoiceAssistantEngine", "Error stopping TTS: ${e.message}")
        }
    }

    /**
     * Offline Multilingual Intent & Entity Parser:
     * Parses intent across English, Hindi, Kannada, and Malayalam.
     * CRITICAL RULE: Voice NEVER directly authorizes payments.
     */
    fun processQuery(
        query: String,
        language: AppLanguage,
        walletBalance: Double,
        offlineExposure: Long,
        offlineLimit: Long,
        recentTransactions: List<Transaction>,
        isOnline: Boolean,
        pendingSyncCount: Int,
        merchantsList: List<Merchant>
    ): VoiceActionResult {
        val q = query.trim().lowercase(Locale.ROOT)
        _transcription.value = query

        // 1. Payment Intent Parsing (e.g., "Pay 200 to Artisan Roasters", "₹250 का भुगतान करें", "200 ರೂಪಾಯಿ ಪಾವತಿಸಿ", "200 രൂപ അടയ്ക്കുക")
        val isPaymentIntent = q.contains("pay") || q.contains("send") || q.contains("भुगतान") ||
                q.contains("पैसे") || q.contains("ಪಾವತಿ") || q.contains("ಕಳುಹಿಸಿ") ||
                q.contains("പണം") || q.contains("അടയ്ക്കുക") || q.contains("നൽകുക")

        if (isPaymentIntent) {
            // Extract numeric amount
            val numberRegex = Regex("""(\d+(\.\d+)?)""")
            val match = numberRegex.find(q)
            val extractedAmount = match?.value?.toDoubleOrNull() ?: 250.0

            // Match merchant from list or default to first
            val matchedMerchant = merchantsList.firstOrNull { merchant ->
                val nameLower = merchant.businessName.lowercase(Locale.ROOT)
                val words = nameLower.split(" ")
                words.any { word -> word.length > 3 && q.contains(word) }
            } ?: merchantsList.firstOrNull() ?: Merchant("merch_01", "Artisan Roasters", "Indiranagar, Bangalore", "Coffee & Bakery", 8, 1420)

            val spokenResponse = when (language) {
                AppLanguage.ENGLISH -> "Preparing payment of ₹${extractedAmount.toInt()} to ${matchedMerchant.businessName}. For your security, voice commands cannot authorize payments. Please review and confirm with your device signature on screen."
                AppLanguage.HINDI -> "${matchedMerchant.businessName} को ₹${extractedAmount.toInt()} का भुगतान तैयार किया जा रहा है। सुरक्षा के लिए, वॉयस कमांड सीधे भुगतान अधिकृत नहीं कर सकते। कृपया स्क्रीन पर समीक्षा करें और डिवाइस हस्ताक्षर से पुष्टि करें।"
                AppLanguage.KANNADA -> "${matchedMerchant.businessName} ಅವರಿಗೆ ₹${extractedAmount.toInt()} ಪಾವತಿಯನ್ನು ಸಿದ್ಧಪಡಿಸಲಾಗುತ್ತಿದೆ. ಭದ್ರತೆಗಾಗಿ ಧ್ವನಿ ಆಜ್ಞೆಗಳು ಪಾವತಿಯನ್ನು ನೇರವಾಗಿ ದೃಢೀಕರಿಸುವುದಿಲ್ಲ. ದಯವಿಟ್ಟು ಪರದೆಯ ಮೇಲೆ ಪರಿಶೀಲಿಸಿ ಮತ್ತು ಸಹಿ ಮಾಡಿ."
                AppLanguage.MALAYALAM -> "${matchedMerchant.businessName}-ലേക്ക് ₹${extractedAmount.toInt()} പേയ്‌മെന്റ് തയ്യാറാക്കുന്നു. സുരക്ഷയ്ക്കായി വോയ്‌സ് കമാൻഡുകൾ വഴി നേരിട്ട് പണമടയ്ക്കാനാവില്ല. ദയവായി സ്ക്രീനിൽ പരിശോധിച്ച് ഒപ്പിടുക."
            }

            _lastResponse.value = spokenResponse
            speak(spokenResponse, language)
            return VoiceActionResult.NavigateToPayment(extractedAmount, matchedMerchant, spokenResponse)
        }

        // 2. Balance & Offline Exposure Intent
        val isBalanceIntent = q.contains("balance") || q.contains("बैलेंस") || q.contains("बाकी") ||
                q.contains("ಬ್ಯಾಲೆನ್ಸ್") || q.contains("ಬಾಕಿ") || q.contains("ബാലൻസ്") ||
                q.contains("account") || q.contains("wallet") || q.contains("पैसे") || q.contains("money")

        if (isBalanceIntent) {
            val remainingOffline = (offlineLimit - offlineExposure).coerceAtLeast(0L)
            val spokenResponse = when (language) {
                AppLanguage.ENGLISH -> "Your total wallet balance is ₹${walletBalance.toInt()}. You have ₹$remainingOffline available in offline spending allowance, with ₹$offlineExposure currently in offline exposure."
                AppLanguage.HINDI -> "आपका कुल वॉलेट बैलेंस ₹${walletBalance.toInt()} है। ऑफलाइन खर्च के लिए ₹$remainingOffline उपलब्ध हैं, और वर्तमान में ₹$offlineExposure ऑफलाइन एक्सपोज़र है।"
                AppLanguage.KANNADA -> "ನಿಮ್ಮ ಒಟ್ಟು ವ್ಯಾಲೆಟ್ ಬ್ಯಾಲೆನ್ಸ್ ₹${walletBalance.toInt()}. ಆಫ್‌ಲೈನ್ ಖರ್ಚಿಗೆ ₹$remainingOffline ಲಭ್ಯವಿದೆ, ಮತ್ತು ಪ್ರಸ್ತುತ ₹$offlineExposure ಆಫ್‌ಲೈನ್ ಎಕ್ಸ್‌ಪೋಸರ್ ಇದೆ."
                AppLanguage.MALAYALAM -> "നിങ്ങളുടെ ആകെ വാലറ്റ് ബാലൻസ് ₹${walletBalance.toInt()} ആണ്. ഓഫ്‌ലൈൻ ചെലവഴിക്കലിനായി ₹$remainingOffline ലഭ്യമാണ്, നിലവിൽ ₹$offlineExposure ഓഫ്‌ലൈൻ എക്സ്പോഷർ ഉണ്ട്."
            }

            _lastResponse.value = spokenResponse
            speak(spokenResponse, language)
            return VoiceActionResult.SpokenAnswer(spokenResponse, "View Wallet")
        }

        // 3. Transactions / History Intent
        val isTxIntent = q.contains("transaction") || q.contains("history") || q.contains("recent") ||
                q.contains("लेनदेन") || q.contains("इतिहास") || q.contains("ವಹಿವಾಟು") ||
                q.contains("ಇತಿಹಾಸ") || q.contains("ഇടപാടുകൾ") || q.contains("ചരിത്രം") ||
                q.contains("payments") || q.contains("खर्च")

        if (isTxIntent) {
            val count = recentTransactions.size
            val lastTx = recentTransactions.firstOrNull()
            val spokenResponse = when (language) {
                AppLanguage.ENGLISH -> if (lastTx != null) {
                    "You have $count recorded transactions. Your latest transaction was ₹${lastTx.amount} to ${lastTx.merchantName}, status is ${lastTx.status.name.replace("_", " ")}."
                } else {
                    "You have no recent transactions recorded."
                }
                AppLanguage.HINDI -> if (lastTx != null) {
                    "आपके $count रिकॉर्ड किए गए लेनदेन हैं। आपका नवीनतम लेनदेन ₹${lastTx.amount} का ${lastTx.merchantName} को था।"
                } else {
                    "कोई हालिया लेनदेन दर्ज नहीं है।"
                }
                AppLanguage.KANNADA -> if (lastTx != null) {
                    "ನಿಮ್ಮಲ್ಲಿ $count ವಹಿವಾಟುಗಳು ದಾಖಲಾಗಿವೆ. ಇತ್ತೀಚಿನ ವಹಿವಾಟು ${lastTx.merchantName} ಅವರಿಗೆ ₹${lastTx.amount} ಪಾವತಿಸಲಾಗಿದೆ."
                } else {
                    "ಯಾವುದೇ ಇತ್ತೀಚಿನ ವಹಿವಾಟುಗಳಿಲ್ಲ."
                }
                AppLanguage.MALAYALAM -> if (lastTx != null) {
                    "നിങ്ങൾക്ക് $count ഇടപാടുകൾ രേഖപ്പെടുത്തിയിട്ടുണ്ട്. അവസാന ഇടപാട് ${lastTx.merchantName}-ലേക്ക് ₹${lastTx.amount} ആയിരുന്നു."
                } else {
                    "സമീപകാല ഇടപാടുകളൊന്നുമില്ല."
                }
            }

            _lastResponse.value = spokenResponse
            speak(spokenResponse, language)
            return VoiceActionResult.NavigateToScreen("ACTIVITY", spokenResponse)
        }

        // 4. Sync & Network Intent
        val isSyncIntent = q.contains("sync") || q.contains("reconciliation") || q.contains("सिंक") ||
                q.contains("सुलह") || q.contains("ಸಿಂಕ್") || q.contains("ಹೊಂದಾಣಿಕೆ") ||
                q.contains("സമന്വയം") || q.contains("cloud") || q.contains("offline") || q.contains("online")

        if (isSyncIntent) {
            val statusText = if (isOnline) "Online and connected to Supabase cloud" else "Offline mode with local Room storage"
            val spokenResponse = when (language) {
                AppLanguage.ENGLISH -> "System is currently $statusText. There are $pendingSyncCount transaction(s) pending reconciliation sync."
                AppLanguage.HINDI -> "सिस्टम वर्तमान में $statusText है। $pendingSyncCount लेनदेन सुलह सिंक के लिए लंबित हैं।"
                AppLanguage.KANNADA -> "ಸಿಸ್ಟಂ ಪ್ರಸ್ತುತ $statusText ಆಗಿದೆ. $pendingSyncCount ವಹಿವಾಟುಗಳು ಸಿಂಕ್‌ಗಾಗಿ ಬಾಕಿ ಉಳಿದಿವೆ."
                AppLanguage.MALAYALAM -> "സിസ്റ്റം നിലവിൽ $statusText ആണ്. $pendingSyncCount ഇടപാടുകൾ സിങ്ക് ചെയ്യാനായി കാത്തിരിക്കുന്നു."
            }

            _lastResponse.value = spokenResponse
            speak(spokenResponse, language)
            return VoiceActionResult.NavigateToScreen("ACTIVITY", spokenResponse)
        }

        // 5. Security & Cryptography Intent
        val isSecurityIntent = q.contains("security") || q.contains("crypto") || q.contains("trust") ||
                q.contains("सुरक्षा") || q.contains("क्रिप्टो") || q.contains("भद्रತೆ") ||
                q.contains("ಕ್ರಿಪ್ಟೋ") || q.contains("സുരക്ഷ") || q.contains("ക്രിപ്റ്റോ") ||
                q.contains("risk") || q.contains("fraud")

        if (isSecurityIntent) {
            val spokenResponse = when (language) {
                AppLanguage.ENGLISH -> "TrustPay uses on-device Ed25519 asymmetric cryptography and TrustAgent risk models. All offline transactions are tamper-proof and mathematically verifiable."
                AppLanguage.HINDI -> "ट्रस्टपे डिवाइस पर Ed25519 असममित क्रिप्टोग्राफी और ट्रस्टएजेंट जोखिम मॉडल का उपयोग करता है। सभी ऑफलाइन लेनदेन छेड़छाड़-मुक्त और गणितीय रूप से सत्यापन योग्य हैं।"
                AppLanguage.KANNADA -> "ಟ್ರಸ್ಟ್‌ಪೇ ಸಾಧನದಲ್ಲಿ Ed25519 ಅಸಮ್ಮಿತ ಕ್ರಿಪ್ಟೋಗ್ರಫಿ ಮತ್ತು ಟ್ರಸ್ಟ್‌ಏಜೆಂಟ್ ಮಾದರಿಗಳನ್ನು ಬಳಸುತ್ತದೆ. ಎಲ್ಲಾ ಆಫ್‌ಲೈನ್ ವಹಿವಾಟುಗಳು ಸಂಪೂರ್ಣ ಸುರಕ್ಷಿತವಾಗಿವೆ."
                AppLanguage.MALAYALAM -> "ട്രസ്റ്റ്പേ ഉപകരണത്തിൽ Ed25519 ക്രിപ്റ്റോഗ്രാഫിയും ട്രസ്റ്റ്ഏജന്റ് മോഡലുകളും ഉപയോഗിക്കുന്നു. എല്ലാ ഓഫ്‌ലൈൻ ഇടപാടുകളും മാറ്റമില്ലാത്തതും സുരക്ഷിതവുമാണ്."
            }

            _lastResponse.value = spokenResponse
            speak(spokenResponse, language)
            return VoiceActionResult.NavigateToScreen("SECURITY", spokenResponse)
        }

        // 6. Fallback General Assistant Answer
        val fallbackResponse = when (language) {
            AppLanguage.ENGLISH -> "I can help you check balance, list transactions, check sync status, or prepare payments to merchants like Artisan Roasters."
            AppLanguage.HINDI -> "मैं बैलेंस जांचने, हाल के लेनदेन देखने, सिंक स्थिति जांचने या व्यापारियों को भुगतान तैयार करने में मदद कर सकता हूँ।"
            AppLanguage.KANNADA -> "ನಾನು ನಿಮ್ಮ ಬ್ಯಾಲೆನ್ಸ್, ಇತ್ತೀಚಿನ ವಹಿವಾಟುಗಳು, ಸಿಂಕ್ ಸ್ಥಿತಿಯನ್ನು ಪರಿಶೀಲಿಸಲು ಅಥವಾ ಪಾವತಿಯನ್ನು ಸಿದ್ಧಪಡಿಸಲು ಸಹಾಯ ಮಾಡಬಲ್ಲೆ."
            AppLanguage.MALAYALAM -> "ബാലൻസ് പരിശോധിക്കാനും, ഇടപാടുകൾ കാണാനും, സിങ്ക് നില അറിയാനും, പേയ്‌മെന്റുകൾ തയ്യാറാക്കാനും എനിക്ക് സഹായിക്കാനാകും."
        }

        _lastResponse.value = fallbackResponse
        speak(fallbackResponse, language)
        return VoiceActionResult.SpokenAnswer(fallbackResponse)
    }

    /**
     * Sends user recognized speech to Gemini with full live user state context.
     * STRICT DECISION ISOLATION: Never authorizes payments directly.
     */
    suspend fun processQueryWithGemini(
        query: String,
        language: AppLanguage,
        walletBalance: Double,
        offlineExposure: Long,
        offlineLimit: Long,
        recentTransactions: List<Transaction>,
        mandateReference: String?,
        mandateStatus: String,
        isOnline: Boolean,
        pendingSyncCount: Int,
        merchantsList: List<Merchant>
    ): VoiceActionResult {
        val q = query.trim().lowercase(Locale.ROOT)
        _transcription.value = query

        // 1. Payment Intent Parsing (e.g., "Pay 200 to Artisan Roasters")
        val isPaymentIntent = q.contains("pay") || q.contains("send") || q.contains("भुगतान") ||
                q.contains("पैसे") || q.contains("ಪಾವತಿ") || q.contains("ಕಳುಹಿಸಿ") ||
                q.contains("പണം") || q.contains("അടയ്ക്കുക") || q.contains("നൽകുക")

        if (isPaymentIntent) {
            val numberRegex = Regex("""(\d+(\.\d+)?)""")
            val match = numberRegex.find(q)
            val extractedAmount = match?.value?.toDoubleOrNull() ?: 250.0

            val matchedMerchant = merchantsList.firstOrNull { merchant ->
                val nameLower = merchant.businessName.lowercase(Locale.ROOT)
                val words = nameLower.split(" ")
                words.any { word -> word.length > 3 && q.contains(word) }
            } ?: merchantsList.firstOrNull() ?: Merchant("merch_01", "Artisan Roasters", "Indiranagar, Bangalore", "Coffee & Bakery", 8, 1420)

            val spokenResponse = when (language) {
                AppLanguage.ENGLISH -> "Preparing payment draft of ₹${extractedAmount.toInt()} to ${matchedMerchant.businessName}. For your security, voice commands cannot authorize payments directly. Please review and sign on screen."
                AppLanguage.HINDI -> "${matchedMerchant.businessName} को ₹${extractedAmount.toInt()} का ड्राफ्ट तैयार किया जा रहा है। सुरक्षा के लिए, वॉयस कमांड सीधे भुगतान अधिकृत नहीं कर सकते। स्क्रीन पर पुष्टि करें।"
                AppLanguage.KANNADA -> "${matchedMerchant.businessName} ಅವರಿಗೆ ₹${extractedAmount.toInt()} ಪಾವತಿಯನ್ನು ಸಿದ್ಧಪಡಿಸಲಾಗುತ್ತಿದೆ. ಭದ್ರತೆಗಾಗಿ ಧ್ವನಿ ಆಜ್ಞೆಗಳು ಪಾವತಿಯನ್ನು ನೇರವಾಗಿ ದೃಢೀಕರಿಸುವುದಿಲ್ಲ. ಪರದೆಯ ಮೇಲೆ ಪರಿಶೀಲಿಸಿ."
                AppLanguage.MALAYALAM -> "${matchedMerchant.businessName}-ലേക്ക് ₹${extractedAmount.toInt()} ഡ്രാഫ്റ്റ് തയ്യാറാക്കുന്നു. വോയ്‌സ് കമാൻഡുകൾ വഴി പണമടയ്ക്കാനാവില്ല. സ്ക്രീനിൽ പരിശോധിച്ച് ഒപ്പിടുക."
            }

            _lastResponse.value = spokenResponse
            speak(spokenResponse, language)
            return VoiceActionResult.NavigateToPayment(extractedAmount, matchedMerchant, spokenResponse)
        }

        // 2. Open-ended questions -> Gemini Explainability Service with real user context
        val langName = when (language) {
            AppLanguage.ENGLISH -> "English"
            AppLanguage.HINDI -> "Hindi"
            AppLanguage.KANNADA -> "Kannada"
            AppLanguage.MALAYALAM -> "Malayalam"
        }

        val geminiAnswer = GeminiExplainabilityService.answerVoiceQueryWithContext(
            query = query,
            languageName = langName,
            walletBalance = walletBalance,
            offlineExposure = offlineExposure,
            offlineLimit = offlineLimit,
            recentTransactions = recentTransactions,
            mandateReference = mandateReference,
            mandateStatus = mandateStatus,
            isNetworkOnline = isOnline,
            pendingSyncCount = pendingSyncCount
        )

        _lastResponse.value = geminiAnswer
        speak(geminiAnswer, language)

        val suggestedAction = when {
            q.contains("balance") || q.contains("wallet") -> "View Wallet"
            q.contains("decline") || q.contains("fraud") || q.contains("risk") -> "View Security Center"
            else -> "View Activity"
        }

        return VoiceActionResult.GeminiSpokenAnswer(geminiAnswer, suggestedAction)
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (e: Exception) {
            Log.e("VoiceAssistantEngine", "Error shutting down voice assistant: ${e.message}")
        }
    }
}
