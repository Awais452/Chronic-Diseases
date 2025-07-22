package com.example.diseasemanagementapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log; // Make sure to import Log
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LogSymptomActivity extends AppCompatActivity {

    // A tag for logging
    private static final String TAG = "LogSymptomActivity";

    private EditText etSymptomName, etAdditionalNotes;
    private Slider sliderIntensity;
    private TextView tvDateTime;
    private MaterialButton btnSaveSymptom;
    private ImageView ivClose;

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    private Calendar selectedDateTime = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_symptom);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Log.e(TAG, "User is not logged in. Cannot log symptom.");
            Toast.makeText(this, "You must be logged in to save a symptom.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("Symptoms").child(currentUser.getUid());

        etSymptomName = findViewById(R.id.et_symptom_name);
        etAdditionalNotes = findViewById(R.id.et_additional_notes);
        sliderIntensity = findViewById(R.id.slider_intensity);
        tvDateTime = findViewById(R.id.tv_date_time);
        btnSaveSymptom = findViewById(R.id.btn_save_symptom);
        ivClose = findViewById(R.id.iv_close);

        updateDateTimeLabel();

        ivClose.setOnClickListener(v -> finish());
        tvDateTime.setOnClickListener(v -> showDateTimePicker());

        // Set the listener for the save button
        btnSaveSymptom.setOnClickListener(v -> saveSymptom());
    }

    private void showDateTimePicker() {
        // ... (This method is fine, no changes needed)
        final Calendar currentDate = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDateTime.set(Calendar.YEAR, year);
            selectedDateTime.set(Calendar.MONTH, month);
            selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedDateTime.set(Calendar.MINUTE, minute);
                updateDateTimeLabel();
            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false).show();
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    }

    private void updateDateTimeLabel() {
        // ... (This method is fine, no changes needed)
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());
        tvDateTime.setText(sdf.format(selectedDateTime.getTime()));
    }

    private void saveSymptom() {
        Log.d(TAG, "saveSymptom called."); // Debug log

        String symptomName = etSymptomName.getText().toString().trim();
        if (TextUtils.isEmpty(symptomName)) {
            Log.d(TAG, "Validation failed: Symptom name is empty.");
            etSymptomName.setError("Symptom name is required");
            etSymptomName.requestFocus();
            return;
        }

        int intensity = (int) sliderIntensity.getValue();
        long timestamp = selectedDateTime.getTimeInMillis();
        String additionalNotes = etAdditionalNotes.getText().toString().trim();

        Log.d(TAG, "Data validated. Attempting to get Firebase key.");
        String symptomId = databaseReference.push().getKey();

        // **IMPROVED CHECK**: Handle the case where the key is null
        if (symptomId != null) {
            Log.d(TAG, "Successfully generated symptom ID: " + symptomId);
            Symptom newSymptom = new Symptom(symptomName, intensity, timestamp, additionalNotes);

            databaseReference.child(symptomId).setValue(newSymptom)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Firebase setValue was successful.");
                        Toast.makeText(LogSymptomActivity.this, "Symptom saved successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Firebase setValue failed.", e);
                        Toast.makeText(LogSymptomActivity.this, "Failed to save symptom: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            // **USER FEEDBACK**: Inform the user that the connection failed
            Log.e(TAG, "Could not generate symptom ID. Firebase key is null.");
            Toast.makeText(this, "Could not connect to database. Please check your network connection and try again.", Toast.LENGTH_LONG).show();
        }
    }
}