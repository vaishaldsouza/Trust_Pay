package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalAppColors

/**
 * Reusable Shimmer Brush providing animated left-to-right sweeping light gradient.
 */
@Composable
fun rememberShimmerBrush(): Brush {
    val colors = LocalAppColors.current
    val baseColor = if (colors.isDark) Color(0xFF2C2C2E) else Color(0xFFE0E0E0)
    val highlightColor = if (colors.isDark) Color(0xFF3A3A3C) else Color(0xFFF5F5F5)

    val shimmerColors = listOf(
        baseColor,
        highlightColor,
        baseColor
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslation"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnimation - 350f, translateAnimation - 350f),
        end = Offset(translateAnimation, translateAnimation)
    )
}

/**
 * Basic Shimmer Box placeholder shape.
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp)
) {
    val brush = rememberShimmerBrush()
    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

/**
 * Skeleton Loader for BuyerHomeScreen (Balance Card, Offline Allowance Card, Pending Sync Card, Transaction Rows).
 */
@Composable
fun BuyerHomeSkeleton(modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Balance Card Skeleton
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
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
                    ShimmerBox(modifier = Modifier.size(width = 120.dp, height = 16.dp))
                    ShimmerBox(modifier = Modifier.size(24.dp), shape = CircleShape)
                }
                ShimmerBox(modifier = Modifier.size(width = 180.dp, height = 36.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShimmerBox(modifier = Modifier.size(width = 90.dp, height = 24.dp), shape = RoundedCornerShape(12.dp))
                    ShimmerBox(modifier = Modifier.size(width = 110.dp, height = 24.dp), shape = RoundedCornerShape(12.dp))
                }
            }
        }

        // Offline Allowance Card Skeleton
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShimmerBox(modifier = Modifier.size(width = 140.dp, height = 16.dp))
                    ShimmerBox(modifier = Modifier.size(width = 100.dp, height = 20.dp))
                }
                ShimmerBox(modifier = Modifier.size(width = 80.dp, height = 36.dp), shape = RoundedCornerShape(8.dp))
            }
        }

        // Recent Transactions Section Header Skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ShimmerBox(modifier = Modifier.size(width = 150.dp, height = 18.dp))
            ShimmerBox(modifier = Modifier.size(width = 60.dp, height = 18.dp))
        }

        // Transaction List Skeleton Rows
        TransactionListSkeleton(count = 3)
    }
}

/**
 * Reusable Transaction List Item Skeleton Rows.
 */
@Composable
fun TransactionListSkeleton(
    count: Int = 4,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(count) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ShimmerBox(modifier = Modifier.size(42.dp), shape = CircleShape)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            ShimmerBox(modifier = Modifier.size(width = 130.dp, height = 15.dp))
                            ShimmerBox(modifier = Modifier.size(width = 90.dp, height = 12.dp))
                        }
                    }
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ShimmerBox(modifier = Modifier.size(width = 70.dp, height = 16.dp))
                        ShimmerBox(modifier = Modifier.size(width = 60.dp, height = 18.dp), shape = RoundedCornerShape(10.dp))
                    }
                }
            }
        }
    }
}

/**
 * Skeleton Loader for ActivitySyncScreen (Reconciliation Checklist & Settlement Cards).
 */
@Composable
fun SyncActivitySkeleton(modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShimmerBox(modifier = Modifier.size(width = 160.dp, height = 18.dp))
                ShimmerBox(modifier = Modifier.size(width = 240.dp, height = 14.dp))
                Spacer(modifier = Modifier.height(4.dp))

                repeat(3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ShimmerBox(modifier = Modifier.size(20.dp), shape = CircleShape)
                            Spacer(modifier = Modifier.width(10.dp))
                            ShimmerBox(modifier = Modifier.size(width = 140.dp, height = 14.dp))
                        }
                        ShimmerBox(modifier = Modifier.size(width = 60.dp, height = 14.dp))
                    }
                }
            }
        }
    }
}

/**
 * Skeleton Loader for SecurityCenterScreen (Dual Rule-Based & ML Prediction Score Cards).
 */
@Composable
fun SecurityCenterSkeleton(modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Dual Risk Cards Skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShimmerBox(modifier = Modifier.size(width = 80.dp, height = 12.dp))
                    ShimmerBox(modifier = Modifier.size(width = 60.dp, height = 24.dp))
                    ShimmerBox(modifier = Modifier.size(width = 90.dp, height = 10.dp))
                }
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShimmerBox(modifier = Modifier.size(width = 80.dp, height = 12.dp))
                    ShimmerBox(modifier = Modifier.size(width = 60.dp, height = 24.dp))
                    ShimmerBox(modifier = Modifier.size(width = 90.dp, height = 10.dp))
                }
            }
        }

        // Fraud Alerts Skeleton Header
        ShimmerBox(modifier = Modifier.size(width = 160.dp, height = 18.dp))

        // Fraud Alert Item Skeletons
        repeat(2) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceLowest),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ShimmerBox(modifier = Modifier.size(width = 120.dp, height = 16.dp))
                        ShimmerBox(modifier = Modifier.size(width = 50.dp, height = 18.dp), shape = RoundedCornerShape(8.dp))
                    }
                    ShimmerBox(modifier = Modifier.size(width = 220.dp, height = 12.dp))
                }
            }
        }
    }
}
