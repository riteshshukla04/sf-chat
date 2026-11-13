package dev.softwarefactory.chatter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import dev.softwarefactory.chatter.state.Clock
import kotlinx.coroutines.launch
import dev.softwarefactory.chatter.ui.AppRoot
import dev.softwarefactory.chatter.ui.NavActions

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as ChatterApp
        val store = app.store
        setContent {
            val state by store.state.collectAsState()
            var attempt by remember { mutableStateOf(0) }
            var error by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(attempt) {
                if (!store.state.value.hydrated) {
                    try {
                        error = null
                        store.hydrate()
                    } catch (e: Exception) {
                        app.crash.captureException(e)
                        error = e.message ?: e.toString()
                    }
                }
            }
            val scope = rememberCoroutineScope()
            val actions = remember {
                NavActions(
                    startChat = { store.startChat(it) },
                    markRead = { store.markRead(it) },
                    now = { Clock.System.now() },
                    updateProfile = { n, a -> store.updateProfile(n, a) },
                    resetChats = { scope.launch { store.resetChats() } },
                    crashReportingEnabled = app.crash.enabled,
                )
            }
            // testTag values double as resource-ids (UiAutomator / Maestro), like RN testIDs.
            Box(Modifier.fillMaxSize().semantics { testTagsAsResourceId = true }) {
                AppRoot(state, error, onRetry = { attempt++ }, actions = actions)
            }
        }
    }
}
