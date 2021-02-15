package com.joseduarte.dwsmessageskotlin

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.Navigation
import androidx.navigation.ui.*
import androidx.recyclerview.widget.RecyclerView
import com.joseduarte.dwsmessageskotlin.adapters.UserChipsGroupViewAdapter
import com.joseduarte.dwsmessageskotlin.adapters.UsersViewAdapter
import com.joseduarte.dwsmessageskotlin.firebase.FirebaseDBManager
import com.joseduarte.dwsmessageskotlin.models.Group
import com.joseduarte.dwsmessageskotlin.models.User
import com.joseduarte.dwsmessageskotlin.models.UserChat
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {
    private lateinit var mAppBarConfiguration: AppBarConfiguration
    private lateinit var user: User
    private lateinit var dbManager: FirebaseDBManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val b = intent.extras
        if (b != null) {
            user = b.getSerializable("User") as User
        }

        setContentView(R.layout.main_activity)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        mAppBarConfiguration = AppBarConfiguration.Builder(R.id.nav_home)
                .build()

        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration)
        dbManager = FirebaseDBManager(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        return (NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.action_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, MyPreferences::class.java)
                startActivity(intent)
            }
            R.id.action_new_group -> {
                newGroup()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun newGroup() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_new_group, null)

        val recyclerView: RecyclerView = dialogView.findViewById<RecyclerView>(R.id.users_group_list)
        recyclerView.adapter = UserChipsGroupViewAdapter(GlobalInformation.USUARIOS)

        builder.setTitle(R.string.action_new_group)
            .setView(dialogView)
            .setPositiveButton(
                R.string.dialog_confirm
            ) { dialog, _ ->
                val field = dialogView.findViewById<EditText>(R.id.field)
                if (field.text.toString().isEmpty()) {
                    Toast.makeText(
                        this@MainActivity,
                        R.string.log_in_empty_error,
                        Toast.LENGTH_LONG
                    ).show()
                }
                else {
                    var fieldText = field.text.toString()

                    for (user in GlobalInformation.USUARIOS) {
                        if(user.mail == fieldText) return@setPositiveButton
                    }

                    fieldText = fieldText.replace("[^a-zA-Z0-9]+", "_")

                    val recyclerView: RecyclerView = dialogView.findViewById<RecyclerView>(R.id.users_group_list)
                    var list: MutableList<User> = (recyclerView.adapter as UserChipsGroupViewAdapter).getSelItems()
                    list.add(user)
                    val newGroup = Group(fieldText, list)
                    newGroup.username = fieldText
                    newGroup.mail = fieldText
                    newGroup.phoneNumber = "0"
                    newGroup.photoURL = ""

                    val newGroupChat : UserChat

                    FirebaseDBManager.createGroupData(this@MainActivity, newGroup)

                    dialog.dismiss()
                }

            }
            .setNegativeButton(
                R.string.dialog_cancel
            ) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    override fun onPause() {
        super.onPause()
        FirebaseDBManager.stopListeners()
    }

    override fun onResume() {
        super.onResume()
        FirebaseDBManager.restartListeners()
    }
}