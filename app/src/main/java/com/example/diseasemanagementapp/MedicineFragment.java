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
import java.util.List;

public class MedicineFragment extends Fragment {

    private RecyclerView recyclerView;
    private MedicineAdapter adapter;
    private List<Medicine> medicineList;
    private MaterialButton btnAddMedicine;

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private ValueEventListener medicineListener;

    public MedicineFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_medicine, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        recyclerView = view.findViewById(R.id.recycler_view_medicines); // Make sure this ID is in your XML
        btnAddMedicine = view.findViewById(R.id.btn_add_medicine); // Make sure this ID is in your XML

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        medicineList = new ArrayList<>();
        adapter = new MedicineAdapter(medicineList);
        recyclerView.setAdapter(adapter);

        btnAddMedicine.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AddMedicineActivity.class));
        });

        if (currentUser != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("Medicines").child(currentUser.getUid());
            loadMedicinesFromFirebase();
        } else {
            Toast.makeText(getContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMedicinesFromFirebase() {
        medicineListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                medicineList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Medicine medicine = dataSnapshot.getValue(Medicine.class);
                    if (medicine != null) {
                        medicineList.add(medicine);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load medicines.", Toast.LENGTH_SHORT).show();
            }
        };
        databaseReference.addValueEventListener(medicineListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Important: Remove the listener to prevent memory leaks when the view is destroyed
        if (databaseReference != null && medicineListener != null) {
            databaseReference.removeEventListener(medicineListener);
        }
    }
}