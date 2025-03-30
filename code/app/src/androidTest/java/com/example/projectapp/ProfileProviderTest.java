package com.example.projectapp;

import static org.junit.Assert.assertEquals;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.projectapp.database_util.ProfileProvider;
import com.example.projectapp.models.MoodEvent;
import com.example.projectapp.models.MoodHistory;
import com.example.projectapp.models.UserProfile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

public class ProfileProviderTest {

    private static FirebaseAuth mAuth;
    private static ProfileProvider provider;

    /*
    The following two methods were taken from Lab 07 of this course
    Taken on 2025-03-09 by Luke Yaremko
    */
    @BeforeClass
    public static void setup() throws InterruptedException {
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";

        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);

        mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword("uitest@email.com", "password").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mAuth.signInWithEmailAndPassword("uitest@email.com", "password");
            }
        });

        Thread.sleep(10000); // needs time to log in

    }

    @After
    public void tearDown() {
        String projectId = "projectapp-f6251";
        URL url = null;
        try {
            url = new URL("http://10.0.2.2:8080/emulator/v1/projects/" + projectId + "/databases/(default)/documents");
        } catch (MalformedURLException exception) {
            Log.e("URL Error", Objects.requireNonNull(exception.getMessage()));
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            int response = urlConnection.getResponseCode();
            Log.i("Response Code", "Response Code: " + response);
        } catch (IOException exception) {
            Log.e("IO Error", Objects.requireNonNull(exception.getMessage()));
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }


    @Before
    public void seedDatabase() throws InterruptedException {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("users");

        String uid = mAuth.getCurrentUser().getUid();
        UserProfile profile = new UserProfile(uid, "TestUser", "Test Guy");
        MoodEvent testEvent = new MoodEvent("Happiness", "Happy guy", null, null, profile.getUID());
        profile.getHistory().addEvent(testEvent);

        usersRef.document(uid).set(profile);


        provider = ProfileProvider.getInstance(db);
        provider.listenForUpdates(new ProfileProvider.DataStatus() {
            @Override
            public void onDataUpdated() {
                // nothing
            }

            @Override
            public void onError(String error) {
                // nothing
            }
        });

        Thread.sleep(10000);
    }

    @Test
    public void testGetProfiles(){
        ArrayList<UserProfile> profs = provider.getProfiles();
        assertEquals(1, profs.size());

        UserProfile profile = profs.get(0);
        assertEquals("TestUser", profile.getUsername());
        assertEquals("Test Guy", profile.getName());

        assertEquals(1, profile.getHistory().getEvents().size());
    }

    @Test
    public void testGetProfilesHistory(){
        ArrayList<UserProfile> profs = provider.getProfiles();
        UserProfile profile = profs.get(0);
        MoodHistory history = profile.getHistory();

        assertEquals(1, history.getEvents().size());
        assertEquals("Happiness", history.getEvents().get(0).getEmotionalState());
        assertEquals("Happy guy", history.getEvents().get(0).getReason());

    }

    @Test
    public void testGetProfileByUid(){
        UserProfile profile = provider.getProfileByUID(mAuth.getCurrentUser().getUid());
        assertEquals("TestUser", profile.getUsername());
        assertEquals("Test Guy", profile.getName());
    }

    @Test
    public void testGetProfileByUIDHistory(){
        UserProfile profile = provider.getProfileByUID(mAuth.getCurrentUser().getUid());

        MoodHistory history = profile.getHistory();

        assertEquals(1, history.getEvents().size());
        assertEquals("Happiness", history.getEvents().get(0).getEmotionalState());
        assertEquals("Happy guy", history.getEvents().get(0).getReason());


    }



}
