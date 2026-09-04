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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.LocalAppColors
import com.example.util.LocalAppLanguage
import com.example.util.LocalAppStrings

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding

@Composable
fun RoleSelectorScreen(
    currentRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    onContinue: () -> Unit,
    onLogin: (String, String, (Boolean, String) -> Unit) -> Unit = { _, _, _ -> },
    onRegister: (String, String, String, UserRole, (Boolean, String) -> Unit) -> Unit = { _, _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val colors = LocalAppColors.current

    var showAuthModal by remember { mutableStateOf(false) }
    var isRegisterMode by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var authFeedbackMsg by remember { mutableStateOf<String?>(null) }
    var isAuthSubmitting by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo & Title (matches Image 7!)
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "TrustPay Shield",
                tint = colors.secondaryFixedDim,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = strings.appName,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            ),
            color = colors.primary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = strings.tagline,
            style = MaterialTheme.typography.bodyMedium.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center),
            color = colors.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Role Cards
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            RoleOptionCard(
                title = strings.buyerRole,
                description = "Pay offline with pre-authorized spending allowances and cryptographic authorization.",
                icon = Icons.Default.Person,
                isSelected = currentRole == UserRole.BUYER,
                onClick = { onRoleSelected(UserRole.BUYER) }
            )

            RoleOptionCard(
                title = strings.merchantRole,
                description = "Accept bounded offline transactions securely and verify cryptographic signatures.",
                icon = Icons.Default.Storefront,
                isSelected = currentRole == UserRole.MERCHANT,
                onClick = { onRoleSelected(UserRole.MERCHANT) }
            )

            RoleOptionCard(
                title = strings.adminRole,
                description = "Monitor exposure limits, review ML fraud anomalies, and track Razorpay settlements.",
                icon = Icons.Default.AdminPanelSettings,
                isSelected = currentRole == UserRole.ADMIN,
                onClick = { onRoleSelected(UserRole.ADMIN) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onContinue,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("role_continue_button")
        ) {
            Text(strings.continueText, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.width(6.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { showAuthModal = true },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("🔑 Account Sign In / Register", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
        }
    }

    if (showAuthModal) {
        AlertDialog(
            onDismissRequest = { showAuthModal = false },
            title = {
                Text(
                    text = if (isRegisterMode) "Register New Account" else "Sign In to TrustPay",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (isRegisterMode) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Full Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (authFeedbackMsg != null) {
                        Text(
                            text = authFeedbackMsg!!,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.primary
                        )
                    }

                    TextButton(
                        onClick = {
                            isRegisterMode = !isRegisterMode
                            authFeedbackMsg = null
                        }
                    ) {
                        Text(
                            text = if (isRegisterMode) "Already have an account? Sign In" else "New to TrustPay? Create an Account",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !isAuthSubmitting && emailInput.isNotBlank() && passwordInput.isNotBlank(),
                    onClick = {
                        isAuthSubmitting = true
                        authFeedbackMsg = "Authenticating..."
                        if (isRegisterMode) {
                            onRegister(nameInput.ifEmpty { emailInput.substringBefore("@") }, emailInput, passwordInput, currentRole) { success, msg ->
                                isAuthSubmitting = false
                                authFeedbackMsg = msg
                                if (success) {
                                    showAuthModal = false
                                    onContinue()
                                }
                            }
                        } else {
                            onLogin(emailInput, passwordInput) { success, msg ->
                                isAuthSubmitting = false
                                authFeedbackMsg = msg
                                if (success) {
                                    showAuthModal = false
                                    onContinue()
                                }
                            }
                        }
                    }
                ) {
                    Text(if (isRegisterMode) "Register & Login" else "Sign In")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAuthModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun RoleOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) colors.surfaceLowest else colors.surfaceContainer.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                1.5.dp,
                if (isSelected) colors.secondary else colors.outlineVariant.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            )
            .testTag("role_card_${title.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) colors.secondaryFixed.copy(alpha = 0.4f) else colors.surfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) colors.secondary else colors.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = colors.secondary,
                    unselectedColor = colors.outlineVariant
                )
            )
        }
    }
}
