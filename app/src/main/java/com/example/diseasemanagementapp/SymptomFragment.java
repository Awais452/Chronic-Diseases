package com.example.diseasemanagementapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SymptomFragment extends Fragment {

    private RecyclerView recyclerView;
    private SymptomAdapter adapter;
    private List<Symptom> symptomList;
    private MaterialButton btnLogNewSymptom;

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private ValueEventListener symptomListener;

    public SymptomFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_symptom, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        recyclerView = view.findViewById(R.id.recycler_view_symptoms);
        btnLogNewSymptom = view.findViewById(R.id.btn_log_new_symptom);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        symptomList = new ArrayList<>();
        adapter = new SymptomAdapter(symptomList);
        recyclerView.setAdapter(adapter);

        btnLogNewSymptom.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LogSymptomActivity.class);
            startActivity(intent);
        });

        if (currentUser != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("Symptoms").child(currentUser.getUid());
            loadSymptomsFromFirebase();
        } else {
            Toast.makeText(getContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSymptomsFromFirebase() {
        symptomListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                symptomList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Symptom symptom = dataSnapshot.getValue(Symptom.class);
                    if (symptom != null) {
                        symptomList.add(symptom);
                    }
                }
                // Sort by timestamp, newest first
                Collections.sort(symptomList, (s1, s2) -> Long.compare(s2.getTimestamp(), s1.getTimestamp()));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load symptoms.", Toast.LENGTH_SHORT).show();
            }
        };
        databaseReference.addValueEventListener(symptomListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // **IMPORTANT**: Remove the listener to prevent memory leaks
        if (databaseReference != null && symptomListener != null) {
            databaseReference.removeEventListener(symptomListener);
        }
    }
}