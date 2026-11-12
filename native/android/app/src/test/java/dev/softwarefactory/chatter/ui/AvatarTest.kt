package dev.softwarefactory.chatter.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class AvatarTest {
    // Expected values computed with the RN implementation (src/components/avatar.tsx) in node.
    @Test fun colourMatchesReactNativeHash() {
        val expected = mapOf(
            "Alice" to 0xFF25A36F,
            "Bob Smith" to 0xFF25A36F,
            "" to 0xFF25A36F,
            "Zoë Ünal" to 0xFFC0612B,
            "Team 🚀 Chat" to 0xFF7A8B2E,
            "A very long name that overflows the 32 bit hash for sure" to 0xFF16A2B8,
        )
        for ((name, colour) in expected) {
            assertEquals(name, colour, AvatarColours[avatarColourIndex(name)])
        }
    }

    @Test fun colourIsStablePerName() {
        assertEquals(avatarColourFor("Maya"), avatarColourFor("Maya"))
    }

    @Test fun initialsTakeFirstTwoWords() {
        assertEquals("AB", avatarInitials("alice bob carol"))
        assertEquals("A", avatarInitials("  alice "))
        assertEquals("?", avatarInitials("   "))
        assertEquals("?", avatarInitials(""))
    }

    @Test fun groupTextIsSmaller() {
        assertEquals(0.34f, avatarTextFraction(true), 0f)
        assertEquals(0.38f, avatarTextFraction(false), 0f)
    }
}
