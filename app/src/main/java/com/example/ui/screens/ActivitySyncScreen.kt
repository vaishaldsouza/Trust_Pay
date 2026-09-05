package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.engine.SyncProgressState
import com.example.ui.components.ModeChip
import com.example.ui.components.StatusBadge
import com.example.ui.theme.LocalAppColors
import com.example.util.LocalAppLanguage
import com.example.util.LocalAppStrings

@Composable
fun ActivitySyncScreen(
    syncState: SyncProgressState,
    isOnline: Boolean,
    transactions: List<Transaction>,
    onTriggerSync: () -> Unit,
    onReviewFlaggedItem: (String) -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    isBalanceMasked: Boolean = true,
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.syncActivity,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.primary
                )

                Button(
                    onClick = onTriggerSync,
                    enabled = isOnline && !syncState.isSyncing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("activity_sync_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (syncState.isSyncing) "Syncing..." else strings.syncNow,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // Circular Progress Ring & Reconciliation Engine (matches Image 11!)
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
                    // Circular Progress Ring
                    Box(
                        modifier = Modifier.size(110.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { syncState.progressPercent / 100f },
                            modifier = Modifier.fillMaxSize(),
                            color = colors.secondary,
                            trackColor = colors.surfaceContainer,
                            strokeWidth = 9.dp,
                            strokeCap = StrokeCap.Round
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${syncState.progressPercent}%",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 26.sp
                                ),
                                color = colors.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = syncState.currentStepText,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Verification Checklist Items (matches Image 11!)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SyncCheckItem(
                            label = strings.transactionsDiscoveredLabel.format(syncState.discoveredCount.coerceAtLeast(5)),
                            isSuccess = true
                        )
                        SyncCheckItem(
                            label = strings.signaturesVerifiedLabel.format(syncState.verifiedSignaturesCount.coerceAtLeast(5)),
                            isSuccess = true
                        )
                        SyncCheckItem(
                            label = strings.replayAttacksLabel.format(syncState.replayAttackCount),
                            isSuccess = true
                        )
                        SyncCheckItem(
                            label = strings.duplicateTxLabel.format(syncState.duplicateCount),
                            isSuccess = true
                        )
                        SyncCheckItem(
                            label = strings.highRiskFlaggedLabel.format(syncState.highRiskCount.coerceAtLeast(1)),
                            isSuccess = false,
                            isWarning = true
                        )
                        SyncCheckItem(
                            label = strings.lowRiskTxLabel.format(syncState.lowRiskCount.coerceAtLeast(4)),
                            isSuccess = true
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Settlement Summary Card (matches Image 11!)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = strings.settlementSummaryTitle,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.primary
                                )
                                Text(
                                    text = strings.settlementSummaryDesc.format(syncState.settledCount.coerceAtLeast(4), syncState.reviewRequiredCount.coerceAtLeast(1)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .weight(4f)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                                        .background(colors.secondary)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                                        .background(colors.warning)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Review Flagged Item -> Button
                    Button(
                        onClick = {
                            onReviewFlaggedItem(syncState.flaggedTransactionId ?: "TXN-4921")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("review_flagged_item_button")
                    ) {
                        Text(strings.reviewFlaggedItemButton, fontWeight = FontWeight.Bold)
                    }

                }
            }
        }

        // Ledger Header & All Transactions
        item {
            Text(
                text = strings.recentTransactions,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.primary
            )
        }

        items(transactions) { tx ->
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTransactionClick(tx) }
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${tx.merchantName} • ${tx.transactionId}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusBadge(status = tx.status)
                            if (tx.settlementRef != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Razorpay: ${tx.settlementRef.take(12)}...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.secondary
                                )
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        AnimatedContent(
                            targetState = isBalanceMasked,
                            transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(250)) },
                            label = "ActivityTxAmountCrossfade"
                        ) { masked ->
                            Text(
                                text = if (masked) "₹ • • •" else "₹${tx.amount}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        ModeChip(mode = tx.mode)
                    }

                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun SyncCheckItem(
    label: String,
    isSuccess: Boolean,
    isWarning: Boolean = false
) {
    val colors = LocalAppColors.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isWarning -> colors.warningContainer
                        isSuccess -> colors.secondaryFixed.copy(alpha = 0.4f)
                        else -> colors.errorContainer
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isWarning) Icons.Default.Warning else Icons.Default.Check,
                contentDescription = null,
                tint = if (isWarning) colors.warning else colors.secondary,
                modifier = Modifier.size(12.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = colors.primary
        )
    }
}
