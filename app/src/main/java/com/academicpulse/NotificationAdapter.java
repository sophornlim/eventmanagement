package com.academicpulse;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.academicpulse.database.entity.Notification;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> notifications;

    public NotificationAdapter(List<Notification> notifications) {
        this.notifications = notifications;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView title;
        public final TextView message;
        public final TextView time;
        public final ImageView icon;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.tv_notif_title);
            message = view.findViewById(R.id.tv_notif_desc);
            time = view.findViewById(R.id.tv_notif_time);
            icon = view.findViewById(R.id.iv_notif_icon);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        
        // Ensure values are not null
        String title = notification.getTitle() != null ? notification.getTitle() : "គ្មានចំណងជើង";
        String message = notification.getMessage() != null ? notification.getMessage() : "គ្មានព័ត៌មាន";
        String time = notification.getSentAt() != null ? notification.getSentAt() : "--:--";

        holder.title.setText(title);
        holder.message.setText(message);
        holder.time.setText(time);

        // Set different icons based on notification type
        if (notification.getType() != null) {
            switch (notification.getType()) {
                case "Reminder" -> holder.icon.setImageResource(android.R.drawable.ic_popup_reminder);
                case "System" -> holder.icon.setImageResource(android.R.drawable.ic_menu_info_details);
                case "Registration" -> holder.icon.setImageResource(android.R.drawable.ic_menu_agenda);
                default -> holder.icon.setImageResource(android.R.drawable.ic_dialog_info);
            }
        }
    }

    @Override
    public int getItemCount() {
        return notifications != null ? notifications.size() : 0;
    }

    public void updateNotifications(List<Notification> newNotifications) {
        this.notifications = newNotifications;
        notifyDataSetChanged();
    }
}
