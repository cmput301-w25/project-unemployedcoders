package com.example.projectapp;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.intent.Intents.intended;

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

import java.security.AlgorithmParameterGenerator;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MoodEventActivityTest {
    @Rule
    public ActivityScenarioRule<MoodEventActivity> scenario = new
            ActivityScenarioRule<MoodEventActivity>(MoodEventActivity.class);

    @Before
    public void setUp() {
        Intents.init(); // Initialize Espresso Intents
    }

    @After
    public void tearDown() {
        Intents.release(); // Clean up after tests
    }

    //@Test
    public void testAddValidMoodEvent() {
        onView(withId(R.id.spinner_trigger)).perform(ViewActions.typeText("one two three"));
        onView(withId(R.id.button_add_event)).perform(click());

        intended(hasComponent(HomeActivity.class.getName()));
    }



}
