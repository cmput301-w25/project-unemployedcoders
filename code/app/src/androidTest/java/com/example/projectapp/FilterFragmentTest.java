package com.example.projectapp;


import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.projectapp.views.activities.MainActivity;
import com.example.projectapp.views.fragments.FilterFragment;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class FilterFragmentTest {

    @Test
    public void testFilterFragmentDisplay() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);

        scenario.onActivity(activity -> {
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new FilterFragment())
                    .commitNow();
        });

        onView(withId(R.id.filter_button)).check(matches(isDisplayed()));
    }

    @Test
    public void testApplyFilter() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);

        scenario.onActivity(activity -> {
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new FilterFragment())
                    .commitNow();
        });

        onView(withId(R.id.filter_button)).perform(click());
    }

}