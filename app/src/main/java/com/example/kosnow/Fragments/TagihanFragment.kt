package com.example.kosnow.Fragments

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kosnow.Adapter.PembayaranAdapter
import com.example.kosnow.BayarActivity
import com.example.kosnow.Model.KamarIsi
import com.example.kosnow.Model.Pembayaran
import com.example.kosnow.Model.User
import com.example.kosnow.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

class TagihanFragment : Fragment() {
    var profile_image: CircleImageView? = null
    var firebaseuser: FirebaseUser? = null
    var reference: DatabaseReference? = null
    var isi_reference: DatabaseReference? = null
    var pembayaran: DatabaseReference? = null
    var progressDialog: ProgressDialog? = null
    private var pembayaranAdapter: PembayaranAdapter? = null
    private var tagihan: RecyclerView? = null
    private var list: MutableList<Pembayaran>? = null
    private var list2: MutableList<KamarIsi>? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v: View = inflater.inflate(R.layout.fragment_tagihan, container, false)
        progressDialog = ProgressDialog(context)
        progressDialog!!.setMessage("Loading Data from Firebase Database")
        progressDialog!!.show()
        profile_image = v.findViewById(R.id.profile_image)
        tagihan = v.findViewById<RecyclerView>(R.id.list_tagihan)
        tagihan?.setHasFixedSize(true)
        tagihan?.layoutManager = LinearLayoutManager(context)
        firebaseuser = FirebaseAuth.getInstance().currentUser
        reference =
            firebaseuser?.let { FirebaseDatabase.getInstance().getReference("Users").child(it.uid) }
        reference!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (context == null) {
                    return
                }
                val user: User? = dataSnapshot.getValue(User::class.java)
                if (user != null) {
                    if (user.imageURL.equals("default")) {
                        profile_image?.setImageResource(R.mipmap.ic_launcher)
                    } else {
                        //change this
                        Glide.with(context!!).load(user.imageURL).into(profile_image!!)
                    }
                }
                if (user != null) {
                    if (user.level.equals("user")) {
                        reference = FirebaseDatabase.getInstance().getReference("Pembayaran")
                        reference!!.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for (dataSnapshot1 in dataSnapshot.getChildren()) {
                                    val pembayaran: Pembayaran? =
                                        dataSnapshot1.getValue(Pembayaran::class.java)
                                    if (pembayaran != null) {
                                        if (pembayaran.id_user.equals(firebaseuser?.uid)) {
                                            if (pembayaran.status.equals("Belum Bayar")) {
                                                startActivity(
                                                    Intent(
                                                        requireContext(),
                                                        BayarActivity::class.java
                                                    ).putExtra("id_pemesanan",pembayaran.id_pembayaran)
                                                )
                                            }
                                            progressDialog!!.dismiss()
                                        }
                                    }
                                }
                                progressDialog!!.dismiss()
                                val fragmentManager = fragmentManager
                                val fragmentTransaction =
                                    fragmentManager!!.beginTransaction()
                                fragmentTransaction.replace(
                                    R.id.fragment_container,
                                    SudahBayarFragment()
                                )
                                fragmentTransaction.commit()
                            }
                            override fun onCancelled(databaseError: DatabaseError) {}
                        })
                    } else if (user.level.equals("admin")) {
                        showTagihan()
                    }
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

    private fun showTagihan() {
        list = ArrayList<Pembayaran>()
        list2 = ArrayList<KamarIsi>()
        isi_reference = FirebaseDatabase.getInstance().getReference("Kamar_terisi")
        reference = FirebaseDatabase.getInstance().getReference("Pembayaran")
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                list!!.clear()
                for (snapshot in dataSnapshot.getChildren()) {
                    val pembayaran: Pembayaran? = snapshot.getValue(Pembayaran::class.java)
                    pembayaran?.key = snapshot.key
                    if (pembayaran != null) {
                        list!!.add(pembayaran)
                    }
                    isi_reference!!.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (snapshot in dataSnapshot.getChildren()) {
                                val kamarIsi: KamarIsi? = snapshot.getValue(KamarIsi::class.java)
                                kamarIsi?.key = snapshot.key
                                if (kamarIsi != null) {
                                    list2!!.add(kamarIsi)
                                }
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
                }
                pembayaranAdapter = PembayaranAdapter(
                    context, list as ArrayList<Pembayaran>,
                    list2 as ArrayList<KamarIsi>
                )
                tagihan?.setAdapter(pembayaranAdapter)
                progressDialog?.dismiss()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println(databaseError.getDetails() + " " + databaseError.getMessage())
                progressDialog?.dismiss()
            }
        })
    }
}