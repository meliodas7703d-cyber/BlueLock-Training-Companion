package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ProgressActivity extends AppCompatActivity {

    private TextView tvOverall;
    private TextView tvTopSkill;
    private SkillChartView skillChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_progress);

        tvOverall = findViewById(R.id.tvOverall);
        tvTopSkill = findViewById(R.id.tvTopSkill);
        skillChart = findViewById(R.id.skillChart);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        setupBottomNavigation();

        loadProgress();
    }

    private void setupBottomNavigation() {

        Button btnHome = findViewById(R.id.btnBottomHome);
        Button btnPerformance = findViewById(R.id.btnBottomPerformance);
        Button btnProgress = findViewById(R.id.btnBottomProgress);
        Button btnBadges = findViewById(R.id.btnBottomBadges);
        Button btnProfile = findViewById(R.id.btnBottomProfile);

        // Highlight PROGRESS
        btnProgress.setTextColor(0xFF42A5F5);
        btnProgress.setTypeface(null, Typeface.BOLD);

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
            // Already here
        });

        btnBadges.setOnClickListener(v -> {
            startActivity(new Intent(this, BadgesActivity.class));
            finish();
        });

        btnProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
    }

    private void loadProgress() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(user.getUid())
                .collection("training")
                .get()
                .addOnSuccessListener(snapshot -> {
                    int[] reps = new int[7];
                    int totalReps = 0;

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String status = doc.getString("sessionStatus");
                        if (!"completed".equalsIgnoreCase(status)) continue;

                        Long value = doc.getLong("reps");
                        int count = (value != null) ? Math.max(0, value.intValue()) : 0;
                        totalReps += count;

                        String skill = doc.getString("skill");
                        int index = getSkillIndex(skill);
                        if (index >= 0) {
                            reps[index] += count;
                        }
                    }

                    int maxTarget = 20; // Example target for 100% overall progress
                    int totalProgress = (totalReps >= maxTarget) ? 100 : (totalReps * 100 / maxTarget);

                    tvOverall.setText(totalProgress + "%");

                    int bestIndex = 0;
                    for (int i = 1; i < reps.length; i++) {
                        if (reps[i] > reps[bestIndex]) {
                            bestIndex = i;
                        }
                    }

                    String[] names = {
                            "Shooting", "Passing", "Dribbling", "Ball Control",
                            "Speed", "Stamina", "Finishing"
                    };

                    if (reps[bestIndex] > 0) {
                        tvTopSkill.setText(names[bestIndex] + " • " + reps[bestIndex] + " reps");
                    } else {
                        tvTopSkill.setText("No training completed yet");
                    }

                    List<Integer> data = new ArrayList<>();
                    for (int r : reps) {
                        data.add(r);
                    }
                    skillChart.setData(data);
                });
    }

    private int getSkillIndex(String skill) {
        if (skill == null) return -1;
        switch (skill.trim()) {
            case "Shooting": return 0;
            case "Passing": return 1;
            case "Dribbling": return 2;
            case "Ball Control": return 3;
            case "Speed": return 4;
            case "Stamina": return 5;
            case "Finishing": return 6;
            default: return -1;
        }
    }
}
