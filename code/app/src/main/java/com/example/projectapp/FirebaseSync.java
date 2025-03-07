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
 * Singleton class that manages db operations
 */
public class FirebaseSync {

    private static FirebaseSync firebaseSync;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public static FirebaseSync getInstance() {
        if (firebaseSync == null)
            firebaseSync = new FirebaseSync();
        return firebaseSync;
    }

    private FirebaseSync(){
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    // sets the current user's data to the one passed in
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

    public void addEventToProfile(UserProfile profile, MoodEvent moodEvent){
        profile.getHistory().addEvent(moodEvent);
        storeUserData(profile);
    }

    public void fetchUserProfileObject(UserProfileCallback callback){
        FirebaseUser current = mAuth.getCurrentUser();

        if (current != null){

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
