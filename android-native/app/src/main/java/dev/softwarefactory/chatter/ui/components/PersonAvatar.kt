package dev.softwarefactory.chatter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import dev.softwarefactory.chatter.domain.initials

private val COLOURS = listOf(0x25A36F, 0x3E8EDE, 0xD9822B, 0x9B59B6, 0xE0566E, 0x16A2B8, 0x7A8B2E, 0xC0612B)

/** Same hash as src/components/avatar.tsx (uint32 arithmetic over UTF-16 units). */
fun avatarColourIndex(name: String): Int {
    var hash = 0L
    for (ch in name) hash = (hash * 31 + ch.code) and 0xFFFFFFFFL
    return (hash % COLOURS.size).toInt()
}

@Composable
fun PersonAvatar(name: String, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier.size(size).clip(CircleShape).background(Color(0xFF000000 or COLOURS[avatarColourIndex(name)].toLong())),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials(name), color = Color.White, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.38f).sp)
    }
}
