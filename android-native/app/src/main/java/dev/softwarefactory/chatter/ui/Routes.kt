package dev.softwarefactory.chatter.ui

object Routes {
    const val Tabs = "tabs"
    const val Chat = "chat/{id}"
    const val NewChat = "new-chat"
    fun chat(id: String) = "chat/$id"
}
