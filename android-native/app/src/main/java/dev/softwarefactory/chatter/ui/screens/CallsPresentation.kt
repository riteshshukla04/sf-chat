package dev.softwarefactory.chatter.ui.screens

import dev.softwarefactory.chatter.domain.Call
import dev.softwarefactory.chatter.domain.CallDirection
import dev.softwarefactory.chatter.domain.Contact
import dev.softwarefactory.chatter.domain.formatTime
import java.util.TimeZone

/** What one row of the Calls tab shows (port of the renderItem logic in src/app/(tabs)/calls.tsx). */
data class CallRowModel(
    val id: String,
    val name: String,
    val missed: Boolean,
    val direction: CallDirection,
    val video: Boolean,
    val time: String,
)

fun callRows(calls: List<Call>, contacts: Map<String, Contact>, now: Long, zone: TimeZone = TimeZone.getDefault()): List<CallRowModel> =
    calls.map {
        CallRowModel(
            id = it.id,
            name = contacts[it.contactId]?.name ?: "Unknown",
            missed = it.direction == CallDirection.Missed,
            direction = it.direction,
            video = it.video,
            time = formatTime(it.at, now, zone),
        )
    }
