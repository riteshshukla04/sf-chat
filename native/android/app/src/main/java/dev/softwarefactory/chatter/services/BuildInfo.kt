package dev.softwarefactory.chatter.services

import dev.softwarefactory.chatter.BuildConfig

/** Port of src/services/build-info.ts for a binary with no OTA updates. */
const val APP_ID = "dev.softwarefactory.chatter"

/** Channel the RN binary asks for updates on; kept so Settings / Sentry read the same. */
const val UPDATE_CHANNEL = "chat"

data class BuildInfo(
    val appVersion: String,
    val runtimeVersion: String,
    val channel: String,
    val updateId: String?,
    val updateLabel: String,
    val updatesEnabled: Boolean,
    val platform: String,
    /** Sentry release, e.g. dev.softwarefactory.chatter@1.0.0 */
    val release: String,
    /** Sentry dist: the update id or "embedded". */
    val dist: String,
) {
    companion object {
        /** Pure so it can be unit tested; the app uses [get]. */
        fun create(appVersion: String): BuildInfo = BuildInfo(
            appVersion = appVersion,
            runtimeVersion = appVersion, // runtimeVersion policy "appVersion"
            channel = UPDATE_CHANNEL,
            updateId = null,
            updateLabel = "embedded",
            updatesEnabled = false,
            platform = "android",
            release = "$APP_ID@$appVersion",
            dist = "embedded",
        )

        val current: BuildInfo by lazy { create(BuildConfig.VERSION_NAME) }

        fun get(): BuildInfo = current
    }
}
