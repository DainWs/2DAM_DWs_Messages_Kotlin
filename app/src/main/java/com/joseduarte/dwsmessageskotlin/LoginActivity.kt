package com.joseduarte.dwsmessageskotlin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.joseduarte.dwsmessageskotlin.firebase.FirebaseLoginManager
import com.joseduarte.dwsmessageskotlin.firebase.GoogleSignInManager
import com.joseduarte.dwsmessageskotlin.models.User
import kotlinx.android.synthetic.main.login_activity.*

class LoginActivity : AppCompatActivity() {
    private lateinit var googleSingInManager: GoogleSignInManager
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private lateinit var auth: FirebaseAuth
    private lateinit var loginManager: FirebaseLoginManager
    private var hasSignIn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        getSupportActionBar()?.hide()

        getAllPermissions()

        auth = FirebaseAuth.getInstance()
        googleSingInManager = GoogleSignInManager(this)
        loginManager = FirebaseLoginManager(this, auth)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        val bundle = Bundle()
        bundle.putString("message", "1 Usuario ha iniciado la aplicación")
        mFirebaseAnalytics.logEvent("PantallaInicial", bundle)

        setUp()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GoogleSignInManager.RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                googleSingInManager.firebaseAuthWithGoogle(
                        GoogleSignIn.getSignedInAccountFromIntent(data),
                        auth
                )
            }
        }
    }

    private fun setUp() {
        log_in_button.setOnClickListener {
            val mailText = login_email_address_field.text.toString()
            val passwordText = login_password_field.text.toString()
            if (mailText.isNotEmpty() && passwordText.isNotEmpty()) {
                loginManager.signInUser(mailText, passwordText)
            } else {
                Toast.makeText(this@LoginActivity, R.string.log_in_empty_error, Toast.LENGTH_LONG).show()
            }
        }
        sign_in_button.setOnClickListener {
            loginManager.signUpUser()
        }
        googleSingInManager.addListener(sign_in_google_button)
    }

    override fun onResume() {
        super.onResume()
        auth.signOut()
    }

    fun continueApp() {
        clearForm()
        hasSignIn = true
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("User", loginManager.getUser(this))
        startActivity(intent)
    }

    fun continueApp(user: User) {
        clearForm()
        hasSignIn = true
        GlobalInformation.SIGN_IN_USER = user
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("User", user)
        startActivity(intent)
    }

    private fun clearForm() {
        login_email_address_field.setText("")
        login_password_field.setText("")
    }

    private fun getAllPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_PHONE_STATE),
                1
            )
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_PHONE_NUMBERS),
                1
            )
        }
    }
}
