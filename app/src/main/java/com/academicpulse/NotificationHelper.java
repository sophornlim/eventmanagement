package com.academicpulse;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.academicpulse.database.AppDatabase;
import com.academicpulse.database.entity.Event;
import com.academicpulse.database.entity.Notification;
import com.academicpulse.database.relational.RegistrationWithEvent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class NotificationHelper {

    public static final String ACTION_EVENT_REMINDER = "com.academicpulse.ACTION_EVENT_REMINDER";
    public static final String CHANNEL_ID = "event_reminder_channel";

    public static void scheduleEventReminder(Context context, Event event) {
        if (event == null || event.getDate() == null || event.getTime() == null) {
            return;
        }

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
            intent.setAction(ACTION_EVENT_REMINDER);
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

    /**
     * Save a notification for a specific student. Used when the student is not logged in yet
     * (e.g. right after they register an account, before a session exists).
     */
    public static void saveNotificationForStudent(Context context, String studentId, String title, String message, String type) {
        if (studentId == null) {
            return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            String sentAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            Notification notification = new Notification(0, studentId, title, message, sentAt, false, type);
            db.notificationDao().insertNotification(notification);
        });
    }

    /**
     * Immediately show a system (heads-up) notification. Degrades gracefully if the
     * POST_NOTIFICATIONS permission has not been granted on Android 13+.
     */
    public static void showSystemNotification(Context context, int notifId, String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Event Reminders", NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(channel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(notifId, builder.build());
    }

    public static void rescheduleAllReminders(Context context) {
        SessionManager sessionManager = new SessionManager(context);
        String userId = sessionManager.getUserId();
        if (userId == null) {
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            List<RegistrationWithEvent> registrations = db.registrationDao().getRegistrationsWithEventsForStudent(userId);
            for (RegistrationWithEvent reg : registrations) {
                if (reg.event != null) {
                    scheduleEventReminder(context, reg.event);
                }
            }
        });
    }
}
