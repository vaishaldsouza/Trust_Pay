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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
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
import com.example.data.model.Merchant
import com.example.data.model.Transaction
import com.example.data.model.TransactionMode
import com.example.data.model.TransactionStatus
import com.example.ui.components.ModeChip
import com.example.ui.components.StatusBadge
import com.example.ui.theme.LocalAppColors
import com.example.util.LocalAppLanguage
import com.example.util.LocalAppStrings

@Composable
fun MerchantHomeScreen(
    merchant: Merchant,
    isOnline: Boolean,
    transactions: List<Transaction>,
    onAcceptPayment: (Transaction) -> Unit,
    onRejectPayment: (Transaction) -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    isBleAdvertising: Boolean = false,
    onStartBleAdvertising: () -> Unit = {},
    onStopBleAdvertising: () -> Unit = {},
    isWifiAdvertising: Boolean = false,
    onStartWifiAdvertising: () -> Unit = {},
    onStopWifiAdvertising: () -> Unit = {},
    generateMerchantReceiveQr: ((Merchant) -> Bitmap?)? = null,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val colors = LocalAppColors.current

    // Find pending incoming payment or fallback to sample
    val incomingTx = transactions.firstOrNull {
        it.status == TransactionStatus.OFFLINE_ACCEPTED || it.status == TransactionStatus.PENDING_SYNC
    } ?: transactions.firstOrNull()

    var acceptedLocally by remember { mutableStateOf(false) }
    var rejectedLocally by remember { mutableStateOf(false) }
    var showMerchantQrModal by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Merchant Business Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = colors.secondaryFixedDim,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = merchant.businessName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.primary
                        )
                        Text(
                            text = merchant.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                        Text(
                            text = "${merchant.category} • Terminal #${merchant.merchantId.takeLast(4)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.secondary
                        )
                    }
                }
            }
        }

        // BLE Terminal Advertising Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (isBleAdvertising) colors.secondary else colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isBleAdvertising) colors.secondary.copy(alpha = 0.2f) else colors.surfaceContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = if (isBleAdvertising) colors.secondary else colors.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "BLE Terminal Broadcasting",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary
                            )
                            Text(
                                text = if (isBleAdvertising) "GATT Service 47a25000 Active • Discoverable by Buyers"
                                else "Advertising Stopped • Tap switch to broadcast POS terminal",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isBleAdvertising) colors.secondary else colors.onSurfaceVariant
                            )
                        }
                    }
                    androidx.compose.material3.Switch(
                        checked = isBleAdvertising,
                        onCheckedChange = { active ->
                            if (active) onStartBleAdvertising() else onStopBleAdvertising()
                        }
                    )
                }
            }
        }

        // Wi-Fi Direct Terminal Broadcasting Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (isWifiAdvertising) colors.secondary else colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isWifiAdvertising) colors.primary.copy(alpha = 0.2f) else colors.surfaceContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Sensors,
                                contentDescription = null,
                                tint = if (isWifiAdvertising) colors.primary else colors.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Wi-Fi Direct P2P Broadcasting",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary
                            )
                            Text(
                                text = if (isWifiAdvertising) "DNS-SD Service _trustpay._tcp Active • TCP Server Port 8988"
                                else "P2P Broadcasting Stopped • Tap switch to enable local TCP server",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isWifiAdvertising) colors.primary else colors.onSurfaceVariant
                            )
                        }
                    }
                    androidx.compose.material3.Switch(
                        checked = isWifiAdvertising,
                        onCheckedChange = { active ->
                            if (active) onStartWifiAdvertising() else onStopWifiAdvertising()
                        }
                    )
                }
            }
        }

        // Merchant Receive QR Code Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.QrCode,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Merchant Receive QR Code",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary
                            )
                            Text(
                                text = "Display static QR code for buyer camera auto-selection",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                    Button(
                        onClick = { showMerchantQrModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Text("Show QR")
                    }
                }
            }
        }

        // Incoming Payment Card (matches Image 15!)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = strings.incomingPaymentPrompt,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (incomingTx != null && !acceptedLocally && !rejectedLocally) {
                        // Amount & Mode badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "₹${incomingTx.amount}",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 38.sp
                                ),
                                color = colors.primary
                            )
                            ModeChip(mode = incomingTx.mode)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Customer Details
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.surfaceContainer, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colors.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = incomingTx.buyerName.take(1),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = incomingTx.buyerName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.primary
                                )
                                Text(
                                    text = "Customer • ${incomingTx.buyerId}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Validation Rows (Signature, Risk, Network)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Row 1: Signature
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = strings.cryptographicProofReady,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.onSurfaceVariant
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = null,
                                        tint = if (incomingTx.isTampered) colors.error else colors.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (incomingTx.isTampered) "INVALID" else "VALID",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (incomingTx.isTampered) colors.error else colors.secondary
                                    )
                                }
                            }

                            // Row 2: Risk Level
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = strings.riskScoreLabel,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.onSurfaceVariant
                                )
                                Text(
                                    text = if (incomingTx.fraudProbability > 0.3f) "MEDIUM" else "LOW",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (incomingTx.fraudProbability > 0.3f) colors.warning else colors.secondary
                                )
                            }

                            // Row 3: Network Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = strings.liveSyncStatus,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.onSurfaceVariant
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(colors.surfaceContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = strings.offlineStatus.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        color = colors.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Action Buttons: Reject & Accept
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    rejectedLocally = true
                                    onRejectPayment(incomingTx)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("merchant_reject_button")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reject")
                            }

                            Button(
                                onClick = {
                                    acceptedLocally = true
                                    onAcceptPayment(incomingTx)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.primary,
                                    contentColor = colors.onPrimary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("merchant_accept_button")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(strings.acceptOfflinePayment)
                            }
                        }
                    } else {
                        // Empty / Completed state
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = colors.secondary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (acceptedLocally) strings.paymentSuccess else strings.readyToScan,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.primary
                                )
                                Text(
                                    text = strings.offlineVerificationNote,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Daily Volume & Stats
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(strings.dailyVolume, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("₹14,200", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.primary)
                        Text("28 ${strings.recentTransactions.lowercase()}", style = MaterialTheme.typography.labelSmall, color = colors.secondary)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(strings.pendingTransactions, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("${transactions.count { it.status == TransactionStatus.PENDING_SYNC || it.status == TransactionStatus.OFFLINE_ACCEPTED }}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.primary)
                        Text(strings.offlineStatus, style = MaterialTheme.typography.labelSmall, color = colors.warning)
                    }
                }
            }
        }

        // Recent Merchant Transactions Header
        item {
            Text(
                text = strings.recentTransactions,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.primary
            )
        }

        items(transactions.take(6)) { tx ->
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
                            text = "${tx.buyerName} • ${tx.transactionId}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusBadge(status = tx.status)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sig: VALID",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.secondary
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "+₹${tx.amount}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.secondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        ModeChip(mode = tx.mode)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    if (showMerchantQrModal) {
        val qrBmp = remember(merchant) {
            generateMerchantReceiveQr?.invoke(merchant)
        }
        AlertDialog(
            onDismissRequest = { showMerchantQrModal = false },
            title = {
                Text("${merchant.businessName} - Receive QR", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (qrBmp != null) {
                        Image(
                            bitmap = qrBmp.asImageBitmap(),
                            contentDescription = "Merchant Receive QR",
                            modifier = Modifier
                                .size(220.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, colors.outlineVariant, RoundedCornerShape(8.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(220.dp)
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Generating Receive QR...")
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Merchant ID: ${merchant.merchantId}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.primary
                    )
                    Text(
                        "Buyers can scan this QR code using TrustPay camera for offline payment.",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showMerchantQrModal = false }) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
