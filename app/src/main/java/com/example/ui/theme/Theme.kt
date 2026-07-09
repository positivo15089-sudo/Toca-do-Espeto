package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkRedPrimary,
    onPrimary = Color.White,
    primaryContainer = DarkRedSecondary,
    onPrimaryContainer = GoldAccent,
    secondary = GoldAccent,
    onSecondary = CharcoalBlack,
    secondaryContainer = DeepGold,
    onSecondaryContainer = OffWhite,
    tertiary = DeepGold,
    onTertiary = CharcoalBlack,
    background = CharcoalBlack,
    onBackground = OffWhite,
    surface = DarkGraySurface,
    onSurface = OffWhite,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = LightGray,
    error = ErrorRed,
    onError = CharcoalBlack
)

private val LightColorScheme = lightColorScheme(
    primary = DarkRedPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = DarkRedSecondary,
    secondary = DeepGold,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF1C5),
    onSecondaryContainer = CharcoalBlack,
    tertiary = GoldAccent,
    onTertiary = CharcoalBlack,
    background = Color(0xFFFCFCFF),
    onBackground = CharcoalBlack,
    surface = Color.White,
    onSurface = CharcoalBlack,
    surfaceVariant = Color(0xFFF4F0F0),
    onSurfaceVariant = Color(0xFF43494A),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to Dark Theme for a premium steakhouse feel
    dynamicColor: Boolean = false, // Disable dynamic colors to enforce the beautiful Red, Black, and Gold branding
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
