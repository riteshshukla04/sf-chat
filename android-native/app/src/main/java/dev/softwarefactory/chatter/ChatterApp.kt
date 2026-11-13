package dev.softwarefactory.chatter

import android.app.Application
import dev.softwarefactory.chatter.data.DataStoreStorage
import dev.softwarefactory.chatter.services.CrashReporter
import dev.softwarefactory.chatter.services.buildInfo
import dev.softwarefactory.chatter.services.crashReporter
import dev.softwarefactory.chatter.state.AppStore
import dev.softwarefactory.chatter.state.Clock
import dev.softwarefactory.chatter.state.MainHandlerScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob

class ChatterApp : Application() {
    lateinit var store: AppStore
        private set
    lateinit var crash: CrashReporter
        private set

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        // Crash reporting first, so errors during startup are captured too (no-op without a DSN).
        crash = crashReporter(BuildConfig.SENTRY_DSN).also { it.init(buildInfo()) }
        store = AppStore(
            storage = DataStoreStorage(this),
            clock = Clock.System,
            scheduler = MainHandlerScheduler(),
            // One thread, so saves are written in the order they were made.
            saveScope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1)),
        )
    }
}
