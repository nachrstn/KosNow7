package com.example.kosnow

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.example.kosnow.Network.ConnectivityReceiver
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.widget.EditText
import android.widget.ImageView
import androidx.core.content.ContentProviderCompat.requireContext
import com.bumptech.glide.Glide
import okhttp3.*
import java.io.IOException

class LoginActivity : AppCompatActivity(), ConnectivityReceiver.ConnectivityReceiverListener {
    var email: EditText? = null
    var password: EditText? = null
    var login: Button? = null
    var signup: TextView? = null
    var forgot: TextView? = null
    private var snackbar: Snackbar? = null
    var auth: FirebaseAuth? = null
    var firebaseuser: FirebaseUser? = null

    override fun onStart() {
        super.onStart()
        firebaseuser = FirebaseAuth.getInstance().currentUser
        if (firebaseuser != null) {
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_login)
        checkConnection()
        val ivLogo = findViewById<ImageView>(R.id.logo)
        Glide.with(this).load(R.drawable.ic_logo).into(ivLogo)
        auth = FirebaseAuth.getInstance()
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        login = findViewById<Button>(R.id.login)
        signup = findViewById<TextView>(R.id.signup)
        forgot = findViewById<TextView>(R.id.forgot)
        login!!.setOnClickListener {
            val txt_email: String = email?.text.toString()
            val txt_password: String = password?.text.toString()
            if (TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)) {
                Toast.makeText(this@LoginActivity, R.string.mustfill, Toast.LENGTH_SHORT).show()
            } else {
                auth!!.signInWithEmailAndPassword(txt_email, txt_password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                R.string.loginfail,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
        signup?.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
        forgot?.setOnClickListener {
            val intent = Intent(this@LoginActivity, ResetPasswordActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
    }

    var doubleback = false
    override fun onBackPressed() {
        if (doubleback) {
            super.onBackPressed()
            return
        }
        doubleback = true
        Toast.makeText(this, R.string.doubleback, Toast.LENGTH_SHORT).show()
        Handler().postDelayed({ doubleback = false }, 2000)
    }

    fun jaringanTest() {
        val client = OkHttpClient()
        val url = "https://www.google.com"
        val request: Request = Request.Builder()
            .url(url)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val snackbar: Snackbar = Snackbar
                    .make(
                        findViewById<View>(R.id.logins),
                        "Not Connected on Internet",
                        Snackbar.LENGTH_INDEFINITE
                    )
                snackbar.show()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val myResponse: String = response.body?.string() ?: ""
                    val snackbar2: Snackbar = Snackbar
                        .make(
                            findViewById<View>(R.id.logins),
                            "Connected on Internet",
                            Snackbar.LENGTH_SHORT
                        )
                    snackbar2.show()
                }
            }
        })
    }

    // Method to manually check connection status
    private fun checkConnection() {
        val isConnected: Boolean = ConnectivityReceiver.isConnected
        showSnack(isConnected)
    }

    // Showing the status in Snackbar
    fun showSnack(isConnected: Boolean) {
        val message: String
        val color: Int
        if (!isConnected) {
            message = "Tidak ada layanan internet"
            color = Color.RED
        } else {
            message = "Terhubung dengan internet"
            color = Color.WHITE
        }
        snackbar = Snackbar
            .make(findViewById<View>(R.id.logins), message, Snackbar.LENGTH_LONG)
        val sbView: View = snackbar!!.view
        val textView: TextView =
            sbView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.setTextColor(color)
        snackbar!!.show()
    }

    override fun onResume() {
        super.onResume()
        // register connection status listener
        MyApplication.instance?.setConnectivityListener(this)
    }

    /**
     * Callback will be triggered when there is change in
     * network connection
     */
    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        showSnack(isConnected)
    }
}