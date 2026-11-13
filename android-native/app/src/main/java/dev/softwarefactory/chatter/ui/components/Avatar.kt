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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.softwarefactory.chatter.domain.initials

/** Same palette as COLOURS in src/components/avatar.tsx. */
internal val AvatarColours = longArrayOf(0xFF25A36F, 0xFF3E8EDE, 0xFFD9822B, 0xFF9B59B6, 0xFFE0566E, 0xFF16A2B8, 0xFF7A8B2E, 0xFFC0612B)

/** A stable colour per name (hash = hash*31 + char, as unsigned 32 bit, like the TS `>>> 0`). */
fun avatarColour(name: String): Color {
    var hash = 0L
    var i = 0
    while (i < name.length) {
        val cp = name.codePointAt(i)
        // JS iterates code points but charCodeAt returns the first UTF-16 unit; mirror that.
        hash = (hash * 31 + name[i].code) and 0xFFFFFFFFL
        i += Character.charCount(cp)
    }
    return Color(AvatarColours[(hash % AvatarColours.size).toInt()])
}

/** Circle with the name's initials on a stable colour. Port of Avatar in src/components/avatar.tsx. */
@Composable
fun Avatar(name: String, modifier: Modifier = Modifier, size: Dp = 48.dp, group: Boolean = false) {
    Box(
        modifier.size(size).clip(CircleShape).background(avatarColour(name)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            initials(name),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * (if (group) 0.34f else 0.38f)).sp,
        )
    }
}
