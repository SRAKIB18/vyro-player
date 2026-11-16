package com.vyro.player.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.vyro.player.domain.model.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = VyroEmeraldPrimary,
    secondary = VyroCyanSecondary,
    background = VyroBlackBackground,
    surface = VyroDarkCardSurface,
    surfaceVariant = VyroDarkCardSurfaceVariant,
    onPrimary = VyroBlackBackground,
    onBackground = VyroTextPrimary,
    onSurface = VyroTextPrimary,
    onSurfaceVariant = VyroTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = VyroLightPrimary,
    secondary = VyroLightSecondary,
    background = VyroLightBackground,
    surface = VyroLightSurface,
    surfaceVariant = VyroLightSurfaceVariant,
    onPrimary = VyroLightSurface,
    onBackground = Color(0xFF1A1C24),
    onSurface = Color(0xFF1A1C24),
    onSurfaceVariant = Color(0xFF5E6578)
)

@Composable
fun VYROPlayerTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
