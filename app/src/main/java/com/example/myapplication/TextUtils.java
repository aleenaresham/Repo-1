package com.example.myapplication;

import android.util.Patterns;

public class TextUtils {

    // Check if string is empty or null
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    // Email validation
    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) return false;
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // ✅ IMPROVED: Name validation
    public static boolean isValidName(String name) {
        if (isEmpty(name)) return false;

        // Remove extra spaces
        name = name.trim();

        // Minimum 2 characters
        if (name.length() < 2) return false;

        // Check each character - only letters and spaces allowed
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isLetter(c) && c != ' ') {
                return false;
            }
        }

        // Should contain at least one letter
        for (int i = 0; i < name.length(); i++) {
            if (Character.isLetter(name.charAt(i))) {
                return true;
            }
        }

        return false;
    }

    // ✅ REAL-TIME: Get name validation message
    public static String getNameValidationMessage(String name) {
        if (isEmpty(name)) {
            return "Enter your name";
        }

        name = name.trim();

        if (name.length() < 2) {
            return "❌ At least 2 characters";
        }

        // Check for invalid characters
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isLetter(c) && c != ' ') {
                return "❌ Only letters & spaces";
            }
        }

        // Check if has letters
        boolean hasLetter = false;
        for (int i = 0; i < name.length(); i++) {
            if (Character.isLetter(name.charAt(i))) {
                hasLetter = true;
                break;
            }
        }

        if (!hasLetter) {
            return "❌ Needs letters";
        }

        // Check if name is too long (optional)
        if (name.length() > 50) {
            return "❌ Too long (max 50)";
        }

        // Valid name
        if (name.length() >= 2 && name.length() <= 20) {
            return "✅ Valid name";
        } else {
            return "✅ Good name";
        }
    }

    // Password strength validation
    public static boolean isStrongPassword(String password) {
        if (isEmpty(password) || password.length() < 6) {
            return false;
        }

        boolean hasUppercase = !password.equals(password.toLowerCase());
        boolean hasLowercase = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

        int criteriaCount = 0;
        if (hasUppercase) criteriaCount++;
        if (hasLowercase) criteriaCount++;
        if (hasDigit) criteriaCount++;
        if (hasSpecial) criteriaCount++;

        return criteriaCount >= 3;
    }

    // Get password strength message
    public static String getPasswordStrengthMessage(String password) {
        if (isEmpty(password)) return "Enter password";
        if (password.length() < 6) return "❌ Minimum 6 characters";

        boolean hasUppercase = !password.equals(password.toLowerCase());
        boolean hasLowercase = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

        int criteriaCount = 0;
        if (hasUppercase) criteriaCount++;
        if (hasLowercase) criteriaCount++;
        if (hasDigit) criteriaCount++;
        if (hasSpecial) criteriaCount++;

        if (criteriaCount >= 4) return "✅ Strong password";
        if (criteriaCount >= 3) return "⚠️ Moderate password";
        if (criteriaCount >= 2) return "⚠️ Weak password";
        return "❌ Very weak password";
    }
}