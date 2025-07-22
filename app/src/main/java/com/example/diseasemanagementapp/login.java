package com.example.diseasemanagementapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;

public class login extends AppCompatActivity {

    // NEW
    private static final String ADMIN_EMAIL = "admin@gmail.com";
    private static final String ADMIN_PASSWORD = "admin123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        TextView logsignup;
        Button button;
        EditText email, password;
        FirebaseAuth auth;
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);

        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        auth = FirebaseAuth.getInstance();
        button = findViewById(R.id.logbutton);
        email = findViewById(R.id.editTexLogEmail);
        password = findViewById(R.id.editTextLogPassword);
        logsignup = findViewById(R.id.logsignup);

        // Navigate to registration screen (No changes here)
        logsignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(login.this, Authentication_Sign_Up.class);
                startActivity(intent);
                finish();
            }
        });

        // Handle sign-in button click
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredEmail = email.getText().toString();
                String enteredPass = password.getText().toString();

                // NEW: First, check if the credentials match the admin credentials
                if (enteredEmail.equals(ADMIN_EMAIL) && enteredPass.equals(ADMIN_PASSWORD)) {
                    // It's the admin! Navigate to the Admin Panel.
                    Toast.makeText(login.this, "Admin Login Successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(login.this, AdminPanelActivity.class); // <-- Make sure you create this Activity
                    startActivity(intent);
                    finish();
                    return; // IMPORTANT: Stop further execution to prevent Firebase login attempt
                }

                // NEW: If it's not the admin, proceed with the regular user login flow.
                // Validate input fields for regular users
                if (TextUtils.isEmpty(enteredEmail)) {
                    email.setError("Enter the email");
                    return;
                } else if (TextUtils.isEmpty(enteredPass)) {
                    password.setError("Enter the password");
                    return;
                } else if (!enteredEmail.matches(emailPattern)) {
                    email.setError("Give proper email address");
                    return;
                } else if (enteredPass.length() < 6) {
                    password.setError("Password needs to be more than 6 characters");
                    return;
                } else {

                    progressDialog.show();

                    auth.signInWithEmailAndPassword(enteredEmail, enteredPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {

                                Intent intent = new Intent(login.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(login.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
