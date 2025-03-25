package com.example.projectapp;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class MoodHistoryTest {

    private MoodEvent mockEvent(){
        return new MoodEvent("Anger", "reason");
    }

    private MoodHistory mockHistory(){
        return new MoodHistory();
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
    public void testFilter(){

    }

}
