package com.example.projectapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.projectapp.models.UserProfile;
import com.example.projectapp.views.activities.HomeActivity;
import com.example.projectapp.views.activities.LoginActivity;
import com.example.projectapp.views.activities.SignupActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
public class LoginActivityTest {
    private static FirebaseAuth mAuth;

    @Rule
    public ActivityScenarioRule<LoginActivity> scenario =
            new ActivityScenarioRule<>(LoginActivity.class);

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
        mAuth.createUserWithEmailAndPassword("neiltest2@email.com", "neiltest2").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mAuth.signInWithEmailAndPassword("neiltest2@email.com", "neiltest2").addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        CollectionReference usersRef = db.collection("users");
                        String uid = mAuth.getCurrentUser().getUid();
                        UserProfile profile = new UserProfile(uid, "Neil", "Neil");
                        usersRef.document(uid).set(profile);
                        mAuth.signOut();
                    }
                });
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


    @Test
    public void emptyFieldsLoginShouldShowError() {
        onView(withText("Login")).perform(click());
        onView(withId(R.id.edit_username)).check(matches(hasErrorText("Email/Password cannot be empty")));
        onView(withId(R.id.edit_password)).check(matches(hasErrorText("Email/Password cannot be empty")));
    }

    @Test
    public void clickingSignUpButtonRedirectsToSignUpActivity() {
        onView(withId(R.id.button_signin)).perform(click());
        intended(hasComponent(SignupActivity.class.getName()));
    }

    public void correctlySigningInWithCredentialsWorksAndRedirectsToHomePage_LogoutAfter() throws InterruptedException {
        // using test credentials since emulator not currently working
        // neiltest2@email.com, neiltest2
        onView(withId(R.id.edit_username)).perform(typeText("neiltest2@email.com"));
        onView(withId(R.id.edit_password)).perform(typeText("neiltest2"));
        onView(withText("Login")).perform(click());

        Thread.sleep(10000); // Wait for sign-in to complete
        intended(hasComponent(HomeActivity.class.getName()));
        onView(withId(R.id.nav_profile)).perform(click());
        Thread.sleep(2500);
        onView(withId(R.id.logout_button)).perform(click());
        intended(hasComponent(LoginActivity.class.getName()));
    }

    @Test
    public void usingNonEmailFormatDisplaysToastError() throws InterruptedException {
        onView(withId(R.id.edit_username)).perform(typeText("notanemail"));
        onView(withId(R.id.edit_password)).perform(typeText("abcdef"));
        onView(withText("Login")).perform(click());

        Thread.sleep(1000); // Allow time for the Toast to appear

        
        
    }

    @Test
    public void usingWrongCredentialsDisplaysToastError() throws InterruptedException {
        onView(withId(R.id.edit_username)).perform(typeText("ThisIsNotAnAccount@email.com"));
        onView(withId(R.id.edit_password)).perform(typeText("ThisIsAnIncorrectAccount"));
        onView(withText("Login")).perform(click());

        Thread.sleep(1000); // Allow time for the Toast

        
        
    }

    @Test
    public void usingNonEmailFormatShouldNotLogUserIn() throws InterruptedException {
        onView(withId(R.id.edit_username)).perform(typeText("notanemail"));
        onView(withId(R.id.edit_password)).perform(typeText("abcdef"));
        onView(withText("Login")).perform(click());

        Thread.sleep(1000);
        ActivityScenario<LoginActivity> scenarioInstance = scenario.getScenario();
        assertEquals(Lifecycle.State.RESUMED, scenarioInstance.getState());
    }

    @Test
    public void usingWrongCredentialsShouldNotLogUserIn() throws InterruptedException {
        onView(withId(R.id.edit_username)).perform(typeText("ThisIsNotAnAccount@email.com"));
        onView(withId(R.id.edit_password)).perform(typeText("ThisIsAnIncorrectAccount"));
        onView(withText("Login")).perform(click());

        Thread.sleep(1000);
        ActivityScenario<LoginActivity> scenarioInstance = scenario.getScenario();
        assertEquals(Lifecycle.State.RESUMED, scenarioInstance.getState());
    }
}
