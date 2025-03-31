package com.example.projectapp;

import com.example.projectapp.models.UserProfile;
import com.example.projectapp.views.adapters.UserAdapter;
import org.junit.Before;
import org.junit.Test;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class UserAdapterTest {

    private UserAdapter adapter;

    @Before
    public void setUp() {
        List<UserProfile> users = new ArrayList<>();
        users.add(new UserProfile("1", "John Doe", "johndoe"));
        users.add(new UserProfile("2", "Jane Smith", "janesmith"));
        adapter = new UserAdapter(null, users);
    }

    @Test
    public void testGetItemCount() {
        assertEquals(2, adapter.getItemCount());
    }


}