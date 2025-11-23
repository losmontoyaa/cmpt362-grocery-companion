package com.example.grocerycompanion.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// LIGHT MODE – what you care about most right now
private val LightColorScheme = lightColorScheme(
    primary = BrandGreen,
    onPrimary = Color.White,

    secondary = BrandTeal,
    onSecondary = Color.White,

    tertiary = BrandYellow,
    onTertiary = TextPrimary,

    background = SurfaceSoft,
    onBackground = TextPrimary,

    surface = SurfaceCard,
    onSurface = TextPrimary,

    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,

    error = ErrorRed,
    onError = Color.White,

    outline = TextSecondary
)

// DARK MODE – still branded, not default purple
private val DarkColorScheme = darkColorScheme(
    primary = BrandGreen,
    onPrimary = Color.Black,

    secondary = BrandTeal,
    onSecondary = Color.Black,

    tertiary = BrandYellow,
    onTertiary = Color.Black,

    background = Color(0xFF020617),
    onBackground = Color(0xFFE5E7EB),

    surface = Color(0xFF020617),
    onSurface = Color(0xFFE5E7EB),

    surfaceVariant = Color(0xFF1F2933),
    onSurfaceVariant = Color(0xFFCBD5F5),

    error = ErrorRed,
    onError = Color.White,

    outline = Color(0xFF94A3B8)
)

@Composable
fun GroceryCompanionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // ❗ Force your own palette, not Android's wallpaper colors
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
