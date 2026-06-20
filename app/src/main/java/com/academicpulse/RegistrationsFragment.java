package com.academicpulse;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.academicpulse.database.AppDatabase;
import com.academicpulse.database.entity.Event;
import com.academicpulse.database.relational.RegistrationWithEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class RegistrationsFragment extends Fragment {

    private EventAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registrations, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            });
        }

        RecyclerView recyclerView = view.findViewById(R.id.rv_registrations);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        loadRegistrations();

        return view;
    }

    private void loadRegistrations() {
        String userId = new SessionManager(requireContext()).getUserId();
        if (userId == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            List<RegistrationWithEvent> registrations = db.registrationDao().getRegistrationsWithEventsForStudent(userId);
            
            List<Event> events = registrations.stream()
                    .map(r -> r.event)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> adapter.updateEvents(events));
            }
        });
    }
}
