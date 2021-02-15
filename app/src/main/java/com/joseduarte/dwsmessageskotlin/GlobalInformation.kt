package com.joseduarte.dwsmessageskotlin

import com.joseduarte.dwsmessageskotlin.models.Messages
import com.joseduarte.dwsmessageskotlin.models.User
import com.joseduarte.dwsmessageskotlin.models.UserChat
import com.joseduarte.dwsmessageskotlin.ui.chat.Chat
import com.joseduarte.dwsmessageskotlin.ui.home.Home
import java.util.*

object GlobalInformation {
    var CHATS: MutableList<UserChat> = ArrayList<UserChat>()
    var USUARIOS: MutableList<User> = ArrayList<User>()
    var MESSAGES: MutableList<Messages> = ArrayList<Messages>()

    var SIGN_IN_USER: User = User()
    var HOME: Home = Home()
    var PREFS = MyPreferences()
    var CHAT: Chat = Chat(false)
}