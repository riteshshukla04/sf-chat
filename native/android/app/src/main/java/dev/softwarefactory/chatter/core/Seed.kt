package dev.softwarefactory.chatter.core

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Port of src/data/seed.ts: the conversations a fresh install starts with, timed relative to "now". */

@Serializable
enum class CallDirection {
    @SerialName("incoming") Incoming,
    @SerialName("outgoing") Outgoing,
    @SerialName("missed") Missed,
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Call(
    val id: String,
    val contactId: String,
    val at: Long,
    val direction: CallDirection,
    @EncodeDefault val video: Boolean = false,
)

data class Seed(val contacts: Map<String, Contact>, val chats: List<Chat>, val calls: List<Call>)

private const val MIN = 60_000L
private const val HOUR = 60 * MIN
private const val DAY = 24 * HOUR

fun buildSeed(now: Long): Seed {
    val contacts = listOf(
        Contact("asha", "Asha Rao", "Busy building things", null),
        Contact("ben", "Ben Okafor", "At the gym", now - 25 * MIN),
        Contact("chen", "Chen Wei", "Available", now - 3 * HOUR),
        Contact("dana", "Dana Silva", "Hey there! I am using Chatter.", now - 2 * DAY),
        Contact("eli", "Eli Novak", "Coffee first", null),
        Contact("fatima", "Fatima Khan", "In a meeting", now - 50 * MIN),
        Contact("gabe", "Gabe Moreau", "Travelling", now - 5 * DAY),
        Contact("hana", "Hana Ito", "Urgent calls only", now - 9 * MIN),
    )
    var n = 0
    fun m(from: String, text: String, ago: Long, status: MessageStatus = MessageStatus.Read) =
        Message(id = "seed-${++n}", from = from, text = text, sentAt = now - ago, status = status)

    val chats = listOf(
        Chat(
            id = "family", kind = ChatKind.Group, title = "Family 🏠", members = listOf("chen", "dana"),
            messages = listOf(
                m("dana", "Sunday lunch at ours?", 3 * HOUR),
                m("chen", "Yes! I will bring dessert", 2 * HOUR + 40 * MIN),
                m("me", "Count me in 🙌", 2 * HOUR + 30 * MIN),
                m("dana", "Lunch at 1. Bring the photos from the trip", 50 * MIN),
            ),
            unread = 1, pinned = true, muted = false, createdAt = now - 40 * DAY,
        ),
        Chat(
            id = "asha", kind = ChatKind.Direct, title = "Asha Rao", members = listOf("asha"),
            messages = listOf(
                m("asha", "Did you see the release notes?", 26 * HOUR),
                m("me", "Yes, the new search is great", 25 * HOUR),
                m("asha", "Can you review my PR before 5?", 12 * MIN),
                m("asha", "It's the one about the chat list", 11 * MIN),
            ),
            unread = 2, pinned = false, muted = false, createdAt = now - 90 * DAY,
        ),
        Chat(
            id = "weekend", kind = ChatKind.Group, title = "Weekend trip ⛺", members = listOf("asha", "ben", "eli"),
            // Just created: nobody has written yet.
            messages = emptyList(),
            unread = 0, pinned = false, muted = false, createdAt = now - 8 * MIN,
        ),
        Chat(
            id = "ben", kind = ChatKind.Direct, title = "Ben Okafor", members = listOf("ben"),
            messages = listOf(m("ben", "Leg day tomorrow?", 3 * HOUR), m("me", "Always. 7am?", 2 * HOUR, MessageStatus.Delivered)),
            unread = 0, pinned = false, muted = false, createdAt = now - 60 * DAY,
        ),
        Chat(
            id = "flat", kind = ChatKind.Group, title = "Flat 4B", members = listOf("fatima", "hana", "gabe"),
            messages = listOf(
                m("fatima", "Rent is due Friday", 2 * DAY),
                m("hana", "Paid ✅", 2 * DAY - 3 * HOUR),
                m("gabe", "Paying tonight, sorry!", 1 * DAY + 2 * HOUR),
                m("fatima", "Also who left the oven on 😅", 5 * HOUR),
            ),
            unread = 4, pinned = false, muted = true, createdAt = now - 200 * DAY,
        ),
        Chat(
            id = "chen", kind = ChatKind.Direct, title = "Chen Wei", members = listOf("chen"),
            messages = listOf(m("me", "Thanks for the book!", 4 * DAY), m("chen", "Enjoy it 📚", 4 * DAY - 20 * MIN)),
            unread = 0, pinned = false, muted = false, createdAt = now - 120 * DAY,
        ),
    )

    val calls = listOf(
        Call("c1", "asha", now - 40 * MIN, CallDirection.Missed, false),
        Call("c2", "ben", now - 5 * HOUR, CallDirection.Outgoing, true),
        Call("c3", "dana", now - 1 * DAY - 2 * HOUR, CallDirection.Incoming, false),
        Call("c4", "hana", now - 3 * DAY, CallDirection.Incoming, true),
    )
    return Seed(contacts.associateBy { it.id }, chats, calls)
}
