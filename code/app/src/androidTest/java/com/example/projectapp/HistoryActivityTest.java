package com.example.projectapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.util.Log;

import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
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
public class HistoryActivityTest {

    private static FirebaseAuth mAuth;

    @Rule
    public ActivityScenarioRule<HistoryActivity> scenario = new
            ActivityScenarioRule<HistoryActivity>(HistoryActivity.class);

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
        mAuth.signInWithEmailAndPassword("uitest@email.com", "password");
        Thread.sleep(5000); // needs time to log in

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
        MoodEvent testEvent = new MoodEvent("Happiness", "Happy guy", null, null);
        profile.getHistory().addEvent(testEvent);
        usersRef.document(uid).set(profile);

    }

    @Test
    public void appShouldDisplayMoodHistoryOnLaunch() throws InterruptedException {
        Thread.sleep(5000);

        onView(withText(getHappyEmoticon() + " Happiness")).check(matches(isDisplayed()));
    }

    @Test
    public void appShouldAllowEditingReasonOnValidMoodEvent() throws InterruptedException {
        Thread.sleep(5000);

        onView(withText(getHappyEmoticon() + " Happiness")).perform(click());

        onView(withId(R.id.details_fragment_edit_reason)).perform(ViewActions.clearText());
        onView(withId(R.id.details_fragment_edit_reason)).perform(ViewActions.typeText("Even Happier"));

        onView(withId(android.R.id.button1)).perform(click());

        onView(withText("Even Happier")).check(matches(isDisplayed()));

    }

    @Test
    public void appShouldRefuseInvalidReasonWhileEditing() throws InterruptedException {
        Thread.sleep(5000);

        onView(withText(getHappyEmoticon() + " Happiness")).perform(click());

        onView(withId(R.id.details_fragment_edit_reason)).perform(ViewActions.clearText());
        onView(withId(R.id.details_fragment_edit_reason)).perform(ViewActions.typeText("Yippee Yippee Yippee Yippee"));


        onView(withId(android.R.id.button1)).perform(click());
        onView(withId(R.id.details_fragment_edit_reason)).check(matches(hasErrorText("Invalid Reason")));

    }

    @Test
    public void appShouldDeleteMoodEvent() throws InterruptedException {
        Thread.sleep(5000);

        onView(withText(getHappyEmoticon() + " Happiness")).perform(ViewActions.longClick());

        onView(withId(android.R.id.button1)).perform(click());

        onView(withText(getHappyEmoticon() + " Happiness")).check(doesNotExist());

    }


    private String getHappyEmoticon(){
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        return context.getString(R.string.happiness_emoticon);
    }

    private String getSadEmoticon(){
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        return context.getString(R.string.sadness_emoticon);
    }

}
