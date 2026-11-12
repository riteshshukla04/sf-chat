package dev.softwarefactory.chatter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import dev.softwarefactory.chatter.services.ErrorReporting
import dev.softwarefactory.chatter.ui.ErrorScreen
import dev.softwarefactory.chatter.ui.nav.ChatterApp
import dev.softwarefactory.chatter.ui.theme.ChatterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ErrorReporting.init(applicationContext)
        CrashCatcher.install(applicationContext)
        enableEdgeToEdge()

        // Set when the previous process died from an error thrown while composing (see CrashCatcher).
        val crashMessage = intent?.getStringExtra(CrashCatcher.EXTRA_ERROR)
        setContent {
            var error by remember { mutableStateOf(crashMessage) }
            ChatterTheme {
                val e = error
                if (e != null) {
                    ErrorScreen(message = e, onRetry = { error = null })
                } else {
                    ChatterApp(Modifier.fillMaxSize().semantics { testTagsAsResourceId = true })
                }
            }
        }
    }
}

/**
 * Global catch. Compose cannot wrap composable calls in try/catch, so an error thrown during
 * composition ends the process. We report it, then relaunch the activity with the message so the
 * user lands on the "Something went wrong" screen (the equivalent of the RN root ErrorBoundary)
 * instead of the app just vanishing.
 */
object CrashCatcher {
    const val EXTRA_ERROR = "dev.softwarefactory.chatter.ERROR_MESSAGE"
    private var installed = false

    fun install(context: Context) {
        if (installed) return
        installed = true
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                ErrorReporting.captureException(throwable)
                // Don't loop if the crash comes straight back on the error screen's relaunch.
                val intent = Intent(context, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .putExtra(EXTRA_ERROR, throwable.message ?: throwable.toString())
                context.startActivity(intent)
            }
            previous?.uncaughtException(thread, throwable)
        }
    }
}
