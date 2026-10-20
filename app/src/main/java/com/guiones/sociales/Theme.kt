package com.guiones.sociales

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

// Paleta tomada del icono: cielo del Egeo, laurel, mármol y el pelaje del filósofo.
private val LightColors = lightColorScheme(
    primary = Color(0xFF2B6E8F), onPrimary = Color.White,
    primaryContainer = Color(0xFFD5EAF4), onPrimaryContainer = Color(0xFF0B2F40),
    secondary = Color(0xFF5E7A34), onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0EBC8), onSecondaryContainer = Color(0xFF1C2A06),
    tertiary = Color(0xFF86552F), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF1DFCC), onTertiaryContainer = Color(0xFF3A2010),
    background = Color(0xFFF4F5F1), onBackground = Color(0xFF1E2326),
    surface = Color(0xFFFCFCFA), onSurface = Color(0xFF1E2326),
    surfaceVariant = Color(0xFFE6EAE6), onSurfaceVariant = Color(0xFF4A5358),
    outline = Color(0xFF9AA5A8), outlineVariant = Color(0xFFD8DEDC),
    surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF7F8F5),
    surfaceContainer = Color(0xFFF0F2EE), surfaceContainerHigh = Color(0xFFEAEDE9),
    surfaceContainerHighest = Color(0xFFE4E8E3)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8CC8E0), onPrimary = Color(0xFF06303F),
    primaryContainer = Color(0xFF1E4C63), onPrimaryContainer = Color(0xFFD5EAF4),
    secondary = Color(0xFFBFD58F), onSecondary = Color(0xFF24330A),
    secondaryContainer = Color(0xFF3A4C1E), onSecondaryContainer = Color(0xFFE0EBC8),
    tertiary = Color(0xFFE3B48A), onTertiary = Color(0xFF3E2410),
    tertiaryContainer = Color(0xFF5B3820), onTertiaryContainer = Color(0xFFF1DFCC),
    background = Color(0xFF11181C), onBackground = Color(0xFFE4E8EA),
    surface = Color(0xFF161E23), onSurface = Color(0xFFE4E8EA),
    surfaceVariant = Color(0xFF28333A), onSurfaceVariant = Color(0xFFB7C2C7),
    outline = Color(0xFF74828A), outlineVariant = Color(0xFF36434B),
    surfaceContainerLowest = Color(0xFF0D1316), surfaceContainerLow = Color(0xFF141C21),
    surfaceContainer = Color(0xFF1A242A), surfaceContainerHigh = Color(0xFF212C33),
    surfaceContainerHighest = Color(0xFF28343B)
)

private val base = Typography()
private val AppTypography = base.copy(
    headlineMedium = base.headlineMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold)
)

@Composable
fun GuionesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
