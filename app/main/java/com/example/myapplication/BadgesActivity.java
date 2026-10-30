package com.example.myapplication;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class BadgesActivity extends AppCompatActivity {

    private LinearLayout badgeContainer;

    private int totalReps = 0;
    private int sessions = 0;

    private int shooting = 0;
    private int passing = 0;
    private int dribbling = 0;
    private int ballControl = 0;
    private int speed = 0;
    private int stamina = 0;
    private int finishing = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_badges
        );

        badgeContainer =
                findViewById(
                        R.id.badgeContainer
                );

        findViewById(R.id.btnBack)
                .setOnClickListener(
                        v -> finish()
                );

        setupBottomNavigation();

        loadBadges();
    }

    private void setupBottomNavigation() {

        Button btnHome = findViewById(R.id.btnBottomHome);
        Button btnPerformance = findViewById(R.id.btnBottomPerformance);
        Button btnProgress = findViewById(R.id.btnBottomProgress);
        Button btnBadges = findViewById(R.id.btnBottomBadges);
        Button btnProfile = findViewById(R.id.btnBottomProfile);

        // Highlight BADGES
        btnBadges.setTextColor(0xFF42A5F5);
        btnBadges.setTypeface(null, Typeface.BOLD);

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
            // Already here
        });

        btnProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
    }

    private void loadBadges() {

        FirebaseUser user =
                FirebaseAuth.getInstance()
                        .getCurrentUser();

        if (user == null) {
            return;
        }

        FirebaseFirestore db =
                FirebaseFirestore.getInstance();

        db.collection("users")
                .document(user.getUid())
                .collection("training")
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            resetData();

                            for (
                                    DocumentSnapshot doc
                                    : snapshot.getDocuments()
                            ) {

                                String status =
                                        doc.getString(
                                                "sessionStatus"
                                        );

                                if (
                                        !"completed"
                                                .equalsIgnoreCase(
                                                        status
                                                )
                                ) {
                                    continue;
                                }

                                sessions++;

                                Long repsValue =
                                        doc.getLong(
                                                "reps"
                                        );

                                int reps =
                                        repsValue != null
                                                ? Math.max(
                                                0,
                                                repsValue.intValue()
                                        )
                                                : 0;

                                totalReps += reps;

                                String skill =
                                        doc.getString(
                                                "skill"
                                        );

                                addSkillReps(
                                        skill,
                                        reps
                                );
                            }

                            displayBadges();
                        }
                );
    }

    private void resetData() {

        totalReps = 0;
        sessions = 0;

        shooting = 0;
        passing = 0;
        dribbling = 0;
        ballControl = 0;
        speed = 0;
        stamina = 0;
        finishing = 0;
    }

    private void addSkillReps(
            String skill,
            int reps
    ) {

        if (skill == null) {
            return;
        }

        switch (skill.trim()) {

            case "Shooting":
                shooting += reps;
                break;

            case "Passing":
                passing += reps;
                break;

            case "Dribbling":
                dribbling += reps;
                break;

            case "Ball Control":
                ballControl += reps;
                break;

            case "Speed":
                speed += reps;
                break;

            case "Stamina":
                stamina += reps;
                break;

            case "Finishing":
                finishing += reps;
                break;
        }
    }

    private void displayBadges() {

        badgeContainer.removeAllViews();

        int unlocked = 0;

        // -------------------------------------------
        // 1
        // -------------------------------------------

        if (passing >= 20) {

            addBadge(
                    "BEGINNER PASSER",
                    "Complete 20 passing reps.",
                    "PASSING • 20+ REPS",
                    true
            );

            unlocked++;
        } else {

            addBadge(
                    "BEGINNER PASSER",
                    "Complete 20 passing reps.",
                    passing
                            + " / 20 reps",
                    false
            );
        }

        // -------------------------------------------
        // 2
        // -------------------------------------------

        if (passing >= 50) {

            addBadge(
                    "PASSING SPECIALIST",
                    "Reach 50 passing reps.",
                    "PASSING • 50+ REPS",
                    true
            );

            unlocked++;
        } else {

            addBadge(
                    "PASSING SPECIALIST",
                    "Reach 50 passing reps.",
                    passing
                            + " / 50 reps",
                    false
            );
        }

        // -------------------------------------------
        // 3
        // -------------------------------------------

        if (speed >= 20) {

            addBadge(
                    "SPEED STARTER",
                    "Complete 20 speed reps.",
                    "SPEED • 20+ REPS",
                    true
            );

            unlocked++;
        } else {

            addBadge(
                    "SPEED STARTER",
                    "Complete 20 speed reps.",
                    speed
                            + " / 20 reps",
                    false
            );
        }

        // -------------------------------------------
        // 4
        // -------------------------------------------

        if (speed >= 100) {

            addBadge(
                    "SPEED MASTER",
                    "Reach 100 speed reps.",
                    "SPEED • 100+ REPS",
                    true
            );

            unlocked++;
        } else {

            addBadge(
                    "SPEED MASTER",
                    "Reach 100 speed reps.",
                    speed
                            + " / 100 reps",
                    false
            );
        }

        // -------------------------------------------
        // 5
        // -------------------------------------------

        int differentSkills =
                countDifferentSkills();

        if (differentSkills >= 2) {

            addBadge(
                    "ALL-ROUNDER",
                    "Train at least 2 different skills.",
                    "2+ SKILLS TRAINED",
                    true
            );

            unlocked++;
        } else {

            addBadge(
                    "ALL-ROUNDER",
                    "Train at least 2 different skills.",
                    differentSkills
                            + " / 2 skills",
                    false
            );
        }

        // -------------------------------------------
        // 6
        // -------------------------------------------

        if (sessions >= 10) {

            addBadge(
                    "TRAINING BEAST",
                    "Complete 10 training sessions.",
                    "10+ SESSIONS",
                    true
            );

            unlocked++;
        } else {

            addBadge(
                    "TRAINING BEAST",
                    "Complete 10 training sessions.",
                    sessions
                            + " / 10 sessions",
                    false
            );
        }

        // -------------------------------------------
        // 7
        // -------------------------------------------

        if (totalReps >= 100) {

            addBadge(
                    "DEDICATED TRAINEE",
                    "Reach 100 total reps.",
                    "100+ TOTAL REPS",
                    true
            );

            unlocked++;
        } else {

            addBadge(
                    "DEDICATED TRAINEE",
                    "Reach 100 total reps.",
                    totalReps
                            + " / 100 reps",
                    false
            );
        }

        // -------------------------------------------
        // 8
        // -------------------------------------------

        if (totalReps >= 500) {

            addBadge(
                    "ELITE TRAINEE",
                    "Reach 500 total reps.",
                    "500+ TOTAL REPS",
                    true
            );

            unlocked++;
        } else {

            addBadge(
                    "ELITE TRAINEE",
                    "Reach 500 total reps.",
                    totalReps
                            + " / 500 reps",
                    false
            );
        }

        TextView summary =
                new TextView(this);

        summary.setText(
                unlocked
                        + " / 8 BADGES UNLOCKED"
        );

        summary.setTextColor(
                0xFF64B5F6
        );

        summary.setTextSize(
                11
        );

        summary.setTypeface(
                null,
                Typeface.BOLD
        );

        summary.setPadding(
                4,
                12,
                4,
                14
        );

        badgeContainer.addView(
                summary,
                0
        );
    }

    private int countDifferentSkills() {

        int count = 0;

        if (shooting > 0) count++;
        if (passing > 0) count++;
        if (dribbling > 0) count++;
        if (ballControl > 0) count++;
        if (speed > 0) count++;
        if (stamina > 0) count++;
        if (finishing > 0) count++;

        return count;
    }

    private void addBadge(
            String title,
            String description,
            String progress,
            boolean unlocked
    ) {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.HORIZONTAL
        );

        card.setGravity(
                Gravity.CENTER_VERTICAL
        );

        card.setPadding(
                14,
                12,
                14,
                12
        );

        card.setBackgroundColor(
                unlocked
                        ? 0xFF082A4C
                        : 0xFF06182C
        );

        // Badge icon

        TextView icon =
                new TextView(this);

        icon.setText(
                unlocked
                        ? "★"
                        : "☆"
        );

        icon.setGravity(
                Gravity.CENTER
        );

        icon.setTextSize(
                27
        );

        icon.setTextColor(
                unlocked
                        ? 0xFF2196F3
                        : 0xFF52687B
        );

        LinearLayout.LayoutParams
                iconParams =
                new LinearLayout.LayoutParams(
                        55,
                        55
                );

        card.addView(
                icon,
                iconParams
        );

        // Text

        LinearLayout text =
                new LinearLayout(this);

        text.setOrientation(
                LinearLayout.VERTICAL
        );

        LinearLayout.LayoutParams
                textParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        text.setPadding(
                12,
                0,
                5,
                0
        );

        TextView titleView =
                new TextView(this);

        titleView.setText(
                title
        );

        titleView.setTextColor(
                unlocked
                        ? 0xFFFFFFFF
                        : 0xFF8EA4BA
        );

        titleView.setTextSize(
                12
        );

        titleView.setTypeface(
                null,
                Typeface.BOLD
        );

        TextView descriptionView =
                new TextView(this);

        descriptionView.setText(
                description
        );

        descriptionView.setTextColor(
                0xFF9FB6D1
        );

        descriptionView.setTextSize(
                9
        );

        descriptionView.setPadding(
                0,
                3,
                0,
                3
        );

        TextView progressView =
                new TextView(this);

        progressView.setText(
                progress
        );

        progressView.setTextColor(
                unlocked
                        ? 0xFF64B5F6
                        : 0xFF71879D
        );

        progressView.setTextSize(
                8
        );

        text.addView(
                titleView
        );

        text.addView(
                descriptionView
        );

        text.addView(
                progressView
        );

        card.addView(
                text,
                textParams
        );

        TextView state =
                new TextView(this);

        state.setText(
                unlocked
                        ? "UNLOCKED"
                        : "LOCKED"
        );

        state.setTextColor(
                unlocked
                        ? 0xFF42A5F5
                        : 0xFF566B7E
        );

        state.setTextSize(
                8
        );

        state.setTypeface(
                null,
                Typeface.BOLD
        );

        card.addView(
                state
        );

        LinearLayout.LayoutParams
                cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(
                0,
                0,
                0,
                7
        );

        badgeContainer.addView(
                card,
                cardParams
        );
    }
}