package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ChallengesActivity extends AppCompatActivity {

    private TextView tvPoints;
    private TextView tvChallengeStatus;

    private Button btnChallenge1;
    private Button btnChallenge2;
    private Button btnChallenge3;
    private Button btnBackDashboard;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private long totalXp = 0;

    // Today's challenge completion state
    private boolean challenge1Completed = false;
    private boolean challenge2Completed = false;
    private boolean challenge3Completed = false;

    private String todayKey;

    // ==================================================
    // ON CREATE
    // ==================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_challenges
        );

        mAuth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();

        // --------------------------------------------------
        // VIEWS
        // --------------------------------------------------

        tvPoints =
                findViewById(
                        R.id.tvPoints
                );

        tvChallengeStatus =
                findViewById(
                        R.id.tvChallengeStatus
                );

        btnChallenge1 =
                findViewById(
                        R.id.btnChallenge1
                );

        btnChallenge2 =
                findViewById(
                        R.id.btnChallenge2
                );

        btnChallenge3 =
                findViewById(
                        R.id.btnChallenge3
                );

        btnBackDashboard =
                findViewById(
                        R.id.btnBackDashboard
                );

        // --------------------------------------------------
        // TODAY KEY
        // --------------------------------------------------

        todayKey =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                ).format(
                        Calendar.getInstance()
                                .getTime()
                );

        // --------------------------------------------------
        // BACK
        // --------------------------------------------------

        btnBackDashboard.setOnClickListener(
                v -> finish()
        );

        // --------------------------------------------------
        // CHALLENGE 1
        // --------------------------------------------------

        btnChallenge1.setOnClickListener(
                v -> verifyChallenge1()
        );

        // --------------------------------------------------
        // CHALLENGE 2
        // --------------------------------------------------

        btnChallenge2.setOnClickListener(
                v -> verifyChallenge2()
        );

        // --------------------------------------------------
        // CHALLENGE 3
        // --------------------------------------------------

        btnChallenge3.setOnClickListener(
                v -> verifyChallenge3()
        );

        loadTotalXp();

        loadChallengeStatus();
    }

    // ==================================================
    // LOAD TOTAL XP
    // ==================================================

    private void loadTotalXp() {

        FirebaseUser user =
                mAuth.getCurrentUser();

        if (user == null) {

            tvPoints.setText(
                    "TOTAL XP: 0"
            );

            return;
        }

        db.collection("users")
                .document(
                        user.getUid()
                )
                .collection("stats")
                .document("main")
                .get()
                .addOnSuccessListener(
                        document -> {

                            if (!document.exists()) {

                                totalXp = 0;

                            } else {

                                Long xp =
                                        document.getLong(
                                                "totalXp"
                                        );

                                totalXp =
                                        xp != null
                                                ? xp
                                                : 0;
                            }

                            updateXpText();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            totalXp = 0;

                            updateXpText();
                        }
                );
    }

    private void updateXpText() {

        tvPoints.setText(
                "TOTAL XP: "
                        + totalXp
        );
    }

    // ==================================================
    // LOAD TODAY'S CHALLENGE STATUS
    // ==================================================

    private void loadChallengeStatus() {

        FirebaseUser user =
                mAuth.getCurrentUser();

        if (user == null) {
            return;
        }

        db.collection("users")
                .document(
                        user.getUid()
                )
                .collection("challenges")
                .document(
                        todayKey
                )
                .get()
                .addOnSuccessListener(
                        document -> {

                            if (!document.exists()) {

                                updateButtons();

                                return;
                            }

                            Boolean c1 =
                                    document.getBoolean(
                                            "challenge1"
                                    );

                            Boolean c2 =
                                    document.getBoolean(
                                            "challenge2"
                                    );

                            Boolean c3 =
                                    document.getBoolean(
                                            "challenge3"
                                    );

                            challenge1Completed =
                                    Boolean.TRUE.equals(c1);

                            challenge2Completed =
                                    Boolean.TRUE.equals(c2);

                            challenge3Completed =
                                    Boolean.TRUE.equals(c3);

                            updateButtons();
                        }
                );
    }

    // ==================================================
    // UPDATE BUTTONS
    // ==================================================

    private void updateButtons() {

        if (challenge1Completed) {

            btnChallenge1.setText(
                    "COMPLETED ✓"
            );

            btnChallenge1.setEnabled(
                    false
            );

        } else {

            btnChallenge1.setText(
                    "VERIFY"
            );

            btnChallenge1.setEnabled(
                    true
            );
        }

        if (challenge2Completed) {

            btnChallenge2.setText(
                    "COMPLETED ✓"
            );

            btnChallenge2.setEnabled(
                    false
            );

        } else {

            btnChallenge2.setText(
                    "VERIFY"
            );

            btnChallenge2.setEnabled(
                    true
            );
        }

        if (challenge3Completed) {

            btnChallenge3.setText(
                    "COMPLETED ✓"
            );

            btnChallenge3.setEnabled(
                    false
            );

        } else {

            btnChallenge3.setText(
                    "VERIFY"
            );

            btnChallenge3.setEnabled(
                    true
            );
        }
    }

    // ==================================================
    // CHALLENGE 1
    // ONE FULL TRAINING
    // ==================================================

    private void verifyChallenge1() {

        setStatus(
                "Checking today's training..."
        );

        getTodayTraining(
                trainingList -> {

                    boolean completed =
                            false;

                    for (
                            DocumentSnapshot doc
                            : trainingList
                    ) {

                        String status =
                                doc.getString(
                                        "sessionStatus"
                                );

                        if (
                                "completed".equalsIgnoreCase(
                                        status
                                )
                        ) {

                            completed = true;

                            break;
                        }
                    }

                    if (completed) {

                        completeChallenge(
                                1,
                                "Complete 1 Full Training",
                                50
                        );

                    } else {

                        setStatus(
                                "Not completed yet. Finish one full training session today."
                        );

                        Toast.makeText(
                                ChallengesActivity.this,
                                "Challenge not completed yet.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    // ==================================================
    // CHALLENGE 2
    // 20 REPS
    // ==================================================

    private void verifyChallenge2() {

        setStatus(
                "Checking today's reps..."
        );

        getTodayTraining(
                trainingList -> {

                    int totalReps = 0;

                    for (
                            DocumentSnapshot doc
                            : trainingList
                    ) {

                        String status =
                                doc.getString(
                                        "sessionStatus"
                                );

                        if (
                                !"completed".equalsIgnoreCase(
                                        status
                                )
                        ) {

                            continue;
                        }

                        Long reps =
                                doc.getLong(
                                        "reps"
                                );

                        if (reps != null) {

                            totalReps +=
                                    reps.intValue();
                        }
                    }

                    if (totalReps >= 20) {

                        completeChallenge(
                                2,
                                "Complete 20 Reps",
                                75
                        );

                    } else {

                        setStatus(
                                "Today's verified reps: "
                                        + totalReps
                                        + "/20"
                        );

                        Toast.makeText(
                                ChallengesActivity.this,
                                "Need at least 20 completed reps today.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    // ==================================================
    // CHALLENGE 3
    // TWO DIFFERENT SKILLS
    // ==================================================

    private void verifyChallenge3() {

        setStatus(
                "Checking today's skills..."
        );

        getTodayTraining(
                trainingList -> {

                    Set<String> skills =
                            new HashSet<>();

                    for (
                            DocumentSnapshot doc
                            : trainingList
                    ) {

                        String status =
                                doc.getString(
                                        "sessionStatus"
                                );

                        if (
                                !"completed".equalsIgnoreCase(
                                        status
                                )
                        ) {

                            continue;
                        }

                        String skill =
                                doc.getString(
                                        "skill"
                                );

                        if (
                                skill != null &&
                                        !skill.trim().isEmpty()
                        ) {

                            skills.add(
                                    skill
                            );
                        }
                    }

                    if (skills.size() >= 2) {

                        completeChallenge(
                                3,
                                "Train 2 Different Skills",
                                100
                        );

                    } else {

                        setStatus(
                                "Verified skills today: "
                                        + skills.size()
                                        + "/2"
                        );

                        Toast.makeText(
                                ChallengesActivity.this,
                                "Complete two different training skills today.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    // ==================================================
    // GET TODAY TRAINING
    // ==================================================

    private void getTodayTraining(
            TrainingCallback callback
    ) {

        FirebaseUser user =
                mAuth.getCurrentUser();

        if (user == null) {

            setStatus(
                    "Please login again."
            );

            return;
        }

        long startOfDay =
                getStartOfToday();

        long endOfDay =
                getStartOfTomorrow();

        db.collection("users")
                .document(
                        user.getUid()
                )
                .collection("training")
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            java.util.ArrayList<DocumentSnapshot> result =
                                    new java.util.ArrayList<>();

                            for (
                                    DocumentSnapshot doc
                                    : snapshot.getDocuments()
                            ) {

                                Long timestamp =
                                        doc.getLong(
                                                "completedAt"
                                        );

                                if (timestamp == null) {
                                    continue;
                                }

                                if (
                                        timestamp >= startOfDay &&
                                                timestamp < endOfDay
                                ) {

                                    result.add(
                                            doc
                                    );
                                }
                            }

                            callback.onResult(
                                    result
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            setStatus(
                                    "Unable to verify challenges. Check your connection."
                            );

                            Toast.makeText(
                                    ChallengesActivity.this,
                                    "Verification failed.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // ==================================================
    // COMPLETE CHALLENGE
    // ==================================================

    private void completeChallenge(
            int challengeNumber,
            String challengeName,
            int reward
    ) {

        FirebaseUser user =
                mAuth.getCurrentUser();

        if (user == null) {

            setStatus(
                    "Please login again."
            );

            return;
        }

        /*
         * Re-check completion state immediately before
         * rewarding, so the same challenge cannot simply
         * be claimed again.
         */

        btnChallenge1.setEnabled(false);
        btnChallenge2.setEnabled(false);
        btnChallenge3.setEnabled(false);

        setStatus(
                "Challenge verified. Saving reward..."
        );

        db.collection("users")
                .document(
                        user.getUid()
                )
                .collection("challenges")
                .document(
                        todayKey
                )
                .get()
                .addOnSuccessListener(
                        document -> {

                            String field =
                                    "challenge"
                                            + challengeNumber;

                            Boolean alreadyCompleted =
                                    document.getBoolean(
                                            field
                                    );

                            if (
                                    Boolean.TRUE.equals(
                                            alreadyCompleted
                                    )
                            ) {

                                loadChallengeStatus();

                                Toast.makeText(
                                        ChallengesActivity.this,
                                        "Challenge already claimed.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            Map<String, Object> challengeData =
                                    new HashMap<>();

                            challengeData.put(
                                    field,
                                    true
                            );

                            challengeData.put(
                                    field + "Name",
                                    challengeName
                            );

                            challengeData.put(
                                    field + "Reward",
                                    reward
                            );

                            challengeData.put(
                                    "date",
                                    todayKey
                            );

                            challengeData.put(
                                    "updatedAt",
                                    System.currentTimeMillis()
                            );

                            db.collection("users")
                                    .document(
                                            user.getUid()
                                    )
                                    .collection("challenges")
                                    .document(
                                            todayKey
                                    )
                                    .set(
                                            challengeData,
                                            SetOptions.merge()
                                    )
                                    .addOnSuccessListener(
                                            unused -> {

                                                addChallengeXp(
                                                        user,
                                                        reward,
                                                        challengeName
                                                );
                                            }
                                    )
                                    .addOnFailureListener(
                                            e -> {

                                                updateButtons();

                                                setStatus(
                                                        "Could not save challenge."
                                                );

                                                Toast.makeText(
                                                        ChallengesActivity.this,
                                                        "Reward save failed.",
                                                        Toast.LENGTH_LONG
                                                ).show();
                                            }
                                    );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            updateButtons();

                            setStatus(
                                    "Could not verify challenge state."
                            );
                        }
                );
    }

    // ==================================================
    // ADD CHALLENGE XP
    // ==================================================

    private void addChallengeXp(
            FirebaseUser user,
            int reward,
            String challengeName
    ) {

        Map<String, Object> updates =
                new HashMap<>();

        updates.put(
                "totalXp",
                FieldValue.increment(
                        reward
                )
        );

        updates.put(
                "challengeXp",
                FieldValue.increment(
                        reward
                )
        );

        updates.put(
                "completedChallenges",
                FieldValue.increment(
                        1
                )
        );

        updates.put(
                "lastChallenge",
                challengeName
        );

        updates.put(
                "lastChallengeReward",
                reward
        );

        updates.put(
                "lastChallengeAt",
                System.currentTimeMillis()
        );

        db.collection("users")
                .document(
                        user.getUid()
                )
                .collection("stats")
                .document("main")
                .set(
                        updates,
                        SetOptions.merge()
                )
                .addOnSuccessListener(
                        unused -> {

                            totalXp += reward;

                            updateXpText();

                            loadChallengeStatus();

                            setStatus(
                                    "Challenge completed! +"
                                            + reward
                                            + " XP"
                            );

                            Toast.makeText(
                                    ChallengesActivity.this,
                                    "+" + reward + " XP added!",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            /*
                             * Reward couldn't be written.
                             * Don't mark UI as successfully rewarded.
                             */
                            loadChallengeStatus();

                            setStatus(
                                    "Challenge verified, but reward could not be added."
                            );

                            Toast.makeText(
                                    ChallengesActivity.this,
                                    "XP update failed.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // ==================================================
    // DATE HELPERS
    // ==================================================

    private long getStartOfToday() {

        Calendar calendar =
                Calendar.getInstance();

        calendar.set(
                Calendar.HOUR_OF_DAY,
                0
        );

        calendar.set(
                Calendar.MINUTE,
                0
        );

        calendar.set(
                Calendar.SECOND,
                0
        );

        calendar.set(
                Calendar.MILLISECOND,
                0
        );

        return calendar.getTimeInMillis();
    }

    private long getStartOfTomorrow() {

        Calendar calendar =
                Calendar.getInstance();

        calendar.set(
                Calendar.HOUR_OF_DAY,
                0
        );

        calendar.set(
                Calendar.MINUTE,
                0
        );

        calendar.set(
                Calendar.SECOND,
                0
        );

        calendar.set(
                Calendar.MILLISECOND,
                0
        );

        calendar.add(
                Calendar.DAY_OF_YEAR,
                1
        );

        return calendar.getTimeInMillis();
    }

    // ==================================================
    // STATUS
    // ==================================================

    private void setStatus(
            String message
    ) {

        if (tvChallengeStatus != null) {

            tvChallengeStatus.setText(
                    message
            );
        }
    }

    // ==================================================
    // BACK
    // ==================================================

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    // ==================================================
    // CALLBACK
    // ==================================================

    private interface TrainingCallback {

        void onResult(
                List<DocumentSnapshot> trainingList
        );
    }
}