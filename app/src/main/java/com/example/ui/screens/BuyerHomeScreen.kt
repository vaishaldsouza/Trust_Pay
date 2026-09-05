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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Switch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.data.model.Buyer
import com.example.data.model.PeerTransactionRole
import com.example.data.model.Transaction
import com.example.data.model.TransactionMode
import com.example.data.model.TransactionStatus
import com.example.ui.components.ModeChip
import com.example.ui.components.StatusBadge
import com.example.ui.theme.LocalAppColors
import com.example.util.LocalAppLanguage
import com.example.util.LocalAppStrings

@Composable
fun BuyerHomeScreen(
    isOnline: Boolean,
    buyer: Buyer,
    walletBalance: Double,
    transactions: List<Transaction>,
    onMakePaymentClick: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onSyncClick: () -> Unit,
    onOpenUssdClick: () -> Unit = {},
    isBalanceMasked: Boolean = true,
    onToggleBalanceMasked: () -> Unit = {},
    peerTransactionRole: PeerTransactionRole = PeerTransactionRole.SENDER,
    onPeerTransactionRoleChange: (PeerTransactionRole) -> Unit = {},
    onAuthorizeMandate: () -> Unit = {},
    qrBitmap: android.graphics.Bitmap? = null,
    isLoading: Boolean = false,
    isBleAdvertising: Boolean = false,
    onStartBleAdvertising: () -> Unit = {},
    onStopBleAdvertising: () -> Unit = {},
    isWifiAdvertising: Boolean = false,
    onStartWifiAdvertising: () -> Unit = {},
    onStopWifiAdvertising: () -> Unit = {},
    isUltrasonicListening: Boolean = false,
    ultrasonicAudioLevel: Float = 0.0f,
    isUltrasonicSignalDetected: Boolean = false,
    ultrasonicStatusText: String? = null,
    onStartUltrasonicListening: () -> Unit = {},
    onStopUltrasonicListening: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val colors = LocalAppColors.current

    if (isLoading) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(16.dp)
        ) {
            com.example.ui.components.BuyerHomeSkeleton()
        }
        return
    }

    val recentTxSlice = remember(transactions) { transactions.take(4) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Mode Selector Toggle ("Send Money" vs "Receive Money")
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceContainer)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (peerTransactionRole == PeerTransactionRole.SENDER) colors.primary else Color.Transparent)
                        .clickable { onPeerTransactionRoleChange(PeerTransactionRole.SENDER) }
                        .testTag("mode_send_money_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send Money",
                            tint = if (peerTransactionRole == PeerTransactionRole.SENDER) colors.onPrimary else colors.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Send Money",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (peerTransactionRole == PeerTransactionRole.SENDER) colors.onPrimary else colors.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (peerTransactionRole == PeerTransactionRole.RECEIVER) colors.secondary else Color.Transparent)
                        .clickable { onPeerTransactionRoleChange(PeerTransactionRole.RECEIVER) }
                        .testTag("mode_receive_money_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CallReceived,
                            contentDescription = "Receive Money",
                            tint = if (peerTransactionRole == PeerTransactionRole.RECEIVER) colors.onSecondary else colors.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Receive Money",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (peerTransactionRole == PeerTransactionRole.RECEIVER) colors.onSecondary else colors.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Mandatory Mandate Gate for "Send Money" Mode
        if (peerTransactionRole == PeerTransactionRole.SENDER && !buyer.hasActiveMandate) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.warningContainer.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.warning.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = colors.warning,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Set Up Payment Authority First",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "To send offline payments, your account must authorize a Razorpay Autopay mandate. Bounded offline exposure requires a registered UPI payment authority — default limits are never fabricated.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onAuthorizeMandate,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = colors.onPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("authorize_mandate_button")
                        ) {
                            Text(
                                text = "Authorize Razorpay Mandate (₹2,000/mo)",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        // Receive Money Terminal View
        if (peerTransactionRole == PeerTransactionRole.RECEIVER) {
            item {
                var selectedReceiveChannel by remember { mutableStateOf(com.example.ui.screens.OfflinePaymentOption.QR) }

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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = null,
                                tint = colors.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Peer-to-Peer Offline Receiver Terminal",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Select any of the 4 offline channels below to accept payments from senders nearby.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // 4-Column Channel Tab Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.surfaceContainer, RoundedCornerShape(12.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            com.example.ui.screens.OfflinePaymentOption.values().forEach { opt ->
                                val isSelected = selectedReceiveChannel == opt
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) colors.primary else Color.Transparent)
                                        .clickable { selectedReceiveChannel = opt }
                                        .padding(vertical = 8.dp, horizontal = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = when (opt) {
                                                com.example.ui.screens.OfflinePaymentOption.QR -> Icons.Default.QrCode
                                                com.example.ui.screens.OfflinePaymentOption.BLUETOOTH -> Icons.Default.Bluetooth
                                                com.example.ui.screens.OfflinePaymentOption.WIFI -> Icons.Default.Sensors
                                                com.example.ui.screens.OfflinePaymentOption.ULTRASONIC -> Icons.Default.GraphicEq
                                            },
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else colors.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = when (opt) {
                                                com.example.ui.screens.OfflinePaymentOption.QR -> "QR"
                                                com.example.ui.screens.OfflinePaymentOption.BLUETOOTH -> "Bluetooth"
                                                com.example.ui.screens.OfflinePaymentOption.WIFI -> "Wi-Fi"
                                                com.example.ui.screens.OfflinePaymentOption.ULTRASONIC -> "Sound"
                                            },
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            ),
                                            color = if (isSelected) Color.White else colors.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        when (selectedReceiveChannel) {
                            com.example.ui.screens.OfflinePaymentOption.QR -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    if (qrBitmap != null) {
                                        Image(
                                            bitmap = qrBitmap.asImageBitmap(),
                                            contentDescription = "Peer Receive QR",
                                            modifier = Modifier
                                                .size(180.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(1.dp, colors.outlineVariant, RoundedCornerShape(12.dp))
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(180.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(colors.surfaceContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Generating P2P Receive QR...", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "📱 Show QR code to sender to scan with camera",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.secondary
                                    )
                                }
                            }
                            com.example.ui.screens.OfflinePaymentOption.BLUETOOTH -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.surfaceContainer)
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Broadcast Bluetooth GATT Terminal",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = colors.primary
                                        )
                                        Text(
                                            text = if (isBleAdvertising) "Broadcasting Service 47a25000 • Senders can discover & connect"
                                            else "GATT Terminal Off • Switch ON to accept BLE payments",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isBleAdvertising) colors.secondary else colors.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = isBleAdvertising,
                                        onCheckedChange = { active ->
                                            if (active) onStartBleAdvertising() else onStopBleAdvertising()
                                        }
                                    )
                                }
                            }
                            com.example.ui.screens.OfflinePaymentOption.WIFI -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.surfaceContainer)
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Wi-Fi Direct P2P Listener",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = colors.primary
                                        )
                                        Text(
                                            text = if (isWifiAdvertising) "DNS-SD Active • Port 8988 socket listening"
                                            else "P2P Listener Off • Switch ON to accept Wi-Fi Direct payments",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isWifiAdvertising) colors.secondary else colors.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = isWifiAdvertising,
                                        onCheckedChange = { active ->
                                            if (active) onStartWifiAdvertising() else onStopWifiAdvertising()
                                        }
                                    )
                                }
                            }
                            com.example.ui.screens.OfflinePaymentOption.ULTRASONIC -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.surfaceContainer)
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Soundwave Receiver (Acoustic POS)",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = colors.primary
                                            )
                                            Text(
                                                text = ultrasonicStatusText ?: if (isUltrasonicListening) "Microphone active • Listening for 17.5–19.5 kHz BFSK tones"
                                                else "Soundwave Receiver Off • Switch ON to accept audio payments",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isUltrasonicSignalDetected) colors.secondary else if (isUltrasonicListening) colors.secondary else colors.onSurfaceVariant
                                            )
                                        }
                                        Switch(
                                            checked = isUltrasonicListening,
                                            onCheckedChange = { active ->
                                                if (active) onStartUltrasonicListening() else onStopUltrasonicListening()
                                            }
                                        )
                                    }

                                    if (isUltrasonicSignalDetected) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(colors.secondary.copy(alpha = 0.2f))
                                                .border(1.dp, colors.secondary, RoundedCornerShape(8.dp))
                                                .padding(10.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = androidx.compose.material.icons.Icons.Default.FlashOn,
                                                    contentDescription = null,
                                                    tint = colors.secondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "⚡ 19.5 kHz Sync Preamble Detected — Decoding payload...",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = colors.primary
                                                )
                                            }
                                        }
                                    }

                                    if (isUltrasonicListening) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Goertzel FFT Signal Strength",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = colors.onSurfaceVariant
                                            )
                                            Text(
                                                text = "${(ultrasonicAudioLevel * 100).toInt()}% Signal",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = colors.secondary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { ultrasonicAudioLevel.coerceIn(0f, 1f) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = if (isUltrasonicSignalDetected || ultrasonicAudioLevel > 0.3f) colors.secondary else colors.outlineVariant,
                                            trackColor = colors.surfaceLow
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 1. Available Balance Card (matches Image 5)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
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
                            Text(
                                text = strings.availableBalance,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = if (isBalanceMasked) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isBalanceMasked) "Show balance" else "Hide balance",
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .clickable { onToggleBalanceMasked() }
                                    .testTag("balance_toggle_icon")
                            )
                        }
                        if (!isOnline) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(colors.surfaceContainer)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = strings.offlineStatus.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    AnimatedContent(
                        targetState = isBalanceMasked,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(250))
                        },
                        label = "BalanceMaskCrossfade"
                    ) { masked ->
                        Text(
                            text = if (masked) "₹ • • • • •" else "₹${"%,.2f".format(walletBalance)}",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 34.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = colors.primary,
                            modifier = Modifier.testTag(if (masked) "masked_balance_text" else "revealed_balance_text")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onMakePaymentClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = colors.onPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("make_payment_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (!isOnline) "${strings.makePayment} (${strings.offlineStatus})" else strings.makePayment,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        OutlinedButton(
                            onClick = onSyncClick,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary),
                            modifier = Modifier
                                .height(46.dp)
                                .testTag("receive_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.forceSync)
                        }
                    }
                }
            }
        }

        // 2. Offline Spending Allowance Card (matches Image 3)
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
                                    .background(colors.secondaryFixed.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = colors.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = strings.offlineAllowanceBadge,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.primary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.secondaryFixed.copy(alpha = 0.3f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = strings.offlineSafeBadge,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.secondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            AnimatedContent(
                                targetState = isBalanceMasked,
                                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(250)) },
                                label = "AllowanceAvailableCrossfade"
                            ) { masked ->
                                Text(
                                    text = if (masked) "₹ • • •" else "₹${buyer.availableExposure}",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 28.sp
                                    ),
                                    color = colors.primary
                                )
                            }
                            AnimatedContent(
                                targetState = isBalanceMasked,
                                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(250)) },
                                label = "AllowanceLimitCrossfade"
                            ) { masked ->
                                Text(
                                    text = if (masked) "${strings.availableBalance} / ₹ • • • (${strings.offlineExposureLimit})"
                                           else "${strings.availableBalance} / ₹${buyer.offlineLimit} (${strings.offlineExposureLimit})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }

                        AnimatedContent(
                            targetState = isBalanceMasked,
                            transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(250)) },
                            label = "ExposureAmountCrossfade"
                        ) { masked ->
                            Text(
                                text = if (masked) "₹ • • • ${strings.exposurePercentage}"
                                       else "₹${buyer.offlineExposure} ${strings.exposurePercentage}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = if (buyer.offlineExposure > 400L) colors.error else colors.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val progress = (buyer.offlineExposure.toFloat() / buyer.offlineLimit.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (progress > 0.8f) colors.error else colors.secondary,
                        trackColor = colors.surfaceContainer,
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    AnimatedContent(
                        targetState = isBalanceMasked,
                        transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(250)) },
                        label = "ProofReadyCrossfade"
                    ) { masked ->
                        Text(
                            text = if (masked) "${strings.cryptographicProofReady}: ₹ • • • ${strings.modeOfflineValueDesc}"
                                   else "${strings.cryptographicProofReady}: ₹${buyer.availableExposure} ${strings.modeOfflineValueDesc}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 3. Grid: Pending Settlement & Risk Status (matches Image 3)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pending Settlement Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = strings.pendingTransactions,
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        AnimatedContent(
                            targetState = isBalanceMasked,
                            transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(250)) },
                            label = "PendingSyncCrossfade"
                        ) { masked ->
                            Text(
                                text = if (masked) "₹ • • •" else "₹250",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.liveSyncStatus,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.secondary
                        )
                    }
                }


                // Risk Status Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.riskScoreLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.onSurfaceVariant
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(colors.secondaryFixed.copy(alpha = 0.35f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (buyer.isRestricted) strings.accountRestricted.uppercase() else strings.offlineSafeBadge.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    ),
                                    color = if (buyer.isRestricted) colors.error else colors.secondary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (buyer.isRestricted) strings.accountRestricted else strings.offlineSafeBadge,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (buyer.isRestricted) colors.error else colors.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.cryptographicProofReady,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 4. Payment Authority Card (Razorpay Mandate - Image 3)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.modeMandate,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                            color = colors.onSurfaceVariant
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(colors.secondaryFixed.copy(alpha = 0.35f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = strings.onlineStatus.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = colors.secondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Razorpay UPI Autopay",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Ref: ${buyer.mandateReference}",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "Max Authorized\n₹${buyer.maxMandateMonthly} / mo",
                            style = MaterialTheme.typography.labelSmall.copy(
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            ),
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 4b. USSD *99# Feature Phone Access Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .testTag("ussd_banking_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
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
                                    .background(colors.primaryContainer.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Access via *99# USSD Banking",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.primary
                                )
                                Text(
                                    text = "Feature phone option • Direct NPCI telecom code",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(colors.surfaceContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "REAL GSM",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                color = colors.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Launches your phone's real dialer pre-filled with *99#. Exits TrustPay to connect directly to your bank's live NPCI USSD network over cellular service.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onOpenUssdClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("open_ussd_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneInTalk,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Launch Dialer with *99#",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // 5. Recent Transactions Header & List (Image 3 & Image 5)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = strings.recentTransactions,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.primary
                    )
                    if (!isOnline) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(colors.surfaceContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = strings.offlineStatus,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                }

                if (transactions.any { it.status == TransactionStatus.PENDING_SYNC || it.status == TransactionStatus.OFFLINE_ACCEPTED }) {
                    Text(
                        text = strings.forceSync,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.secondary,
                        modifier = Modifier.clickable { onSyncClick() }
                    )
                }
            }
        }

        if (transactions.isEmpty()) {
            item {
                Text(
                    text = strings.noTransactionsYet,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }

        items(items = recentTxSlice, key = { it.transactionId }) { tx ->
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTransactionClick(tx) }
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                    .testTag("transaction_item_${tx.transactionId}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(colors.surfaceContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (tx.amount > 0) Icons.Default.Storefront else Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tx.merchantName,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary,
                                softWrap = true,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = tx.transactionId,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = colors.onSurfaceVariant,
                                    softWrap = true,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                StatusBadge(status = tx.status)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        AnimatedContent(
                            targetState = isBalanceMasked,
                            transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(250)) },
                            label = "HomeTxAmountCrossfade"
                        ) { masked ->
                            Text(
                                text = if (masked) "-₹ • • •" else "-₹${tx.amount}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary
                            )
                        }
                        ModeChip(mode = tx.mode)
                    }

                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
