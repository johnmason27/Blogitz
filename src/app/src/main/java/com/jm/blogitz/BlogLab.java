package com.jm.blogitz;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jm.blogitz.database.BlogBaseHelper;
import com.jm.blogitz.database.BlogCursorWrapper;
import com.jm.blogitz.database.BlogitzDbSchema;
import com.jm.blogitz.models.Blog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BlogLab {
    private static BlogLab blogLab;
    private Context context;
    private static SQLiteDatabase database;

    private BlogLab(Context context) {
        this.context = context.getApplicationContext();
        this.database = new BlogBaseHelper(this.context).getWritableDatabase();
    }

    public static BlogLab get(Context context) {
        if (blogLab == null) {
            blogLab = new BlogLab(context);
        }

        return blogLab;
    }

    public List<Blog> getBlogs() {
        List<Blog> blogs = new ArrayList<>();
        BlogCursorWrapper cursor = this.queryBlogs(null, null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                blogs.add(cursor.getBlog());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return blogs;
    }

    public void addBlog(Blog blog) {
        ContentValues values = this.getContentValues(blog);
        this.database.insert(BlogitzDbSchema.BlogTable.TABLE_NAME, null, values);
    }

    public Blog getBlog(UUID id) {
        BlogCursorWrapper cursor = this.queryBlogs(
                BlogitzDbSchema.BlogTable.Cols.UUID + " =?",
                new String[] { id.toString() }
        );

        try {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getBlog();
        } finally {
            cursor.close();
        }
    }

    public void updateBlog(Blog blog) {
        String uuidString = blog.getId().toString();
        ContentValues values = this.getContentValues(blog);
        this.database.update(
                BlogitzDbSchema.BlogTable.TABLE_NAME,
                values,
                BlogitzDbSchema.BlogTable.Cols.UUID + " = ?",
                new String[] { uuidString }
        );
    }

    public void deleteBlog(Blog blog) {
        String uuidString = blog.getId().toString();
        this.database.delete(
                BlogitzDbSchema.BlogTable.TABLE_NAME,
                BlogitzDbSchema.BlogTable.Cols.UUID + " = ?",
                new String[] { uuidString }
        );
    }

    public File getPhotoFile(Blog blog) {
        File fileDir = this.context.getFilesDir();
        return new File(fileDir, blog.getPhotoFilename());
    }

    private BlogCursorWrapper queryBlogs(String whereClause, String[] whereArgs) {
        Cursor cursor = this.database.query(
                BlogitzDbSchema.BlogTable.TABLE_NAME,
                null, // Columns - null selects all columns
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                null // orderBy
        );
        return new BlogCursorWrapper(cursor);
    }

    private static ContentValues getContentValues(Blog blog) {
        ContentValues values = new ContentValues();
        values.put(BlogitzDbSchema.BlogTable.Cols.UUID, blog.getId().toString());
        values.put(BlogitzDbSchema.BlogTable.Cols.TITLE, blog.getTitle());
        values.put(BlogitzDbSchema.BlogTable.Cols.BODY, blog.getBody());
        return values;
    }
}