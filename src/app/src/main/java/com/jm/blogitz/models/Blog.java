package com.jm.blogitz.models;

import android.app.Activity;
import android.widget.Toast;

import java.util.UUID;

public class Blog {
    private final UUID id;
    private String title;
    private String body;

    public Blog(UUID id, String title, String body) {
        this.id = id;
        this.title = title;
        this.body = body;
    }

    public Blog() {
        this.id = UUID.randomUUID();
    }

    public UUID getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getPhotoFilename() {
        return "IMG_" + this.getId().toString() + ".jpg";
    }

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
