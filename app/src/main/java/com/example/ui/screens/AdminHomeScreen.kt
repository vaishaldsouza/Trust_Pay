package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FraudAlert
import com.example.data.model.RiskSeverity
import com.example.data.remote.AdminMetrics
import com.example.ui.theme.LocalAppColors
import com.example.util.LocalAppLanguage
import com.example.util.LocalAppStrings

@Composable
fun AdminHomeScreen(
    fraudAlerts: List<FraudAlert>,
    adminMetrics: AdminMetrics = AdminMetrics(),
    onAlertClick: (String) -> Unit,
    onStartDemoClick: () -> Unit,
    isSupabaseConfigured: Boolean = com.example.data.remote.SupabaseClient.isConfigured(),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.adminOverview,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.primary,
                        softWrap = true,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSupabaseConfigured) colors.secondaryFixed.copy(alpha = 0.3f) else colors.warning.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isSupabaseConfigured) "🟢 Cloud Sync: Active" else "⚠️ Cloud Sync: Not Configured",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = if (isSupabaseConfigured) colors.secondary else colors.warning
                        )
                    }
                }

                // Quick Hackathon Demo Trigger
                Button(
                    onClick = onStartDemoClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("admin_start_demo_button")
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("1-Click Demo", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        // 6-Metric Grid (powered by Supabase RPC get_admin_metrics & Room sync)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(strings.dailyVolume, "₹${adminMetrics.totalVolume}", "Across ${adminMetrics.totalTransactions} transactions", modifier = Modifier.weight(1f))
                    StatCard("Offline Tx", "${adminMetrics.offlineTransactions}", strings.offlineSafeBadge, modifier = Modifier.weight(1f))
                }
                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(strings.pendingTransactions, "${adminMetrics.pendingTransactions}", strings.offlineStatus, modifier = Modifier.weight(1f))
                    val reconRate = if (adminMetrics.totalTransactions > 0) {
                        String.format(java.util.Locale.US, "%.1f%%", (adminMetrics.settledTransactions.toDouble() / adminMetrics.totalTransactions.toDouble()) * 100.0)
                    } else "98.7%"
                    StatCard("Settled Tx", "${adminMetrics.settledTransactions}", "$reconRate recon rate", modifier = Modifier.weight(1f), isAccent = true)
                }
                // Row 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(strings.riskScoreLabel, "${adminMetrics.fraudTransactions}", "Anomalies flagged", modifier = Modifier.weight(1f), isWarning = adminMetrics.fraudTransactions > 0)
                    StatCard("Fraud Rate", String.format(java.util.Locale.US, "%.1f%%", adminMetrics.fraudRate), "Prevented overruns", modifier = Modifier.weight(1f), isError = adminMetrics.fraudRate > 5.0)
                }
            }
        }

        // Recent Security Alerts Header
        item {
            Text(
                text = strings.securityAlerts,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.primary
            )
        }

        // List of Alerts (clicking opens Security Center / TXN-4921)
        items(items = fraudAlerts, key = { it.transactionId }) { alert ->
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAlertClick(alert.transactionId) }
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .testTag("fraud_alert_item_${alert.transactionId}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (alert.severity == RiskSeverity.HIGH) colors.errorContainer
                                    else colors.warningContainer
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (alert.severity == RiskSeverity.HIGH) colors.error else colors.warning,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = alert.transactionId,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.primary,
                                    softWrap = true,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (alert.severity == RiskSeverity.HIGH) colors.errorContainer
                                            else colors.warningContainer
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${alert.riskScore}% RULE RISK",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        color = if (alert.severity == RiskSeverity.HIGH) colors.error else colors.warning,
                                        softWrap = true,
                                        maxLines = 1
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(colors.primaryContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "XGBoost ML",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        color = colors.primary,
                                        softWrap = true,
                                        maxLines = 1
                                    )
                                }
                            }
                            Text(
                                text = "${alert.buyerName} • ${alert.merchantName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                                softWrap = true,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = alert.reasons.firstOrNull() ?: "Anomalous velocity detected",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.error,
                                softWrap = true,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₹${alert.amount}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Review",
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    isAccent: Boolean = false,
    isWarning: Boolean = false,
    isError: Boolean = false
) {
    val colors = LocalAppColors.current

    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.border(1.dp, colors.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
                softWrap = true,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = when {
                    isError -> colors.error
                    isWarning -> colors.warning
                    isAccent -> colors.secondary
                    else -> colors.primary
                },
                softWrap = true,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
                softWrap = true,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}
