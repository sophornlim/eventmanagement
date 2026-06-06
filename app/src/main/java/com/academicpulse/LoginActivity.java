package com.academicpulse;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.academicpulse.database.AppDatabase;
import com.academicpulse.database.entity.Student;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText etUsername = findViewById(R.id.et_username);
        EditText etPassword = findViewById(R.id.et_password);
        Button btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(LoginActivity.this);
                Student user = db.studentDao().login(username, password);

                runOnUiThread(() -> {
                    if (user != null) {
                        new SessionManager(LoginActivity.this).login(user.getId());
                        Toast.makeText(LoginActivity.this, "Login Successful! Welcome " + user.getName(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "អត្តលេខនិស្សិត ឬ អ៊ីមែល / លេខសម្ងាត់មិនត្រឹមត្រូវ!", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        findViewById(R.id.tv_register).setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
}
