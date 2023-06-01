package com.jm.blogitz.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Insets;
import android.graphics.Rect;
import android.view.WindowInsets;
import android.view.WindowMetrics;

/**
 * Utilities to help with pictures in the Activities.
 * <p>
 * Code was sourced from the workshops.
 */
public class PictureUtils {
    /**
     * Get the scaled bitmap of a given file.
     * @param path Path of the image to scale.
     * @param destWidth Desired image width.
     * @param destHeight Desired image height.
     * @return The scaled bitmap of the image.
     */
    private static Bitmap getScaledBitmap(String path, int destWidth, int destHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        // Allows the caller to ignore having to allocate memory.
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;
        int inSampleSize = 1;
        // If either the src image height or width is greater than the destination scale the image so it will fit.
        if (srcHeight > destHeight || srcWidth > destWidth) {
            float heightScale = srcHeight / destHeight;
            float widthScale = srcWidth / destWidth;
            // Set to the larger value of the two.
            inSampleSize = Math.round(Math.max(heightScale, widthScale));
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * Get the scaled bitmap for a given path.
     * @param path Path to the image.
     * @param activity Current screen/view of the UI.
     * @return The scaled bitmap.
     */
    public static Bitmap getScaledBitmap(String path, Activity activity) {
        WindowMetrics windowMetrics = activity.getWindowManager().getCurrentWindowMetrics();
        Insets insets = windowMetrics.getWindowInsets()
                .getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars());
        // Workout the size of the current window based on the insets and bounds of the screen.
        int insetsWidth = insets.right + insets.left;
        int insetsHeight = insets.top + insets.bottom;
        Rect bounds = windowMetrics.getBounds();
        int width = bounds.width() - insetsWidth;
        int height = bounds.height() - insetsHeight;

        return getScaledBitmap(path, width, height);
    }
}
