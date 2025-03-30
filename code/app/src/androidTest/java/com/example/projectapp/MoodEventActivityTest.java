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



import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.projectapp.views.activities.HomeActivity;
import com.example.projectapp.views.activities.MainActivity;
import com.example.projectapp.views.activities.MoodEventActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MoodEventActivityTest {

    @Rule
    public ActivityScenarioRule<MoodEventActivity> activityRule =
            new ActivityScenarioRule<>(MoodEventActivity.class);

    @Before
    public void setup() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }



    /**
     * Test bottom navigation: tapping "Home" navigates to MainActivity with "home" fragment.
     */
    @Test
    public void bottomNavHomeNavigatesToMainActivity() {
        // Tap the "Home" menu item from the bottom navigation.
        onView(withId(R.id.nav_home)).perform(click());
        intended(allOf(
                hasComponent(MainActivity.class.getName()),
                hasExtra("selected_fragment", equalTo("home"))
        ));
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
