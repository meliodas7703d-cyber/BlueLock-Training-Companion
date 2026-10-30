package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class IntroVideoActivity extends AppCompatActivity {

    private VideoView introVideo;
    private TextView errorText;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private boolean videoFinished = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable timeoutRunnable;

    @SuppressLint({"deprecation", "InlinedApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // -----------------------------------------
        // FULL SCREEN
        // -----------------------------------------

        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        // -----------------------------------------
        // LAYOUT
        // -----------------------------------------

        setContentView(R.layout.activity_intro_video);

        // -----------------------------------------
        // FIREBASE
        // -----------------------------------------

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // -----------------------------------------
        // VIEWS
        // -----------------------------------------

        introVideo = findViewById(R.id.introVideo);
        errorText = findViewById(R.id.errorText);

        errorText.setText(R.string.loading);
        errorText.setVisibility(View.VISIBLE);

        // Tap screen to skip video on any device
        View rootLayout = findViewById(android.R.id.content);
        if (rootLayout != null) {
            rootLayout.setOnClickListener(v -> finishVideoAndProceed());
        }
        introVideo.setOnClickListener(v -> finishVideoAndProceed());

        // -----------------------------------------
        // LOGIN VIDEO PLAYBACK
        // -----------------------------------------

        try {
            Uri videoUri = Uri.parse(
                    "android.resource://"
                            + getPackageName()
                            + "/"
                            + R.raw.login
            );

            introVideo.setVideoURI(videoUri);

            // Safety timeout: Auto skip after 5s if playback hangs on low-end device
            timeoutRunnable = this::finishVideoAndProceed;
            handler.postDelayed(timeoutRunnable, 5000);

            introVideo.setOnPreparedListener(
                    mediaPlayer -> {
                        errorText.setVisibility(View.GONE);
                        mediaPlayer.setLooping(false);
                        introVideo.start();
                    }
            );

            introVideo.setOnErrorListener(
                    (mediaPlayer, what, extra) -> {
                        // Video playback issue on this phone - skip video gracefully
                        finishVideoAndProceed();
                        return true;
                    }
            );

            introVideo.setOnCompletionListener(
                    mediaPlayer -> finishVideoAndProceed()
            );
        } catch (Exception e) {
            finishVideoAndProceed();
        }
    }

    private void finishVideoAndProceed() {
        if (videoFinished) {
            return;
        }
        videoFinished = true;
        if (timeoutRunnable != null) {
            handler.removeCallbacks(timeoutRunnable);
        }
        decideNextScreen();
    }

    // =====================================================
    // DECIDE NEXT SCREEN
    // =====================================================

    private void decideNextScreen() {

        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            openLogin();
            return;
        }

        boolean isNewRegistration =
                getIntent().getBooleanExtra(
                        "isNewRegistration",
                        false
                );

        if (isNewRegistration) {
            openProfileSetup();
            return;
        }

        checkExistingProfile(user);
    }

    // =====================================================
    // CHECK FIRESTORE PROFILE
    // =====================================================

    private void checkExistingProfile(FirebaseUser user) {

        // Check local profile cache first
        if (ProfilePreferences.hasProfile(this, user.getUid())) {
            openDashboard();
            return;
        }

        errorText.setText(R.string.checking_player_profile);
        errorText.setVisibility(View.VISIBLE);

        db.collection("users")
                .document(user.getUid())
                .collection("profile")
                .document("main")
                .get()
                .addOnSuccessListener(
                        document -> {
                            if (document.exists()) {
                                String name = document.getString("name");
                                Long age = document.getLong("age");
                                String position = document.getString("position");
                                String experience = document.getString("experience");
                                String foot = document.getString("preferredFoot");
                                String goal = document.getString("trainingGoal");
                                String photoUrl = document.getString("photoUrl");

                                ProfilePreferences.saveProfile(
                                        IntroVideoActivity.this,
                                        user.getUid(),
                                        name != null ? name : "",
                                        age != null ? age.intValue() : 0,
                                        position != null ? position : "",
                                        experience != null ? experience : "",
                                        foot != null ? foot : "",
                                        goal != null ? goal : "",
                                        photoUrl
                                );

                                openDashboard();
                            } else {
                                openProfileSetup();
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {
                            // If local profile exists, proceed to dashboard
                            if (ProfilePreferences.hasProfile(IntroVideoActivity.this, user.getUid())) {
                                openDashboard();
                                return;
                            }

                            String msg = e.getMessage();
                            final String errorMessage = TextUtils.isEmpty(msg) ? "Unknown Firebase error" : msg;

                            errorText.setText(
                                    getString(R.string.firestore_permission_error, errorMessage)
                            );
                            errorText.setVisibility(View.VISIBLE);
                            errorText.setOnClickListener(v -> openProfileSetup());
                        }
                );
    }

    private void openProfileSetup() {
        Intent intent = new Intent(IntroVideoActivity.this, ProfileSetupActivity.class);
        startActivity(intent);
        finish();
    }

    private void openDashboard() {
        Intent intent = new Intent(IntroVideoActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }

    private void openLogin() {
        Intent intent = new Intent(IntroVideoActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        if (timeoutRunnable != null) {
            handler.removeCallbacks(timeoutRunnable);
        }
    }
}
