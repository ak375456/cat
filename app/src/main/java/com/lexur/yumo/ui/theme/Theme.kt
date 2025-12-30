package com.lexur.yumo.ui.theme

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

// Light Theme Colors (Your existing colors)
val AppLightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = Container,
    onPrimaryContainer = OnContainer,

    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = ContainerHigh,
    onSecondaryContainer = OnContainer,

    tertiary = MainColor,
    onTertiary = OnPrimary,

    background = Background,
    onBackground = OnBackground,

    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceContainer = Container,
    surfaceContainerHigh = ContainerHigh,
    surfaceContainerHighest = CardBackground,

    outline = OutlinePrimary,
    outlineVariant = OutlineVariant,

    error = Error,
    onError = OnError,
    errorContainer = Error.copy(alpha = 0.1f),
    onErrorContainer = OnError
)

// Dark Theme Colors - Eye-pleasing dark palette matching your pink theme
val AppDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB8D3),              // Lighter soft pink for dark mode
    onPrimary = Color(0xFF2D1B22),            // Very dark for text on primary
    primaryContainer = Color(0xFF8B5A6F),     // Muted dark pink container
    onPrimaryContainer = Color(0xFFFFDAE8),   // Light pink text on container

    secondary = Color(0xFF4A3840),            // Dark muted pink-brown
    onSecondary = Color(0xFFE8D5DC),          // Light pink text
    secondaryContainer = Color(0xFF3D2E35),   // Darker container
    onSecondaryContainer = Color(0xFFFFDAE8), // Light pink text

    tertiary = Color(0xFFD4A5B8),             // Soft muted pink
    onTertiary = Color(0xFF3A2329),           // Dark text on tertiary

    background = Color(0xFF1C1618),           // Very dark background with pink undertone
    onBackground = Color(0xFFECE0E3),         // Light text on dark background

    surface = Color(0xFF231D1F),              // Dark surface with slight pink tint
    onSurface = Color(0xFFECE0E3),            // Light text on surface
    surfaceVariant = Color(0xFF2D2528),       // Slightly lighter surface variant
    onSurfaceVariant = Color(0xFFD0C3C7),     // Muted light text
    surfaceContainer = Color(0xFF2D2528),     // Container color
    surfaceContainerHigh = Color(0xFF3D3338), // High emphasis container
    surfaceContainerHighest = Color(0xFF4A4044), // Highest emphasis container

    outline = Color(0xFF9A8C91),              // Muted outline
    outlineVariant = Color(0xFF4A4044),       // Darker outline variant

    error = Color(0xFFFFB4AB),                // Soft red for dark mode
    onError = Color(0xFF690005),              // Dark text on error
    errorContainer = Color(0xFF93000A),       // Dark error container
    onErrorContainer = Color(0xFFFFDAD6),     // Light text on error container

    surfaceTint = Color(0xFFFFB8D3),          // Pink tint for surfaces
)

@Composable
fun CatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Set to false to use our custom colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> AppDarkColorScheme
        else -> AppLightColorScheme
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

// Extension properties for custom colors that work with both themes
val androidx.compose.material3.ColorScheme.mainColor: Color
    get() = tertiary

val androidx.compose.material3.ColorScheme.buttonPrimary: Color
    get() = primaryContainer

val androidx.compose.material3.ColorScheme.buttonSecondary: Color
    get() = secondaryContainer

val androidx.compose.material3.ColorScheme.cardBackground: Color
    get() = surfaceContainerHighest

val androidx.compose.material3.ColorScheme.containerColor: Color
    get() = surfaceContainer

val androidx.compose.material3.ColorScheme.containerHigh: Color
    get() = surfaceContainerHigh

val androidx.compose.material3.ColorScheme.iconPrimary: Color
    get() = onSurfaceVariant

val androidx.compose.material3.ColorScheme.iconSecondary: Color
    get() = onSurfaceVariant.copy(alpha = 0.7f)

val androidx.compose.material3.ColorScheme.inputBackground: Color
    get() = surfaceContainerHighest

val androidx.compose.material3.ColorScheme.inputBorder: Color
    get() = outline

val androidx.compose.material3.ColorScheme.inputBorderFocused: Color
    get() = primary

val androidx.compose.material3.ColorScheme.success: Color
    get() = Success

val androidx.compose.material3.ColorScheme.warning: Color
    get() = Warning

val androidx.compose.material3.ColorScheme.info: Color
    get() = Info