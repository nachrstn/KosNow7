package com.example.kosnow

import android.app.DatePickerDialog
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
import com.example.kosnow.Model.KamarIsi
import com.example.kosnow.Model.Pembayaran
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class CekStrukActivity : AppCompatActivity() {
    private val title = "KosNow"
    var datePickerDialog: DatePickerDialog? = null
    var dateFormatter: SimpleDateFormat? = null
    var dateResult: TextView? = null
    var penyewa: TextView? = null
    var id_user: TextView? = null
    var id_kamare: TextView? = null
    var hargaKamar: TextView? = null
    var lama: TextView? = null
    var tgls: TextView? = null
    var tagih: Button? = null
    var konfirmasi: Button? = null
    var date: Date? = null
    var key_pemb: String? = null
    var lamas: String? = null
    var tgl_depan: String? = null
    var id_kamar: String? = null
    var id_users: String? = null
    var key_kamaris: String? = null
    var tgl: String? = null
    var firebaseuser: FirebaseUser? = null
    var reference: DatabaseReference? = null
    var dreference: DatabaseReference? = null
    var dreference2: DatabaseReference? = null
    var pembayaran_ref: DatabaseReference? = null
    var isi_reference: DatabaseReference? = null
    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_cek_struk)
        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar)
        setActionBarTitle(title)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val intent: Intent = intent
        id_kamar = intent.getStringExtra("id_kamar")
        id_users = intent.getStringExtra("id_user")
        val nomor: String? = intent.getStringExtra("nomor")
        val nama: String? = intent.getStringExtra("username")
        val lokasi: String? = intent.getStringExtra("lokasi")
        val fasilitas: String? = intent.getStringExtra("fasilitas")
        val luas: String? = intent.getStringExtra("luas")
        val harga: String? = intent.getStringExtra("harga")
        val struk: String? = intent.getStringExtra("struk")
        lamas = intent.getStringExtra("lama")
        tgl_depan = intent.getStringExtra("tgl_depan")
        tgl = intent.getStringExtra("tgl")
        key_pemb = intent.getStringExtra("key")
        //key_kamaris = intent.getStringExtra("kamarisi_key");
        if (nama != null && nomor != null && lokasi != null && fasilitas != null && luas != null && harga != null && struk != null) {
            setData(nama, nomor, lokasi, fasilitas, luas, harga, struk, lamas, tgl_depan)
        }
        id_user = findViewById<TextView>(R.id.id_user)
        tagih = findViewById<Button>(R.id.tagih)
        firebaseuser = FirebaseAuth.getInstance().currentUser
        reference =
            firebaseuser?.uid?.let {
                FirebaseDatabase.getInstance().getReference("Users").child(it)
            }
        konfirmasi = findViewById<Button>(R.id.konfirmasikamar)
        pembayaran_ref = key_pemb?.let {
            FirebaseDatabase.getInstance().getReference("Pembayaran").child(
                it
            )
        }
        pembayaran_ref?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (applicationContext == null) {
                    return
                }
                val pembayaran: Pembayaran? = dataSnapshot.getValue(Pembayaran::class.java)
                if (pembayaran?.status.equals("Sudah Bayar")) {
                    konfirmasi!!.visibility = View.INVISIBLE
                    tagih!!.visibility = View.VISIBLE
                } else {
                    konfirmasi!!.visibility = View.VISIBLE
                    tagih!!.visibility = View.INVISIBLE
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        konfirmasi!!.setOnClickListener {
            masukin_kekamar()
            try {
                konfirmasi_bayar()
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            startActivity(
                Intent(
                    this@CekStrukActivity,
                    MainActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
        tagih!!.setOnClickListener {
            pembayaran_ref = FirebaseDatabase.getInstance().getReference("Pembayaran")
            pembayaran_ref?.child(key_pemb.toString())?.child("status")?.setValue("Belum Bayar")
            startActivity(
                Intent(
                    this@CekStrukActivity,
                    MainActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    private fun setActionBarTitle(title: String) {
        if (supportActionBar != null) {
            supportActionBar?.setTitle(title)
        }
    }

    private fun setData(
        nama: String,
        nokamar: String,
        lokasi: String,
        fas: String,
        luas: String,
        harga: String,
        gambar: String,
        lamas: String?,
        tgl: String?
    ) {
        penyewa = findViewById<TextView>(R.id.penyewa)
        val noKamar: TextView = findViewById<TextView>(R.id.nomor)
        val lokasis: TextView = findViewById<TextView>(R.id.lokasi)
        val fasilitas: TextView = findViewById<TextView>(R.id.fasilitas)
        val luasKamar: TextView = findViewById<TextView>(R.id.luas)
        val image: ImageView = findViewById(R.id.strukbayar)
        hargaKamar = findViewById<TextView>(R.id.harga)
        lama = findViewById<TextView>(R.id.lama)
        tgls = findViewById<TextView>(R.id.hasil_tgl)
        setCurrency(hargaKamar)
        penyewa?.setText(nama)
        noKamar.text = nokamar
        lokasis.text = lokasi
        fasilitas.text = fas
        luasKamar.text = luas
        hargaKamar?.setText(harga)
        lama?.setText(lamas)
        val fucek = tgl!!.toLong()
        tgls?.setText(getDate(fucek, "dd-MM-yyyy"))
        Glide.with(this)
            .load(gambar)
            .apply(RequestOptions().override(1000, 1000))
            .into(image)
    }

    private fun setCurrency(harga: TextView?) {
        if (harga != null) {
            harga.addTextChangedListener(object : TextWatcher {
                private var current = ""
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

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

    @Throws(ParseException::class)
    fun konfirmasi_bayar() {
        pembayaran_ref = FirebaseDatabase.getInstance().getReference("Pembayaran")
        dreference = FirebaseDatabase.getInstance().getReference("Kamar_terisi")
        dreference2 = FirebaseDatabase.getInstance().getReference("Kamar")
        pembayaran_ref!!.child(key_pemb.toString()).child("status").setValue("Sudah Bayar")
        val subtract = lamas!!.toInt()
        val result = subtract - 1
        val cal = Calendar.getInstance()
        val formatter = SimpleDateFormat("dd-MM-yyyy")
        cal.time = formatter.parse(tgls?.getText().toString())
        cal.add(Calendar.MONTH, 1)
        val TempBulan = java.lang.Long.toString(cal.timeInMillis)
        pembayaran_ref?.child(key_pemb.toString())?.child("bulan_depan")?.setValue(TempBulan.trim { it <= ' ' })
        dreference2?.child(id_kamar.toString())?.child("tersedia")?.setValue("tidak")
        isi_reference = FirebaseDatabase.getInstance().getReference("Kamar_terisi")
        if (result == 0) {
            val myRef1: DatabaseReference? = pembayaran_ref?.child(key_pemb.toString())
            val myRef2: DatabaseReference? = dreference?.child(id_kamar.toString())
            dreference2?.child(id_kamar.toString())?.child("tersedia")?.setValue("ya")
            isi_reference?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (dataSnapshot1 in dataSnapshot.children) {
                        val kamarIsi: KamarIsi? = dataSnapshot1.getValue(KamarIsi::class.java)
                        kamarIsi?.key = dataSnapshot1.key
                        if (kamarIsi?.id_kamar.equals(id_kamar)) {
                            kamarIsi?.key?.let { isi_reference?.child(it)?.removeValue() }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
            myRef1?.removeValue()
            myRef2?.removeValue()
        } else {
            key_pemb?.let {
                pembayaran_ref!!.child(it).child("sisa_bayar").setValue(Integer.toString(result))
            }
            key_pemb?.let { pembayaran_ref!!.child(it).child("struk").setValue("") }
        }
    }

    fun masukin_kekamar() {
        isi_reference = FirebaseDatabase.getInstance().getReference("Kamar_terisi")
        val TempKamar = id_kamar!!.trim { it <= ' ' }
        val TempUser = id_users!!.trim { it <= ' ' }
        val TempPemb = key_pemb!!.trim { it <= ' ' }
        val TempAwal = tgl!!.trim { it <= ' ' }
        val TempSisa: String = lama?.text.toString().trim { it <= ' ' }
        val MasukUploadId: String? = isi_reference?.push()?.key
        val kamarIsi = KamarIsi(TempKamar, TempUser, TempPemb, TempAwal, TempSisa)
        MasukUploadId?.let { isi_reference?.child(it)?.setValue(kamarIsi) }
    }

    companion object {
        fun getDate(milliSeconds: Long, dateFormat: String?): String {
            // Create a DateFormatter object for displaying date in specified format.
            val formatter = SimpleDateFormat(dateFormat)

            // Create a calendar object that will convert the date and time value in milliseconds to date.
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = milliSeconds
            return formatter.format(calendar.time)
        }
    }
}