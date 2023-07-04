package com.example.kosnow.Model

import java.io.Serializable

class Pembayaran : Serializable {
    var id_pembayaran: String? = null
    var id_user: String? = null
    var id_kamar: String? = null
    var status: String? = null
    var tgl_pembayaran: String? = null
    var bulan_depan: String? = null
    var harga: String? = null
    var sisa_bayar: String? = null
    var struk: String? = null
    var key: String? = null

    constructor(
        id_pembayaran: String?,
        id_user: String?,
        id_kamar: String?,
        status: String?,
        tgl_pembayaran: String?,
        bulan_depan: String?,
        harga: String?,
        struk: String?,
        sisa_bayar: String?
    ) {
        this.id_pembayaran = id_pembayaran
        this.id_user = id_user
        this.id_kamar = id_kamar
        this.status = status
        this.tgl_pembayaran = tgl_pembayaran
        this.bulan_depan = bulan_depan
        this.sisa_bayar = sisa_bayar
        this.struk = struk
        this.harga = harga
    }

    constructor() {}
}