package ru.railway.dc.routes.database.assets.photos

import android.arch.persistence.room.Entity

@Entity
data class Country(
        val id: Int,
        val name: String,
        val listRegion: MutableList<Region> = mutableListOf()
)

data class Region(
        val id: Int,
        val name: String,
        val listStation: MutableList<Station> = mutableListOf()
)

data class Station(
        val id: Int,
        val name: String,
        val latitude: Float? = null,
        val longitude: Float? = null,
        val listImage: MutableList<Image> = mutableListOf()
)

data class Image(val url: String, val description: String?)