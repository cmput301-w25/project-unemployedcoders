// -----------------------------------------------------------------------------
// File: UserProfile.java
// -----------------------------------------------------------------------------
// This file defines the UserProfile class, a model class in the ProjectApp that
// represents a user's profile. It stores the user's username, name, password, and
// mood history. The class is part of the Model-View-Controller (MVC) pattern,
// acting as the model for user-related data.
//
// Design Pattern: MVC (Model)
// Outstanding Issues:
// N/A
// -----------------------------------------------------------------------------
package com.example.projectapp;

public class UserProfile {

    private MoodHistory history;
    private String username;
    private String name; // the user's actual name

    private String password; // do we need to encrypt this??

    /**
     * Constructor for the UserProfile class
     * @param username
     *      The user's chosen username
     * @param name
     *      The user's actual name
     * @param password
     *      The user's chosen password
     */
    public UserProfile(String username, String name, String password){
        if (usernameAvailable(username)){
            this.username = username;
        } else {
            throw new IllegalArgumentException("Username is not available");
        }

        this.name = name;
        this.password = password;
        this.history = new MoodHistory();
    }

    /**
     * Getter for the user's mood history
     * @return
     *      The user's mood history
     */
    public MoodHistory getHistory() {
        return history;
    }

    /**
     * Getter for the user's username
     * @return
     *      The user's username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Getter for the user's name
     * @return
     *      The user's name
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for the user's password
     * @return
     *      The user's password
     */
    public String getPassword() {
        return password;
    }


    /**
     * Static method that checks if a certain username is taken already or not
     * @param username
     *      The proposed username
     * @return
     *      true if it's available, false otherwise
     */
    public static boolean usernameAvailable(String username){
        /* TODO when we make our database */
        return true;
    }






}
