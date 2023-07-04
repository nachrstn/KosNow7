package com.example.kosnow

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
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.kosnow.Model.Kamar
import com.example.kosnow.Model.KamarIsi
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import java.net.URL
import java.text.NumberFormat
import java.util.*

class TambahKamarActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    var ac: CheckBox? = null
    var kipas: CheckBox? = null
    var lemari: CheckBox? = null
    var toilet: CheckBox? = null
    var nomor: EditText? = null
    var lebar: EditText? = null
    var panjang: EditText? = null
    var harga: EditText? = null
    var tambah: Button? = null
    var gambarkamar: ImageView? = null
    var fasilitas = ""
    private val title = "Data Kamar"
    var spin: Spinner? = null
    var KamarUploadId: String? = null
    var FilePathUri: Uri? = null
    var storageReference: StorageReference? = null
    var databaseReference: DatabaseReference? = null
    var isi_reference: DatabaseReference? = null
    var Image_Request_Code = 7
    var progressDialog: ProgressDialog? = null

    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_tambah_kamar)
        ac = findViewById(R.id.ac)
        kipas = findViewById<CheckBox>(R.id.kipas)
        lemari = findViewById<CheckBox>(R.id.lemari)
        toilet = findViewById<CheckBox>(R.id.toilet)
        nomor = findViewById(R.id.nomor)
        spin = findViewById<Spinner>(R.id.wilayah)
        lebar = findViewById(R.id.lebar)
        panjang = findViewById(R.id.panjang)
        harga = findViewById(R.id.harga)
        gambarkamar = findViewById<ImageView>(R.id.gambarkamar)
        tambah = findViewById<Button>(R.id.tambahkamar)
        val adapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
            this,
            R.array.wilayah_array, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spin?.adapter = adapter
        spin?.onItemSelectedListener = this
        setCurrency(harga)
        storageReference = FirebaseStorage.getInstance().getReference("Kamar")
        databaseReference = FirebaseDatabase.getInstance().getReference("Kamar")
        KamarUploadId = databaseReference!!.push().key
        progressDialog = ProgressDialog(this@TambahKamarActivity)
        val kamar: Kamar = intent.getSerializableExtra("data") as Kamar
        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar)
        setActionBarTitle(title)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        gambarkamar!!.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Image"), Image_Request_Code)
        }
        if (kamar != null) {
            nomor?.setText(kamar.nomor)
            harga?.setText(kamar.harga)
            val compareValue: String? = kamar.lokasi
            val spinnerPosition: Int = adapter.getPosition(compareValue)
            spin?.setSelection(spinnerPosition)
            if (kamar.luas == null) {
                return
            }
            val luasas: String = kamar.luas!!
            val luas = luasas.replace("Meter", "")
            val luases = luas.replace("x", "")
            lebar?.setText(luases.substring(0, 1))
            panjang?.setText(luases.substring(1, 2))
            DownLoadImageTask(gambarkamar!!).execute(kamar.gambar)
            tambah!!.setOnClickListener {
                UpdateData()
                startActivity(Intent(this@TambahKamarActivity, MainActivity::class.java))
            }
        } else {
            tambah!!.setOnClickListener {
                if (!isEmpty(nomor?.text.toString()) && !isEmpty(
                        harga?.text.toString()
                    ) && !isEmpty(lebar?.text.toString())
                    && !isEmpty(panjang?.text.toString())
                ) {
                    UploadImage()
                    Kamar_isi()
                } else {
                    Snackbar.make(
                        findViewById<View>(R.id.tambahkamar),
                        "Data tidak boleh kosong",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun isEmpty(s: String): Boolean {
        // Cek apakah ada fields yang kosong, sebelum disubmit
        return TextUtils.isEmpty(s)
    }

    private fun setActionBarTitle(title: String) {
        if (supportActionBar != null) {
            supportActionBar?.setTitle(title)
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this@TambahKamarActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    // Select Image method
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Image_Request_Code && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            FilePathUri = data.data
            try {
                val bitmap: Bitmap =
                    MediaStore.Images.Media.getBitmap(contentResolver, FilePathUri)
                val x: Int = bitmap.width
                val y: Int = bitmap.height
                gambarkamar!!.setImageBitmap(Bitmap.createScaledBitmap(bitmap, x, y, false))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun GetFileExtension(uri: Uri?): String? {
        val contentResolver: ContentResolver = contentResolver
        val mimeTypeMap: MimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(uri?.let { contentResolver.getType(it) })
    }

    fun UploadImage() {
        if (FilePathUri != null) {
            progressDialog?.setTitle("Data sedang ditambahkan...")
            progressDialog?.show()
            val storageReference2: StorageReference? = storageReference?.child(
                System.currentTimeMillis().toString() + "." + GetFileExtension(FilePathUri)
            )
//            storageReference2.putFile(FilePathUri!!)
//                .continueWithTask(Continuation<Any?, Task<Uri>> { task ->
//                    if (!task.isSuccessful) {
//                        throw task.exception!!
//                    }
//                    storageReference2.downloadUrl
//                }).addOnCompleteListener(object : OnCompleteListener<Uri?> {
//                override fun onComplete(task: Task<Uri?>) {
//                    if (ac?.isChecked == true) {
//                        fasilitas += ac?.text.toString() + ", "
//                    }
//                    if (kipas?.isChecked == true) {
//                        fasilitas += kipas?.text.toString() + ", "
//                    }
//                    if (lemari?.isChecked == true) {
//                        fasilitas += lemari?.text.toString() + ", "
//                    }
//                    if (toilet?.isChecked == true) {
//                        fasilitas += toilet?.text.toString()
//                    }
//                    val downloadUri = task.result
//                    fasilitas = fasilitas.replace(", $".toRegex(), "")
//                    val TempNomor: String = nomor?.text.toString().trim()
//                    val TempWilayah: String = spin?.selectedItem.toString().trim { it <= ' ' }
//                    val TempLuas: String = (lebar?.text.toString() + "x" + panjang?.text
//                        .toString()).toString() + "Meter".trim { it <= ' ' }
//                    val Harga: String = harga?.text.toString().trim()
//                    val TempHarga = Harga.replace("\\.".toRegex(), "")
//                    val TempFasilitas = fasilitas.trim { it <= ' ' }
//                    val Ketersediaan = "ya"
//                    progressDialog?.dismiss()
//                    Toast.makeText(
//                        applicationContext,
//                        "Data berhasil ditambahkan ",
//                        Toast.LENGTH_LONG
//                    ).show()
//                    val kamarUploadInfo = Kamar(
//                        TempNomor,
//                        TempWilayah,
//                        TempFasilitas,
//                        TempLuas,
//                        TempHarga,
//                        downloadUri.toString(),
//                        Ketersediaan
//                    )
//                    KamarUploadId?.let { databaseReference?.child(it)?.setValue(kamarUploadInfo) }
//                    startActivity(
//                        Intent(
//                            this@TambahKamarActivity,
//                            MainActivity::class.java
//                        ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
//                    )
//                }
//            })
        } else {
            Snackbar.make(
                findViewById<View>(R.id.tambahkamar),
                "(Tambah)Tolong periksa kembali data dan gambar",
                Snackbar.LENGTH_LONG
            ).show()
            //Toast.makeText(TambahKamarActivity.this, "(Tambah)Tolong periksa kembali field dan gambar", Toast.LENGTH_LONG).show();
        }
    }

    private fun setCurrency(harga: EditText?) {
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
                    harga.setText(clean)
                    harga.setSelection(clean.length)
                    harga.addTextChangedListener(this)
                }
            }
        })
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val selected: String = parent.getItemAtPosition(position).toString()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    //    private void updateBarang(Kamar kamar) {
    //        databaseReference.child("kamar") //akses parent index, ibaratnya seperti nama tabel
    //                .child(kamar.getKey()) //select barang berdasarkan key
    //                .setValue(kamar) //set value barang yang baru
    //                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
    //                    @Override
    //                    public void onSuccess(Void aVoid) {
    //                        Snackbar.make(findViewById(R.id.tambah), "Data berhasil diupdatekan", Snackbar.LENGTH_LONG).setAction("Oke", new View.OnClickListener() {
    //                            @Override
    //                            public void onClick(View view) {
    //                                finish();
    //                            }
    //                        }).show();
    //                    }
    //                });
    //    }
    private inner class DownLoadImageTask(var imageView: ImageView) :
        AsyncTask<String?, Void?, Bitmap?>() {
        /*
            doInBackground(Params... params)
                Override this method to perform a computation on a background thread.
         */
        override fun doInBackground(vararg urls: String?): Bitmap? {
            val urlOfImage = urls[0]
            var logo: Bitmap? = null
            try {
                val `is` = URL(urlOfImage).openStream()
                /*
                    decodeStream(InputStream is)
                        Decode an input stream into a bitmap.
                 */logo = BitmapFactory.decodeStream(`is`)
            } catch (e: Exception) { // Catch the download exception
                e.printStackTrace()
            }
            return logo
        }

        /*
            onPostExecute(Result result)
                Runs on the UI thread after doInBackground(Params...).
         */
        override fun onPostExecute(result: Bitmap?) {
            imageView.setImageBitmap(result)
        }
    }

    fun UpdateData() {
        if (FilePathUri != null) {
            progressDialog?.setTitle("Data sedang diupdate...")
            progressDialog?.show()
            val kamar: Kamar = intent.getSerializableExtra("data") as Kamar
            val storageReference2: StorageReference? = storageReference?.child(
                System.currentTimeMillis().toString() + "." + GetFileExtension(FilePathUri)
            )
//            storageReference2?.putFile(FilePathUri!!)
//                .continueWithTask(Continuation<Any?, Task<Uri>> { task ->
//                    if (!task.isSuccessful) {
//                        throw task.exception!!
//                    }
//                    storageReference2.downloadUrl
//                }).addOnCompleteListener(object : OnCompleteListener<Uri?> {
//                override fun onComplete(task: Task<Uri?>) {
//                    if (ac?.isChecked == true) {
//                        fasilitas += ac?.text.toString() + ", "
//                    }
//                    if (kipas?.isChecked == true) {
//                        fasilitas += kipas?.text.toString() + ", "
//                    }
//                    if (lemari?.isChecked == true) {
//                        fasilitas += lemari?.text.toString() + ", "
//                    }
//                    if (toilet?.isChecked == true) {
//                        fasilitas += toilet?.text.toString()
//                    }
//                    val downloadUri = task.result
//                    fasilitas = fasilitas.replace(", $".toRegex(), "")
//                    val TempNomor: String = nomor?.text.toString().trim()
//                    val TempWilayah: String = spin?.selectedItem.toString().trim { it <= ' ' }
//                    val TempLuas: String = (lebar?.text.toString() + "x" + panjang?.text
//                        .toString()).toString() + "Meter".trim { it <= ' ' }
//                    val Harga: String = harga?.text.toString().trim()
//                    val TempHarga = Harga.replace("\\.".toRegex(), "")
//                    val TempFasilitas = fasilitas.trim { it <= ' ' }
//                    val Ketersediaan = "ya"
//                    progressDialog?.dismiss()
//                    Toast.makeText(
//                        applicationContext,
//                        "Data berhasil diupdate ",
//                        Toast.LENGTH_LONG
//                    ).show()
//                    val KamarUploadId: String? = kamar.key
//                    val kamarUploadInfo = Kamar(
//                        TempNomor,
//                        TempWilayah,
//                        TempFasilitas,
//                        TempLuas,
//                        TempHarga,
//                        downloadUri.toString(),
//                        Ketersediaan
//                    )
//                    KamarUploadId?.let { databaseReference?.child(it)?.setValue(kamarUploadInfo) }
//                    startActivity(
//                        Intent(
//                            this@TambahKamarActivity,
//                            MainActivity::class.java
//                        ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
//                    )
//                }
//            })
        } else if (FilePathUri == null) {
            val kamar: Kamar = intent.getSerializableExtra("data") as Kamar
            if (ac?.isChecked == true) {
                fasilitas += ac?.text.toString() + ", "
            }
            if (kipas?.isChecked == true) {
                fasilitas += kipas?.text.toString() + ", "
            }
            if (lemari?.isChecked == true) {
                fasilitas += lemari?.text.toString() + ", "
            }
            if (toilet?.isChecked == true) {
                fasilitas += toilet?.text.toString()
            }
            val downloadUri: String? = kamar.gambar
            fasilitas = fasilitas.replace(", $".toRegex(), "")
            val TempNomor: String = nomor?.text.toString().trim()
            val TempWilayah: String = spin?.selectedItem.toString().trim { it <= ' ' }
            val TempLuas: String = (lebar?.text.toString() + "x" + panjang?.text
                .toString()).toString() + "Meter".trim { it <= ' ' }
            val Harga: String = harga?.text.toString().trim()
            val TempHarga = Harga.replace("\\.".toRegex(), "")
            val TempFasilitas = fasilitas.trim { it <= ' ' }
            val Ketersediaan = "ya"
            progressDialog?.dismiss()
            Toast.makeText(applicationContext, "Data berhasil diupdate ", Toast.LENGTH_LONG)
                .show()
            val KamarUploadId: String? = kamar.key
            val kamarUploadInfo = Kamar(
                TempNomor,
                TempWilayah,
                TempFasilitas,
                TempLuas,
                TempHarga,
                downloadUri,
                Ketersediaan
            )
            KamarUploadId?.let { databaseReference?.child(it)?.setValue(kamarUploadInfo) }
            startActivity(
                Intent(
                    this@TambahKamarActivity,
                    MainActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } else {
            Snackbar.make(
                findViewById<View>(R.id.tambahkamar),
                "(Update)Tolong periksa kembali data dan gambar",
                Snackbar.LENGTH_LONG
            ).show()
            //Toast.makeText(TambahKamarActivity.this, "(Update)Tolong periksa kembali field dan gambar", Toast.LENGTH_LONG).show();
        }
    }

    fun Kamar_isi() {
        isi_reference = FirebaseDatabase.getInstance().getReference("Kamar_terisi")
        val TempKamar = KamarUploadId
        val TempUser = ""
        val TempPemb = ""
        //        Calendar cal = Calendar.getInstance();
//        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
//        cal.setTime(formatter.parse(tgls.getText().toString()));
//        cal.add(Calendar.MONTH, 1);
//        String TempAwal = Long.toString(cal.getTimeInMillis()).trim();
        val TempAwal = ""
        val TempSisa = ""
        val MasukUploadId: String? = isi_reference!!.push().key
        val kamarIsi = KamarIsi(TempKamar, TempUser, TempPemb, TempAwal, TempSisa)
        if (MasukUploadId != null) {
            isi_reference!!.child(MasukUploadId).setValue(kamarIsi)
        }
    }

    companion object {
        fun getActIntent(activity: Activity?): Intent {
            // kode untuk pengambilan Intent
            return Intent(activity, TambahKamarActivity::class.java)
        }
    }
}