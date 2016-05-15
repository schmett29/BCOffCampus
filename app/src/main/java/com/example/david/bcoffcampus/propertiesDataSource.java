package com.example.david.bcoffcampus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by David on 5/10/16.
 */
public class propertiesDataSource {
    // Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.TITLE, MySQLiteHelper.ADDRESS, MySQLiteHelper.IMG, MySQLiteHelper.PRICE};

    private static final String TAG = "DBDEMO";

    public propertiesDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Property createProperty(String title, String address, String imgURL, String price) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.TITLE, title);
        values.put(MySQLiteHelper.ADDRESS, address);
        values.put(MySQLiteHelper.IMG, imgURL);
        values.put(MySQLiteHelper.PRICE, price);
        long insertId = database.insert(MySQLiteHelper.TABLE_NAME, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_NAME,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        Property newProperty = cursorToComment(cursor);

        // Log the comment stored
        Log.d(TAG, "comment = " + cursorToComment(cursor).toString()
                + " insert ID = " + insertId);

        cursor.close();
        return newProperty;
    }

    public void deleteComment(Property property) {
        long id = property.getID();
        Log.d(TAG, "delete comment = " + id);
        System.out.println("Comment deleted with id: " + id);
        database.delete(MySQLiteHelper.TABLE_NAME, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }

    public void deleteAllComments() {
        System.out.println("Comment deleted all");
        Log.d(TAG, "delete all = ");
        database.delete(MySQLiteHelper.TABLE_NAME, null, null);
    }

    public void updateProperty(int id, String title, String address, String imgURL, String price){
        ContentValues newValues = new ContentValues();
        newValues.put(MySQLiteHelper.TITLE, title);
        newValues.put(MySQLiteHelper.ADDRESS, address);
        newValues.put(MySQLiteHelper.IMG, imgURL);
        newValues.put(MySQLiteHelper.PRICE, price);
        database.update(MySQLiteHelper.TABLE_NAME, newValues, MySQLiteHelper.COLUMN_ID + " = " + id, null);
    }

    public ArrayList<Property> getAllComments() {
        ArrayList<Property> theproperties = new ArrayList<Property>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_NAME,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Property property = cursorToComment(cursor);
            Log.d(TAG, "get comment = " + cursorToComment(cursor).toString());
            theproperties.add(property);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return theproperties;
    }

    private Property cursorToComment(Cursor cursor) {
        Property property = new Property("","", "","");
        property.setID(cursor.getInt(0));
        property.setTitle(cursor.getString(1));
        property.setAddress(cursor.getString(2));
        property.setImgURL(cursor.getString(3));
        property.setPrice(cursor.getString(4));
        return property;
    }
}
