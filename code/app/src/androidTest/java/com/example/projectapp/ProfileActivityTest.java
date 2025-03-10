package com.example.projectapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ProfileActivityTest {

    @Rule
    public ActivityScenarioRule<ProfileActivity> scenarioRule =
            new ActivityScenarioRule<>(ProfileActivity.class);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    /**
     * Verifies that the Profile screen is displayed, and that
     * the username text field is visible. You might want to
     * confirm the actual text once the profile fetch completes.
     */
    @Test
    public void profileActivityIsDisplayed() throws InterruptedException {
        // Wait a moment for fetchUserProfileObject to complete (simple approach)
        Thread.sleep(2000);

        onView(withId(R.id.profile_username))
                .check(matches(isDisplayed()));
    }

    /**
     * Tests the bottom navigation:
     * Tapping the "History" tab should navigate to HistoryActivity.
     */
    @Test
    public void bottomNavNavigatesToHistory() {
        // Tap on the nav_history item
        onView(withId(R.id.nav_history)).perform(click());

        // Check that we intend to go to HistoryActivity
        intended(hasComponent(HistoryActivity.class.getName()));
    }

    /**
     * Tests that tapping the "Logout" button signs out and returns to LoginActivity.
     */
    @Test
    public void logoutButtonSignsOutAndGoesToLogin() throws InterruptedException {
        // Wait for any data fetch to complete
        Thread.sleep(1000);

        // Tap the logout button
        onView(withId(R.id.logout_button)).perform(click());

        // Verify that we land on LoginActivity
        intended(hasComponent(LoginActivity.class.getName()));
    }

    @Test
    public void profileElementsAreVisible() throws InterruptedException {
        Thread.sleep(1000); // Wait for fetchUserProfileObject to complete

        onView(withId(R.id.profile_username)).check(matches(isDisplayed()));
        onView(withId(R.id.logout_button)).check(matches(isDisplayed()));
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()));
    }

    

    @Test
    public void usernameIsDisplayedCorrectly() throws InterruptedException {
        // Wait for FirebaseSync fetch to complete
        Thread.sleep(2000);

        // If the user profile has username "TestUser", check it’s displayed
        onView(withId(R.id.profile_username))
                .check(matches(withText("Username")));
    }





}
