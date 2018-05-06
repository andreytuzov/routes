package ru.railway.dc.routes.database.assets.search

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

class AssetsDB(val context: Context) {

    private var db: SQLiteDatabase? = null

    fun getStationCursor(pattern: String?): Cursor {
        var pattern = pattern
        val selection: String
        if (pattern != null) {
            pattern = if (pattern.length > 1)
                pattern.substring(0, 1).toUpperCase() + pattern.substring(1)
            else
                pattern.toUpperCase()
            selection = CountryView.COLUMN_NAME + " like '" + pattern + "%'"
        } else {
            selection = CountryView.COLUMN_FAVOURITE + " = '1'"
        }
        return db!!.query(CountryView.VIEW_COUNTRY, null, selection, null, null, null,
                CountryView.COLUMN_FAVOURITE + " desc ," + CountryView.COLUMN_NAME, null)
    }

    fun getRegionCursor(pattern: String?): Cursor {
        val selection: String = if (pattern == null) {
            "tableRegionCountry.isChoice = '1'"
        } else {
            " tableRegion.name like '$pattern%'"
        }
        val sql = " " +
                " select tableRegionCountry._id as _id, " +
                " tableRegion.name as region, " +
                " tableCountry.name as country " +
                " from tableRegionCountry, tableRegion, tableCountry " +
                " where tableRegionCountry.countryTableID = tableCountry._id " +
                " and tableRegionCountry.regionTableID = tableRegion._id " +
                " and " + selection +
                " order by tableRegionCountry.isChoice desc, country, region"
        return db!!.rawQuery(sql, null)
    }

    fun updateTableRegion(list: List<Long>) {
        db!!.beginTransaction()
        try {
            // Обновление таблицы с выбранными регионами
            val size = list.size
            val listID = StringBuilder()
            for (i in 0 until size - 1) {
                listID.append(list[i].toString() + ", ")
            }
            listID.append(list[size - 1])
            db!!.execSQL("update " + RegionCountryTable.TABLE_REGION_COUNTRY
                    + " set " + RegionCountryTable.COLUMN_IS_CHOICE + " = '1'"
                    + " where " + RegionCountryTable.COLUMN_ID + " in (" + listID + ")")
            db!!.execSQL("update " + RegionCountryTable.TABLE_REGION_COUNTRY
                    + " set " + RegionCountryTable.COLUMN_IS_CHOICE + " = '0'"
                    + " where " + RegionCountryTable.COLUMN_ID + " not in (" + listID + ")")
            // Обновляем представление
            db!!.execSQL("drop view " + CountryView.VIEW_COUNTRY)
            val sql = "create view " + CountryView.VIEW_COUNTRY + " as" +
                    " select tableStation._id," +
                    " tableStation.name as name," +
                    " tableRegion.name as region," +
                    " tableStation.isFavourite as favourite" +
                    " from tableStation, tableRegionCountry, tableRegion" +
                    " where tableStation.countryTableID = tableRegionCountry.countryTableID" +
                    " and tableStation.regionTableID = tableRegionCountry.regionTableID" +
                    " and tableRegionCountry.regionTableID = tableRegion._id" +
                    " and tableRegionCountry._id in (" + listID + ")"
            db!!.execSQL(sql)
            db!!.setTransactionSuccessful()
        } finally {
            db!!.endTransaction()
        }
    }

    fun setFavourite(_id: Int, favourite: Int) {
        val cv = ContentValues()
        cv.put(StationTable.COLUMN_IS_FAVOURITE, favourite)
        db!!.update(StationTable.TABLE_STATION, cv, StationTable.COLUMN_ID + " = '" + _id + "'", null)
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

        val logger: Logger = Logger.getLogger(AssetsDB::class.java)

        // Параметры базы данных
        private const val DB_NAME = "stations.db"
        const val DB_VERSION = 1

        fun configure(cnxt: Context) {
            // Copy table
            AssetsDBLoader(cnxt, DB_NAME).copyDBFromAssets()
        }
    }
}
