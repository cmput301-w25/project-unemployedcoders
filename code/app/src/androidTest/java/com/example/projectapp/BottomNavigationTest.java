package com.example.projectapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.intent.Intents.intended;
import static org.hamcrest.Matchers.allOf;

import android.util.Log;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import com.example.projectapp.models.UserProfile;
import com.example.projectapp.views.activities.HistoryActivity;
import com.example.projectapp.views.activities.HomeActivity;
import com.example.projectapp.views.activities.InboxActivity;
import com.example.projectapp.views.activities.MainActivity;
import com.example.projectapp.views.activities.MapActivity;
import com.example.projectapp.views.activities.MoodEventActivity;
import com.example.projectapp.views.activities.ProfileActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
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
public class BottomNavigationTest {

    private static FirebaseAuth mAuth;

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


    @Rule
    public ActivityScenarioRule<MoodEventActivity> activityRule = new ActivityScenarioRule<>(MoodEventActivity.class);

    @Rule
    public GrantPermissionRule permissionRule =
            GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);

    @Test
    public void clickingHomeTabNavigatesToMainActivityWithHomeFragment() {
        onView(withId(R.id.nav_home)).perform(click());
        intended(
                allOf(
                        hasComponent(HomeActivity.class.getName())
                )
        );
    }

    @Test
    public void clickingMapTabNavigatesToMainActivityWithMapFragment() {
        onView(withId(R.id.nav_map)).perform(click());
        intended(
                allOf(
                        hasComponent(MapActivity.class.getName())
                )
        );
    }

    @Test
    public void clickingHistoryTabNavigatesToMainActivityWithHistoryFragment() {
        onView(withId(R.id.nav_history)).perform(click());
        intended(
                allOf(
                        hasComponent(HistoryActivity.class.getName())
                )
        );
    }

    @Test
    public void clickingInboxTabNavigatesToMainActivityWithInboxFragment() {
        onView(withId(R.id.nav_inbox)).perform(click());
        intended(
                allOf(
                        hasComponent(InboxActivity.class.getName())
                )
        );
    }

    @Test
    public void clickingProfileTabNavigatesToMainActivityWithProfileFragment() {
        onView(withId(R.id.nav_profile)).perform(click());
        intended(
                allOf(
                        hasComponent(ProfileActivity.class.getName())
                )
        );
    }
}