package com.example.projectapp;

import java.io.Serializable;

public class UserProfile implements Serializable {

    private MoodHistory history;
    private String username;
    private String name; // the user's actual name
    private String uid;

    /**
     * Constructor for the UserProfile class
     * @param username
     *      The user's chosen username
     * @param name
     *      The user's actual name
     * @param uid
     *      The user's firebase uid
     */
    public UserProfile(String uid, String username, String name){
        this.uid = uid;

        if (usernameAvailable(username)){
            this.username = username;
        } else {
            throw new IllegalArgumentException("Username is not available");
        }

        this.name = name;
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
    public String getUID() {
        return uid;
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
