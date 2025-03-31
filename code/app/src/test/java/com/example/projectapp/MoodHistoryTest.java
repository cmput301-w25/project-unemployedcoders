package com.example.projectapp;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.example.projectapp.models.MoodEvent;
import com.example.projectapp.models.MoodHistory;

import java.util.ArrayList;

public class MoodHistoryTest {

    private MoodEvent mockEvent(){
        return new MoodEvent("Anger", "Angry Guy", null, null, "1234567890");
    }

    private MoodHistory mockHistory(){
        return new MoodHistory();
    }

    private MoodHistory mockFullHistory() throws InterruptedException {
        MoodHistory history = new MoodHistory();
        history.addEvent(new MoodEvent("Happiness", "I am happy", null, null, "1234567890"));
        Thread.sleep(1000);
        history.addEvent(new MoodEvent("Anger", "I am mad", "Alone", null, "1234567890"));
        Thread.sleep(1000);
        history.addEvent(new MoodEvent("Shame", null, "With one other person", null, "1234567890"));
        Thread.sleep(1000);
        MoodEvent m = new MoodEvent("Anger", "Mad again", null, null, "1234567890");
        history.addEvent(m);

        return history;
    }

    @Test
    public void testAdd(){
        MoodHistory m = mockHistory();
        MoodEvent event = mockEvent();
        m.addEvent(event);
        assertTrue(m.contains(mockEvent()));
    }

    @Test
    public void testDelete(){
        MoodHistory m = mockHistory();
        MoodEvent event = mockEvent();
        m.addEvent(event);
        assertTrue(m.contains(event));
        m.deleteEvent(event);
        assertFalse(m.contains(event));
    }

    @Test
    public void testEmotionalStateFilter() throws InterruptedException {
        MoodHistory history = mockFullHistory();

        ArrayList<String> filters = new ArrayList<>();
        filters.add("Emotional State:Anger");

        MoodHistory filteredHistory = history.getFilteredVersion(filters);

        assertEquals(2, filteredHistory.getEvents().size());
        assertEquals("I am mad", filteredHistory.getEvents().get(1).getReason());
        assertEquals("Mad again", filteredHistory.getEvents().get(0).getReason());
    }

    @Test
    public void testReasonFilter() throws InterruptedException {
        MoodHistory history = mockFullHistory();

        ArrayList<String> filters = new ArrayList<>();
        filters.add("Reason Contains:Mad");

        MoodHistory filteredHistory = history.getFilteredVersion(filters);

        assertEquals(2, filteredHistory.getEvents().size());
        assertEquals("I am mad", filteredHistory.getEvents().get(1).getReason());
        assertEquals("Mad again", filteredHistory.getEvents().get(0).getReason());
    }

    @Test
    public void testMultipleFilters() throws InterruptedException {
        MoodHistory history = mockFullHistory();

        ArrayList<String> filters = new ArrayList<>();
        filters.add("Emotional State:Anger");
        filters.add("Reason Contains:am");

        MoodHistory filteredHistory = history.getFilteredVersion(filters);

        assertEquals(1, filteredHistory.getEvents().size());
        assertEquals("I am mad", filteredHistory.getEvents().get(0).getReason());
    }



}
