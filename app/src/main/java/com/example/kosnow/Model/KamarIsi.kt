package com.example.kosnow.Model

import java.io.Serializable

class KamarIsi : Serializable {
    var id_kamar: String? = null
    var id_user: String? = null
    var id_pembayaran: String? = null
    var tgl_masuk: String? = null
    var sisa_waktu: String? = null
    var key: String? = null

    constructor(
        id_kamar: String?,
        id_user: String?,
        id_pembayaran: String?,
        tgl_masuk: String?,
        sisa_waktu: String?
    ) {
        this.id_kamar = id_kamar
        this.id_user = id_user
        this.id_pembayaran = id_pembayaran
        this.tgl_masuk = tgl_masuk
        this.sisa_waktu = sisa_waktu
    }

    constructor() {}
}