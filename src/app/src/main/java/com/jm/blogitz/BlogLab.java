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

/**
 * Handles the database within the application.
 * <p>
 * Majority of the code sourced from workshops in this class.
 */
public class BlogLab {
    /**
     * Shared instance of the class.
     */
    private static BlogLab blogLab;
    /**
     * Current state of the application.
     */
    private final Context context;
    /**
     * Instance of the applications database.
     */
    private static SQLiteDatabase database;

    /**
     * Construct an instance of the database when creating an instance of the class.
     * @param context Current state of the application.
     */
    private BlogLab(Context context) {
        this.context = context.getApplicationContext();
        database = new BlogBaseHelper(this.context).getWritableDatabase();
    }

    /**
     * Get the current instance of the database manager or create a new one.
     * @param context Current state of the application.
     * @return The database manager.
     */
    public static BlogLab get(Context context) {
        if (blogLab == null) {
            blogLab = new BlogLab(context);
        }

        return blogLab;
    }

    /**
     * Get all the blogs stored in the database.
     * @return The blogs.
     */
    public List<Blog> getBlogs() {
        List<Blog> blogs = new ArrayList<>();

        try (BlogCursorWrapper cursor = this.queryBlogs(null, null)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                blogs.add(cursor.getBlog());
                cursor.moveToNext();
            }
        }

        return blogs;
    }

    /**
     * Insert a new blog in the database.
     * @param blog Blog to insert.
     */
    public void addBlog(Blog blog) {
        ContentValues values = getContentValues(blog);
        database.insert(BlogitzDbSchema.BlogTable.TABLE_NAME, null, values);
    }

    /**
     * Get a blog from the database using it's id.
     * @param id Id of the blog to get.
     * @return The blog from the database or null if it can't be found.
     */
    public Blog getBlog(UUID id) {

        try (BlogCursorWrapper cursor = this.queryBlogs(
                BlogitzDbSchema.BlogTable.Cols.UUID + " =?",
                new String[]{id.toString()}
        )) {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getBlog();
        }
    }


    /**
     * Update an existing blog in the database.
     * @param blog The blog to update.
     */
    public void updateBlog(Blog blog) {
        String uuidString = blog.getId().toString();
        ContentValues values = getContentValues(blog);
        database.update(
                BlogitzDbSchema.BlogTable.TABLE_NAME,
                values,
                BlogitzDbSchema.BlogTable.Cols.UUID + " = ?",
                new String[] { uuidString }
        );
    }

    /**
     * Delete a given blog from the database.
     * @param blog The blog to delete.
     */
    public void deleteBlog(Blog blog) {
        String uuidString = blog.getId().toString();
        database.delete(
                BlogitzDbSchema.BlogTable.TABLE_NAME,
                BlogitzDbSchema.BlogTable.Cols.UUID + " = ?",
                new String[] { uuidString }
        );
    }

    /**
     * Get the photo file of a given blog.
     * @param blog The blog.
     * @return The photo file.
     */
    public File getPhotoFile(Blog blog) {
        // Get the directory of the file storage.
        File fileDir = this.context.getFilesDir();
        return new File(fileDir, blog.getPhotoFilename());
    }

    /**
     * Create a database query to find blogs in the database.
     * @param whereClause The search query.
     * @param whereArgs The args of the where clause.
     * @return The database as a cursor object.
     */
    private BlogCursorWrapper queryBlogs(String whereClause, String[] whereArgs) {
        Cursor cursor = database.query(
                BlogitzDbSchema.BlogTable.TABLE_NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
        return new BlogCursorWrapper(cursor);
    }

    /**
     * Get the values from a Blog entity for the database to store.
     * @param blog The blog.
     * @return The values from the blog.
     */
    private static ContentValues getContentValues(Blog blog) {
        ContentValues values = new ContentValues();
        values.put(BlogitzDbSchema.BlogTable.Cols.UUID, blog.getId().toString());
        values.put(BlogitzDbSchema.BlogTable.Cols.TITLE, blog.getTitle());
        values.put(BlogitzDbSchema.BlogTable.Cols.BODY, blog.getBody());
        return values;
    }
}
