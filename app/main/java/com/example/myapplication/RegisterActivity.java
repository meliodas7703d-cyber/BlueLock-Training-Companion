package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvBack, tvRegisterStatus;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        // Phone field is still in XML but we will ignore it or remove it from logic
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvBack = findViewById(R.id.tvBack);
        tvRegisterStatus = findViewById(R.id.tvRegisterStatus);

        btnRegister.setOnClickListener(v -> createAccount());
        tvBack.setOnClickListener(v -> finish());
    }

    private void createAccount() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) { etName.setError("Name required"); return; }
        if (TextUtils.isEmpty(email)) { etEmail.setError("Email required"); return; }
        if (password.length() < 6) { etPassword.setError("Min 6 characters"); return; }
        if (!password.equals(confirm)) { etConfirmPassword.setError("Passwords do not match"); return; }

        btnRegister.setEnabled(false);
        LoadingUtil.show(this);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Save user to Firestore 'users' collection
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("email", email);
                            userData.put("name", name);
                            userData.put("createdAt", FieldValue.serverTimestamp());
                            userData.put("role", email.toLowerCase().contains("admin") ? "Admin" : "Client");
                            db.collection("users").document(user.getUid()).set(userData);

                            // Also track in config/app_stats for reliable admin viewing across security rules
                            db.collection("config").document("app_stats")
                                    .update("registered_users", FieldValue.arrayUnion(email))
                                    .addOnFailureListener(err -> {
                                        Map<String, Object> initStats = new HashMap<>();
                                        java.util.List<String> list = new java.util.ArrayList<>();
                                        list.add(email);
                                        initStats.put("registered_users", list);
                                        db.collection("config").document("app_stats").set(initStats);
                                    });

                            // 1. Set Display Name
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                            
                            user.updateProfile(profileUpdates);

                            // 2. Send Verification Email (FREE & UNLIMITED)
                            user.sendEmailVerification()
                                    .addOnCompleteListener(verifyTask -> {
                                        LoadingUtil.hide();
                                        if (verifyTask.isSuccessful()) {
                                            Toast.makeText(RegisterActivity.this, 
                                                "Verification link sent to your email. Please verify before logging in.", 
                                                Toast.LENGTH_LONG).show();
                                            
                                            // Sign out so they have to login after verification
                                            mAuth.signOut();
                                            finish();
                                        } else {
                                            tvRegisterStatus.setText("Error sending verification email.");
                                            btnRegister.setEnabled(true);
                                        }
                                    });
                        }
                    } else {
                        LoadingUtil.hide();
                        btnRegister.setEnabled(true);
                        tvRegisterStatus.setText(task.getException() != null ? task.getException().getMessage() : "Registration failed");
                    }
                });
    }
}
