package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.ui.theme.LocalAppColors
import com.example.util.LocalAppStrings

@Composable
fun ProfileScreen(
    user: User,
    isRealSession: Boolean,
    onSaveProfile: (name: String, phoneNumber: String, gender: String, onResult: (Boolean, String) -> Unit) -> Unit,
    onBack: () -> Unit,
    onChangePin: (oldPin: String, newPin: String, onResult: (Boolean, String) -> Unit) -> Unit = { _, _, _ -> },
    isPinSet: Boolean = false,
    onOpenSupportDesk: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val colors = LocalAppColors.current

    var nameInput by remember(user.name) { mutableStateOf(user.name) }
    var phoneInput by remember(user.phoneNumber) { mutableStateOf(user.phoneNumber) }
    var genderInput by remember(user.gender) { mutableStateOf(user.gender) }
    var isSaving by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessMessage by remember { mutableStateOf(false) }

    var showChangePinDialog by remember { mutableStateOf(false) }
    var currentPinInput by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }
    var pinFeedbackMessage by remember { mutableStateOf<String?>(null) }
    var isPinSuccess by remember { mutableStateOf(false) }

    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showLicenseDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // Top Navigation Bar
        Surface(
            color = colors.surfaceLowest,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.primary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Edit Profile",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.primary
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Avatar & Profile Header Badge
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(colors.primaryContainer)
                    .border(2.dp, colors.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.take(1).uppercase(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = user.name,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = colors.primary,
                textAlign = TextAlign.Center,
                softWrap = true,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                softWrap = true,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Role & Session Badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.secondaryFixed.copy(alpha = 0.4f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "ROLE: ${user.role.name}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.secondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isRealSession) colors.primaryContainer
                            else colors.surfaceHigh
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isRealSession) "SUPABASE AUTH" else "DEMO SESSION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isRealSession) colors.primary else colors.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Editable Account Details Form Card
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLow),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Account Information",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.primary
                    )

                    // Editable Name Field
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = colors.primary) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outline
                        )
                    )

                    // Editable Phone Number Field
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = colors.primary) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_phone_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outline
                        )
                    )

                    // Gender Selection Section
                    Column {
                        Text(
                            text = "Gender",
                            fontSize = 12.sp,
                            color = colors.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Male", "Female", "Other", "Prefer not to say").forEach { g ->
                                val isSelected = genderInput.equals(g, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) colors.primary else colors.surfaceHigh)
                                        .border(
                                            1.dp,
                                            if (isSelected) colors.primary else colors.outlineVariant.copy(alpha = 0.5f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { genderInput = g }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = g,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) colors.onPrimary else colors.onSurface,
                                        softWrap = true,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Read-only Email Field with explanation
                    Column {
                        OutlinedTextField(
                            value = user.email,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = colors.onSurfaceVariant) },
                            trailingIcon = { Icon(Icons.Default.Lock, contentDescription = "Read only", tint = colors.onSurfaceVariant) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = colors.outlineVariant,
                                disabledTextColor = colors.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Email address is read-only (changes require link re-verification)",
                            fontSize = 11.sp,
                            color = colors.onSurfaceVariant
                        )
                    }

                    // Read-Only User ID Chip
                    Column {
                        Text(
                            text = "User Identifier (UUID)",
                            fontSize = 12.sp,
                            color = colors.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.surfaceHigh)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = user.id,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = colors.primary
                            )
                        }
                    }

                    // Error / Success Feedback Banner
                    feedbackMessage?.let { msg ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSuccessMessage) colors.secondary.copy(alpha = 0.15f)
                                    else colors.error.copy(alpha = 0.15f)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = msg,
                                fontSize = 13.sp,
                                color = if (isSuccessMessage) colors.secondary else colors.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Save Profile Button
                    Button(
                        onClick = {
                            if (nameInput.isBlank()) {
                                feedbackMessage = "Full Name cannot be empty"
                                isSuccessMessage = false
                                return@Button
                            }
                            isSaving = true
                            feedbackMessage = null
                            onSaveProfile(nameInput.trim(), phoneInput.trim(), genderInput) { success, msg ->
                                isSaving = false
                                isSuccessMessage = success
                                feedbackMessage = msg
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("profile_save_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = colors.onPrimary,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = "Save Changes",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            // Payment Security PIN Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = colors.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Payment Security PIN",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.onSurface
                            )
                        }
                        Surface(
                            color = if (isPinSet) colors.secondary.copy(alpha = 0.15f) else colors.error.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = if (isPinSet) "PIN Active" else "Not Set",
                                color = if (isPinSet) colors.secondary else colors.error,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "Your 4-6 digit PIN is hashed via PBKDF2WithHmacSHA256 and required to cryptographically sign every outgoing transaction.",
                        fontSize = 12.sp,
                        color = colors.onSurfaceVariant
                    )

                    if (!pinFeedbackMessage.isNullOrBlank()) {
                        Text(
                            text = pinFeedbackMessage!!,
                            fontSize = 13.sp,
                            color = if (isPinSuccess) colors.secondary else colors.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = { showChangePinDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.surfaceContainer),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isPinSet) "Change Payment PIN" else "Set Payment PIN",
                            color = colors.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // About & Legal Section Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = colors.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "About TrustPay",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.onSurface
                        )
                    }

                    // Terms of Service Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceHigh)
                            .clickable { showTermsDialog = true }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Gavel, contentDescription = null, tint = colors.secondary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Terms of Service",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.onSurface,
                                softWrap = true,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colors.onSurfaceVariant)
                    }

                    // Privacy Policy Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceHigh)
                            .clickable { showPrivacyDialog = true }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = colors.secondary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Privacy Policy",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.onSurface,
                                softWrap = true,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colors.onSurfaceVariant)
                    }

                    // Version & Software License Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceHigh)
                            .clickable { showLicenseDialog = true }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = colors.secondary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Version & Software Licenses",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.onSurface,
                                    softWrap = true,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "v2.5.0-trustpay • Build 2026.09",
                                    fontSize = 11.sp,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colors.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Help & Support Desk Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenSupportDesk() }
                    .testTag("profile_support_desk_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(colors.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = colors.primary, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Help & Support Desk",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary,
                                softWrap = true,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Report bugs, send feedback, or request features",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                                softWrap = true,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colors.primary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal Dialog: Terms of Service
    if (showTermsDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Gavel, contentDescription = null, tint = colors.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Terms of Service", color = colors.onSurface, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "TrustPay Bounded Offline Payment Service Agreement",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = colors.primary
                    )
                    Text(
                        text = "1. Authorization & Mandate: By utilizing TrustPay offline payments, you grant authorization to process pre-approved offline spending limits up to your Razorpay mandate ceiling.",
                        fontSize = 12.sp,
                        color = colors.onSurface
                    )
                    Text(
                        text = "2. Hardware Cryptographic Signature: Every offline transaction generated on your device is signed using an ECC Secp256r1 private key backed by your device hardware keystore and protected by your PBKDF2 payment PIN.",
                        fontSize = 12.sp,
                        color = colors.onSurface
                    )
                    Text(
                        text = "3. Settlement Commitment: Outstanding offline authorizations are automatically queued and settled via Razorpay recurring APIs upon reconnection to cellular or Wi-Fi data.",
                        fontSize = 12.sp,
                        color = colors.onSurface
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTermsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text("Close", color = colors.onPrimary)
                }
            },
            containerColor = colors.surface
        )
    }

    // Modal Dialog: Privacy Policy
    if (showPrivacyDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = colors.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Privacy Policy", color = colors.onSurface, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "TrustPay Zero-Knowledge & Security Privacy Commitment",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = colors.primary
                    )
                    Text(
                        text = "1. PIN Isolation: Your 4-6 digit payment security PIN is never transmitted over network calls, Bluetooth, Wi-Fi Direct, QR payloads, or Ultrasonic soundwaves. It is hashed locally using PBKDF2WithHmacSHA256 with 10,000+ iterations.",
                        fontSize = 12.sp,
                        color = colors.onSurface
                    )
                    Text(
                        text = "2. Local Cryptographic Credentials: ECC private keys remain encrypted inside Android Keystore or EncryptedSharedPreferences.",
                        fontSize = 12.sp,
                        color = colors.onSurface
                    )
                    Text(
                        text = "3. Offline Transports: QR, BLE, Wi-Fi Direct, and Ultrasonic channels transmit only ephemeral, zero-knowledge signed payload tokens containing no personal banking credentials.",
                        fontSize = 12.sp,
                        color = colors.onSurface
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text("Close", color = colors.onPrimary)
                }
            },
            containerColor = colors.surface
        )
    }

    // Modal Dialog: Version & Software Licenses
    if (showLicenseDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showLicenseDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = colors.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Version & Licenses", color = colors.onSurface, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "TrustPay Engine v2.5.0 (Build 2026.09)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = colors.primary
                    )
                    Text(
                        text = "Razorpay Bounded Offline Payment Architecture",
                        fontSize = 11.sp,
                        color = colors.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Open Source Dependencies:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = colors.onSurface)
                    Text(text = "• Android Jetpack Compose — Apache 2.0 License", fontSize = 11.sp, color = colors.onSurfaceVariant)
                    Text(text = "• CameraX & ZXing QRCode Library — Apache 2.0 License", fontSize = 11.sp, color = colors.onSurfaceVariant)
                    Text(text = "• Supabase Auth & Storage SDK — MIT License", fontSize = 11.sp, color = colors.onSurfaceVariant)
                    Text(text = "• Ktor Asynchronous HTTP Client — Apache 2.0 License", fontSize = 11.sp, color = colors.onSurfaceVariant)
                    Text(text = "• Razorpay Autopay API Integration — Commercial SDK", fontSize = 11.sp, color = colors.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showLicenseDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text("Close", color = colors.onPrimary)
                }
            },
            containerColor = colors.surface
        )
    }

    if (showChangePinDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = { Text("Change Payment PIN", color = colors.onSurface, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (isPinSet) {
                        OutlinedTextField(
                            value = currentPinInput,
                            onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) currentPinInput = it },
                            label = { Text("Current 4-6 Digit PIN") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.outline
                            )
                        )
                    }
                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) newPinInput = it },
                        label = { Text("New 4-6 Digit PIN") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outline
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onChangePin(currentPinInput, newPinInput) { success, msg ->
                            isPinSuccess = success
                            pinFeedbackMessage = msg
                            if (success) {
                                showChangePinDialog = false
                                currentPinInput = ""
                                newPinInput = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text("Save PIN", color = colors.onPrimary)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showChangePinDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.surfaceLowest)
                ) {
                    Text("Cancel", color = colors.onSurface)
                }
            },
            containerColor = colors.surface
        )
    }
}
