package com.example.myapplication;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HelpFeedbackActivity extends AppCompatActivity {

    // IMPORTANT: Replace with your actual Gemini API Key
    // Get one at: https://aistudio.google.com/app/apikey
    private static final String GEMINI_API_KEY = "AQ.Ab8RN6IDsuAXKYrn2ngJqPgHJUryVQDtUUzLwnckj6E_DLhiOg";

    private LinearLayout chatContainer;
    private ScrollView chatScrollView;
    private EditText etMessage;
    private Button btnSend;

    private GenerativeModelFutures model;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_feedback);

        chatContainer = findViewById(R.id.chatContainer);
        chatScrollView = findViewById(R.id.chatScrollView);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        findViewById(R.id.btnBack).setOnClickListener(v -> showExitFeedbackDialog());

        btnSend.setOnClickListener(v -> handleSend());

        initGemini();

        // Initial AI Message
        addMessage("AI ASSISTANT", "I am your Blue Lock AI training assistant. Ask me anything about your evolution, training, or why this app is your best weapon. How can I help you today?", false);

        // Handle system back button
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitFeedbackDialog();
            }
        });
    }

    private void initGemini() {
        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", GEMINI_API_KEY);
        model = GenerativeModelFutures.from(gm);
    }

    private void handleSend() {
        String message = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(message)) return;

        etMessage.setText("");
        addMessage("YOU", message, true);
        processAIResponse(message);
    }

    private void processAIResponse(String userQuery) {
        if (GEMINI_API_KEY.equals("YOUR_API_KEY_HERE")) {
            processLocalResponse(userQuery);
            return;
        }

        addMessage("AI ASSISTANT", "Thinking...", false);

        String systemPrompt = "You are a professional training assistant for the 'Blue Lock Companion' app. " +
                "This app helps football players train skills like Shooting, Passing, Dribbling, Ball Control, Speed, Stamina, and Finishing. " +
                "Users earn XP and rank up from Rookie to Blue Lock Monster. " +
                "Answer user queries about the app or football in an encouraging, competitive, and intense tone similar to the Blue Lock anime. " +
                "Keep responses concise (max 3 sentences). " +
                "Current user query: " + userQuery;

        Content content = new Content.Builder()
                .addText(systemPrompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String aiText = result.getText();
                if (aiText == null) aiText = "I encountered an error in my data banks.";
                
                final String finalResponse = aiText;
                
                runOnUiThread(() -> {
                    if (chatContainer.getChildCount() > 0) {
                        chatContainer.removeViewAt(chatContainer.getChildCount() - 1);
                    }
                    addMessage("AI ASSISTANT", finalResponse, false);
                });
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                runOnUiThread(() -> {
                    if (chatContainer.getChildCount() > 0) {
                        chatContainer.removeViewAt(chatContainer.getChildCount() - 1);
                    }
                    processLocalResponse(userQuery);
                });
            }
        }, executor);
    }

    private void processLocalResponse(String userQuery) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String response;
            String query = userQuery.toLowerCase();
            
            if (query.contains("why should i choose") || query.contains("why blue lock companion") || query.contains("why this app")) {
                response = "Because it's the only app designed to cultivate your EGO. It provides real-time tracking, skill-specific training drills, and a ranking system that pushes you to become a true monster on the pitch!";
            } else if (query.contains("training")) {
                response = "You can start training sessions from the dashboard. Each session is a step towards your evolution!";
            } else if (query.contains("xp") || query.contains("level") || query.contains("rank")) {
                response = "XP is your fuel. Earn it by completing reps and climb the ranks from Rookie to Master Striker!";
            } else {
                response = "I'm here to assist your evolution. Ask about training, XP, or why you need this app to win.";
            }
            addMessage("AI ASSISTANT", response, false);
        }, 1000);
    }

    private void showExitFeedbackDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Before you go...");
        builder.setMessage("How is your experience with the Blue Lock Companion app so far?");

        final EditText input = new EditText(this);
        input.setHint("Enter your feedback here...");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setLines(3);
        input.setGravity(Gravity.TOP);

        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, 
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        int margin = dp(20);
        params.leftMargin = margin;
        params.rightMargin = margin;
        input.setLayoutParams(params);
        container.addView(input);

        builder.setView(container);

        builder.setPositiveButton("SAVE", (dialog, which) -> {
            String feedback = input.getText().toString().trim();
            if (!TextUtils.isEmpty(feedback)) {
                saveFeedbackAndExit(feedback);
            } else {
                finish();
            }
        });

        builder.setNegativeButton("CANCEL", (dialog, which) -> finish());

        builder.show();
    }

    private void saveFeedbackAndExit(String feedback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("uid", user.getUid());
        data.put("email", user.getEmail());
        data.put("feedback", feedback);
        data.put("timestamp", System.currentTimeMillis());

        FirebaseFirestore.getInstance().collection("app_feedback")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(HelpFeedbackActivity.this, "Feedback saved. Thank you!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(HelpFeedbackActivity.this, "Error saving feedback.", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void addMessage(String sender, String text, boolean isUser) {
        LinearLayout messageLayout = new LinearLayout(this);
        messageLayout.setOrientation(LinearLayout.VERTICAL);
        messageLayout.setPadding(0, 0, 0, 24);

        TextView tvSender = new TextView(this);
        tvSender.setText(sender);
        tvSender.setTextColor(isUser ? 0xFF64B5F6 : 0xFF9FB6D1);
        tvSender.setTextSize(10);
        tvSender.setTypeface(null, Typeface.BOLD);

        TextView tvText = new TextView(this);
        tvText.setText(text);
        tvText.setTextColor(Color.WHITE);
        tvText.setTextSize(14);
        
        if (isUser) {
            tvText.setBackgroundResource(R.drawable.bg_chat_user);
            tvText.setPadding(32, 16, 32, 16);
            messageLayout.setGravity(Gravity.END);
            tvSender.setGravity(Gravity.END);
        } else {
            tvText.setPadding(0, 8, 48, 0);
            messageLayout.setGravity(Gravity.START);
        }

        messageLayout.addView(tvSender);
        messageLayout.addView(tvText);
        chatContainer.addView(messageLayout);

        chatScrollView.post(() -> chatScrollView.fullScroll(View.FOCUS_DOWN));
    }

    private int dp(int px) {
        return (int) (px * getResources().getDisplayMetrics().density);
    }
}
