package com.example.projectapp;

/**
 * Model class for storing a follow request in Firestore.
 */
public class FollowRequest {
    private String fromUid;       // who is requesting
    private String fromUsername;  // optional: to display easily in Inbox
    private String status;        // "pending", "accepted", or "declined"

    // Required for Firestore
    public FollowRequest() { }

    public FollowRequest(String fromUid, String fromUsername, String status) {
        this.fromUid = fromUid;
        this.fromUsername = fromUsername;
        this.status = status;
    }

    public String getFromUid() {
        return fromUid;
    }

    public void setFromUid(String fromUid) {
        this.fromUid = fromUid;
    }



    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
