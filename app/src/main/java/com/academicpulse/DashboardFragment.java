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
import com.academicpulse.database.entity.Student;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class DashboardFragment extends Fragment {

    private AdminEventAdapter adapter;
    private TextView tvTotalParticipants;
    private TextView tvTotalEvents;
    private TextView tvNewPercentage;
    private TextView tvMaleCount;
    private TextView tvFemaleCount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        tvTotalParticipants = view.findViewById(R.id.tv_total_participants);
        tvTotalEvents = view.findViewById(R.id.tv_total_events);
        tvNewPercentage = view.findViewById(R.id.tv_new_percentage);
        tvMaleCount = view.findViewById(R.id.tv_male_count);
        tvFemaleCount = view.findViewById(R.id.tv_female_count);

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
            List<Student> students = db.studentDao().getAllStudents();
            
            int maleCount = 0;
            int femaleCount = 0;
            for (Student s : students) {
                if ("Male".equalsIgnoreCase(s.getGender())) {
                    maleCount++;
                } else if ("Female".equalsIgnoreCase(s.getGender())) {
                    femaleCount++;
                }
            }

            int growthPercentage = calculateGrowth(registrations);

            if (getActivity() != null) {
                int finalMaleCount = maleCount;
                int finalFemaleCount = femaleCount;
                getActivity().runOnUiThread(() -> {
                    adapter.updateEvents(events);
                    if (tvTotalEvents != null) tvTotalEvents.setText(String.valueOf(events.size()));
                    if (tvTotalParticipants != null) tvTotalParticipants.setText(String.valueOf(registrations.size()));
                    if (tvMaleCount != null) tvMaleCount.setText(String.valueOf(finalMaleCount));
                    if (tvFemaleCount != null) tvFemaleCount.setText(String.valueOf(finalFemaleCount));
                    if (tvNewPercentage != null) {
                        String text = (growthPercentage >= 0 ? "+" : "") + growthPercentage + "%";
                        tvNewPercentage.setText(text);
                    }
                });
            }
        });
    }

    private int calculateGrowth(List<Registration> registrations) {
        if (registrations.isEmpty()) {
            return 0;
        }

        long now = System.currentTimeMillis();
        long thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000);
        long sixtyDaysAgo = now - (60L * 24 * 60 * 60 * 1000);

        int currentMonthCount = 0;
        int lastMonthCount = 0;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        for (Registration reg : registrations) {
            try {
                Date date = sdf.parse(reg.getRegisteredAt());
                if (date != null) {
                    long time = date.getTime();
                    if (time > thirtyDaysAgo) {
                        currentMonthCount++;
                    } else if (time > sixtyDaysAgo) {
                        lastMonthCount++;
                    }
                }
            } catch (Exception ignored) {}
        }

        if (lastMonthCount == 0) {
            return currentMonthCount > 0 ? 100 : 0;
        }

        return ((currentMonthCount - lastMonthCount) * 100) / lastMonthCount;
    }
}
