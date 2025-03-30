package com.example.projectapp;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.example.projectapp.models.MoodEvent;

public class MoodEventTest {

    private MoodEvent mockEvent(){
        return new MoodEvent("Anger", "Angry Guy", null, null, "1234567890");
    }

    @Test
    public void testEquals(){
        MoodEvent m = mockEvent();
        assertEquals(m, m);
    }

    @Test
    public void testCompare() throws InterruptedException {
        MoodEvent mood = mockEvent();
        Thread.sleep(5000);
        MoodEvent mood2 = mockEvent();

        assertEquals(0, mood.compareTo(mood));
        assertTrue(mood.compareTo(mood2) > 0);
        assertTrue(mood2.compareTo(mood) < 0);
    }

    @Test
    public void testRejectInvalidEmotionalState(){
        assertThrows(IllegalArgumentException.class, () -> new MoodEvent("Not an emotional state", "Angry Guy", null, null, "1234567890"));
    }

    @Test
    public void testRejectInvalidReason(){
        assertThrows(IllegalArgumentException.class, () -> new MoodEvent("Anger", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", null, null, "1234567890"));
    }

    @Test
    public void testRejectNoUID(){
        assertThrows(IllegalArgumentException.class, () -> new MoodEvent("Anger", "Angry Guy", null, null, null));
    }

    @Test
    public void testRejectInvalidSocialSituation(){
        assertThrows(IllegalArgumentException.class, () -> new MoodEvent("Anger", "Angry Guy", "With myself and with a crowd ;)", null, "1234567890"));
    }

    @Test
    public void rejectInvalidEmotionalStateUpdate(){
        MoodEvent m = mockEvent();
        assertThrows(IllegalArgumentException.class, () -> m.setEmotionalState("Not an emotional state"));
    }

    @Test
    public void rejectInvalidSocialSituationUpdate(){
        MoodEvent m = mockEvent();
        assertThrows(IllegalArgumentException.class, () -> m.setSocialSituation("Not an social situation"));
    }

    @Test
    public void testChangeEmotionalStateChangesColor(){
        MoodEvent m = mockEvent();
        m.setEmotionalState("Happiness");
        assertEquals(R.color.happiness, m.getColorResource());
    }

    @Test
    public void testEqualsOnNull(){
        MoodEvent m = mockEvent();
        assertFalse(m.equals(null));
    }

    @Test
    public void testValidTrigger(){
        assertTrue(MoodEvent.validReason("trigger"));
        assertTrue(MoodEvent.validReason("one two three"));
        assertTrue(MoodEvent.validReason("12345678901234567890"));

        assertTrue(MoodEvent.validReason("123456789012345678901"));
        assertTrue(MoodEvent.validReason("one two three four"));
        assertTrue(MoodEvent.validReason("1 2 3 4"));
        assertTrue(MoodEvent.validReason("a                    b"));
        assertFalse(MoodEvent.validReason("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa" +
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa" +
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
    }


}
