package com.example.kosnow.Model

import java.io.Serializable

class Kamar : Serializable {
    var nomor: String? = null
    var lokasi: String? = null
    var fasilitas: String? = null
    var luas: String? = null
    var harga: String? = null
    var gambar: String? = null
    var tersedia: String? = null
    var key: String? = null

    constructor(
        nomor: String?,
        lokasi: String?,
        fasilitas: String?,
        luas: String?,
        harga: String?,
        gambar: String?,
        tersedia: String?
    ) {
        this.nomor = nomor
        this.lokasi = lokasi
        this.fasilitas = fasilitas
        this.luas = luas
        this.harga = harga
        this.gambar = gambar
        this.tersedia = tersedia
    }

    constructor() {}
}