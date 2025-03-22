package com.example.projectapp;

import android.graphics.Movie;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;

public class ProfileProvider {

    private static ProfileProvider movieProvider;
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
                    profiles.add(p);
                    Log.d("FirestoreData", "Uid: " + p.getUID() + "Len of history" + p.getHistory().getEvents().size());

                }
                dataStatus.onDataUpdated();
            }
        });
    }

    public static ProfileProvider getInstance(FirebaseFirestore firestore) {
        if (movieProvider == null)
            movieProvider = new ProfileProvider(firestore);
        return movieProvider;
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

    public boolean usernameAvailable(String username){
        for (UserProfile prof: profiles){
            if (prof.getUsername().equals(username)){
                return false;
            }
        }
        return true;
    }



}
