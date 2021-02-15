package com.joseduarte.dwsmessageskotlin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.joseduarte.dwsmessageskotlin.R
import com.joseduarte.dwsmessageskotlin.models.Messages
import java.text.SimpleDateFormat
import java.util.*

class MessagesViewAdapter(items: List<Messages>) :
    RecyclerView.Adapter<MessagesViewAdapter.ViewHolder>() {

    private var mValues: List<Messages>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_message, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = mValues[position]
        holder.start()
    }

    fun update(newMessages: List<Messages>) {
        mValues = newMessages
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(
        mView
    ) {
        val mUserId: TextView = mView.findViewById<View>(R.id.message_id) as TextView
        val mData: TextView = mView.findViewById<View>(R.id.message_data) as TextView
        val mSendedTime: TextView = mView.findViewById<View>(R.id.message_time) as TextView
        lateinit var mItem: Messages

        fun start() {
            mUserId.text = mItem.senderId
            mData.text = mItem.data
            val date = SimpleDateFormat("mm:HH dd/MM/yyyy")
                .format(Date(mItem.time))
            mSendedTime.text = date
        }

        override fun toString(): String {
            return super.toString() + " '" + mUserId.text + "' : " + mData
        }

    }

    init {
        mValues = items
    }
}