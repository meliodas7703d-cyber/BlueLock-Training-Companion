package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvCreateAccount, tvLoginStatus, tvForgotPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        Button btnLoginAdmin = findViewById(R.id.btnLoginAdmin);
        tvCreateAccount = findViewById(R.id.tvCreateAccount);
        tvLoginStatus = findViewById(R.id.tvLoginStatus);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnLogin.setOnClickListener(v -> loginUser());
        btnLoginAdmin.setOnClickListener(v -> showAdminLoginPrompt());
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
        tvCreateAccount.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) { etEmail.setError("Email required"); return; }
        if (TextUtils.isEmpty(password)) { etPassword.setError("Password required"); return; }

        LoadingUtil.show(this);
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null && user.isEmailVerified()) {
                    LoadingUtil.hide();
                    startActivity(new Intent(this, IntroVideoActivity.class));
                    finish();
                } else if (user != null) {
                    LoadingUtil.hide();
                    mAuth.signOut();
                    tvLoginStatus.setText("Please verify your email via the link sent to you.");
                    Toast.makeText(this, "Email not verified.", Toast.LENGTH_LONG).show();
                }
            } else {
                LoadingUtil.hide();
                tvLoginStatus.setText(task.getException() != null ? task.getException().getMessage() : "Login failed");
            }
        });
    }

    private void showForgotPasswordDialog() {
        EditText resetMail = new EditText(this);
        resetMail.setHint("Enter your email address");
        resetMail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int margin = (int) (20 * getResources().getDisplayMetrics().density);
        params.leftMargin = margin;
        params.rightMargin = margin;
        resetMail.setLayoutParams(params);
        container.addView(resetMail);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password?");
        builder.setMessage("A reset link will be sent to your email.");
        builder.setView(container);

        builder.setPositiveButton("SEND LINK", (dialog, which) -> {
            String mail = resetMail.getText().toString().trim();
            if (TextUtils.isEmpty(mail)) {
                Toast.makeText(this, "Email required", Toast.LENGTH_SHORT).show();
                return;
            }
            
            LoadingUtil.show(this);
            mAuth.sendPasswordResetEmail(mail).addOnSuccessListener(unused -> {
                LoadingUtil.hide();
                Toast.makeText(this, "Reset link sent to your email.", Toast.LENGTH_LONG).show();
            }).addOnFailureListener(e -> {
                LoadingUtil.hide();
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        });

        builder.setNegativeButton("CANCEL", null);
        builder.show();
    }

    private void showAdminLoginPrompt() {
        EditText etAdminEmail = new EditText(this);
        etAdminEmail.setHint("Admin Email (e.g. admin@bluelock.com)");
        etAdminEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        EditText etAdminKey = new EditText(this);
        etAdminKey.setHint("Admin Key (Master Key: admin123)");
        etAdminKey.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int margin = (int) (20 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = margin;
        params.rightMargin = margin;
        etAdminEmail.setLayoutParams(params);
        etAdminKey.setLayoutParams(params);
        container.addView(etAdminEmail);
        container.addView(etAdminKey);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Admin Authentication");
        builder.setMessage("Enter Admin Email and Security Key:\n(Use Master Key: admin123)");
        builder.setView(container);

        builder.setPositiveButton("LOGIN AS ADMIN", (dialog, which) -> {
            String email = etAdminEmail.getText().toString().trim();
            String key = etAdminKey.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(key)) {
                Toast.makeText(this, "Email and Key required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!key.equals("admin123") && !key.equals("admin")) {
                Toast.makeText(this, "Invalid Master Key! Use: admin123", Toast.LENGTH_LONG).show();
                return;
            }

            LoadingUtil.show(this);

            // Use dedicated master admin credentials in Firebase Auth to guarantee successful authentication
            String masterAdminEmail = "admin@bluelock.com";
            String masterAdminPass = "admin123";

            mAuth.signInWithEmailAndPassword(masterAdminEmail, masterAdminPass).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    grantAdminAndProceed(task.getResult().getUser(), email);
                } else {
                    mAuth.createUserWithEmailAndPassword(masterAdminEmail, masterAdminPass).addOnCompleteListener(createTask -> {
                        if (createTask.isSuccessful()) {
                            grantAdminAndProceed(createTask.getResult().getUser(), email);
                        } else {
                            LoadingUtil.hide();
                            Toast.makeText(this, "Error: " + createTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        });

        builder.setNegativeButton("CANCEL", null);
        builder.show();
    }

    private void grantAdminAndProceed(FirebaseUser user, String enteredEmail) {
        if (user != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("email", enteredEmail);
            updates.put("role", "Admin");
            updates.put("status", "Active");
            updates.put("name", "Administrator");

            FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                    .set(updates, SetOptions.merge())
                    .addOnSuccessListener(unused -> {
                        LoadingUtil.hide();
                        Toast.makeText(this, "Admin Login Successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, AdminPanelActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        LoadingUtil.hide();
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }
}
