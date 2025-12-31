package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class forgotpass extends AppCompatActivity {

    private EditText etForgotEmail;
    private Button btnResetPassword, btnBackToLogin;
    private ImageView ivLogo;
    private TextView tvTitle;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpass);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        etForgotEmail = findViewById(R.id.etForgotEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        ivLogo = findViewById(R.id.ivLogo);
        tvTitle = findViewById(R.id.tvTitle);

        // Reset Password Button Click
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etForgotEmail.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(forgotpass.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isValidEmail(email)) {
                    Toast.makeText(forgotpass.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Send password reset email
                sendPasswordResetEmail(email);
            }
        });

        // Back to Login Button Click
        btnBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(forgotpass.this, Loginpage.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // Email validation method
    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
        return email.matches(emailPattern);
    }

    private void sendPasswordResetEmail(String email) {
        // Show loading/progress
        btnResetPassword.setText("Sending...");
        btnResetPassword.setEnabled(false);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        // Reset button state
                        btnResetPassword.setText("Reset Password");
                        btnResetPassword.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(forgotpass.this,
                                    "✅ Password reset email sent to: " + email,
                                    Toast.LENGTH_LONG).show();

                            // Clear email field
                            etForgotEmail.setText("");

                            // Optional: Auto redirect to login after 3 seconds
                            new android.os.Handler().postDelayed(
                                    new Runnable() {
                                        public void run() {
                                            Intent intent = new Intent(forgotpass.this, Loginpage.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    },
                                    3000);

                        } else {
                            Toast.makeText(forgotpass.this,
                                    "❌ Failed to send reset email: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(forgotpass.this, Loginpage.class);
        startActivity(intent);
        finish();
    }
}