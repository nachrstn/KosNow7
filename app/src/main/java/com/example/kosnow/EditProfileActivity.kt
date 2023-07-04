package com.example.kosnow

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.kosnow.Model.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import android.widget.EditText
import java.util.*

class EditProfileActivity : AppCompatActivity() {
    private val title = "KosNow"
    var username: EditText? = null
    var email: EditText? = null
    var password: EditText? = null
    var repassword: EditText? = null
    var oldpassword: EditText? = null
    var signup: Button? = null
    var signin: TextView? = null
    var auth: FirebaseAuth? = null
    var fuser: FirebaseUser? = null
    var reference: DatabaseReference? = null
    var references: DatabaseReference? = null
    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_edit_profile)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setActionBarTitle(title)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        username = findViewById(R.id.username)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        oldpassword = findViewById(R.id.oldpassword)
        repassword = findViewById(R.id.retype)
        signup = findViewById(R.id.signup)
        auth = FirebaseAuth.getInstance()
        fuser = FirebaseAuth.getInstance().currentUser
        reference = fuser?.uid?.let { FirebaseDatabase.getInstance().getReference("Users").child(it) }
        reference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userss: User? = dataSnapshot.getValue(User::class.java)
                username?.setText(userss?.username)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        references = FirebaseDatabase.getInstance().getReference("Kamar")
        email?.setText(fuser?.email)
        // Get auth credentials from the user for re-authentication
        val txt_username: String = username?.text.toString()
        val txt_email: String = email?.text.toString()
        val txt_oldpassword: String = oldpassword?.text.toString()
        val txt_password: String = password?.text.toString()
        val users: FirebaseUser? = FirebaseAuth.getInstance().currentUser

        signup!!.setOnClickListener {
            if (txt_oldpassword == "" && txt_password == "" && txt_email == fuser?.email) {
                val hashmap = HashMap<String, String>()
                hashmap["username"] = txt_username
                hashmap["search"] = txt_username.lowercase(Locale.getDefault())
            } else if (txt_oldpassword != "" && txt_password != "") {
                val hashmap = HashMap<String, String>()
                hashmap["username"] = txt_username
                hashmap["search"] = txt_username.lowercase(Locale.getDefault())
                val credential: AuthCredential? = users?.email?.let { it1 ->
                    EmailAuthProvider
                        .getCredential(it1, txt_oldpassword)
                }
                if (credential != null) {
                    users.reauthenticate(credential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                users.updatePassword(txt_password)
                                    ?.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Toast.makeText(
                                                this@EditProfileActivity,
                                                "Password updated",
                                                Toast.LENGTH_SHORT
                                            )
                                        } else {
                                            Toast.makeText(
                                                this@EditProfileActivity,
                                                "Error password not updated",
                                                Toast.LENGTH_SHORT
                                            )
                                        }
                                    }
                            }
                            users.updateEmail(txt_email)
                                ?.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(
                                            this@EditProfileActivity,
                                            "User email address updated.",
                                            Toast.LENGTH_SHORT
                                        )
                                    } else {
                                        Toast.makeText(
                                            this@EditProfileActivity,
                                            "Gagal Update Email",
                                            Toast.LENGTH_SHORT
                                        )
                                    }
                                }
                        }
                }
            }
        }
    }

    private fun setActionBarTitle(title: String) {
        if (supportActionBar != null) {
            supportActionBar?.setTitle(title)
        }
    }
}