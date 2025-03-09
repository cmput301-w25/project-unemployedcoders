package com.example.projectapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static com.example.projectapp.ToastMatcher.isToast; // Assuming you have a ToastMatcher with this static method
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

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
public class LoginActivityTest {
    @Rule
    public ActivityScenarioRule<LoginActivity> scenario =
            new ActivityScenarioRule<>(LoginActivity.class);

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
        // using test credentials since emulator not currently working
        // neiltest2@email.com, neiltest2
        onView(withId(R.id.edit_username)).perform(typeText("neiltest2@email.com"));
        onView(withId(R.id.edit_password)).perform(typeText("neiltest2"));
        onView(withId(R.id.button_login)).perform(click());

        Thread.sleep(3000); // Wait for sign-in to complete
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
        onView(withId(R.id.button_login)).perform(click());

        Thread.sleep(1000); // Allow time for the Toast to appear

        // Adjust the expected string as needed to match the actual Firebase error message
        onView(withText("Login failed: The email address is badly formatted."))
                .inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));
    }

    @Test
    public void usingWrongCredentialsDisplaysToastError() throws InterruptedException {
        onView(withId(R.id.edit_username)).perform(typeText("ThisIsNotAnAccount@email.com"));
        onView(withId(R.id.edit_password)).perform(typeText("ThisIsAnIncorrectAccount"));
        onView(withId(R.id.button_login)).perform(click());

        Thread.sleep(1000); // Allow time for the Toast

        // Adjust the expected string as needed. This is an example; Firebase may return a different message.
        onView(withText("Login failed: The password is invalid or the user does not have a password."))
                .inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));
    }

    @Test
    public void usingNonEmailFormatShouldNotLogUserIn() throws InterruptedException {
        onView(withId(R.id.edit_username)).perform(typeText("notanemail"));
        onView(withId(R.id.edit_password)).perform(typeText("abcdef"));
        onView(withId(R.id.button_login)).perform(click());

        Thread.sleep(1000);
        ActivityScenario<LoginActivity> scenarioInstance = scenario.getScenario();
        assertEquals(Lifecycle.State.RESUMED, scenarioInstance.getState());
    }

    @Test
    public void usingWrongCredentialsShouldNotLogUserIn() throws InterruptedException {
        onView(withId(R.id.edit_username)).perform(typeText("ThisIsNotAnAccount@email.com"));
        onView(withId(R.id.edit_password)).perform(typeText("ThisIsAnIncorrectAccount"));
        onView(withId(R.id.button_login)).perform(click());

        Thread.sleep(1000);
        ActivityScenario<LoginActivity> scenarioInstance = scenario.getScenario();
        assertEquals(Lifecycle.State.RESUMED, scenarioInstance.getState());
    }
}
