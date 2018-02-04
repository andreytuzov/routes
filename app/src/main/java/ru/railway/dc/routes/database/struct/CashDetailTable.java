package ru.railway.dc.routes.database.struct;

/**
 * Created by SQL on 04.01.2017.
 */

public class CashDetailTable {

    // Таблица для хранения кэша
    public static final String TABLE_CASH_DETAIL = "tableCashDetail";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_B_STATION = "bStation";
    public static final String COLUMN_E_STATION = "eStation";
    public static final String COLUMN_B_ENTER_STATION = "bEnterStation";
    public static final String COLUMN_E_ENTER_STATION = "eEnterStation";

    public static final String COLUMN_B_DATETIME = "bDatetime";
    public static final String COLUMN_E_DATETIME = "eDatetime";
    public static final String COLUMN_DATE = "date";

    public static final String COLUMN_NUMBER_TRAIN = "numberTrain";
    public static final String COLUMN_TYPE_TRAIN = "typeTrain";

    public static final String SQL_DB_TABLE_CASH_DETAIL_CREATE =
            "create table " + TABLE_CASH_DETAIL + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_B_STATION + " text, " +
                    COLUMN_E_STATION + " text, " +
                    COLUMN_B_ENTER_STATION + " text, " +
                    COLUMN_E_ENTER_STATION + " text, " +
                    COLUMN_DATE + " text, " +
                    COLUMN_B_DATETIME + " integer, " +
                    COLUMN_E_DATETIME + " integer, " +
                    COLUMN_NUMBER_TRAIN + " text, " +
                    COLUMN_TYPE_TRAIN + " text " +
                    ");";
}
