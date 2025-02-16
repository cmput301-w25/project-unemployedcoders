package com.example.projectapp;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

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


}
