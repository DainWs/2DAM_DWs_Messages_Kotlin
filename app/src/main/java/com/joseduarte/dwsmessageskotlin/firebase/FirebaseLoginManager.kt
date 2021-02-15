package com.joseduarte.dwsmessageskotlin.firebase

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.TelephonyManager
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.joseduarte.dwsmessageskotlin.LoginActivity
import com.joseduarte.dwsmessageskotlin.R
import com.joseduarte.dwsmessageskotlin.models.User
import kotlinx.android.synthetic.main.login_sign_up_dialog.*
import kotlinx.android.synthetic.main.login_sign_up_dialog.view.*

class FirebaseLoginManager {

    private lateinit var activity: LoginActivity
    private lateinit var auth: FirebaseAuth
    private lateinit var user: User

    constructor(activity: LoginActivity, auth: FirebaseAuth) {
        this.activity = activity
        this.auth = auth
    }

    fun signInUser(mail: String, password: String) {
        auth.signInWithEmailAndPassword(mail, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    signTaskSuccessfully(false, User(mail), task)
                }
                else {
                    logInTaskExceptionHandler(task)
                }
            }
    }

    fun signUpUser() {
        val dialogView: View =
            activity.layoutInflater.inflate(R.layout.login_sign_up_dialog, null)

        val builder = AlertDialog.Builder(activity)

        val dialog: AlertDialog = builder.setView(dialogView)
            .setPositiveButton(R.string.dialog_registrarse, null)
            .setNegativeButton(R.string.dialog_cancel, null)
            .create()
        dialog.setOnShowListener { dialogInterface ->
            (dialogInterface as AlertDialog)
                .getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {

                    val usernameText = dialogView.login_username_field.text.toString()
                    val mailText = dialogView.login_email_address_field.text.toString()
                    val passwordText = dialogView.login_password_field.text.toString()

                    if (!mailText.isEmpty() && !passwordText.isEmpty()) {
                        val isSuccessful = signUpLikeNewUserWith(usernameText, mailText, passwordText)
                        dialog.dismiss()
                    }
                    else if (!mailText.isEmpty()) {
                        Toast.makeText(activity, R.string.sign_up_no_mail_found, Toast.LENGTH_LONG)
                            .show()
                    }
                    else {
                        Toast.makeText(
                            activity,
                            R.string.sign_up_no_password_found,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            dialogInterface
                .getButton(AlertDialog.BUTTON_NEGATIVE)
                .setOnClickListener { dialog.dismiss() }
        }
        dialog.show()
    }

    /*
        Crea un nuevo usuario y te registra en firebase.
     */
    private fun signUpLikeNewUserWith(username: String, mail: String, password: String): Boolean {
        val customUser = User()
        customUser.username = username
        customUser.mail = mail

        val task: Task<*> = auth.createUserWithEmailAndPassword(mail, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    signTaskSuccessfully(true, customUser, task)
                } else {
                    signUpTaskExceptionHandler(task)
                }
            }

        return task.isSuccessful
    }


    /*
        Comprueba las credenciales en el Auth del firebase con las locales,
        si a las locales les falta algun datos, se le añaden los datos
     */
    private fun signTaskSuccessfully(
        isSigningUp: Boolean,
        customUser: User,
        task: Task<AuthResult>
    ) {
        val firebaseUser = task.result!!.user

        user = customUser

        if (user.username.isEmpty() &&
            firebaseUser!!.displayName != null
        ) {
            user.username = firebaseUser.displayName.toString()
        }

        if (firebaseUser!!.phoneNumber != null) {
            user.phoneNumber = firebaseUser.phoneNumber.toString()
        }

        if (firebaseUser!!.photoUrl != null) {
            user.photoURL = firebaseUser.photoUrl.toString()
        }

        if (isSigningUp) {
            FirebaseDBManager.createUserData(activity, user)
        }

        activity.continueApp(user)
    }

    private fun signUpTaskExceptionHandler(task: Task<AuthResult>) {
        val taskExceptionHandler = task.exception

        if (taskExceptionHandler is FirebaseAuthWeakPasswordException) {
            Toast.makeText(activity, R.string.login_weak_password, Toast.LENGTH_LONG).show()
        }
        else if (taskExceptionHandler is FirebaseAuthInvalidCredentialsException) {
            Toast.makeText(activity, R.string.login_mal_formed_mail, Toast.LENGTH_LONG).show()
        }
        else if (taskExceptionHandler is FirebaseAuthUserCollisionException) {
            Toast.makeText(activity, R.string.login_already_registered, Toast.LENGTH_LONG).show()
        }
    }

    private fun logInTaskExceptionHandler(task: Task<AuthResult>) {
        val taskExceptionHandler = task.exception

        if (taskExceptionHandler is FirebaseAuthInvalidUserException) {
            Toast.makeText(activity, R.string.login_bad_credentials, Toast.LENGTH_LONG).show()
        }
        else if (taskExceptionHandler is FirebaseAuthInvalidCredentialsException) {
            Toast.makeText(activity, R.string.login_bad_credentials, Toast.LENGTH_LONG).show()
        }
    }

    private fun getPhoneNumber(activity: Activity): String? {
        var mPhoneNumber = ""
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE)
            == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_NUMBERS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val tMgr = activity.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            mPhoneNumber = tMgr.line1Number
        }
        return mPhoneNumber
    }

    fun getUser(activity: Activity): User {
        val firebaseUser = auth.currentUser

        val user = User()
        if(firebaseUser != null) {
            user.mail = firebaseUser.email.toString()
            user.username = firebaseUser.displayName.toString()
            user.phoneNumber = if (firebaseUser.phoneNumber != null) firebaseUser.phoneNumber.toString() else ""
            user.photoURL = firebaseUser.photoUrl.toString()
        }

        return user
    }
}