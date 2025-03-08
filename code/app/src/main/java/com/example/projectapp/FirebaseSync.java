package com.example.projectapp;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Singleton class that manages db operations through firestore
 */
public class FirebaseSync {

    private static FirebaseSync firebaseSync;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    /**
     * gets an instance of FireBaseSync, since its a singleton class
     * @return
     *      an instance of FireBaseSync
     */
    public static FirebaseSync getInstance() {
        if (firebaseSync == null)
            firebaseSync = new FirebaseSync();
        return firebaseSync;
    }

    /**
     * Private constructor for FirebaseSync
     */
    private FirebaseSync(){
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * stores a UserProfile object into the current FireBase user's db
     * @param profile
     *      the UserProfile object to store
     */
    public void storeUserData(UserProfile profile){
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){
            String uid = user.getUid();
            db.collection("users")
                    .document(uid)
                    .set(profile).addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "User profile successfully saved!");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error saving user profile", e);  // Log error
                    });
        }

    }

    /**
     * adds a MoodEvent to a user's profile and stores it in the db
     * @param profile
     *      The profile to store the MoodEvent in
     * @param moodEvent
     *      The mood event to store
     */
    public void addEventToProfile(UserProfile profile, MoodEvent moodEvent){
        profile.getHistory().addEvent(moodEvent);
        storeUserData(profile);
    }

    /**
     * Gets the current firebase user's UserProfile object. This has to be handled in a callback because FireStore operations are not instantaneous.
     * @param callback
     *      The callback object, to be instantiated by a class that is going to use it
     */
    public void fetchUserProfileObject(UserProfileCallback callback){
        FirebaseUser current = mAuth.getCurrentUser();

        if (current != null){

            /*
            The following code was inspired by Google Firebase Firestore documentation "Get data with Cloud Firestore"
            Written by Google.
            Taken on 2025-03-06 by Luke Yaremko
             */

            DocumentReference ref = db.collection("users").document(current.getUid());

            ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        DocumentSnapshot doc = task.getResult();

                        if (doc.exists()){
                            UserProfile profile = doc.toObject(UserProfile.class);

                            if (profile != null){
                                callback.onUserProfileLoaded(profile);
                            } else {
                                callback.onFailure(new Exception("Error converting document to UserProfile"));
                            }

                        } else {
                            callback.onFailure(new Exception("No such document exists"));
                        }
                    } else {
                        callback.onFailure(task.getException());
                    }
                }
            });

        } else {
            callback.onFailure(new Exception("No user is currently signed in"));
        }

    }
}


