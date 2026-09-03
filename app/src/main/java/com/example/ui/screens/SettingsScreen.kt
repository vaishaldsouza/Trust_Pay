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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.LocalAppColors
import com.example.util.AppLanguage
import com.example.util.AppThemeMode
import com.example.util.LocalAppStrings

@Composable
fun SettingsScreen(
    currentRole: UserRole,
    onRoleChange: (UserRole) -> Unit,
    isOnline: Boolean,
    onToggleConnection: () -> Unit,
    selectedLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    themeMode: AppThemeMode,
    onSelectThemeMode: (AppThemeMode) -> Unit,
    onLaunchVoiceAssistant: () -> Unit,
    onRunDemo: (onUpdate: (String) -> Unit) -> Unit,
    supabaseStatus: String = "Connected",
    lastSyncTimestamp: Long? = null,
    pendingTransactionsCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val colors = LocalAppColors.current

    var demoStatusMessage by remember { mutableStateOf<String?>(null) }
    var isDemoRunning by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = strings.settingsTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.primary,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        // 1. Multi-Language Selector Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .testTag("settings_language_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = colors.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = strings.languageSection,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary
                            )
                            Text(
                                text = strings.selectLanguageSubtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppLanguage.values().forEach { lang ->
                            val isSelected = selectedLanguage == lang
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) colors.primary else colors.surfaceContainer
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) colors.primary else colors.outlineVariant.copy(alpha = 0.4f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onSelectLanguage(lang) }
                                    .padding(vertical = 10.dp, horizontal = 4.dp)
                                    .testTag("lang_chip_${lang.code}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = lang.flag,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = lang.nativeName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp
                                        ),
                                        color = if (isSelected) colors.onPrimary else colors.onSurface
                                    )
                                    Text(
                                        text = lang.displayName,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = if (isSelected) colors.onPrimary.copy(alpha = 0.8f) else colors.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Theme Mode Selector Card (Light / Dark / System)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .testTag("settings_theme_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (themeMode) {
                                    AppThemeMode.LIGHT -> Icons.Default.LightMode
                                    AppThemeMode.DARK -> Icons.Default.DarkMode
                                    AppThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
                                },
                                contentDescription = null,
                                tint = colors.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = strings.themeModeSection,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary
                            )
                            Text(
                                text = strings.selectThemeSubtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppThemeMode.values().forEach { mode ->
                            val isSelected = themeMode == mode
                            val label = when (mode) {
                                AppThemeMode.LIGHT -> strings.themeLight
                                AppThemeMode.DARK -> strings.themeDark
                                AppThemeMode.SYSTEM -> strings.themeSystem
                            }
                            val icon = when (mode) {
                                AppThemeMode.LIGHT -> Icons.Default.LightMode
                                AppThemeMode.DARK -> Icons.Default.DarkMode
                                AppThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) colors.primary else colors.surfaceContainer
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) colors.primary else colors.outlineVariant.copy(alpha = 0.4f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onSelectThemeMode(mode) }
                                    .padding(vertical = 10.dp, horizontal = 4.dp)
                                    .testTag("theme_chip_${mode.name}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) colors.onPrimary else colors.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) colors.onPrimary else colors.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Multilingual Voice Assistant Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.secondary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .testTag("settings_voice_assistant_card")
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colors.secondaryFixed.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = colors.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = strings.voiceAssistantSection,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.primary
                                )
                                Text(
                                    text = strings.voiceAssistantSubtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onLaunchVoiceAssistant,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.secondary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("launch_voice_assistant_button")
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.launchVoiceAssistant,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // 4. Backend & Supabase Cloud Status Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .testTag("settings_backend_status_card")
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isOnline && supabaseStatus != "Error") colors.secondaryFixed.copy(alpha = 0.5f)
                                        else colors.errorContainer
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = if (isOnline) colors.secondary else colors.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = strings.backendStatus,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.primary
                                )
                                Text(
                                    text = if (isOnline) "Supabase: $supabaseStatus" else "Supabase: Offline Mode",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isOnline) colors.secondary else colors.error
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isOnline) colors.secondaryFixed.copy(alpha = 0.3f)
                                    else colors.surfaceContainer
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isOnline) "LIVE SYNC" else "ROOM ONLY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = if (isOnline) colors.secondary else colors.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Pending Room Queue", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                            Text(
                                text = "$pendingTransactionsCount transaction(s)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Last Sync", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                            Text(
                                text = if (lastSyncTimestamp != null) {
                                    val diffSec = (System.currentTimeMillis() - lastSyncTimestamp) / 1000
                                    if (diffSec < 60) "Just now" else "${diffSec / 60}m ago"
                                } else "Not synced yet",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary
                            )
                        }
                    }
                }
            }
        }

        // 5. End-to-End Hackathon Demo Runner Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, colors.secondary, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.secondaryFixed.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = colors.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = strings.hackathonDemoTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary
                            )
                            Text(
                                text = strings.hackathonDemoSubtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (demoStatusMessage != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = colors.surfaceContainer),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = colors.secondary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = demoStatusMessage!!,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = colors.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    Button(
                        onClick = {
                            isDemoRunning = true
                            onRunDemo { step ->
                                demoStatusMessage = step
                                if (step.contains("Complete")) {
                                    isDemoRunning = false
                                }
                            }
                        },
                        enabled = !isDemoRunning,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("run_hackathon_demo_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isDemoRunning) "Running Showcase..." else strings.startDemoButton,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 6. Active Role Switcher Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = strings.activePersona,
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UserRole.values().forEach { role ->
                            val isSelected = currentRole == role
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) colors.primary else colors.surfaceContainer)
                                    .clickable { onRoleChange(role) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = role.name,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) colors.onPrimary else colors.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. Network Simulator Control Card
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.connectionSimulation,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.primary
                        )
                        Text(
                            text = if (isOnline) "Status: Online (Connected to cloud)" else "Status: Offline (Local storage & cryptography only)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOnline) colors.secondary else colors.error
                        )
                    }

                    Switch(
                        checked = isOnline,
                        onCheckedChange = { onToggleConnection() },
                        modifier = Modifier.testTag("settings_network_switch")
                    )
                }
            }
        }

        // 8. Core Architectural Principles Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = strings.architecturePrinciples,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ComplianceItem("Does not create an alternative currency or token", colors.secondary, colors.primary)
                        ComplianceItem("Does not replace UPI; integrates with Razorpay Autopay", colors.secondary, colors.primary)
                        ComplianceItem("Offline transactions labeled 'Offline Authorized', not settled", colors.secondary, colors.primary)
                        ComplianceItem("Deterministic exposure limits (no AI for double-spend)", colors.secondary, colors.primary)
                        ComplianceItem("Ed25519 asymmetric cryptographic signing", colors.secondary, colors.primary)
                        ComplianceItem("Isolation Forest & XGBoost ML behavioral anomaly scoring", colors.secondary, colors.primary)
                        ComplianceItem("Gemini AI explainability for compliance audit", colors.secondary, colors.primary)
                    }
                }
            }
        }

        // 9. Cryptographic Credentials Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = strings.deviceCryptography,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Algorithm: Ed25519 (ECDSA P-256 fallback)\n" +
                                "Device: dev_buyer_01\n" +
                                "Autopay Mandate: MND-9823-XYZ\n" +
                                "Settlement Gateway: Razorpay Mock API (Protected)",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, lineHeight = 18.sp),
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun ComplianceItem(text: String, iconColor: Color, textColor: Color) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = textColor
        )
    }
}
