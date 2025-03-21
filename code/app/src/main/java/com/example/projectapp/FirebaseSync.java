package com.example.projectapp;

import android.util.Log;
import androidx.annotation.NonNull;
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

    public static FirebaseSync getInstance() {
        if (firebaseSync == null) {
            firebaseSync = new FirebaseSync();
        }
        return firebaseSync;
    }

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
     * Overwrites the Firestore doc for the current user with 'profile'.
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
     * Appends a new MoodEvent to the user's existing history and writes to Firestore.
     */
    public void addEventToProfile(UserProfile profile, MoodEvent moodEvent) {
        Log.d("FirebaseSync", "Before adding: " + profile.getHistory().getEvents().size() + " events");
        profile.getHistory().addEvent(moodEvent);
        Log.d("FirebaseSync", "After adding: " + profile.getHistory().getEvents().size() + " events");
        storeUserData(profile);
    }

    /**
     * Fetches the current user's profile from Firestore.
     * If doc is missing, calls onFailure with "No such document exists".
     */
    public void fetchUserProfileObject(UserProfileCallback callback) {
        FirebaseUser current = mAuth.getCurrentUser();
        if (current == null) {
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
