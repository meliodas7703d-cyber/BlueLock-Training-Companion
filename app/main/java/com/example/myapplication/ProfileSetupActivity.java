package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileSetupActivity extends AppCompatActivity {

    private EditText etName;
    private EditText etAge;

    private Spinner spinnerPosition;
    private Spinner spinnerExperience;
    private Spinner spinnerFoot;
    private Spinner spinnerGoal;

    private Button btnSaveProfile;
    private TextView tvProfileStatus;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private boolean isSaving = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile_setup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);

        spinnerPosition = findViewById(R.id.spinnerPosition);
        spinnerExperience = findViewById(R.id.spinnerExperience);
        spinnerFoot = findViewById(R.id.spinnerFoot);
        spinnerGoal = findViewById(R.id.spinnerGoal);

        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        tvProfileStatus = findViewById(R.id.tvProfileStatus);

        setupSpinners();
        loadUserName();

        btnSaveProfile.setOnClickListener(
                v -> saveProfile()
        );
    }

    private void setupSpinners() {

        String[] positions = {
                "Select Position",
                "Striker",
                "Winger",
                "Attacking Midfielder",
                "Central Midfielder",
                "Defensive Midfielder",
                "Full Back",
                "Centre Back",
                "Goalkeeper"
        };

        String[] experiences = {
                "Select Experience",
                "Beginner",
                "1-2 Years",
                "3-5 Years",
                "5+ Years"
        };

        String[] feet = {
                "Select Preferred Foot",
                "Right",
                "Left",
                "Both"
        };

        String[] goals = {
                "Select Training Goal",
                "Improve Shooting",
                "Improve Passing",
                "Improve Dribbling",
                "Improve Ball Control",
                "Improve Speed",
                "Improve Stamina",
                "Improve Finishing",
                "Overall Development"
        };

        setSpinnerAdapter(
                spinnerPosition,
                positions
        );

        setSpinnerAdapter(
                spinnerExperience,
                experiences
        );

        setSpinnerAdapter(
                spinnerFoot,
                feet
        );

        setSpinnerAdapter(
                spinnerGoal,
                goals
        );
    }

    private void setSpinnerAdapter(
            Spinner spinner,
            String[] items
    ) {

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        R.layout.spinner_item,
                        items
                );

        adapter.setDropDownViewResource(
                R.layout.spinner_dropdown_item
        );

        spinner.setAdapter(adapter);
    }

    private void loadUserName() {

        FirebaseUser user =
                mAuth.getCurrentUser();

        if (user != null) {
            if (!TextUtils.isEmpty(user.getDisplayName())) {
                etName.setText(user.getDisplayName());
            }

            Map<String, Object> saved = ProfilePreferences.getProfile(this, user.getUid());
            if (saved != null) {
                String name = (String) saved.get("name");
                if (!TextUtils.isEmpty(name)) {
                    etName.setText(name);
                }

                Object ageObj = saved.get("age");
                if (ageObj instanceof Integer && ((Integer) ageObj) > 0) {
                    etAge.setText(String.valueOf(ageObj));
                }

                setSpinnerValue(spinnerPosition, (String) saved.get("position"));
                setSpinnerValue(spinnerExperience, (String) saved.get("experience"));
                setSpinnerValue(spinnerFoot, (String) saved.get("preferredFoot"));
                setSpinnerValue(spinnerGoal, (String) saved.get("trainingGoal"));
            }
        }
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        if (TextUtils.isEmpty(value)) return;
        for (int i = 0; i < spinner.getCount(); i++) {
            Object itemObject = spinner.getItemAtPosition(i);
            if (itemObject != null && itemObject.toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void saveProfile() {

        if (isSaving) {
            return;
        }

        String name =
                etName.getText()
                        .toString()
                        .trim();

        String ageText =
                etAge.getText()
                        .toString()
                        .trim();

        // NAME
        if (TextUtils.isEmpty(name)) {

            etName.setError(
                    "Enter your name"
            );

            etName.requestFocus();
            return;
        }

        // AGE
        if (TextUtils.isEmpty(ageText)) {

            etAge.setError(
                    "Enter your age"
            );

            etAge.requestFocus();
            return;
        }

        int age;

        try {

            age =
                    Integer.parseInt(ageText);

        } catch (NumberFormatException e) {

            etAge.setError(
                    "Enter a valid age"
            );

            etAge.requestFocus();
            return;
        }

        if (age < 10 || age > 80) {

            etAge.setError(
                    "Enter a valid age"
            );

            etAge.requestFocus();
            return;
        }

        // POSITION
        if (spinnerPosition
                .getSelectedItemPosition() == 0) {

            tvProfileStatus.setText(
                    "Please select your playing position."
            );

            return;
        }

        // EXPERIENCE
        if (spinnerExperience
                .getSelectedItemPosition() == 0) {

            tvProfileStatus.setText(
                    "Please select your experience."
            );

            return;
        }

        // FOOT
        if (spinnerFoot
                .getSelectedItemPosition() == 0) {

            tvProfileStatus.setText(
                    "Please select your preferred foot."
            );

            return;
        }

        // GOAL
        if (spinnerGoal
                .getSelectedItemPosition() == 0) {

            tvProfileStatus.setText(
                    "Please select your training goal."
            );

            return;
        }

        // CURRENT USER
        FirebaseUser user =
                mAuth.getCurrentUser();

        if (user == null) {

            tvProfileStatus.setText(
                    "Login session not found. Please login again."
            );

            return;
        }

        // SPINNER VALUES
        String position =
                spinnerPosition
                        .getSelectedItem()
                        .toString();

        String experience =
                spinnerExperience
                        .getSelectedItem()
                        .toString();

        String preferredFoot =
                spinnerFoot
                        .getSelectedItem()
                        .toString();

        String trainingGoal =
                spinnerGoal
                        .getSelectedItem()
                        .toString();

        // FIRESTORE DATA
        Map<String, Object> profile =
                new HashMap<>();

        profile.put(
                "uid",
                user.getUid()
        );

        profile.put(
                "name",
                name
        );

        profile.put(
                "age",
                age
        );

        profile.put(
                "position",
                position
        );

        profile.put(
                "experience",
                experience
        );

        profile.put(
                "preferredFoot",
                preferredFoot
        );

        profile.put(
                "trainingGoal",
                trainingGoal
        );

        profile.put(
                "updatedAt",
                System.currentTimeMillis()
        );

        // Save locally first so profile data is never lost
        ProfilePreferences.saveProfile(
                this,
                user.getUid(),
                name,
                age,
                position,
                experience,
                preferredFoot,
                trainingGoal,
                ""
        );

        isSaving = true;

        btnSaveProfile.setEnabled(false);

        tvProfileStatus.setText(
                "Saving..."
        );

        /*
         * FIRESTORE SAVE
         */
        db.collection("users")
                .document(user.getUid())
                .collection("profile")
                .document("main")
                .set(profile)
                .addOnSuccessListener(unused -> openDashboard())
                .addOnFailureListener(e -> {
                    // Profile saved locally, open dashboard anyway
                    android.widget.Toast.makeText(
                            ProfileSetupActivity.this,
                            "Profile saved successfully!",
                            android.widget.Toast.LENGTH_SHORT
                    ).show();
                    openDashboard();
                });
    }

    private void openDashboard() {

        Intent intent =
                new Intent(
                        ProfileSetupActivity.this,
                        DashboardActivity.class
                );

        startActivity(intent);

        finish();
    }

    @Override
    protected void onDestroy() {

        /*
         * Profile screen ko koi loader nahi chala raha,
         * isliye LoadingUtil ki zarurat nahi.
         */
        super.onDestroy();
    }
}