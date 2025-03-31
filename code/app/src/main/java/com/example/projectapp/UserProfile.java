package com.example.projectapp;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A class to model a user's profile.
 */
public class UserProfile implements Serializable {

    private MoodHistory history;
    private String username;
    private String name; // the user's actual name
    private String uid;
    private ArrayList<String> following = new ArrayList<>(); // List of UIDs this user follows
    private ArrayList<String> followers = new ArrayList<>(); // List of UIDs following this user
    private int followingCount = 0; // Optional: for display
    private int followersCount = 0; // Optional: for display

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

    // Added getters and setters for followers and following

    /**
     * Returns the list of UIDs this user is following.
     *
     * @return The list of followed user UIDs.
     */
    public ArrayList<String> getFollowing() {
        return following;
    }

    /**
     * Sets the list of UIDs this user is following and updates the count.
     *
     * @param following The list of followed user UIDs.
     */
    public void setFollowing(ArrayList<String> following) {
        this.following = following != null ? following : new ArrayList<>(); // Prevent null
        this.followingCount = this.following.size();
    }

    /**
     * Returns the list of UIDs following this user.
     *
     * @return The list of follower UIDs.
     */
    public ArrayList<String> getFollowers() {
        return followers;
    }

    /**
     * Sets the list of UIDs following this user and updates the count.
     *
     * @param followers The list of follower UIDs.
     */
    public void setFollowers(ArrayList<String> followers) {
        this.followers = followers != null ? followers : new ArrayList<>(); // Prevent null
        this.followersCount = this.followers.size();
    }

    /**
     * Returns the number of users this user is following.
     *
     * @return The following count.
     */
    public int getFollowingCount() {
        return followingCount;
    }

    /**
     * Sets the following count.
     *
     * @param followingCount The number of users followed.
     */
    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    /**
     * Returns the number of users following this user.
     *
     * @return The followers count.
     */
    public int getFollowersCount() {
        return followersCount;
    }

    /**
     * Sets the followers count.
     *
     * @param followersCount The number of followers.
     */
    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }
}