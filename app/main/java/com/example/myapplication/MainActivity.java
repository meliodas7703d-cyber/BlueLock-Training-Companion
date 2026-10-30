package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private VideoView openingVideo;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // -----------------------------------------
        // FULL SCREEN
        // -----------------------------------------

        getWindow().setStatusBarColor(Color.TRANSPARENT);
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
        // FIREBASE AUTH
        // -----------------------------------------

        mAuth = FirebaseAuth.getInstance();

        // -----------------------------------------
        // IMPORTANT:
        // OPENING VIDEO WILL ALWAYS PLAY FIRST
        // -----------------------------------------

        setContentView(R.layout.activity_main);

        openingVideo = findViewById(
                R.id.openingVideo
        );

        Uri videoUri = Uri.parse(
                "android.resource://"
                        + getPackageName()
                        + "/"
                        + R.raw.opening
        );

        openingVideo.setVideoURI(videoUri);

        // -----------------------------------------
        // VIDEO READY
        // -----------------------------------------

        openingVideo.setOnPreparedListener(
                mediaPlayer -> {

                    mediaPlayer.setLooping(false);

                    openingVideo.start();
                }
        );

        // -----------------------------------------
        // VIDEO FINISHED
        // -----------------------------------------

        openingVideo.setOnCompletionListener(
                mediaPlayer -> goToNextScreen()
        );

        // -----------------------------------------
        // VIDEO ERROR
        // -----------------------------------------

        openingVideo.setOnErrorListener(
                (mediaPlayer, what, extra) -> {

                    /*
                     * Opening video fail ho jaye to bhi
                     * normal app flow continue karega.
                     */
                    goToNextScreen();

                    return true;
                }
        );
    }

    private void goToNextScreen() {

        FirebaseUser user =
                mAuth.getCurrentUser();

        if (user != null) {

            // =====================================
            // USER ALREADY LOGGED IN
            // =====================================

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            DashboardActivity.class
                    );

            startActivity(intent);
            finish();

        } else {

            // =====================================
            // USER NOT LOGGED IN
            // =====================================

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            LoginActivity.class
                    );

            startActivity(intent);
            finish();
        }
    }
}