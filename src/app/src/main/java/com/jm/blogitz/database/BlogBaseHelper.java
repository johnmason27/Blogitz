package com.jm.blogitz.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite helper to create and update the application database.
 */
public class BlogBaseHelper extends SQLiteOpenHelper {
    /**
     * Database version.
     */
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "blogBase.db";

    /**
     * Construct a database helper with the classes database name and database version.
     * @param context Current state of the application.
     */
    public BlogBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    /**
     * Create the blogs table.
     * @param db blog database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableSql = String.format("CREATE TABLE %s( _id integer PRIMARY KEY AUTOINCREMENT, %s, %s, %s)",
                BlogitzDbSchema.BlogTable.TABLE_NAME,
                BlogitzDbSchema.BlogTable.Cols.UUID,
                BlogitzDbSchema.BlogTable.Cols.TITLE,
                BlogitzDbSchema.BlogTable.Cols.BODY);
        db.execSQL(createTableSql);
    }

    /**
     * Upgrade the blogs database.
     * @param db blog database.
     * @param oldVersion old version number.
     * @param newVersion new version number.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }
}
