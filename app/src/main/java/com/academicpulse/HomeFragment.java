package com.academicpulse;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.academicpulse.database.AppDatabase;
import com.academicpulse.database.entity.Department;
import com.academicpulse.database.entity.Event;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment {

    private static final String CATEGORY_ALL = "ទាំងអស់";

    private EventAdapter adapter;
    private ChipGroup chipGroup;
    private List<Event> allEvents = new ArrayList<>();
    private String currentCategory = CATEGORY_ALL;
    private String currentQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            });
        }

        view.findViewById(R.id.iv_notifications).setOnClickListener(v -> startActivity(new Intent(requireContext(), NotificationsActivity.class)));

        view.findViewById(R.id.fab_add).setOnClickListener(v -> startActivity(new Intent(requireContext(), CreateEventActivity.class)));

        RecyclerView recyclerView = view.findViewById(R.id.rv_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EventAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        SearchView searchView = view.findViewById(R.id.search_view);
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

        chipGroup = view.findViewById(R.id.chip_group);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentCategory = CATEGORY_ALL;
            } else {
                Chip chip = group.findViewById(checkedIds.get(0));
                currentCategory = (chip != null && chip.getTag() != null)
                        ? chip.getTag().toString() : CATEGORY_ALL;
            }
            applyFilters();
        });

        loadCategories();

        return view;
    }

    private void loadCategories() {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Department> departments = db.departmentDao().getAllDepartments();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> buildCategoryChips(departments));
            }
        });
    }

    private void buildCategoryChips(List<Department> departments) {
        if (chipGroup == null) return;
        chipGroup.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();

        // "All" chip, selected by default
        Chip allChip = (Chip) inflater.inflate(R.layout.item_chip_choice, chipGroup, false);
        allChip.setText(R.string.all);
        allChip.setTag(CATEGORY_ALL);
        allChip.setChecked(true);
        chipGroup.addView(allChip);

        // One chip per department loaded from the departments table
        for (Department department : departments) {
            Chip chip = (Chip) inflater.inflate(R.layout.item_chip_choice, chipGroup, false);
            chip.setText(department.getName());
            chip.setTag(department.getName());
            chipGroup.addView(chip);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadEvents();
    }

    private void loadEvents() {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        Executors.newSingleThreadExecutor().execute(() -> {
            allEvents = db.eventDao().getAllEvents();
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::applyFilters);
            }
        });
    }

    private void applyFilters() {
        List<Event> filtered = allEvents.stream()
                .filter(e -> CATEGORY_ALL.equals(currentCategory) || e.getCategory().equalsIgnoreCase(currentCategory))
                .filter(e -> currentQuery.isEmpty() || e.getTitle().toLowerCase().contains(currentQuery.toLowerCase()))
                .collect(Collectors.toList());
        adapter.updateEvents(filtered);
    }
}
