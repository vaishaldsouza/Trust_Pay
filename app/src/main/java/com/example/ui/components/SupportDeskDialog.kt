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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppColors
import kotlin.random.Random

enum class SupportTab(val label: String) {
    REPORT_ISSUE("Report Issue"),
    GIVE_FEEDBACK("Give Feedback"),
    REQUEST_FEATURE("Request Feature")
}

@Composable
fun SupportDeskDialog(
    onDismiss: () -> Unit,
    onSubmitTicket: (type: String, title: String, description: String, category: String, priority: String, rating: Int, callback: (String) -> Unit) -> Unit
) {
    val colors = LocalAppColors.current

    var selectedTab by remember { mutableStateOf(SupportTab.REPORT_ISSUE) }
    var submittedTicketId by remember { mutableStateOf<String?>(null) }

    // Issue state
    var issueCategory by remember { mutableStateOf("Payment Failure") }
    var issueSubject by remember { mutableStateOf("") }
    var issueDescription by remember { mutableStateOf("") }
    var issuePriority by remember { mutableStateOf("Medium") }

    // Feedback state
    var feedbackRating by remember { mutableIntStateOf(5) }
    var feedbackCategory by remember { mutableStateOf("Overall App Experience") }
    var feedbackText by remember { mutableStateOf("") }

    // Feature state
    var featureTitle by remember { mutableStateOf("") }
    var featureDescription by remember { mutableStateOf("") }
    var featureModule by remember { mutableStateOf("Offline Transports") }

    var isDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TrustPay Support Desk",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary,
                                softWrap = true,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Help us improve or solve payment issues",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant,
                                softWrap = true,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (submittedTicketId != null) {
                    // Success View
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = colors.secondary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Ticket Created Successfully!",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tracking Reference: $submittedTicketId",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.secondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Thank you for helping us refine TrustPay's zero-trust payment network. Our engineering team will review your submission shortly.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Tabs
                    TabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        containerColor = colors.surfaceContainer,
                        contentColor = colors.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                    ) {
                        SupportTab.values().forEach { tab ->
                            Tab(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                                text = {
                                    Text(
                                        text = tab.label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        softWrap = true,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    when (selectedTab) {
                        SupportTab.REPORT_ISSUE -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "Category",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.primary
                                )
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedButton(
                                        onClick = { isDropdownExpanded = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(issueCategory, modifier = Modifier.weight(1f))
                                    }
                                    DropdownMenu(
                                        expanded = isDropdownExpanded,
                                        onDismissRequest = { isDropdownExpanded = false }
                                    ) {
                                        listOf(
                                            "Payment Failure",
                                            "Soundwave / BLE Connection",
                                            "Offline Sync & Reconciliation",
                                            "UI / Display Bug",
                                            "Security & PIN Issue"
                                        ).forEach { cat ->
                                            DropdownMenuItem(
                                                text = { Text(cat) },
                                                onClick = {
                                                    issueCategory = cat
                                                    isDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = issueSubject,
                                    onValueChange = { issueSubject = it },
                                    label = { Text("Issue Subject") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("support_issue_subject"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colors.primary,
                                        unfocusedBorderColor = colors.outline
                                    )
                                )

                                OutlinedTextField(
                                    value = issueDescription,
                                    onValueChange = { issueDescription = it },
                                    label = { Text("Detailed Description") },
                                    minLines = 3,
                                    maxLines = 5,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("support_issue_description"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colors.primary,
                                        unfocusedBorderColor = colors.outline
                                    )
                                )

                                Text(
                                    text = "Priority Level",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.primary
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf("Low", "Medium", "High", "Critical").forEach { prio ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (issuePriority == prio) colors.primary else colors.surfaceContainer)
                                                .clickable { issuePriority = prio }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = prio,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (issuePriority == prio) colors.onPrimary else colors.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        if (issueSubject.isNotBlank() && issueDescription.isNotBlank()) {
                                            onSubmitTicket(
                                                "BUG_REPORT",
                                                issueSubject,
                                                issueDescription,
                                                issueCategory,
                                                issuePriority,
                                                0
                                            ) { ticketId ->
                                                submittedTicketId = ticketId
                                            }
                                        }
                                    },
                                    enabled = issueSubject.isNotBlank() && issueDescription.isNotBlank(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("submit_issue_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Submit Bug Report", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        SupportTab.GIVE_FEEDBACK -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "Rate your experience",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.primary
                                )
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    (1..5).forEach { star ->
                                        IconButton(onClick = { feedbackRating = star }) {
                                            Icon(
                                                imageVector = if (star <= feedbackRating) Icons.Default.Star else Icons.Default.StarOutline,
                                                contentDescription = "$star Stars",
                                                tint = if (star <= feedbackRating) colors.secondary else colors.outline,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = feedbackText,
                                    onValueChange = { feedbackText = it },
                                    label = { Text("What did you like or want improved?") },
                                    minLines = 3,
                                    maxLines = 5,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("support_feedback_text"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colors.primary,
                                        unfocusedBorderColor = colors.outline
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        if (feedbackText.isNotBlank()) {
                                            onSubmitTicket(
                                                "FEEDBACK",
                                                "User Feedback (${feedbackRating}/5 Stars)",
                                                feedbackText,
                                                feedbackCategory,
                                                "Low",
                                                feedbackRating
                                            ) { ticketId ->
                                                submittedTicketId = ticketId
                                            }
                                        }
                                    },
                                    enabled = feedbackText.isNotBlank(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("submit_feedback_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.secondary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Feedback, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Submit Feedback", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        SupportTab.REQUEST_FEATURE -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = featureTitle,
                                    onValueChange = { featureTitle = it },
                                    label = { Text("Proposed Feature Title") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("support_feature_title"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colors.primary,
                                        unfocusedBorderColor = colors.outline
                                    )
                                )

                                OutlinedTextField(
                                    value = featureDescription,
                                    onValueChange = { featureDescription = it },
                                    label = { Text("Describe the Feature & Use Case") },
                                    minLines = 3,
                                    maxLines = 5,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("support_feature_description"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colors.primary,
                                        unfocusedBorderColor = colors.outline
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        if (featureTitle.isNotBlank() && featureDescription.isNotBlank()) {
                                            onSubmitTicket(
                                                "FEATURE_REQUEST",
                                                featureTitle,
                                                featureDescription,
                                                featureModule,
                                                "Medium",
                                                0
                                            ) { ticketId ->
                                                submittedTicketId = ticketId
                                            }
                                        }
                                    },
                                    enabled = featureTitle.isNotBlank() && featureDescription.isNotBlank(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("submit_feature_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Submit Feature Request", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
