package dev.softwarefactory.chatter.services

import dev.softwarefactory.chatter.BuildConfig

/**
 * What is actually running. Port of src/services/build-info.ts, minus expo-updates: the native app has
 * no OTA channel, so the update id is always "embedded" and updates are never enabled.
 */
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
    /** Sentry dist. */
    val dist: String,
)

const val APP_ID = "dev.softwarefactory.chatter"

fun buildInfo(appVersion: String = BuildConfig.VERSION_NAME): BuildInfo = BuildInfo(
    appVersion = appVersion,
    runtimeVersion = appVersion,
    channel = "none",
    updateId = null,
    updateLabel = "embedded",
    updatesEnabled = false,
    platform = "android",
    release = "$APP_ID@$appVersion",
    dist = "embedded",
)
