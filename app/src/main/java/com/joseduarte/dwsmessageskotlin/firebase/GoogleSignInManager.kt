package com.joseduarte.dwsmessageskotlin.firebase

import android.app.Activity
import android.content.DialogInterface
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.joseduarte.dwsmessageskotlin.GlobalInformation
import com.joseduarte.dwsmessageskotlin.LoginActivity
import com.joseduarte.dwsmessageskotlin.R
import com.joseduarte.dwsmessageskotlin.models.User

class GoogleSignInManager(var activity: Activity) {

    var googleSignInClient: GoogleSignInClient

    fun addListener(btn: Button) {

        btn.setOnClickListener {
            val builder = AlertDialog.Builder(activity)

            builder.setMessage(R.string.dialog_replace_account)
                .setPositiveButton(R.string.dialog_confirm) { dialog, _ ->
                    val signInIntent = googleSignInClient.signInIntent

                    activity.startActivityForResult(
                        signInIntent,
                        RC_SIGN_IN
                    )

                    dialog.dismiss()
                }
                .setNegativeButton(R.string.dialog_cancel) { dialog, _ -> dialog.dismiss() }
            builder.create().show()
        }
    }

    fun firebaseAuthWithGoogle(task: Task<GoogleSignInAccount>, auth: FirebaseAuth) {
        var account: GoogleSignInAccount? = null

        try {
            account = task.getResult(ApiException::class.java)!!
        }
        catch (e: ApiException) {}

        if (account != null) {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val finalAccount: GoogleSignInAccount = account

            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val configuredUser = User(finalAccount.email.toString())
                        configuredUser.username = finalAccount.displayName.toString()
                        configuredUser.phoneNumber = ""
                        configuredUser.photoURL = finalAccount.photoUrl.toString()

                        GlobalInformation.SIGN_IN_USER = configuredUser

                        FirebaseDBManager.createUserData(activity, configuredUser)
                        (activity as LoginActivity).continueApp(configuredUser)
                    }
                }
        }
    }

    companion object {
        const val RC_SIGN_IN = 9001
    }

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(activity, gso)
    }
}