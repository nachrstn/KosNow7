package com.example.kosnow.Fragments

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.kosnow.Model.User
import com.example.kosnow.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

class MemberFragment : Fragment() {
    var fab: FloatingActionButton? = null
    var username: TextView? = null
    var bujank: TextView? = null
    var profile_image: CircleImageView? = null
    var firebaseuser: FirebaseUser? = null
    var reference: DatabaseReference? = null
    var pembayaran_ref: DatabaseReference? = null
    var progressDialog: ProgressDialog? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v: View = inflater.inflate(R.layout.fragment_member, container, false)
        progressDialog = ProgressDialog(context)
        progressDialog!!.setMessage("Loading Data from Firebase Database")
        progressDialog!!.show()
        username = v.findViewById(R.id.username)
        bujank = v.findViewById(R.id.bujank)
        profile_image = v.findViewById(R.id.profile_image)
        firebaseuser = FirebaseAuth.getInstance().getCurrentUser()
        reference =
            firebaseuser?.uid?.let {
                FirebaseDatabase.getInstance().getReference("Users").child(it)
            }
        reference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (context == null) {
                    return
                }
                val user: User? = dataSnapshot.getValue(User::class.java)
                username?.setText(user?.username)
                bujank?.setText("Dashboard " + user?.level)
                if (user?.imageURL.equals("default")) {
                    profile_image?.setImageResource(R.mipmap.ic_launcher)
                } else {
                    Glide.with(context!!).load(user?.imageURL).into(profile_image!!)
                }
                progressDialog!!.dismiss()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                progressDialog!!.dismiss()
            }
        })
        progressDialog!!.dismiss()
        return v
    }
}