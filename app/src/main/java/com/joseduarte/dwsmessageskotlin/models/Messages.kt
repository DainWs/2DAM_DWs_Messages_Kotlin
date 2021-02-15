package com.joseduarte.dwsmessageskotlin.models

import java.text.SimpleDateFormat
import java.util.*

class Messages {
    lateinit var senderMail: String
    lateinit var senderId: String
    var time: Long = 0
    lateinit var data: String

    constructor() {}
    constructor(sender: User, data: String) {
        senderMail = sender.mail
        senderId = sender.getId()
        time = Date().time

        this.data = data
    }

    fun getMessageID(): String {
        return SimpleDateFormat("ssmmHHddMMyyyy").format(time)
    }
}