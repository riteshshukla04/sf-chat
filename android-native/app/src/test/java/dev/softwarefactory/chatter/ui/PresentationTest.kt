package dev.softwarefactory.chatter.ui

import dev.softwarefactory.chatter.domain.Call
import dev.softwarefactory.chatter.domain.CallDirection
import dev.softwarefactory.chatter.domain.Contact
import dev.softwarefactory.chatter.services.buildInfo
import dev.softwarefactory.chatter.ui.components.avatarColourIndex
import dev.softwarefactory.chatter.ui.screens.callRows
import dev.softwarefactory.chatter.ui.screens.crashReportingValue
import dev.softwarefactory.chatter.ui.screens.sanitizeProfile
import dev.softwarefactory.chatter.ui.screens.settingsRows
import dev.softwarefactory.chatter.ui.screens.versionValue
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PresentationTest {
    private val utc = TimeZone.getTimeZone("UTC")
    private val now = 1_800_000_000_000L
    private val contacts = mapOf("asha" to Contact("asha", "Asha Rao", "", null))

    @Test fun callRowsResolveNamesAndMissed() {
        val calls = listOf(
            Call("c1", "asha", now - 60_000, CallDirection.Missed, true),
            Call("c2", "ghost", now - 60_000, CallDirection.Outgoing, false),
        )
        val rows = callRows(calls, contacts, now, utc)
        assertEquals("Asha Rao", rows[0].name)
        assertTrue(rows[0].missed)
        assertTrue(rows[0].video)
        assertEquals("Unknown", rows[1].name)
        assertFalse(rows[1].missed)
    }

    @Test fun emptyCallsGiveNoRows() = assertTrue(callRows(emptyList(), contacts, now, utc).isEmpty())

    @Test fun versionLabelBuiltInOrShortUpdateId() {
        val info = buildInfo("1.2.3")
        assertEquals("1.2.3 (built in)", versionValue(info))
        assertEquals("1.2.3 (abcdef12)", versionValue(info.copy(updateLabel = "abcdef1234567")))
    }

    @Test fun crashReportingLabel() {
        assertEquals("off (no DSN)", crashReportingValue(false))
        assertEquals("Sentry on", crashReportingValue(true))
    }

    @Test fun settingsRowsKeepTestIdsAndOrder() {
        val rows = settingsRows(buildInfo("1.0.0"), false)
        assertEquals(listOf("Account", "Notifications", "Storage", "Version", "Update channel", "Crash reporting"), rows.map { it.label })
        assertEquals("version-row", rows[3].tag)
        assertEquals("crash-reporting-row", rows[5].tag)
        assertEquals("android", buildInfo("1.0.0").platform)
        assertEquals("embedded", buildInfo("1.0.0").updateLabel)
        assertNull(rows[0].tag)
    }

    @Test fun profileEditsAreSanitised() {
        assertEquals("Sam" to "Hi", sanitizeProfile("  Sam ", " Hi ", "You" to "Available"))
        assertEquals("You" to "", sanitizeProfile("   ", "", "You" to "Available"))
    }

    @Test fun avatarColourMatchesRnHash() {
        // RN: hash=((hash*31)+code)>>>0 ; "A" = 65 -> 65 % 8 = 1
        assertEquals(1, avatarColourIndex("A"))
        assertEquals(avatarColourIndex("Asha Rao"), avatarColourIndex("Asha Rao"))
    }
}
