package com.example.david.bcoffcampus;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by David on 4/9/16.
 */
public class MySQLiteHelper extends SQLiteOpenHelper {
    public static final String TABLE_NAME = "properties";
    public static final String COLUMN_ID = "_id";
    public static final String TITLE = "title";
    public static final String ADDRESS = "address";
    public static final String IMG = "img";
    public static final String PRICE = "price";

    private static final String DATABASE_NAME = "properties.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_NAME + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + TITLE
            + " VARCHAR(250), " + ADDRESS
            + " VARCHAR(250), " + IMG
            + " VARCHAR(250), " + PRICE
            + " VARCHAR(250)"
            + ");";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
