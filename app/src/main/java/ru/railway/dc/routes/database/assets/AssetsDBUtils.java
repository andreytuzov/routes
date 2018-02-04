package ru.railway.dc.routes.database.assets;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ru.railway.dc.routes.database.AssetsDB;

/**
 * Created by SQL on 14.01.2017.
 */

public class AssetsDBUtils {

    private static final Logger logger = Logger.getLogger(AssetsDBUtils.class);

    // Проверка инициализации базы данных
    public static boolean isInitialized() {
        SQLiteDatabase checkDB = null;
        try {
            checkDB = SQLiteDatabase.openDatabase(AssetsDB.DB_PATH, null,
                    SQLiteDatabase.OPEN_READONLY);
        } catch (Exception e) {
            logger.debug("Ошибка поиска базы данных: " + e.getMessage());
        } finally {
            if (checkDB != null) {
                checkDB.close();
            }
        }
        return checkDB != null;
    }

    public static void copyDBFromAssets(Context context) {
        InputStream in = null;
        OutputStream out = null;

        try {
            // Получаем in поток
            in = new BufferedInputStream(context.getAssets()
                    .open(AssetsDB.DB_ASSETS_PATH), AssetsDB.DB_FILES_COPY_BUFFER_SIZE);
            // Создаем папку если ее нет
            File dir = new File(AssetsDB.DB_FOLDER);
            if (!dir.exists()) {
                dir.mkdir();
            }
            // Получаем out поток
            out = new BufferedOutputStream(new FileOutputStream(AssetsDB.DB_PATH),
                    AssetsDB.DB_FILES_COPY_BUFFER_SIZE);
            byte[] buffer = new byte[AssetsDB.DB_FILES_COPY_BUFFER_SIZE];
            // Выполняем копирование
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            out.flush();
            out.close();
            in.close();
        } catch (IOException ex) {
            logger.error("Ошибка переноса БД");
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
