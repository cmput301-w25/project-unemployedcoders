package com.example.projectapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollCompletelyTo;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Root;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.rule.IntentsTestRule;


import android.util.Log;
import android.view.View;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginActivityTest {
    @Rule
    public ActivityScenarioRule<LoginActivity> scenario = new
            ActivityScenarioRule<LoginActivity>(LoginActivity.class);

    private View decorView;


    @Before
    public void setup() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void emptyFieldsLoginShouldShowError() {
        onView(withId(R.id.button_login)).perform(click());

        onView(withId(R.id.edit_username)).check(matches(hasErrorText("Email/Password cannot be empty")));
        onView(withId(R.id.edit_password)).check(matches(hasErrorText("Email/Password cannot be empty")));
    }

    @Test
    public void clickingSignUpButtonRedirectsToSignUpActivity() {
        onView(withId(R.id.button_signin)).perform(click());
        intended(hasComponent(SignupActivity.class.getName()));
    }

    @Test
    public void correctlySigningInWithCredentialsWorksAndRedirectsToHomePage_LogoutAfter() throws InterruptedException {
        //using test credentials since emulator not currently working
        //neiltest2@email.com, neiltest2
        onView(withId(R.id.edit_username)).perform(ViewActions.typeText("neiltest2@email.com"));
        onView(withId(R.id.edit_password)).perform(ViewActions.typeText("neiltest2"));
        onView(withId(R.id.button_login)).perform(click());

        Thread.sleep(3000);
        intended(hasComponent(HomeActivity.class.getName()));
        onView(withId(R.id.nav_profile)).perform(click());
        Thread.sleep(2500);
        onView(withId(R.id.logout_button)).perform(click());
        intended(hasComponent(LoginActivity.class.getName()));

    }
    //The email address is badly formatted.
    //The supplied auth credential is incorrect, malformed or has expired.

    @Test
    public void usingNonEmailFormatShouldDisplayError() throws InterruptedException {
        onView(withId(R.id.edit_username)).perform(ViewActions.typeText("notanemail"));
        onView(withId(R.id.edit_password)).perform(ViewActions.typeText("abcdef"));
        onView(withId(R.id.button_login)).perform(click());

        //The email address is badly formatted.
        //I don't know how to check Toasts with Espresso, so we'll just check that we stay in the same page
        //onView(withText("Login Failed: The email address is badly formatted.")).inRoot(isToast()).check(matches(isDisplayed()));
        Thread.sleep(1000);
        assertEquals(Lifecycle.State.RESUMED, scenario.getScenario().getState());
    }

    @Test
    public void usingWrongCredentialsShouldNotLogUserIn() throws InterruptedException {
        onView(withId(R.id.edit_username)).perform(ViewActions.typeText("ThisIsNotAnAccount@email.com"));
        onView(withId(R.id.edit_password)).perform(ViewActions.typeText("ThisIsAnIncorrectAccount"));
        onView(withId(R.id.button_login)).perform(click());

        //Once again, don't really know how to test Toasts so we'll just check
        //that the we stay in the same activity
        Thread.sleep(1000);
        assertEquals(Lifecycle.State.RESUMED, scenario.getScenario().getState());

    }
}