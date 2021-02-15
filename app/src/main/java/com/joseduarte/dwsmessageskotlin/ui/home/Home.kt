package com.joseduarte.dwsmessageskotlin.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.joseduarte.dwsmessageskotlin.GlobalInformation
import com.joseduarte.dwsmessageskotlin.R
import com.joseduarte.dwsmessageskotlin.adapters.UsersViewAdapter
import com.joseduarte.dwsmessageskotlin.models.User

class Home : Fragment() {
    private lateinit var root: View
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        root = inflater.inflate(R.layout.fragment_home, container, false)

        if (root is RecyclerView) {
            val context = context
            recyclerView = root as RecyclerView
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = object : UsersViewAdapter(GlobalInformation.USUARIOS) {
                override fun onClickItem(v: View, user: User) {
                    openChat(v, user)
                }
            }
        }

        return root
    }

    override fun onResume() {
        super.onResume()
    }

    fun update() {
        recyclerView.adapter!!.notifyDataSetChanged()
    }

    fun openChat(view: View, user: User) {
        val b = Bundle()
        b.putString("title", user.getId())
        b.putSerializable("user", user)
        Navigation.findNavController(root).navigate(R.id.action_from_home_to_chat, b)
    }

    init {
        GlobalInformation.HOME = this
    }
}