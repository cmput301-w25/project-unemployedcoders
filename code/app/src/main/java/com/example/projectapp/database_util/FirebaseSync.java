// -----------------------------------------------------------------------------
// File: FirebaseSync.java
// -----------------------------------------------------------------------------
// This file provides a class to read and write from the firebase firestore
// database. It is mainly used for operations to do with the current user.
// It collaborates with the interface UserProfileCallback to provide data
// about the current user
//
// Design Pattern: Data Access Object
// Outstanding Issues:
//  N/A
// -----------------------------------------------------------------------------

package com.example.projectapp.database_util;

import android.util.Log;
import androidx.annotation.NonNull;

import com.example.projectapp.models.MoodEvent;
import com.example.projectapp.models.UserProfile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Singleton class to manage Firebase Firestore interactions.
 */
public class FirebaseSync {

    private static FirebaseSync firebaseSync;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public interface DataStatus {
        void onDataUpdated();
        void onError(String error);
    }
    /**
     * gets an instance of FireBaseSync, since its a singleton class
     * @return
     *      an instance of FireBaseSync
     */

    public static FirebaseSync getInstance() {
        if (firebaseSync == null) {
            firebaseSync = new FirebaseSync();
        }
        return firebaseSync;
    }

    /**
     * Private constructor for FirebaseSync
     */

    private FirebaseSync() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    public void listenForUpdates(final DataStatus dataStatus) {
        CollectionReference profiles = db.collection("users");
        profiles.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                dataStatus.onError(error.getMessage());
                return;
            }
            if (snapshot != null) {
                dataStatus.onDataUpdated();
            }
        });
    }

    /**
     * stores a UserProfile object into the current FireBase user's db
     * @param profile
     *      the UserProfile object to store
     */
    public void storeUserData(UserProfile profile) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            db.collection("users")
                    .document(uid)
                    .set(profile)
                    .addOnSuccessListener(aVoid ->
                            Log.d("Firestore", "User profile saved! events="
                                    + profile.getHistory().getEvents().size()))
                    .addOnFailureListener(e ->
                            Log.e("Firestore", "Error saving user profile", e));
        }
    }

    /**
     * adds a MoodEvent to a user's profile and stores it in the db
     * @param profile
     *      The profile to store the MoodEvent in
     * @param moodEvent
     *      The mood event to store
     */
    public void addEventToProfile(UserProfile profile, MoodEvent moodEvent) {
        Log.d("FirebaseSync", "Before adding: " + profile.getHistory().getEvents().size() + " events");
        profile.getHistory().addEvent(moodEvent);
        Log.d("FirebaseSync", "After adding: " + profile.getHistory().getEvents().size() + " events");
        storeUserData(profile);
    }

    /**
     * Gets the current firebase user's UserProfile object. This has to be handled in a callback because FireStore operations are not instantaneous.
     * @param callback
     *      The callback object, to be instantiated by a class that is going to use it
     */
    public void fetchUserProfileObject(UserProfileCallback callback) {
        FirebaseUser current = mAuth.getCurrentUser();
        if (current == null) {
            /*
            The following code was inspired by Google Firebase Firestore documentation "Get data with Cloud Firestore"
            Written by Google.
            Taken on 2025-03-06 by Luke Yaremko
             */
            callback.onFailure(new Exception("No user signed in"));
            return;
        }

        DocumentReference ref = db.collection("users").document(current.getUid());
        ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful() || task.getResult() == null) {
                    callback.onFailure(task.getException());
                    return;
                }
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()) {
                    UserProfile profile = doc.toObject(UserProfile.class);
                    if (profile != null) {
                        callback.onUserProfileLoaded(profile);
                    } else {
                        callback.onFailure(new Exception("Error converting document to UserProfile"));
                    }
                } else {
                    callback.onFailure(new Exception("No such document exists"));
                }
            }
        });
    }
}
