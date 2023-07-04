package com.example.kosnow.Model

import java.io.Serializable

class User : Serializable {
    var id: String? = null
    var username: String? = null
    var imageURL: String? = null
    var status: String? = null
    var level: String? = null
    var search: String? = null

    constructor(
        id: String?,
        username: String?,
        imageURL: String?,
        status: String?,
        level: String?,
        search: String?
    ) {
        this.id = id
        this.username = username
        this.imageURL = imageURL
        this.status = status
        this.level = level
        this.search = search
    }

    constructor() {}
}