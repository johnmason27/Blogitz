package com.jm.blogitz.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BlogBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "blogBase.db";

    public BlogBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableSql = String.format("CREATE TABLE %s( _id integer PRIMARY KEY AUTOINCREMENT, %s, %s, %s)",
                BlogitzDbSchema.BlogTable.TABLE_NAME,
                BlogitzDbSchema.BlogTable.Cols.UUID,
                BlogitzDbSchema.BlogTable.Cols.TITLE,
                BlogitzDbSchema.BlogTable.Cols.BODY);
        db.execSQL(createTableSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }
}
