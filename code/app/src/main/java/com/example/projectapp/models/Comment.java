// -----------------------------------------------------------------------------
// File: Comment.java
// -----------------------------------------------------------------------------
// This file models a comment for a mood event.
//
// Design Pattern: MVC (Model)
// Outstanding Issues:
//  N/A
// -----------------------------------------------------------------------------

package com.example.projectapp.models;

import java.io.Serializable;
import java.util.Date;

/**
 * This class models a comment for a mood event.
 */
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

    /**
     * Gets commenter uid
     * @return
     *      the commenter uid
     */
    public String getCommenterUid() {
        return commenterUid;
    }

    /**
     * Sets commenter uid
     * @param commenterUid
     *      the uid to set
     */
    public void setCommenterUid(String commenterUid) {
        this.commenterUid = commenterUid;
    }

    /**
     * Gets commenter username
     * @return
     *      the commenter username
     */
    public String getCommenterUsername() {
        return commenterUsername;
    }

    /**
     * Sets commenter username
     * @param commenterUsername
     *      commenter username to set
     */
    public void setCommenterUsername(String commenterUsername) {
        this.commenterUsername = commenterUsername;
    }

    /**
     * Gets the comment text
     * @return
     *      the comment text
     */
    public String getText() {
        return text;
    }

    /**
     * sets comment text
     * @param text
     *      text to set in comment
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * gets the timestamp
     * @return
     *      the timestamp of the comment
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp
     * @param timestamp
     *      timestamp to set
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}