// -----------------------------------------------------------------------------
// File: SignupActivity.java
// -----------------------------------------------------------------------------
// This file defines the SignupActivity class, which serves as the user registration
// screen in the ProjectApp. It allows users to sign up with an email and password
// using Firebase Authentication, upload a profile photo, and navigate to the
// MoodEventActivity upon successful registration. The activity follows the
// Model-View-Controller (MVC) pattern, acting as the controller.
//
// Design Pattern: MVC (Controller)
// Outstanding Issues:
// N/A
// -----------------------------------------------------------------------------
package com.example.projectapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class SignupActivity extends AppCompatActivity {

    private EditText editEmail, editPassword;
    private Button buttonSignup;
    private FrameLayout profileImageContainer;
    private ImageView imageUserIcon, iconUpload;
    private Uri profilePhotoUri = null;
    private static final int REQUEST_PROFILE_PHOTO = 101;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Bind UI elements
        editEmail = findViewById(R.id.edit_email);
        editPassword = findViewById(R.id.edit_password);
        buttonSignup = findViewById(R.id.button_signup);
        profileImageContainer = findViewById(R.id.profile_image_container);
        imageUserIcon = findViewById(R.id.image_user_icon);
        iconUpload = findViewById(R.id.icon_upload);

        // Set click listener for the upload icon overlay
        iconUpload.setOnClickListener(v -> {
            if (profilePhotoUri == null) {
                // No custom image yet; open gallery for image selection
                openGallery();
            } else {
                // A custom image is already set; ask to remove it
                new AlertDialog.Builder(this)
                        .setTitle("Remove Profile Photo")
                        .setMessage("Do you want to remove your uploaded profile photo and revert to the default?")
                        .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                profilePhotoUri = null;
                                // Revert to default user icon
                                imageUserIcon.setImageResource(R.drawable.ic_user);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        // Sign Up button listener
        buttonSignup.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(SignupActivity.this, "Email and Password cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create new user with Firebase Auth
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {

                                UserProfileChangeRequest.Builder profileUpdatesBuilder = new UserProfileChangeRequest.Builder();
                                if (profilePhotoUri != null) {
                                    profileUpdatesBuilder.setPhotoUri(profilePhotoUri);
                                }
                                // Optionally, you can set a display name here.
                                UserProfileChangeRequest profileUpdates = profileUpdatesBuilder.build();

                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(updateTask -> {
                                            if (updateTask.isSuccessful()) {

                                                // This is what is gonna put the user's data in the firestore, but we might need to update it if we put username field in signup
                                                UserProfile newProfile = new UserProfile(user.getUid(), "Username", "Name");
                                                FirebaseSync fb = FirebaseSync.getInstance();

                                                fb.storeUserData(newProfile);

                                                Toast.makeText(SignupActivity.this, "Sign Up Successful!", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(SignupActivity.this, MoodEventActivity.class));
                                                finish();
                                            } else {
                                                Toast.makeText(SignupActivity.this, "Profile update failed: " +
                                                        Objects.requireNonNull(updateTask.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(SignupActivity.this, "Sign Up failed: " +
                                    Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    // Open the gallery for image selection
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_PROFILE_PHOTO);
    }

    // Handle the result from image selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PROFILE_PHOTO && resultCode == RESULT_OK && data != null) {
            profilePhotoUri = data.getData();
            if (profilePhotoUri != null) {
                imageUserIcon.setImageURI(profilePhotoUri);
                Toast.makeText(this, "Profile photo updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
