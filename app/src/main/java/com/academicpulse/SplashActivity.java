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
            List<Student> users = db.studentDao().getAllStudents();
            if (users.isEmpty()) {
                db.studentDao().insertStudent(
                    new Student("admin", "Administrator", "admin@university.edu.kh", "", "admin", "admin123")
                );
            }

            List<Department> depts = db.departmentDao().getAllDepartments();
            if (depts.isEmpty()) {
                db.departmentDao().insertDepartment(new Department(101, "Computer Science & Tech", "CS"));
                db.departmentDao().insertDepartment(new Department(102, "Engineering", "ENG"));
                db.departmentDao().insertDepartment(new Department(103, "Arts & Humanities", "ART"));
            }
        });

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }, 2000);
    }
}
