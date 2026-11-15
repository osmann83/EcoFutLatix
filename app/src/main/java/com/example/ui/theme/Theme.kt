package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF00390A),
    primaryContainer = Color(0xFF1B5E20),
    onPrimaryContainer = Color(0xFFA5D6A7),
    secondary = Color(0xFF80CBC4),
    onSecondary = Color(0xFF003730),
    tertiary = Color(0xFFFFD54F),
    background = EcoDarkBackground,
    surface = EcoDarkSurface,
    surfaceVariant = EcoDarkSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = EcoGreenPrimary,
    onPrimary = EcoGreenOnPrimary,
    primaryContainer = EcoGreenPrimaryContainer,
    onPrimaryContainer = EcoGreenOnPrimaryContainer,
    secondary = EcoTealSecondary,
    onSecondary = EcoTealOnSecondary,
    secondaryContainer = EcoTealSecondaryContainer,
    onSecondaryContainer = EcoTealOnSecondaryContainer,
    tertiary = EcoAmberTertiary,
    onTertiary = EcoAmberOnTertiary,
    tertiaryContainer = EcoAmberTertiaryContainer,
    onTertiaryContainer = EcoAmberOnTertiaryContainer,
    background = EcoBackgroundLight,
    surface = EcoSurfaceLight,
    surfaceVariant = EcoSurfaceVariantLight
)

@Composable
fun EcoFutLatixTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Mantener paleta ecológica distintiva
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
