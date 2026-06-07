package com.academicpulse;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.academicpulse.database.AppDatabase;
import com.academicpulse.database.entity.Event;
import com.academicpulse.database.entity.Registration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> events;

    public EventAdapter(List<Event> events) {
        this.events = events;
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        public final TextView title;
        public final TextView date;
        public final TextView location;
        public final ImageView image;
        public final Button btnRegister;

        public EventViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.tv_event_title);
            date = view.findViewById(R.id.tv_event_date);
            location = view.findViewById(R.id.tv_event_location);
            image = view.findViewById(R.id.iv_event_image);
            btnRegister = view.findViewById(R.id.btn_register);
        }
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.title.setText(event.getTitle());
        holder.date.setText(String.format("%s • %s", event.getDate(), event.getTime()));
        holder.location.setText(event.getLocation());

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

        holder.itemView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, EventDetailActivity.class);
            intent.putExtra("EVENT_TITLE", event.getTitle());
            intent.putExtra("EVENT_DESC", event.getDescription());
            intent.putExtra("EVENT_DATE", event.getDate());
            intent.putExtra("EVENT_TIME", event.getTime());
            intent.putExtra("EVENT_LOCATION", event.getLocation());
            intent.putExtra("EVENT_IMAGE", event.getImageUrl());
            context.startActivity(intent);
        });

        holder.btnRegister.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            SessionManager sessionManager = new SessionManager(context);
            String userId = sessionManager.getUserId();

            if (userId == null) {
                Toast.makeText(context, "សូមចូលប្រើប្រាស់ជាមុនសិន", Toast.LENGTH_SHORT).show();
                return;
            }

            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    AppDatabase db = AppDatabase.getInstance(context);
                    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                    Registration registration = new Registration(userId, event.getId(), timestamp, "Registered");
                    
                    db.registrationDao().insertRegistration(registration);
                    
                    if (context instanceof android.app.Activity) {
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            Toast.makeText(context, "ចុះឈ្មោះជោគជ័យ!", Toast.LENGTH_SHORT).show();
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
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
