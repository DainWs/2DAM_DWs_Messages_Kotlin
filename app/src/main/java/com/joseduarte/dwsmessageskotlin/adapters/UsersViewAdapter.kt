package com.joseduarte.dwsmessageskotlin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.joseduarte.dwsmessageskotlin.R
import com.joseduarte.dwsmessageskotlin.models.User
import com.squareup.picasso.Picasso

abstract class UsersViewAdapter(items: List<User>) :
    RecyclerView.Adapter<UsersViewAdapter.ViewHolder>() {

    private val mValues: List<User> = items

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_home_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = mValues[position]
        holder.start()
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(
        mView
    ) {
        val mUserPhotoView: ImageView = mView.findViewById<View>(R.id.contact_item_photo) as ImageView
        val mIdView: TextView = mView.findViewById<View>(R.id.contact_item_username) as TextView
        val mNotReadedView: TextView = mView.findViewById<View>(R.id.contact_item_notreaded) as TextView
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
            mNotReadedView.text = "0"
            mView.setOnClickListener { v -> onClickItem(v, mItem) }
        }

        override fun toString(): String {
            return super.toString() + " '" + mIdView.text + "' has " + mNotReadedView + "new messages"
        }

    }

    abstract fun onClickItem(v: View, user: User)

}