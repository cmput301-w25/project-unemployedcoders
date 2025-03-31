package com.example.projectapp;

import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.projectapp.views.activities.SearchActivity;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.notNullValue;

@RunWith(AndroidJUnit4.class)
public class SearchActivityTest {

    @Test
    public void testSearchActivityLaunch() {
        ActivityScenario<SearchActivity> scenario = ActivityScenario.launch(SearchActivity.class);
        assertThat(scenario, notNullValue());

        onView(withId(R.id.search_input)).check(matches(isDisplayed()));
    }

    @Test
    public void testBackNavigation() {
        ActivityScenario<SearchActivity> scenario = ActivityScenario.launch(SearchActivity.class);
        onView(withId(R.id.search_input)).check(matches(isDisplayed()));
        onView(withId(android.R.id.content)).perform(pressBack());

    }
}