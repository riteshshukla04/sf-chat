package dev.softwarefactory.chatter.ui.screens

import dev.softwarefactory.chatter.core.Call
import dev.softwarefactory.chatter.core.CallDirection
import dev.softwarefactory.chatter.core.buildSeed
import dev.softwarefactory.chatter.services.BuildInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

class ScreenRowsTest {
    private val utc = TimeZone.getTimeZone("UTC")
    private val now = 1_700_000_000_000L
    private val seed = buildSeed(now)

    @Test fun unknownContactAndArrows() {
        val rows = callRows(
            listOf(
                Call("a", "nobody", now, CallDirection.Missed, false),
                Call("b", "ben", now, CallDirection.Outgoing, true),
                Call("c", "ben", now, CallDirection.Incoming, false),
            ),
            seed.contacts, now, utc,
        )
        assertEquals("Unknown", rows[0].name)
        assertTrue(rows[0].missed); assertEquals(CallArrow.Missed, rows[0].arrow)
        assertFalse(rows[1].missed); assertEquals(CallArrow.Made, rows[1].arrow); assertTrue(rows[1].video)
        assertEquals(CallArrow.Received, rows[2].arrow); assertFalse(rows[2].video)
        assertEquals(seed.contacts["ben"]!!.name, rows[1].name)
    }

    @Test fun seedCallsKeepOrder() {
        assertEquals(seed.calls.map { it.id }, callRows(seed.calls, seed.contacts, now, utc).map { it.id })
    }

    @Test fun settingsRowsMatchRn() {
        val rows = settingsRows(BuildInfo.create("1.0.0"), sentryEnabled = false)
        assertEquals(
            listOf(
                "Account" to "Privacy, security",
                "Notifications" to "Message and call tones",
                "Storage" to "Messages are kept on this phone",
                "Version" to "1.0.0 (built in)",
                "Update channel" to "chat",
                "Crash reporting" to "off (no DSN)",
            ),
            rows.map { it.label to it.value },
        )
        assertEquals("version-row", rows[3].tag)
        assertEquals("crash-reporting-row", rows[5].tag)
        assertEquals("Sentry on", crashReportingLabel(true))
    }

    @Test fun versionLabelTruncatesUpdateIds() {
        val info = BuildInfo.create("2.0.0").copy(updateLabel = "0123456789abcdef")
        assertEquals("2.0.0 (01234567)", versionLabel(info))
    }
}
