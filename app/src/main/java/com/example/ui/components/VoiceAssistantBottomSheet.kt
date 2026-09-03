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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onToggleListening: () -> Unit,
    onPlayAudio: () -> Unit,
    onStopAudio: () -> Unit,
    onExecuteSampleQuery: (String) -> Unit,
    onNavigateToPaymentConfirmed: (Double, String) -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    if (!isOpen) return

    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current
    val colors = LocalAppColors.current

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
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .padding(bottom = 32.dp),
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
                            .background(colors.secondaryFixed.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = colors.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = strings.voiceAssistantSection,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.primary
                        )
                        Text(
                            text = "${language.flag} ${language.nativeName} (${language.displayName})",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant
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

            Spacer(modifier = Modifier.height(20.dp))

            // Pulsating Mic Visualizer
            Box(
                modifier = Modifier
                    .size(110.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer glow ripple
                if (isListening || isSpeaking) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                if (isListening) colors.secondaryFixedDim.copy(alpha = 0.35f)
                                else colors.primaryContainer.copy(alpha = 0.25f)
                            )
                    )
                }

                // Inner Mic Button
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            if (isListening) colors.secondary
                            else colors.primary
                        )
                        .clickable { onToggleListening() }
                        .testTag("voice_assistant_mic_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Mic else Icons.Default.Mic,
                        contentDescription = "Microphone",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = when {
                    isListening -> strings.voiceListening
                    isSpeaking -> strings.voiceSpeaking
                    else -> strings.voiceTapToSpeak
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = if (isListening) colors.secondary else colors.onSurfaceVariant
            )

            // Transcription Box
            if (transcription.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "“$transcription”",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = colors.primary,
                        modifier = Modifier.padding(14.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Spoken Response Box
            if (lastResponse.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, colors.secondary.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TrustPay Voice Response",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.secondary
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSpeaking) {
                                    IconButton(
                                        onClick = onStopAudio,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Stop,
                                            contentDescription = strings.voiceStopAudio,
                                            tint = colors.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else {
                                    IconButton(
                                        onClick = onPlayAudio,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = strings.voicePlayAudio,
                                            tint = colors.secondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = lastResponse,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                            color = colors.primary
                        )

                        // If the voice result triggered payment navigation, show action button
                        if (lastActionResult is VoiceActionResult.NavigateToPayment) {
                            Spacer(modifier = Modifier.height(14.dp))
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
                                    .height(44.dp)
                                    .testTag("voice_confirm_payment_nav_button")
                            ) {
                                Text(
                                    text = "Review & Sign Payment (₹${lastActionResult.amount.toInt()})",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Prompt Suggestion Chips in User's Language
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = strings.voiceTryAsking,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                val samplePrompts = when (language) {
                    AppLanguage.ENGLISH -> listOf(
                        "What is my balance?",
                        "Show recent transactions",
                        "Is sync pending?",
                        "Pay ₹250 to Artisan Roasters",
                        "Check offline exposure"
                    )
                    AppLanguage.HINDI -> listOf(
                        "मेरा बैलेंस क्या है?",
                        "हाल के लेनदेन दिखाएं",
                        "क्या सिंक लंबित है?",
                        "आर्टिसन रोस्टर्स को ₹250 का भुगतान करें",
                        "ऑफलाइन एक्सपोज़र जांचें"
                    )
                    AppLanguage.KANNADA -> listOf(
                        "ನನ್ನ ಬ್ಯಾಲೆನ್ಸ್ ಎಷ್ಟು?",
                        "ಇತ್ತೀಚಿನ ವಹಿವಾಟುಗಳನ್ನು ತೋರಿಸಿ",
                        "ಸಿಂಕ್ ಬಾಕಿ ಇದೆಯೇ?",
                        "ಆರ್ಟಿಸನ್ ರೋಸ್ಟರ್ಸ್‌ಗೆ ₹250 ಪಾವತಿಸಿ",
                        "ಆಫ್‌ಲೈನ್ ಎಕ್ಸ್‌ಪೋಸರ್ ಪರಿಶೀಲಿಸಿ"
                    )
                    AppLanguage.MALAYALAM -> listOf(
                        "എന്റെ ബാലൻസ് എത്രയാണ്?",
                        "സമീപകാല ഇടപാടുകൾ കാണിക്കുക",
                        "സമന്വയം ബാക്കിയുണ്ടോ?",
                        "ആർട്ടിസാൻ റോസ്റ്റേഴ്സിന് ₹250 അടയ്ക്കുക",
                        "ഓഫ്‌ലൈൻ പരിധി പരിശോധിക്കുക"
                    )
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    samplePrompts.forEach { prompt ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(colors.surfaceContainer)
                                .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                .clickable { onExecuteSampleQuery(prompt) }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = prompt,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = colors.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
