package com.example.kosnow

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import android.widget.EditText

class ResetPasswordActivity : AppCompatActivity() {
    var email: EditText? = null
    var signin: TextView? = null
    var sent: Button? = null
    var firebaseAuth: FirebaseAuth? = null

    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_reset_password)
        email = findViewById(R.id.email)
        signin = findViewById(R.id.signin)
        sent = findViewById(R.id.sent)
        firebaseAuth = FirebaseAuth.getInstance()
        sent!!.setOnClickListener {
            val sent_email: String = email?.getText().toString()
            if (sent_email == "") {
                Toast.makeText(this@ResetPasswordActivity, R.string.mustfill, Toast.LENGTH_SHORT)
                    .show()
            } else {
                firebaseAuth?.sendPasswordResetEmail(sent_email)
                    ?.addOnCompleteListener(OnCompleteListener<Void?> { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this@ResetPasswordActivity,
                                R.string.checkemail,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val error = task.exception!!.message
                            Toast.makeText(
                                this@ResetPasswordActivity,
                                error,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }
        }
        signin?.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@ResetPasswordActivity, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        })
    }

    override fun onBackPressed() {
        val intent = Intent(this@ResetPasswordActivity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}