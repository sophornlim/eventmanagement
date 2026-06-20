package com.academicpulse;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.academicpulse.database.AppDatabase;
import com.academicpulse.database.entity.Department;
import com.academicpulse.database.entity.Event;
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
                db.departmentDao().insertDepartment(new Department(101, "Computer Science & Tech", "CS"));
                db.departmentDao().insertDepartment(new Department(102, "Engineering", "ENG"));
                db.departmentDao().insertDepartment(new Department(103, "Arts & Humanities", "ART"));
            }

            // 3. Pre-populate Sample Events
            List<Event> events = db.eventDao().getAllEvents();
            if (events.isEmpty()) {
                Event e1 = new Event("សន្និសីទបច្ចេកវិទ្យា ២០២៤", "សិក្សាអំពី AI និង Machine Learning", "", "ការសិក្សា", "សាលប្រជុំ A", "2024-11-20", "09:00 AM", 101, 100, "Active");
                e1.setSpeakerName("បណ្ឌិត សុខ សាន");
                e1.setSpeakerRole("អ្នកជំនាញ AI");
                e1.setOrganizer("ដេប៉ាតឺម៉ង់វិទ្យាសាស្ត្រកុំព្យូទ័រ");
                db.eventDao().insertEvent(e1);

                Event e2 = new Event("ការប្រកួតបាល់ទាត់និស្សិត", "ព្រឹត្តិការណ៍កីឡាប្រចាំឆ្នាំ", "", "កីឡា", "តារាងបាល់ទាត់សាកលវិទ្យាល័យ", "2024-11-25", "02:00 PM", 102, 50, "Active");
                e2.setSpeakerName("លោក ហុង ដារ៉ា");
                e2.setSpeakerRole("គ្រូបង្វឹកកីឡា");
                e2.setOrganizer("សមាគមនិស្សិត");
                db.eventDao().insertEvent(e2);
            }
        });

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }, 2000);
    }
}
