package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.data.model.TransactionMode
import com.example.data.model.TransactionStatus
import com.example.data.model.UserRole
import com.example.ui.theme.LocalAppColors
import com.example.util.AppThemeMode
import com.example.util.LocalAppLanguage
import com.example.util.LocalAppStrings

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding

@Composable
fun TrustPayTopBar(
    isOnline: Boolean,
    onToggleConnection: () -> Unit,
    currentRole: UserRole,
    onOpenProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
    onMicClick: () -> Unit,
    themeMode: AppThemeMode,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current
    val colors = LocalAppColors.current

    var showProfileMenu by remember { mutableStateOf(false) }

    Surface(
        color = colors.surfaceLowest,
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
            // Brand Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "TrustPay Logo",
                        tint = colors.secondaryFixedDim,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = strings.appTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = colors.primary
                    )
                    Text(
                        text = when (currentRole) {
                            UserRole.BUYER -> strings.buyerRolePrefix
                            UserRole.MERCHANT -> strings.merchantRolePrefix
                            UserRole.ADMIN -> strings.adminRolePrefix
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            // Right side: Voice Assistant Mic Button + Switch Theme Button + Connection Simulator Pill + Profile Icon Menu
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Voice Assistant Launcher Icon Button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(colors.secondaryFixed.copy(alpha = 0.45f))
                        .border(1.dp, colors.secondary.copy(alpha = 0.5f), CircleShape)
                        .clickable { onMicClick() }
                        .testTag("topbar_mic_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Assistant (${language.displayName})",
                        tint = colors.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Switch Theme Button (Beside Audio Button)
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceContainer)
                        .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), CircleShape)
                        .clickable { onToggleTheme() }
                        .testTag("topbar_theme_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (themeMode) {
                            AppThemeMode.DARK -> Icons.Default.DarkMode
                            AppThemeMode.LIGHT -> Icons.Default.LightMode
                            AppThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                        },
                        contentDescription = "Switch Theme (${themeMode.name})",
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Interactive Connection Toggle Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isOnline) colors.secondaryFixed.copy(alpha = 0.35f)
                            else colors.errorContainer
                        )
                        .border(
                            1.dp,
                            if (isOnline) colors.secondary else colors.error,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { onToggleConnection() }
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                        .testTag("connection_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) colors.secondary else colors.error)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isOnline) strings.onlineStatus.uppercase() else strings.offlineStatus.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = if (isOnline) colors.secondary else colors.error
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Profile Avatar Icon Button with Dropdown Menu
                Box {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(colors.primaryContainer)
                            .border(1.dp, colors.primary.copy(alpha = 0.4f), CircleShape)
                            .clickable { showProfileMenu = true }
                            .testTag("topbar_profile_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "User Profile",
                            tint = colors.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showProfileMenu,
                        onDismissRequest = { showProfileMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Profile", fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = colors.primary
                                )
                            },
                            onClick = {
                                showProfileMenu = false
                                onOpenProfile()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Logout", fontWeight = FontWeight.Medium, color = colors.error) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = null,
                                    tint = colors.error
                                )
                            },
                            onClick = {
                                showProfileMenu = false
                                onLogout()
                            }
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
fun OfflineNoticeBanner(
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val colors = LocalAppColors.current

    AnimatedVisibility(
        visible = !isOnline,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.errorContainer)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = "Offline indicator",
                    tint = colors.onErrorContainer,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.offlineBannerNotice,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    ),
                    color = colors.onErrorContainer
                )
            }
        }
    }
}

@Composable
fun ModeChip(
    mode: TransactionMode,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val (bg, textColor, label, icon) = when (mode) {
        TransactionMode.OFFLINE_VALUE -> Quad(
            colors.secondaryFixed.copy(alpha = 0.4f),
            colors.secondary,
            "OFFLINE VALUE",
            Icons.Default.Security
        )
        TransactionMode.AUTHORIZATION -> Quad(
            colors.primaryContainer,
            colors.secondaryFixedDim,
            "⚡ AUTHORIZATION",
            Icons.Default.FlashOn
        )
        TransactionMode.ONLINE -> Quad(
            colors.secondaryFixed.copy(alpha = 0.35f),
            colors.primary,
            "🌐 ONLINE (RAZORPAY)",
            Icons.Default.CheckCircle
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                ),
                color = textColor
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun StatusBadge(
    status: TransactionStatus,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val (bg, fg, label) = when (status) {
        TransactionStatus.SETTLED -> Triple(
            colors.secondaryFixed.copy(alpha = 0.35f),
            colors.secondary,
            "SETTLED"
        )
        TransactionStatus.OFFLINE_ACCEPTED -> Triple(
            colors.surfaceContainer,
            colors.onSurfaceVariant,
            "OFFLINE ACCEPTED"
        )
        TransactionStatus.PENDING_SYNC -> Triple(
            colors.warningContainer,
            colors.warning,
            "PENDING SYNC"
        )
        TransactionStatus.SETTLEMENT_PENDING -> Triple(
            colors.warningContainer.copy(alpha = 0.85f),
            colors.warning,
            "⏳ SETTLEMENT PENDING"
        )
        TransactionStatus.FRAUD_REVIEW -> Triple(
            colors.errorContainer,
            colors.error,
            "FRAUD REVIEW"
        )
        TransactionStatus.REPLAY_DETECTED -> Triple(
            colors.errorContainer,
            colors.error,
            "REPLAY DETECTED"
        )
        TransactionStatus.INVALID_SIGNATURE -> Triple(
            colors.errorContainer,
            colors.error,
            "INVALID SIGNATURE"
        )
        TransactionStatus.DUPLICATE -> Triple(
            colors.errorContainer,
            colors.error,
            "DUPLICATE"
        )
        else -> Triple(
            colors.surfaceContainer,
            colors.onSurfaceVariant,
            status.name.replace("_", " ")
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 9.sp
            ),
            color = fg
        )
    }
}
