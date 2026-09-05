package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// Default Light Palette
val TrustPayBackgroundLight = Color(0xFFFCF8FA)
val TrustPaySurfaceLight = Color(0xFFFCF8FA)
val TrustPaySurfaceLowestLight = Color(0xFFFFFFFF)
val TrustPaySurfaceLowLight = Color(0xFFF6F3F4)
val TrustPaySurfaceContainerLight = Color(0xFFF0EDEF)
val TrustPaySurfaceHighLight = Color(0xFFEAE7E9)
val TrustPaySurfaceHighestLight = Color(0xFFE4E2E3)

val TrustPayOnSurfaceLight = Color(0xFF1B1B1D)
val TrustPayOnSurfaceVariantLight = Color(0xFF45474C)
val TrustPayOutlineLight = Color(0xFF76777D)
val TrustPayOutlineVariantLight = Color(0xFFC6C6CD)

val TrustPayPrimaryLight = Color(0xFF0F172A)
val TrustPayOnPrimaryLight = Color(0xFFFFFFFF)
val TrustPayPrimaryContainerLight = Color(0xFFE0F2FE)
val TrustPayOnPrimaryContainerLight = Color(0xFF0369A1)

val TrustPaySecondaryLight = Color(0xFF006C49)
val TrustPayOnSecondaryLight = Color(0xFFFFFFFF)
val TrustPaySecondaryFixedLight = Color(0xFFD1FAE5)
val TrustPaySecondaryFixedDimLight = Color(0xFFA7F3D0)
val TrustPayOnSecondaryFixedLight = Color(0xFF065F46)

val TrustPayWarningLight = Color(0xFFF59E0B)
val TrustPayWarningContainerLight = Color(0xFFFFDDB8)
val TrustPayWarningDimLight = Color(0xFFFFB95F)

val TrustPayErrorLight = Color(0xFFBA1A1A)
val TrustPayErrorContainerLight = Color(0xFFFFDAD6)
val TrustPayOnErrorContainerLight = Color(0xFF93000A)

// Dark Palette
val TrustPayBackgroundDark = Color(0xFF0B0F19)
val TrustPaySurfaceDark = Color(0xFF111827)
val TrustPaySurfaceLowestDark = Color(0xFF161F30)
val TrustPaySurfaceLowDark = Color(0xFF1F2937)
val TrustPaySurfaceContainerDark = Color(0xFF263245)
val TrustPaySurfaceHighDark = Color(0xFF334155)
val TrustPaySurfaceHighestDark = Color(0xFF475569)

val TrustPayOnSurfaceDark = Color(0xFFF8FAFC)
val TrustPayOnSurfaceVariantDark = Color(0xFF94A3B8)
val TrustPayOutlineDark = Color(0xFF64748B)
val TrustPayOutlineVariantDark = Color(0xFF334155)

val TrustPayPrimaryDark = Color(0xFF38BDF8)
val TrustPayOnPrimaryDark = Color(0xFF0F172A)
val TrustPayPrimaryContainerDark = Color(0xFF1E293B)
val TrustPayOnPrimaryContainerDark = Color(0xFFBAE6FD)

val TrustPaySecondaryDark = Color(0xFF10B981)
val TrustPayOnSecondaryDark = Color(0xFF022C22)
val TrustPaySecondaryFixedDark = Color(0xFF065F46)
val TrustPaySecondaryFixedDimDark = Color(0xFF34D399)
val TrustPayOnSecondaryFixedDark = Color(0xFFD1FAE5)

val TrustPayWarningDark = Color(0xFFFBBF24)
val TrustPayWarningContainerDark = Color(0xFF78350F)
val TrustPayWarningDimDark = Color(0xFFFCD34D)

val TrustPayErrorDark = Color(0xFFF87171)
val TrustPayErrorContainerDark = Color(0xFF7F1D1D)
val TrustPayOnErrorContainerDark = Color(0xFFFECACA)

// Legacy alias compatibility
var TrustPayBackground = TrustPayBackgroundLight
var TrustPaySurface = TrustPaySurfaceLight
var TrustPaySurfaceLowest = TrustPaySurfaceLowestLight
var TrustPaySurfaceLow = TrustPaySurfaceLowLight
var TrustPaySurfaceContainer = TrustPaySurfaceContainerLight
var TrustPaySurfaceHigh = TrustPaySurfaceHighLight
var TrustPaySurfaceHighest = TrustPaySurfaceHighestLight
var TrustPayOnSurface = TrustPayOnSurfaceLight
var TrustPayOnSurfaceVariant = TrustPayOnSurfaceVariantLight
var TrustPayOutline = TrustPayOutlineLight
var TrustPayOutlineVariant = TrustPayOutlineVariantLight
var TrustPayPrimary = TrustPayPrimaryLight
var TrustPayOnPrimary = TrustPayOnPrimaryLight
var TrustPayPrimaryContainer = TrustPayPrimaryContainerLight
var TrustPayOnPrimaryContainer = TrustPayOnPrimaryContainerLight
var TrustPaySecondary = TrustPaySecondaryLight
var TrustPayOnSecondary = TrustPayOnSecondaryLight
var TrustPaySecondaryFixed = TrustPaySecondaryFixedLight
var TrustPaySecondaryFixedDim = TrustPaySecondaryFixedDimLight
var TrustPayOnSecondaryFixed = TrustPayOnSecondaryFixedLight
var TrustPayWarning = TrustPayWarningLight
var TrustPayWarningContainer = TrustPayWarningContainerLight
var TrustPayWarningDim = TrustPayWarningDimLight
var TrustPayError = TrustPayErrorLight
var TrustPayErrorContainer = TrustPayErrorContainerLight
var TrustPayOnErrorContainer = TrustPayOnErrorContainerLight

data class AppColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceLowest: Color,
    val surfaceLow: Color,
    val surfaceContainer: Color,
    val surfaceHigh: Color,
    val surfaceHighest: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val outlineVariant: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryFixed: Color,
    val secondaryFixedDim: Color,
    val onSecondaryFixed: Color,
    val warning: Color,
    val warningContainer: Color,
    val warningDim: Color,
    val error: Color,
    val errorContainer: Color,
    val onErrorContainer: Color
)

val LightColorsInstance = AppColors(
    isDark = false,
    background = TrustPayBackgroundLight,
    surface = TrustPaySurfaceLight,
    surfaceLowest = TrustPaySurfaceLowestLight,
    surfaceLow = TrustPaySurfaceLowLight,
    surfaceContainer = TrustPaySurfaceContainerLight,
    surfaceHigh = TrustPaySurfaceHighLight,
    surfaceHighest = TrustPaySurfaceHighestLight,
    onSurface = TrustPayOnSurfaceLight,
    onSurfaceVariant = TrustPayOnSurfaceVariantLight,
    outline = TrustPayOutlineLight,
    outlineVariant = TrustPayOutlineVariantLight,
    primary = TrustPayPrimaryLight,
    onPrimary = TrustPayOnPrimaryLight,
    primaryContainer = TrustPayPrimaryContainerLight,
    onPrimaryContainer = TrustPayOnPrimaryContainerLight,
    secondary = TrustPaySecondaryLight,
    onSecondary = TrustPayOnSecondaryLight,
    secondaryFixed = TrustPaySecondaryFixedLight,
    secondaryFixedDim = TrustPaySecondaryFixedDimLight,
    onSecondaryFixed = TrustPayOnSecondaryFixedLight,
    warning = TrustPayWarningLight,
    warningContainer = TrustPayWarningContainerLight,
    warningDim = TrustPayWarningDimLight,
    error = TrustPayErrorLight,
    errorContainer = TrustPayErrorContainerLight,
    onErrorContainer = TrustPayOnErrorContainerLight
)

val DarkColorsInstance = AppColors(
    isDark = true,
    background = TrustPayBackgroundDark,
    surface = TrustPaySurfaceDark,
    surfaceLowest = TrustPaySurfaceLowestDark,
    surfaceLow = TrustPaySurfaceLowDark,
    surfaceContainer = TrustPaySurfaceContainerDark,
    surfaceHigh = TrustPaySurfaceHighDark,
    surfaceHighest = TrustPaySurfaceHighestDark,
    onSurface = TrustPayOnSurfaceDark,
    onSurfaceVariant = TrustPayOnSurfaceVariantDark,
    outline = TrustPayOutlineDark,
    outlineVariant = TrustPayOutlineVariantDark,
    primary = TrustPayPrimaryDark,
    onPrimary = TrustPayOnPrimaryDark,
    primaryContainer = TrustPayPrimaryContainerDark,
    onPrimaryContainer = TrustPayOnPrimaryContainerDark,
    secondary = TrustPaySecondaryDark,
    onSecondary = TrustPayOnSecondaryDark,
    secondaryFixed = TrustPaySecondaryFixedDark,
    secondaryFixedDim = TrustPaySecondaryFixedDimDark,
    onSecondaryFixed = TrustPayOnSecondaryFixedDark,
    warning = TrustPayWarningDark,
    warningContainer = TrustPayWarningContainerDark,
    warningDim = TrustPayWarningDimDark,
    error = TrustPayErrorDark,
    errorContainer = TrustPayErrorContainerDark,
    onErrorContainer = TrustPayOnErrorContainerDark
)

val LocalAppColors = compositionLocalOf { LightColorsInstance }
