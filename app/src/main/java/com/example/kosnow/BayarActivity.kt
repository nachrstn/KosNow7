package com.example.kosnow

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.kosnow.Model.Pembayaran
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import android.widget.EditText
import com.google.firebase.ktx.Firebase
import java.io.IOException
import java.net.URL
import java.text.NumberFormat
import java.util.*

class BayarActivity : AppCompatActivity() {
    private val title = "KosNow"
    var id_user: TextView? = null
    var harga: TextView? = null
    var ket: TextView? = null
    var bayarkamar: Button? = null
    var strukbayar: ImageView? = null
    var FilePathUri: Uri? = null
    var Image_Request_Code = 7
    var progressDialog: ProgressDialog? = null
    var firebaseuser: FirebaseUser? = null
    var storageReference: StorageReference? = null
    var pembayaran_ref: DatabaseReference? = null
    lateinit var pembId:String

    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_bayar)
        pembId = intent.extras!!.getString("id_pemesanan")!!
        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar)
        setActionBarTitle(title)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        ket = findViewById<TextView>(R.id.ket)
        harga = findViewById<TextView>(R.id.harga)
        bayarkamar = findViewById<Button>(R.id.bayar)
        setCurrency(harga)
        strukbayar = findViewById<ImageView>(R.id.strukbayar)
        firebaseuser = FirebaseAuth.getInstance().currentUser
        storageReference = FirebaseStorage.getInstance().getReference("StrukBayar")
        pembayaran_ref = FirebaseDatabase.getInstance().getReference("Pembayaran")
        progressDialog = ProgressDialog(this@BayarActivity)
        strukbayar!!.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Image"), Image_Request_Code)
        }
        pembayaran_ref?.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val pembayaran: Pembayaran? = snapshot.getValue(Pembayaran::class.java)
                    if (pembayaran?.id_pembayaran!!.equals(pembId)) {
                        harga?.text = pembayaran?.harga
                        if (!pembayaran?.struk.equals("")&&!pembayaran!!.status.equals("Sudah Bayar")) {
                            ket?.setText(R.string.sudah_bukti)
                            bayarkamar!!.setText(R.string.kirim_lagi)
                            DownLoadImageTask(strukbayar!!).execute(pembayaran?.struk)
                            bayarkamar!!.setOnClickListener {
                                UploadImage()
                            }
                        } else if (pembayaran?.status.equals("Sudah Bayar")) {
                            finish()
                        } else {
                            bayarkamar!!.setOnClickListener {
                                UploadImage()
                            }
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun setActionBarTitle(title: String) {
        if (supportActionBar != null) {
            supportActionBar?.setTitle(title)
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this@BayarActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
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

    fun GetFileExtension(uri: Uri?): String? {
        val contentResolver: ContentResolver = contentResolver
        val mimeTypeMap: MimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(uri?.let { contentResolver.getType(it) })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Image_Request_Code && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            FilePathUri = data.data
            try {
                val bitmap: Bitmap =
                    MediaStore.Images.Media.getBitmap(contentResolver, FilePathUri)
                val x: Int = bitmap.width
                val y: Int = bitmap.height
                strukbayar!!.setImageBitmap(Bitmap.createScaledBitmap(bitmap, x, y, false))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private inner class DownLoadImageTask(var imageView: ImageView) :
        AsyncTask<String?, Void?, Bitmap?>() {
        override fun doInBackground(vararg urls: String?): Bitmap? {
            val urlOfImage = urls[0]
            var logo: Bitmap? = null
            try {
                val `is` = URL(urlOfImage).openStream()
                logo = BitmapFactory.decodeStream(`is`)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return logo
        }

        override fun onPostExecute(result: Bitmap?) {
            imageView.setImageBitmap(result)
        }
    }

    fun UploadImage() {
        if (FilePathUri != null) {
            progressDialog?.setTitle("Data sedang dikirim...")
            progressDialog?.show()
            storageReference?.child(
                System.currentTimeMillis().toString() + "." + GetFileExtension(FilePathUri)
            )?.putFile(FilePathUri!!)
                ?.addOnSuccessListener{ taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener {uri->
                        pembayaran_ref!!.child(pembId).child("struk").setValue(uri.toString())
                        Toast.makeText(
                            applicationContext,
                            "Data berhasil dikirim ",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    progressDialog!!.dismiss()
                }?.addOnFailureListener{ex->
                    Toast.makeText(this,"Terjadi kesalahan, silahkan ulangi",Toast.LENGTH_LONG).show()
                    progressDialog!!.dismiss()
                }
        } else {
            Snackbar.make(
                findViewById<View>(R.id.dibayar),
                "Tolong periksa kembali data dan gambar",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }
}