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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.academicpulse.database.entity.Event;

import java.util.List;

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
            holder.image.setImageURI(Uri.parse(event.getImageUrl()));
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
