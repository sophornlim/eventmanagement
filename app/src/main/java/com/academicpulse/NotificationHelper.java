package com.academicpulse;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.academicpulse.database.AppDatabase;
import com.academicpulse.database.entity.Event;
import com.academicpulse.database.entity.Notification;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

public class NotificationHelper {

    public static void scheduleEventReminder(Context context, Event event) {
        if (event == null || event.getDate() == null || event.getTime() == null) return;

        try {
            // Parse date and time. Format: "yyyy-MM-dd" and "hh:mm a"
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
            Date eventDate = sdf.parse(event.getDate() + " " + event.getTime());
            if (eventDate == null) {
                return;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(eventDate);
            calendar.add(Calendar.MINUTE, -10); // 10 minutes before

            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                return; // Already passed
            }

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.putExtra("title", "រំលឹកព្រឹត្តិការណ៍: " + event.getTitle());
            intent.putExtra("message", "ព្រឹត្តិការណ៍នឹងចាប់ផ្តើមក្នុងរយៈពេល ១០ នាទីទៀត");
            intent.putExtra("eventId", event.getId());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, event.getId(), intent, 
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager != null && alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                } else if (alarmManager != null) {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            } else if (alarmManager != null) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveNotificationToDb(Context context, String title, String message, String type) {
        SessionManager sessionManager = new SessionManager(context);
        String userId = sessionManager.getUserId();

        if (userId != null) {
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(context);
                String sentAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                Notification notification = new Notification(0, userId, title, message, sentAt, false, type);
                db.notificationDao().insertNotification(notification);
            });
        }
    }
}
