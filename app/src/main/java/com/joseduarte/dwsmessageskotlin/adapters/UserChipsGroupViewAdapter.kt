package com.joseduarte.dwsmessageskotlin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.joseduarte.dwsmessageskotlin.R
import com.joseduarte.dwsmessageskotlin.models.User
import com.squareup.picasso.Picasso

class UserChipsGroupViewAdapter(items: List<User>) :
    RecyclerView.Adapter<UserChipsGroupViewAdapter.ViewHolder>() {

    private val mValues: List<User> = items
    private val mSelValues: MutableList<User> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserChipsGroupViewAdapter.ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.dialog_new_group_user_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserChipsGroupViewAdapter.ViewHolder, position: Int) {
        holder.mItem = mValues[position]
        holder.start()
    }

    fun getSelItems(): MutableList<User> {
        return mSelValues
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(
        mView
    ) {
        val mUserPhotoView: ImageView = mView.findViewById(R.id.contact_item_photo) as ImageView
        val mIdView: TextView = mView.findViewById(R.id.contact_item_username) as TextView
        val mChip: CheckBox = mView.findViewById(R.id.selection_chip) as CheckBox
        lateinit var mItem: User

        fun start() {
            if (mItem.photoURL != null && !mItem.photoURL.isEmpty()) {
                Picasso.get()
                    .load(mItem.photoURL)
                    .placeholder(R.mipmap.app_default_user_photo)
                    .error(R.mipmap.app_default_user_photo)
                    .into(mUserPhotoView)
            }

            mIdView.text = mItem.getId()
            mChip.setOnClickListener { v ->
                if(mSelValues.contains(mItem)) mSelValues.remove(mItem)
                else mSelValues.add(mItem)
            }
        }

        override fun toString(): String {
            return super.toString() + " '" + mIdView.text + "'"
        }

    }

}