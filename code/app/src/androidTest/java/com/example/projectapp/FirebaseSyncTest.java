package com.example.projectapp;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;

import com.example.projectapp.database_util.FirebaseSync;
import com.example.projectapp.database_util.FirebaseSync;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class FirebaseSyncTest {

    private FirebaseSync firebaseSync;
    private Context context;

    @Before
    public void setUp() {
        // Get application context
        context = ApplicationProvider.getApplicationContext();
        firebaseSync = FirebaseSync.getInstance();


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();


    }

    @Test
    public void testFirebaseSyncInstance() {
        assertNotNull(firebaseSync);
        assertNotNull(FirebaseFirestore.getInstance());
        assertNotNull(FirebaseAuth.getInstance());
    }


}