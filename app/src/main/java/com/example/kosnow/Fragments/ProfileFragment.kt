package com.example.kosnow.Fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.kosnow.*
import com.example.kosnow.Model.Pembayaran
import com.example.kosnow.Model.User
import com.example.kosnow.R
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import de.hdodenhof.circleimageview.CircleImageView

class ProfileFragment : Fragment() {
    var image_profile: CircleImageView? = null
    var username: TextView? = null
    var adm: TextView? = null
    var logout: CardView? = null
    var tagihan: CardView? = null
    var tambahadm: CardView? = null
    var editprofile: CardView? = null
    var kontak_adm: CardView? = null
    var about: CardView? = null
    var reference: DatabaseReference? = null
    var pembayaran_ref: DatabaseReference? = null
    var fuser: FirebaseUser? = null
    var storageReference: StorageReference? = null
    private var imageUri: Uri? = null
//    private var uploadTask: StorageTask? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_profile, container, false)
        image_profile = view.findViewById(R.id.profile_image)
        logout = view.findViewById(R.id.keluar)
        tagihan = view.findViewById(R.id.tagihan)
        about = view.findViewById(R.id.about)
        tambahadm = view.findViewById(R.id.tambahadmin)
        kontak_adm = view.findViewById(R.id.kontak_adm)
        editprofile = view.findViewById(R.id.edit_profile)
        adm = view.findViewById(R.id.adm)
        username = view.findViewById(R.id.username)
        storageReference = FirebaseStorage.getInstance().getReference("uploads")
        fuser = FirebaseAuth.getInstance().getCurrentUser()
        reference = fuser?.uid?.let { FirebaseDatabase.getInstance().getReference("Users").child(it) }
        reference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user: User? = dataSnapshot.getValue(User::class.java)
                username?.setText(user?.username)
                adm?.setText("Profile " + user?.level)
                if (activity == null) {
                    return
                }
                if (user?.imageURL.equals("default")) {
                    image_profile?.setImageResource(R.mipmap.ic_launcher)
                } else {
                    Glide.with(activity!!).load(user?.imageURL).into(image_profile!!)
                }
                if (user?.level.equals("admin")) {
                    tagihan?.setVisibility(View.GONE)
                    kontak_adm?.setVisibility(View.GONE)
                } else if (user?.level.equals("user")) {
                    tambahadm?.setVisibility(View.GONE)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        image_profile?.setOnClickListener { openImage() }
        logout?.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(
                Intent(
                    activity,
                    LoginActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
        kontak_adm?.setOnClickListener {
            val installed = appInstaledOrNot("com.whatsapp")
            if (installed) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setData(Uri.parse("http://api.whatsapp.com/send?phone=" + "+6212345678" + "&text=Permisi admin"))
                startActivity(intent)
            } else {
                Toast.makeText(
                    activity,
                    "WhatsApp belum ter-install di perangkat anda",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        tambahadm?.setOnClickListener {
            startActivity(
                Intent(
                    context,
                    TambahAdminActivity::class.java
                )
            )
        }
        about?.setOnClickListener {
            startActivity(
                Intent(
                    context,
                    AboutActivity::class.java
                )
            )
        }
        editprofile?.setOnClickListener {
            startActivity(
                Intent(
                    context,
                    EditProfileActivity::class.java
                )
            )
        }
        tagihan?.setOnClickListener {
            pembayaran_ref = FirebaseDatabase.getInstance().getReference("Pembayaran")
            pembayaran_ref?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (postSnapshot in dataSnapshot.getChildren()) {
                        val pembayaran: Pembayaran? = postSnapshot.getValue(Pembayaran::class.java)
                        if (pembayaran?.id_user.equals(fuser?.uid)) {
                            if (pembayaran?.status.equals("Sudah Bayar")) {
                                if (pembayaran == null) {
                                    return
                                }
                                startActivity(Intent(activity, SudahBayarActivity::class.java))
                            } else if (pembayaran?.status.equals("Belum Bayar")) {
                                startActivity(Intent(activity, BayarActivity::class.java))
                            }
                        } else if (!pembayaran?.id_user.equals(fuser?.uid)) {
                            Toast.makeText(activity, "Tidak ada tagihan", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
        return view
    }

    private fun appInstaledOrNot(url: String): Boolean {
        val packageManager: PackageManager = activity!!.packageManager
        val app_installed: Boolean
        app_installed = try {
            packageManager.getPackageInfo(url, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
        return app_installed
    }

    private fun openImage() {
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(intent, IMAGE_REQUEST)
    }

    private fun getFileExtention(uri: Uri): String? {
        val contentResolver: ContentResolver = context!!.contentResolver
        val mimeTypeMap: MimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    private fun uploadImage() {
        val pd = ProgressDialog(context)
        pd.setMessage("Uploading")
        pd.show()
        if (imageUri != null) {
            val fileReference: StorageReference = storageReference!!.child(
                System.currentTimeMillis()
                    .toString() + "." + getFileExtention(imageUri!!)
            )
//            uploadTask = fileReference.putFile(imageUri!!)
//            uploadTask.continueWithTask(Continuation<Any?, Task<Uri>> { task ->
//                if (!task.isSuccessful) {
//                    throw task.exception!!
//                }
//                fileReference.getDownloadUrl()
//            }).addOnCompleteListener(object : OnCompleteListener<Uri?> {
//                override fun onComplete(task: Task<Uri?>) {
//                    if (task.isSuccessful) {
//                        val downloadUri = task.result
//                        val mUri = downloadUri.toString()
//                        reference = fuser?.uid?.let {
//                            FirebaseDatabase.getInstance().getReference("Users")
//                                .child(it)
//                        }
//                        val map = HashMap<String, Any>()
//                        map["imageURL"] = mUri
//                        reference!!.updateChildren(map)
//                        pd.dismiss()
//                    } else {
//                        Toast.makeText(context, "Failed!", Toast.LENGTH_SHORT).show()
//                        pd.dismiss()
//                    }
//                }
//            }).addOnFailureListener(object : OnFailureListener {
//                override fun onFailure(e: Exception) {
//                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
//                    pd.dismiss()
//                }
//            })
        } else {
            Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData()
//            if (uploadTask != null && uploadTask.isInProgress()) {
//                Toast.makeText(context, "Upload in progress..", Toast.LENGTH_SHORT).show()
//            } else {
//                uploadImage()
//            }
        }
    }

    companion object {
        private const val IMAGE_REQUEST = 1
    }
}