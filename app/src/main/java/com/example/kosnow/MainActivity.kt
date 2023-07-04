package com.example.kosnow

import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.kosnow.Adapter.KamarListAdapter
import com.example.kosnow.Fragments.HomeFragment
import com.example.kosnow.Fragments.ProfileFragment
import com.example.kosnow.Fragments.TagihanFragment
import com.example.kosnow.Model.Kamar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {
    var botNav: BottomNavigationView? = null
    var firebaseuser: FirebaseUser? = null
    var reference: DatabaseReference? = null
    private val title = "KosNow"
    var kamarListAdapter: KamarListAdapter? = null
    private val sewakamar: RecyclerView? = null
    private val list: List<Kamar> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        setActionBarTitle(title)
        botNav = findViewById(R.id.bottom_navigation)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment()).commit()
        botNav?.setOnNavigationItemSelectedListener(navListener)
        firebaseuser = FirebaseAuth.getInstance().currentUser
        reference =
            firebaseuser?.let { FirebaseDatabase.getInstance().getReference("Users").child(it.uid) }
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

    private val navListener: BottomNavigationView.OnNavigationItemSelectedListener =
        object : BottomNavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                var selectedFragment: Fragment? = null
                when (item.itemId) {
                    R.id.nav_home -> selectedFragment = HomeFragment()
                    R.id.nav_profil -> selectedFragment = ProfileFragment()
                    R.id.nav_payment -> selectedFragment = TagihanFragment()
                }
                selectedFragment?.let {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, it).commit()
                }
                return true
            }
        }

    private fun status(status: String) {
        reference =
            firebaseuser?.uid?.let {
                FirebaseDatabase.getInstance().getReference("Users").child(it)
            }
        val hashMap = HashMap<String, Any>()
        hashMap["status"] = status
        reference!!.updateChildren(hashMap)
    }

    override fun onResume() {
        super.onResume()
        status("online")
    }

    override fun onPause() {
        super.onPause()
        status("offline")
    }

    private fun setActionBarTitle(title: String) {
        if (supportActionBar != null) {
            supportActionBar?.title = title
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_bar, menu)

//        MenuItem searchItem = menu.findItem(R.id.action_search);
//        SearchView searchView = (SearchView) searchItem.getActionView();
//        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                return false;
//            }
//        });
        return true
    }

    fun search(harga: String?, luas: String?, fasilitas: String?) {
        FirebaseDatabase.getInstance().reference.child("Kamar").equalTo("harga").startAt(harga)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                    val id: String? = dataSnapshot.key
                    if (dataSnapshot.child("luas").exists() && dataSnapshot.child("fasilitas")
                            .exists()
                    ) {
                        if (dataSnapshot.child("luas").getValue(String::class.java)
                                .equals(luas) && dataSnapshot.child("fasilitas").getValue(
                                String::class.java
                            ).equals(fasilitas)
                        ) {
                            //list.add(kamar);
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}