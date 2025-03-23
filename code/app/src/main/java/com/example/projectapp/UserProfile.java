// -----------------------------------------------------------------------------
// File: UserProfile.java
// -----------------------------------------------------------------------------
// This file defines the UserProfile class, a model class in the ProjectApp that
// represents a user's profile. It stores the user's username, name, and mood history.
// The class is part of the Model-View-Controller (MVC) pattern, acting as the model
// for user-related data.
//
// Design Pattern: MVC (Model)
// Outstanding Issues:
// N/A
// -----------------------------------------------------------------------------

package com.example.projectapp;

import java.io.Serializable;

/**
 * A class to model a user's profile.
 */
public class UserProfile implements Serializable {

    private MoodHistory history;
    private String username;
    private String name; // the user's actual name
    private String uid;

    /**
     * Constructs a new UserProfile with the specified Firebase UID, username, and name.
     *
     * @param uid      The user's Firebase UID.
     * @param username The user's chosen username.
     * @param name     The user's actual name.
     * @throws IllegalArgumentException if the username is not available.
     */
    public UserProfile(String uid, String username, String name) {
        this.uid = uid;
        this.username = username;
        this.name = name;
        this.history = new MoodHistory();
    }

    /**
     * Public no-argument constructor needed for Firestore deserialization.
     */
    public UserProfile() {
        // needed by Firestore
    }

    /**
     * Sets the user's mood history.
     *
     * @param history The MoodHistory to set.
     */
    public void setHistory(MoodHistory history) {
        this.history = history;
    }

    /**
     * Returns the user's mood history.
     *
     * @return The MoodHistory associated with this user.
     */
    public MoodHistory getHistory() {
        return history;
    }

    /**
     * Returns the user's username.
     *
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the user's actual name.
     *
     * @return The user's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the user's Firebase UID.
     *
     * @return The UID.
     */
    public String getUID() {
        return uid;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setName(String name) {
        this.name = name;
    }

}
