package com.joseduarte.dwsmessageskotlin.models

import java.util.ArrayList

class Group(mail: String, participantes: MutableList<User>) : User(mail) {
    var participantes: MutableList<String> = ArrayList()
    private var messages: MutableList<Messages> = ArrayList<Messages>()

    init {
        username = mail
        for (participante in participantes) {
            this.participantes.add(participante.mail)
        }
    }

    fun getMessages(): List<Messages> {
        return messages
    }

    fun setMessages(messages: MutableList<Messages>) {
        this.messages = messages
    }

    fun containsAll(participants: List<String>): Boolean {
        return (participantes.containsAll(participants))
    }

    fun addMessage(message: Messages) {
        messages.add(message)
    }

}