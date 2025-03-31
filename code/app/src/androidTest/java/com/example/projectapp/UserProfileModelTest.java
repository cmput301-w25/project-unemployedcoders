package com.example.projectapp;


import com.example.projectapp.models.MoodEvent;
import com.example.projectapp.models.MoodHistory;
import com.example.projectapp.models.UserProfile;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UserProfileModelTest {

    @Test
    public void testFollowingList() {
        UserProfile profile = new UserProfile("1", "testuser", "Test User");
        ArrayList<String> following = new ArrayList<>();
        following.add("user2");
        following.add("user3");
        profile.setFollowing(following);

        assertEquals(2, profile.getFollowing().size());
        assertTrue(profile.getFollowing().contains("user2"));
        assertTrue(profile.getFollowing().contains("user3"));
    }

    @Test
    public void testRecentEvents() {
        UserProfile profile = new UserProfile("1", "testuser", "Test User");
        MoodHistory history = new MoodHistory();
        MoodEvent event1 = new MoodEvent();
        event1.setDate(new Date(2023 - 1900, 1, 1));
        MoodEvent event2 = new MoodEvent();
        event2.setDate(new Date(2023 - 1900, 2, 1));
        history.addEvent(event1);
        history.addEvent(event2);
        profile.setHistory(history);

        assertEquals(2, profile.getRecentEvents().size());
        assertEquals(event2.getDate(), profile.getRecentEvents().get(0).getDate()); // Most recent first
    }
}