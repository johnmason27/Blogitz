package com.jm.blogitz.models;

import android.app.Activity;
import android.widget.Toast;

import java.util.UUID;

/**
 * Blog entity.
 */
public class Blog {
    /**
     * Blog id.
     */
    private final UUID id;
    /**
     * Blog title.
     */
    private String title;
    /**
     * Blog body.
     */
    private String body;

    /**
     * Initialise a Blog entity.
     * @param id Blog id.
     * @param title Blog title.
     * @param body Blog body.
     */
    public Blog(UUID id, String title, String body) {
        this.id = id;
        this.title = title;
        this.body = body;
    }

    /**
     * Initialise a Blog entity without parameters.
     */
    public Blog() {
        this.id = UUID.randomUUID();
    }

    /**
     * Get the blog id.
     * @return The blog id.
     */
    public UUID getId() {
        return this.id;
    }

    /**
     * Get the blog title.
     * @return The blog title.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Set the blog title.
     * @param title The new blog title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get the blog body.
     * @return The blog body.
     */
    public String getBody() {
        return this.body;
    }

    /**
     * Set the blog body.
     * @param body The new blog body.
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Get the blog photo filename.
     * @return The blogs photo filename.
     */
    public String getPhotoFilename() {
        return "IMG_" + this.getId().toString() + ".jpg";
    }

    /**
     * Validate the blog checking whether it's title and body are valid. If either
     * aren't valid the blogs title or body will be defaulted respectively and a toast will
     * be displayed to alert the user of this.
     * @param activity Current screen/view of the UI.
     */
    public void validateBlog(Activity activity) {
        boolean isTitleEmpty = this.getTitle() == null || this.getTitle().equals("");
        boolean isBodyEmpty = this.getBody() == null || this.getBody().equals("");
        if (isTitleEmpty && isBodyEmpty) {
            Toast.makeText(activity, "Your Blog Title and Body shouldn't be empty. Setting to default values.", Toast.LENGTH_SHORT).show();
            this.setTitle("Empty title...");
            this.setBody("Empty body...");
        } else if (isTitleEmpty) {
            Toast.makeText(activity, "Your Blog Title shouldn't be empty. Setting to default value.", Toast.LENGTH_SHORT).show();
            this.setTitle("Empty title...");
        } else if (isBodyEmpty) {
            Toast.makeText(activity, "Your Blog Body shouldn't be empty. Setting to default value.", Toast.LENGTH_SHORT).show();
            this.setBody("Empty body...");
        }
    }
}
