package com.joseduarte.dwsmessageskotlin

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.joseduarte.dwsmessageskotlin.firebase.FirebaseDBManager
import com.joseduarte.dwsmessageskotlin.models.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.settings_activity.*
import java.util.regex.Pattern

class MyPreferences : AppCompatActivity() {

    private lateinit var user: User
    private var started = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        user = GlobalInformation.SIGN_IN_USER
        val photoFrame = findViewById<FrameLayout>(R.id.photoFrame)
        photoFrame.setOnClickListener { }
        started = true
        update()
        val usernameEditButton = findViewById<ImageButton>(R.id.edit_username_button)
        val phoneEditButton = findViewById<ImageButton>(R.id.edit_phone_button)
        usernameEditButton.setOnClickListener { editUsername() }
        phoneEditButton.setOnClickListener { editPhone() }
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setTitle(R.string.action_bar_settings)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
        GlobalInformation.PREFS = this
    }

    private fun editPhone() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_edit_phone, null)
        builder.setTitle(R.string.dialog_edit_phone)
            .setView(dialogView)
            .setPositiveButton(
                R.string.dialog_confirm
            ) { dialog, _ ->
                val field = dialogView.findViewById<EditText>(R.id.field)
                if (field.text.toString().isEmpty()) {
                    Toast.makeText(
                        this@MyPreferences,
                        R.string.dialog_edit_field_empty,
                        Toast.LENGTH_LONG
                    ).show()
                }
                else {
                    val fieldText = field.text.toString()
                    val p = Pattern.compile("[^0-9\\s\\+]+")
                    val m = p.matcher(fieldText)
                    if (m.matches()) {
                        val invalidChar = getString(R.string.invalid_character) + " + 0-9"
                        Toast.makeText(
                            this@MyPreferences,
                            invalidChar,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else if (fieldText.length >= 9) {
                        user.phoneNumber = fieldText

                        val emptyUser = User()
                        emptyUser.username = "DoNotChange"
                        emptyUser.photoURL = "DoNotChange"

                        FirebaseDBManager.updateUserData(this@MyPreferences, user, emptyUser)
                    }
                    else {
                        Toast.makeText(
                            this@MyPreferences,
                            R.string.invalid_phone_lenght,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton(
                R.string.dialog_cancel
            ) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun editUsername() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_edit_username, null)
        builder.setTitle(R.string.dialog_edit_username)
            .setView(dialogView)
            .setPositiveButton(
                R.string.dialog_confirm
            ) { dialog, _ ->
                val field = dialogView.findViewById<EditText>(R.id.field)
                if (field.text.toString().isEmpty()) {
                    Toast.makeText(this@MyPreferences,R.string.dialog_edit_field_empty,Toast.LENGTH_LONG).show()
                }
                else {
                    val fieldText = field.text.toString()
                    user.username = fieldText
                    val emptyUser = User()
                    emptyUser.username = "DoNotChange"
                    emptyUser.photoURL = "DoNotChange"
                    FirebaseDBManager.updateUserData(this@MyPreferences, user, emptyUser)
                }
                dialog.dismiss()
            }
            .setNegativeButton(
                R.string.dialog_cancel
            ) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun update() {
        if (started) {
            username_field.text = user.username
            mail_field.text = user.mail
            phone_field.text = user.phoneNumber
            Picasso.get()
                .load(user.photoURL)
                .placeholder(R.mipmap.app_default_user_photo)
                .error(R.mipmap.app_default_user_photo)
                .into(contact_item_photo)
        }
    }
}
