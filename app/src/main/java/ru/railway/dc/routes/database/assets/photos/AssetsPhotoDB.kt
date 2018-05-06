package ru.railway.dc.routes.database.assets.photos

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import org.apache.log4j.Logger
import ru.railway.dc.routes.database.assets.AssetsDBLoader

import ru.railway.dc.routes.database.assets.search.struct.CountryView
import ru.railway.dc.routes.database.assets.search.struct.RegionCountryTable
import ru.railway.dc.routes.database.assets.search.struct.StationTable

class AssetsPhotoDB(val context: Context) {

    private var db: SQLiteDatabase? = null

    fun getPhotoList(stationName: String): List<Image>? {
        val imageList = mutableListOf<Image>()
        val sql = "$SQL_SELECT_IMAGE '$stationName%'"
        val c = db!!.rawQuery(sql, null)
        if (c.moveToFirst()) {
            val urlColumn = c.getColumnIndex(Schemas.COLUMN_IMAGE_URL)
            val descriptionColumn = c.getColumnIndex(Schemas.COLUMN_IMAGE_DESCRIPTION)

            do {
                val url = PREFIX_IMAGE_URL + c.getString(urlColumn) + SUFFIX_IMAGE_URL
                val description = c.getString(descriptionColumn)
                imageList.add(Image(url, description))
            } while (c.moveToNext())
        }

        if (imageList.isEmpty())
            return null
        return imageList
    }

    fun getCoordinate(stationName: String): Station? {
        val selection = Schemas.COLUMN_STATION_NAME + " like '" + stationName + "%'"

        val c = db!!.query(Schemas.TABLE_STATION, arrayOf(Schemas.COLUMN_STATION_LATITUDE, Schemas.COLUMN_STATION_LONGITUDE, Schemas.COLUMN_STATION_NAME),
                selection, null, null, null, null, null)
        if (c.moveToFirst()) {
            val latitudeColumn = c.getColumnIndex(Schemas.COLUMN_STATION_LATITUDE)
            val longitudeColumn = c.getColumnIndex(Schemas.COLUMN_STATION_LONGITUDE)
            val stationNameColumn = c.getColumnIndex(Schemas.COLUMN_STATION_NAME)

            val latitude = c.getFloat(latitudeColumn)
            val longitude = c.getFloat(longitudeColumn)
            val stationName = c.getString(stationNameColumn)
            return Station(0, stationName, latitude, longitude)
        }
        return null
    }

    fun open() {
        val dbHelper = DBHelper(context, DB_NAME, null, DB_VERSION)
        db = dbHelper.writableDatabase
    }

    fun close() {
        db?.close()
    }

    class DBHelper(context: Context, name: String, factory: SQLiteDatabase.CursorFactory?, version: Int) : SQLiteOpenHelper(context, name, factory, version) {
        override fun onCreate(db: SQLiteDatabase) {}
        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    }

    companion object {

        val logger: Logger = Logger.getLogger(AssetsPhotoDB::class.java)
        const val SQL_SELECT_IMAGE = "select url, description from image, station where image.stationID = station._id and station.name like"
        const val PREFIX_IMAGE_URL = "https://railwayz.info/photolines/images/"
        const val SUFFIX_IMAGE_URL = "_s.jpg"

        // Параметры базы данных
        private const val DB_NAME = "photos.db"
        const val DB_VERSION = 1

        fun configure(cnxt: Context) {
            // Copy table
            AssetsDBLoader(cnxt, DB_NAME).copyDBFromAssets()
        }
    }
}
