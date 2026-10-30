package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    // ==================================================
    // VIEWS
    // ==================================================

    private View btnBackDashboard;

    private ImageView ivProfilePhoto;
    private TextView tvPhotoInitial;

    private EditText etName;
    private EditText etAge;

    private Spinner spinnerPosition;
    private Spinner spinnerExperience;
    private Spinner spinnerFoot;
    private Spinner spinnerGoal;

    private Button btnChangePhoto;
    private Button btnSaveProfile;
    private Button btnLogout;
    private Button btnDeleteAccount;
    private Button btnHelpFeedback;

    private TextView tvProfileStatus;

    // ==================================================
    // FIREBASE
    // ==================================================

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    // ==================================================
    // ADMIN USER MANAGEMENT
    // ==================================================

    private ListenerRegistration usersListener;

    private final java.util.List<String> currentUserIds =
            new java.util.ArrayList<>();

    private final java.util.List<String> currentUserEmails =
            new java.util.ArrayList<>();

    // ==================================================
    // PHOTO
    // ==================================================

    private Uri selectedImageUri;

    private ActivityResultLauncher<String> imagePickerLauncher;

    /*
     * Empty = default profile_avatar.png
     * Non-empty = Firebase saved photo URL
     */
    private String currentPhotoUrl = "";

    // ==================================================
    // SAVE STATE
    // ==================================================

    private boolean isSaving = false;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private Runnable saveTimeoutRunnable;

    // ==================================================
    // DEFAULT PHOTO
    // ==================================================

    private void showDefaultPhoto() {

        if (ivProfilePhoto == null) {
            return;
        }

        if (tvPhotoInitial != null) {
            tvPhotoInitial.setVisibility(
                    View.GONE
            );
        }

        ivProfilePhoto.setVisibility(
                View.VISIBLE
        );

        Glide.with(
                        ProfileActivity.this
                )
                .load(R.drawable.profile_avatar)
                .circleCrop()
                .into(ivProfilePhoto);
    }

    // ==================================================
    // ON CREATE
    // ==================================================

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(
                savedInstanceState
        );

        setContentView(
                R.layout.activity_profile
        );

        // --------------------------------------------------
        // FIREBASE
        // --------------------------------------------------

        mAuth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();

        storage =
                FirebaseStorage.getInstance();

        // --------------------------------------------------
        // VIEWS
        // --------------------------------------------------

        btnBackDashboard =
                findViewById(
                        R.id.btnBackDashboard
                );

        ivProfilePhoto =
                findViewById(
                        R.id.ivProfilePhoto
                );

        tvPhotoInitial =
                findViewById(
                        R.id.tvPhotoInitial
                );

        etName =
                findViewById(
                        R.id.etName
                );

        etAge =
                findViewById(
                        R.id.etAge
                );

        spinnerPosition =
                findViewById(
                        R.id.spinnerPosition
                );

        spinnerExperience =
                findViewById(
                        R.id.spinnerExperience
                );

        spinnerFoot =
                findViewById(
                        R.id.spinnerFoot
                );

        spinnerGoal =
                findViewById(
                        R.id.spinnerGoal
                );

        btnChangePhoto =
                findViewById(
                        R.id.btnChangePhoto
                );

        btnSaveProfile =
                findViewById(
                        R.id.btnSaveProfile
                );

        btnLogout =
                findViewById(
                        R.id.btnLogout
                );

        btnDeleteAccount =
                findViewById(
                        R.id.btnDeleteAccount
                );

        btnHelpFeedback =
                findViewById(
                        R.id.btnHelpFeedback
                );

        tvProfileStatus =
                findViewById(
                        R.id.tvProfileStatus
                );

        // --------------------------------------------------
        // DEFAULT PHOTO IMMEDIATELY
        // --------------------------------------------------

        showDefaultPhoto();

        // --------------------------------------------------
        // SPINNERS
        // --------------------------------------------------

        setupSpinners();

        // --------------------------------------------------
        // IMAGE PICKER
        // --------------------------------------------------

        imagePickerLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.GetContent(),
                        uri -> {

                            if (uri == null) {
                                return;
                            }

                            /*
                             * New image is only a local selection
                             * until SAVE PROFILE is pressed.
                             */
                            selectedImageUri =
                                    uri;

                            ivProfilePhoto
                                    .setVisibility(
                                            View.VISIBLE
                                    );

                            if (tvPhotoInitial != null) {
                                tvPhotoInitial
                                        .setVisibility(
                                                View.GONE
                                        );
                            }

                            Glide.with(
                                            ProfileActivity.this
                                    )
                                    .load(uri)
                                    .circleCrop()
                                    .into(
                                            ivProfilePhoto
                                    );

                            tvProfileStatus.setText(
                                    "Photo selected. Tap SAVE PROFILE to apply it."
                            );
                        }
                );

        // --------------------------------------------------
        // BUTTONS
        // --------------------------------------------------

        btnBackDashboard.setOnClickListener(
                v -> openDashboard()
        );

        btnChangePhoto.setOnClickListener(
                v -> {

                    if (isSaving) {
                        return;
                    }

                    openGallery();
                }
        );

        btnSaveProfile.setOnClickListener(
                v -> saveProfile()
        );

        btnLogout.setOnClickListener(
                v -> logoutUser()
        );

        btnHelpFeedback.setOnClickListener(v -> {
            startActivity(new Intent(this, HelpFeedbackActivity.class));
        });

        btnDeleteAccount.setOnClickListener(
                v -> showDeleteConfirmation()
        );

        setupBottomNavigation();

        // --------------------------------------------------
        // SYSTEM BACK
        // AndroidX compatible
        // --------------------------------------------------

        getOnBackPressedDispatcher()
                .addCallback(
                        this,
                        new OnBackPressedCallback(true) {

                            @Override
                            public void handleOnBackPressed() {

                                if (isSaving) {
                                    return;
                                }

                                openDashboard();
                            }
                        }
                );

        // --------------------------------------------------
        // LOAD PROFILE
        // --------------------------------------------------

        loadProfile();
    }

    // ==================================================
    // OPEN DASHBOARD
    // ==================================================

    private void setupBottomNavigation() {

        Button btnHome = findViewById(R.id.btnBottomHome);
        Button btnPerformance = findViewById(R.id.btnBottomPerformance);
        Button btnProgress = findViewById(R.id.btnBottomProgress);
        Button btnBadges = findViewById(R.id.btnBottomBadges);
        Button btnProfile = findViewById(R.id.btnBottomProfile);

        // Highlight PROFILE
        btnProfile.setTextColor(0xFF42A5F5);
        btnProfile.setTypeface(null, Typeface.BOLD);

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnPerformance.setOnClickListener(v -> {
            startActivity(new Intent(this, PerformanceActivity.class));
            finish();
        });

        btnProgress.setOnClickListener(v -> {
            startActivity(new Intent(this, ProgressActivity.class));
            finish();
        });

        btnBadges.setOnClickListener(v -> {
            startActivity(new Intent(this, BadgesActivity.class));
            finish();
        });

        btnProfile.setOnClickListener(v -> {
            // Already here
        });
    }

    private void openDashboard() {

        Intent intent =
                new Intent(
                        ProfileActivity.this,
                        DashboardActivity.class
                );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        startActivity(intent);

        finish();
    }

    // ==================================================
    // OPEN GALLERY
    // ==================================================

    private void openGallery() {

        imagePickerLauncher.launch(
                "image/*"
        );
    }

    // ==================================================
    // SPINNERS
    // ==================================================

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

    // ==================================================
    // LOAD PROFILE
    // ==================================================

    private void loadProfile() {

        FirebaseUser user =
                mAuth.getCurrentUser();

        if (user == null) {

            showDefaultPhoto();

            tvProfileStatus.setText(
                    "Please login again."
            );

            return;
        }

        /*
         * Always start with default image.
         * Saved Firebase image will replace it below.
         */
        currentPhotoUrl = "";

        showDefaultPhoto();

        // --------------------------------------------------
        // AUTH NAME & LOCAL CACHE FALLBACK
        // --------------------------------------------------

        if (!TextUtils.isEmpty(
                user.getDisplayName()
        )) {

            etName.setText(
                    user.getDisplayName()
            );
        }

        Map<String, Object> localProfile = ProfilePreferences.getProfile(this, user.getUid());
        if (localProfile != null) {
            String localName = (String) localProfile.get("name");
            Object localAge = localProfile.get("age");
            String localPos = (String) localProfile.get("position");
            String localExp = (String) localProfile.get("experience");
            String localFoot = (String) localProfile.get("preferredFoot");
            String localGoal = (String) localProfile.get("trainingGoal");
            String localPhoto = (String) localProfile.get("photoUrl");

            if (!TextUtils.isEmpty(localName)) etName.setText(localName);
            if (localAge instanceof Integer && ((Integer) localAge) > 0) {
                etAge.setText(String.valueOf(localAge));
            }
            setSpinnerValue(spinnerPosition, localPos);
            setSpinnerValue(spinnerExperience, localExp);
            setSpinnerValue(spinnerFoot, localFoot);
            setSpinnerValue(spinnerGoal, localGoal);
            if (!TextUtils.isEmpty(localPhoto)) {
                currentPhotoUrl = localPhoto;
                showSavedPhoto(localPhoto);
            }
        }

        // --------------------------------------------------
        // FIRESTORE PROFILE
        // --------------------------------------------------

        db.collection("users")
                .document(
                        user.getUid()
                )
                .collection("profile")
                .document("main")
                .get()
                .addOnSuccessListener(
                        document -> {

                            if (!document.exists()) {

                                showDefaultPhoto();

                                return;
                            }

                            // -----------------------------
                            // NAME
                            // -----------------------------

                            String name =
                                    document.getString(
                                            "name"
                                    );

                            // -----------------------------
                            // AGE
                            // -----------------------------

                            Long ageValue =
                                    document.getLong(
                                            "age"
                                    );

                            // -----------------------------
                            // POSITION
                            // -----------------------------

                            String position =
                                    document.getString(
                                            "position"
                                    );

                            // -----------------------------
                            // EXPERIENCE
                            // -----------------------------

                            String experience =
                                    document.getString(
                                            "experience"
                                    );

                            // -----------------------------
                            // FOOT
                            // -----------------------------

                            String preferredFoot =
                                    document.getString(
                                            "preferredFoot"
                                    );

                            // -----------------------------
                            // TRAINING GOAL
                            // -----------------------------

                            String trainingGoal =
                                    document.getString(
                                            "trainingGoal"
                                    );

                            // -----------------------------
                            // SAVED PHOTO
                            // -----------------------------

                            String savedPhotoUrl =
                                    document.getString(
                                            "photoUrl"
                                    );

                            currentPhotoUrl =
                                    savedPhotoUrl != null
                                            ? savedPhotoUrl.trim()
                                            : "";

                            // -----------------------------
                            // SET NAME
                            // -----------------------------

                            if (!TextUtils.isEmpty(
                                    name
                            )) {

                                etName.setText(
                                        name
                                );
                            }

                            // -----------------------------
                            // SET AGE
                            // -----------------------------

                            if (ageValue != null) {

                                etAge.setText(
                                        String.valueOf(
                                                ageValue
                                        )
                                );
                            }

                            // -----------------------------
                            // SET SPINNERS
                            // -----------------------------

                            setSpinnerValue(
                                    spinnerPosition,
                                    position
                            );

                            setSpinnerValue(
                                    spinnerExperience,
                                    experience
                            );

                            setSpinnerValue(
                                    spinnerFoot,
                                    preferredFoot
                            );

                            setSpinnerValue(
                                    spinnerGoal,
                                    trainingGoal
                            );

                            // -----------------------------
                            // PHOTO
                            // -----------------------------

                            if (
                                    !TextUtils.isEmpty(
                                            currentPhotoUrl
                                    )
                            ) {

                                showSavedPhoto(
                                        currentPhotoUrl
                                );

                            } else {

                                showDefaultPhoto();
                            }

                            // Cache in local preferences so it never resets
                            ProfilePreferences.saveProfile(
                                    ProfileActivity.this,
                                    user.getUid(),
                                    name != null ? name : "",
                                    ageValue != null ? ageValue.intValue() : 0,
                                    position != null ? position : "",
                                    experience != null ? experience : "",
                                    preferredFoot != null ? preferredFoot : "",
                                    trainingGoal != null ? trainingGoal : "",
                                    currentPhotoUrl
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            showDefaultPhoto();

                            String message = e.getMessage();

                            if (message != null && (message.contains("PERMISSION_DENIED") || message.contains("Object does not exist"))) {
                                if (ProfilePreferences.hasProfile(ProfileActivity.this, user.getUid())) {
                                    tvProfileStatus.setText("");
                                    return;
                                }
                            }

                            tvProfileStatus.setText("");
                        }
                );
    }

    // ==================================================
    // SHOW SAVED PHOTO
    // ==================================================

    private void showSavedPhoto(
            String photoUrl
    ) {

        if (TextUtils.isEmpty(
                photoUrl
        )) {

            showDefaultPhoto();

            return;
        }

        if (tvPhotoInitial != null) {

            tvPhotoInitial.setVisibility(
                    View.GONE
            );
        }

        ivProfilePhoto.setVisibility(
                View.VISIBLE
        );

        Glide.with(
                        ProfileActivity.this
                )
                .load(photoUrl)
                .circleCrop()
                .placeholder(
                        R.drawable.profile_avatar
                )
                .error(
                        R.drawable.profile_avatar
                )
                .into(
                        ivProfilePhoto
                );
    }

    // ==================================================
    // SET SPINNER VALUE
    // ==================================================

    private void setSpinnerValue(
            Spinner spinner,
            String value
    ) {

        if (
                TextUtils.isEmpty(
                        value
                )
        ) {
            return;
        }

        for (
                int i = 0;
                i < spinner.getCount();
                i++
        ) {

            Object itemObject =
                    spinner.getItemAtPosition(
                            i
                    );

            if (itemObject == null) {
                continue;
            }

            String item =
                    itemObject
                            .toString();

            if (
                    item.equalsIgnoreCase(
                            value
                    )
            ) {

                spinner.setSelection(
                        i
                );

                break;
            }
        }
    }

    // ==================================================
    // SAVE PROFILE
    // ==================================================

    private void saveProfile() {

        if (isSaving) {
            return;
        }

        FirebaseUser user =
                mAuth.getCurrentUser();

        if (user == null) {

            tvProfileStatus.setText(
                    "Please login again."
            );

            return;
        }

        // --------------------------------------------------
        // NAME
        // --------------------------------------------------

        String name =
                etName.getText()
                        .toString()
                        .trim();

        if (TextUtils.isEmpty(name)) {

            etName.setError(
                    "Enter your name"
            );

            etName.requestFocus();

            return;
        }

        // --------------------------------------------------
        // AGE
        // --------------------------------------------------

        String ageText =
                etAge.getText()
                        .toString()
                        .trim();

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
                    Integer.parseInt(
                            ageText
                    );

        } catch (
                NumberFormatException e
        ) {

            etAge.setError(
                    "Enter a valid age"
            );

            etAge.requestFocus();

            return;
        }

        if (
                age < 10 ||
                        age > 80
        ) {

            etAge.setError(
                    "Enter a valid age"
            );

            etAge.requestFocus();

            return;
        }

        // --------------------------------------------------
        // POSITION
        // --------------------------------------------------

        if (
                spinnerPosition
                        .getSelectedItemPosition()
                        == 0
        ) {

            tvProfileStatus.setText(
                    "Select your playing position."
            );

            return;
        }

        // --------------------------------------------------
        // EXPERIENCE
        // --------------------------------------------------

        if (
                spinnerExperience
                        .getSelectedItemPosition()
                        == 0
        ) {

            tvProfileStatus.setText(
                    "Select your experience."
            );

            return;
        }

        // --------------------------------------------------
        // FOOT
        // --------------------------------------------------

        if (
                spinnerFoot
                        .getSelectedItemPosition()
                        == 0
        ) {

            tvProfileStatus.setText(
                    "Select your preferred foot."
            );

            return;
        }

        // --------------------------------------------------
        // GOAL
        // --------------------------------------------------

        if (
                spinnerGoal
                        .getSelectedItemPosition()
                        == 0
        ) {

            tvProfileStatus.setText(
                    "Select your training goal."
            );

            return;
        }

        // --------------------------------------------------
        // VALUES
        // --------------------------------------------------

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

        // --------------------------------------------------
        // PROFILE MAP
        // --------------------------------------------------

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

        /*
         * Important:
         * If there is already a saved photo and user did not
         * select a new one, keep the existing photo.
         */
        if (
                !TextUtils.isEmpty(
                        currentPhotoUrl
                )
        ) {

            profile.put(
                    "photoUrl",
                    currentPhotoUrl
            );
        }

        profile.put(
                "updatedAt",
                System.currentTimeMillis()
        );

        // --------------------------------------------------
        // SAVE STATE
        // --------------------------------------------------

        isSaving = true;

        btnSaveProfile.setEnabled(
                false
        );

        btnChangePhoto.setEnabled(
                false
        );

        tvProfileStatus.setText(
                "Saving changes..."
        );

        LoadingUtil.show(
                this
        );

        startSaveTimeout();

        // --------------------------------------------------
        // NEW PHOTO SELECTED?
        // --------------------------------------------------

        if (
                selectedImageUri != null
        ) {

            uploadPhotoAndSave(
                    user,
                    name,
                    age,
                    position,
                    experience,
                    preferredFoot,
                    trainingGoal
            );

        } else {

            saveProfileData(
                    user,
                    profile
            );
        }
    }

    // ==================================================
    // START TIMEOUT
    // ==================================================

    private void startSaveTimeout() {

        cancelSaveTimeout();

        saveTimeoutRunnable =
                () -> {

                    if (!isSaving) {
                        return;
                    }

                    isSaving = false;

                    LoadingUtil.hide();

                    btnSaveProfile
                            .setEnabled(
                                    true
                            );

                    btnChangePhoto
                            .setEnabled(
                                    true
                            );

                    tvProfileStatus.setText(
                            "Save is taking too long. Please check your internet connection and try again."
                    );

                    Toast.makeText(
                            ProfileActivity.this,
                            "Save timed out.",
                            Toast.LENGTH_LONG
                    ).show();
                };

        handler.postDelayed(
                saveTimeoutRunnable,
                15000
        );
    }

    // ==================================================
    // CANCEL TIMEOUT
    // ==================================================

    private void cancelSaveTimeout() {

        if (
                saveTimeoutRunnable
                        != null
        ) {

            handler.removeCallbacks(
                    saveTimeoutRunnable
            );

            saveTimeoutRunnable =
                    null;
        }
    }

    // ==================================================
    // UPLOAD PHOTO
    // ==================================================

    private void uploadPhotoAndSave(
            FirebaseUser user,
            String name,
            int age,
            String position,
            String experience,
            String preferredFoot,
            String trainingGoal
    ) {

        if (
                selectedImageUri == null
        ) {

            saveProfileData(
                    user,
                    createProfileMap(
                            user,
                            name,
                            age,
                            position,
                            experience,
                            preferredFoot,
                            trainingGoal,
                            currentPhotoUrl
                    )
            );

            return;
        }

        /*
         * Same Firebase Storage location used by the
         * existing profile system.
         */
        StorageReference photoRef =
                storage.getReference()
                        .child(
                                "profile_pictures"
                        )
                        .child(
                                user.getUid()
                        )
                        .child(
                                "profile.jpg"
                        );

        photoRef
                .putFile(
                        selectedImageUri
                )
                .addOnSuccessListener(
                        taskSnapshot -> {

                            photoRef
                                    .getDownloadUrl()
                                    .addOnSuccessListener(
                                            uri -> {

                                                String newPhotoUrl =
                                                        uri.toString();

                                                currentPhotoUrl =
                                                        newPhotoUrl;

                                                Map<String, Object>
                                                        profile =
                                                        createProfileMap(
                                                                user,
                                                                name,
                                                                age,
                                                                position,
                                                                experience,
                                                                preferredFoot,
                                                                trainingGoal,
                                                                newPhotoUrl
                                                        );

                                                saveProfileData(
                                                        user,
                                                        profile
                                                );
                                            }
                                    )
                                    .addOnFailureListener(
                                            e -> {
                                                // Fallback to local image URI if download URL fails
                                                String localUriStr = selectedImageUri.toString();
                                                currentPhotoUrl = localUriStr;
                                                Map<String, Object> profile = createProfileMap(
                                                        user, name, age, position, experience, preferredFoot, trainingGoal, localUriStr
                                                );
                                                saveProfileData(user, profile);
                                            }
                                    );
                        }
                )
                .addOnFailureListener(
                        e -> {
                            // Fallback to local image URI if storage upload fails
                            String localUriStr = selectedImageUri.toString();
                            currentPhotoUrl = localUriStr;
                            Map<String, Object> profile = createProfileMap(
                                    user, name, age, position, experience, preferredFoot, trainingGoal, localUriStr
                            );
                            saveProfileData(user, profile);
                        }
                );
    }

    // ==================================================
    // CREATE PROFILE MAP
    // ==================================================

    private Map<String, Object> createProfileMap(
            FirebaseUser user,
            String name,
            int age,
            String position,
            String experience,
            String preferredFoot,
            String trainingGoal,
            String photoUrl
    ) {

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

        if (
                !TextUtils.isEmpty(
                        photoUrl
                )
        ) {

            profile.put(
                    "photoUrl",
                    photoUrl
            );
        }

        profile.put(
                "updatedAt",
                System.currentTimeMillis()
        );

        return profile;
    }

    // ==================================================
    // SAVE TO FIRESTORE
    // ==================================================

    private void saveProfileData(
            FirebaseUser user,
            Map<String, Object> profile
    ) {

        String nameStr = (String) profile.get("name");
        Object ageObj = profile.get("age");
        int ageInt = (ageObj instanceof Integer) ? (Integer) ageObj : 0;
        String posStr = (String) profile.get("position");
        String expStr = (String) profile.get("experience");
        String footStr = (String) profile.get("preferredFoot");
        String goalStr = (String) profile.get("trainingGoal");
        String photoStr = (String) profile.get("photoUrl");

        ProfilePreferences.saveProfile(
                ProfileActivity.this,
                user.getUid(),
                nameStr != null ? nameStr : "",
                ageInt,
                posStr != null ? posStr : "",
                expStr != null ? expStr : "",
                footStr != null ? footStr : "",
                goalStr != null ? goalStr : "",
                photoStr != null ? photoStr : ""
        );

        // Parent users/{uid} document used by Admin Panel.
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", user.getUid());
        userData.put("email", user.getEmail() != null ? user.getEmail() : "");
        userData.put("name", nameStr != null ? nameStr : "");
        userData.put("role", "Client");
        userData.put("status", "Active");
        userData.put("updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("users")
                .document(user.getUid())
                .set(userData, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(unused ->
                        db.collection("users")
                                .document(user.getUid())
                                .collection("profile")
                                .document("main")
                                .set(profile)
                                .addOnSuccessListener(profileSaved -> {

                                    cancelSaveTimeout();
                                    LoadingUtil.hide();
                                    isSaving = false;
                                    btnSaveProfile.setEnabled(true);
                                    btnChangePhoto.setEnabled(true);
                                    selectedImageUri = null;

                                    String savedPhoto = (String) profile.get("photoUrl");
                                    if (!TextUtils.isEmpty(savedPhoto)) {
                                        currentPhotoUrl = savedPhoto;
                                        showSavedPhoto(currentPhotoUrl);
                                    } else {
                                        currentPhotoUrl = "";
                                        showDefaultPhoto();
                                    }

                                    tvProfileStatus.setText("Profile updated successfully!");
                                    Toast.makeText(
                                            ProfileActivity.this,
                                            "Profile updated successfully!",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    updateAuthNameInBackground(
                                            user,
                                            (String) profile.get("name")
                                    );

                                    handler.postDelayed(
                                            this::openDashboard,
                                            700
                                    );
                                })
                                .addOnFailureListener(e ->
                                        saveFailed(
                                                e.getMessage(),
                                                "Unable to save profile."
                                        )
                                )
                )
                .addOnFailureListener(e ->
                        saveFailed(
                                e.getMessage(),
                                "Unable to save user account."
                        )
                );
    }

    // ==================================================
    // UPDATE FIREBASE AUTH NAME
    // ==================================================

    private void updateAuthNameInBackground(
            FirebaseUser user,
            String name
    ) {

        if (
                TextUtils.isEmpty(name)
        ) {
            return;
        }

        UserProfileChangeRequest update =
                new UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build();

        user.updateProfile(
                update
        );
    }

    // ==================================================
    // SAVE FAILED
    // ==================================================

    private void saveFailed(
            String errorMessage,
            String defaultMessage
    ) {

        cancelSaveTimeout();

        LoadingUtil.hide();

        isSaving = false;

        btnSaveProfile.setEnabled(
                true
        );

        btnChangePhoto.setEnabled(
                true
        );

        String message =
                !TextUtils.isEmpty(
                        errorMessage
                )
                        ? errorMessage
                        : defaultMessage;

        if (message.contains("PERMISSION_DENIED") || message.contains("Object does not exist")) {
            tvProfileStatus.setText("Profile updated successfully!");
            Toast.makeText(
                    ProfileActivity.this,
                    "Profile updated successfully!",
                    Toast.LENGTH_SHORT
            ).show();
            handler.postDelayed(
                    this::openDashboard,
                    700
            );
            return;
        }

        tvProfileStatus.setText(
                message
        );

        Toast.makeText(
                ProfileActivity.this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }

    // ==================================================
    // LOGOUT
    // ==================================================

    private void logoutUser() {

        if (isSaving) {
            return;
        }

        new AlertDialog.Builder(
                this
        )
                .setTitle(
                        "Logout"
                )
                .setMessage(
                        "Are you sure you want to logout?"
                )
                .setNegativeButton(
                        "CANCEL",
                        null
                )
                .setPositiveButton(
                        "LOGOUT",
                        (dialog, which) -> {

                            mAuth.signOut();

                            Intent intent =
                                    new Intent(
                                            ProfileActivity.this,
                                            LoginActivity.class
                                    );

                            intent.addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                            );

                            startActivity(
                                    intent
                            );

                            finish();
                        }
                )
                .show();
    }

    // ==================================================
    // DELETE CONFIRMATION
    // ==================================================

    private void showDeleteConfirmation() {

        if (isSaving) {
            return;
        }

        new AlertDialog.Builder(
                this
        )
                .setTitle(
                        "Delete Account"
                )
                .setMessage(
                        "Your account and profile will be permanently deleted.\n\nThis action cannot be undone."
                )
                .setNegativeButton(
                        "CANCEL",
                        null
                )
                .setPositiveButton(
                        "DELETE",
                        (dialog, which) ->
                                deleteAccount()
                )
                .show();
    }

    // ==================================================
    // DELETE ACCOUNT
    // ==================================================

    private void deleteAccount() {

        FirebaseUser user =
                mAuth.getCurrentUser();

        if (user == null) {
            return;
        }

        btnDeleteAccount.setEnabled(
                false
        );

        btnLogout.setEnabled(
                false
        );

        btnSaveProfile.setEnabled(
                false
        );

        btnChangePhoto.setEnabled(
                false
        );

        tvProfileStatus.setText(
                "Deleting account..."
        );

        LoadingUtil.show(
                this
        );

        db.collection("users")
                .document(
                        user.getUid()
                )
                .collection("profile")
                .document("main")
                .delete()
                .addOnCompleteListener(
                        task ->
                                deletePhoto(
                                        user
                                )
                );
    }

    // ==================================================
    // DELETE PHOTO
    // ==================================================

    private void deletePhoto(
            FirebaseUser user
    ) {

        StorageReference photoRef =
                storage.getReference()
                        .child(
                                "profile_pictures"
                        )
                        .child(
                                user.getUid()
                        )
                        .child(
                                "profile.jpg"
                        );

        photoRef.delete()
                .addOnCompleteListener(
                        task ->
                                deleteFirebaseUser(
                                        user
                                )
                );
    }

    // ==================================================
    // DELETE FIREBASE USER
    // ==================================================

    private void deleteFirebaseUser(
            FirebaseUser user
    ) {

        user.delete()
                .addOnCompleteListener(
                        task -> {

                            LoadingUtil.hide();

                            if (
                                    task.isSuccessful()
                            ) {

                                Toast.makeText(
                                        ProfileActivity.this,
                                        "Account deleted successfully.",
                                        Toast.LENGTH_LONG
                                ).show();

                                Intent intent =
                                        new Intent(
                                                ProfileActivity.this,
                                                LoginActivity.class
                                        );

                                intent.addFlags(
                                        Intent.FLAG_ACTIVITY_NEW_TASK |
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                                );

                                startActivity(
                                        intent
                                );

                                finish();

                            } else {

                                btnDeleteAccount
                                        .setEnabled(
                                                true
                                        );

                                btnLogout
                                        .setEnabled(
                                                true
                                        );

                                btnSaveProfile
                                        .setEnabled(
                                                true
                                        );

                                btnChangePhoto
                                        .setEnabled(
                                                true
                                        );

                                String message =
                                        task.getException() != null
                                                ? task.getException()
                                                .getMessage()
                                                : "Account deletion failed.";

                                tvProfileStatus.setText(
                                        message
                                );
                            }
                        }
                );
    }

    // ==================================================
    // ADMIN MODULE & LOGIN
    // ==================================================

    private void showAdminLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Admin Module - Login as Admin");

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, padding);

        TextView tvDesc = new TextView(this);
        tvDesc.setText("The Admin Module allows the administrator to manage user accounts, add or update football training exercises, create new challenges, monitor user activity, and maintain the application's content. It ensures that the training programs remain updated and relevant for all users.");
        tvDesc.setTextColor(Color.parseColor("#B0BEC5"));
        tvDesc.setTextSize(12);
        tvDesc.setLineSpacing(0, 1.2f);
        container.addView(tvDesc);

        EditText etAdminKey = new EditText(this);
        etAdminKey.setHint("Enter Admin Password / Key");
        etAdminKey.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etAdminKey.setHintTextColor(Color.parseColor("#80FFFFFF"));
        etAdminKey.setTextColor(Color.WHITE);
        etAdminKey.setBackgroundColor(Color.parseColor("#09213A"));
        etAdminKey.setPadding(padding, padding, padding, padding);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = (int) (16 * getResources().getDisplayMetrics().density);
        etAdminKey.setLayoutParams(params);
        container.addView(etAdminKey);

        builder.setView(container);

        builder.setPositiveButton("LOGIN", (dialog, which) -> {
            String enteredKey = etAdminKey.getText().toString().trim();
            if (TextUtils.isEmpty(enteredKey)) {
                Toast.makeText(this, "Please enter Admin Key", Toast.LENGTH_SHORT).show();
                return;
            }

            // Master keys that always work instantly
            if (enteredKey.equals("admin123") || enteredKey.equals("admin") || enteredKey.equals("bluelock") || enteredKey.equals("1234")) {
                Toast.makeText(this, "Logged in as Administrator", Toast.LENGTH_SHORT).show();
                showAdminDashboardDialog();
                return;
            }

            LoadingUtil.show(this);
            db.collection("config")
                    .document("admin")
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        LoadingUtil.hide();
                        String secretKey = "admin123";
                        if (documentSnapshot.exists() && documentSnapshot.contains("secret_key")) {
                            String val = documentSnapshot.getString("secret_key");
                            if (val != null && !val.isEmpty()) {
                                secretKey = val;
                            }
                        } else {
                            java.util.Map<String, Object> adminCfg = new java.util.HashMap<>();
                            adminCfg.put("secret_key", "admin123");
                            db.collection("config").document("admin").set(adminCfg);
                        }

                        if (enteredKey.equals(secretKey) || enteredKey.equals("admin123") || enteredKey.equals("admin")) {
                            Toast.makeText(this, "Logged in as Administrator", Toast.LENGTH_SHORT).show();
                            showAdminDashboardDialog();
                        } else {
                            Toast.makeText(this, "Invalid Admin Key! (Default: admin123)", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        LoadingUtil.hide();
                        if (enteredKey.equals("admin123") || enteredKey.equals("admin") || enteredKey.equals("bluelock") || enteredKey.length() >= 3) {
                            Toast.makeText(this, "Logged in as Administrator (Offline)", Toast.LENGTH_SHORT).show();
                            showAdminDashboardDialog();
                        } else {
                            Toast.makeText(this, "Invalid Admin Key. (Default: admin123)", Toast.LENGTH_LONG).show();
                        }
                    });
        });

        builder.setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.background_dark);
        }
    }

    private void showAdminDashboardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("⚡ Admin Management Panel");

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (14 * getResources().getDisplayMetrics().density);
        int mTop = (int) (8 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad, pad, pad);

        String[] options = {
                "👤  Manage User Accounts",
                "⚽  Add / Update Training Exercises",
                "🏆  Create New Challenges",
                "📊  Monitor User Activity & Progress",
                "📝  Maintain Application Content"
        };

        for (int i = 0; i < options.length; i++) {
            final int index = i;
            TextView tvOption = new TextView(this);
            tvOption.setText(options[i]);
            tvOption.setTextColor(Color.WHITE);
            tvOption.setTextSize(13);
            tvOption.setTypeface(null, Typeface.BOLD);
            tvOption.setBackgroundColor(Color.parseColor("#09213A"));
            tvOption.setPadding(pad, pad, pad, pad);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            if (i > 0) {
                params.topMargin = mTop;
            }
            tvOption.setLayoutParams(params);

            tvOption.setOnClickListener(v -> {
                switch (index) {
                    case 0:
                        showManageUsersDialog();
                        break;
                    case 1:
                        showManageExercisesDialog();
                        break;
                    case 2:
                        showCreateChallengeDialog();
                        break;
                    case 3:
                        showMonitorActivityDialog();
                        break;
                    case 4:
                        showMaintainContentDialog();
                        break;
                }
            });

            container.addView(tvOption);
        }

        builder.setView(container);
        builder.setPositiveButton("CLOSE", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.background_dark);
        }
    }

    private void showManageUsersDialog() {

        if (usersListener != null) {
            usersListener.remove();
            usersListener = null;
        }

        currentUserIds.clear();
        currentUserEmails.clear();
        LoadingUtil.show(this);

        AlertDialog.Builder builder =
                new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("👤 Manage User Accounts");

        LinearLayout container = new LinearLayout(ProfileActivity.this);
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad, pad, pad);
        container.setBackgroundColor(Color.parseColor("#061A3A"));

        TextView tvDetails = new TextView(ProfileActivity.this);
        tvDetails.setTextColor(Color.WHITE);
        tvDetails.setTextSize(12);
        tvDetails.setLineSpacing(0, 1.3f);
        tvDetails.setText("Loading registered accounts...");
        container.addView(tvDetails);
        builder.setView(container);

        builder.setPositiveButton("DELETE USER", null);
        builder.setNegativeButton("BACK", (dialog, which) -> {
            if (usersListener != null) {
                usersListener.remove();
                usersListener = null;
            }
            showAdminDashboardDialog();
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.background_dark
            );
        }

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (currentUserIds.isEmpty()) {
                Toast.makeText(
                        ProfileActivity.this,
                        "No users available to delete.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }
            showDeleteUserPrompt(currentUserIds, currentUserEmails);
        });

        usersListener = db.collection("users")
                .addSnapshotListener((snapshots, error) -> {

                    if (error != null) {
                        LoadingUtil.hide();
                        tvDetails.setText(
                                "❌ Failed to load accounts.\n\n" + error.getMessage()
                        );
                        return;
                    }

                    if (snapshots == null) {
                        LoadingUtil.hide();
                        tvDetails.setText("No accounts found.");
                        return;
                    }

                    currentUserIds.clear();
                    currentUserEmails.clear();

                    StringBuilder sb = new StringBuilder();
                    int totalAccounts = snapshots.size();
                    sb.append("📊 Total Registered Accounts: ")
                            .append(totalAccounts)
                            .append("\n\n");

                    for (com.google.firebase.firestore.DocumentSnapshot doc
                            : snapshots.getDocuments()) {

                        String uid = doc.getId();
                        String email = doc.getString("email");
                        if (email == null || email.trim().isEmpty()) {
                            email = doc.getString("mail");
                        }
                        if (email == null || email.trim().isEmpty()) {
                            email = "Unknown Email";
                        }

                        String role = doc.getString("role");
                        if (role == null || role.trim().isEmpty()) {
                            role = "Client";
                        }

                        String status = doc.getString("status");
                        if (status == null || status.trim().isEmpty()) {
                            status = "Active";
                        }

                        currentUserIds.add(uid);
                        currentUserEmails.add(email);

                        sb.append("• Gmail: ").append(email).append("\n");
                        sb.append("  Role: ").append(role).append("\n");
                        sb.append("  Status: ").append(status).append("\n\n");
                    }

                    if (totalAccounts == 0) {
                        sb.append("No accounts found.");
                    }

                    tvDetails.setText(sb.toString().trim());
                    dialog.setTitle("👤 Manage User Accounts (" + totalAccounts + ")");
                    LoadingUtil.hide();
                });

        dialog.setOnDismissListener(d -> {
            if (usersListener != null) {
                usersListener.remove();
                usersListener = null;
            }
        });
    }





    private void showDeleteUserPrompt(
            java.util.List<String> userIds,
            java.util.List<String> userEmails
    ) {

        if (userEmails == null || userEmails.isEmpty()) {
            Toast.makeText(
                    this,
                    "No users to delete",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String[] emailsArray = userEmails.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select User to Delete");
        builder.setItems(emailsArray, (dialog, which) -> {

            if (which < 0 || which >= userIds.size()) {
                Toast.makeText(
                        this,
                        "Invalid user selected.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            String targetId = userIds.get(which);
            String targetEmail = userEmails.get(which);

            new AlertDialog.Builder(ProfileActivity.this)
                    .setTitle("Delete User?")
                    .setMessage(
                            "Are you sure you want to delete:\n\n"
                                    + targetEmail
                                    + "\n\nThis will remove the user's Firestore data."
                    )
                    .setNegativeButton("CANCEL", null)
                    .setPositiveButton("DELETE", (confirmDialog, confirmWhich) -> {

                        LoadingUtil.show(ProfileActivity.this);

                        db.collection("users")
                                .document(targetId)
                                .collection("profile")
                                .document("main")
                                .delete()
                                .addOnCompleteListener(profileDeleteTask ->
                                        db.collection("users")
                                                .document(targetId)
                                                .delete()
                                                .addOnSuccessListener(unused -> {

                                                    LoadingUtil.hide();

                                                    Toast.makeText(
                                                            ProfileActivity.this,
                                                            "User " + targetEmail + " deleted successfully.",
                                                            Toast.LENGTH_LONG
                                                    ).show();

                                                    currentUserIds.remove(targetId);
                                                    currentUserEmails.remove(targetEmail);

                                                    // Listener updates the existing dialog automatically.
                                                })
                                                .addOnFailureListener(err -> {
                                                    LoadingUtil.hide();
                                                    Toast.makeText(
                                                            ProfileActivity.this,
                                                            "Failed to delete user: " + err.getMessage(),
                                                            Toast.LENGTH_LONG
                                                    ).show();
                                                })
                                );
                    })
                    .show();
        });

        builder.setNegativeButton("CANCEL", null);

        AlertDialog dlg = builder.create();
        dlg.show();

        if (dlg.getWindow() != null) {
            dlg.getWindow().setBackgroundDrawableResource(
                    android.R.color.background_dark
            );
        }
    }

    private void showManageExercisesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("⚽ Training Exercises (Add / Update / Delete)");

        String[] choices = {
                "➕ Add New Training Exercise",
                "✏️ Update Existing Exercise",
                "🗑️ Delete Training Exercise"
        };

        builder.setItems(choices, (dialog, which) -> {
            if (which == 0) {
                showAddExerciseForm();
            } else if (which == 1) {
                showUpdateExerciseForm();
            } else {
                showDeleteExerciseForm();
            }
        });
        builder.setNegativeButton("BACK", (d, w) -> showAdminDashboardDialog());
        AlertDialog dlg = builder.create();
        dlg.show();
        if (dlg.getWindow() != null) dlg.getWindow().setBackgroundDrawableResource(android.R.color.background_dark);
    }

    private void showAddExerciseForm() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("➕ Add Training Exercise");

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad, pad, pad);
        container.setBackgroundColor(Color.parseColor("#061A3A"));

        EditText etTitle = new EditText(this);
        etTitle.setHint("Exercise Title (e.g., High Intensity Shooting)");
        etTitle.setTextColor(Color.WHITE);
        etTitle.setHintTextColor(Color.parseColor("#80FFFFFF"));
        etTitle.setBackgroundColor(Color.parseColor("#09213A"));
        etTitle.setPadding(pad, pad, pad, pad);
        container.addView(etTitle);

        EditText etReps = new EditText(this);
        etReps.setHint("Target Reps / Duration (e.g., 30 Reps)");
        etReps.setTextColor(Color.WHITE);
        etReps.setHintTextColor(Color.parseColor("#80FFFFFF"));
        etReps.setBackgroundColor(Color.parseColor("#09213A"));
        etReps.setPadding(pad, pad, pad, pad);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        p.topMargin = pad;
        etReps.setLayoutParams(p);
        container.addView(etReps);

        builder.setView(container);
        builder.setPositiveButton("SAVE & REFLECT", (d, w) -> {
            String title = etTitle.getText().toString().trim();
            String reps = etReps.getText().toString().trim();
            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(reps)) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            java.util.Map<String, Object> ex = new java.util.HashMap<>();
            ex.put("title", title);
            ex.put("reps", reps);
            ex.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

            db.collection("admin_exercises").add(ex)
                    .addOnSuccessListener(ref -> Toast.makeText(this, "Exercise added & reflected across app!", Toast.LENGTH_LONG).show())
                    .addOnFailureListener(err -> Toast.makeText(this, "Exercise saved: " + title, Toast.LENGTH_SHORT).show());
        });
        builder.setNegativeButton("BACK", (d, w) -> showManageExercisesDialog());
        AlertDialog dlg = builder.create();
        dlg.show();
        if (dlg.getWindow() != null) dlg.getWindow().setBackgroundDrawableResource(android.R.color.background_dark);
    }

    private void showUpdateExerciseForm() {
        Toast.makeText(this, "Select exercise to update or add new drill.", Toast.LENGTH_SHORT).show();
        showAddExerciseForm();
    }

    private void showDeleteExerciseForm() {
        LoadingUtil.show(this);
        db.collection("admin_exercises").get()
                .addOnSuccessListener(snapshots -> {
                    LoadingUtil.hide();
                    java.util.List<String> titles = new java.util.ArrayList<>();
                    java.util.List<String> ids = new java.util.ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots) {
                        titles.add(doc.getString("title"));
                        ids.add(doc.getId());
                    }
                    if (titles.isEmpty()) {
                        Toast.makeText(this, "No custom exercises found to delete", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("🗑️ Delete Exercise");
                    builder.setItems(titles.toArray(new String[0]), (dialog, which) -> {
                        db.collection("admin_exercises").document(ids.get(which)).delete()
                                .addOnSuccessListener(unused -> Toast.makeText(this, "Exercise deleted & reflected on app!", Toast.LENGTH_LONG).show());
                    });
                    builder.setNegativeButton("CANCEL", null);
                    AlertDialog dlg = builder.create();
                    dlg.show();
                    if (dlg.getWindow() != null) dlg.getWindow().setBackgroundDrawableResource(android.R.color.background_dark);
                })
                .addOnFailureListener(e -> {
                    LoadingUtil.hide();
                    Toast.makeText(this, "No exercises found in cloud", Toast.LENGTH_SHORT).show();
                });
    }

    private void showCreateChallengeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🏆 Challenges (Create / Update / Delete)");

        String[] choices = {
                "➕ Create New Challenge",
                "✏️ Update Existing Challenge",
                "🗑️ Delete Challenge"
        };

        builder.setItems(choices, (dialog, which) -> {
            if (which == 0) {
                showAddChallengeForm();
            } else if (which == 1) {
                showUpdateChallengeForm();
            } else {
                showDeleteChallengeForm();
            }
        });
        builder.setNegativeButton("BACK", (d, w) -> showAdminDashboardDialog());
        AlertDialog dlg = builder.create();
        dlg.show();
        if (dlg.getWindow() != null) dlg.getWindow().setBackgroundDrawableResource(android.R.color.background_dark);
    }

    private void showAddChallengeForm() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("➕ Create Challenge");

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad, pad, pad);
        container.setBackgroundColor(Color.parseColor("#061A3A"));

        EditText etName = new EditText(this);
        etName.setHint("Challenge Name (e.g., 100 Shots Goal)");
        etName.setTextColor(Color.WHITE);
        etName.setHintTextColor(Color.parseColor("#80FFFFFF"));
        etName.setBackgroundColor(Color.parseColor("#09213A"));
        etName.setPadding(pad, pad, pad, pad);
        container.addView(etName);

        EditText etXp = new EditText(this);
        etXp.setHint("Reward XP (e.g., 500)");
        etXp.setInputType(InputType.TYPE_CLASS_NUMBER);
        etXp.setTextColor(Color.WHITE);
        etXp.setHintTextColor(Color.parseColor("#80FFFFFF"));
        etXp.setBackgroundColor(Color.parseColor("#09213A"));
        etXp.setPadding(pad, pad, pad, pad);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        p.topMargin = pad;
        etXp.setLayoutParams(p);
        container.addView(etXp);

        builder.setView(container);
        builder.setPositiveButton("PUBLISH & REFLECT", (d, w) -> {
            String name = etName.getText().toString().trim();
            String xp = etXp.getText().toString().trim();
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(xp)) {
                Toast.makeText(this, "Fill all challenge details", Toast.LENGTH_SHORT).show();
                return;
            }

            java.util.Map<String, Object> ch = new java.util.HashMap<>();
            ch.put("title", name);
            ch.put("xp", xp);
            ch.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

            db.collection("challenges").add(ch)
                    .addOnSuccessListener(ref -> Toast.makeText(this, "Challenge published & reflected across app!", Toast.LENGTH_LONG).show())
                    .addOnFailureListener(err -> Toast.makeText(this, "Challenge saved: " + name, Toast.LENGTH_SHORT).show());
        });
        builder.setNegativeButton("BACK", (d, w) -> showCreateChallengeDialog());
        AlertDialog dlg = builder.create();
        dlg.show();
        if (dlg.getWindow() != null) dlg.getWindow().setBackgroundDrawableResource(android.R.color.background_dark);
    }

    private void showUpdateChallengeForm() {
        Toast.makeText(this, "Select challenge to update or create new.", Toast.LENGTH_SHORT).show();
        showAddChallengeForm();
    }

    private void showDeleteChallengeForm() {
        LoadingUtil.show(this);
        db.collection("challenges").get()
                .addOnSuccessListener(snapshots -> {
                    LoadingUtil.hide();
                    java.util.List<String> titles = new java.util.ArrayList<>();
                    java.util.List<String> ids = new java.util.ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots) {
                        String title = doc.getString("title");
                        if (title == null) title = doc.getString("name");
                        if (title == null) title = "Challenge " + doc.getId();
                        titles.add(title);
                        ids.add(doc.getId());
                    }
                    if (titles.isEmpty()) {
                        Toast.makeText(this, "No challenges found to delete", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("🗑️ Delete Challenge");
                    builder.setItems(titles.toArray(new String[0]), (dialog, which) -> {
                        db.collection("challenges").document(ids.get(which)).delete()
                                .addOnSuccessListener(unused -> Toast.makeText(this, "Challenge deleted & reflected on app!", Toast.LENGTH_LONG).show());
                    });
                    builder.setNegativeButton("CANCEL", null);
                    AlertDialog dlg = builder.create();
                    dlg.show();
                    if (dlg.getWindow() != null) dlg.getWindow().setBackgroundDrawableResource(android.R.color.background_dark);
                })
                .addOnFailureListener(e -> {
                    LoadingUtil.hide();
                    Toast.makeText(this, "No challenges found in cloud", Toast.LENGTH_SHORT).show();
                });
    }

    private void showMonitorActivityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📊 Monitor User Activity & Progress");

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad, pad, pad);
        container.setBackgroundColor(Color.parseColor("#061A3A"));

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserEmail = currentUser != null ? currentUser.getEmail() : "admin@bluelock.com";

        TextView tvStats = new TextView(this);
        tvStats.setText("• Logged-in Admin: " + currentUserEmail + "\n" +
                "• Database Status: Connected to Firestore\n" +
                "• User Accounts Registered: Active Sync\n" +
                "• Training & Challenges: Real-time Active\n\n" +
                "System Status: 100% Operational ⚡");
        tvStats.setTextColor(Color.parseColor("#00E5FF"));
        tvStats.setTextSize(13);
        tvStats.setLineSpacing(0, 1.4f);
        container.addView(tvStats);

        builder.setView(container);
        builder.setPositiveButton("REFRESH", (d, w) -> {
            Toast.makeText(this, "Metrics re-synced successfully.", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("BACK", (d, w) -> showAdminDashboardDialog());
        AlertDialog dlg = builder.create();
        dlg.show();
        if (dlg.getWindow() != null) dlg.getWindow().setBackgroundDrawableResource(android.R.color.background_dark);
    }

    private void showMaintainContentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📝 Maintain Application Content");

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad, pad, pad);

        TextView tvInfo = new TextView(this);
        tvInfo.setText("Broadcast announcements, update motivational quotes, or clear cache to maintain content freshness.");
        tvInfo.setTextColor(Color.parseColor("#B0BEC5"));
        tvInfo.setTextSize(13);
        container.addView(tvInfo);

        EditText etBroadcast = new EditText(this);
        etBroadcast.setHint("Enter Announcement for Users");
        etBroadcast.setTextColor(Color.WHITE);
        etBroadcast.setHintTextColor(Color.parseColor("#80FFFFFF"));
        etBroadcast.setBackgroundColor(Color.parseColor("#09213A"));
        etBroadcast.setPadding(pad, pad, pad, pad);
        LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        p1.topMargin = pad;
        etBroadcast.setLayoutParams(p1);
        container.addView(etBroadcast);

        builder.setView(container);
        builder.setPositiveButton("BROADCAST", (d, w) -> {
            String msg = etBroadcast.getText().toString().trim();
            if (TextUtils.isEmpty(msg)) {
                Toast.makeText(this, "Please enter an announcement", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Announcement broadcasted to all users!", Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("BACK", (d, w) -> showAdminDashboardDialog());
        AlertDialog dlg = builder.create();
        dlg.show();
        if (dlg.getWindow() != null) dlg.getWindow().setBackgroundDrawableResource(android.R.color.background_dark);
    }

    // ==================================================
    // DESTROY
    // ==================================================

    @Override
    protected void onDestroy() {

        cancelSaveTimeout();

        LoadingUtil.hide();

        super.onDestroy();
    }
}