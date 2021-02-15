package com.joseduarte.dwsmessageskotlin.models

import java.io.Serializable

open class User : Serializable {
    var mail: String = ""
    internal var id: String = ""
    var username: String = ""
    var photoURL: String = ""
    var phoneNumber: String = ""
    private val isPersonalizedIDEnable = false
    private val personalizedID = USERNAME_PERSONALIZED_ID

    constructor() {}
    constructor(mail: String) {
        this.mail = mail
    }

    fun setId(id: String) {
        this.id = id
    }

    //External methods
    fun getId(): String {
        if (isPersonalizedIDEnable) {
            when (personalizedID) {
                USERNAME_PERSONALIZED_ID -> id = username
                MAIL_PERSONALIZED_ID -> id = mail
                PHONE_NUMBER_PERSONALIZED_ID -> id = phoneNumber
            }
        }

        if (id == null || id!!.isEmpty()) {
            if (username != null && !username!!.isEmpty()) {
                id = username
            }
            else if (mail != null && !mail!!.isEmpty()) {
                id = mail
            }
            else if (phoneNumber != null && !phoneNumber!!.isEmpty()) {
                id = phoneNumber
            }
        }

        return id
    }

    fun hasNewChanges(user: User): Boolean {
        var phoneChanges = false

        if (phoneNumber == null) {
            phoneChanges = user.phoneNumber != null
        }
        else if (user.phoneNumber != null) {
            phoneChanges = phoneNumber != user.phoneNumber
        }

        var photoChanges = false

        if (photoURL == null) {
            photoChanges = user.photoURL != null
        }
        else if (user.photoURL != null) {
            photoChanges = photoURL != user.photoURL
        }

        return phoneChanges || photoChanges
    }

    companion object {
        const val USERNAME_PERSONALIZED_ID = 1
        const val MAIL_PERSONALIZED_ID = 2
        const val PHONE_NUMBER_PERSONALIZED_ID = 3
    }
}