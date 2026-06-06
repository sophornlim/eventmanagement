package com.academicpulse;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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
        
        findViewById(R.id.tv_login).setOnClickListener(v -> finish());
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        Button btnRegister = findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String studentId = etStudentId.getText().toString();
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();

            if (name.isEmpty() || studentId.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "សូមបំពេញព័ត៌មានឱ្យបានគ្រប់គ្រាន់។", Toast.LENGTH_SHORT).show();
                return;
            }

            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(this);
                Student existing = db.studentDao().getStudentById(studentId);
                if (existing != null) {
                    runOnUiThread(() -> Toast.makeText(this, "អត្តលេខនិស្សិតធ្លាប់មានក្នុងប្រព័ន្ធរួចរាល់ហើយ!", Toast.LENGTH_SHORT).show());
                    return;
                }

                Student student = new Student(studentId, name, email, "", "student", password);
                db.studentDao().insertStudent(student);
                
                runOnUiThread(() -> {
                    Toast.makeText(this, "ការចុះឈ្មោះទទួួលបានជោគជ័យ!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        });
    }
}
