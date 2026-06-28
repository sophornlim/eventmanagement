package com.academicpulse;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.academicpulse.database.AppDatabase;
import com.academicpulse.database.entity.Student;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText etName = findViewById(R.id.et_name);
        EditText etStudentId = findViewById(R.id.et_student_id);
        EditText etEmail = findViewById(R.id.et_email);
        EditText etPassword = findViewById(R.id.et_password);
        RadioGroup rgGender = findViewById(R.id.rg_gender);
        
        findViewById(R.id.tv_login).setOnClickListener(v -> finish());
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        Button btnRegister = findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String studentId = etStudentId.getText().toString();
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();

            int selectedGenderId = rgGender.getCheckedRadioButtonId();
            String gender = "Other";
            if (selectedGenderId == R.id.rb_male) {
                gender = "Male";
            } else if (selectedGenderId == R.id.rb_female) {
                gender = "Female";
            }

            if (name.isEmpty() || studentId.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            String finalGender = gender;
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(this);
                Student existing = db.studentDao().getStudentById(studentId);
                if (existing != null) {
                    runOnUiThread(() -> Toast.makeText(this, R.string.student_id_exists, Toast.LENGTH_SHORT).show());
                    return;
                }

                Student student = new Student(studentId, name, email, "", "student", password);
                student.setGender(finalGender);
                db.studentDao().insertStudent(student);

                // Action 1: notify the student that their account was created.
                String welcomeTitle = getString(R.string.notif_welcome_title);
                String welcomeMessage = getString(R.string.notif_welcome_message, name);
                NotificationHelper.saveNotificationForStudent(this, studentId, welcomeTitle, welcomeMessage, "System");
                NotificationHelper.showSystemNotification(this, studentId.hashCode(), welcomeTitle, welcomeMessage);

                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        });
    }
}
