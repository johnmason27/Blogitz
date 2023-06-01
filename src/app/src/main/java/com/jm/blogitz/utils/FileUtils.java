package com.jm.blogitz.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utilities to help with Files in the application.
 * <p>
 * Code from stack overflow post by vishwaxjit76 - <a href="https://stackoverflow.com/questions/2975197/convert-file-uri-to-file-in-android#:~:text=38-,Best%20Solution,-Create%20one%20simple">Link to post.</a>
 */
public class FileUtils {
    /**
     * Static property representing the end of a file.
     */
    private static final int EOF = -1;
    /**
     * Default buffer size to use when copying a file from an input stream to an output stream.
     */
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    /**
     * Extension method for File to return a file from a given uri.
     * @param context Current state of the application.
     * @param uri Uri of the File to retrieve.
     * @return The File.
     * @throws IOException Exception could be thrown when trying to find the file.
     */
    public static File from(Context context, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        // Get the filename from the uri given.
        String fileName = getFileName(context, uri);

        // Split the filename into the filename without extension and the extension.
        String[] splitName = splitFileName(fileName);

        // Create a temporary file using the filename and extension.
        File tempFile = File.createTempFile(splitName[0], splitName[1]);

        // Rename the temp file to our new filename.
        tempFile = rename(tempFile, fileName);
        // Delete the file once the VM is exited.
        tempFile.deleteOnExit();

        FileOutputStream out = null;
        // Create an output stream for the temp file.
        try {
            out = new FileOutputStream(tempFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Copy the input stream (original uri) to the new output file.
        if (inputStream != null) {
            copy(inputStream, out);
            inputStream.close();
        }

        if (out != null) {
            out.close();
        }

        return tempFile;
    }

    /**
     * Check whether a content:file exists using the content resolver.
     * @param uri Path to content file.
     * @param resolver Content resolver.
     * @return True is the file exists, False if it doesn't.
     */
    public static boolean exists(Uri uri, ContentResolver resolver) {
        try (Cursor cursor = resolver.query(uri, null, null, null, null)) {
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            cursor.moveToFirst();
            long size = cursor.getLong(sizeIndex);

            return size > 0;
        }
    }

    /**
     * Split the filename into it's filename without extension and extension.
     * @param fileName Filename to split.
     * @return Split filename.
     */
    private static String[] splitFileName(String fileName) {
        String name = fileName;
        String extension = "";
        int i = fileName.lastIndexOf(".");
        // If there isn't a value for ".".
        if (i != -1) {
            name = fileName.substring(0, i);
            extension = fileName.substring(i);
        }

        return new String[]{name, extension};
    }

    /**
     * Get the filename from the uri.
     * @param context Current state of the application.
     * @param uri Uri to file.
     * @return The filename.
     */
    private static String getFileName(Context context, Uri uri) {
        String result = null;
        // Accessing an Android content provider.
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    // Get the human friendly name from the cursor.
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index > 0) {
                        result = cursor.getString(index);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        // If the cursor is empty get the path from the uri
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf(File.separator);
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    /**
     * Rename the old file.
     * @param file Old file.
     * @param newName New filename.
     * @return The new file.
     */
    private static File rename(File file, String newName) {
        // Create a new file using the parent directory of the old file and new filename.
        File newFile = new File(file.getParent(), newName);
        // If the new file doesn't match the old file.
        if (!newFile.equals(file)) {
            // If the new file exists and the new file can be deleted, delete it.
            if (newFile.exists() && newFile.delete()) {
                Log.d("FileUtil", "Delete old " + newName + " file");
            }
            // Try to rename the old filename to the new filename.
            if (file.renameTo(newFile)) {
                Log.d("FileUtil", "Rename file to " + newName);
            }
        }
        return newFile;
    }

    /**
     * Copy the input stream into the output stream.
     * @param input Input stream.
     * @param output Output stream.
     * @return The number of bytes read from the input stream.
     * @throws IOException If an exception occurs during reading from the input stream.
     */
    private static long copy(InputStream input, OutputStream output) throws IOException {
        long count = 0;
        int n;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        while (EOF !=  (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}