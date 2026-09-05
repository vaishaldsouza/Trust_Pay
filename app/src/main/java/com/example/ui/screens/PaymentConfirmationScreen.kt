package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.data.model.TransactionMode
import com.example.data.model.TransactionStatus
import com.example.ui.components.ModeChip
import com.example.ui.components.StatusBadge
import com.example.ui.theme.LocalAppColors
import com.example.util.LocalAppLanguage
import com.example.util.LocalAppStrings

@Composable
fun PaymentConfirmationScreen(
    transaction: Transaction,
    onForceSync: () -> Unit,
    onVoidTransaction: () -> Unit,
    onBack: () -> Unit,
    onReplaySoundwave: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val colors = LocalAppColors.current

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
                Text(
                    text = strings.paymentDetails,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.primary
                )
            }
        }

        // Amount & Mode Card (Image 13)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = transaction.transactionId,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.onSurfaceVariant,
                            softWrap = true,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        ModeChip(mode = transaction.mode)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "₹${transaction.amount}",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 44.sp
                        ),
                        color = colors.primary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${strings.payToMerchant} ${transaction.merchantName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (transaction.status == TransactionStatus.SETTLED) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.secondaryFixed.copy(alpha = 0.35f))
                                .padding(vertical = 10.dp, horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "🟢 Payment Settled Online",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.primary
                                )
                                Text(
                                    text = "Settled via Razorpay Flow • Ref: ${transaction.settlementRef ?: "pay_rzp_online"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }
                    } else if (transaction.status == TransactionStatus.SETTLEMENT_PENDING) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.warningContainer.copy(alpha = 0.6f))
                                .border(1.dp, colors.warning.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .padding(vertical = 12.dp, horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "⏳ Settlement Pending",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.warning
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Razorpay Order ID: ${transaction.settlementRef ?: "order_registered"}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.primary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Awaiting mandate confirmation — will settle automatically once Razorpay processes the recurring charge webhook.",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.surfaceContainer)
                                .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(vertical = 10.dp, horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "🟠 Payment Accepted Offline",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.primary
                                )
                                Text(
                                    text = "\"Will settle when connection returns\"",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.secondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Signature verification badge (Image 13)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (transaction.isTampered) colors.error.copy(alpha = 0.15f)
                                else colors.secondaryFixed.copy(alpha = 0.35f)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = if (transaction.isTampered) colors.error else colors.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (transaction.isTampered) "Signature: INVALID" else "Signature: VALID",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (transaction.isTampered) colors.error else colors.secondary
                        )
                    }
                }
            }
        }

        // Status Stepper (matches Image 13!)
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
                        text = "Transaction Lifecycle",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val steps = listOf(
                        StepItem("Created", "Nonce generated & payload prepared", isCompleted = true, isInProgress = false),
                        StepItem("Signed", "Cryptographic key applied locally", isCompleted = true, isInProgress = false),
                        StepItem("Offline Accepted", "Bounded limits verified by Trust Agent", isCompleted = true, isInProgress = false),
                        StepItem(
                            "Pending Settlement",
                            if (transaction.status == TransactionStatus.SETTLED) "Order settled"
                            else if (transaction.status == TransactionStatus.SETTLEMENT_PENDING) "Razorpay Order ${transaction.settlementRef ?: ""} registered • Awaiting webhook capture"
                            else "Retrying in 2:45 (Awaiting reconnect)",
                            isCompleted = transaction.status == TransactionStatus.SETTLED,
                            isInProgress = transaction.status == TransactionStatus.SETTLEMENT_PENDING || transaction.status == TransactionStatus.PENDING_SYNC || transaction.status == TransactionStatus.OFFLINE_ACCEPTED
                        ),
                        StepItem(
                            "Settled",
                            if (transaction.status == TransactionStatus.SETTLED) "Razorpay payment captured: ${transaction.settlementRef ?: "MND-9823-XYZ"}" else "Final reconciliation with Razorpay",
                            isCompleted = transaction.status == TransactionStatus.SETTLED,
                            isInProgress = false
                        )
                    )

                    steps.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                step.isCompleted -> colors.secondary
                                                step.isInProgress -> colors.warning
                                                else -> colors.surfaceContainer
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (step.isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    } else if (step.isInProgress) {
                                        Icon(
                                            imageVector = Icons.Default.HourglassEmpty,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                if (index < steps.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(28.dp)
                                            .background(
                                                if (step.isCompleted) colors.secondary
                                                else colors.surfaceContainer
                                            )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = step.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (step.isCompleted || step.isInProgress) colors.primary else colors.onSurfaceVariant,
                                    softWrap = true
                                )
                                Text(
                                    text = step.subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.onSurfaceVariant,
                                    softWrap = true
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons: Force Sync Attempt & Void Transaction (matches Image 13)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onForceSync,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("force_sync_button")
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.syncNow, fontWeight = FontWeight.Bold)
                }

                if (transaction.mode == TransactionMode.OFFLINE_VALUE || transaction.transactionId.contains("soundwave", ignoreCase = true)) {
                    var isReplayingWave by remember { mutableStateOf(false) }
                    AnimatedSoundwaveGraphic(
                        isPlaying = isReplayingWave,
                        waveColor = colors.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            isReplayingWave = true
                            onReplaySoundwave()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("replay_soundwave_button")
                    ) {
                        Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Replay Soundwave Pulse", fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = onVoidTransaction,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("void_transaction_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Void Transaction")
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

private data class StepItem(
    val title: String,
    val subtitle: String,
    val isCompleted: Boolean,
    val isInProgress: Boolean
)
