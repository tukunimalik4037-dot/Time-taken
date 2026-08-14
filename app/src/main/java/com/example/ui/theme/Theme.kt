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
    primary = GeometricPurpleTrack,
    onPrimary = GeometricDarkPurple,
    primaryContainer = GeometricActivePurple,
    onPrimaryContainer = GeometricLightPurple,
    secondary = GeometricPurpleTrack,
    onSecondary = GeometricDarkPurple,
    secondaryContainer = SurfaceDarkElevated,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = GeometricPurpleTrack,
    onTertiary = GeometricDarkPurple,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceDarkCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = SurfaceBorderDark,
    error = GeometricRedAlert,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = GeometricPurple,
    onPrimary = Color.White,
    primaryContainer = GeometricLightPurple,
    onPrimaryContainer = GeometricDarkPurple,
    secondary = GeometricPurple,
    onSecondary = Color.White,
    secondaryContainer = GeometricSurface,
    onSecondaryContainer = GeometricTextPrimary,
    tertiary = GeometricPurple,
    onTertiary = Color.White,
    background = GeometricBackground,
    onBackground = GeometricTextPrimary,
    surface = GeometricCardBg,
    onSurface = GeometricTextPrimary,
    surfaceVariant = GeometricSurface,
    onSurfaceVariant = GeometricTextSecondary,
    outline = GeometricBorder,
    error = GeometricRedAlert,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme // Geometric Balance light theme by default
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

