package com.example.diseasemanagementapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.diseasemanagementapp.databinding.FragmentProfileBinding; // Import generated binding class
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    // Use View Binding to avoid findViewById
    private FragmentProfileBinding binding;

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseUser currentUser;
    private String userId;

    // Activity Result Launcher for picking an image from the gallery
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri imageUri;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Ensure user is logged in
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        // Initialize the ActivityResultLauncher
        // This is the modern and recommended way to handle activity results
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                        imageUri = result.getData().getData();
                        // Set the selected image to the ImageView and start upload
                        binding.profileImage.setImageURI(imageUri);
                        uploadImageToFirebase();
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout using View Binding
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // If the user is not logged in, you might want to redirect them
        if (currentUser == null) {
            // e.g., navigate to LoginFragment/Activity
            return;
        }

        loadUserProfile();
        setupClickListeners();
    }

    /**
     * Loads the user's profile data from Firestore and populates the views.
     */
    private void loadUserProfile() {
        binding.textViewUserEmail.setText(currentUser.getEmail());

        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                binding.textViewUserName.setText(name);

                if (profileImageUrl != null && !profileImageUrl.isEmpty() && getContext() != null) {
                    Glide.with(getContext()).load(profileImageUrl).circleCrop().into(binding.profileImage);
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to load profile.", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Sets up all the click listeners for the buttons and settings.
     */
    private void setupClickListeners() {
        // Edit Profile Picture
        binding.profileImageCard.setOnClickListener(v -> openImagePicker());

        // Edit Profile Button (to change name, etc.)
        binding.editProfileButton.setOnClickListener(v -> {
            // You can implement an edit profile dialog or a new fragment here
            Toast.makeText(getContext(), "Edit Profile clicked", Toast.LENGTH_SHORT).show();
        });

        // Change Password
        binding.changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());

        // Settings Listeners
        binding.appThemeLayout.setOnClickListener(v -> showThemeSelectionDialog());
        binding.privacyLayout.setOnClickListener(v -> {
            // Make sure you have created PrivacyPolicyActivity
            // startActivity(new Intent(getActivity(), PrivacyPolicyActivity.class));
            Toast.makeText(getContext(), "Privacy Policy clicked", Toast.LENGTH_SHORT).show();
        });

        // Add listeners for Notifications and Connected Devices if needed
        binding.notificationsLayout.setOnClickListener(v -> Toast.makeText(getContext(), "Notifications Clicked", Toast.LENGTH_SHORT).show());
        binding.connectedDevicesLayout.setOnClickListener(v -> Toast.makeText(getContext(), "Connected Devices Clicked", Toast.LENGTH_SHORT).show());

        // Logout
        binding.logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            // Navigate back to the login screen
            // Intent intent = new Intent(getActivity(), LoginActivity.class);
            // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // startActivity(intent);
            Toast.makeText(getContext(), "Logged Out", Toast.LENGTH_SHORT).show();
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(intent);
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            // Show a progress bar
            binding.progressBar.setVisibility(View.VISIBLE);

            StorageReference fileReference = storage.getReference().child("profile_pictures/" + userId);
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        binding.progressBar.setVisibility(View.GONE);
                        String imageUrl = uri.toString();
                        updateProfileImageUrlInFirestore(imageUrl);
                    }))
                    .addOnFailureListener(e -> {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateProfileImageUrlInFirestore(String imageUrl) {
        db.collection("users").document(userId).update("profileImageUrl", imageUrl)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Profile picture updated.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update profile picture.", Toast.LENGTH_SHORT).show());
    }

    private void showChangePasswordDialog() {
        final EditText newPasswordInput = new EditText(getContext());
        newPasswordInput.setHint("Enter new password");

        new AlertDialog.Builder(getContext())
                .setTitle("Change Password")
                .setView(newPasswordInput)
                .setPositiveButton("Change", (dialog, which) -> {
                    String newPassword = newPasswordInput.getText().toString().trim();
                    if (newPassword.length() < 6) {
                        Toast.makeText(getContext(), "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    currentUser.updatePassword(newPassword).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Password updated successfully.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to update password. " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showThemeSelectionDialog() {
        String[] themes = {"Light", "Dark", "System Default"};
        new AlertDialog.Builder(getContext())
                .setTitle("Choose Theme")
                .setItems(themes, (dialog, which) -> {
                    String themePreference;
                    switch (which) {
                        case 0:
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            themePreference = "light";
                            break;
                        case 1:
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            themePreference = "dark";
                            break;
                        default:
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                            themePreference = "system";
                            break;
                    }
                    // Save the theme preference to Firestore so it can be applied on app startup
                    db.collection("users").document(userId).update("theme", themePreference);
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Set binding to null to avoid memory leaks
        binding = null;
    }
}