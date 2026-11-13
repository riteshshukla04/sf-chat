package dev.softwarefactory.chatter.ui

import androidx.compose.ui.graphics.Color
import dev.softwarefactory.chatter.ui.theme.Palette
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeTest {
    @Test fun keyColoursMirrorThemeTs() {
        assertEquals(Color(0xFFEFEAE2), Palette.light.chatBackground)
        assertEquals(Color(0xFF0F5E44), Palette.dark.bubbleMine)
        assertEquals(Color(0xFF53BDEB), Palette.dark.tickRead)
        assertEquals(Color(0xFFC9302C), Palette.light.danger)
        assertEquals(Color(0xFF128C5E), Palette.light.accent)
        assertEquals(Color(0xFF21C07A), Palette.dark.accent)
    }
}
