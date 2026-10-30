package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class TrainingSelectionActivity extends AppCompatActivity {

    private Button btnShooting;
    private Button btnPassing;
    private Button btnDribbling;
    private Button btnBallControl;
    private Button btnSpeed;
    private Button btnStamina;
    private Button btnFinishing;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_training_selection
        );

        btnShooting =
                findViewById(R.id.btnShooting);

        btnPassing =
                findViewById(R.id.btnPassing);

        btnDribbling =
                findViewById(R.id.btnDribbling);

        btnBallControl =
                findViewById(R.id.btnBallControl);

        btnSpeed =
                findViewById(R.id.btnSpeed);

        btnStamina =
                findViewById(R.id.btnStamina);

        btnFinishing =
                findViewById(R.id.btnFinishing);

        btnBack =
                findViewById(R.id.btnBack);

        btnShooting.setOnClickListener(
                v -> openTraining("Shooting")
        );

        btnPassing.setOnClickListener(
                v -> openTraining("Passing")
        );

        btnDribbling.setOnClickListener(
                v -> openTraining("Dribbling")
        );

        btnBallControl.setOnClickListener(
                v -> openTraining("Ball Control")
        );

        btnSpeed.setOnClickListener(
                v -> openTraining("Speed")
        );

        btnStamina.setOnClickListener(
                v -> openTraining("Stamina")
        );

        btnFinishing.setOnClickListener(
                v -> openTraining("Finishing")
        );

        btnBack.setOnClickListener(
                v -> finish()
        );
    }

    private void openTraining(
            String skill
    ) {

        Intent intent =
                new Intent(
                        TrainingSelectionActivity.this,
                        TrainingActivity.class
                );

        intent.putExtra(
                "skill",
                skill
        );

        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}