package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.filled.Refresh
import com.example.data.model.Transaction
import com.example.engine.FraudDetector
import com.example.engine.MlEvaluationResult
import com.example.ui.theme.LocalAppColors
import com.example.util.LocalAppLanguage
import com.example.util.LocalAppStrings
import com.example.ui.components.ShimmerBox

@Composable
fun SecurityCenterScreen(
    transaction: Transaction,
    isRestricted: Boolean,
    onToggleRestriction: () -> Unit,
    geminiExplanation: String?,
    isGeminiLoading: Boolean,
    onAskGemini: (String) -> Unit,
    onBack: () -> Unit,
    mlEvaluation: MlEvaluationResult? = null,
    isMlLoading: Boolean = false,
    onRetryMlEvaluation: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val colors = LocalAppColors.current
    var customQueryInput by remember { mutableStateOf("") }

    LaunchedEffect(transaction.transactionId) {
        onRetryMlEvaluation?.invoke()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.onSurface)
                }
                Column {
                    Text(
                        text = strings.securityCenter,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.primary
                    )
                    Text(
                        text = "${strings.anomalyInvestigationTitle}: ${transaction.transactionId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }

        // Risk Meter & Score Card with Dual Rule-Based + Deployed ML Model Scoring
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "₹${transaction.amount}",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 38.sp
                        ),
                        color = colors.primary
                    )
                    Text(
                        text = "Buyer: ${transaction.buyerName} • Merchant: ${transaction.merchantName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Gradient Risk Meter Bar
                    Text(
                        text = strings.riskSpectrumTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        colors.secondary,
                                        colors.warning,
                                        colors.error
                                    )
                                )
                            )
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(strings.riskSpectrumLow, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                        Text(strings.riskSpectrumMedium, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                        Text(strings.riskSpectrumCritical, style = MaterialTheme.typography.labelSmall, color = colors.error)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- DUAL SCORING EVALUATION SECTION ---
                    Text(
                        text = strings.dualRiskAnalysisTitle,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // 1. Rule-Based Score Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = strings.ruleBasedScoreTitle,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = colors.primary
                                    )
                                    Text(
                                        text = strings.ruleBasedScoreSubtitle,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = colors.onSurfaceVariant
                                    )
                                }
                                val rulePct = (transaction.fraudProbability * 100).toInt()
                                val ruleSev = if (rulePct >= 70) "HIGH RISK" else if (rulePct >= 30) "MEDIUM RISK" else "LOW RISK"
                                val ruleColor = if (rulePct >= 70) colors.error else if (rulePct >= 30) colors.warning else colors.secondary
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(ruleColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "$ruleSev $rulePct%",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = ruleColor
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. ML Model Score Card (Remote XGBoost Microservice)
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (mlEvaluation?.isAvailable == false) colors.warningContainer.copy(alpha = 0.15f) else colors.surface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = if (mlEvaluation?.isAvailable == false) colors.warning else colors.outlineVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = strings.mlModelScoreTitle,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = colors.primary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(colors.primaryContainer)
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "XGBoost",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                color = colors.primary
                                            )
                                        }
                                    }
                                    Text(
                                        text = strings.mlModelScoreSubtitle,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = colors.onSurfaceVariant
                                    )
                                }

                                if (isMlLoading) {
                                    ShimmerBox(
                                        modifier = Modifier.size(width = 90.dp, height = 24.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                } else if (mlEvaluation != null && mlEvaluation.isAvailable) {
                                    val mlProb = mlEvaluation.fraudProbability ?: 0f
                                    val mlPctStr = String.format(java.util.Locale.US, "%.1f%%", mlProb * 100f)
                                    val mlRiskLvl = mlEvaluation.riskLevel ?: "UNKNOWN"
                                    val mlColor = if (mlRiskLvl.equals("HIGH", ignoreCase = true) || mlProb >= 0.7f) colors.error
                                    else if (mlRiskLvl.equals("MEDIUM", ignoreCase = true) || mlProb >= 0.3f) colors.warning
                                    else colors.secondary

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(mlColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "$mlRiskLvl $mlPctStr",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = mlColor
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(colors.warningContainer)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "OFFLINE",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = colors.warning
                                        )
                                    }
                                }
                            }

                            // If ML Service is unavailable or timed out, display explicit fallback banner
                            if (!isMlLoading && (mlEvaluation == null || !mlEvaluation.isAvailable)) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = colors.warning,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = strings.mlServiceUnavailable,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = colors.warning
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = strings.mlFallbackNotice,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = colors.onSurfaceVariant
                                )
                                if (onRetryMlEvaluation != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = onRetryMlEvaluation,
                                        modifier = Modifier.height(30.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(strings.retryMlServiceButton, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // "Why this was flagged" Reasons List (matches Image 17!)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = strings.whyFlaggedTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val reasons = transaction.fraudReasons.ifEmpty {
                        listOf(
                            "Transaction amount is 12x customer's average (Historical avg: ₹2,400)",
                            "Velocity Anomaly: 5 transactions occurred within 8 minutes",
                            "Offline Exposure: ₹450 pending offline exposure",
                            "Sync Delay: Last sync was 18 hours ago"
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        reasons.forEach { reason ->
                            Row(verticalAlignment = Alignment.Top) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(colors.errorContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = colors.error,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = reason,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = colors.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Gemini AI Assistant Explainability (Priority 10)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
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
                                    tint = colors.secondaryFixedDim,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = strings.investigateWithGeminiTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.primary
                                )
                                Text(
                                    text = strings.geminiModelSubtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Suggested Questions
                    Text(strings.suggestedQuestionsTitle, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(
                            listOf(
                                strings.qWhyFlagged,
                                strings.qIsDeviceCompromised,
                                strings.qRecommendedActions
                            )
                        ) { q ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.surfaceContainer)
                                    .clickable { onAskGemini(q) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = q,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.primary,
                                    softWrap = false,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }


                    Spacer(modifier = Modifier.height(14.dp))

                    // Custom Question Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customQueryInput,
                            onValueChange = { customQueryInput = it },
                            placeholder = { Text("Ask compliance question...", fontSize = 13.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (customQueryInput.isNotBlank()) {
                                    onAskGemini(customQueryInput)
                                    customQueryInput = ""
                                }
                            },
                            enabled = !isGeminiLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(52.dp)
                        ) {
                            if (isGeminiLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(18.dp), tint = colors.onPrimary)
                            }
                        }
                    }

                    // Explanation Output Box
                    if (geminiExplanation != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = colors.surfaceContainer),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = colors.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "AI Compliance Findings",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = colors.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = geminiExplanation,
                                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                                    color = colors.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Operational Actions: Restrict Future Offline Payments (Image 17)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onToggleRestriction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRestricted) colors.error else colors.primary,
                        contentColor = colors.onPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("restrict_offline_payments_button")
                ) {
                    Icon(
                        imageVector = if (isRestricted) Icons.Default.Check else Icons.Default.Block,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRestricted) "Remove Offline Restriction" else "Restrict Future Offline Payments",
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Return to Dashboard", color = colors.primary)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
