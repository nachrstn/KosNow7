package com.example.kosnow.Adapter

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kosnow.DetailActivity
import com.example.kosnow.Model.Kamar
import com.example.kosnow.Model.KamarIsi
import com.example.kosnow.Model.User
import com.example.kosnow.R
import com.example.kosnow.TambahKamarActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.text.NumberFormat
import java.util.*

class KamarListAdapter(private val mContext: Context?, list: List<Kamar>, list2: List<KamarIsi>) :
    RecyclerView.Adapter<KamarListAdapter.ViewHolder?>() {
    private val listKamar: List<Kamar>
    private val listKamarIsi: List<KamarIsi>
    var firebaseuser: FirebaseUser? = null
    var reference: DatabaseReference? = null
    var listener: FirebaseDataListener? = null

    init {
        listKamar = list
        listKamarIsi = list2
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): KamarListAdapter.ViewHolder {
        val view: View =
            LayoutInflater.from(mContext).inflate(R.layout.item_kamar, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val kamar: Kamar = listKamar[holder.absoluteAdapterPosition]
        holder.nomor.text = kamar.nomor
        holder.lokasi.text = kamar.lokasi
        holder.luas.text = kamar.luas
        holder.harga.text = kamar.harga
        if (kamar.gambar == null) {
            return
        }
        if (kamar.gambar == "default") {
            holder.gambar.setImageResource(R.mipmap.ic_launcher)
        } else {
            if (mContext != null) {
                Glide.with(mContext).load(kamar.gambar).into(holder.gambar)
            }
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, DetailActivity::class.java)
            intent.putExtra("id_kamar", kamar.key)
            intent.putExtra("gambar", kamar.gambar)
            intent.putExtra("nomor", kamar.nomor)
            intent.putExtra("lokasi", kamar.lokasi)
            intent.putExtra("fasilitas", kamar.fasilitas)
            intent.putExtra("luas", kamar.luas)
            intent.putExtra("harga", kamar.harga)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            mContext?.startActivity(intent)
        }
        firebaseuser = FirebaseAuth.getInstance().currentUser
        reference =
            firebaseuser?.let {
                FirebaseDatabase.getInstance().getReference("Users").child(it.uid)
            }
        reference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (mContext == null) {
                    return
                }

                val user: User? = dataSnapshot.getValue(User::class.java)
                if (user?.level.equals("admin")) {
                    holder.itemView.setOnLongClickListener {
                        val dialog = Dialog(mContext)
                        dialog.setContentView(R.layout.edit_delete_view)
                        dialog.setTitle("Pilih Aksi")
                        dialog.show()
                        val editButton = dialog.findViewById<Button>(R.id.bt_edit_data)
                        val delButton = dialog.findViewById<Button>(R.id.bt_delete_data)
                        editButton.setOnClickListener {
                            dialog.dismiss()
                            mContext.startActivity(
                                TambahKamarActivity.getActIntent(mContext as Activity?)
                                    .putExtra("data", listKamar[holder.absoluteAdapterPosition])
                            )
                        }
                        delButton.setOnClickListener {
                            dialog.dismiss()
                            deleteData(holder.absoluteAdapterPosition)
                        }
                        true
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun getItemCount(): Int {
        return listKamar.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var nomor: TextView
        var lokasi: TextView
        var luas: TextView
        var harga: TextView
        var gambar: ImageView

        init {
            nomor = itemView.findViewById<TextView>(R.id.no_kamar)
            lokasi = itemView.findViewById<TextView>(R.id.lokasi)
            luas = itemView.findViewById<TextView>(R.id.luas)
            harga = itemView.findViewById<TextView>(R.id.harga)
            setCurrency(harga)
            gambar = itemView.findViewById<ImageView>(R.id.gambar_kamar)
        }
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
                    val replaceable = NumberFormat.getCurrencyInstance().currency?.let {
                        String.format(
                            "[Rp,.\\s]",
                            it
                                .getSymbol(local)
                        )
                    }
                    val cleanString: String = s.toString().replace(
                        replaceable!!.toRegex(),
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
                    val replace = NumberFormat.getCurrencyInstance().currency?.let {
                        String.format(
                            "[Rp\\s]",
                            it
                                .getSymbol(local)
                        )
                    }
                    val clean = formatted.replace(replace!!.toRegex(), "")
                    current = formatted
                    harga.text = clean
                    harga.addTextChangedListener(this)
                }
            }
        })
    }

    private fun deleteData(position: Int) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(
            mContext!!
        ).setMessage("Do you want to delete this room?")
            .setPositiveButton("Yes", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
                    val kamar: Kamar = listKamar[position]
                    val myRef: DatabaseReference? =
                        kamar.key?.let { database.reference.child("Kamar").child(it) }
                    myRef?.removeValue()
                    val cekKamarIsi: DatabaseReference = database.reference
                    cekKamarIsi.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.hasChild("Kamar_terisi")) {
                                val kamarIsi: KamarIsi = listKamarIsi[position]
                                val myRef2: DatabaseReference? =
                                    kamarIsi.key?.let {
                                        database.reference.child("Kamar_terisi")
                                            .child(it)
                                    }
                                myRef2?.removeValue()
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
                    dialog.dismiss()
                }
            }).setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    dialog.dismiss()
                }
            })
        builder.show()
    }

    interface FirebaseDataListener {
        fun onDeleteData(kamar: Kamar?, position: Int)
    }
}