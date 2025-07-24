package com.example.diseasemanagementapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- Find views by their IDs ---
        TextView tvUsername = view.findViewById(R.id.tv_username1);
        TextView tvProfileInitial = view.findViewById(R.id.tv_profile_initial1);
        MaterialButton Sbtn= view.findViewById(R.id.btn_log_symptom1);
        MaterialButton Mbtn= view.findViewById(R.id.btn_add_medicine1);
        Sbtn.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), LogSymptomActivity.class));
        });
        Mbtn.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AddMedicineActivity.class));
        });

        // --- Connect to Firebase to get User Data ---
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // This is the placeholder for fetching the user's name.
            // For now, we will use the email.
            // Replace this with your logic to get the full name from Firestore if you have it.
            String email = currentUser.getEmail();
            if (email != null && !email.isEmpty()) {
                String username = email.split("@")[0]; // Simple way to get username from email
                tvUsername.setText(username);

                // Set the initial for the profile circle
                tvProfileInitial.setText(String.valueOf(username.charAt(0)).toUpperCase());
            }
        }

        // You can set OnClickListeners for your buttons here
        // Example:
        // Button btnLogSymptom = view.findViewById(R.id.btn_log_symptom);
        // btnLogSymptom.setOnClickListener(v -> {
        //     // Navigate to Log Symptom screen
        // });
    }
}
