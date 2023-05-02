package com.jm.blogitz.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.jm.blogitz.models.Blog;

import java.util.UUID;

public class BlogCursorWrapper extends CursorWrapper {
    public BlogCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Blog getBlog() {
        String uuidString = getString(getColumnIndex(BlogitzDbSchema.BlogTable.Cols.UUID));
        String title = getString(getColumnIndex(BlogitzDbSchema.BlogTable.Cols.TITLE));
        String body = getString(getColumnIndex(BlogitzDbSchema.BlogTable.Cols.BODY));

        Blog blog = new Blog(UUID.fromString(uuidString), title, body);
        return blog;
    }
}
