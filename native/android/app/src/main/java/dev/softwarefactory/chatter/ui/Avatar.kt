package dev.softwarefactory.chatter.ui

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Port of src/components/avatar.tsx. */

// A stable colour per name, so the same person always gets the same avatar.
internal val AvatarColours = longArrayOf(
    0xFF25A36F, 0xFF3E8EDE, 0xFFD9822B, 0xFF9B59B6, 0xFFE0566E, 0xFF16A2B8, 0xFF7A8B2E, 0xFFC0612B,
)

/**
 * Same hash as the RN app: `hash = (hash * 31 + ch.charCodeAt(0)) >>> 0` over code points,
 * where charCodeAt(0) is the first UTF-16 unit of the code point.
 */
internal fun avatarColourIndex(name: String): Int {
    var hash = 0L
    var i = 0
    while (i < name.length) {
        val cp = name.codePointAt(i)
        hash = (hash * 31 + name[i].code) and 0xFFFFFFFFL
        i += Character.charCount(cp)
    }
    return (hash % AvatarColours.size).toInt()
}

fun avatarColourFor(name: String): Color = Color(AvatarColours[avatarColourIndex(name)])

/**
 * Local port of `initials` from src/lib/chats.ts (two first words, upper-cased, "?" when empty).
 * Named differently from the core task's `initials` in Chats.kt so the two never clash; the
 * Avatar can switch to the shared one later.
 */
internal fun avatarInitials(name: String): String {
    val letters = name.split(Regex("\\s+")).filter { it.isNotEmpty() }.take(2).map { it[0].uppercaseChar() }
    return letters.joinToString("").ifEmpty { "?" }
}

/** Text size as a fraction of the avatar size: 0.34 for groups, 0.38 for people. */
internal fun avatarTextFraction(group: Boolean): Float = if (group) 0.34f else 0.38f

@Composable
fun Avatar(name: String, modifier: Modifier = Modifier, size: Dp = 48.dp, group: Boolean = false) {
    Box(
        modifier = modifier.size(size).clip(CircleShape).background(avatarColourFor(name)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = avatarInitials(name),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * avatarTextFraction(group)).sp,
        )
    }
}
