package com.example.wrappedspotify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EditAccountActivity extends AppCompatActivity {

    private EditText emailField, newPasswordField;
    private Button updateEmailButton, updatePasswordButton, goHomeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);

        emailField = findViewById(R.id.emailField);
        newPasswordField = findViewById(R.id.newPasswordField);
        updateEmailButton = findViewById(R.id.updateEmailButton);
        updatePasswordButton = findViewById(R.id.updatePasswordButton);
        goHomeButton = findViewById(R.id.goHomeButton);

        updateEmailButton.setOnClickListener(v -> updateEmail());
        updatePasswordButton.setOnClickListener(v -> updatePassword());
        goHomeButton.setOnClickListener(v -> goHome());
    }

    private void goHome() {
        Intent intent = new Intent(this, HomePageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private void updateEmail() {
        String newEmail = emailField.getText().toString().trim();
        if (!newEmail.isEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                user.updateEmail(newEmail)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(EditAccountActivity.this, "Email updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(EditAccountActivity.this, "Failed to update email", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        } else {
            Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePassword() {
        String newPassword = newPasswordField.getText().toString().trim();
        if (!newPassword.isEmpty() && newPassword.length() >= 6) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                user.updatePassword(newPassword)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(EditAccountActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(EditAccountActivity.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        } else {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
        }
    }
    }

