package dev.softwarefactory.chatter.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.setValue

/** Where the chat screen puts its header (avatar + title + subtitle) inside the shell's top bar. */
@Stable
class HeaderSlot {
    var content: (@Composable () -> Unit)? by mutableStateOf(null)
}

val LocalHeaderSlot = compositionLocalOf { HeaderSlot() }

/** Call from a screen to fill the top bar title; cleared when the screen leaves. */
@Composable
fun ProvideHeader(content: @Composable () -> Unit) {
    val slot = LocalHeaderSlot.current
    DisposableEffect(slot, content) {
        slot.content = content
        onDispose { if (slot.content === content) slot.content = null }
    }
}
