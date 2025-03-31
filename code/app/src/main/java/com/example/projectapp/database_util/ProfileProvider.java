// -----------------------------------------------------------------------------
// File: ProfileProvider.java
// -----------------------------------------------------------------------------
// This file provides a class to read and write from the firebase firestore
// database. It is mainly used for operations to do with the all users,
// not just the current user.
// It contains the interface DataStatus to provide a callback and to implement
// behavior that can be different for each class that implements it.
//
// Design Pattern: Data Access Object
// Outstanding Issues:
//  N/A
// -----------------------------------------------------------------------------

package com.example.projectapp.database_util;

import android.util.Log;

import com.example.projectapp.models.UserProfile;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * Provides user profiles from Firestore. Singleton pattern.
 */
public class ProfileProvider {

    private static ProfileProvider profileProvider;
    private final ArrayList<UserProfile> profiles;
    private final CollectionReference userCollection;

    private ProfileProvider(FirebaseFirestore firestore) {
        profiles = new ArrayList<>();
        userCollection = firestore.collection("users");
    }

    public interface DataStatus {
        void onDataUpdated();
        void onError(String error);
    }

    public void listenForUpdates(final DataStatus dataStatus) {
        userCollection.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                dataStatus.onError(error.getMessage());
                return;
            }
            profiles.clear();
            if (snapshot != null) {
                for (QueryDocumentSnapshot item : snapshot) {
                    UserProfile p = item.toObject(UserProfile.class);
                    // CRUCIAL: set the UID from the doc ID
                    p.setUID(item.getId());
                    profiles.add(p);
                }
                dataStatus.onDataUpdated();
            }
        });
    }

    public static ProfileProvider getInstance(FirebaseFirestore firestore) {
        if (profileProvider == null)
            profileProvider = new ProfileProvider(firestore);
        return profileProvider;
    }

    public ArrayList<UserProfile> getProfiles() {
        return profiles;
    }

    public UserProfile getProfileByUID(String uid){

        for (UserProfile prof: profiles){
            Log.d("Testing", "Trying " + prof.getUID());
            if (prof.getUID().equals(uid)){
                return prof;
            }
        }
        return null;
    }

    /**
     * Helper method to get a UserProfile by username.
     * Assumes usernames are unique.
     *
     * @param username The username to search for.
     * @return The matching UserProfile, or null if not found.
     */
    public UserProfile getProfileByUsername(String username) {
        for (UserProfile prof : profiles) {
            if (prof.getUsername() != null && prof.getUsername().equals(username)) {
                return prof;
            }
        }
        return null;
    }

    public boolean usernameAvailable(String username){
        for (UserProfile prof: profiles){
            if (prof.getUsername().equals(username)){
                return false;
            }
        }
        return true;
    }

    public static void setInstanceForTesting(FirebaseFirestore firestore) {
        profileProvider = new ProfileProvider(firestore);
    }



}
