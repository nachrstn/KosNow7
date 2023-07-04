package com.example.kosnow

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.widget.EditText
import android.widget.ImageView
import com.bumptech.glide.Glide
import java.util.*

class RegisterActivity : AppCompatActivity() {
    var username: EditText? = null
    var email: EditText? = null
    var password: EditText? = null
    var repassword: EditText? = null
    var signup: Button? = null
    var signin: TextView? = null
    var auth: FirebaseAuth? = null
    var reference: DatabaseReference? = null

    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_register)
        username = findViewById(R.id.username)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        repassword = findViewById(R.id.retype)
        signup = findViewById<Button>(R.id.signup)
        signin = findViewById<TextView>(R.id.signin)
        auth = FirebaseAuth.getInstance()
        val ivLogo = findViewById<ImageView>(R.id.logo)
        Glide.with(this).load(R.drawable.ic_logo).into(ivLogo)
        signup!!.setOnClickListener {
            val txt_username: String = username?.getText().toString()
            val txt_email: String = email?.getText().toString()
            val txt_password: String = password?.getText().toString()
            val txt_repassword: String = repassword?.getText().toString()
            if (TextUtils.isEmpty(txt_username) || TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(
                    txt_password
                ) || TextUtils.isEmpty(txt_repassword)
            ) {
                Toast.makeText(this@RegisterActivity, R.string.mustfill, Toast.LENGTH_SHORT).show()
            } else if (txt_password.length < 6) {
                Toast.makeText(this@RegisterActivity, R.string.mustpass, Toast.LENGTH_SHORT).show()
            } else if (txt_repassword != txt_password) {
                Toast.makeText(this@RegisterActivity, R.string.mustre, Toast.LENGTH_SHORT).show()
            } else {
                register(txt_username, txt_email, txt_password)
            }
        }
        signin?.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        })
    }

    override fun onBackPressed() {
        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun register(username: String, email: String, password: String) {
        auth?.createUserWithEmailAndPassword(email, password)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful()) {
                    val firebaseUser: FirebaseUser = auth?.getCurrentUser()!!
                    val userid: String = firebaseUser.getUid()
                    reference =
                        FirebaseDatabase.getInstance().getReference("Users").child(userid)
                    val hashmap = HashMap<String, String>()
                    hashmap["id"] = userid
                    hashmap["username"] = username
                    hashmap["imageURL"] = "default"
                    hashmap["status"] = "offline"
                    hashmap["level"] = "user"
                    hashmap["search"] = username.lowercase(Locale.getDefault())
                    reference?.setValue(hashmap)
                        ?.addOnCompleteListener(object : OnCompleteListener<Void?> {
                            override fun onComplete(task: Task<Void?>) {
                                if (task.isSuccessful) {
                                    val intent =
                                        Intent(this@RegisterActivity, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                        })
                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        R.string.cantregist,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}