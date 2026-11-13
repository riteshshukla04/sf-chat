package dev.softwarefactory.chatter.state

import android.os.Handler
import android.os.Looper

/** Wall clock, injectable. */
fun interface Clock {
    fun now(): Long

    companion object {
        val System = Clock { java.lang.System.currentTimeMillis() }
    }
}

/** Runs [task] once after [delayMs]. Injectable so tests can use [FakeScheduler] instead of real timers. */
fun interface Scheduler {
    fun schedule(delayMs: Long, task: () -> Unit)
}

/** Posts on the main looper, so store updates stay on the UI thread. */
class MainHandlerScheduler : Scheduler {
    private val handler = Handler(Looper.getMainLooper())
    override fun schedule(delayMs: Long, task: () -> Unit) {
        handler.postDelayed(task, delayMs)
    }
}

/** Manual time for tests: [advanceBy] runs every task that becomes due, in order. */
class FakeScheduler(var nowMs: Long = 0) : Scheduler, Clock {
    private class Entry(val at: Long, val seq: Int, val task: () -> Unit)

    private val queue = mutableListOf<Entry>()
    private var seq = 0

    override fun now(): Long = nowMs

    val pending: Int get() = queue.size

    override fun schedule(delayMs: Long, task: () -> Unit) {
        queue.add(Entry(nowMs + delayMs, seq++, task))
    }

    fun advanceBy(ms: Long) {
        val target = nowMs + ms
        while (true) {
            val next = queue.filter { it.at <= target }.minWithOrNull(compareBy({ it.at }, { it.seq })) ?: break
            queue.remove(next)
            nowMs = next.at
            next.task()
        }
        nowMs = target
    }
}
