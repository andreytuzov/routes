package ru.railway.dc.routes.database.assets

import android.content.Context
import android.database.sqlite.SQLiteDatabase

import org.apache.log4j.Logger

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class AssetsDBLoader(val context: Context, dbName: String) {

    private val dbFolder = "/data/data/" +
            context.packageName + "/databases/";
    private val dbPath = dbFolder + dbName
    private val dbAssetsPath = "db/$dbName"

    private val logger = Logger.getLogger(AssetsDBLoader::class.java)

    // Проверка инициализации базы данных
    private fun isInitialized(): Boolean {
        var checkDB: SQLiteDatabase? = null
        try {
            checkDB = SQLiteDatabase.openDatabase(dbPath, null,
                    SQLiteDatabase.OPEN_READONLY)
        } catch (e: Exception) {
            logger.debug("Database was not found: " + e.message)
        } finally {
            if (checkDB != null) {
                checkDB.close()
            }
        }
        return checkDB != null
    }

    fun copyDBFromAssets() {
        if (isInitialized()) {
            logger.debug("database exists")
            return
        }
        var `in`: InputStream? = null
        var out: OutputStream? = null

        try {
            // Get in-thread
            `in` = BufferedInputStream(context.assets
                    .open(dbAssetsPath), DB_FILES_COPY_BUFFER_SIZE)
            // Создаем папку если ее нет
            val dir = File(dbFolder)
            if (!dir.exists()) {
                dir.mkdir()
            }
            // Get out-thread
            out = BufferedOutputStream(FileOutputStream(dbPath),
                    DB_FILES_COPY_BUFFER_SIZE)
            val buffer = ByteArray(DB_FILES_COPY_BUFFER_SIZE)

            // Start copy
            do {
                val length = `in`.read(buffer)
                if (length > 0)
                    out.write(buffer, 0, length)
            } while (length > 0)
            out.flush()
            out.close()
            `in`.close()
            logger.debug("Database was copied")
        } catch (ex: IOException) {
            logger.error("Error move database")
        } finally {
            try {
                if (`in` != null) {
                    `in`.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            try {
                if (out != null) {
                    out.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val DB_FILES_COPY_BUFFER_SIZE = 8192
    }
}
