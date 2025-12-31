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
    TextView tvLogin;

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

        btnSignUp.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(Signuppage.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isValidName = true;
            for (int i = 0; i < name.length(); i++) {
                char c = name.charAt(i);
                if (!Character.isLetter(c) && c != ' ') {
                    isValidName = false;
                    break;
                }
            }

            if (!isValidName) {
                Toast.makeText(Signuppage.this, "Name should contain only letters (A-Z) and spaces", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(Signuppage.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(Signuppage.this, "Password should be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            createUserWithEmailPassword(name, email, password);
        });

        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(Signuppage.this, Loginpage.class);
            startActivity(intent);
            finish(); // add finish
        });

        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(Signuppage.this, Loginpage.class);
            startActivity(intent);
            finish(); // add finish
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
                        Toast.makeText(Signuppage.this, "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserDataToFirestore(String userId, String name, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("createdAt", System.currentTimeMillis());

        db.collection("users")
                .document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> Toast.makeText(Signuppage.this, "Profile created successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(Signuppage.this, "Failed to save profile data", Toast.LENGTH_SHORT).show());
    }
}
