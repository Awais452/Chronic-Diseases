package com.example.diseasemanagementapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class Authentication_Sign_Up extends AppCompatActivity {
    TextView loginbut;
    EditText rg_username, rg_email, rg_password, rg_repassword;
    Button rg_signup;
    CircleImageView rg_profileImg;
    FirebaseAuth auth;
    Uri imageURI;
    String imageuri;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication_sign_up);


            // Initialize ProgressDialog
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Establishing The Account");
            progressDialog.setCancelable(false);

            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }

            // Initialize Firebase instances
            database = FirebaseDatabase.getInstance();
            storage = FirebaseStorage.getInstance();
            auth = FirebaseAuth.getInstance();

            // Initialize views
            loginbut = findViewById(R.id.loginbut);
            rg_username = findViewById(R.id.rgusername);
            rg_email = findViewById(R.id.rgemail);
            rg_password = findViewById(R.id.rgpassword);
            rg_repassword = findViewById(R.id.rgrepassword);
            rg_profileImg = findViewById(R.id.profilerg0);
            rg_signup = findViewById(R.id.signupbutton);

            // Navigate to login
            loginbut.setOnClickListener(v -> {
                Intent intent = new Intent(Authentication_Sign_Up.this, login.class);
                startActivity(intent);
                finish();
            });

            // Sign-up button click
            rg_signup.setOnClickListener(v -> {
                String name = rg_username.getText().toString();
                String email = rg_email.getText().toString();
                String password = rg_password.getText().toString();
                String confirmPassword = rg_repassword.getText().toString();
                String status = "Hey I'm Using This Application";

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) ||
                        TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                    Toast.makeText(Authentication_Sign_Up.this, "Please Enter Valid Information", Toast.LENGTH_SHORT).show();
                } else if (!email.matches(emailPattern)) {
                    rg_email.setError("Type A Valid Email Here");
                } else if (password.length() < 6) {
                    rg_password.setError("Password Must Be 6 Characters Or More");
                } else if (!password.equals(confirmPassword)) {
                    rg_password.setError("The Password Doesn't Match");
                } else {
                    progressDialog.show();
                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    String id = task.getResult().getUser().getUid();
                                    DatabaseReference reference = database.getReference().child("user").child(id);
                                    StorageReference storageReference = storage.getReference().child("Upload").child(id);

                                    if (imageURI != null) {
                                        storageReference.putFile(imageURI)
                                                .addOnCompleteListener(uploadTask -> {
                                                    if (uploadTask.isSuccessful()) {
                                                        storageReference.getDownloadUrl()
                                                                .addOnSuccessListener(uri -> {
                                                                    imageuri = uri.toString();
                                                                    Users users = new Users(id, name, email, password, imageuri, status);
                                                                    saveUser(reference, users);
                                                                });
                                                    }
                                                });
                                    } else {
                                        imageuri = "https://firebasestorage.googleapis.com/v0/b/av-messenger-dc8f3.appspot.com/o/man.png?alt=media&token=880f431d-9344-45e7-afe4-c2cafe8a5257";
                                        Users users = new Users(id, name, email, password, imageuri, status);
                                        saveUser(reference, users);
                                    }
                                } else {
                                    progressDialog.dismiss();
                                    Toast.makeText(Authentication_Sign_Up.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });

            // Profile image selection
            rg_profileImg.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
            });
        }

        private void saveUser(DatabaseReference reference, Users users) {
            reference.setValue(users)
                    .addOnCompleteListener(saveTask -> {
                        if (saveTask.isSuccessful()) {
                            if (!isFinishing() && !isDestroyed()) {
                                progressDialog.dismiss();
                            }
                            Intent intent = new Intent(Authentication_Sign_Up.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(Authentication_Sign_Up.this, "Error in creating the user", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 10 && data != null) {
                imageURI = data.getData();
                rg_profileImg.setImageURI(imageURI);
            }
        }

        @Override
        protected void onDestroy() {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            super.onDestroy();
        }
    }

