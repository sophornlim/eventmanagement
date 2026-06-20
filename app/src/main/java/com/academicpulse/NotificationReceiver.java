package com.academicpulse;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "event_reminder_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        int eventId = intent.getIntExtra("eventId", 0);

        // 1. Save to database so it shows up in the notification list
        NotificationHelper.saveNotificationToDb(context, title, message, "Reminder");

        // 2. Show system notification alert
        showSystemNotification(context, title, message, eventId);
    }

    private void showSystemNotification(Context context, String title, String message, int eventId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Event Reminders", NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(channel);

        Intent detailIntent = new Intent(context, EventDetailActivity.class);
        detailIntent.putExtra("EVENT_ID", eventId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, eventId, detailIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(eventId, builder.build());
    }
}
