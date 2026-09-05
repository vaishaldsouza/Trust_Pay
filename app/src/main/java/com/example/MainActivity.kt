package com.example

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserRole
import com.example.engine.VoiceActionResult
import com.example.ui.TrustPayViewModel
import com.example.ui.components.OfflineNoticeBanner
import com.example.ui.components.TrustPayTopBar
import com.example.ui.components.VoiceAssistantBottomSheet
import com.example.ui.screens.ActivitySyncScreen
import com.example.ui.screens.AdminHomeScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BuyerHomeScreen
import com.example.ui.screens.MerchantHomeScreen
import com.example.ui.screens.PaymentConfirmationScreen
import com.example.ui.screens.PaymentScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RoleSelectorScreen
import com.example.ui.screens.SecurityCenterScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.TrustPayTheme
import com.example.util.LocalAppLanguage
import com.example.util.LocalAppStrings
import com.example.util.LocalizationManager
import kotlinx.coroutines.launch

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBarItemColors

enum class ScreenTab {
    AUTH,
    HOME,
    PAY,
    ACTIVITY,
    SECURITY,
    SETTINGS,
    ROLE_SELECTOR,
    CONFIRMATION,
    PROFILE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: TrustPayViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val selectedLanguage by viewModel.selectedLanguage.collectAsState()
            val strings = LocalizationManager.getStrings(selectedLanguage)

            TrustPayTheme(themeMode = themeMode) {
                CompositionLocalProvider(
                    LocalAppLanguage provides selectedLanguage,
                    LocalAppStrings provides strings
                ) {
                    TrustPayApp(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrustPayApp(viewModel: TrustPayViewModel = viewModel()) {
    val context = LocalContext.current
    val isOnline by viewModel.isOnline.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val buyerState by viewModel.buyerState.collectAsState()
    val walletBalance by viewModel.walletBalance.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    val selectedMerchant by viewModel.selectedMerchant.collectAsState()
    val paymentAmountInput by viewModel.paymentAmountInput.collectAsState()
    val selectedModeChoice by viewModel.selectedModeChoice.collectAsState()
    val trustDecision by viewModel.trustDecision.collectAsState()
    val isTamperActive by viewModel.isTamperSimulationActive.collectAsState()
    val activeTx by viewModel.activeTransaction.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val fraudAlerts by viewModel.fraudAlerts.collectAsState()
    val geminiExplanation by viewModel.geminiExplanation.collectAsState()
    val isGeminiLoading by viewModel.isGeminiLoading.collectAsState()
    val mlEvaluation by viewModel.mlFraudResult.collectAsState()
    val isMlLoading by viewModel.isMlFraudEvaluating.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isChatbotThinking by viewModel.isChatbotThinking.collectAsState()
    val adminMetrics by viewModel.adminMetrics.collectAsState()
    val supabaseStatus by viewModel.supabaseStatus.collectAsState()
    val lastSyncTimestamp by viewModel.lastSyncTimestamp.collectAsState()
    val pendingOfflineCount by viewModel.pendingOfflineCount.collectAsState()
    val razorpayBackendUrl by viewModel.razorpayBackendUrl.collectAsState()

    // BLE & Wi-Fi Direct State
    val bleConnectionState by viewModel.bleConnectionState.collectAsState()
    val bleDiscoveredDevices by viewModel.bleDiscoveredDevices.collectAsState()
    val isMerchantBleAdvertising by viewModel.isMerchantBleAdvertising.collectAsState()

    val wifiDirectConnectionState by viewModel.wifiDirectConnectionState.collectAsState()
    val wifiDirectDiscoveredPeers by viewModel.wifiDirectDiscoveredPeers.collectAsState()
    val isMerchantWifiAdvertising by viewModel.isMerchantWifiAdvertising.collectAsState()

    val qrScanState by viewModel.qrScanState.collectAsState()

    val isUltrasonicListening by viewModel.isUltrasonicListening.collectAsState()
    val ultrasonicAudioLevel by viewModel.ultrasonicAudioLevel.collectAsState()

    // Language, Theme & Balance Privacy State
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val isBalanceMasked by viewModel.isBalanceMasked.collectAsState()
    val strings = LocalAppStrings.current
    val colors = LocalAppColors.current

    // Voice Assistant State
    val isVoiceAssistantOpen by viewModel.isVoiceAssistantOpen.collectAsState()
    val isListening by viewModel.voiceEngine.isListening.collectAsState()
    val isSpeaking by viewModel.voiceEngine.isSpeaking.collectAsState()
    val transcription by viewModel.voiceEngine.transcription.collectAsState()
    val lastVoiceResponse by viewModel.voiceEngine.lastResponse.collectAsState()
    val lastVoiceActionResult by viewModel.lastVoiceActionResult.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val isRealSession by viewModel.isRealSession.collectAsState()
    var currentScreen by remember { mutableStateOf(if (isRealSession) ScreenTab.HOME else ScreenTab.AUTH) }
    var showMicRationaleDialog by remember { mutableStateOf(false) }

    val hasAudioPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    // Permission launcher for microphone recording
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleVoiceListening { errorMsg ->
                coroutineScope.launch { snackbarHostState.showSnackbar(errorMsg) }
            }
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Microphone permission denied. Tap 'Open App Settings' to grant manually.")
            }
        }
    }

    if (showMicRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showMicRationaleDialog = false },
            title = { Text("Microphone Permission Required", fontWeight = FontWeight.Bold) },
            text = {
                Text("TrustPay Voice Assistant requires microphone access (RECORD_AUDIO) to process natural language voice commands in Hindi, English, Kannada, and Malayalam for hands-free offline payments and balance inquiries.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showMicRationaleDialog = false
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                ) {
                    Text("Grant Permission", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMicRationaleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val bleEnableLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.startBleScan()
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Bluetooth turn-on action was cancelled.")
            }
        }
    }

    val blePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            viewModel.startBleScan()
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Bluetooth permissions denied. You can tap 'Open App Settings' to grant manually.")
            }
        }
    }

    val wifiPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            viewModel.startWifiDirectScan()
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Wi-Fi Direct permissions denied. You can tap 'Open App Settings' to grant manually.")
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startQrScan()
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Camera permission denied. You can tap 'Open App Settings' to grant manually.")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (currentScreen != ScreenTab.AUTH && currentScreen != ScreenTab.ROLE_SELECTOR && currentScreen != ScreenTab.PROFILE) {
                Column {
                    TrustPayTopBar(
                        isOnline = isOnline,
                        onToggleConnection = {
                            viewModel.toggleNetworkConnection()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    if (!isOnline) "Simulated: ONLINE. Sync engine active."
                                    else "Simulated: OFFLINE. Offline spending allowance active."
                                )
                            }
                        },
                        currentRole = currentRole,
                        onOpenProfile = { currentScreen = ScreenTab.PROFILE },
                        onLogout = { viewModel.logout { currentScreen = ScreenTab.AUTH } },
                        onMicClick = { viewModel.openVoiceAssistant() },
                        themeMode = themeMode,
                        onToggleTheme = {
                            viewModel.toggleThemeMode()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    "Theme switched"
                                )
                            }
                        }
                    )
                    OfflineNoticeBanner(isOnline = isOnline)
                }
            }
        },
        bottomBar = {
            if (currentScreen != ScreenTab.AUTH && currentScreen != ScreenTab.ROLE_SELECTOR && currentScreen != ScreenTab.CONFIRMATION && currentScreen != ScreenTab.PROFILE) {
                NavigationBar(
                    containerColor = colors.surfaceLowest,
                    tonalElevation = 4.dp,
                    modifier = Modifier.testTag("main_navigation_bar")
                ) {
                    when (currentRole) {
                        UserRole.BUYER -> {
                            NavigationBarItem(
                                selected = currentScreen == ScreenTab.HOME,
                                onClick = { currentScreen = ScreenTab.HOME },
                                icon = { Icon(Icons.Default.Home, contentDescription = strings.navHome) },
                                label = { Text(strings.navHome, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = navItemColors(),
                                modifier = Modifier.testTag("nav_buyer_home")
                            )
                            NavigationBarItem(
                                selected = currentScreen == ScreenTab.PAY,
                                onClick = { currentScreen = ScreenTab.PAY },
                                icon = { Icon(Icons.Default.Payment, contentDescription = strings.navPay) },
                                label = { Text(strings.navPay, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = navItemColors(),
                                modifier = Modifier.testTag("nav_buyer_pay")
                            )
                            NavigationBarItem(
                                selected = currentScreen == ScreenTab.ACTIVITY,
                                onClick = { currentScreen = ScreenTab.ACTIVITY },
                                icon = { Icon(Icons.Default.Sync, contentDescription = strings.navActivity) },
                                label = { Text(strings.navActivity, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = navItemColors(),
                                modifier = Modifier.testTag("nav_buyer_activity")
                            )
                            NavigationBarItem(
                                selected = currentScreen == ScreenTab.SETTINGS,
                                onClick = { currentScreen = ScreenTab.SETTINGS },
                                icon = { Icon(Icons.Default.Settings, contentDescription = strings.navSettings) },
                                label = { Text(strings.navSettings, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = navItemColors(),
                                modifier = Modifier.testTag("nav_buyer_settings")
                            )
                        }
                        UserRole.MERCHANT -> {
                            NavigationBarItem(
                                selected = currentScreen == ScreenTab.HOME,
                                onClick = { currentScreen = ScreenTab.HOME },
                                icon = { Icon(Icons.Default.Storefront, contentDescription = strings.navTerminal) },
                                label = { Text(strings.navTerminal, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = navItemColors(),
                                modifier = Modifier.testTag("nav_merchant_terminal")
                            )
                            NavigationBarItem(
                                selected = currentScreen == ScreenTab.ACTIVITY,
                                onClick = { currentScreen = ScreenTab.ACTIVITY },
                                icon = { Icon(Icons.Default.Sync, contentDescription = strings.navSync) },
                                label = { Text(strings.navSync, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = navItemColors(),
                                modifier = Modifier.testTag("nav_merchant_sync")
                            )
                            NavigationBarItem(
                                selected = currentScreen == ScreenTab.SETTINGS,
                                onClick = { currentScreen = ScreenTab.SETTINGS },
                                icon = { Icon(Icons.Default.Settings, contentDescription = strings.navSettings) },
                                label = { Text(strings.navSettings, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = navItemColors(),
                                modifier = Modifier.testTag("nav_merchant_settings")
                            )
                        }
                        UserRole.ADMIN -> {
                            NavigationBarItem(
                                selected = currentScreen == ScreenTab.HOME,
                                onClick = { currentScreen = ScreenTab.HOME },
                                icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = strings.navMonitor) },
                                label = { Text(strings.navMonitor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = navItemColors(),
                                modifier = Modifier.testTag("nav_admin_monitor")
                            )
                            NavigationBarItem(
                                selected = currentScreen == ScreenTab.SECURITY,
                                onClick = { currentScreen = ScreenTab.SECURITY },
                                icon = { Icon(Icons.Default.Security, contentDescription = strings.navSecurity) },
                                label = { Text(strings.navSecurity, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = navItemColors(),
                                modifier = Modifier.testTag("nav_admin_security")
                            )
                            NavigationBarItem(
                                selected = currentScreen == ScreenTab.ACTIVITY,
                                onClick = { currentScreen = ScreenTab.ACTIVITY },
                                icon = { Icon(Icons.Default.Sync, contentDescription = strings.navRecon) },
                                label = { Text(strings.navRecon, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = navItemColors(),
                                modifier = Modifier.testTag("nav_admin_recon")
                            )
                            NavigationBarItem(
                                selected = currentScreen == ScreenTab.SETTINGS,
                                onClick = { currentScreen = ScreenTab.SETTINGS },
                                icon = { Icon(Icons.Default.Settings, contentDescription = strings.navAudit) },
                                label = { Text(strings.navAudit, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = navItemColors(),
                                modifier = Modifier.testTag("nav_admin_audit")
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.background)
        ) {
            Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                when (screen) {
                    ScreenTab.AUTH -> {
                        AuthScreen(
                            onLogin = { email, pass, onResult ->
                                viewModel.loginUser(email, pass) { success, msg ->
                                    if (success) {
                                        currentScreen = ScreenTab.HOME
                                    }
                                    onResult(success, msg)
                                }
                            },
                            onRegister = { name, email, pass, role, onResult ->
                                viewModel.registerUser(name, email, pass, role) { success, msg ->
                                    if (success) {
                                        currentScreen = ScreenTab.HOME
                                    }
                                    onResult(success, msg)
                                }
                            },
                            onDemoSelect = { role ->
                                viewModel.selectDemoRole(role)
                                currentScreen = ScreenTab.HOME
                            }
                        )
                    }

                    ScreenTab.HOME -> {
                        when (currentRole) {
                            UserRole.BUYER -> {
                                BuyerHomeScreen(
                                    isOnline = isOnline,
                                    buyer = buyerState,
                                    walletBalance = walletBalance,
                                    transactions = transactions,
                                    onMakePaymentClick = { currentScreen = ScreenTab.PAY },
                                    onTransactionClick = { tx ->
                                        viewModel.setActiveTransaction(tx)
                                        currentScreen = ScreenTab.CONFIRMATION
                                    },
                                    onSyncClick = {
                                        viewModel.triggerReconciliationSync()
                                        currentScreen = ScreenTab.ACTIVITY
                                    },
                                    onOpenUssdClick = {
                                        viewModel.openUssdDialer(context) { errorMsg ->
                                            coroutineScope.launch { snackbarHostState.showSnackbar(errorMsg) }
                                        }
                                    },
                                    isBalanceMasked = isBalanceMasked,
                                    onToggleBalanceMasked = { viewModel.toggleBalanceMasked() }
                                )

                            }
                            UserRole.MERCHANT -> {
                                MerchantHomeScreen(
                                    merchant = selectedMerchant,
                                    isOnline = isOnline,
                                    transactions = transactions.filter { it.merchantId == selectedMerchant.merchantId },
                                    onAcceptPayment = { tx ->
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Payment ${tx.transactionId} accepted offline!")
                                        }
                                    },
                                    onRejectPayment = { tx ->
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Payment ${tx.transactionId} rejected by merchant.")
                                        }
                                    },
                                    onTransactionClick = { tx ->
                                        viewModel.setActiveTransaction(tx)
                                        currentScreen = ScreenTab.CONFIRMATION
                                    },
                                    isBleAdvertising = isMerchantBleAdvertising,
                                    onStartBleAdvertising = {
                                        if (viewModel.bluetoothEngine.hasAdvertisePermission()) {
                                            viewModel.startMerchantBleAdvertising { msg ->
                                                coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                                            }
                                        } else {
                                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                                blePermissionLauncher.launch(
                                                    arrayOf(
                                                        Manifest.permission.BLUETOOTH_ADVERTISE,
                                                        Manifest.permission.BLUETOOTH_CONNECT
                                                    )
                                                )
                                            }
                                        }
                                    },
                                    onStopBleAdvertising = { viewModel.stopMerchantBleAdvertising() },
                                    isWifiAdvertising = isMerchantWifiAdvertising,
                                    onStartWifiAdvertising = {
                                        if (viewModel.wifiDirectEngine.hasRequiredPermissions()) {
                                            viewModel.startMerchantWifiDirectBroadcasting { msg ->
                                                coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                                            }
                                        } else {
                                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                                wifiPermissionLauncher.launch(arrayOf(Manifest.permission.NEARBY_WIFI_DEVICES))
                                            } else {
                                                wifiPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                                            }
                                        }
                                    },
                                    onStopWifiAdvertising = { viewModel.stopMerchantWifiDirectBroadcasting() },
                                    isUltrasonicListening = isUltrasonicListening,
                                    ultrasonicAudioLevel = ultrasonicAudioLevel,
                                    onStartUltrasonicListening = {
                                        if (hasAudioPermission) {
                                            viewModel.startUltrasonicListening { success, msg ->
                                                coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                                            }
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    },
                                    onStopUltrasonicListening = { viewModel.stopUltrasonicListening() },
                                    generateMerchantReceiveQr = { m -> viewModel.generateMerchantReceiveQr(m) }
                                )
                            }
                            UserRole.ADMIN -> {
                                AdminHomeScreen(
                                    fraudAlerts = fraudAlerts,
                                    adminMetrics = adminMetrics,
                                    onAlertClick = { alertTxId ->
                                        val targetTx = transactions.firstOrNull { it.transactionId == alertTxId }
                                            ?: transactions.first()
                                        viewModel.setActiveTransaction(targetTx)
                                        currentScreen = ScreenTab.SECURITY
                                    },
                                    onStartDemoClick = {
                                        viewModel.runHackathonDemo { step ->
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(step)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }

                    ScreenTab.PAY -> {
                        PaymentScreen(
                            isOnline = isOnline,
                            buyer = buyerState,
                            merchants = viewModel.merchantsList,
                            selectedMerchant = selectedMerchant,
                            onSelectMerchant = { viewModel.setSelectedMerchant(it) },
                            amountInput = paymentAmountInput,
                            onAmountChange = { viewModel.setPaymentAmount(it) },
                            modeChoice = selectedModeChoice,
                            onModeChange = { viewModel.setModeChoice(it) },
                            trustDecision = trustDecision,
                            isTamperSimulationActive = isTamperActive,
                            onToggleTamper = { viewModel.toggleTamperSimulation() },
                            onSubmitPayment = { offlineMethod ->
                                viewModel.submitPaymentWithHardwareTransport(
                                    offlineOption = offlineMethod,
                                    onSuccess = {
                                        currentScreen = ScreenTab.CONFIRMATION
                                    },
                                    onDeclined = {
                                        // Handled inside PaymentScreen modal
                                    }
                                )
                            },
                            walletBalance = walletBalance,
                            bleConnectionState = bleConnectionState,
                            bleDiscoveredDevices = bleDiscoveredDevices,
                            onStartBleScan = { viewModel.startBleScan() },
                            onStopBleScan = { viewModel.stopBleScan() },
                            onConnectBleDevice = { peer -> viewModel.connectToBleDevice(peer) },
                            onDisconnectBleDevice = { viewModel.disconnectBleDevice() },
                            onOpenSettings = { viewModel.openAppSettings(context) },
                            onEnableBluetooth = {
                                bleEnableLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                            },
                            onRequestPermissions = {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                    blePermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.BLUETOOTH_SCAN,
                                            Manifest.permission.BLUETOOTH_CONNECT,
                                            Manifest.permission.BLUETOOTH_ADVERTISE
                                        )
                                    )
                                } else {
                                    blePermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            },
                            wifiDirectConnectionState = wifiDirectConnectionState,
                            wifiDirectDiscoveredPeers = wifiDirectDiscoveredPeers,
                            onStartWifiDirectScan = { viewModel.startWifiDirectScan() },
                            onStopWifiDirectScan = { viewModel.stopWifiDirectScan() },
                            onConnectWifiDirectPeer = { peer -> viewModel.connectToWifiDirectPeer(peer) },
                            onDisconnectWifiDirectPeer = { viewModel.disconnectWifiDirectPeer() },
                            qrScanState = qrScanState,
                            onStartQrScan = {
                                if (viewModel.qrEngine.hasCameraPermission()) {
                                    viewModel.startQrScan()
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            onStopQrScan = { viewModel.stopQrScan() },
                            onProcessQrPayload = { payload -> viewModel.processScannedQrPayload(payload) },
                            onRequestCameraPermission = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                            generateSignedTransactionQr = { tx -> viewModel.generateSignedTransactionQr(tx) },
                            generateMerchantReceiveQr = { merchant -> viewModel.generateMerchantReceiveQr(merchant) },
                            onOpenUssdClick = {
                                viewModel.openUssdDialer(context) { errorMsg ->
                                    coroutineScope.launch { snackbarHostState.showSnackbar(errorMsg) }
                                }
                            }
                        )
                    }

                    ScreenTab.CONFIRMATION -> {
                        val tx = activeTx ?: transactions.firstOrNull()
                        if (tx != null) {
                            PaymentConfirmationScreen(
                                transaction = tx,
                                onForceSync = {
                                    if (isOnline) {
                                        viewModel.triggerReconciliationSync()
                                        currentScreen = ScreenTab.ACTIVITY
                                    } else {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Device is offline. Connect to sync.")
                                        }
                                    }
                                },
                                onVoidTransaction = {
                                    currentScreen = ScreenTab.HOME
                                },
                                onBack = { currentScreen = ScreenTab.HOME }
                            )
                        } else {
                            currentScreen = ScreenTab.HOME
                        }
                    }

                    ScreenTab.ACTIVITY -> {
                        ActivitySyncScreen(
                            syncState = syncState,
                            isOnline = isOnline,
                            transactions = transactions,
                            onTriggerSync = { viewModel.triggerReconciliationSync() },
                            onReviewFlaggedItem = { flaggedId ->
                                val target = transactions.firstOrNull { it.transactionId == flaggedId }
                                    ?: transactions.first()
                                viewModel.setActiveTransaction(target)
                                currentScreen = ScreenTab.SECURITY
                            },
                            onTransactionClick = { tx ->
                                viewModel.setActiveTransaction(tx)
                                currentScreen = ScreenTab.CONFIRMATION
                            },
                            isBalanceMasked = isBalanceMasked
                        )
                    }


                    ScreenTab.SECURITY -> {
                        val targetTx = activeTx ?: transactions.firstOrNull { it.status == com.example.data.model.TransactionStatus.FRAUD_REVIEW }
                        ?: transactions.first()
                        SecurityCenterScreen(
                            transaction = targetTx,
                            isRestricted = buyerState.isRestricted,
                            onToggleRestriction = {
                                viewModel.toggleBuyerRestriction()
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (buyerState.isRestricted) "Restriction removed for buyer dev_buyer_01"
                                        else "Offline payments restricted for buyer dev_buyer_01"
                                    )
                                }
                            },
                            geminiExplanation = geminiExplanation,
                            isGeminiLoading = isGeminiLoading,
                            onAskGemini = { query ->
                                viewModel.investigateWithGemini(targetTx, query)
                            },
                            onBack = { currentScreen = ScreenTab.HOME },
                            mlEvaluation = mlEvaluation,
                            isMlLoading = isMlLoading,
                            onRetryMlEvaluation = { viewModel.evaluateMlFraud(targetTx) }
                        )
                    }

                    ScreenTab.SETTINGS -> {
                        SettingsScreen(
                            currentRole = currentRole,
                            onRoleChange = { viewModel.setRole(it) },
                            isOnline = isOnline,
                            onToggleConnection = { viewModel.toggleNetworkConnection() },
                            selectedLanguage = selectedLanguage,
                            onSelectLanguage = { viewModel.setLanguage(it) },
                            themeMode = themeMode,
                            onSelectThemeMode = { viewModel.setThemeMode(it) },
                            onLaunchVoiceAssistant = { viewModel.openVoiceAssistant() },
                            onRunDemo = { onStep ->
                                viewModel.runHackathonDemo(onStep)
                            },
                            supabaseStatus = supabaseStatus,
                            lastSyncTimestamp = lastSyncTimestamp,
                            pendingTransactionsCount = pendingOfflineCount,
                            razorpayBackendUrl = razorpayBackendUrl,
                            onSelectRazorpayBackendUrl = { url -> viewModel.setRazorpayBackendUrl(url) },
                            onLogout = {
                                viewModel.logout { currentScreen = ScreenTab.AUTH }
                            }
                        )
                    }

                    ScreenTab.ROLE_SELECTOR -> {
                        RoleSelectorScreen(
                            currentRole = currentRole,
                            onRoleSelected = { viewModel.setRole(it) },
                            onContinue = { currentScreen = ScreenTab.HOME },
                            onLogin = { email, pass, onResult ->
                                viewModel.loginUser(email, pass, onResult)
                            },
                            onRegister = { name, email, pass, role, onResult ->
                                viewModel.registerUser(name, email, pass, role, onResult)
                            }
                        )
                    }

                    ScreenTab.PROFILE -> {
                        ProfileScreen(
                            user = currentUser,
                            isRealSession = isRealSession,
                            onSaveProfile = { newName, onResult ->
                                viewModel.updateUserProfile(newName, onResult)
                            },
                            onBack = { currentScreen = ScreenTab.HOME }
                        )
                    }
                }
            }

            // Global Multilingual Voice Assistant Bottom Sheet
            VoiceAssistantBottomSheet(
                isOpen = isVoiceAssistantOpen,
                onDismiss = { viewModel.closeVoiceAssistant() },
                isListening = isListening,
                isSpeaking = isSpeaking,
                transcription = transcription,
                lastResponse = lastVoiceResponse,
                lastActionResult = lastVoiceActionResult,
                chatMessages = chatMessages,
                isThinking = isChatbotThinking,
                onSendTextMessage = { text -> viewModel.sendChatMessage(text, isVoiceInput = false) },
                onClearChat = { viewModel.clearChatHistory() },
                onToggleListening = {
                    if (hasAudioPermission) {
                        viewModel.toggleVoiceListening { errorMsg ->
                            coroutineScope.launch { snackbarHostState.showSnackbar(errorMsg) }
                        }
                    } else {
                        showMicRationaleDialog = true
                    }
                },
                onPlayAudio = { viewModel.playLastVoiceResponse() },
                onStopAudio = { viewModel.stopVoiceAudio() },
                onExecuteSampleQuery = { query ->
                    viewModel.processVoiceQuery(query)
                },
                onNavigateToPaymentConfirmed = { amount, merchantId ->
                    val targetMerchant = viewModel.merchantsList.firstOrNull { it.merchantId == merchantId }
                        ?: viewModel.merchantsList.first()
                    viewModel.setSelectedMerchant(targetMerchant)
                    viewModel.setPaymentAmount(amount.toInt().toString())
                    currentScreen = ScreenTab.PAY
                },
                hasAudioPermission = hasAudioPermission,
                onRequestAudioPermission = { showMicRationaleDialog = true },
                onOpenSettings = { viewModel.openAppSettings(context) }
            )
        }
    }
}

@Composable
private fun navItemColors(): NavigationBarItemColors {
    val colors = LocalAppColors.current
    return NavigationBarItemDefaults.colors(
        selectedIconColor = colors.primary,
        selectedTextColor = colors.primary,
        unselectedIconColor = colors.onSurfaceVariant,
        unselectedTextColor = colors.onSurfaceVariant,
        indicatorColor = colors.surfaceContainer
    )
}
