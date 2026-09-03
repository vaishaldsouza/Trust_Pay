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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.engine.FraudDetector
import com.example.ui.theme.LocalAppColors
import com.example.util.LocalAppLanguage
import com.example.util.LocalAppStrings

@Composable
fun SecurityCenterScreen(
    transaction: Transaction,
    isRestricted: Boolean,
    onToggleRestriction: () -> Unit,
    geminiExplanation: String?,
    isGeminiLoading: Boolean,
    onAskGemini: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val colors = LocalAppColors.current
    var customQueryInput by remember { mutableStateOf("") }

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
                        text = "Anomaly Investigation: ${transaction.transactionId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }

        // Risk Meter & Score Card (matches Image 17!)
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
                        Text(
                            text = "Analysis",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.onSurfaceVariant
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.errorContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "HIGH RISK ${(transaction.fraudProbability * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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
                        text = "Risk Probability Spectrum",
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
                        Text("0% (Low)", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                        Text("50% (Medium)", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                        Text("100% (Critical)", style = MaterialTheme.typography.labelSmall, color = colors.error)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = FraudDetector.MODEL_LABEL,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = colors.onSurfaceVariant
                    )
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
                        text = "Why this was flagged",
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
                                    text = "Investigate with Gemini AI",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.primary
                                )
                                Text(
                                    text = "Model: gemini-2.5-flash • Explainability Only",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Suggested Questions
                    Text("Suggested Questions:", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            "Why was this flagged?",
                            "Is device compromised?",
                            "Recommended actions"
                        ).forEach { q ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.surfaceContainer)
                                    .clickable { onAskGemini(q) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = q,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = colors.primary
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
