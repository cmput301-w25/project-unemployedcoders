package com.example.projectapp;

import android.graphics.Movie;

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
        void onDataUpdated(ArrayList<UserProfile> profiles);
        void onError(String error);
    }

    public interface ProfileProviderCallback {
        void onProfilesLoaded();
        void onFailure(Exception e);
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
                    profiles.add(item.toObject(UserProfile.class));
                }
                dataStatus.onDataUpdated(profiles);
            }
        });
    }

    /*
    public void getUserProfiles(ProfileProviderCallback callback){


        userCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    profiles.clear();
                    for (DocumentSnapshot item: task.getResult()){
                        profiles.add(item.toObject(UserProfile.class));
                    }


                    callback.onProfilesLoaded(profiles);


                } else {
                    callback.onFailure(task.getException());
                }
            }
        });

    }*/

    public static ProfileProvider getInstance(FirebaseFirestore firestore) {
        if (movieProvider == null)
            movieProvider = new ProfileProvider(firestore);
        return movieProvider;
    }

    public ArrayList<UserProfile> getProfiles() {
        return profiles;
    }


}
