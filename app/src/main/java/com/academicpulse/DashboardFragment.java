package com.academicpulse;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.academicpulse.database.AppDatabase;
import com.academicpulse.database.entity.Event;
import com.academicpulse.database.entity.Registration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class DashboardFragment extends Fragment {

    private AdminEventAdapter adapter;
    private TextView tvTotalParticipants;
    private TextView tvTotalEvents;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        tvTotalParticipants = view.findViewById(R.id.tv_total_participants);
        tvTotalEvents = view.findViewById(R.id.tv_total_events);

        view.findViewById(R.id.iv_notifications).setOnClickListener(v -> startActivity(new Intent(requireContext(), NotificationsActivity.class)));

        RecyclerView recyclerView = view.findViewById(R.id.rv_dashboard_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new AdminEventAdapter(new ArrayList<>(), 
            event -> {
                // On Edit
                Intent intent = new Intent(requireContext(), CreateEventActivity.class);
                intent.putExtra("EVENT_ID", event.getId());
                intent.putExtra("EDIT_MODE", true);
                startActivity(intent);
            }, 
            event -> {
                // On Delete
                AppDatabase db = AppDatabase.getInstance(requireContext());
                Executors.newSingleThreadExecutor().execute(() -> {
                    db.eventDao().deleteEvent(event);
                    loadData();
                });
            },
            event -> {
                // On Item Click (Detail)
                Intent intent = new Intent(requireContext(), EventDetailActivity.class);
                intent.putExtra("EVENT_ID", event.getId());
                startActivity(intent);
            }
        );
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Event> events = db.eventDao().getAllEvents();
            List<Registration> registrations = db.registrationDao().getAllRegistrations();
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    adapter.updateEvents(events);
                    if (tvTotalEvents != null) tvTotalEvents.setText(String.valueOf(events.size()));
                    if (tvTotalParticipants != null) tvTotalParticipants.setText(String.valueOf(registrations.size()));
                });
            }
        });
    }
}
