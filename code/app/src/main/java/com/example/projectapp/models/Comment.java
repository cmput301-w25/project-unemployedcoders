package com.example.projectapp.models;

import java.io.Serializable;
import java.util.Date;

public class Comment implements Serializable {
    private String commenterUid;
    private String commenterUsername; // Optional but useful for display.
    private String text;
    private Date timestamp;

    // Required empty constructor for Firestore
    public Comment() {}

    public Comment(String commenterUid, String commenterUsername, String text, Date timestamp) {
        this.commenterUid = commenterUid;
        this.commenterUsername = commenterUsername;
        this.text = text;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getCommenterUid() {
        return commenterUid;
    }

    public void setCommenterUid(String commenterUid) {
        this.commenterUid = commenterUid;
    }

    public String getCommenterUsername() {
        return commenterUsername;
    }

    public void setCommenterUsername(String commenterUsername) {
        this.commenterUsername = commenterUsername;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
