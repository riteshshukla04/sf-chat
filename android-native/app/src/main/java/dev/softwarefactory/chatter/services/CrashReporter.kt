package dev.softwarefactory.chatter.services

/**
 * Crash reporting. Port of src/services/sentry.ts: every call is a no-op unless a DSN was set at build time
 * (`SENTRY_DSN=... ./gradlew assembleDebug`, exposed as BuildConfig.SENTRY_DSN).
 *
 * TODO: add the real Sentry Android SDK (io.sentry:sentry-android), call SentryAndroid.init with
 * release/dist from [BuildInfo], and implement [CrashReporter] with it. Until then even with a DSN
 * nothing is sent; only the interface is wired up.
 */
interface CrashReporter {
    val enabled: Boolean
    fun init(info: BuildInfo)
    fun captureException(error: Throwable)
    fun addBreadcrumb(message: String, data: Map<String, Any?> = emptyMap())
}

object NoopCrashReporter : CrashReporter {
    override val enabled = false
    override fun init(info: BuildInfo) = Unit
    override fun captureException(error: Throwable) = Unit
    override fun addBreadcrumb(message: String, data: Map<String, Any?>) = Unit
}

/** The reporter for this build: a no-op without a DSN. */
fun crashReporter(dsn: String): CrashReporter = if (dsn.isBlank()) NoopCrashReporter else NoopCrashReporter // TODO real SDK
