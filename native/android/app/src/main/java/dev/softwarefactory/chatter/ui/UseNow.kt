package dev.softwarefactory.chatter.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/**
 * Port of src/hooks/use-now.ts: the current time in ms, refreshed every [intervalMs]
 * so relative labels ("14:05", "Yesterday") don't go stale while a screen stays open.
 */
@Composable
fun rememberNow(intervalMs: Long = 60_000L): Long {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(intervalMs) {
        while (true) {
            delay(intervalMs)
            now = System.currentTimeMillis()
        }
    }
    return now
}
