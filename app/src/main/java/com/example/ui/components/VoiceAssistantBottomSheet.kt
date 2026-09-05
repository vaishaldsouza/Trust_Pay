package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.ChatMessage
import com.example.engine.ChatSender
import com.example.engine.VoiceActionResult
import com.example.ui.theme.LocalAppColors
import com.example.util.AppLanguage
import com.example.util.LocalAppLanguage
import com.example.util.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VoiceAssistantBottomSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    isListening: Boolean,
    isSpeaking: Boolean,
    transcription: String,
    lastResponse: String,
    lastActionResult: VoiceActionResult?,
    chatMessages: List<ChatMessage> = emptyList(),
    isThinking: Boolean = false,
    onSendTextMessage: (String) -> Unit = {},
    onClearChat: () -> Unit = {},
    onToggleListening: () -> Unit,
    onPlayAudio: () -> Unit,
    onStopAudio: () -> Unit,
    onExecuteSampleQuery: (String) -> Unit,
    onNavigateToPaymentConfirmed: (Double, String) -> Unit,
    hasAudioPermission: Boolean = true,
    onRequestAudioPermission: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    if (!isOpen) return

    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current
    val colors = LocalAppColors.current

    var typedInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(chatMessages.size, isThinking) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surfaceLowest,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier.testTag("voice_assistant_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = colors.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "TrustPay Conversational AI",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.primary
                        )
                        Text(
                            text = "${language.flag} ${language.nativeName} (${language.displayName}) • Read-Only",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (chatMessages.isNotEmpty()) {
                        IconButton(
                            onClick = onClearChat,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear Chat",
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Conversation Thread Container
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                if (chatMessages.isEmpty() && !isThinking && transcription.isBlank()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = colors.primary.copy(alpha = 0.6f),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Ask anything about your balance, allowance, or transactions",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(chatMessages, key = { it.id }) { message ->
                            ChatBubble(
                                message = message,
                                colors = colors,
                                isSpeaking = isSpeaking,
                                onPlayAudio = onPlayAudio,
                                onStopAudio = onStopAudio
                            )
                        }

                        if (isThinking) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = colors.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Gemini AI is thinking...",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = colors.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action payment navigation prompt if intent matched
            if (lastActionResult is VoiceActionResult.NavigateToPayment) {
                Button(
                    onClick = {
                        onNavigateToPaymentConfirmed(lastActionResult.amount, lastActionResult.merchant.merchantId)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("voice_confirm_payment_nav_button")
                ) {
                    Text(
                        text = "Review & Sign Payment (₹${lastActionResult.amount.toInt()})",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Input Row: Mic Button + OutlinedTextField + Send Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Mic Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .scale(if (isListening) pulseScale else 1f)
                        .clip(CircleShape)
                        .background(
                            if (isListening) colors.secondary
                            else if (!hasAudioPermission) colors.errorContainer
                            else colors.primaryContainer
                        )
                        .clickable {
                            if (!hasAudioPermission) {
                                onRequestAudioPermission()
                            } else {
                                onToggleListening()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (!hasAudioPermission) Icons.Default.MicOff
                        else if (isListening) Icons.Default.Mic
                        else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (isListening) colors.onSecondary
                        else if (!hasAudioPermission) colors.error
                        else colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Text Input Field
                OutlinedTextField(
                    value = typedInput,
                    onValueChange = { typedInput = it },
                    placeholder = {
                        Text(
                            text = if (isListening) "Listening to speech..." else "Type your question...",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.outlineVariant
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Send Button
                IconButton(
                    onClick = {
                        if (typedInput.isNotBlank()) {
                            val msg = typedInput
                            typedInput = ""
                            onSendTextMessage(msg)
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(colors.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = colors.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Prompt Suggestion Chips in User's Language
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = strings.voiceTryAsking,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                val samplePrompts = when (language) {
                    AppLanguage.ENGLISH -> listOf(
                        "How much can I spend offline today?",
                        "Why was my last payment declined?",
                        "What is my mandate status?",
                        "Pay ₹250 to Artisan Roasters",
                        "Show recent transactions"
                    )
                    AppLanguage.HINDI -> listOf(
                        "आज मैं कितना ऑफलाइन खर्च कर सकता हूँ?",
                        "मेरा पिछला भुगतान क्यों अस्वीकार हुआ?",
                        "मैंडेट स्थिति क्या है?",
                        "आर्टिसन रोस्टर्स को ₹250 का भुगतान करें",
                        "हाल के लेनदेन दिखाएं"
                    )
                    AppLanguage.KANNADA -> listOf(
                        "ಇಂದು ಎಷ್ಟು ಆಫ್‌ಲೈನ್ ಖರ್ಚು ಮಾಡಬಹುದು?",
                        "ನನ್ನ ಕೊನೆಯ ಪಾವತಿ ಏಕೆ ತಿರಸ್ಕೃತವಾಯಿತು?",
                        "ಮ್ಯಾಂಡೇಟ್ ಸ್ಥಿತಿ ಏನು?",
                        "ಆರ್ಟಿಸನ್ ರೋಸ್ಟರ್ಸ್‌ಗೆ ₹250 ಪಾವತಿಸಿ",
                        "ಇತ್ತೀಚಿನ ವಹಿವಾಟುಗಳನ್ನು ತೋರಿಸಿ"
                    )
                    AppLanguage.MALAYALAM -> listOf(
                        "ഇന്ന് എത്ര ഓഫ്‌ലൈനായി ചെലവഴിക്കാം?",
                        "എന്റെ അവസാന പേയ്‌മെന്റ് നിരസിച്ചത് എന്തുകൊണ്ട്?",
                        "മാൻഡേറ്റ് നില എന്താണ്?",
                        "ആർട്ടിസാൻ റോസ്റ്റേഴ്സിന് ₹250 അടയ്ക്കുക",
                        "സമീപകാല ഇടപാടുകൾ കാണിക്കുക"
                    )
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(samplePrompts) { prompt ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.surface)
                                .border(1.dp, colors.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                .clickable { onSendTextMessage(prompt) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = prompt,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = colors.primary,
                                softWrap = true,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    colors: com.example.ui.theme.AppColors,
    isSpeaking: Boolean,
    onPlayAudio: () -> Unit,
    onStopAudio: () -> Unit
) {
    val isUser = message.sender == ChatSender.USER_TEXT || message.sender == ChatSender.USER_VOICE

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            if (!isUser) {
                val labelText = if (message.isError) {
                    "🌐 Network Required"
                } else if (message.isLocalAnswer) {
                    "⚡ Instant Local Response"
                } else {
                    "✨ Powered by Gemini"
                }
                val labelColor = if (message.isError) colors.error else if (message.isLocalAnswer) colors.primary else colors.secondary

                Text(
                    text = labelText,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = labelColor
                )
            } else {
                Text(
                    text = if (message.sender == ChatSender.USER_VOICE) "🎤 Voice Input" else "💬 Typed Question",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = colors.onSurfaceVariant
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (message.isError) colors.errorContainer
                    else if (isUser) colors.primary
                    else colors.surfaceLowest
                )
                .border(
                    width = 1.dp,
                    color = if (message.isError) colors.error else colors.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 19.sp),
                    color = if (message.isError) colors.error else if (isUser) colors.onPrimary else colors.primary
                )

                if (!isUser && !message.isError) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = if (isSpeaking) onStopAudio else onPlayAudio,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = "TTS",
                                tint = colors.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
