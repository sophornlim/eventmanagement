package com.academicpulse;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.academicpulse.database.AppDatabase;
import com.academicpulse.database.entity.Department;
import com.academicpulse.database.entity.Student;
import java.util.List;
import java.util.concurrent.Executors;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        AppDatabase db = AppDatabase.getInstance(this);
        Executors.newSingleThreadExecutor().execute(() -> {
            // 1. Pre-populate Students
            List<Student> users = db.studentDao().getAllStudents();
            if (users.isEmpty()) {
                db.studentDao().insertStudent(
                    new Student("admin", "Administrator", "admin@university.edu.kh", "", "admin", "admin123")
                );
            }

            // 2. Pre-populate Departments
            List<Department> depts = db.departmentDao().getAllDepartments();
            if (depts.isEmpty()) {
                db.departmentDao().insertDepartment(new Department(101, "ការសិក្សា", "EDU"));
                db.departmentDao().insertDepartment(new Department(102, "សង្គម", "SC"));
                db.departmentDao().insertDepartment(new Department(103, "កីឡា", "SP"));
                db.departmentDao().insertDepartment(new Department(104, "សិក្ខាសាលា", "WS"));
            } else if (db.departmentDao().getDepartmentById(104) == null) {
                // Backfill the Workshop department for installs seeded before it was added
                db.departmentDao().insertDepartment(new Department(104, "សិក្ខាសាលា", "WS"));
            }
        });

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }, 3000);
    }
}
