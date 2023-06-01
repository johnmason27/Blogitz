package com.jm.blogitz.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.jm.blogitz.models.Blog;

import java.util.UUID;

/**
 * Blog wrapper for a database cursor object.
 */
public class BlogCursorWrapper extends CursorWrapper {
    /**
     * Initialise the base cursor.
     * @param cursor Base cursor.
     */
    public BlogCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    /**
     * Get the blog from the cursor object.
     * @return The blog from the database.
     */
    public Blog getBlog() {
        // getString will query the database cursor using a given column.
        String uuidString = getString(getColumnIndex(BlogitzDbSchema.BlogTable.Cols.UUID));
        String title = getString(getColumnIndex(BlogitzDbSchema.BlogTable.Cols.TITLE));
        String body = getString(getColumnIndex(BlogitzDbSchema.BlogTable.Cols.BODY));

        return new Blog(UUID.fromString(uuidString), title, body);
    }
}
