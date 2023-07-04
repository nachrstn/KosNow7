package com.example.kosnow

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class TambahAdminActivity : AppCompatActivity() {
    private val title = "KosNow"
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
        setContentView(R.layout.activity_tambah_admin)
        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar)
        setActionBarTitle(title)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        username = findViewById(R.id.username)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        repassword = findViewById(R.id.retype)
        signup = findViewById(R.id.signup)
        auth = FirebaseAuth.getInstance()
        signup!!.setOnClickListener {
            val txt_username: String = username?.text.toString()
            val txt_email: String = email?.text.toString()
            val txt_password: String = password?.text.toString()
            val txt_repassword: String = repassword?.text.toString()
            if (TextUtils.isEmpty(txt_username) || TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(
                    txt_password
                ) || TextUtils.isEmpty(txt_repassword)
            ) {
                Toast.makeText(this@TambahAdminActivity, R.string.mustfill, Toast.LENGTH_SHORT)
                    .show()
            } else if (txt_password.length < 6) {
                Toast.makeText(this@TambahAdminActivity, R.string.mustpass, Toast.LENGTH_SHORT)
                    .show()
            } else if (txt_repassword != txt_password) {
                Toast.makeText(this@TambahAdminActivity, R.string.mustre, Toast.LENGTH_SHORT).show()
            } else {
                register(txt_username, txt_email, txt_password)
            }
        }
    }

    private fun setActionBarTitle(title: String) {
        if (supportActionBar != null) {
            supportActionBar?.setTitle(title)
        }
    }

    private fun register(username: String, email: String, password: String) {
        auth?.createUserWithEmailAndPassword(email, password)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser: FirebaseUser = auth?.currentUser!!
                    val userid: String = firebaseUser.uid
                    reference =
                        FirebaseDatabase.getInstance().getReference("Users").child(userid)
                    val hashmap = HashMap<String, String>()
                    hashmap["id"] = userid
                    hashmap["username"] = username
                    hashmap["imageURL"] = "default"
                    hashmap["status"] = "offline"
                    hashmap["level"] = "admin"
                    hashmap["search"] = username.lowercase(Locale.getDefault())
                    startActivity(Intent(this@TambahAdminActivity, MainActivity::class.java))
                } else {
                    Toast.makeText(
                        this@TambahAdminActivity,
                        R.string.cantregist,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}