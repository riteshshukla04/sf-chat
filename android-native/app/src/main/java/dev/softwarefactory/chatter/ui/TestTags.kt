package dev.softwarefactory.chatter.ui

/** Same ids as the React Native testIDs (see AGENTS.md and .maestro/). Exposed as resource-ids too. */
object TestTags {
    fun chatRow(id: String) = "chat-row-$id"
    const val ChatSubtitle = "chat-subtitle"
    const val MessageInput = "message-input"
    const val Send = "send"
    const val NewChat = "new-chat"
    const val TabChats = "tab-chats"
    const val TabCalls = "tab-calls"
    const val TabSettings = "tab-settings"
    const val ErrorScreen = "error-screen"
    const val ErrorMessage = "error-message"
    const val ErrorRetry = "error-retry"
}
