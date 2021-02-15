package com.joseduarte.dwsmessageskotlin.ui.chat

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.joseduarte.dwsmessageskotlin.GlobalInformation
import com.joseduarte.dwsmessageskotlin.R
import com.joseduarte.dwsmessageskotlin.adapters.MessagesViewAdapter
import com.joseduarte.dwsmessageskotlin.firebase.FirebaseDBManager
import com.joseduarte.dwsmessageskotlin.models.Group
import com.joseduarte.dwsmessageskotlin.models.Messages
import com.joseduarte.dwsmessageskotlin.models.User
import com.joseduarte.dwsmessageskotlin.models.UserChat
import java.util.*

class Chat : Fragment {
    private lateinit var root: View
    private var recyclerView: RecyclerView? = null

    private var chat: UserChat = UserChat()
    private var remoteUser: User = User()
    private var connectedUser: User = User()
    private var checkFor = true

    private var isGroup: Boolean = false
    private lateinit var group : Group

    constructor() {
        GlobalInformation.CHAT = this
    }

    constructor(checkFor: Boolean) {
        this.checkFor = checkFor
    }

    fun getCurrentlyChat(): UserChat {
        return chat
    }

    fun getCurrentlyGroup(): Group {
        return group
    }

    fun isGroup(): Boolean {
        return isGroup
    }

    fun getRemoteUser(): User {
        return remoteUser
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        remoteUser = (requireArguments().getSerializable("user") as User)!!
        if(remoteUser is Group) {
            group = (remoteUser as Group)
            isGroup = true
        }

        connectedUser = GlobalInformation.SIGN_IN_USER
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        root = inflater.inflate(R.layout.fragment_chat, container, false)

        val participantes: MutableList<String> = ArrayList()

        if(isGroup) {
            participantes.addAll(group.participantes)
        }
        else {
            participantes.add(remoteUser.mail)
            participantes.add(connectedUser.mail)
        }

        chat.participantes = participantes
        recyclerView = root.findViewById(R.id.chat)

        val context = context
        recyclerView!!.layoutManager = LinearLayoutManager(context)

        if(isGroup) {
            recyclerView!!.adapter = MessagesViewAdapter(group.getMessages());
        }
        else {
            recyclerView!!.adapter = MessagesViewAdapter(GlobalInformation.MESSAGES)
        }

        val messageField = root.findViewById<EditText>(R.id.chat_message_text)

        root.findViewById<View>(R.id.chat_message_send_button).setOnClickListener {
            val data = messageField.text.toString()

            if (data.isNotEmpty()) {
                val message = Messages(connectedUser, data)

                if(isGroup) {
                    group.addMessage(message);
                    FirebaseDBManager.saveGroup(group)
                }
                else {
                    chat.addMessage(message)
                    FirebaseDBManager.saveChat(chat)
                }

                messageField.clearFocus()
                messageField.setText("")
                val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                imm.hideSoftInputFromWindow(messageField.windowToken, 0)
            }
        }
        if(isGroup) updateAdapter()
        else update()
        return root
    }

    private fun updateAdapter() {
        if (recyclerView != null) {
            if(!isGroup) (recyclerView!!.adapter as MessagesViewAdapter).update(chat.getMessages())
            else (recyclerView!!.adapter as MessagesViewAdapter).update(group.getMessages())

            recyclerView!!.adapter!!.notifyDataSetChanged()
            recyclerView!!.smoothScrollToPosition((recyclerView!!.adapter as MessagesViewAdapter).itemCount)
        }
    }

    fun update() {
        if(isGroup) {
            if(group == null) {
                for (user in GlobalInformation.USUARIOS) {
                    if (user is Group) {
                        var nGroup: Group = user
                        val matchAllParticipantes: Boolean = nGroup.containsAll(this.chat.participantes)
                        if (matchAllParticipantes) {
                            this.group = nGroup
                            break
                        }
                    }
                }
            }
        }
        else {
            for (chat in GlobalInformation.CHATS) {
                val matchAllParticipantes: Boolean = this.chat.containsAll(chat.participantes)
                if (matchAllParticipantes) {
                    this.chat = chat
                    break
                }
            }
        }
        updateAdapter()
    }

    fun update(chat: UserChat) {
        val matchAllParticipantes: Boolean = this.chat.containsAll(chat.participantes)
        if (matchAllParticipantes) {
            this.chat = chat
        }
        updateAdapter()
    }

    fun update(chat: Group) {
        val matchAllParticipantes: Boolean = group.username == chat.username
        if (matchAllParticipantes) {
            this.group = chat
        }
        updateAdapter()
    }

    companion object {
        var instance = Chat()
            private set

        fun newInstance(): Chat {
            instance = Chat()
            return instance
        }
    }
}