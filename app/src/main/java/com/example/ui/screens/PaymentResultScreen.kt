package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.ui.theme.LocalAppColors
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class PaymentResultType {
    SUCCESS,
    PENDING,
    FAILURE
}

/**
 * Dedicated Full-Screen Animated Payment Result Screen matching GPay / PhonePe / Paytm patterns.
 * Provides distinct visual themes, draw-on stroke animations, and haptic feedback tied to TransactionStatus.
 */
@Composable
fun PaymentResultScreen(
    transaction: Transaction,
    onDone: () -> Unit,
    onTryAgain: () -> Unit,
    onViewReceipt: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val haptic = LocalHapticFeedback.current

    val resultType = when (transaction.status) {
        TransactionStatus.SETTLED -> PaymentResultType.SUCCESS
        TransactionStatus.SETTLEMENT_PENDING, TransactionStatus.OFFLINE_ACCEPTED, TransactionStatus.PENDING_SYNC -> PaymentResultType.PENDING
        TransactionStatus.SETTLEMENT_FAILED, TransactionStatus.INVALID_SIGNATURE,
        TransactionStatus.DUPLICATE, TransactionStatus.REPLAY_DETECTED,
        TransactionStatus.FRAUD_REVIEW, TransactionStatus.CANCELLED -> PaymentResultType.FAILURE
        else -> PaymentResultType.PENDING
    }

    val themeBackgroundColor = when (resultType) {
        PaymentResultType.SUCCESS -> if (colors.isDark) Color(0xFF0F3820) else Color(0xFFE8F5E9)
        PaymentResultType.PENDING -> if (colors.isDark) Color(0xFF3E2723) else Color(0xFFFFF8E1)
        PaymentResultType.FAILURE -> if (colors.isDark) Color(0xFF3B1215) else Color(0xFFFFEBEE)
    }

    val themePrimaryColor = when (resultType) {
        PaymentResultType.SUCCESS -> Color(0xFF2E7D32)
        PaymentResultType.PENDING -> Color(0xFFF57F17)
        PaymentResultType.FAILURE -> Color(0xFFD32F2F)
    }

    // Animation progress state (0f to 1f)
    val checkmarkAnim = remember { Animatable(0f) }
    LaunchedEffect(resultType) {
        if (resultType == PaymentResultType.SUCCESS) {
            checkmarkAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
            )
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } else {
            checkmarkAnim.snapTo(1f)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeBackgroundColor)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Center Content Container
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Animated Status Circle Icon
            when (resultType) {
                PaymentResultType.SUCCESS -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(110.dp)
                    ) {
                        Canvas(modifier = Modifier.size(100.dp)) {
                            val strokeWidth = 8.dp.toPx()
                            val radius = size.minDimension / 2 - strokeWidth
                            val center = Offset(size.width / 2, size.height / 2)

                            // Outer background circle
                            drawCircle(
                                color = themePrimaryColor.copy(alpha = 0.2f),
                                radius = radius,
                                center = center,
                                style = Stroke(width = strokeWidth)
                            )

                            // Animated progress circle sweep
                            drawArc(
                                color = themePrimaryColor,
                                startAngle = -90f,
                                sweepAngle = 360f * checkmarkAnim.value,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )

                            // Animated Checkmark stroke
                            if (checkmarkAnim.value > 0.4f) {
                                val p = ((checkmarkAnim.value - 0.4f) / 0.6f).coerceIn(0f, 1f)
                                val path = Path().apply {
                                    moveTo(size.width * 0.28f, size.height * 0.52f)
                                    lineTo(size.width * 0.45f, size.height * 0.68f)
                                    lineTo(size.width * 0.72f, size.height * 0.36f)
                                }
                                drawPath(
                                    path = path,
                                    color = themePrimaryColor,
                                    style = Stroke(width = strokeWidth * 1.1f, cap = StrokeCap.Round)
                                )
                            }
                        }
                    }
                }

                PaymentResultType.PENDING -> {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2400, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "rotate"
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(themePrimaryColor.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassEmpty,
                            contentDescription = "Pending",
                            tint = themePrimaryColor,
                            modifier = Modifier
                                .size(56.dp)
                                .rotate(rotation)
                        )
                    }
                }

                PaymentResultType.FAILURE -> {
                    val infiniteTransition = rememberInfiniteTransition(label = "shake")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 0.95f,
                        targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale"
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(100.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(themePrimaryColor.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Failed",
                            tint = themePrimaryColor,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Result Headline Title
            val titleText = when (resultType) {
                PaymentResultType.SUCCESS -> "Payment Successful"
                PaymentResultType.PENDING -> "Payment Authorized — Settling"
                PaymentResultType.FAILURE -> "Payment Could Not Complete"
            }

            Text(
                text = titleText,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Large Bold Amount Display
            val formattedAmount = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                .format(transaction.amount)
            Text(
                text = formattedAmount,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = themePrimaryColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Recipient & Date Details
            Text(
                text = "Paid to ${transaction.merchantName}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                .format(Date(transaction.timestamp))
            Text(
                text = dateStr,
                fontSize = 12.sp,
                color = colors.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Explanatory Banner Card
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.9f)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (resultType) {
                        PaymentResultType.SUCCESS -> {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Razorpay Ref:", fontSize = 12.sp, color = colors.onSurfaceVariant)
                                Text(
                                    text = transaction.settlementRef ?: transaction.transactionId,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onSurface
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Mode:", fontSize = 12.sp, color = colors.onSurfaceVariant)
                                Text(
                                    text = transaction.mode.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.primary
                                )
                            }
                        }

                        PaymentResultType.PENDING -> {
                            Text(
                                text = "Awaiting mandate confirmation — will settle automatically once Razorpay processes the recurring charge.",
                                fontSize = 13.sp,
                                color = colors.onSurface,
                                lineHeight = 18.sp
                            )
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Text("Order ID:", fontSize = 12.sp, color = colors.onSurfaceVariant)
                                Text(
                                    text = transaction.transactionId,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onSurface
                                )
                            }
                        }

                        PaymentResultType.FAILURE -> {
                            val failureReason = transaction.fraudReasons.firstOrNull()
                                ?: "Cryptographic signature or exposure verification failed."
                            Text(
                                text = failureReason,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.error,
                                lineHeight = 18.sp
                            )
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Text("Txn Ref:", fontSize = 12.sp, color = colors.onSurfaceVariant)
                                Text(
                                    text = transaction.transactionId,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons Group
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            when (resultType) {
                PaymentResultType.FAILURE -> {
                    Button(
                        onClick = onTryAgain,
                        colors = ButtonDefaults.buttonColors(containerColor = themePrimaryColor),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Try Again", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = onDone,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Go Home", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.onSurface)
                    }
                }

                else -> {
                    Button(
                        onClick = onDone,
                        colors = ButtonDefaults.buttonColors(containerColor = themePrimaryColor),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text("Done", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = onViewReceipt,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = colors.onSurface, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View Receipt", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.onSurface)
                    }
                }
            }
        }
    }
}
