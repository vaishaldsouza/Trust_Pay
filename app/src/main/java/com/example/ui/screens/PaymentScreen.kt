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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Buyer
import com.example.data.model.Merchant
import com.example.engine.ModeSelectorChoice
import com.example.engine.TrustDecision
import com.example.ui.theme.LocalAppColors
import com.example.util.LocalAppStrings

enum class OnlinePaymentOption {
    SCAN_QR,
    UPI_ID,
    TRUSTPAY_DIRECT
}

enum class OfflinePaymentOption(val label: String) {
    QR("QR Code"),
    BLUETOOTH("Bluetooth"),
    WIFI("Wi-Fi Direct"),
    ULTRASONIC("Soundwave")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    isOnline: Boolean,
    buyer: Buyer,
    merchants: List<Merchant>,
    selectedMerchant: Merchant,
    onSelectMerchant: (Merchant) -> Unit,
    amountInput: String,
    onAmountChange: (String) -> Unit,
    modeChoice: ModeSelectorChoice,
    onModeChange: (ModeSelectorChoice) -> Unit,
    trustDecision: TrustDecision?,
    isTamperSimulationActive: Boolean,
    onToggleTamper: () -> Unit,
    onSubmitPayment: (String) -> Unit,
    walletBalance: Double,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val colors = LocalAppColors.current

    var selectedOnlineOption by remember { mutableStateOf(OnlinePaymentOption.SCAN_QR) }
    var selectedOfflineOption by remember { mutableStateOf(OfflinePaymentOption.QR) }
    var upiIdInput by remember { mutableStateOf("artisanroasters@okhdfcbank") }
    var isMerchantDropdownOpen by remember { mutableStateOf(false) }
    var showLimitExceededModal by remember { mutableStateOf(false) }

    // Interactive hardware testing states
    var isSoundwaveTesting by remember { mutableStateOf(false) }
    var soundwaveStatusText by remember { mutableStateOf<String?>(null) }
    var isScanningBlePeers by remember { mutableStateOf(false) }
    var isScanningWifiPeers by remember { mutableStateOf(false) }

    // If decision is rejected due to offline limit exceeded, show limit exceeded view
    if (showLimitExceededModal && trustDecision != null && !trustDecision.isApproved) {
        OfflineLimitExceededView(
            decision = trustDecision,
            onDismiss = { showLimitExceededModal = false },
            onExplainClick = {}
        )
        return
    }

    val availableOfflineLimit = (buyer.offlineLimit - buyer.offlineExposure).coerceAtLeast(0L)
    val parsedAmount = amountInput.toLongOrNull() ?: 0L
    val isAmountValid = parsedAmount > 0
    val isWithinOfflineCap = isOnline || (parsedAmount <= availableOfflineLimit)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // ==========================================
        // 1. CONNECTIVITY HEADER / BANNER
        // ==========================================
        item {
            if (isOnline) {
                // 🟢 ONLINE MODE HEADER
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.secondary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.secondaryFixed.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = null,
                                tint = colors.secondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🟢 ONLINE MODE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.secondary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(colors.secondaryFixed.copy(alpha = 0.3f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Razorpay Flow Active",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                        color = colors.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Standard UPI & online banking settlement available.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // 🟠 / 🔴 OFFLINE MODE HEADER
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, colors.error.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(colors.errorContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WifiOff,
                                    contentDescription = null,
                                    tint = colors.error,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🔴 OFFLINE MODE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = colors.error
                                )
                                Text(
                                    text = "Choose Offline Transmission Method",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.errorContainer.copy(alpha = 0.5f))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Standard UPI & Razorpay gateways disabled. Select QR, Bluetooth, Wi-Fi Direct, or Soundwave to authorize payment offline via TrustAgent cryptography.",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.error
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. PAYMENT METHOD SELECTION TABS
        // ==========================================
        item {
            if (isOnline) {
                // 🟢 ONLINE PAYMENT OPTIONS
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Digital Payment Options",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.primary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.surfaceContainer, RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        OnlinePaymentOption.values().forEach { opt ->
                            val isSelected = selectedOnlineOption == opt
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) colors.primary else Color.Transparent)
                                    .clickable { selectedOnlineOption = opt }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = when (opt) {
                                            OnlinePaymentOption.SCAN_QR -> Icons.Default.QrCodeScanner
                                            OnlinePaymentOption.UPI_ID -> Icons.Default.PhoneAndroid
                                            OnlinePaymentOption.TRUSTPAY_DIRECT -> Icons.Default.AccountBalance
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else colors.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when (opt) {
                                            OnlinePaymentOption.SCAN_QR -> "Scan QR"
                                            OnlinePaymentOption.UPI_ID -> "UPI ID"
                                            OnlinePaymentOption.TRUSTPAY_DIRECT -> "TrustPay"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) Color.White else colors.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // 🟠 OFFLINE PAYMENT OPTIONS: QR, Bluetooth, Wi-Fi Direct, Ultrasonic Soundwave
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Offline Method",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.primary
                        )
                        Text(
                            text = "4 Channels Supported",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.secondary
                        )
                    }

                    // 4-Column Grid/Row for Offline Payment Channels
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.surfaceContainer, RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        OfflinePaymentOption.values().forEach { opt ->
                            val isSelected = selectedOfflineOption == opt
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) colors.primary else Color.Transparent)
                                    .clickable { selectedOfflineOption = opt }
                                    .padding(vertical = 8.dp, horizontal = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = when (opt) {
                                            OfflinePaymentOption.QR -> Icons.Default.QrCode
                                            OfflinePaymentOption.BLUETOOTH -> Icons.Default.Bluetooth
                                            OfflinePaymentOption.WIFI -> Icons.Default.Sensors
                                            OfflinePaymentOption.ULTRASONIC -> Icons.Default.GraphicEq
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else colors.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = when (opt) {
                                            OfflinePaymentOption.QR -> "QR"
                                            OfflinePaymentOption.BLUETOOTH -> "Bluetooth"
                                            OfflinePaymentOption.WIFI -> "Wi-Fi"
                                            OfflinePaymentOption.ULTRASONIC -> "Sound"
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
                }
            }
        }

        // ==========================================
        // 3. INTERACTIVE METHOD DETAILS / VIEW
        // ==========================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (isOnline) {
                        when (selectedOnlineOption) {
                            OnlinePaymentOption.SCAN_QR -> {
                                Text(
                                    text = "📷 Scan Merchant Dynamic UPI QR",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.primary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.surfaceContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QrCodeScanner,
                                            contentDescription = null,
                                            tint = colors.secondary,
                                            modifier = Modifier.size(44.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Target scanned: ${selectedMerchant.businessName}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = colors.primary
                                        )
                                        Text(
                                            text = "VPA: ${selectedMerchant.merchantId.lowercase()}@razorpay",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            OnlinePaymentOption.UPI_ID -> {
                                Text(
                                    text = "📱 Enter UPI ID or Mobile Number",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = upiIdInput,
                                    onValueChange = { upiIdInput = it },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    trailingIcon = {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colors.secondary)
                                    },
                                    placeholder = { Text("example@upi") }
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "✅ Verified Merchant: ${selectedMerchant.businessName}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.secondary
                                )
                            }
                            OnlinePaymentOption.TRUSTPAY_DIRECT -> {
                                Text(
                                    text = "💰 TrustPay Direct Settlement",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.surfaceContainer)
                                        .clickable { isMerchantDropdownOpen = true }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(colors.primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(selectedMerchant.businessName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = colors.primary)
                                            Text("${selectedMerchant.category} • ${selectedMerchant.location}", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                                        }
                                    }
                                    Text("Change ▾", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = colors.secondary)
                                }
                            }
                        }
                    } else {
                        // 4 OFFLINE TRANSMISSION METHOD SUBVIEWS
                        when (selectedOfflineOption) {
                            OfflinePaymentOption.QR -> {
                                Text(
                                    text = "📷 Scan Offline Merchant QR Code",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.surfaceContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QrCode,
                                            contentDescription = null,
                                            tint = colors.primary,
                                            modifier = Modifier.size(44.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Target: ${selectedMerchant.businessName}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = colors.primary
                                        )
                                        Text(
                                            text = "Verified Device Public Key: MCowBQYDK2VwAyEA...78b1",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            OfflinePaymentOption.BLUETOOTH -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🔵 Pay via Bluetooth (BLE Nearby Terminal)",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = colors.primary
                                    )
                                    Text(
                                        text = if (isScanningBlePeers) "Scanning..." else "Channel: Ready",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = colors.secondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.surfaceContainer)
                                        .padding(14.dp)
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(colors.secondaryFixed.copy(alpha = 0.4f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Bluetooth,
                                                    contentDescription = null,
                                                    tint = colors.secondary,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = "BLE Terminal: ${selectedMerchant.businessName}",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = colors.primary
                                                )
                                                Text(
                                                    text = "Signal: -42 dBm (Strong) • GATT Service UUID: fee0",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = colors.secondary
                                                )
                                                Text(
                                                    text = "Hardware channel: Android BluetoothAdapter active",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = colors.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            OfflinePaymentOption.WIFI -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📶 Pay via Wi-Fi Direct (Local P2P Mesh)",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = colors.primary
                                    )
                                    Text(
                                        text = "Port: 8988",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = colors.secondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.surfaceContainer)
                                        .padding(14.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(colors.primary.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Sensors,
                                                contentDescription = null,
                                                tint = colors.primary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Wi-Fi Direct P2P: TPAY_DIRECT_MESH",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = colors.primary
                                            )
                                            Text(
                                                text = "Peer Connected: ${selectedMerchant.businessName} (192.168.49.1)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = colors.secondary
                                            )
                                            Text(
                                                text = "Direct high-speed local TCP socket stream (Zero Internet)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = colors.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                            OfflinePaymentOption.ULTRASONIC -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🔊 Pay via Ultrasonic Soundwave (Audio Pulse)",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = colors.primary
                                    )
                                    Text(
                                        text = "18.5 kHz Carrier",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = colors.secondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.surfaceContainer)
                                        .padding(14.dp)
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(colors.secondaryFixed.copy(alpha = 0.4f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.GraphicEq,
                                                    contentDescription = null,
                                                    tint = colors.secondary,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = "18.5 kHz Acoustic Dual-Tone BFSK Modem",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = colors.primary
                                                )
                                                Text(
                                                    text = "Real PCM AudioTrack generation & microphone acoustic link",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = colors.secondary
                                                )
                                                Text(
                                                    text = "Target Receiver: ${selectedMerchant.businessName}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = colors.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Merchant selector dropdown
                    DropdownMenu(
                        expanded = isMerchantDropdownOpen,
                        onDismissRequest = { isMerchantDropdownOpen = false }
                    ) {
                        merchants.forEach { m ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(m.businessName, fontWeight = FontWeight.Bold)
                                        Text("${m.category} • ${m.location}", style = MaterialTheme.typography.labelSmall)
                                    }
                                },
                                onClick = {
                                    onSelectMerchant(m)
                                    isMerchantDropdownOpen = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 4. AVAILABLE OFFLINE LIMIT CARD (ONLY IN OFFLINE MODE)
        // ==========================================
        if (!isOnline) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = colors.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🔐 Available Offline Limit",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.primary
                                )
                            }
                            Text(
                                text = "₹$availableOfflineLimit",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.secondary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val limitRatio = if (buyer.offlineLimit > 0) {
                            (buyer.offlineExposure.toFloat() / buyer.offlineLimit.toFloat()).coerceIn(0f, 1f)
                        } else 0f

                        LinearProgressIndicator(
                            progress = { limitRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (limitRatio > 0.8f) colors.error else colors.secondary,
                            trackColor = colors.surfaceContainer,
                            strokeCap = StrokeCap.Round
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Used: ₹${buyer.offlineExposure}",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant
                            )
                            Text(
                                text = "Max Cap: ₹${buyer.offlineLimit}",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 5. ENTER AMOUNT CARD (WITH METHOD CONTEXT)
        // ==========================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isOnline) "Enter Amount to send via Razorpay Flow"
                        else "Enter Amount to send via ${selectedOfflineOption.label}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "₹",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 42.sp
                            ),
                            color = colors.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = onAmountChange,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 42.sp,
                                color = colors.primary
                            ),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .width(180.dp)
                                .testTag("payment_amount_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Preset Quick Amounts
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("120", "150", "420", "800", "2500").forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (amountInput == preset) colors.primary else colors.surfaceContainer)
                                    .clickable { onAmountChange(preset) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "₹$preset",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (amountInput == preset) Color.White else colors.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 6. PIPELINE & TRUST AGENT EVALUATION (OFFLINE & ONLINE FLOW DIAGRAMS)
        // ==========================================
        if (!isOnline) {
            // 🟠 OFFLINE TRUSTAGENT DECISION FLOW
            item {
                val isApproved = parsedAmount > 0 && parsedAmount <= availableOfflineLimit

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isApproved) colors.secondaryFixed.copy(alpha = 0.25f)
                        else colors.surfaceLowest
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isApproved) colors.secondary else colors.outlineVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(16.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = if (isApproved) colors.secondary else colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TrustAgent Offline Evaluation (${selectedOfflineOption.label})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Step 1: Amount
                        FlowStepRow(
                            stepNumber = "1",
                            title = "Amount: ₹${if (amountInput.isEmpty()) "0" else amountInput}",
                            subtitle = "Requested payment value via ${selectedOfflineOption.label}"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Step 2: Question
                        FlowStepRow(
                            stepNumber = "2",
                            title = "TrustAgent Check",
                            subtitle = "Can this transaction be accepted?"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Step 3: Decision
                        FlowStepRow(
                            stepNumber = "3",
                            title = if (isApproved) "YES: Within offline limit (₹$availableOfflineLimit cap)" else "NO: Exceeds available limit of ₹$availableOfflineLimit",
                            subtitle = if (isApproved) "Safe bounded risk margin approved" else "Rejection triggered: connect to internet to replenish",
                            isHighlight = true,
                            isPositive = isApproved
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Step 4: Cryptographic Signature
                        FlowStepRow(
                            stepNumber = "4",
                            title = "Sign transaction (Ed25519)",
                            subtitle = "Device private key signs amount, nonce, & merchant PK"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Step 5: Room DB & Pending Sync
                        FlowStepRow(
                            stepNumber = "5",
                            title = "Payment Accepted Offline (Room DB)",
                            subtitle = "\"Will settle when connection returns\"",
                            isLast = true
                        )
                    }
                }
            }

            // Cryptographic Test Path: Tamper Simulation Switch
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Test Tampered Signature",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.primary
                            )
                            Text(
                                text = "Corrupts signature byte array to prove offline cryptographic verification fails",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isTamperSimulationActive,
                            onCheckedChange = { onToggleTamper() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.error,
                                checkedTrackColor = colors.errorContainer
                            ),
                            modifier = Modifier.testTag("tamper_simulation_switch")
                        )
                    }
                }
            }
        } else {
            // 🟢 ONLINE RAZORPAY SETTLEMENT FLOW DIAGRAM
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = colors.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Razorpay Online Settlement Flow",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        FlowStepRow(
                            stepNumber = "1",
                            title = "Payment Authorization",
                            subtitle = "Amount: ₹${if (amountInput.isEmpty()) "0" else amountInput} via ${when (selectedOnlineOption) {
                                OnlinePaymentOption.SCAN_QR -> "Dynamic UPI QR"
                                OnlinePaymentOption.UPI_ID -> "UPI Virtual Address"
                                OnlinePaymentOption.TRUSTPAY_DIRECT -> "TrustPay Direct"
                            }}"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        FlowStepRow(
                            stepNumber = "2",
                            title = "Instant Bank Gateway Settlement",
                            subtitle = "Razorpay Webhook & NPCI Clearing live"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        FlowStepRow(
                            stepNumber = "3",
                            title = "Direct Cloud Receipt",
                            subtitle = "Immediate Supabase balance settlement",
                            isHighlight = true,
                            isPositive = true,
                            isLast = true
                        )
                    }
                }
            }
        }

        // ==========================================
        // 7. SUBMIT BUTTON
        // ==========================================
        item {
            Button(
                onClick = {
                    if (isOnline || isWithinOfflineCap) {
                        onSubmitPayment(if (isOnline) "ONLINE" else selectedOfflineOption.label)
                    } else {
                        showLimitExceededModal = true
                    }
                },
                enabled = isAmountValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isWithinOfflineCap) colors.error else colors.primary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("pay_submit_button")
            ) {
                Icon(
                    imageVector = if (isOnline) Icons.Default.CheckCircle else when (selectedOfflineOption) {
                        OfflinePaymentOption.QR -> Icons.Default.QrCode
                        OfflinePaymentOption.BLUETOOTH -> Icons.Default.Bluetooth
                        OfflinePaymentOption.WIFI -> Icons.Default.Sensors
                        OfflinePaymentOption.ULTRASONIC -> Icons.Default.GraphicEq
                    },
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isOnline) "Pay ₹${if (amountInput.isEmpty()) "0" else amountInput} via Razorpay"
                    else if (!isWithinOfflineCap) "Exceeds Offline Limit (Max ₹$availableOfflineLimit)"
                    else "Authorize ₹${if (amountInput.isEmpty()) "0" else amountInput} via ${selectedOfflineOption.label} (Ed25519)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
private fun FlowStepRow(
    stepNumber: String,
    title: String,
    subtitle: String,
    isHighlight: Boolean = false,
    isPositive: Boolean = true,
    isLast: Boolean = false
) {
    val colors = LocalAppColors.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(
                        if (isHighlight) {
                            if (isPositive) colors.secondary else colors.error
                        } else colors.surfaceContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stepNumber,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isHighlight) Color.White else colors.primary
                )
            }
            if (!isLast) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(18.dp)
                        .background(colors.outlineVariant.copy(alpha = 0.5f))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.SemiBold
                ),
                color = if (isHighlight) {
                    if (isPositive) colors.secondary else colors.error
                } else colors.primary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
fun OfflineLimitExceededView(
    decision: TrustDecision,
    onDismiss: () -> Unit,
    onExplainClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(colors.errorContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = colors.error,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Offline Limit Exceeded",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = colors.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = decision.reason.ifEmpty { "This transaction exceeds your allocated offline exposure limit." },
            style = MaterialTheme.typography.bodyMedium.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center),
            color = colors.onSurfaceVariant
        )

        if (decision.subReason.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = decision.subReason,
                style = MaterialTheme.typography.bodySmall.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                color = colors.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Adjust Amount", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
        }
    }
}
