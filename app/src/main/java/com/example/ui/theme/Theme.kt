package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.example.util.AppThemeMode

private val DarkColorScheme =
    darkColorScheme(
        primary = TrustPayPrimaryDark,
        onPrimary = TrustPayOnPrimaryDark,
        primaryContainer = TrustPayPrimaryContainerDark,
        onPrimaryContainer = TrustPayOnPrimaryContainerDark,
        secondary = TrustPaySecondaryDark,
        onSecondary = TrustPayOnSecondaryDark,
        secondaryContainer = TrustPaySecondaryFixedDark,
        onSecondaryContainer = TrustPayOnSecondaryFixedDark,
        background = TrustPayBackgroundDark,
        surface = TrustPaySurfaceDark,
        surfaceVariant = TrustPaySurfaceContainerDark,
        onBackground = TrustPayOnSurfaceDark,
        onSurface = TrustPayOnSurfaceDark,
        onSurfaceVariant = TrustPayOnSurfaceVariantDark,
        outline = TrustPayOutlineDark,
        outlineVariant = TrustPayOutlineVariantDark,
        error = TrustPayErrorDark,
        errorContainer = TrustPayErrorContainerDark,
        onErrorContainer = TrustPayOnErrorContainerDark
    )

private val LightColorScheme =
    lightColorScheme(
        primary = TrustPayPrimaryLight,
        onPrimary = TrustPayOnPrimaryLight,
        primaryContainer = TrustPayPrimaryContainerLight,
        onPrimaryContainer = TrustPayOnPrimaryContainerLight,
        secondary = TrustPaySecondaryLight,
        onSecondary = TrustPayOnSecondaryLight,
        secondaryContainer = TrustPaySecondaryFixedLight,
        onSecondaryContainer = TrustPayOnSecondaryFixedLight,
        background = TrustPayBackgroundLight,
        surface = TrustPaySurfaceLight,
        surfaceVariant = TrustPaySurfaceHighestLight,
        onBackground = TrustPayOnSurfaceLight,
        onSurface = TrustPayOnSurfaceLight,
        onSurfaceVariant = TrustPayOnSurfaceVariantLight,
        outline = TrustPayOutlineLight,
        outlineVariant = TrustPayOutlineVariantLight,
        error = TrustPayErrorLight,
        errorContainer = TrustPayErrorContainerLight,
        onErrorContainer = TrustPayOnErrorContainerLight
    )

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    // Sync legacy color vars for compatibility
    if (isDark) {
        TrustPayBackground = TrustPayBackgroundDark
        TrustPaySurface = TrustPaySurfaceDark
        TrustPaySurfaceLowest = TrustPaySurfaceLowestDark
        TrustPaySurfaceLow = TrustPaySurfaceLowDark
        TrustPaySurfaceContainer = TrustPaySurfaceContainerDark
        TrustPaySurfaceHigh = TrustPaySurfaceHighDark
        TrustPaySurfaceHighest = TrustPaySurfaceHighestDark
        TrustPayOnSurface = TrustPayOnSurfaceDark
        TrustPayOnSurfaceVariant = TrustPayOnSurfaceVariantDark
        TrustPayOutline = TrustPayOutlineDark
        TrustPayOutlineVariant = TrustPayOutlineVariantDark
        TrustPayPrimary = TrustPayPrimaryDark
        TrustPayOnPrimary = TrustPayOnPrimaryDark
        TrustPayPrimaryContainer = TrustPayPrimaryContainerDark
        TrustPayOnPrimaryContainer = TrustPayOnPrimaryContainerDark
        TrustPaySecondary = TrustPaySecondaryDark
        TrustPayOnSecondary = TrustPayOnSecondaryDark
        TrustPaySecondaryFixed = TrustPaySecondaryFixedDark
        TrustPaySecondaryFixedDim = TrustPaySecondaryFixedDimDark
        TrustPayOnSecondaryFixed = TrustPayOnSecondaryFixedDark
        TrustPayWarning = TrustPayWarningDark
        TrustPayWarningContainer = TrustPayWarningContainerDark
        TrustPayWarningDim = TrustPayWarningDimDark
        TrustPayError = TrustPayErrorDark
        TrustPayErrorContainer = TrustPayErrorContainerDark
        TrustPayOnErrorContainer = TrustPayOnErrorContainerDark
    } else {
        TrustPayBackground = TrustPayBackgroundLight
        TrustPaySurface = TrustPaySurfaceLight
        TrustPaySurfaceLowest = TrustPaySurfaceLowestLight
        TrustPaySurfaceLow = TrustPaySurfaceLowLight
        TrustPaySurfaceContainer = TrustPaySurfaceContainerLight
        TrustPaySurfaceHigh = TrustPaySurfaceHighLight
        TrustPaySurfaceHighest = TrustPaySurfaceHighestLight
        TrustPayOnSurface = TrustPayOnSurfaceLight
        TrustPayOnSurfaceVariant = TrustPayOnSurfaceVariantLight
        TrustPayOutline = TrustPayOutlineLight
        TrustPayOutlineVariant = TrustPayOutlineVariantLight
        TrustPayPrimary = TrustPayPrimaryLight
        TrustPayOnPrimary = TrustPayOnPrimaryLight
        TrustPayPrimaryContainer = TrustPayPrimaryContainerLight
        TrustPayOnPrimaryContainer = TrustPayOnPrimaryContainerLight
        TrustPaySecondary = TrustPaySecondaryLight
        TrustPayOnSecondary = TrustPayOnSecondaryLight
        TrustPaySecondaryFixed = TrustPaySecondaryFixedLight
        TrustPaySecondaryFixedDim = TrustPaySecondaryFixedDimLight
        TrustPayOnSecondaryFixed = TrustPayOnSecondaryFixedLight
        TrustPayWarning = TrustPayWarningLight
        TrustPayWarningContainer = TrustPayWarningContainerLight
        TrustPayWarningDim = TrustPayWarningDimLight
        TrustPayError = TrustPayErrorLight
        TrustPayErrorContainer = TrustPayErrorContainerLight
        TrustPayOnErrorContainer = TrustPayOnErrorContainerLight
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme
    val appColors = if (isDark) DarkColorsInstance else LightColorsInstance

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun TrustPayTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MyApplicationTheme(
        themeMode = themeMode,
        dynamicColor = dynamicColor,
        content = content
    )
}
