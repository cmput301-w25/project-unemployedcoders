package com.example.projectapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;

import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;


import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


import android.util.Log;

import androidx.annotation.NonNull;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.projectapp.models.MoodEvent;
import com.example.projectapp.models.MoodHistory;
import com.example.projectapp.models.UserProfile;
import com.example.projectapp.views.activities.HomeActivity;
import com.example.projectapp.views.activities.MainActivity;
import com.example.projectapp.views.activities.MoodEventActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MoodEventActivityTest {
    private static FirebaseAuth mAuth;

    @Rule
    public ActivityScenarioRule<MoodEventActivity> activityRule =
            new ActivityScenarioRule<>(MoodEventActivity.class);

    /*
    The following two methods were taken from Lab 07 of this course
    Taken on 2025-03-09 by Luke Yaremko
     */
    @BeforeClass
    public static void setup() throws InterruptedException {
        Thread.sleep(10000);
        Intents.init();

        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";

        int portNumber = 8080;
        try {
            FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
        } catch (IllegalStateException e){
            // do nothing
        }
        mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword("uitest@email.com", "password").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mAuth.signInWithEmailAndPassword("uitest@email.com", "password");
            }
        });

        Thread.sleep(10000); // needs time to log in

    }

    @AfterClass
    public static void tearDown() {
        Intents.release();

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
        usersRef.document(uid).set(profile);
        Thread.sleep(3000);
    }


    @Test
    public void testAddValidEventInDatabase(){
        onView(withId(R.id.edit_reason)).perform(ViewActions.typeText("I'm so sad and lonely :(((((("));
        onView(withId(R.id.button_add_event)).perform(click());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("users");
        String uid = mAuth.getCurrentUser().getUid();
        usersRef.document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                UserProfile profile = documentSnapshot.toObject(UserProfile.class);
                MoodHistory history = profile.getHistory();
                assertEquals("Sadness", history.getEvents().get(0).getEmotionalState());
                assertEquals("I'm so sad and lonely :((((((", history.getEvents().get(0).getReason());
            }
        });

    }

    @Test
    public void testAddInvalidEventNotInDatabase(){
        onView(withId(R.id.edit_reason)).perform(ViewActions.typeText("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
        onView(withId(R.id.button_add_event)).perform(click());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("users");
        String uid = mAuth.getCurrentUser().getUid();
        usersRef.document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                UserProfile profile = documentSnapshot.toObject(UserProfile.class);
                MoodHistory history = profile.getHistory();
                assertEquals(0, history.getEvents().size());

            }
        });

    }



    /**
     * Test that when valid input is provided and "Add Event" is clicked, the app navigates to HomeActivity.
     */
    @Test
    public void addEventNavigatesToHomeActivity() throws InterruptedException {
        // Provide valid input in the editReason EditText (assuming valid input is <=20 chars, <=3 words)
        onView(withId(R.id.edit_reason)).perform(typeText("Good mood"));
        // For spinners, assume default selections are acceptable.

        // Click the Add Event button
        onView(withId(R.id.button_add_event)).perform(click());
        // Wait for asynchronous operations (e.g., Firebase calls) to complete
        Thread.sleep(3000);
        // Verify that HomeActivity is launched
        intended(hasComponent(HomeActivity.class.getName()));
    }

    /**
     * Test that the Upload Photo button is clickable.
     * (Since it triggers a dialog, we just verify that the button responds.)
     */
    @Test
    public void uploadPhotoButtonIsClickable() {
        onView(withId(R.id.button_upload_photo)).perform(click());
        // Further assertions can be added if the dialog shows with specific text, etc.
    }

    @Test
    public void AddLocationButtonIsClickable() {
        onView(withId(R.id.button_add_location)).perform(click());
        // Further assertions can be added if the dialog shows with specific text, etc.
    }

    @Test
    public void ViewMapButtonIsClickable() {
        onView(withId(R.id.button_view_map)).perform(click());
        // Further assertions can be added if the dialog shows with specific text, etc.
    }


    @Test
    public void allUIElementsAreVisible() {
        onView(withId(R.id.edit_reason)).check(matches(isDisplayed()));
        onView(withId(R.id.spinner_emotional_state)).check(matches(isDisplayed()));
        onView(withId(R.id.spinner_social_situation)).check(matches(isDisplayed()));
        onView(withId(R.id.button_add_event)).check(matches(isDisplayed()));
        onView(withId(R.id.button_upload_photo)).check(matches(isDisplayed()));
        onView(withId(R.id.button_add_location)).check(matches(isDisplayed()));
        onView(withId(R.id.button_view_map)).check(matches(isDisplayed()));
        onView(withId(R.id.button_back_home)).check(matches(isDisplayed()));
    }






}
