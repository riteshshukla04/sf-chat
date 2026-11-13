package dev.softwarefactory.chatter.services

import android.content.Context
import dev.softwarefactory.chatter.BuildConfig
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.android.core.SentryAndroid

/**
 * Port of src/services/sentry.ts. Sentry only starts when a DSN was passed at build time
 * (BuildConfig.SENTRY_DSN from EXPO_PUBLIC_SENTRY_DSN / SENTRY_DSN); otherwise every call is a no-op.
 *
 * release = dev.softwarefactory.chatter@<app version>, dist = "embedded",
 * tags = app, platform, update_id, channel, runtime_version.
 */
object ErrorReporting {
    private var initialised = false

    val enabled: Boolean get() = BuildConfig.SENTRY_DSN.isNotEmpty()

    fun init(context: Context, dsn: String = BuildConfig.SENTRY_DSN, info: BuildInfo = BuildInfo.get()) {
        if (dsn.isEmpty() || initialised) return
        initialised = true
        SentryAndroid.init(context) { o ->
            o.dsn = dsn
            o.release = info.release
            o.dist = info.dist
            o.environment = if (BuildConfig.DEBUG) "development" else info.channel
            o.isSendDefaultPii = false
            o.tracesSampleRate = 0.2
            o.isDebug = false
        }
        for ((k, v) in tags(info)) Sentry.setTag(k, v)
    }

    internal fun tags(info: BuildInfo): Map<String, String> = mapOf(
        "app" to "chatter",
        "platform" to info.platform,
        "update_id" to info.updateLabel,
        "channel" to info.channel,
        "runtime_version" to info.runtimeVersion,
    )

    fun captureException(error: Throwable) {
        if (initialised) Sentry.captureException(error)
    }

    fun addBreadcrumb(message: String) {
        if (initialised) Sentry.addBreadcrumb(Breadcrumb().apply { category = "chatter"; this.message = message })
    }
}
