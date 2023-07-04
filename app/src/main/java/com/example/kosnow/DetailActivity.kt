package com.example.kosnow

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.kosnow.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.text.NumberFormat
import java.util.*

class DetailActivity : AppCompatActivity() {
    private val title = "KosNow"
    var sewa: Button? = null
    var nomor2: TextView? = null
    var lokasi2: TextView? = null
    var fasilitas2: TextView? = null
    var luas2: TextView? = null
    var harga2: TextView? = null
    var id_kamar2: TextView? = null
    var firebaseuser: FirebaseUser? = null
    var reference: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        sewa = findViewById<Button>(R.id.sewa)
        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar)
        setActionBarTitle(title)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        id_kamar2 = findViewById<TextView>(R.id.id_kamar)
        id_kamar2?.visibility = View.INVISIBLE
        nomor2 = findViewById<TextView>(R.id.no_kamar)
        lokasi2 = findViewById<TextView>(R.id.lokasi)
        fasilitas2 = findViewById<TextView>(R.id.fasilitas)
        luas2 = findViewById<TextView>(R.id.luas)
        harga2 = findViewById<TextView>(R.id.harga)
        val intent: Intent = intent
        val id_kamar: String = intent.getStringExtra("id_kamar").toString()
        val gambar: String = intent.getStringExtra("gambar").toString()
        val nama: String = intent.getStringExtra("nomor").toString()
        val lokasi: String = intent.getStringExtra("lokasi").toString()
        val fasilitas: String = intent.getStringExtra("fasilitas").toString()
        val luas: String = intent.getStringExtra("luas").toString()
        val harga: String = intent.getStringExtra("harga").toString()
        setData(id_kamar, gambar, nama, lokasi, fasilitas, luas, harga)
        firebaseuser = FirebaseAuth.getInstance().currentUser
        reference =
            firebaseuser?.uid?.let {
                FirebaseDatabase.getInstance().getReference("Users").child(it)
            }
        reference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (applicationContext == null) {
                    return
                }
                val user: User? = dataSnapshot.getValue(User::class.java)
                if (user?.level.equals("admin")) {
                    sewa!!.visibility = View.GONE
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        sewa!!.setOnClickListener {
            val intent = Intent(this@DetailActivity, PemesananActivity::class.java)
            intent.putExtra("id_kamar", id_kamar2?.text)
            intent.putExtra("nomor", nomor2?.text)
            intent.putExtra("lokasi", lokasi2?.text)
            intent.putExtra("fasilitas", fasilitas2?.text)
            intent.putExtra("luas", luas2?.text)
            intent.putExtra("harga", harga2?.text)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    private fun setActionBarTitle(title: String) {
        if (supportActionBar != null) {
            supportActionBar?.setTitle(title)
        }
    }

    private fun setData(
        id_kamar: String,
        gambar: String,
        nokamar: String,
        lokasi: String,
        fas: String,
        luas: String,
        harga: String
    ) {
        val idKamar: TextView = findViewById<TextView>(R.id.id_kamar)
        val image: ImageView = findViewById<ImageView>(R.id.gambar_kamar)
        val noKamar: TextView = findViewById<TextView>(R.id.no_kamar)
        val lokasis: TextView = findViewById<TextView>(R.id.lokasi)
        val fasilitas: TextView = findViewById<TextView>(R.id.fasilitas)
        val luasKamar: TextView = findViewById<TextView>(R.id.luas)
        val hargaKamar: TextView = findViewById<TextView>(R.id.harga)
        setCurrency(hargaKamar)
        idKamar.text = id_kamar
        noKamar.text = nokamar
        lokasis.text = lokasi
        fasilitas.text = fas
        luasKamar.text = luas
        hargaKamar.text = harga
        Glide.with(this)
            .load(gambar)
            .apply(RequestOptions().override(250, 250))
            .into(image)
    }

    private fun setCurrency(harga: TextView) {
        harga.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.toString() != current) {
                    harga.removeTextChangedListener(this)
                    val local = Locale("id", "id")
                    val replaceable = String.format(
                        "[Rp,.\\s]",
                        NumberFormat.getCurrencyInstance().currency
                            .getSymbol(local)
                    )
                    val cleanString: String = s.toString().replace(
                        replaceable.toRegex(),
                        ""
                    )
                    val parsed: Double
                    parsed = try {
                        cleanString.toDouble()
                    } catch (e: NumberFormatException) {
                        0.00
                    }
                    val formatter = NumberFormat
                        .getCurrencyInstance(local)
                    formatter.maximumFractionDigits = 0
                    formatter.isParseIntegerOnly = true
                    val formatted = formatter.format(parsed)
                    val replace = String.format(
                        "[Rp\\s]",
                        NumberFormat.getCurrencyInstance().currency
                            .getSymbol(local)
                    )
                    val clean = formatted.replace(replace.toRegex(), "")
                    current = formatted
                    harga.text = clean
                    harga.addTextChangedListener(this)
                }
            }
        })
    }
}