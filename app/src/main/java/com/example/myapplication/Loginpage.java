package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Loginpage extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnPrevious, btnNext;
    private TextView tvSignUp, tvForgotPassword;

    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;

    // Session timeout (30 minutes) - TESTING KE LIYE 10 SECONDS KARDO
    private static final long SESSION_TIMEOUT = 10 * 1000; // 10 seconds for testing
    // REAL USE KE LIYE: private static final long SESSION_TIMEOUT = 30 * 60 * 1000;

    private static final String PREF_NAME = "MyAppSession";
    private static final String KEY_LOGIN_TIME = "login_time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginpage);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize SharedPreferences for session
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Initialize views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // ✅ SHOW CURRENT SESSION STATUS ON CREATE
        showCurrentSessionStatus();

        // Login button click
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(Loginpage.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                loginUser(email, password);
            }
        });

        // Previous button click
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Loginpage.this, Signuppage.class);
                startActivity(intent);
                finish();
            }
        });

        // Next button click
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    // ✅ CHECK SESSION BEFORE GOING TO QR
                    if (isSessionValid()) {
                        Toast.makeText(Loginpage.this, "✅ Session valid! Going to QR...", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Loginpage.this, Qr.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(Loginpage.this, "❌ Session expired! Please login again.", Toast.LENGTH_SHORT).show();
                        clearSession();
                    }
                } else {
                    Toast.makeText(Loginpage.this, "Please login first", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Sign Up text click
        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Loginpage.this, Signuppage.class);
                startActivity(intent);
                finish();
            }
        });

        // Forgot Password text click - DIRECT OPEN NEW ACTIVITY
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open ForgotPasswordActivity directly
                Intent intent = new Intent(Loginpage.this, forgotpass.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // ✅ YAHAN SESSION CHECK HOGI WITH TOAST MESSAGES
        Toast.makeText(this, "🔍 Checking session...", Toast.LENGTH_SHORT).show();
        checkSession();
    }

    // ✅ SHOW CURRENT SESSION STATUS
    private void showCurrentSessionStatus() {
        long lastLoginTime = sharedPreferences.getLong(KEY_LOGIN_TIME, 0);
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (lastLoginTime == 0) {
            Toast.makeText(this, "📝 Never logged in before", Toast.LENGTH_LONG).show();
        } else {
            long currentTime = System.currentTimeMillis();
            long minutesAgo = (currentTime - lastLoginTime) / (60 * 1000);
            long secondsAgo = (currentTime - lastLoginTime) / 1000;

            String userStatus = currentUser != null ? "User: " + currentUser.getEmail() : "No Firebase user";
            String timeStatus = "Last login: " + minutesAgo + " minutes (" + secondsAgo + " seconds) ago";

            Toast.makeText(this, userStatus + "\n" + timeStatus, Toast.LENGTH_LONG).show();
        }
    }

    // ==================== SESSION HANDLING ====================
    private void loginUser(String email, String password) {
        Toast.makeText(this, "🔐 Attempting login...", Toast.LENGTH_SHORT).show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new com.google.android.gms.tasks.OnCompleteListener<com.google.firebase.auth.AuthResult>() {
                    @Override
                    public void onComplete(Task<com.google.firebase.auth.AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Login successful
                            FirebaseUser user = mAuth.getCurrentUser();

                            // ✅ SESSION SAVE KARO WITH MESSAGE
                            saveSession();

                            Toast.makeText(Loginpage.this, "✅ Successfully Logged In!", Toast.LENGTH_SHORT).show();
                            Toast.makeText(Loginpage.this, "👋 Welcome " + user.getEmail(), Toast.LENGTH_LONG).show();

                            // 2 second delay to show messages
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(Loginpage.this, Qr.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }, 2000);

                        } else {
                            // Login failed
                            Toast.makeText(Loginpage.this,
                                    "❌ Login failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // ✅ SESSION SAVE KARNE KA METHOD WITH TOAST
    private void saveSession() {
        long currentTime = System.currentTimeMillis();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_LOGIN_TIME, currentTime);
        editor.apply();

        Toast.makeText(this, "💾 Session saved at: " +
                        new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date(currentTime)),
                Toast.LENGTH_LONG).show();
    }

    // ✅ SESSION CHECK KARNE KA METHOD WITH DETAILS
    private boolean isSessionValid() {
        long lastLoginTime = sharedPreferences.getLong(KEY_LOGIN_TIME, 0);
        long currentTime = System.currentTimeMillis();

        // Agar kabhi login hi nahi kiya
        if (lastLoginTime == 0) {
            Toast.makeText(this, "⏰ Never logged in before", Toast.LENGTH_SHORT).show();
            return false;
        }

        long timeDifference = currentTime - lastLoginTime;
        long secondsDifference = timeDifference / 1000;

        // Show session details
        String lastLoginTimeStr = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date(lastLoginTime));
        String currentTimeStr = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date(currentTime));

        Toast.makeText(this,
                "⏰ Last login: " + lastLoginTimeStr +
                        "\n🕒 Current: " + currentTimeStr +
                        "\n⏱️ Difference: " + secondsDifference + " seconds",
                Toast.LENGTH_LONG).show();

        // Check if session expired (10 seconds se zyada ho gaya - TESTING)
        boolean isValid = timeDifference < SESSION_TIMEOUT;

        if (isValid) {
            long timeLeft = (SESSION_TIMEOUT - timeDifference) / 1000;
            Toast.makeText(this, "✅ Session valid! Time left: " + timeLeft + " seconds", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "❌ Session expired! " + secondsDifference + " seconds passed", Toast.LENGTH_LONG).show();
        }

        return isValid;
    }

    // ✅ AUTOMATIC SESSION CHECK WITH TOAST MESSAGES
    private void checkSession() {
        FirebaseUser user = mAuth.getCurrentUser();

        // Show Firebase user status
        if (user != null) {
            Toast.makeText(this, "🔥 Firebase User: " + user.getEmail(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "🔥 No Firebase user", Toast.LENGTH_SHORT).show();
        }

        // Agar Firebase user logged in hai AUR session valid hai
        if (user != null && isSessionValid()) {
            Toast.makeText(this, "🚀 Auto-login starting...", Toast.LENGTH_SHORT).show();

            // 3 second delay to show all messages
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Auto login - direct QR page par le jao
                    Intent intent = new Intent(Loginpage.this, Qr.class);
                    startActivity(intent);
                    finish();
                }
            }, 3000);

        } else {
            // Reason show karo
            String reason = "";
            if (user == null) {
                reason = "No Firebase user found";
            } else if (!isSessionValid()) {
                reason = "Session expired";
            }

            Toast.makeText(this, "⚠️ " + reason + " - Showing login screen", Toast.LENGTH_LONG).show();

            // Session expired ya user not logged in
            // Login screen hi dikhao
            clearSession();
        }
    }

    // ✅ SESSION CLEAR KARO WITH TOAST
    private void clearSession() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_LOGIN_TIME);
        editor.apply();
        mAuth.signOut();

        Toast.makeText(this, "🧹 Session cleared!", Toast.LENGTH_SHORT).show();
    }
}