package dev.softwarefactory.chatter

import android.app.Application
import dev.softwarefactory.chatter.data.PrefsStorage
import dev.softwarefactory.chatter.state.AppStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Application-level singleton holder: screens use `AppContainer.store` (state: StateFlow<AppState>). */
object AppContainer {
    lateinit var store: AppStore
        private set

    internal fun init(app: Application) {
        if (::store.isInitialized) return
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        store = AppStore(PrefsStorage(app), scope)
        scope.launch { store.hydrate() }
    }
}

class ChatterApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
    }
}
