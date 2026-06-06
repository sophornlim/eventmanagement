package com.academicpulse;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.academicpulse.database.AppDatabase;
import com.academicpulse.database.entity.Student;

import java.util.concurrent.Executors;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etName;
    private EditText etEmail;
    private EditText etPassword;
    private ImageView ivProfile;

    private Uri selectedImageUri = null;

    private final ActivityResultLauncher<String> pickImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivProfile.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etUsername = findViewById(R.id.et_username);
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        ivProfile = findViewById(R.id.iv_edit_profile);

        loadUserData();

        findViewById(R.id.profile_image_edit_container).setOnClickListener(v -> pickImage.launch("image/*"));

        findViewById(R.id.btn_update).setOnClickListener(v -> updateProfile());
    }

    private void loadUserData() {
        String userId = new SessionManager(this).getUserId();
        if (userId == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            Student user = AppDatabase.getInstance(this).studentDao().getStudentById(userId);
            runOnUiThread(() -> {
                if (user != null) {
                    etUsername.setText(user.getId());
                    etName.setText(user.getName());
                    etEmail.setText(user.getEmail());
                    etPassword.setText(user.getPassword());
                    if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                        ivProfile.setImageURI(Uri.parse(user.getAvatarUrl()));
                    } else {
                        ivProfile.setImageResource(R.drawable.ic_default_avatar);
                    }
                }
            });
        });
    }

    private void updateProfile() {
        String userId = new SessionManager(this).getUserId();
        if (userId == null) return;

        String name = etName.getText().toString();
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String avatarUrl = selectedImageUri != null ? selectedImageUri.toString() : "";

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "សូមបំពេញព័ត៌មានឱ្យបានគ្រប់គ្រាន់", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            Student user = db.studentDao().getStudentById(userId);
            if (user != null) {
                user.setName(name);
                user.setEmail(email);
                user.setPassword(password);
                if (!avatarUrl.isEmpty()) {
                    user.setAvatarUrl(avatarUrl);
                }
                db.studentDao().updateStudent(user);
                runOnUiThread(() -> {
                    Toast.makeText(this, "ព័ត៌មានត្រូវបានកែប្រែដោយជោគជ័យ", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }
}
