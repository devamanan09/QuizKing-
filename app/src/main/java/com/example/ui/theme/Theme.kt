package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = GlassCanvasBg,
    primaryContainer = GlassSurfaceElevatedSolid,
    onPrimaryContainer = NeonCyanLight,
    secondary = ElectricBlue,
    onSecondary = GlassCanvasBg,
    secondaryContainer = GlassSurfaceHighlightSolid,
    onSecondaryContainer = TextPrimary,
    tertiary = AccentGold,
    onTertiary = GlassCanvasBg,
    background = GlassCanvasBg,
    onBackground = TextPrimary,
    surface = GlassSurfaceSolid,
    onSurface = TextPrimary,
    surfaceVariant = GlassSurfaceElevatedSolid,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    error = ErrorRed,
    onError = TextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = GlassCanvasBg.toArgb()
            window.navigationBarColor = GlassCanvasBg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
