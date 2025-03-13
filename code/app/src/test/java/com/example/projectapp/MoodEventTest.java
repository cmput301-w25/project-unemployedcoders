package com.example.projectapp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the MoodEvent class.
 */
public class MoodEventTest {

    private MoodEvent mockEvent() {
        return new MoodEvent("Anger", "Test reason");
    }


    /**
     * Tests the equals() method.
     */
    @Test
    public void testEquals() {
        assertEquals(mockEvent(), mockEvent());
    }

    /**
     * Tests the compareTo() method, ensuring it compares two identical MoodEvents
     * and returns 0 (meaning equal in ordering).
     */
    @Test
    public void testCompare() {
        MoodEvent mood = mockEvent();
        assertEquals(0, mood.compareTo(mood));
    }

    /**
     * Tests the validReason() method with various inputs,
     * ensuring it returns true for valid reasons and false otherwise.
     */
    @Test
    public void testValidReason() {
        // Valid
        assertTrue(MoodEvent.validReason("trigger"));
        assertTrue(MoodEvent.validReason("one two three"));
        assertTrue(MoodEvent.validReason("12345678901234567890"));

        // Invalid
        assertFalse(MoodEvent.validReason("123456789012345678901")); // 21 chars
        assertFalse(MoodEvent.validReason("one two three four"));     // 4 words
        assertFalse(MoodEvent.validReason("1 2 3 4"));                // 4 words
        assertFalse(MoodEvent.validReason("a                    b"));  // More than 20 chars
        assertFalse(MoodEvent.validReason(null));
        assertFalse(MoodEvent.validReason(""));
    }
}
