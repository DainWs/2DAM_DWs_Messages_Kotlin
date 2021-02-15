package com.joseduarte.dwsmessageskotlin.firebase

import android.app.Activity
import com.google.firebase.database.*
import com.joseduarte.dwsmessageskotlin.GlobalInformation
import com.joseduarte.dwsmessageskotlin.MainActivity
import com.joseduarte.dwsmessageskotlin.models.Group
import com.joseduarte.dwsmessageskotlin.models.Messages
import com.joseduarte.dwsmessageskotlin.models.User
import com.joseduarte.dwsmessageskotlin.models.UserChat
import java.util.*

class FirebaseDBManager(activity: MainActivity) {
    private val activity: Activity

    private fun initListeners(user: User) {
        val db = FirebaseDatabase.getInstance()

        db.reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                //RECOGEMOS LOS USUARIOS
                val users = snapshot.child("Usuarios").children
                GlobalInformation.USUARIOS.clear()

                for (userData in users) {
                    val tmpUser: User = userData.getValue(User::class.java)!!
                    if (tmpUser.mail == user.mail) {
                        checkChanges(tmpUser, user)
                        GlobalInformation.SIGN_IN_USER = tmpUser
                    }
                    else {
                        GlobalInformation.USUARIOS.add(tmpUser)
                    }
                }


                //RECOGEMOS LOS GRUPOS
                val groups = snapshot.child("Grupos").children

                for (groupData in groups) {
                    val participantes: MutableList<String> = ArrayList()
                    for (participante in groupData.child("participantes").children) {
                        participantes.add(
                            participante.getValue(String::class.java).toString()
                        )
                    }

                    var tmpGroup: Group = Group(
                        groupData.child("mail").getValue(String::class.java).toString(),
                        ArrayList()
                    )
                    tmpGroup.participantes = participantes
                    tmpGroup.photoURL =
                        groupData.child("photoURL").getValue(String::class.java).toString()
                    tmpGroup.phoneNumber =
                        groupData.child("phoneNumber").getValue(String::class.java).toString()

                    val messages: MutableList<Messages> = ArrayList<Messages>()

                    for (message in groupData.child("messages").children) {
                        messages.add(message.getValue(Messages::class.java)!!)
                    }

                    tmpGroup.setMessages(messages)

                    GlobalInformation.USUARIOS.add(tmpGroup)
                }

                GlobalInformation.HOME.update()

                //RECOGEMOS LOS CHATS
                val chats = snapshot.child("Chats").children
                for (chat in chats) {
                    val messages: MutableList<Messages> = ArrayList<Messages>()

                    for (message in chat.child("messages").children) {
                        messages.add(message.getValue(Messages::class.java)!!)
                    }

                    //SI EL CHAT TIENE MENSAJES, SE AÑADE, SI NO, NO SE AÑADE
                    if (messages.size > 0) {
                        val participantes: MutableList<String> = ArrayList()
                        for (participante in chat.child("participantes").children) {
                            participantes.add(
                                participante.getValue(String::class.java).toString()
                            )
                        }

                        val chatObj = UserChat(participantes)
                        chatObj.id = chat.child("id").getValue(String::class.java).toString()
                        chatObj.setMessages(messages)

                        GlobalInformation.CHATS.add(chatObj)
                    }
                }

                //En caso de que el chat se haya creado, y este esperando los datos, lo notificamos
                //para que recoga los datos, pero si no se creo no hay problema, cuando se cree el comprobara
                //una vez si hay datos
                GlobalInformation.CHAT.update()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        //CUANDO EL USUARIO CAMBIA SUS DATOS
        USER_EVENT_LISTENER = USER_REF.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: User = snapshot.getValue(User::class.java)!!

                val currentUser: User = GlobalInformation.SIGN_IN_USER
                if (user.mail == currentUser.mail) {
                    GlobalInformation.SIGN_IN_USER = user
                }

                GlobalInformation.PREFS.update()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        MESSAGES_EVENT_LISTENER = CHATS_REF.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val currentlyChat: UserChat = GlobalInformation.CHAT.getCurrentlyChat()

                val participantes: MutableList<String> = ArrayList()
                for (participante in snapshot.child("participantes").children) {
                    participantes.add(participante.value as String)
                }

                if (currentlyChat != null &&
                    currentlyChat.containsAll(participantes)
                ) {

                    //TODO(CHECAR SU FUNCIONAMIENTO)
                    val messages: MutableList<Messages> = ArrayList<Messages>()
                    for (message in snapshot.child("messages").children) {
                        messages.add(message.getValue(Messages::class.java)!!)
                    }
                    currentlyChat.setMessages(messages)

                    GlobalInformation.CHAT.update(currentlyChat)
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                //TODO(NO SOPORTADO)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                //TODO(NO SOPORTADO)
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        GROUP_MESSAGES_EVENT_LISTENER = GROUPS_REF.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                if(GlobalInformation.CHAT.isGroup()) {
                    val currentlyGroup: Group = GlobalInformation.CHAT.getCurrentlyGroup()

                    println("remoto : " + snapshot.child("username").value.toString())
                    println("local : " + currentlyGroup.username)

                    if (currentlyGroup != null &&
                        snapshot.child("username").value.toString() == currentlyGroup.username
                    ) {

                        //TODO(CHECAR SU FUNCIONAMIENTO)
                        val messages: MutableList<Messages> = ArrayList<Messages>()
                        for (message in snapshot.child("messages").children) {
                            messages.add(message.getValue(Messages::class.java)!!)
                        }
                        currentlyGroup.setMessages(messages)

                        GlobalInformation.CHAT.update(currentlyGroup)
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                //TODO(NO SOPORTADO)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                //TODO(NO SOPORTADO)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun checkChanges(user: User, tmpUser: User) {
        if (user.hasNewChanges(tmpUser)) {
            updateUserData(activity, user, tmpUser)
        }
    }

    companion object {
        private const val VALID_CHARACTERS_REGEX = "[^a-zA-Z0-9]+"

        private var USER_FIREBASE_DB_PATH = ""

        private lateinit var USER_EVENT_LISTENER: ValueEventListener
        private lateinit var MESSAGES_EVENT_LISTENER: ChildEventListener
        private lateinit var GROUP_MESSAGES_EVENT_LISTENER: ChildEventListener
        private lateinit var USER_REF: DatabaseReference
        private lateinit var CHATS_REF: DatabaseReference
        private lateinit var GROUPS_REF: DatabaseReference

        fun makeFirebaseURLPath(data: String): String {
            USER_FIREBASE_DB_PATH = "Usuarios/" + data.replace(VALID_CHARACTERS_REGEX.toRegex(), "_")
            return USER_FIREBASE_DB_PATH
        }

        fun createUserData(activity: Activity, user: User) {
            val db = FirebaseDatabase.getInstance()
            val userPath = makeFirebaseURLPath(user.mail)

            USER_REF = db.getReference(userPath)
            USER_REF.child("id").setValue(user.getId())
            USER_REF.child("mail").setValue(user.mail)
            USER_REF.child("username").setValue(user.username)
            USER_REF.child("photoURL").setValue(user.photoURL)
            USER_REF.child("phoneNumber").setValue(user.phoneNumber)
        }

        fun createGroupData(activity: Activity, user: Group) {
            val db = FirebaseDatabase.getInstance()
            val userPath = "Grupos/" + user.mail.replace(VALID_CHARACTERS_REGEX.toRegex(), "_")

            USER_REF = db.getReference(userPath)
            USER_REF.child("id").setValue(user.getId())
            USER_REF.child("mail").setValue(user.mail)
            USER_REF.child("username").setValue(user.username)
            USER_REF.child("photoURL").setValue(user.photoURL)
            USER_REF.child("participantes").setValue(user.participantes)
            USER_REF.child("messages").setValue(user.getMessages())
        }

        fun updateUserData(activity: Activity, user: User, tmpUser: User) {
            val db = FirebaseDatabase.getInstance()
            val userPath = makeFirebaseURLPath(user.mail)

            USER_REF = db.getReference(userPath)
            USER_REF.child("id").setValue(user.getId())
            USER_REF.child("mail").setValue(user.mail)

            if (tmpUser.username.isEmpty()) {
                USER_REF.child("username").setValue(user.username)
            }
            if (tmpUser.photoURL.isEmpty()) {
                USER_REF.child("photoURL").setValue(user.photoURL)
            }
            if (tmpUser.phoneNumber.isEmpty()) {
                USER_REF.child("phoneNumber").setValue(user.phoneNumber)
            }
        }

        fun saveChat(chat: UserChat) {
            if (chat.id.isEmpty()) {
                chat.id = CHATS_REF.push().key.toString()
            }

            CHATS_REF.child(chat.id).setValue(chat)
        }

        fun saveGroup(group: Group) {
            val db = FirebaseDatabase.getInstance()
            val userPath = "Grupos/" + group.mail.replace(VALID_CHARACTERS_REGEX.toRegex(), "_")

            USER_REF = db.getReference(userPath)
            USER_REF.child("id").setValue(group.getId())
            USER_REF.child("mail").setValue(group.mail)
            USER_REF.child("username").setValue(group.username)
            USER_REF.child("photoURL").setValue(group.photoURL)
            USER_REF.child("participantes").setValue(group.participantes)
            USER_REF.child("messages").setValue(group.getMessages())
        }

        fun restartListeners() {

            if (USER_REF != null && USER_EVENT_LISTENER != null) {
                USER_REF.addValueEventListener(USER_EVENT_LISTENER)
            }

            if (CHATS_REF != null && MESSAGES_EVENT_LISTENER != null) {
                CHATS_REF.addChildEventListener(MESSAGES_EVENT_LISTENER)
            }

            if (GROUPS_REF != null && GROUP_MESSAGES_EVENT_LISTENER != null) {
                GROUPS_REF.addChildEventListener(GROUP_MESSAGES_EVENT_LISTENER)
            }
        }

        fun stopListeners() {
            if (USER_REF != null && USER_EVENT_LISTENER != null) {
                USER_REF!!.removeEventListener(USER_EVENT_LISTENER!!)
            }
            if (CHATS_REF != null && MESSAGES_EVENT_LISTENER != null) {
                CHATS_REF!!.removeEventListener(MESSAGES_EVENT_LISTENER!!)
            }
            if (GROUPS_REF != null && GROUP_MESSAGES_EVENT_LISTENER != null) {
                GROUPS_REF!!.removeEventListener(GROUP_MESSAGES_EVENT_LISTENER!!)
            }
        }
    }

    init {
        this.activity = activity
        val user: User = GlobalInformation.SIGN_IN_USER
        val db = FirebaseDatabase.getInstance()
        USER_REF = db.getReference(makeFirebaseURLPath(user.mail))
        CHATS_REF = db.getReference("Chats")
        GROUPS_REF = db.getReference("Grupos")
        initListeners(user)
    }
}
