package dev.softwarefactory.chatter.services

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class BuildInfoTest {
    @Test fun matchesReactNativeEmbeddedBuild() {
        val info = BuildInfo.create("1.0.0")
        assertEquals("1.0.0", info.appVersion)
        assertEquals("1.0.0", info.runtimeVersion)
        assertEquals("chat", info.channel)
        assertEquals("embedded", info.updateLabel)
        assertNull(info.updateId)
        assertFalse(info.updatesEnabled)
        assertEquals("dev.softwarefactory.chatter@1.0.0", info.release)
        assertEquals("embedded", info.dist)
    }

    @Test fun appVersionIsOneDotZero() {
        assertEquals("1.0.0", BuildInfo.get().appVersion)
    }

    @Test fun sentryTagsMatchService() {
        assertEquals(
            mapOf("app" to "chatter", "platform" to "android", "update_id" to "embedded", "channel" to "chat", "runtime_version" to "1.0.0"),
            ErrorReporting.tags(BuildInfo.create("1.0.0")),
        )
    }
}
