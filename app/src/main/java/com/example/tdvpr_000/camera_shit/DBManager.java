package com.example.tdvpr_000.camera_shit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.DropBoxManager;
import android.widget.ArrayAdapter;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tdvpr_000 on 2/14/2017.
 */

public class DBManager extends SQLiteOpenHelper {
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE IF NOT EXISTS " + DBContract.FeedEntry.TABLE_NAME
            + " (" + DBContract.FeedEntry._ID + " INTEGER PRIMARY KEY,"
            + DBContract.FeedEntry.COLUMN_FILE + " TEXT,"
            + DBContract.FeedEntry.COLUMN_TAGS + " TEXT,"
            + DBContract.FeedEntry.COLUMN_DATES + " INT)";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS" + DBContract.FeedEntry.TABLE_NAME;

    // get tags from file location
    private static final String SQL_TAGS = "SELECT " + DBContract.FeedEntry.COLUMN_TAGS
            + " FROM " + DBContract.FeedEntry.TABLE_NAME
            + " WHERE " + DBContract.FeedEntry.COLUMN_FILE + " = ";

    private static final String SQL_TAG_COUNT = "SELECT " + DBContract.FeedEntry.COLUMN_TAGS + ", count("
            + DBContract.FeedEntry.COLUMN_TAGS + ") AS c" + " FROM " + DBContract.FeedEntry.TABLE_NAME + " GROUP BY "
            + DBContract.FeedEntry.COLUMN_TAGS;


    public static final int DATABASE_VERSION = 1;


    public List<String> tagsFrom(String previewFilePath) {
        SQLiteDatabase db = getReadableDatabase();
        List<String> result = new ArrayList<String>();
        Cursor cursor = db.rawQuery(SQL_TAGS + "'" + previewFilePath + "'", null);
        while(cursor.moveToNext()) {
            String tag = cursor.getString(
                    cursor.getColumnIndexOrThrow(DBContract.FeedEntry.COLUMN_TAGS));
            result.add(tag);
        }
        cursor.close();
        return result;
    }

    public DBManager(Context context) {
        super(context, DBContract.FeedEntry.DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void insertTags(String previewFilePath, ArrayAdapter<String> adapter) {
        if (adapter.getCount() != 0) {
            SQLiteDatabase db = this.getWritableDatabase();

            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(DBContract.FeedEntry.COLUMN_FILE, previewFilePath);
            values.put(DBContract.FeedEntry.COLUMN_TAGS, adapter.getItem(0));
            values.put(DBContract.FeedEntry.COLUMN_DATES, System.currentTimeMillis());

            db.insert(DBContract.FeedEntry.TABLE_NAME, DBContract.FeedEntry.COLUMN_TAGS, values);

            // puts a row in the database for every tag.
            // null, or column name as second parameter says what to do if values did not have a column (ie, null or don't insert)
            for (int i = 1; i < adapter.getCount(); i++) {
                values.put(DBContract.FeedEntry.COLUMN_TAGS, adapter.getItem(i));
                db.insert(DBContract.FeedEntry.TABLE_NAME, null, values);
            }
        }

    }

    public Map<String, Integer> countTags() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(SQL_TAG_COUNT, null);
        if (cursor.getCount() == 0)
            return null;
        Map<String, Integer> result = new HashMap<String, Integer>();
        while(cursor.moveToNext()) {
            String tag = cursor.getString(
                    cursor.getColumnIndex(DBContract.FeedEntry.COLUMN_TAGS));
            Integer i = cursor.getInt(cursor.getColumnIndex("c"));
            result.put(tag, i);
        }
        cursor.close();
        return result;
    }
}
