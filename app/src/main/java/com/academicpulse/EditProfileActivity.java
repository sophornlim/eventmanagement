package com.academicpulse;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
    private RadioGroup rgGender;

    private Uri selectedImageUri = null;

    private final ActivityResultLauncher<String[]> pickImage = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    try {
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
        rgGender = findViewById(R.id.rg_edit_gender);

        loadUserData();

        findViewById(R.id.profile_image_edit_container).setOnClickListener(v -> pickImage.launch(new String[]{"image/*"}));

        findViewById(R.id.btn_update).setOnClickListener(v -> updateProfile());
    }

    private void loadUserData() {
        String userId = new SessionManager(this).getUserId();
        if (userId == null) {
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            Student user = AppDatabase.getInstance(this).studentDao().getStudentById(userId);
            runOnUiThread(() -> {
                if (user != null) {
                    etUsername.setText(user.getId());
                    etName.setText(user.getName());
                    etEmail.setText(user.getEmail());
                    etPassword.setText(user.getPassword());

                    if ("Male".equalsIgnoreCase(user.getGender())) {
                        rgGender.check(R.id.rb_edit_male);
                    } else if ("Female".equalsIgnoreCase(user.getGender())) {
                        rgGender.check(R.id.rb_edit_female);
                    }

                    if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                        try {
                            ivProfile.setImageURI(Uri.parse(user.getAvatarUrl()));
                        } catch (Exception e) {
                            ivProfile.setImageResource(R.drawable.ic_default_avatar);
                            e.printStackTrace();
                        }
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

        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        String gender = "Other";
        if (selectedGenderId == R.id.rb_edit_male) {
            gender = "Male";
        } else if (selectedGenderId == R.id.rb_edit_female) {
            gender = "Female";
        }

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "សូមបំពេញព័ត៌មានឱ្យបានគ្រប់គ្រាន់", Toast.LENGTH_SHORT).show();
            return;
        }

        String finalGender = gender;
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            Student user = db.studentDao().getStudentById(userId);
            if (user != null) {
                user.setName(name);
                user.setEmail(email);
                user.setPassword(password);
                user.setGender(finalGender);
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
