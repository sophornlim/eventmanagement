package com.academicpulse;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.academicpulse.database.entity.Event;

import java.util.List;

public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.ViewHolder> {

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    private List<Event> events;
    private final OnEventClickListener onEdit;
    private final OnEventClickListener onDelete;

    public AdminEventAdapter(List<Event> events, OnEventClickListener onEdit, OnEventClickListener onDelete) {
        this.events = events;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView title;
        public final TextView date;
        public final TextView registrations;
        public final ImageButton btnEdit;
        public final ImageButton btnDelete;
        public final ImageView image;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.tv_admin_event_title);
            date = view.findViewById(R.id.tv_admin_event_date);
            registrations = view.findViewById(R.id.tv_admin_event_registrations);
            btnEdit = view.findViewById(R.id.btn_edit);
            btnDelete = view.findViewById(R.id.btn_delete);
            image = view.findViewById(R.id.iv_admin_event_image);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);
        holder.title.setText(event.getTitle());
        holder.date.setText(event.getDate());
        holder.registrations.setText(String.format("%d / 500", event.getCapacity()));

        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            try {
                holder.image.setImageURI(Uri.parse(event.getImageUrl()));
            } catch (Exception e) {
                holder.image.setImageResource(R.drawable.ic_launcher_background);
                e.printStackTrace();
            }
        } else {
            holder.image.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.btnEdit.setOnClickListener(v -> onEdit.onEventClick(event));
        holder.btnDelete.setOnClickListener(v -> onDelete.onEventClick(event));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void updateEvents(List<Event> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }
}
