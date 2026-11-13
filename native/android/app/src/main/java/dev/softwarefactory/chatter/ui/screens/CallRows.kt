package dev.softwarefactory.chatter.ui.screens

import dev.softwarefactory.chatter.core.Call
import dev.softwarefactory.chatter.core.CallDirection
import dev.softwarefactory.chatter.core.Contact
import dev.softwarefactory.chatter.core.formatTime
import java.util.TimeZone

/** Which arrow the call row shows (call-made / call-received / call-missed in the RN app). */
enum class CallArrow { Made, Received, Missed }

/** What one row of the Calls list displays; pure so it can be unit tested. */
data class CallRow(
    val id: String,
    val name: String,
    val missed: Boolean,
    val arrow: CallArrow,
    val time: String,
    val video: Boolean,
)

const val UNKNOWN_CONTACT = "Unknown"

fun callArrow(direction: CallDirection): CallArrow = when (direction) {
    CallDirection.Outgoing -> CallArrow.Made
    CallDirection.Incoming -> CallArrow.Received
    CallDirection.Missed -> CallArrow.Missed
}

fun callRow(call: Call, contacts: Map<String, Contact>, now: Long, zone: TimeZone = TimeZone.getDefault()): CallRow =
    CallRow(
        id = call.id,
        name = contacts[call.contactId]?.name ?: UNKNOWN_CONTACT,
        missed = call.direction == CallDirection.Missed,
        arrow = callArrow(call.direction),
        time = formatTime(call.at, now, zone),
        video = call.video,
    )

fun callRows(calls: List<Call>, contacts: Map<String, Contact>, now: Long, zone: TimeZone = TimeZone.getDefault()): List<CallRow> =
    calls.map { callRow(it, contacts, now, zone) }
