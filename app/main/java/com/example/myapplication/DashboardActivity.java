package com.example.myapplication;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private TextView tvCompanion;
    private TextView tvProfileInitial;
    private ImageView ivProfilePhoto;

    private TextView tvXp;
    private TextView tvPlayerLevel;
    private TextView tvRankName;
    private TextView tvNextRank;
    private ProgressBar progressRank;

    private TextView tvProgressPercent;
    private TextView tvTrainingProgressDetail;
    private TextView tvRepsProgressDetail;
    private TextView tvSkillsProgressDetail;
    private ProgressBar progressTraining;
    private View dailyTrainingCard;

    private TextView tvShooting;
    private TextView tvPassing;
    private TextView tvDribbling;
    private TextView tvBallControl;
    private TextView tvSpeed;
    private TextView tvStamina;
    private TextView tvFinishing;

    private LinearLayout recentActivityContainer;

    private Button btnTraining;
    private Button btnChallenge;

    private View layoutPerformance;
    private View layoutProgress;
    private View layoutAchievements;

    private Button btnBottomHome;
    private Button btnBottomPerformance;
    private Button btnBottomProgress;
    private Button btnBottomBadges;
    private Button btnBottomProfile;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private int todayCompletedSessions = 0;
    private int todayTotalReps = 0;
    private int todayDifferentSkills = 0;
    private int todayProgress = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        bindViews();
        setupListeners();

        loadUserData();
        loadTotalXp();
        loadTodayProgress();
        loadSkillOverview();
        loadRecentActivity();
    }

    private void bindViews() {

        tvWelcome = findViewById(R.id.tvWelcome);
        tvCompanion = findViewById(R.id.tvCompanion);
        tvProfileInitial = findViewById(R.id.tvProfileInitial);
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);

        tvXp = findViewById(R.id.tvXp);
        tvPlayerLevel = findViewById(R.id.tvPlayerLevel);
        tvRankName = findViewById(R.id.tvRankName);
        tvNextRank = findViewById(R.id.tvNextRank);
        progressRank = findViewById(R.id.progressRank);

        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        tvTrainingProgressDetail =
                findViewById(R.id.tvTrainingProgressDetail);
        tvRepsProgressDetail =
                findViewById(R.id.tvRepsProgressDetail);
        tvSkillsProgressDetail =
                findViewById(R.id.tvSkillsProgressDetail);

        progressTraining =
                findViewById(R.id.progressTraining);

        dailyTrainingCard =
                findViewById(R.id.dailyTrainingCard);

        tvShooting = findViewById(R.id.tvShooting);
        tvPassing = findViewById(R.id.tvPassing);
        tvDribbling = findViewById(R.id.tvDribbling);
        tvBallControl = findViewById(R.id.tvBallControl);
        tvSpeed = findViewById(R.id.tvSpeed);
        tvStamina = findViewById(R.id.tvStamina);
        tvFinishing = findViewById(R.id.tvFinishing);

        recentActivityContainer =
                findViewById(R.id.recentActivityContainer);

        btnTraining = findViewById(R.id.btnTraining);
        btnChallenge = findViewById(R.id.btnChallenge);

        layoutPerformance = findViewById(R.id.layoutPerformance);
        layoutProgress = findViewById(R.id.layoutProgress);
        layoutAchievements = findViewById(R.id.layoutAchievements);

        btnBottomHome = findViewById(R.id.btnBottomHome);
        btnBottomPerformance =
                findViewById(R.id.btnBottomPerformance);
        btnBottomProgress =
                findViewById(R.id.btnBottomProgress);
        btnBottomBadges =
                findViewById(R.id.btnBottomBadges);
        btnBottomProfile =
                findViewById(R.id.btnBottomProfile);

        // Highlight HOME
        btnBottomHome.setTextColor(0xFF42A5F5);
        btnBottomHome.setTypeface(null, Typeface.BOLD);

        tvWelcome.setText("Welcome back,\nPLAYER");
        tvCompanion.setText("TRAINING COMPANION");

        tvXp.setText("0");
        tvPlayerLevel.setText("LV. 1");
        tvRankName.setText("ROOKIE STRIKER");
        tvNextRank.setText("500 XP to LV. 2");

        tvProgressPercent.setText("0%");

        tvTrainingProgressDetail.setText(
                "Training Sessions: 0 / 1"
        );

        tvRepsProgressDetail.setText(
                "Reps Completed: 0 / 20"
        );

        tvSkillsProgressDetail.setText(
                "Different Skills: 0 / 2"
        );

        progressRank.setProgress(0);
        progressTraining.setProgress(0);

        setSkillText(tvShooting, 0);
        setSkillText(tvPassing, 0);
        setSkillText(tvDribbling, 0);
        setSkillText(tvBallControl, 0);
        setSkillText(tvSpeed, 0);
        setSkillText(tvStamina, 0);
        setSkillText(tvFinishing, 0);
    }

    private void setupListeners() {

        btnTraining.setOnClickListener(v -> {

            Intent intent = new Intent(
                    DashboardActivity.this,
                    TrainingSelectionActivity.class
            );

            startActivity(intent);
        });

        btnChallenge.setOnClickListener(v -> {

            Intent intent = new Intent(
                    DashboardActivity.this,
                    ChallengesActivity.class
            );

            startActivity(intent);
        });

        dailyTrainingCard.setOnClickListener(
                v -> showDailyProgressDialog()
        );

        tvProfileInitial.setOnClickListener(
                v -> openProfile()
        );

        ivProfilePhoto.setOnClickListener(
                v -> openProfile()
        );

        layoutPerformance.setOnClickListener(
                v -> openPage(PerformanceActivity.class)
        );

        layoutProgress.setOnClickListener(
                v -> openPage(ProgressActivity.class)
        );

        layoutAchievements.setOnClickListener(
                v -> openPage(BadgesActivity.class)
        );

        btnBottomHome.setOnClickListener(
                v -> {
                    // Already on dashboard
                }
        );

        btnBottomPerformance.setOnClickListener(
                v -> openPage(PerformanceActivity.class)
        );

        btnBottomProgress.setOnClickListener(
                v -> openPage(ProgressActivity.class)
        );

        btnBottomBadges.setOnClickListener(
                v -> openPage(BadgesActivity.class)
        );

        btnBottomProfile.setOnClickListener(
                v -> openProfile()
        );
    }

    private void openPage(Class<?> activityClass) {

        Intent intent = new Intent(
                DashboardActivity.this,
                activityClass
        );

        startActivity(intent);
    }

    private void loadUserData() {

        FirebaseUser user =
                mAuth.getCurrentUser();

        if (user == null) {
            return;
        }

        String authName =
                user.getDisplayName();

        if (authName != null &&
                !authName.trim().isEmpty()) {

            String name =
                    authName.trim();

            tvWelcome.setText(
                    "Welcome back,\n"
                            + name.toUpperCase()
            );

            tvProfileInitial.setText(
                    name.substring(0, 1).toUpperCase()
            );
        }

        Map<String, Object> localProf = ProfilePreferences.getProfile(this, user.getUid());
        if (localProf != null) {
            String localName = (String) localProf.get("name");
            String localPhoto = (String) localProf.get("photoUrl");
            if (localName != null && !localName.trim().isEmpty()) {
                String cleanName = localName.trim();
                tvWelcome.setText("Welcome back,\n" + cleanName.toUpperCase());
                tvProfileInitial.setText(cleanName.substring(0, 1).toUpperCase());
            }
            if (localPhoto != null && !localPhoto.trim().isEmpty()) {
                tvProfileInitial.setVisibility(View.GONE);
                ivProfilePhoto.setVisibility(View.VISIBLE);
                Glide.with(DashboardActivity.this)
                        .load(localPhoto)
                        .circleCrop()
                        .into(ivProfilePhoto);
            }
        }

        db.collection("users")
                .document(user.getUid())
                .collection("profile")
                .document("main")
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {
                        return;
                    }

                    String name =
                            document.getString("name");

                    String photoUrl =
                            document.getString("photoUrl");

                    if (name != null &&
                            !name.trim().isEmpty()) {

                        String cleanName =
                                name.trim();

                        tvWelcome.setText(
                                "Welcome back,\n"
                                        + cleanName.toUpperCase()
                        );

                        tvProfileInitial.setText(
                                cleanName.substring(
                                        0,
                                        1
                                ).toUpperCase()
                        );
                    }

                    if (photoUrl != null &&
                            !photoUrl.trim().isEmpty()) {

                        tvProfileInitial.setVisibility(
                                View.GONE
                        );

                        ivProfilePhoto.setVisibility(
                                View.VISIBLE
                        );

                        Glide.with(
                                        DashboardActivity.this
                                )
                                .load(photoUrl)
                                .circleCrop()
                                .into(ivProfilePhoto);

                    } else {

                        tvProfileInitial.setVisibility(
                                View.GONE
                        );

                        ivProfilePhoto.setVisibility(
                                View.VISIBLE
                        );

                        ivProfilePhoto.setImageResource(
                                R.drawable.profile_avatar
                        );
                    }
                });
    }

    private void loadTotalXp() {

        FirebaseUser user =
                mAuth.getCurrentUser();

        if (user == null) {
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .collection("stats")
                .document("main")
                .get()
                .addOnSuccessListener(document -> {

                    Long totalXp =
                            document.getLong("totalXp");

                    if (totalXp == null) {
                        totalXp =
                                document.getLong("points");
                    }

                    long xp =
                            totalXp != null
                                    ? totalXp
                                    : 0;

                    showXpData(xp);
                });
    }

    private void showXpData(long xp) {

        tvXp.setText(
                String.format(
                        Locale.US,
                        "%,d",
                        xp
                )
        );

        int level =
                getPlayerLevel(xp);

        tvPlayerLevel.setText(
                "LV. " + level
        );

        tvRankName.setText(
                getRankName(level)
        );

        long currentStart =
                getLevelStartXp(level);

        long nextStart =
                getLevelStartXp(level + 1);

        long range =
                nextStart - currentStart;

        long inside =
                xp - currentStart;

        int progress =
                range > 0
                        ? (int) Math.min(
                        100,
                        (inside * 100) / range
                )
                        : 0;

        progressRank.setProgress(
                progress
        );

        long remaining =
                Math.max(
                        0,
                        nextStart - xp
                );

        tvNextRank.setText(
                remaining
                        + " XP to LV. "
                        + (level + 1)
        );
    }

    private int getPlayerLevel(long xp) {

        if (xp < 500) {
            return 1;
        }

        return 1 + (int) (xp / 500);
    }

    private long getLevelStartXp(int level) {

        if (level <= 1) {
            return 0;
        }

        return (
                (long) (level - 1) * 500
        );
    }

    private String getRankName(int level) {

        if (level <= 2) {
            return "ROOKIE STRIKER";
        }

        if (level <= 5) {
            return "RISING STRIKER";
        }

        if (level <= 8) {
            return "ELITE STRIKER";
        }

        if (level <= 12) {
            return "MASTER STRIKER";
        }

        return "BLUE LOCK MONSTER";
    }

    private void loadTodayProgress() {

        FirebaseUser user =
                mAuth.getCurrentUser();

        if (user == null) {
            return;
        }

        long startToday =
                getStartOfToday();

        long startTomorrow =
                getStartOfTomorrow();

        db.collection("users")
                .document(user.getUid())
                .collection("training")
                .get()
                .addOnSuccessListener(snapshot -> {

                    int sessions = 0;
                    int reps = 0;

                    Set<String> skills =
                            new HashSet<>();

                    for (
                            DocumentSnapshot document
                            : snapshot.getDocuments()
                    ) {

                        Long timestamp =
                                document.getLong(
                                        "completedAt"
                                );

                        if (timestamp == null) {
                            continue;
                        }

                        if (
                                timestamp < startToday ||
                                        timestamp >= startTomorrow
                        ) {
                            continue;
                        }

                        String status =
                                document.getString(
                                        "sessionStatus"
                                );

                        Long repsValue =
                                document.getLong(
                                        "reps"
                                );

                        int sessionReps =
                                repsValue != null
                                        ? Math.max(
                                        0,
                                        repsValue.intValue()
                                )
                                        : 0;

                        if (
                                "completed"
                                        .equalsIgnoreCase(status)
                                        ||
                                        "stopped"
                                                .equalsIgnoreCase(status)
                        ) {

                            reps += sessionReps;
                        }

                        if (
                                "completed"
                                        .equalsIgnoreCase(status)
                        ) {

                            sessions++;

                            String skill =
                                    document.getString(
                                            "skill"
                                    );

                            if (
                                    skill != null &&
                                            !skill.trim().isEmpty()
                            ) {

                                skills.add(
                                        skill.trim()
                                );
                            }
                        }
                    }

                    todayCompletedSessions =
                            sessions;

                    todayTotalReps =
                            reps;

                    todayDifferentSkills =
                            skills.size();

                    int sessionProgress =
                            sessions >= 1
                                    ? 100
                                    : 0;

                    int repsProgress =
                            calculatePercent(
                                    reps,
                                    20
                            );

                    int skillsProgress =
                            calculatePercent(
                                    skills.size(),
                                    2
                            );

                    todayProgress =
                            (
                                    sessionProgress
                                            + repsProgress
                                            + skillsProgress
                            ) / 3;

                    updateTodayUI();
                })
                .addOnFailureListener(
                        e -> updateTodayUI()
                );
    }

    private void updateTodayUI() {

        progressTraining.setProgress(
                todayProgress
        );

        tvProgressPercent.setText(
                todayProgress + "%"
        );

        // Sessions Detail
        String sessionText = "Sessions: " + todayCompletedSessions + " / 1";
        if (todayCompletedSessions >= 1) {
            sessionText += "  ✓";
            tvTrainingProgressDetail.setTextColor(0xFF64B5F6);
        } else {
            tvTrainingProgressDetail.setTextColor(0xFFFFFFFF);
        }
        tvTrainingProgressDetail.setText(sessionText);

        // Reps Detail
        String repsText = "Reps: " + todayTotalReps + " / 20";
        if (todayTotalReps >= 20) {
            repsText += "  ✓";
            tvRepsProgressDetail.setTextColor(0xFF64B5F6);
        } else {
            tvRepsProgressDetail.setTextColor(0xFFFFFFFF);
        }
        tvRepsProgressDetail.setText(repsText);

        // Skills Detail
        String skillsText = "Skills: " + todayDifferentSkills + " / 2";
        if (todayDifferentSkills >= 2) {
            skillsText += "  ✓";
            tvSkillsProgressDetail.setTextColor(0xFF64B5F6);
        } else {
            tvSkillsProgressDetail.setTextColor(0xFFFFFFFF);
        }
        tvSkillsProgressDetail.setText(skillsText);
    }

    private void loadSkillOverview() {

        FirebaseUser user =
                mAuth.getCurrentUser();

        if (user == null) {
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .collection("training")
                .get()
                .addOnSuccessListener(snapshot -> {

                    int shooting = 0;
                    int passing = 0;
                    int dribbling = 0;
                    int ballControl = 0;
                    int speed = 0;
                    int stamina = 0;
                    int finishing = 0;

                    for (
                            DocumentSnapshot document
                            : snapshot.getDocuments()
                    ) {

                        String status =
                                document.getString(
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

                        Long repsValue =
                                document.getLong(
                                        "reps"
                                );

                        int reps =
                                repsValue != null
                                        ? Math.max(
                                        0,
                                        repsValue.intValue()
                                )
                                        : 0;

                        String skill =
                                document.getString(
                                        "skill"
                                );

                        if (skill == null) {
                            continue;
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

                    setSkillText(
                            tvShooting,
                            shooting
                    );

                    setSkillText(
                            tvPassing,
                            passing
                    );

                    setSkillText(
                            tvDribbling,
                            dribbling
                    );

                    setSkillText(
                            tvBallControl,
                            ballControl
                    );

                    setSkillText(
                            tvSpeed,
                            speed
                    );

                    setSkillText(
                            tvStamina,
                            stamina
                    );

                    setSkillText(
                            tvFinishing,
                            finishing
                    );
                });
    }

    private void setSkillText(
            TextView view,
            int reps
    ) {

        int level =
                getSkillLevel(reps);

        String title =
                getSkillTitle(level);

        int start =
                getSkillLevelStart(reps);

        int size =
                getSkillLevelSize(reps);

        int inside =
                Math.max(
                        0,
                        reps - start
                );

        int progress =
                size > 0
                        ? Math.min(
                        100,
                        (inside * 100) / size
                )
                        : 0;

        view.setText(
                "Level "
                        + level
                        + " • "
                        + title
                        + "\n"
                        + inside
                        + " / "
                        + size
                        + " reps • "
                        + progress
                        + "%"
        );
    }

    private int getSkillLevel(int reps) {

        if (reps < 20) return 1;
        if (reps < 40) return 2;
        if (reps < 60) return 3;
        if (reps < 80) return 4;
        if (reps < 100) return 5;

        return 6 + (
                (reps - 100) / 100
        );
    }

    private String getSkillTitle(int level) {

        switch (level) {

            case 1:
                return "Beginner";

            case 2:
                return "Developing";

            case 3:
                return "Improving";

            case 4:
                return "Advanced";

            case 5:
                return "Elite";

            default:
                return "Master";
        }
    }

    private int getSkillLevelStart(int reps) {

        if (reps < 20) return 0;
        if (reps < 40) return 20;
        if (reps < 60) return 40;
        if (reps < 80) return 60;
        if (reps < 100) return 80;

        return 100
                + (
                ((reps - 100) / 100)
                        * 100
        );
    }

    private int getSkillLevelSize(int reps) {

        if (reps < 100) {
            return 20;
        }

        return 100;
    }

    private void loadRecentActivity() {

        FirebaseUser user =
                mAuth.getCurrentUser();

        if (user == null) {
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .collection("training")
                .get()
                .addOnSuccessListener(snapshot -> {

                    recentActivityContainer
                            .removeAllViews();

                    List<DocumentSnapshot> docs =
                            new ArrayList<>(
                                    snapshot.getDocuments()
                            );

                    Collections.sort(
                            docs,
                            (a, b) -> {

                                Long aTime =
                                        a.getLong(
                                                "completedAt"
                                        );

                                Long bTime =
                                        b.getLong(
                                                "completedAt"
                                        );

                                long x =
                                        aTime != null
                                                ? aTime
                                                : 0;

                                long y =
                                        bTime != null
                                                ? bTime
                                                : 0;

                                return Long.compare(y, x);
                            }
                    );

                    int shown = 0;

                    for (
                            DocumentSnapshot document
                            : docs
                    ) {

                        if (shown >= 3) {
                            break;
                        }

                        String status =
                                document.getString(
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

                        String skill =
                                document.getString(
                                        "skill"
                                );

                        if (skill == null) {
                            skill = "Training";
                        }

                        Long repsValue =
                                document.getLong(
                                        "reps"
                                );

                        int reps =
                                repsValue != null
                                        ? repsValue.intValue()
                                        : 0;

                        Long xpValue =
                                document.getLong(
                                        "earnedXp"
                                );

                        long xp =
                                xpValue != null
                                        ? xpValue
                                        : 0;

                        Long timeValue =
                                document.getLong(
                                        "completedAt"
                                );

                        String date =
                                formatDate(
                                        timeValue != null
                                                ? timeValue
                                                : 0
                                );

                        addRecentActivity(
                                skill + " Training",
                                reps + " reps • " + date,
                                "+" + xp + " XP"
                        );

                        shown++;
                    }

                    if (shown == 0) {

                        TextView empty =
                                new TextView(
                                        DashboardActivity.this
                                );

                        empty.setText(
                                "No completed training yet."
                        );

                        empty.setTextColor(
                                0xFF9FB6D1
                        );

                        empty.setTextSize(12);

                        empty.setPadding(
                                12,
                                14,
                                12,
                                14
                        );

                        recentActivityContainer
                                .addView(empty);
                    }
                });
    }

    private void addRecentActivity(
            String title,
            String subtitle,
            String xp
    ) {

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                Gravity.CENTER_VERTICAL
        );

        row.setPadding(
                0,
                0,
                0,
                14
        );

        // Indicator
        View indicator = new View(this);
        LinearLayout.LayoutParams indParams = new LinearLayout.LayoutParams(
                dp(2),
                dp(24)
        );
        indicator.setBackgroundColor(0xFF2196F3);
        row.addView(indicator, indParams);

        LinearLayout holder =
                new LinearLayout(this);

        holder.setOrientation(
                LinearLayout.VERTICAL
        );

        LinearLayout.LayoutParams
                holderParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );
        holderParams.setMarginStart(dp(10));

        TextView titleView =
                new TextView(this);

        titleView.setText(title.toUpperCase());
        titleView.setTextColor(
                0xFFFFFFFF
        );
        titleView.setTextSize(11);
        titleView.setTypeface(
                null,
                Typeface.BOLD
        );
        titleView.setLetterSpacing(0.02f);

        TextView subtitleView =
                new TextView(this);

        subtitleView.setText(subtitle);
        subtitleView.setTextColor(
                0xFF8FA4BE
        );
        subtitleView.setTextSize(8);

        holder.addView(titleView);
        holder.addView(subtitleView);

        row.addView(
                holder,
                holderParams
        );

        TextView xpView =
                new TextView(this);

        xpView.setText(xp);
        xpView.setTextColor(
                0xFF64B5F6
        );
        xpView.setTextSize(10);
        xpView.setTypeface(
                null,
                Typeface.BOLD
        );

        row.addView(xpView);

        recentActivityContainer.addView(row);
    }

    private int dp(int px) {
        return (int) (px * getResources().getDisplayMetrics().density);
    }

    private String formatDate(
            long timestamp
    ) {

        if (timestamp <= 0) {
            return "-";
        }

        return DateFormat
                .getDateInstance(
                        DateFormat.MEDIUM,
                        Locale.getDefault()
                )
                .format(
                        new Date(timestamp)
                );
    }

    private void showDailyProgressDialog() {

        String session =
                todayCompletedSessions >= 1
                        ? "✓ Training completed"
                        : "○ Complete 1 training";

        String reps =
                todayTotalReps >= 20
                        ? "✓ Rep target reached"
                        : "○ "
                          + todayTotalReps
                          + " / 20 reps";

        String skills =
                todayDifferentSkills >= 2
                        ? "✓ Skill target reached"
                        : "○ "
                          + todayDifferentSkills
                          + " / 2 skills";

        String message =
                session
                        + "\n\n"
                        + reps
                        + "\n\n"
                        + skills
                        + "\n\n"
                        + "Overall Progress: "
                        + todayProgress
                        + "%";

        new AlertDialog.Builder(this)
                .setTitle(
                        "Today's Progress"
                )
                .setMessage(message)
                .setPositiveButton(
                        "OK",
                        null
                )
                .show();
    }

    private int calculatePercent(
            int current,
            int target
    ) {

        if (target <= 0) {
            return 100;
        }

        if (current <= 0) {
            return 0;
        }

        return Math.min(
                100,
                (current * 100) / target
        );
    }

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

    private void openProfile() {

        Intent intent =
                new Intent(
                        DashboardActivity.this,
                        ProfileActivity.class
                );

        startActivity(intent);
    }

    @Override
    protected void onResume() {

        super.onResume();

        loadUserData();
        loadTotalXp();
        loadTodayProgress();
        loadSkillOverview();
        loadRecentActivity();
    }
}