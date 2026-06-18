package com.academicpulse;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.academicpulse.database.AppDatabase;
import com.academicpulse.database.entity.Student;
import com.academicpulse.database.entity.Registration;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ParticipantsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ParticipantsAdapter adapter;
    private TextView tvTotalParticipants;
    private TextView tvRegisteredCount;
    private TextView tvAttendedCount;
    private LinearLayout layoutRecentActivities;
    
    private List<Student> allParticipants = new ArrayList<>();
    private List<Registration> allRegistrations = new ArrayList<>();
    private String currentFilter = "ទាំងអស់";
    private String currentQuery = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_participants, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            });
        }

        tvTotalParticipants = view.findViewById(R.id.tv_total_participants);
        tvRegisteredCount = view.findViewById(R.id.tv_registered_count);
        tvAttendedCount = view.findViewById(R.id.tv_attended_count);
        layoutRecentActivities = view.findViewById(R.id.layout_recent_activities);

        recyclerView = view.findViewById(R.id.rv_participants);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new ParticipantsAdapter(new ArrayList<>());
        adapter.setOnDeleteClickListener(student -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            Executors.newSingleThreadExecutor().execute(() -> {
                db.studentDao().deleteStudent(student);
                loadData();
            });
        });
        recyclerView.setAdapter(adapter);

        SearchView searchView = view.findViewById(R.id.search_participants);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentQuery = query;
                applyFilters();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                applyFilters();
                return true;
            }
        });

        ChipGroup chipGroup = view.findViewById(R.id.chip_group_participants);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentFilter = "ទាំងអស់";
            } else {
                int id = checkedIds.get(0);
                if (id == R.id.chip_all) {
                    currentFilter = "ទាំងអស់";
                } else if (id == R.id.chip_registered) {
                    currentFilter = "Registered";
                } else if (id == R.id.chip_attended) {
                    currentFilter = "Attended";
                }
            }
            applyFilters();
        });

        View.OnClickListener onAddParticipantClick = v -> {
            startActivity(new Intent(requireContext(), RegisterActivity.class));
        };

        view.findViewById(R.id.card_add_participant).setOnClickListener(onAddParticipantClick);
        view.findViewById(R.id.fab_add_participant).setOnClickListener(onAddParticipantClick);
        
        loadData();
        
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
            allParticipants = db.studentDao().getAllParticipants();
            allRegistrations = db.registrationDao().getAllRegistrations();
            
            // Calculate attended as a subset of registrations
            int registered = allRegistrations.size();
            int attended = 0;
            for (Registration r : allRegistrations) {
                if ("attended".equalsIgnoreCase(r.getStatus())) {
                    attended++;
                }
            }

            final int finalTotal = allParticipants.size();
            final int finalRegistered = registered;
            final int finalAttended = attended;

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tvTotalParticipants.setText(finalTotal + " នាក់");
                    tvRegisteredCount.setText(String.valueOf(finalRegistered));
                    tvAttendedCount.setText(String.valueOf(finalAttended));
                    updateRecentActivities(allRegistrations, allParticipants);
                    applyFilters();
                });
            }
        });
    }

    private void applyFilters() {
        List<Student> filtered = allParticipants.stream()
                .filter(s -> currentQuery.isEmpty() || 
                        s.getName().toLowerCase().contains(currentQuery.toLowerCase()) ||
                        s.getId().toLowerCase().contains(currentQuery.toLowerCase()))
                .filter(s -> {
                    if ("ទាំងអស់".equals(currentFilter)) return true;
                    // Check if student has a registration with matching status
                    return allRegistrations.stream().anyMatch(r -> 
                            r.getStudentId().equals(s.getId()) && 
                            r.getStatus().equalsIgnoreCase(currentFilter));
                })
                .collect(Collectors.toList());
        adapter.updateParticipants(filtered);
    }

    private void updateRecentActivities(List<Registration> registrations, List<Student> students) {
        if (layoutRecentActivities == null) return;
        layoutRecentActivities.removeAllViews();

        // Show last 5 activities
        int count = 0;
        for (int i = registrations.size() - 1; i >= 0 && count < 5; i--) {
            Registration reg = registrations.get(i);
            Student student = null;
            for (Student s : students) {
                if (s.getId().equals(reg.getStudentId())) {
                    student = s;
                    break;
                }
            }

            if (student != null) {
                TextView tv = new TextView(getContext());
                String action = "attended".equalsIgnoreCase(reg.getStatus()) ? "បានចូលរួម" : "បានចុះឈ្មោះ";
                int color = "attended".equalsIgnoreCase(reg.getStatus()) ? 
                        getResources().getColor(R.color.primary_blue, null) : 
                        getResources().getColor(R.color.success_green, null);
                
                tv.setText("• " + student.getName() + " " + action);
                tv.setTextColor(color);
                tv.setTextSize(12);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 4, 0, 4);
                tv.setLayoutParams(params);
                layoutRecentActivities.addView(tv);
                count++;
            }
        }

        if (count == 0) {
            TextView tv = new TextView(getContext());
            tv.setText("មិនទាន់មានសកម្មភាពនៅឡើយ");
            tv.setTextColor(getResources().getColor(R.color.text_grey, null));
            tv.setTextSize(12);
            layoutRecentActivities.addView(tv);
        }
    }
}
