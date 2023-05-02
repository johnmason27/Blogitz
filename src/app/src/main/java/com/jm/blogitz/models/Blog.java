package com.jm.blogitz.models;

import java.util.UUID;

public class Blog {
    private UUID id;
    private String title;
    private String body;

    public Blog(UUID id, String title, String body) {
        this.id = id;
        this.title = title;
        this.body = body;
    }

    public Blog(String title, String body) {
        this.id = UUID.randomUUID();
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
        if (this.title == null || this.title.equals("")) {
            return "Empty Title";
        }
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        if (this.body == null || this.body.equals("")) {
            return "Empty Body";
        }
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getPhotoFilename() {
        return "IMG_" + this.getId().toString() + ".jpg";
    }
}
