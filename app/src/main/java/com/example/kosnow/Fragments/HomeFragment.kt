package com.example.kosnow.Fragments

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kosnow.Adapter.KamarListAdapter
import com.example.kosnow.Model.Kamar
import com.example.kosnow.Model.KamarIsi
import com.example.kosnow.Model.User
import com.example.kosnow.R
import com.example.kosnow.TambahKamarActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

class HomeFragment : Fragment() {
    var fab: FloatingActionButton? = null
    var fab1: FloatingActionButton? = null
    var fab2: FloatingActionButton? = null
    var translationY = 100f
    var interpolator: OvershootInterpolator = OvershootInterpolator()
    var isMenuOpen = false
    var username: TextView? = null
    var bujank: TextView? = null
    var fab11: TextView? = null
    var fab22: TextView? = null
    var profile_image: CircleImageView? = null
    var firebaseuser: FirebaseUser? = null
    var reference: DatabaseReference? = null
    var pembayaran_ref: DatabaseReference? = null
    var isi_reference: DatabaseReference? = null
    var progressDialog: ProgressDialog? = null
    private var kamarListAdapter: KamarListAdapter? = null
    private var sewakamar: RecyclerView? = null
    private var list: MutableList<Kamar>? = null
    private var list2: MutableList<KamarIsi>? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v: View = inflater.inflate(R.layout.fragment_home, container, false)
        progressDialog = ProgressDialog(context)
        progressDialog!!.setMessage("Loading Data from Firebase Database")
        progressDialog!!.show()
        username = v.findViewById(R.id.username)
        bujank = v.findViewById(R.id.bujank)
        profile_image = v.findViewById(R.id.profile_image)
        fab = v.findViewById(R.id.tambah)
        fab1 = v.findViewById(R.id.fab1)
        //fab2 = v.findViewById(R.id.fab2);
        //fab11 = v.findViewById(R.id.fab11);
        fab22 = v.findViewById(R.id.fab22)
        //fab11.setVisibility(View.INVISIBLE);
        fab22?.visibility = View.INVISIBLE
        fab?.hide()
        fab1?.hide()
        //fab2.hide();
        sewakamar = v.findViewById(R.id.list_kamar)
        sewakamar?.setHasFixedSize(true)
        sewakamar?.layoutManager = LinearLayoutManager(context)
        firebaseuser = FirebaseAuth.getInstance().currentUser
        pembayaran_ref = FirebaseDatabase.getInstance().getReference("Pembayaran")
        isi_reference = FirebaseDatabase.getInstance().getReference("Kamar_terisi")
        reference =
            firebaseuser?.let { FirebaseDatabase.getInstance().getReference("Users").child(it.uid) }
        reference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (context == null) {
                    return
                }
                val user: User? = dataSnapshot.getValue(User::class.java)
                username?.text = user?.username
                bujank?.text = "Dashboard " + user?.level
                if (user?.imageURL.equals("default")) {
                    profile_image?.setImageResource(R.mipmap.ic_launcher)
                } else {
                    //change this
                    Glide.with(context!!).load(user?.imageURL).into(profile_image!!)
                }
                if (user?.level.equals("admin")) {
                    fab?.show()
                    fab1?.show()
                    // fab2.show();
                    fab1?.alpha = 0f
                    //fab2.setAlpha(0f);
                    fab1?.translationY = translationY
                    // fab2.setTranslationY(translationY);
                }
                if (user?.level.equals("admin")) {
                    fab?.show()
                    fab1?.show()
                    // fab2.show();
                    fab1?.alpha = 0f
                    // fab2.setAlpha(0f);
                    fab1?.translationY = translationY
                    // fab2.setTranslationY(translationY);
                }
                fab?.setOnClickListener {
                    if (isMenuOpen) {
                        closeMenu()
                    } else {
                        openMenu()
                    }
                }
                fab1?.setOnClickListener {
                    startActivity(
                        Intent(
                            activity,
                            TambahKamarActivity::class.java
                        )
                    )
                }

                isi_reference?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (snapshot in dataSnapshot.children) {
                            val kamarIsi: KamarIsi =
                                snapshot.getValue(KamarIsi::class.java) ?: return
                            if (kamarIsi.id_user
                                    .equals(firebaseuser?.uid) && user?.level.equals("user")
                            ) {
                                val fragmentManager = fragmentManager
                                val fragmentTransaction = fragmentManager!!.beginTransaction()
                                fragmentTransaction.replace(
                                    R.id.fragment_container,
                                    MemberFragment()
                                )
                                fragmentTransaction.commit()
                                progressDialog?.dismiss()
                            } else if (!kamarIsi.id_user.equals(firebaseuser?.uid)) {
                            } else if (!kamarIsi.id_user
                                    .equals(firebaseuser?.uid) && user?.level.equals("user")
                            ) {
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
                showKamar()
                progressDialog!!.dismiss()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                progressDialog!!.dismiss()
            }
        })


//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                        startActivity(new Intent(getActivity(), TambahKamarActivity.class));
//            }
//        });
        progressDialog!!.dismiss()
        return v
    }

    private fun openMenu() {
        isMenuOpen = !isMenuOpen
        fab?.animate()?.setInterpolator(interpolator)?.rotation(45f)?.setDuration(300)?.start()
        fab1?.animate()?.translationY(0f)?.alpha(1f)?.setInterpolator(interpolator)
            ?.setDuration(300)
            ?.start()
        //fab2.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        //fab11.setVisibility(View.VISIBLE);
        fab22?.visibility = View.VISIBLE
    }

    private fun closeMenu() {
        isMenuOpen = !isMenuOpen
        fab?.animate()?.setInterpolator(interpolator)?.rotation(0f)?.setDuration(300)?.start()
        fab1?.animate()?.translationY(translationY)?.alpha(0f)?.setInterpolator(interpolator)
            ?.setDuration(300)?.start()
        //fab2.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        //fab11.setVisibility(View.INVISIBLE);
        fab22?.visibility = View.INVISIBLE
    }

    //    public void onClick(View view) {
    //        switch (view.getId()) {
    //            case R.id.tambah:
    //                if (isMenuOpen) {
    //                    closeMenu();
    //                } else {
    //                    openMenu();
    //                }
    //                break;
    //            case R.id.fab1:
    //                if (isMenuOpen) {
    //                    closeMenu();
    //                } else {
    //                    openMenu();
    //                }
    //                break;
    //            case R.id.fab2:
    //                break;
    //        }
    //    }
    private fun showKamar() {
        list = ArrayList<Kamar>()
        list2 = ArrayList<KamarIsi>()
        reference = FirebaseDatabase.getInstance().getReference("kamar")
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                list!!.clear()
                for (snapshot in dataSnapshot.children) {
                    val kamar: Kamar? = snapshot.getValue(Kamar::class.java)
                    kamar?.key = snapshot.key
                    if (kamar != null) {
                        if (kamar.tersedia.equals("ya")) {
                            if (kamar != null) {
                                list!!.add(kamar)
                            }
                            isi_reference?.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    for (snapshot in dataSnapshot.children) {
                                        val kamarIsi: KamarIsi? =
                                            snapshot.getValue(KamarIsi::class.java)
                                        kamarIsi?.key = snapshot.key
                                        isi_reference!!.addValueEventListener(object :
                                            ValueEventListener {
                                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                if (kamarIsi != null) {
                                                    if (snapshot.hasChild(kamarIsi.key.toString())) {
                                                        list2!!.add(kamarIsi)
                                                    }
                                                }
                                            }

                                            override fun onCancelled(databaseError: DatabaseError) {}
                                        })
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {}
                            })
                        } else if (kamar.tersedia.equals("tidak")) {
                        }
                    }
                }
                kamarListAdapter = KamarListAdapter(
                    context, list as ArrayList<Kamar>,
                    list2 as ArrayList<KamarIsi>
                )
                sewakamar?.adapter = kamarListAdapter
                progressDialog?.dismiss()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println(databaseError.details + " " + databaseError.message)
                progressDialog?.dismiss()
            }
        })
    }
}