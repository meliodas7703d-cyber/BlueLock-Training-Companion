package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PerformanceActivity extends AppCompatActivity {

    private TextView tvTotalXp;
    private TextView tvSessions;
    private TextView tvTotalReps;
    private TextView tvBestSkill;

    private FrameLayout weeklyGraphContainer;
    private FrameLayout monthlyGraphContainer;

    private final List<Long> weeklyXp =
            new ArrayList<>();

    private final List<Long> monthlyXp =
            new ArrayList<>();

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_performance
        );

        tvTotalXp =
                findViewById(
                        R.id.tvTotalXp
                );

        tvSessions =
                findViewById(
                        R.id.tvSessions
                );

        tvTotalReps =
                findViewById(
                        R.id.tvTotalReps
                );

        tvBestSkill =
                findViewById(
                        R.id.tvBestSkill
                );

        weeklyGraphContainer =
                findViewById(
                        R.id.weeklyGraphContainer
                );

        monthlyGraphContainer =
                findViewById(
                        R.id.monthlyGraphContainer
                );

        findViewById(
                R.id.btnBack
        ).setOnClickListener(
                v -> finish()
        );

        setupBottomNavigation();

        // Create graphs programmatically.
        weeklyGraphContainer.addView(
                new LineGraphView(
                        this
                )
        );

        monthlyGraphContainer.addView(
                new LineGraphView(
                        this
                )
        );

        loadPerformance();
    }

    private void setupBottomNavigation() {

        Button btnHome = findViewById(R.id.btnBottomHome);
        Button btnPerformance = findViewById(R.id.btnBottomPerformance);
        Button btnProgress = findViewById(R.id.btnBottomProgress);
        Button btnBadges = findViewById(R.id.btnBottomBadges);
        Button btnProfile = findViewById(R.id.btnBottomProfile);

        // Highlight PERFORMANCE
        btnPerformance.setTextColor(0xFF42A5F5);
        btnPerformance.setTypeface(null, Typeface.BOLD);

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnPerformance.setOnClickListener(v -> {
            // Already here
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
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
    }

    private void loadPerformance() {

        FirebaseUser user =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();

        if (user == null) {
            return;
        }

        FirebaseFirestore db =
                FirebaseFirestore
                        .getInstance();

        // ----------------------------------------
        // TOTAL XP
        // ----------------------------------------

        db.collection("users")
                .document(user.getUid())
                .collection("stats")
                .document("main")
                .get()
                .addOnSuccessListener(
                        document -> {

                            Long value =
                                    document.getLong(
                                            "totalXp"
                                    );

                            long xp =
                                    value != null
                                            ? value
                                            : 0;

                            tvTotalXp.setText(
                                    String.valueOf(xp)
                            );
                        }
                );

        // ----------------------------------------
        // TRAINING DATA
        // ----------------------------------------

        db.collection("users")
                .document(user.getUid())
                .collection("training")
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            prepareEmptyGraphs();

                            int sessions = 0;
                            int totalReps = 0;

                            int[] skillReps =
                                    new int[7];

                            Calendar now =
                                    Calendar.getInstance();

                            Calendar weekStart =
                                    (Calendar)
                                            now.clone();

                            weekStart.set(
                                    Calendar.DAY_OF_WEEK,
                                    Calendar.MONDAY
                            );

                            weekStart.set(
                                    Calendar.HOUR_OF_DAY,
                                    0
                            );

                            weekStart.set(
                                    Calendar.MINUTE,
                                    0
                            );

                            weekStart.set(
                                    Calendar.SECOND,
                                    0
                            );

                            weekStart.set(
                                    Calendar.MILLISECOND,
                                    0
                            );

                            Calendar monthStart =
                                    (Calendar)
                                            now.clone();

                            monthStart.add(
                                    Calendar.DAY_OF_YEAR,
                                    -29
                            );

                            monthStart.set(
                                    Calendar.HOUR_OF_DAY,
                                    0
                            );

                            monthStart.set(
                                    Calendar.MINUTE,
                                    0
                            );

                            monthStart.set(
                                    Calendar.SECOND,
                                    0
                            );

                            monthStart.set(
                                    Calendar.MILLISECOND,
                                    0
                            );

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

                                Long time =
                                        doc.getLong(
                                                "completedAt"
                                        );

                                if (time == null) {
                                    continue;
                                }

                                Long xpValue =
                                        doc.getLong(
                                                "earnedXp"
                                        );

                                Long repsValue =
                                        doc.getLong(
                                                "reps"
                                        );

                                long xp =
                                        xpValue != null
                                                ? xpValue
                                                : 0;

                                int reps =
                                        repsValue != null
                                                ? Math.max(
                                                0,
                                                repsValue
                                                        .intValue()
                                        )
                                                : 0;

                                sessions++;
                                totalReps += reps;

                                // ----------------------------
                                // SKILL
                                // ----------------------------

                                String skill =
                                        doc.getString(
                                                "skill"
                                        );

                                int skillIndex =
                                        getSkillIndex(
                                                skill
                                        );

                                if (
                                        skillIndex >= 0
                                ) {

                                    skillReps[
                                            skillIndex
                                            ] += reps;
                                }

                                // ----------------------------
                                // WEEKLY
                                // ----------------------------

                                if (
                                        time >=
                                                weekStart
                                                        .getTimeInMillis()
                                ) {

                                    Calendar c =
                                            Calendar
                                                    .getInstance();

                                    c.setTime(
                                            new Date(time)
                                    );

                                    int day =
                                            c.get(
                                                    Calendar.DAY_OF_WEEK
                                            );

                                    int index;

                                    if (
                                            day ==
                                                    Calendar.SUNDAY
                                    ) {

                                        index = 6;

                                    } else {

                                        index =
                                                day -
                                                        Calendar.MONDAY;
                                    }

                                    if (
                                            index >= 0 &&
                                                    index < 7
                                    ) {

                                        weeklyXp.set(
                                                index,
                                                weeklyXp.get(index)
                                                        + xp
                                        );
                                    }
                                }

                                // ----------------------------
                                // 30 DAYS
                                // ----------------------------

                                if (
                                        time >=
                                                monthStart
                                                        .getTimeInMillis()
                                ) {

                                    long difference =
                                            time -
                                                    monthStart
                                                            .getTimeInMillis();

                                    int index =
                                            (int)
                                                    (
                                                            difference /
                                                                    (
                                                                            24L *
                                                                                    60L *
                                                                                    60L *
                                                                                    1000L
                                                                    )
                                                    );

                                    if (
                                            index >= 0 &&
                                                    index < 30
                                    ) {

                                        monthlyXp.set(
                                                index,
                                                monthlyXp.get(index)
                                                        + xp
                                        );
                                    }
                                }
                            }

                            tvSessions.setText(
                                    String.valueOf(
                                            sessions
                                    )
                            );

                            tvTotalReps.setText(
                                    String.valueOf(
                                            totalReps
                                    )
                            );

                            tvBestSkill.setText(
                                    getBestSkill(
                                            skillReps
                                    )
                            );

                            updateGraphs();
                        }
                );
    }

    private void prepareEmptyGraphs() {

        weeklyXp.clear();
        monthlyXp.clear();

        for (int i = 0; i < 7; i++) {
            weeklyXp.add(0L);
        }

        for (int i = 0; i < 30; i++) {
            monthlyXp.add(0L);
        }
    }

    private void updateGraphs() {

        if (
                weeklyGraphContainer
                        .getChildCount() > 0
        ) {

            View view =
                    weeklyGraphContainer
                            .getChildAt(0);

            if (
                    view instanceof
                            LineGraphView
            ) {

                ((LineGraphView) view)
                        .setData(
                                weeklyXp
                        );
            }
        }

        if (
                monthlyGraphContainer
                        .getChildCount() > 0
        ) {

            View view =
                    monthlyGraphContainer
                            .getChildAt(0);

            if (
                    view instanceof
                            LineGraphView
            ) {

                ((LineGraphView) view)
                        .setData(
                                monthlyXp
                        );
            }
        }
    }

    private String getBestSkill(
            int[] values
    ) {

        String[] names = {
                "Shooting",
                "Passing",
                "Dribbling",
                "Ball Control",
                "Speed",
                "Stamina",
                "Finishing"
        };

        int bestIndex = 0;

        for (
                int i = 1;
                i < values.length;
                i++
        ) {

            if (
                    values[i] >
                            values[bestIndex]
            ) {

                bestIndex = i;
            }
        }

        if (
                values[bestIndex] <= 0
        ) {

            return "No training yet";
        }

        return names[bestIndex];
    }

    private int getSkillIndex(
            String skill
    ) {

        if (skill == null) {
            return -1;
        }

        switch (skill.trim()) {

            case "Shooting":
                return 0;

            case "Passing":
                return 1;

            case "Dribbling":
                return 2;

            case "Ball Control":
                return 3;

            case "Speed":
                return 4;

            case "Stamina":
                return 5;

            case "Finishing":
                return 6;

            default:
                return -1;
        }
    }

    // ==================================================
    // GRAPH
    // ==================================================

    public static class LineGraphView
            extends View {

        private final Paint linePaint =
                new Paint(
                        Paint.ANTI_ALIAS_FLAG
                );

        private final Paint pointPaint =
                new Paint(
                        Paint.ANTI_ALIAS_FLAG
                );

        private final Paint gridPaint =
                new Paint(
                        Paint.ANTI_ALIAS_FLAG
                );

        private List<Long> data =
                new ArrayList<>();

        public LineGraphView(
                Context context
        ) {

            super(context);

            setBackgroundColor(
                    0xFF06182C
            );

            linePaint.setColor(
                    0xFF2196F3
            );

            linePaint.setStrokeWidth(
                    6f
            );

            linePaint.setStyle(
                    Paint.Style.STROKE
            );

            pointPaint.setColor(
                    0xFFFFFFFF
            );

            gridPaint.setColor(
                    0xFF19324A
            );

            gridPaint.setStrokeWidth(
                    2f
            );
        }

        public void setData(
                List<Long> values
        ) {

            data =
                    new ArrayList<>(
                            values
                    );

            invalidate();
        }

        @Override
        protected void onDraw(
                Canvas canvas
        ) {

            super.onDraw(
                    canvas
            );

            if (
                    data == null ||
                            data.isEmpty()
            ) {
                return;
            }

            float left = 25f;

            float right =
                    getWidth() - 18f;

            float top = 18f;

            float bottom =
                    getHeight() - 22f;

            // ------------------------------------
            // GRID
            // ------------------------------------

            float gridStep =
                    (
                            bottom - top
                    ) / 4f;

            for (int i = 0; i < 5; i++) {

                float y =
                        top
                                +
                                i * gridStep;

                canvas.drawLine(
                        left,
                        y,
                        right,
                        y,
                        gridPaint
                );
            }

            // ------------------------------------
            // MAX
            // ------------------------------------

            long max = 1;

            for (
                    long value : data
            ) {

                if (
                        value > max
                ) {

                    max = value;
                }
            }

            // ------------------------------------
            // PATH
            // ------------------------------------

            Path path =
                    new Path();

            for (
                    int i = 0;
                    i < data.size();
                    i++
            ) {

                float x;

                if (
                        data.size() == 1
                ) {

                    x =
                            (
                                    left + right
                            ) /
                                    2f;

                } else {

                    x =
                            left
                                    +
                                    (
                                            right -
                                                    left
                                    )
                                            * i
                                            /
                                            (
                                                    float
                                                    ) (
                                                    data.size()
                                                            - 1
                                            );
                }

                float y =
                        bottom
                                -
                                (
                                        (
                                                bottom -
                                                        top
                                        )
                                                *
                                                data.get(i)
                                                /
                                                (float) max
                                );

                if (
                        i == 0
                ) {

                    path.moveTo(
                            x,
                            y
                    );

                } else {

                    path.lineTo(
                            x,
                            y
                    );
                }

                canvas.drawCircle(
                        x,
                        y,
                        5f,
                        pointPaint
                );
            }

            canvas.drawPath(
                    path,
                    linePaint
            );
        }
    }
}