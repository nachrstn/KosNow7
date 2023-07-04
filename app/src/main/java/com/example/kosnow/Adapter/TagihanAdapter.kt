package com.example.kosnow.Adapter

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.kosnow.CekStrukActivity
import com.example.kosnow.Model.Kamar
import com.example.kosnow.Model.KamarIsi
import com.example.kosnow.Model.Pembayaran
import com.example.kosnow.Model.User
import com.example.kosnow.R
import com.google.firebase.database.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class TagihanAdapter constructor(
    private val mContext: Context?,
    list: List<Pembayaran>,
    list2: List<KamarIsi>
) : RecyclerView.Adapter<TagihanAdapter.ViewHolder?>() {
    private val listPembayaran: List<Pembayaran>
    private val listKamarIsi: List<KamarIsi>
    var reference: DatabaseReference? = null
    var dreference: DatabaseReference? = null
    var dreference2: DatabaseReference? = null
    var username: String? = null

    init {
        listPembayaran = list
        listKamarIsi = list2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.item_tagihan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pembayaran: Pembayaran = listPembayaran[holder.absoluteAdapterPosition]
        reference = FirebaseDatabase.getInstance().getReference("Users")
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (mContext == null) {
                    return
                }
                for (postSnapshot: DataSnapshot in dataSnapshot.children) {
                    val user: User? = postSnapshot.getValue(User::class.java)
                    if (user != null) {
                        if (user.id.equals(pembayaran.id_user)) {
                            holder.nama_penyewa.text = user.username
                            username = user.username
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        holder.lama_sewa.text = pembayaran.sisa_bayar
        val fucek: Long? = pembayaran.tgl_pembayaran?.toLong()
        holder.tgl.text = fucek?.let { getDate(it, "dd/MM/yyyy") }
        holder.tagih.text = pembayaran.harga
        if (pembayaran.status.equals("Sudah Bayar")) {
            holder.status.setTextColor(Color.GREEN)
        }
        holder.status.text = pembayaran.status
        dreference = FirebaseDatabase.getInstance().getReference("Kamar")
        dreference2 = FirebaseDatabase.getInstance().getReference("KamarIsi")
        dreference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (mContext == null) {
                    return
                }
                for (snapshot: DataSnapshot in dataSnapshot.children) {
                    val kamar: Kamar? = snapshot.getValue(Kamar::class.java)
                    kamar?.key = snapshot.key
                    if (kamar != null) {
                        if (kamar.key.equals(pembayaran.id_kamar)) {
                            holder.no_kamar.text = kamar.nomor
                            holder.lokasi.text = kamar.lokasi
                            dreference2!!.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    for (snapshot: DataSnapshot in dataSnapshot.children) {
                                        val kamarIsi: KamarIsi? =
                                            snapshot.getValue(KamarIsi::class.java)
                                        kamarIsi?.key = snapshot.key
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {}
                            })
                            holder.itemView.setOnClickListener {
                                val intent: Intent = Intent(mContext, CekStrukActivity::class.java)
                                intent.putExtra("id_kamar", kamar.key)
                                intent.putExtra("username", username)
                                intent.putExtra("nomor", kamar.nomor)
                                intent.putExtra("lokasi", kamar.lokasi)
                                intent.putExtra("fasilitas", kamar.fasilitas)
                                intent.putExtra("luas", kamar.luas)
                                intent.putExtra("harga", kamar.harga)
                                intent.putExtra("struk", pembayaran.struk)
                                intent.putExtra("lama", pembayaran.sisa_bayar)
                                intent.putExtra("tgl", pembayaran.tgl_pembayaran)
                                intent.putExtra("tgl_depan", pembayaran.bulan_depan)
                                intent.putExtra("id_user", pembayaran.id_user)
                                intent.putExtra("key", pembayaran.key)
                                // intent.putExtra("kamarisi_key", kamarIsi.getKey());
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                mContext.startActivity(intent)
                            }
                            holder.itemView.setOnLongClickListener {
                                val dialog: Dialog = Dialog(mContext)
                                dialog.setContentView(R.layout.edit_delete_view)
                                dialog.setTitle("Pilih Aksi")
                                dialog.show()
                                val editButton: Button =
                                    dialog.findViewById<Button>(R.id.bt_edit_data)
                                editButton.visibility = View.GONE
                                val delButton: Button =
                                    dialog.findViewById<Button>(R.id.bt_delete_data)
                                delButton.setOnClickListener {
                                    dialog.dismiss()
                                    deleteData(holder.absoluteAdapterPosition)
                                }
                                true
                            }
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun getItemCount(): Int {
        return listPembayaran.size
    }

    inner class ViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var nama_penyewa: TextView
        var no_kamar: TextView
        var lokasi: TextView
        var lama_sewa: TextView
        var tgl: TextView
        var tagih: TextView
        var status: TextView

        init {
            nama_penyewa = itemView.findViewById<TextView>(R.id.nama_penyewa)
            no_kamar = itemView.findViewById<TextView>(R.id.no_kamar)
            lokasi = itemView.findViewById<TextView>(R.id.lokasi)
            lama_sewa = itemView.findViewById<TextView>(R.id.lama_sewa)
            tgl = itemView.findViewById<TextView>(R.id.tgl)
            tagih = itemView.findViewById<TextView>(R.id.tagih)
            status = itemView.findViewById<TextView>(R.id.status)
            setCurrency(tagih)
        }
    }

    private fun setCurrency(harga: TextView) {
        harga.addTextChangedListener(object : TextWatcher {
            private var current: String = ""
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
                if (!(s.toString() == current)) {
                    harga.removeTextChangedListener(this)
                    val local: Locale = Locale("id", "id")
                    val replaceable: String = String.format(
                        "[Rp,.\\s]",
                        NumberFormat.getCurrencyInstance().currency
                            .getSymbol(local)
                    )
                    val cleanString: String = s.toString().replace(
                        replaceable.toRegex(),
                        ""
                    )
                    var parsed: Double
                    try {
                        parsed = cleanString.toDouble()
                    } catch (e: NumberFormatException) {
                        parsed = 0.00
                    }
                    val formatter: NumberFormat = NumberFormat
                        .getCurrencyInstance(local)
                    formatter.maximumFractionDigits = 0
                    formatter.isParseIntegerOnly = true
                    val formatted: String = formatter.format((parsed))
                    val replace: String = String.format(
                        "[Rp\\s]",
                        NumberFormat.getCurrencyInstance().currency
                            .getSymbol(local)
                    )
                    val clean: String = formatted.replace(replace.toRegex(), "")
                    current = formatted
                    harga.text = clean
                    harga.addTextChangedListener(this)
                }
            }
        })
    }

    private fun deleteData(position: Int) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(
            (mContext)!!
        ).setMessage("Do you want to delete this room?")
            .setPositiveButton(
                "Yes"
            ) { dialog, which ->
                val database: FirebaseDatabase = FirebaseDatabase.getInstance()
                val pembayaran: Pembayaran = listPembayaran.get(position)
                val myRef: DatabaseReference? =
                    pembayaran.key?.let { database.reference.child("Pembayaran").child(it) }
                if (myRef != null) {
                    myRef.removeValue()
                }
                val cekKamarIsi: DatabaseReference = database.reference
                cekKamarIsi.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.hasChild("Kamar_terisi")) {
                            val kamarIsi: KamarIsi = listKamarIsi.get(position)
                            val myRef2: DatabaseReference? =
                                kamarIsi.key?.let {
                                    database.reference.child("Kamar_terisi")
                                        .child(it)
                                }
                            if (myRef2 != null) {
                                myRef2.removeValue()
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
                //                        myRef2.child("id_pembayaran").setValue("");
                //                        myRef2.child("id_user").setValue("");
                //                        myRef2.child("sisa_waktu").setValue("");
                //                        myRef2.child("tgl_masuk").setValue("");
                dialog.dismiss()
            }.setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    dialog.dismiss()
                }
            })
        builder.show()
    }

    companion object {
        fun getDate(milliSeconds: Long, dateFormat: String?): String {
            // Create a DateFormatter object for displaying date in specified format.
            val formatter: SimpleDateFormat = SimpleDateFormat(dateFormat)

            // Create a calendar object that will convert the date and time value in milliseconds to date.
            val calendar: Calendar = Calendar.getInstance()
            calendar.timeInMillis = milliSeconds
            return formatter.format(calendar.time)
        }
    }
}