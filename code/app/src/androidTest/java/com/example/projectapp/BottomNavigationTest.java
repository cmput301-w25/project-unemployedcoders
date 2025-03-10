package com.example.projectapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.intent.Intents.intended;
import static org.hamcrest.Matchers.allOf;

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
public class BottomNavigationTest {

    @Rule
    public ActivityScenarioRule<MoodEventActivity> activityRule = new ActivityScenarioRule<>(MoodEventActivity.class);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void clickingHomeTabNavigatesToMainActivityWithHomeFragment() {
        onView(withId(R.id.nav_home)).perform(click());
        intended(
                allOf(
                        hasComponent(MainActivity.class.getName()),
                        hasExtra("selected_fragment", "home")
                )
        );
    }

    @Test
    public void clickingMapTabNavigatesToMainActivityWithMapFragment() {
        onView(withId(R.id.nav_map)).perform(click());
        intended(
                allOf(
                        hasComponent(MainActivity.class.getName()),
                        hasExtra("selected_fragment", "map")
                )
        );
    }

    @Test
    public void clickingHistoryTabNavigatesToMainActivityWithHistoryFragment() {
        onView(withId(R.id.nav_history)).perform(click());
        intended(
                allOf(
                        hasComponent(MainActivity.class.getName()),
                        hasExtra("selected_fragment", "history")
                )
        );
    }

    @Test
    public void clickingInboxTabNavigatesToMainActivityWithInboxFragment() {
        onView(withId(R.id.nav_inbox)).perform(click());
        intended(
                allOf(
                        hasComponent(MainActivity.class.getName()),
                        hasExtra("selected_fragment", "inbox")
                )
        );
    }

    @Test
    public void clickingProfileTabNavigatesToMainActivityWithProfileFragment() {
        onView(withId(R.id.nav_profile)).perform(click());
        intended(
                allOf(
                        hasComponent(MainActivity.class.getName()),
                        hasExtra("selected_fragment", "profile")
                )
        );
    }
}