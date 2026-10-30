package com.example.myapplication;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;

public class AdminPanelActivity extends AppCompatActivity {

    private TextView tvAdminEmail, tvTotalUsersCount, tvTotalExercisesCount, tvTotalChallengesCount, tvActiveUsersCount;
    private LinearLayout llUsersContainer, llExercisesContainer, llChallengesContainer;
    private Button btnAddExercise, btnAddChallenge, btnBackToDashboard;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private ListenerRegistration usersListener, exercisesListener, challengesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        bindViews();
        setupListeners();
        loadAdminInfo();
        setupRealtimeListeners();
    }

    private void bindViews() {
        tvAdminEmail = findViewById(R.id.tvAdminEmail);
        tvTotalUsersCount = findViewById(R.id.tvTotalUsersCount);
        tvTotalExercisesCount = findViewById(R.id.tvTotalExercisesCount);
        tvTotalChallengesCount = findViewById(R.id.tvTotalChallengesCount);
        tvActiveUsersCount = findViewById(R.id.tvActiveUsersCount);

        llUsersContainer = findViewById(R.id.llUsersContainer);
        llExercisesContainer = findViewById(R.id.llExercisesContainer);
        llChallengesContainer = findViewById(R.id.llChallengesContainer);

        btnAddExercise = findViewById(R.id.btnAddExercise);
        btnAddChallenge = findViewById(R.id.btnAddChallenge);
        btnBackToDashboard = findViewById(R.id.btnBackToDashboard);
    }

    private void setupListeners() {
        btnAddExercise.setOnClickListener(v -> showAddExerciseDialog());
        btnAddChallenge.setOnClickListener(v -> showAddChallengeDialog());

        btnBackToDashboard.setOnClickListener(v -> {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        });
    }

    private void loadAdminInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            tvAdminEmail.setText(currentUser.getEmail());
        } else {
            tvAdminEmail.setText("Admin");
        }
    }

    private void setupRealtimeListeners() {
        // 1. Users Realtime Listener
        usersListener = db.collection("users").addSnapshotListener((value, error) -> {
            if (error != null || value == null) return;

            llUsersContainer.removeAllViews();
            int totalUsers = value.size();
            int activeUsers = 0;

            tvTotalUsersCount.setText(String.valueOf(totalUsers));

            for (DocumentSnapshot doc : value.getDocuments()) {
                String userId = doc.getId();
                String name = doc.getString("name");
                if (name == null) name = "Unknown User";
                String email = doc.getString("email");
                if (email == null) email = "No Email";
                String status = doc.getString("status");
                if (status == null) status = "Active";

                if (status.equalsIgnoreCase("Active")) {
                    activeUsers++;
                }

                addDynamicRow(llUsersContainer, userId, name, email, status, "user");
            }

            tvActiveUsersCount.setText(String.valueOf(activeUsers));
        });

        // 2. Exercises Realtime Listener
        exercisesListener = db.collection("exercises").addSnapshotListener((value, error) -> {
            if (error != null || value == null) return;

            llExercisesContainer.removeAllViews();
            tvTotalExercisesCount.setText(String.valueOf(value.size()));

            for (DocumentSnapshot doc : value.getDocuments()) {
                String exId = doc.getId();
                String title = doc.getString("title");
                if (title == null) title = "Untitled Exercise";
                String level = doc.getString("level");
                if (level == null) level = "Beginner";

                addDynamicRow(llExercisesContainer, exId, title, level, "Exercise", "exercise");
            }
        });

        // 3. Challenges Realtime Listener
        challengesListener = db.collection("challenges").addSnapshotListener((value, error) -> {
            if (error != null || value == null) return;

            llChallengesContainer.removeAllViews();
            tvTotalChallengesCount.setText(String.valueOf(value.size()));

            for (DocumentSnapshot doc : value.getDocuments()) {
                String chId = doc.getId();
                String title = doc.getString("title");
                if (title == null) title = "Untitled Challenge";
                String type = doc.getString("type");
                if (type == null) type = "Skill";

                addDynamicRow(llChallengesContainer, chId, title, type, "Challenge", "challenge");
            }
        });
    }

    private void addDynamicRow(LinearLayout container, String docId, String title, String subtitle, String badgeText, String type) {
        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int pad = (int) (10 * getResources().getDisplayMetrics().density);
        row.setPadding(0, pad, 0, pad);

        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        textLayout.setOrientation(LinearLayout.VERTICAL);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextColor(getResources().getColor(android.R.color.white));
        tvTitle.setTextSize(14f);
        tvTitle.setTypeface(null, Typeface.BOLD);

        TextView tvSub = new TextView(this);
        tvSub.setText(subtitle);
        tvSub.setTextColor(getResources().getColor(android.R.color.darker_gray));
        tvSub.setTextSize(12f);

        textLayout.addView(tvTitle);
        textLayout.addView(tvSub);

        TextView tvBadge = new TextView(this);
        tvBadge.setText(badgeText);
        tvBadge.setTextSize(11f);
        tvBadge.setGravity(Gravity.CENTER);
        int padH = (int) (8 * getResources().getDisplayMetrics().density);
        int padV = (int) (4 * getResources().getDisplayMetrics().density);
        tvBadge.setPadding(padH, padV, padH, padV);

        if (badgeText.equalsIgnoreCase("Active") || badgeText.equalsIgnoreCase("Beginner")) {
            tvBadge.setTextColor(getResources().getColor(android.R.color.holo_green_light));
            tvBadge.setBackgroundColor(0xFF064E3B);
        } else if (badgeText.equalsIgnoreCase("Inactive") || badgeText.equalsIgnoreCase("Advanced")) {
            tvBadge.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            tvBadge.setBackgroundColor(0xFF450A0A);
        } else {
            tvBadge.setTextColor(0xFF64B5F6);
            tvBadge.setBackgroundColor(0xFF1E3A8A);
        }

        row.addView(textLayout);
        row.addView(tvBadge);

        // Make row clickable to Edit or Delete
        row.setOnClickListener(v -> showItemOptionsDialog(docId, title, subtitle, type));

        container.addView(row);

        // Divider
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(0xFF1E293B);
        container.addView(divider);
    }

    private void showItemOptionsDialog(String docId, String title, String subtitle, String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Manage " + type.toUpperCase());
        builder.setMessage("Selected: " + title + "\nChoose an action:");

        builder.setPositiveButton("DELETE", (dialog, which) -> {
            String colName = type.equals("user") ? "users" : (type.equals("exercise") ? "exercises" : "challenges");
            db.collection(colName).document(docId).delete()
                    .addOnSuccessListener(unused -> Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        builder.setNeutralButton("EDIT", (dialog, which) -> showEditDialog(docId, title, subtitle, type));
        builder.setNegativeButton("CANCEL", null);
        builder.show();
    }

    private void showEditDialog(String docId, String currentTitle, String currentSub, String type) {
        EditText etName = new EditText(this);
        etName.setText(currentTitle);
        etName.setHint("Title / Name");

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int margin = (int) (20 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = margin;
        params.rightMargin = margin;
        etName.setLayoutParams(params);
        container.addView(etName);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit " + type);
        builder.setView(container);

        builder.setPositiveButton("UPDATE", (dialog, which) -> {
            String newTitle = etName.getText().toString().trim();
            if (newTitle.isEmpty()) return;

            String colName = type.equals("user") ? "users" : (type.equals("exercise") ? "exercises" : "challenges");
            String fieldKey = type.equals("user") ? "name" : "title";

            db.collection(colName).document(docId).update(fieldKey, newTitle)
                    .addOnSuccessListener(unused -> Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("CANCEL", null);
        builder.show();
    }

    private void showAddExerciseDialog() {
        EditText etTitle = new EditText(this);
        etTitle.setHint("Exercise Title (e.g. Dribbling Mastery)");
        EditText etLevel = new EditText(this);
        etLevel.setHint("Level (Beginner/Intermediate/Advanced)");

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int margin = (int) (20 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = margin;
        params.rightMargin = margin;
        etTitle.setLayoutParams(params);
        etLevel.setLayoutParams(params);
        container.addView(etTitle);
        container.addView(etLevel);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Exercise");
        builder.setView(container);

        builder.setPositiveButton("ADD", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String level = etLevel.getText().toString().trim();
            if (title.isEmpty()) return;
            if (level.isEmpty()) level = "Beginner";

            Map<String, Object> map = new HashMap<>();
            map.put("title", title);
            map.put("level", level);
            map.put("category", "Training");

            db.collection("exercises").add(map)
                    .addOnSuccessListener(ref -> Toast.makeText(this, "Exercise added!", Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("CANCEL", null);
        builder.show();
    }

    private void showAddChallengeDialog() {
        EditText etTitle = new EditText(this);
        etTitle.setHint("Challenge Title");
        EditText etType = new EditText(this);
        etType.setHint("Type (Skill / Stamina / Speed)");

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int margin = (int) (20 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = margin;
        params.rightMargin = margin;
        etTitle.setLayoutParams(params);
        etType.setLayoutParams(params);
        container.addView(etTitle);
        container.addView(etType);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Challenge");
        builder.setView(container);

        builder.setPositiveButton("CREATE", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String type = etType.getText().toString().trim();
            if (title.isEmpty()) return;
            if (type.isEmpty()) type = "Skill";

            Map<String, Object> map = new HashMap<>();
            map.put("title", title);
            map.put("type", type);
            map.put("status", "Active");

            db.collection("challenges").add(map)
                    .addOnSuccessListener(ref -> Toast.makeText(this, "Challenge created!", Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("CANCEL", null);
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usersListener != null) usersListener.remove();
        if (exercisesListener != null) exercisesListener.remove();
        if (challengesListener != null) challengesListener.remove();
    }
}
