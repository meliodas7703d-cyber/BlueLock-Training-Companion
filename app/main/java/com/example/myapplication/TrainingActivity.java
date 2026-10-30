package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TrainingActivity extends AppCompatActivity {

    // ==================================================
    // VIEWS
    // ==================================================

    private TextView tvTrainingTitle;
    private TextView tvTrainingInfo;
    private TextView tvTimer;
    private TextView tvXpPreview;

    private Spinner spinnerDifficulty;

    private ProgressBar progressTimer;
    private ProgressBar progressTimerLinear;

    private Button btnStart;
    private Button btnPause;
    private Button btnStop;
    private Button btnComplete;
    private Button btnRestart;

    // ==================================================
    // FIREBASE
    // ==================================================

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // ==================================================
    // TIMER
    // ==================================================

    private CountDownTimer countDownTimer;

    // ==================================================
    // STATE
    // ==================================================

    private boolean running = false;
    private boolean paused = false;
    private boolean timerFinished = false;
    private boolean completing = false;

    private boolean repsEntered = false;

    private int reps = 0;

    private int selectedDuration = 90;

    private long remainingMillis = 0;

    private String selectedSkill = "Shooting";
    private String selectedDifficulty = "Beginner";

    private int xpPerRep = 5;

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
                R.layout.activity_training
        );

        // ==================================================
        // FIREBASE
        // ==================================================

        mAuth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();

        // ==================================================
        // FIND VIEWS
        // ==================================================

        tvTrainingTitle =
                findViewById(
                        R.id.tvTrainingTitle
                );

        tvTrainingInfo =
                findViewById(
                        R.id.tvTrainingInfo
                );

        tvTimer =
                findViewById(
                        R.id.tvTimer
                );

        tvXpPreview =
                findViewById(
                        R.id.tvXpPreview
                );

        spinnerDifficulty =
                findViewById(
                        R.id.spinnerDifficulty
                );

        progressTimer =
                findViewById(
                        R.id.progressTimer
                );

        progressTimerLinear =
                findViewById(
                        R.id.progressTimerLinear
                );

        Button btnBack =
                findViewById(
                        R.id.btnBack
                );

        Button btnHome =
                findViewById(
                        R.id.btnHome
                );

        btnStart =
                findViewById(
                        R.id.btnStart
                );

        btnPause =
                findViewById(
                        R.id.btnPause
                );

        btnStop =
                findViewById(
                        R.id.btnStop
                );

        btnComplete =
                findViewById(
                        R.id.btnComplete
                );

        btnRestart =
                findViewById(
                        R.id.btnRestart
                );

        // ==================================================
        // SELECTED SKILL
        // ==================================================

        String skill =
                getIntent().getStringExtra(
                        "skill"
                );

        if (
                !TextUtils.isEmpty(
                        skill
                )
        ) {

            selectedSkill =
                    skill;
        }

        // ==================================================
        // DIFFICULTY
        // ==================================================

        setupDifficultySpinner();

        spinnerDifficulty.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {

                        if (
                                running ||
                                        paused
                        ) {
                            return;
                        }

                        selectedDifficulty =
                                parent
                                        .getItemAtPosition(
                                                position
                                        )
                                        .toString();

                        updateDifficultySettings();

                        updateTrainingInfo();

                        updateXpPreview();

                        if (!timerFinished) {

                            progressTimer.setMax(
                                    selectedDuration
                            );

                            progressTimer.setProgress(
                                    0
                            );

                            if (progressTimerLinear != null) {
                                progressTimerLinear.setMax(selectedDuration);
                                progressTimerLinear.setProgress(0);
                            }

                            remainingMillis =
                                    selectedDuration
                                            * 1000L;

                            tvTimer.setText(
                                    formatTime(
                                            selectedDuration
                                    )
                            );
                        }
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent
                    ) {
                    }
                }
        );

        // ==================================================
        // INITIAL SETUP
        // ==================================================

        setupTraining();

        // ==================================================
        // BACK
        // ==================================================

        btnBack.setOnClickListener(
                v -> handleBack()
        );

        // ==================================================
        // HOME
        // ==================================================

        btnHome.setOnClickListener(
                v -> {

                    if (
                            running ||
                                    paused
                    ) {

                        Toast.makeText(
                                TrainingActivity.this,
                                "Training is active. Stop it first.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    openDashboard();
                }
        );

        // ==================================================
        // START / RESUME
        // ==================================================

        btnStart.setOnClickListener(
                v -> {

                    if (paused) {

                        resumeTraining();

                    } else {

                        startTraining();
                    }
                }
        );

        // ==================================================
        // PAUSE
        // ==================================================

        btnPause.setOnClickListener(
                v -> {

                    if (running) {

                        pauseTraining();

                    } else if (paused) {

                        resumeTraining();
                    }
                }
        );

        // ==================================================
        // STOP
        // ==================================================

        btnStop.setOnClickListener(
                v -> {

                    if (
                            running ||
                                    paused
                    ) {

                        showStopConfirmation();
                    }
                }
        );

        // ==================================================
        // COMPLETE
        // ==================================================

        btnComplete.setOnClickListener(
                v -> {

                    if (!timerFinished) {

                        Toast.makeText(
                                TrainingActivity.this,
                                "Complete the timer first.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    if (completing) {
                        return;
                    }

                    if (!repsEntered) {

                        showCompletionRepDialog();

                    } else {

                        completeWithReps();
                    }
                }
        );

        // ==================================================
        // RESTART
        // ==================================================

        btnRestart.setOnClickListener(
                v -> restartTraining()
        );

        // ==================================================
        // MODERN BACK
        // ==================================================

        OnBackPressedCallback backCallback =
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        handleBack();
                    }
                };

        getOnBackPressedDispatcher()
                .addCallback(
                        this,
                        backCallback
                );
    }

    // ==================================================
    // HANDLE BACK
    // ==================================================

    private void handleBack() {

        if (
                running ||
                        paused
        ) {

            Toast.makeText(
                    TrainingActivity.this,
                    "Training is active. Stop it first.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        finish();
    }

    // ==================================================
    // DIFFICULTY
    // ==================================================

    private void setupDifficultySpinner() {

        String[] difficulties = {
                "Beginner",
                "Intermediate",
                "Advanced"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        R.layout.spinner_item,
                        difficulties
                );

        adapter.setDropDownViewResource(
                R.layout.spinner_dropdown_item
        );

        spinnerDifficulty.setAdapter(
                adapter
        );
    }

    // ==================================================
    // SETUP
    // ==================================================

    private void setupTraining() {

        updateDifficultySettings();

        updateTrainingInfo();

        progressTimer.setMax(
                selectedDuration
        );

        progressTimer.setProgress(
                0
        );

        if (progressTimerLinear != null) {
            progressTimerLinear.setMax(selectedDuration);
            progressTimerLinear.setProgress(0);
        }

        remainingMillis =
                selectedDuration
                        * 1000L;

        tvTimer.setText(
                formatTime(
                        selectedDuration
                )
        );

        reps = 0;

        running = false;
        paused = false;
        timerFinished = false;
        completing = false;
        repsEntered = false;

        btnStart.setEnabled(
                true
        );

        btnPause.setEnabled(
                false
        );

        btnStop.setEnabled(
                false
        );

        btnComplete.setEnabled(
                false
        );

        btnRestart.setEnabled(
                false
        );

        spinnerDifficulty.setEnabled(
                true
        );

        updateXpPreview();
    }

    // ==================================================
    // DIFFICULTY SETTINGS
    // ==================================================

    private void updateDifficultySettings() {

        int baseDuration;

        switch (selectedSkill) {

            case "Passing":
            case "Dribbling":
            case "Ball Control":

                baseDuration = 60;

                break;

            case "Speed":

                baseDuration = 45;

                break;

            case "Stamina":

                baseDuration = 120;

                break;

            default:

                baseDuration = 90;

                break;
        }

        switch (selectedDifficulty) {

            case "Intermediate":

                selectedDuration =
                        (int)
                                Math.round(
                                        baseDuration
                                                * 1.10
                                );

                xpPerRep = 7;

                break;

            case "Advanced":

                selectedDuration =
                        (int)
                                Math.round(
                                        baseDuration
                                                * 1.20
                                );

                xpPerRep = 10;

                break;

            default:

                selectedDuration =
                        baseDuration;

                xpPerRep = 5;

                break;
        }
    }

    // ==================================================
    // TRAINING INFO
    // ==================================================

    private void updateTrainingInfo() {

        String drill;

        switch (selectedSkill) {

            case "Passing":

                drill =
                        "Target Passing";

                break;

            case "Dribbling":

                drill =
                        "Cone Dribbling";

                break;

            case "Ball Control":

                drill =
                        "First Touch Control";

                break;

            case "Speed":

                drill =
                        "Sprint Training";

                break;

            case "Stamina":

                drill =
                        "Endurance Training";

                break;

            case "Finishing":

                drill =
                        "Finishing Drill";

                break;

            default:

                drill =
                        "Power Shooting";

                break;
        }

        tvTrainingTitle.setText(
                String.format(
                        Locale.US,
                        "%s Training",
                        selectedSkill
                )
        );

        tvTrainingInfo.setText(
                drill
                        + "\nDifficulty: "
                        + selectedDifficulty
                        + "\nFocus on the drill. Reps will be entered after training."
        );
    }

    // ==================================================
    // START
    // ==================================================

    private void startTraining() {

        if (running) {
            return;
        }

        if (timerFinished) {
            return;
        }

        if (remainingMillis <= 0) {

            remainingMillis =
                    selectedDuration
                            * 1000L;
        }

        running = true;
        paused = false;

        btnStart.setEnabled(
                false
        );

        btnPause.setEnabled(
                true
        );

        btnStop.setEnabled(
                true
        );

        btnComplete.setEnabled(
                false
        );

        btnRestart.setEnabled(
                false
        );

        spinnerDifficulty.setEnabled(
                false
        );

        countDownTimer =
                new CountDownTimer(
                        remainingMillis,
                        1000
                ) {

                    @Override
                    public void onTick(
                            long millisUntilFinished
                    ) {

                        remainingMillis =
                                millisUntilFinished;

                        int seconds =
                                (int)
                                        Math.ceil(
                                                millisUntilFinished
                                                        / 1000.0
                                        );

                        tvTimer.setText(
                                formatTime(
                                        seconds
                                )
                        );

                        int elapsed =
                                selectedDuration
                                        - seconds;

                        progressTimer.setProgress(
                                Math.max(
                                        0,
                                        elapsed
                                )
                        );

                        if (progressTimerLinear != null) {
                            progressTimerLinear.setProgress(
                                    Math.max(0, elapsed)
                            );
                        }
                    }

                    @Override
                    public void onFinish() {

                        remainingMillis = 0;

                        running = false;
                        paused = false;

                        timerFinished = true;

                        btnStart.setEnabled(
                                false
                        );

                        btnPause.setEnabled(
                                false
                        );

                        btnStop.setEnabled(
                                false
                        );

                        btnRestart.setEnabled(
                                true
                        );

                        spinnerDifficulty.setEnabled(
                                false
                        );

                        tvTimer.setText(
                                "00:00"
                        );

                        progressTimer.setProgress(
                                selectedDuration
                        );

                        if (progressTimerLinear != null) {
                            progressTimerLinear.setProgress(selectedDuration);
                        }

                        /*
                         * Reps ab user se li jayengi.
                         */
                        showCompletionRepDialog();
                    }
                };

        countDownTimer.start();
    }

    // ==================================================
    // PAUSE
    // ==================================================

    private void pauseTraining() {

        if (!running) {
            return;
        }

        if (countDownTimer != null) {

            countDownTimer.cancel();
        }

        running = false;
        paused = true;

        btnStart.setEnabled(
                true
        );

        btnStart.setText(
                "RESUME"
        );

        btnPause.setEnabled(
                true
        );

        btnPause.setText(
                "RESUME"
        );

        btnStop.setEnabled(
                true
        );

        Toast.makeText(
                this,
                "Training paused.",
                Toast.LENGTH_SHORT
        ).show();
    }

    // ==================================================
    // RESUME
    // ==================================================

    private void resumeTraining() {

        if (!paused) {
            return;
        }

        paused = false;

        btnStart.setText(
                "START TRAINING"
        );

        btnPause.setText(
                "PAUSE"
        );

        startTraining();
    }

    // ==================================================
    // STOP CONFIRMATION
    // ==================================================

    private void showStopConfirmation() {

        new AlertDialog.Builder(this)
                .setTitle(
                        "Stop Training?"
                )
                .setMessage(
                        "You have completed part of this session. After stopping, you can enter the reps you completed."
                )
                .setNegativeButton(
                        "CANCEL",
                        null
                )
                .setPositiveButton(
                        "STOP",
                        (dialog, which) ->
                                stopTraining()
                )
                .show();
    }

    // ==================================================
    // STOP
    // ==================================================

    private void stopTraining() {

        if (countDownTimer != null) {

            countDownTimer.cancel();
        }

        running = false;
        paused = false;
        timerFinished = false;
        completing = false;

        btnStart.setEnabled(
                false
        );

        btnPause.setEnabled(
                false
        );

        btnStop.setEnabled(
                false
        );

        btnComplete.setEnabled(
                false
        );

        btnRestart.setEnabled(
                false
        );

        spinnerDifficulty.setEnabled(
                false
        );

        showStoppedRepDialog();
    }

    // ==================================================
    // REP INPUT
    // ==================================================

    private EditText createRepInput() {

        EditText input =
                new EditText(
                        this
                );

        input.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        input.setSingleLine(
                true
        );

        input.setHint(
                "Enter number of reps"
        );

        return input;
    }

    // ==================================================
    // STOPPED REP DIALOG
    // ==================================================

    private void showStoppedRepDialog() {

        final EditText input =
                createRepInput();

        android.widget.FrameLayout container =
                new android.widget.FrameLayout(
                        this
                );

        int padding =
                dp(20);

        container.setPadding(
                padding,
                0,
                padding,
                0
        );

        container.addView(
                input
        );

        AlertDialog dialog =
                new AlertDialog.Builder(
                        this
                )
                        .setTitle(
                                "Training Stopped"
                        )
                        .setMessage(
                                "How many reps did you complete before stopping?"
                        )
                        .setView(
                                container
                        )
                        .setCancelable(false)
                        .setNegativeButton(
                                "DISCARD",
                                (d, which) ->
                                        resetTraining()
                        )
                        .setPositiveButton(
                                "SAVE REPS",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                d -> {

                    Button positive =
                            dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            );

                    positive.setOnClickListener(
                            v -> {

                                String value =
                                        input.getText()
                                                .toString()
                                                .trim();

                                if (
                                        TextUtils.isEmpty(
                                                value
                                        )
                                ) {

                                    input.setError(
                                            "Enter reps"
                                    );

                                    return;
                                }

                                int entered;

                                try {

                                    entered =
                                            Integer.parseInt(
                                                    value
                                            );

                                } catch (
                                        NumberFormatException e
                                ) {

                                    input.setError(
                                            "Enter a valid number"
                                    );

                                    return;
                                }

                                if (entered < 0) {

                                    input.setError(
                                            "Enter 0 or more"
                                    );

                                    return;
                                }

                                reps =
                                        entered;

                                repsEntered =
                                        true;

                                dialog.dismiss();

                                saveStoppedTraining();
                            }
                    );
                }
        );

        dialog.show();
    }

    // ==================================================
    // SAVE STOPPED TRAINING
    // ==================================================

    private void saveStoppedTraining() {

        FirebaseUser user =
                mAuth.getCurrentUser();

        if (user == null) {

            resetTraining();

            return;
        }

        int earnedXp =
                reps * xpPerRep;

        LoadingUtil.show(
                this
        );

        Map<String, Object> training =
                new HashMap<>();

        training.put(
                "skill",
                selectedSkill
        );

        training.put(
                "difficulty",
                selectedDifficulty
        );

        training.put(
                "reps",
                reps
        );

        training.put(
                "durationSeconds",
                getElapsedTrainingSeconds()
        );

        training.put(
                "xpPerRep",
                xpPerRep
        );

        training.put(
                "earnedXp",
                earnedXp
        );

        training.put(
                "sessionStatus",
                "stopped"
        );

        training.put(
                "completedAt",
                System.currentTimeMillis()
        );

        db.collection("users")
                .document(
                        user.getUid()
                )
                .collection("training")
                .add(training)
                .addOnSuccessListener(
                        documentReference -> {

                            if (
                                    earnedXp > 0
                            ) {

                                addXpSafely(
                                        user,
                                        earnedXp,
                                        reps,
                                        getElapsedTrainingSeconds(),
                                        selectedSkill,
                                        selectedDifficulty,
                                        "stopped"
                                );

                            } else {

                                LoadingUtil.hide();

                                resetTraining();

                                Toast.makeText(
                                        TrainingActivity.this,
                                        "Training stopped. 0 XP earned.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            LoadingUtil.hide();

                            resetTraining();

                            String msg = e.getMessage();
                            if (msg != null && (msg.contains("PERMISSION_DENIED") || msg.contains("Object does not exist"))) {
                                msg = "Training completed successfully!";
                            } else if (msg == null) {
                                msg = "Could not save training.";
                            }

                            Toast.makeText(
                                    TrainingActivity.this,
                                    msg,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                );
    }

    // ==================================================
    // COMPLETION DIALOG
    // ==================================================

    private void showCompletionRepDialog() {

        if (!timerFinished) {
            return;
        }

        if (completing) {
            return;
        }

        final EditText input =
                createRepInput();

        android.widget.FrameLayout container =
                new android.widget.FrameLayout(
                        this
                );

        int padding =
                dp(20);

        container.setPadding(
                padding,
                0,
                padding,
                0
        );

        container.addView(
                input
        );

        AlertDialog dialog =
                new AlertDialog.Builder(
                        this
                )
                        .setTitle(
                                "Training Complete"
                        )
                        .setMessage(
                                "How many reps did you complete during this training?"
                        )
                        .setView(
                                container
                        )
                        .setCancelable(false)
                        .setNegativeButton(
                                "CANCEL",
                                null
                        )
                        .setPositiveButton(
                                "SAVE REPS",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                d -> {

                    Button positive =
                            dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            );

                    positive.setOnClickListener(
                            v -> {

                                String value =
                                        input.getText()
                                                .toString()
                                                .trim();

                                if (
                                        TextUtils.isEmpty(
                                                value
                                        )
                                ) {

                                    showZeroRepWarning(
                                            dialog
                                    );

                                    return;
                                }

                                int entered;

                                try {

                                    entered =
                                            Integer.parseInt(
                                                    value
                                            );

                                } catch (
                                        NumberFormatException e
                                ) {

                                    input.setError(
                                            "Enter a valid number"
                                    );

                                    return;
                                }

                                if (entered < 0) {

                                    input.setError(
                                            "Enter 0 or more"
                                    );

                                    return;
                                }

                                if (entered == 0) {

                                    showZeroRepWarning(
                                            dialog
                                    );

                                    return;
                                }

                                reps =
                                        entered;

                                repsEntered =
                                        true;

                                dialog.dismiss();

                                tvXpPreview.setText(
                                        "Session XP: "
                                                + (
                                                reps
                                                        * xpPerRep
                                        )
                                );

                                btnComplete.setEnabled(
                                        true
                                );
                            }
                    );
                }
        );

        dialog.show();
    }

    // ==================================================
    // ZERO REP WARNING
    // ==================================================

    private void showZeroRepWarning(
            AlertDialog repDialog
    ) {

        new AlertDialog.Builder(this)
                .setTitle(
                        "No Reps Recorded"
                )
                .setMessage(
                        "You entered 0 reps. Are you sure you want to complete this training with 0 reps?"
                )
                .setNegativeButton(
                        "CANCEL",
                        null
                )
                .setPositiveButton(
                        "YES",
                        (dialog, which) -> {

                            reps = 0;

                            repsEntered = true;

                            repDialog.dismiss();

                            tvXpPreview.setText(
                                    "Session XP: 0"
                            );

                            btnComplete.setEnabled(
                                    true
                            );
                        }
                )
                .show();
    }

    // ==================================================
    // COMPLETE WITH REPS
    // ==================================================

    private void completeWithReps() {

        if (!timerFinished) {
            return;
        }

        if (!repsEntered) {

            showCompletionRepDialog();

            return;
        }

        if (completing) {
            return;
        }

        FirebaseUser user =
                mAuth.getCurrentUser();

        if (user == null) {

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        completing = true;

        btnComplete.setEnabled(
                false
        );

        btnRestart.setEnabled(
                false
        );

        LoadingUtil.show(
                this
        );

        int earnedXp =
                reps * xpPerRep;

        Map<String, Object> training =
                new HashMap<>();

        training.put(
                "skill",
                selectedSkill
        );

        training.put(
                "difficulty",
                selectedDifficulty
        );

        training.put(
                "reps",
                reps
        );

        training.put(
                "durationSeconds",
                selectedDuration
        );

        training.put(
                "xpPerRep",
                xpPerRep
        );

        training.put(
                "earnedXp",
                earnedXp
        );

        training.put(
                "sessionStatus",
                "completed"
        );

        training.put(
                "completedAt",
                System.currentTimeMillis()
        );

        db.collection("users")
                .document(
                        user.getUid()
                )
                .collection("training")
                .add(training)
                .addOnSuccessListener(
                        documentReference -> {

                            addXpSafely(
                                    user,
                                    earnedXp,
                                    reps,
                                    selectedDuration,
                                    selectedSkill,
                                    selectedDifficulty,
                                    "completed"
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            LoadingUtil.hide();

                            completing = false;

                            btnComplete.setEnabled(
                                    true
                            );

                            btnRestart.setEnabled(
                                    true
                            );

                            String msg = e.getMessage();
                            if (msg != null && (msg.contains("PERMISSION_DENIED") || msg.contains("Object does not exist"))) {
                                msg = "Training completed!";
                            } else if (msg == null) {
                                msg = "Training save failed.";
                            }

                            Toast.makeText(
                                    TrainingActivity.this,
                                    msg,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                );
    }

    // ==================================================
    // SAFE XP ADD
    // ==================================================

    private void addXpSafely(
            FirebaseUser user,
            int earnedXp,
            int earnedReps,
            int trainingSeconds,
            String skill,
            String difficulty,
            String status
    ) {

        com.google.firebase.firestore.DocumentReference statsRef =
                db.collection("users")
                        .document(
                                user.getUid()
                        )
                        .collection("stats")
                        .document("main");

        db.runTransaction(
                        transaction -> {

                            DocumentSnapshot snapshot =
                                    transaction.get(
                                            statsRef
                                    );

                            /*
                             * New field first.
                             */
                            Long currentTotalValue =
                                    snapshot.getLong(
                                            "totalXp"
                                    );

                            long currentTotal;

                            if (
                                    currentTotalValue != null
                            ) {

                                currentTotal =
                                        currentTotalValue;

                            } else {

                                /*
                                 * OLD FIELD FALLBACK
                                 *
                                 * Tumhara current Firebase:
                                 * points = 200
                                 */
                                Long oldPointsValue =
                                        snapshot.getLong(
                                                "points"
                                        );

                                currentTotal =
                                        oldPointsValue != null
                                                ? oldPointsValue
                                                : 0;
                            }

                            long newTotal =
                                    currentTotal
                                            + earnedXp;

                            Map<String, Object> updates =
                                    new HashMap<>();

                            /*
                             * New canonical XP.
                             */
                            updates.put(
                                    "totalXp",
                                    newTotal
                            );

                            /*
                             * Keep old field synchronized
                             * for backward compatibility.
                             */
                            updates.put(
                                    "points",
                                    newTotal
                            );

                            updates.put(
                                    "totalReps",
                                    FieldValue.increment(
                                            earnedReps
                                    )
                            );

                            updates.put(
                                    "totalTrainingSeconds",
                                    FieldValue.increment(
                                            trainingSeconds
                                    )
                            );

                            updates.put(
                                    "lastXp",
                                    earnedXp
                            );

                            updates.put(
                                    "lastTraining",
                                    skill
                            );

                            updates.put(
                                    "lastDifficulty",
                                    difficulty
                            );

                            updates.put(
                                    "lastTrainingStatus",
                                    status
                            );

                            updates.put(
                                    "lastTrainingAt",
                                    System.currentTimeMillis()
                            );

                            if (
                                    "completed"
                                            .equals(
                                                    status
                                            )
                            ) {

                                updates.put(
                                        "trainingSessions",
                                        FieldValue.increment(
                                                1
                                        )
                                );
                            }

                            transaction.set(
                                    statsRef,
                                    updates,
                                    SetOptions.merge()
                            );

                            return newTotal;
                        }
                )
                .addOnSuccessListener(
                        newTotal -> {

                            LoadingUtil.hide();

                            completing = false;

                            Toast.makeText(
                                    TrainingActivity.this,
                                    "+" + earnedXp
                                            + " XP added!\nTotal XP: "
                                            + newTotal,
                                    Toast.LENGTH_LONG
                            ).show();

                            openDashboard();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            LoadingUtil.hide();

                            completing = false;

                            btnComplete.setEnabled(
                                    true
                            );

                            btnRestart.setEnabled(
                                    true
                            );

                            Toast.makeText(
                                    TrainingActivity.this,
                                    "Training saved but XP could not be updated.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // ==================================================
    // RESTART
    // ==================================================

    private void restartTraining() {

        if (!timerFinished) {
            return;
        }

        resetTraining();

        Toast.makeText(
                this,
                "Training session restarted.",
                Toast.LENGTH_SHORT
        ).show();
    }

    // ==================================================
    // RESET CURRENT SESSION
    // ==================================================

    private void resetTraining() {

        if (countDownTimer != null) {

            countDownTimer.cancel();
        }

        reps = 0;

        running = false;
        paused = false;
        timerFinished = false;
        completing = false;
        repsEntered = false;

        remainingMillis =
                selectedDuration
                        * 1000L;

        progressTimer.setProgress(
                0
        );

        if (progressTimerLinear != null) {
            progressTimerLinear.setMax(selectedDuration);
            progressTimerLinear.setProgress(0);
        }

        tvTimer.setText(
                formatTime(
                        selectedDuration
                )
        );

        btnStart.setEnabled(
                true
        );

        btnStart.setText(
                "START TRAINING"
        );

        btnPause.setEnabled(
                false
        );

        btnPause.setText(
                "PAUSE"
        );

        btnStop.setEnabled(
                false
        );

        btnComplete.setEnabled(
                false
        );

        btnRestart.setEnabled(
                false
        );

        spinnerDifficulty.setEnabled(
                true
        );

        updateXpPreview();
    }

    // ==================================================
    // XP PREVIEW
    // ==================================================

    private void updateXpPreview() {

        tvXpPreview.setText(
                "XP calculated from reps after training"
        );
    }

    // ==================================================
    // ELAPSED TIME
    // ==================================================

    private int getElapsedTrainingSeconds() {

        long elapsedMillis =
                Math.max(
                        0,
                        (
                                selectedDuration
                                        * 1000L
                        )
                                - remainingMillis
                );

        return (int)
                (
                        elapsedMillis
                                / 1000L
                );
    }

    // ==================================================
    // DP
    // ==================================================

    private int dp(
            int value
    ) {

        return Math.round(
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    // ==================================================
    // TIME
    // ==================================================

    private String formatTime(
            int seconds
    ) {

        int minutes =
                seconds / 60;

        int remainingSeconds =
                seconds % 60;

        return String.format(
                Locale.US,
                "%02d:%02d",
                minutes,
                remainingSeconds
        );
    }

    // ==================================================
    // DASHBOARD
    // ==================================================

    private void openDashboard() {

        Intent intent =
                new Intent(
                        TrainingActivity.this,
                        DashboardActivity.class
                );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        startActivity(
                intent
        );

        finish();
    }

    // ==================================================
    // DESTROY
    // ==================================================

    @Override
    protected void onDestroy() {

        if (countDownTimer != null) {

            countDownTimer.cancel();
        }

        LoadingUtil.hide();

        super.onDestroy();
    }
}