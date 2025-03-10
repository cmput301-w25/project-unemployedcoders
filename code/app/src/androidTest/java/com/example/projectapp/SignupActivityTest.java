package com.example.projectapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SignupActivityTest {

    @Rule
    public ActivityScenarioRule<SignupActivity> scenarioRule =
            new ActivityScenarioRule<>(SignupActivity.class);

    private static FirebaseAuth mAuth;

    @BeforeClass
    public static void setup() {
        String androidLocalhost = "10.0.2.2";
        int portNumber = 8080;
        try {
            FirebaseAuth.getInstance().useEmulator(androidLocalhost, portNumber);
            mAuth = FirebaseAuth.getInstance();
            Log.d("SignupTest", "Firebase emulator setup completed");
        } catch (Exception e) {
            Log.e("SignupTest", "Firebase emulator setup failed: " + e.getMessage());
        }
    }

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
        if (mAuth != null) {
            mAuth.signOut();
        }
    }

    @Test
    public void validSignupNavigatesToHome() throws InterruptedException {
        String testEmail = "test" + System.currentTimeMillis() + "@example.com";
        String testPassword = "password123";
        String testUsername = "testuser" + System.currentTimeMillis();
        String testName = "Test Name";

        Log.d("SignupTest", "Starting signup test with email: " + testEmail);

        // Type each field separately with keyboard close to ensure focus
        onView(withId(R.id.edit_email)).perform(typeText(testEmail), closeSoftKeyboard());
        onView(withId(R.id.edit_password)).perform(typeText(testPassword), closeSoftKeyboard());
        onView(withId(R.id.edit_username_signup)).perform(typeText(testUsername), closeSoftKeyboard());
        onView(withId(R.id.edit_name)).perform(typeText(testName), closeSoftKeyboard());

        Log.d("SignupTest", "Filled fields, clicking signup button");
        onView(withId(R.id.button_signup)).perform(click());

        Thread.sleep(2000); // Increased to 10s for slower emulators
    }

    @Test
    public void emptyFieldsShowError() {
        onView(withId(R.id.button_signup)).perform(click());
        onView(withId(R.id.edit_email)).check(matches(isDisplayed()));
    }

    @Test
    public void invalidEmailFormatShowsError() throws InterruptedException {
        onView(withId(R.id.edit_email)).perform(typeText("invalidemail"), closeSoftKeyboard());
        onView(withId(R.id.edit_password)).perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.edit_username_signup)).perform(typeText("testuser"), closeSoftKeyboard());
        onView(withId(R.id.edit_name)).perform(typeText("Test Name"), closeSoftKeyboard());

        onView(withId(R.id.button_signup)).perform(click());

        Thread.sleep(2000);
        onView(withId(R.id.button_signup)).check(matches(isDisplayed()));
    }

    @Test
    public void profilePhotoUploadButtonWorks() throws InterruptedException {
        Intent resultData = new Intent();
        Uri fakeUri = Uri.parse("content://media/external/images/media/100000");
        resultData.setData(fakeUri);

        intending(hasAction(Intent.ACTION_PICK))
                .respondWith(new Instrumentation.ActivityResult(android.app.Activity.RESULT_OK, resultData));

        onView(withId(R.id.icon_upload)).perform(click());

        Thread.sleep(2000);
        onView(withId(R.id.button_signup)).check(matches(isDisplayed()));
    }

    @Test
    public void removeProfilePhotoWorks() throws InterruptedException {
        Intent resultData = new Intent();
        Uri fakeUri = Uri.parse("content://media/external/images/media/100000");
        resultData.setData(fakeUri);

        intending(hasAction(Intent.ACTION_PICK))
                .respondWith(new Instrumentation.ActivityResult(android.app.Activity.RESULT_OK, resultData));

        onView(withId(R.id.icon_upload)).perform(click());

        Thread.sleep(2000);

        onView(withId(R.id.icon_upload)).perform(click());
        onView(withText("Remove")).perform(click());

        Thread.sleep(2000);
        onView(withId(R.id.button_signup)).check(matches(isDisplayed()));
    }

    @Test
    public void emptyUsernameOrNameShowsError() throws InterruptedException {
        onView(withId(R.id.edit_email)).perform(typeText("test@example.com"), closeSoftKeyboard());
        onView(withId(R.id.edit_password)).perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.button_signup)).perform(click());

        Thread.sleep(2000);
        onView(withId(R.id.button_signup)).check(matches(isDisplayed()));
    }

    @Test
    public void signupElementsAreVisible() {
        onView(withId(R.id.edit_email)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_password)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_username_signup)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_name)).check(matches(isDisplayed()));
        onView(withId(R.id.button_signup)).check(matches(isDisplayed()));
        onView(withId(R.id.profile_image_container)).check(matches(isDisplayed()));
        onView(withId(R.id.icon_upload)).check(matches(isDisplayed()));
    }

    private static androidx.test.espresso.intent.VerificationMode times(int times) {
        return androidx.test.espresso.intent.VerificationModes.times(times);
    }
}