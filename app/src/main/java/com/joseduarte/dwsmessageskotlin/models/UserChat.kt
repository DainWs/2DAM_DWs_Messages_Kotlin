package com.joseduarte.dwsmessageskotlin.models

import java.util.*

class UserChat {
    var id = ""
    var participantes: List<String> = ArrayList()
    private var messages: MutableList<Messages> = ArrayList<Messages>()

    constructor() {}
    constructor(participantes: MutableList<String>) {
        this.participantes = participantes
    }

    fun getMessages(): List<Messages> {
        return messages
    }

    fun setMessages(messages: MutableList<Messages>) {
        this.messages = messages
    }

    fun containsAll(participants: List<String>): Boolean {
        return (participants.containsAll(participantes) ||
                participantes.containsAll(participants)) &&
                participants.size == participantes.size
    }

    fun addMessage(message: Messages) {
        messages.add(message)
    }
}