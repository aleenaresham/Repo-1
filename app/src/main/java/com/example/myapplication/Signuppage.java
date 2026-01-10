package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Signuppage extends AppCompatActivity {

    EditText etName, etEmail, etPassword, etConfirmPassword;
    Button btnSignUp, btnNext;
    LinearLayout btnGoogle, btnMicrosoft;
    TextView tvLogin, tvNameValidation, tvPasswordStrength; // ✅ Added tvNameValidation

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signuppage);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnSignUp = findViewById(R.id.btnSignUp);
        btnNext = findViewById(R.id.btnNext);

        btnGoogle = findViewById(R.id.btnGoogle);
        btnMicrosoft = findViewById(R.id.btnMicrosoft);

        tvLogin = findViewById(R.id.tvLogin);
        tvNameValidation = findViewById(R.id.tvNameValidation); // ✅ Initialize
        tvPasswordStrength = findViewById(R.id.tvPasswordStrength);

        // ✅ REAL-TIME: Name Validation
        etName.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = s.toString();
                if (name.length() > 0) {
                    String validationMsg = TextUtils.getNameValidationMessage(name);
                    tvNameValidation.setText(validationMsg);

                    // Color coding for name
                    if (validationMsg.startsWith("✅")) {
                        tvNameValidation.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    } else if (validationMsg.startsWith("⚠️")) {
                        tvNameValidation.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                    } else {
                        tvNameValidation.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    }
                } else {
                    tvNameValidation.setText("");
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // ✅ REAL-TIME: Password Strength Check
        etPassword.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s.toString();
                if (password.length() > 0) {
                    String strengthMessage = TextUtils.getPasswordStrengthMessage(password);
                    tvPasswordStrength.setText(strengthMessage);

                    // Color coding for password
                    if (strengthMessage.startsWith("✅")) {
                        tvPasswordStrength.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    } else if (strengthMessage.startsWith("⚠️")) {
                        tvPasswordStrength.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                    } else {
                        tvPasswordStrength.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    }
                } else {
                    tvPasswordStrength.setText("");
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        btnSignUp.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // ✅ Using TextUtils for validation
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) ||
                    TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(Signuppage.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Name validation
            if (!TextUtils.isValidName(name)) {
                String nameMessage = TextUtils.getNameValidationMessage(name);
                Toast.makeText(Signuppage.this,
                        "Invalid name: " + nameMessage.replace("❌", "").replace("✅", ""),
                        Toast.LENGTH_LONG).show();
                return;
            }

            // ✅ Email validation
            if (!TextUtils.isValidEmail(email)) {
                Toast.makeText(Signuppage.this,
                        "Please enter a valid email address",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Password match validation
            if (!password.equals(confirmPassword)) {
                Toast.makeText(Signuppage.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Password strength validation
            if (!TextUtils.isStrongPassword(password)) {
                String message = TextUtils.getPasswordStrengthMessage(password);
                Toast.makeText(Signuppage.this,
                        "Weak password! " + message.replace("❌", "").replace("✅", "").replace("⚠️", ""),
                        Toast.LENGTH_LONG).show();
                return;
            }

            // All validations passed
            createUserWithEmailPassword(name, email, password);
        });

        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(Signuppage.this, Loginpage.class);
            startActivity(intent);
            finish();
        });

        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(Signuppage.this, Loginpage.class);
            startActivity(intent);
            finish();
        });

        btnGoogle.setOnClickListener(v -> Toast.makeText(Signuppage.this, "Google Sign Up clicked", Toast.LENGTH_SHORT).show());
        btnMicrosoft.setOnClickListener(v -> Toast.makeText(Signuppage.this, "Microsoft Sign Up clicked", Toast.LENGTH_SHORT).show());
    }

    private void createUserWithEmailPassword(String name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        saveUserDataToFirestore(user.getUid(), name, email);

                        Toast.makeText(Signuppage.this, "Registration Successful!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(Signuppage.this, Loginpage.class);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMessage = "Registration failed";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                            if (errorMessage.contains("email address is already")) {
                                errorMessage = "This email is already registered. Please login.";
                            }
                        }
                        Toast.makeText(Signuppage.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserDataToFirestore(String userId, String name, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("createdAt", System.currentTimeMillis());
        user.put("passwordStrength", TextUtils.getPasswordStrengthMessage(etPassword.getText().toString()));

        db.collection("users")
                .document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> Toast.makeText(Signuppage.this, "Profile created successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(Signuppage.this, "Failed to save profile data", Toast.LENGTH_SHORT).show());
    }
}