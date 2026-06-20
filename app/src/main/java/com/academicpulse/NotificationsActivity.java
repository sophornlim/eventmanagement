package com.academicpulse;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.academicpulse.database.AppDatabase;
import com.academicpulse.database.entity.Notification;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class NotificationsActivity extends AppCompatActivity {
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        RecyclerView recyclerView = findViewById(R.id.rv_notifications);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new NotificationAdapter(new ArrayList<>());
            recyclerView.setAdapter(adapter);
        }

        loadNotifications();
    }

    private void loadNotifications() {
        SessionManager sessionManager = new SessionManager(this);
        String userId = sessionManager.getUserId();

        if (userId != null) {
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(this);
                List<Notification> notifications = db.notificationDao().getNotificationsForStudent(userId);
                
                runOnUiThread(() -> {
                    if (adapter != null) {
                        adapter.updateNotifications(notifications);
                    }
                });
            });
        }
    }
}
