package com.example.kosnow

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.kosnow.Model.Pembayaran
import com.example.kosnow.Model.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import android.widget.EditText
import java.text.DateFormat
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class PemesananActivity : AppCompatActivity() {
    private val title = "KowNow"
    var datePickerDialog: DatePickerDialog? = null
    var dateFormatter: SimpleDateFormat? = null
    var dateResult: TextView? = null
    var penyewa: TextView? = null
    var idUser: TextView? = null
    var idKamar: TextView? = null
    var tvHargaKamar: TextView? = null
    var etLama: EditText? = null
    var btnDatePicker: Button? = null
    var btnSewaKamar: Button? = null
    var firebaseUser: FirebaseUser? = null
    var userRef: DatabaseReference? = null
    var pembayaranRef: DatabaseReference? = null

    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_pemesanan)
        penyewa = findViewById(R.id.penyewa)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setActionBarTitle(title)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val intent: Intent = intent

        val id_kamar: String? = intent.getStringExtra("id_kamar")
        val nama: String? = intent.getStringExtra("nomor")
        val lokasi: String? = intent.getStringExtra("lokasi")
        val fasilitas: String? = intent.getStringExtra("fasilitas")
        val luas: String? = intent.getStringExtra("luas")
        val harga: String? = intent.getStringExtra("harga")

        if (id_kamar != null && nama != null && lokasi != null && fasilitas != null && luas != null && harga != null) {
            setData(id_kamar, nama, lokasi, fasilitas, luas, harga)
        }
        idUser = findViewById<TextView>(R.id.id_user)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        userRef = firebaseUser?.uid?.let { FirebaseDatabase.getInstance().getReference("Users").child(it) }
        userRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (applicationContext == null) {
                    return
                }
                val user: User? = dataSnapshot.getValue(User::class.java)
                penyewa?.text = user?.username
                idUser?.text = user?.id
                idUser?.visibility = View.INVISIBLE
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
        dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        dateResult = findViewById(R.id.hasil_tgl)
        btnDatePicker = findViewById(R.id.tanggal)
        btnSewaKamar = findViewById(R.id.sewakamar)
        etLama = findViewById(R.id.lama)
        btnDatePicker!!.setOnClickListener { showDateDialog() }
        btnSewaKamar!!.setOnClickListener {
            if (!TextUtils.isEmpty(
                    etLama?.text.toString()
                ) && !TextUtils.isEmpty(dateResult?.text.toString())
            ) {
                try {
                    PesanKamar()
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
            } else Snackbar.make(
                it,
                "Data tidak boleh kosong",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun setActionBarTitle(title: String) {
        if (supportActionBar != null) {
            supportActionBar?.setTitle(title)
        }
    }

    private fun setData(
        id_kamar: String,
        nokamar: String,
        lokasi: String,
        fas: String,
        luas: String,
        harga: String
    ) {
        this.idKamar = findViewById(R.id.id_kamar)
        val noKamar: TextView = findViewById(R.id.nomor)
        val lokasis: TextView = findViewById(R.id.lokasi)
        val fasilitas: TextView = findViewById(R.id.fasilitas)
        val luasKamar: TextView = findViewById(R.id.luas)
        tvHargaKamar = findViewById(R.id.harga)
        this.idKamar?.visibility = View.INVISIBLE
        setCurrency(tvHargaKamar)
        this.idKamar?.text = id_kamar
        noKamar.text = nokamar
        lokasis.text = lokasi
        fasilitas.text = fas
        luasKamar.text = luas
        tvHargaKamar?.text = harga
    }

    private fun setCurrency(harga: TextView?) {
        harga?.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.toString() != current) {
                    harga.removeTextChangedListener(this)
                    val local = Locale("id", "id")
                    val replaceable = NumberFormat.getCurrencyInstance().currency?.let {
                        String.format(
                            "[Rp,.\\s]",
                            it.getSymbol(local)
                        )
                    }
                    val cleanString: String? = replaceable?.let {
                        s.toString().replace(
                            it.toRegex(),
                            ""
                        )
                    }
                    val parsed: Double
                    parsed = try {
                        cleanString!!.toDouble()
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
                    harga?.setText(clean)
                    harga?.addTextChangedListener(this)
                }
            }
        })
    }

    private fun showDateDialog() {
        /**
         * Calendar untuk mendapatkan tanggal sekarang
         */
        val newCalendar = Calendar.getInstance()
        /**
         * Initiate DatePicker dialog
         */
        datePickerDialog = DatePickerDialog(
            this,
            object : DatePickerDialog.OnDateSetListener {
                override fun onDateSet(
                    view: DatePicker,
                    year: Int,
                    monthOfYear: Int,
                    dayOfMonth: Int
                ) {
                    /**
                     * Method ini dipanggil saat kita selesai memilih tanggal di DatePicker
                     */
                    /**
                     * Set Calendar untuk menampung tanggal yang dipilih
                     */
                    val newDate = Calendar.getInstance()
                    newDate[year, monthOfYear] = dayOfMonth
                    /**
                     * Update TextView dengan tanggal yang kita pilih
                     */
                    dateResult?.setText(dateFormatter!!.format(newDate.time))
                }
            },
            newCalendar[Calendar.YEAR],
            newCalendar[Calendar.MONTH],
            newCalendar[Calendar.DAY_OF_MONTH]
        )
        /**
         * Tampilkan DatePicker dialog
         */
        datePickerDialog!!.show()
    }

    @Throws(ParseException::class)
    fun PesanKamar() {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        pembayaranRef = FirebaseDatabase.getInstance().getReference("Pembayaran")
        val TempKamar: String = idKamar?.getText().toString().trim { it <= ' ' }
        val TempUser: String = idUser?.getText().toString().trim { it <= ' ' }
        val TempStatus = "Belum Bayar".trim { it <= ' ' }
        val tgl: String = dateResult?.getText().toString()
        val formatter: DateFormat = SimpleDateFormat("dd-MM-yyyy")
        val dateMulaiSewa = formatter.parse(tgl)
        val TempTglBayar = java.lang.Long.toString(dateMulaiSewa.time).trim { it <= ' ' }
        val cal = Calendar.getInstance()
        cal.time = formatter.parse(tgl)
        cal.add(Calendar.MONTH, 1)

        val TempBulanDepan = java.lang.Long.toString(cal.timeInMillis).trim { it <= ' ' }
        val TempStruk = ""
        val Harga: String = tvHargaKamar?.getText().toString().trim { it <= ' ' }
        val TempHarga = Harga.replace("\\.".toRegex(), "")
        val TempSisa: String = etLama?.getText().toString().trim()
        val PembUploadId: String? = pembayaranRef!!.push().key
        val kamarUploadInfo = Pembayaran(
            PembUploadId,
            TempUser,
            TempKamar,
            TempStatus,
            TempTglBayar,
            TempBulanDepan,
            TempHarga,
            TempStruk,
            TempSisa
        )
        if (PembUploadId != null) {
            pembayaranRef!!.child(PembUploadId).setValue(kamarUploadInfo)
        }
        startActivity(Intent(this@PemesananActivity, BayarActivity::class.java).putExtra("id_pemesanan",PembUploadId))
        finish()
    }
}