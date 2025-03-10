package com.example.projectapp;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MoodEventTest {

    private MoodEvent mockEvent(){
        return new MoodEvent("Anger");
    }

    @Test
    public void testEquals(){
        assertEquals(mockEvent(), mockEvent());
    }

    @Test
    public void testCompare(){

        MoodEvent mood = mockEvent();
        assertEquals(0, mood.compareTo(mood));
    }

    @Test
    public void testValidTrigger(){
        assertTrue(MoodEvent.validReason("trigger"));
        assertTrue(MoodEvent.validReason("one two three"));
        assertTrue(MoodEvent.validReason("12345678901234567890"));

        assertFalse(MoodEvent.validReason("123456789012345678901"));
        assertFalse(MoodEvent.validReason("one two three four"));
        assertFalse(MoodEvent.validReason("1 2 3 4"));
        assertFalse(MoodEvent.validReason("a                    b"));
        assertFalse(MoodEvent.validReason(null));
        assertFalse(MoodEvent.validReason(""));
    }


}
