// -----------------------------------------------------------------------------
// File: LoginActivity.java
// -----------------------------------------------------------------------------
// This file defines the LoginActivity class, which serves as the entry point
// for user authentication in the ProjectApp application. It handles user login
// via email and password using Firebase Authentication, navigates to HomeActivity
// upon successful login, and provides access to the SignupActivity for new users.
// Additionally, it implements a password reset feature using a ResetPasswordDialogFragment.
//
// Design Pattern: MVC (Controller) and MVC (View)
// Outstanding Issues:
//  N/A
// -----------------------------------------------------------------------------

package com.example.projectapp.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projectapp.R;
import com.example.projectapp.views.fragments.ResetPasswordDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText editUsername, editPassword;
    private Button buttonLogin, buttonSignin, buttonReset;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        editUsername = findViewById(R.id.edit_username);
        editPassword = findViewById(R.id.edit_password);
        buttonLogin = findViewById(R.id.button_login);
        buttonSignin = findViewById(R.id.button_signin);
        buttonReset = findViewById(R.id.button_reset);  // Ensure this ID matches your layout

        buttonLogin.setOnClickListener(view -> {
            String email = editUsername.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                editUsername.setError("Email/Password cannot be empty");
                editPassword.setError("Email/Password cannot be empty");
                Toast.makeText(this, "Email/Password cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Login Success!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        buttonSignin.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        // When the reset password button is clicked, show the ResetPasswordDialogFragment
        buttonReset.setOnClickListener(view -> {
            ResetPasswordDialogFragment dialog = new ResetPasswordDialogFragment();
            dialog.show(getSupportFragmentManager(), "ResetPasswordDialog");
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        }
    }
}
