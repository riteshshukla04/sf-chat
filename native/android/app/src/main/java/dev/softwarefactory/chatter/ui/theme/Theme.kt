package dev.softwarefactory.chatter.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Mirrors `Colors` / `Palette` in src/constants/theme.ts, value for value. */
data class ChatterColors(
    val background: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val chatBackground: Color,
    val text: Color,
    val muted: Color,
    val border: Color,
    val accent: Color,
    val accentStrong: Color,
    val onAccent: Color,
    val bubbleMine: Color,
    val bubbleTheirs: Color,
    val tickRead: Color,
    val danger: Color,
)

object Palette {
    val light = ChatterColors(
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFFFFFFF),
        surfaceAlt = Color(0xFFF2F4F3),
        chatBackground = Color(0xFFEFEAE2),
        text = Color(0xFF111B21),
        muted = Color(0xFF667781),
        border = Color(0xFFE4E8E6),
        accent = Color(0xFF128C5E),
        accentStrong = Color(0xFF0B6E4A),
        onAccent = Color(0xFFFFFFFF),
        bubbleMine = Color(0xFFD7F5DE),
        bubbleTheirs = Color(0xFFFFFFFF),
        tickRead = Color(0xFF2E9BF0),
        danger = Color(0xFFC9302C),
    )
    val dark = ChatterColors(
        background = Color(0xFF0B141A),
        surface = Color(0xFF111B21),
        surfaceAlt = Color(0xFF1F2C34),
        chatBackground = Color(0xFF0B141A),
        text = Color(0xFFE9EDEF),
        muted = Color(0xFF8696A0),
        border = Color(0xFF22303A),
        accent = Color(0xFF21C07A),
        accentStrong = Color(0xFF128C5E),
        onAccent = Color(0xFF0B141A),
        bubbleMine = Color(0xFF0F5E44),
        bubbleTheirs = Color(0xFF1F2C34),
        tickRead = Color(0xFF53BDEB),
        danger = Color(0xFFFF6B66),
    )
}

/** Same scale as `Spacing` in theme.ts (dp). */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
}

val LocalChatterColors = staticCompositionLocalOf { Palette.light }

/** Equivalent of `useColors()`. */
object ChatterTheme {
    val colors: ChatterColors
        @Composable @ReadOnlyComposable get() = LocalChatterColors.current
}

@Composable
fun ChatterTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val c = if (darkTheme) Palette.dark else Palette.light
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = c.accent, onPrimary = c.onAccent, background = c.background, onBackground = c.text,
            surface = c.surface, onSurface = c.text, surfaceVariant = c.surfaceAlt, onSurfaceVariant = c.muted,
            outline = c.border, error = c.danger,
        )
    } else {
        lightColorScheme(
            primary = c.accent, onPrimary = c.onAccent, background = c.background, onBackground = c.text,
            surface = c.surface, onSurface = c.text, surfaceVariant = c.surfaceAlt, onSurfaceVariant = c.muted,
            outline = c.border, error = c.danger,
        )
    }
    CompositionLocalProvider(LocalChatterColors provides c) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
