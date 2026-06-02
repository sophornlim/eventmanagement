package com.academicpulse;

import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventAdapter adapter;

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

        view.findViewById(R.id.iv_notifications).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), NotificationsActivity.class));
        });

        view.findViewById(R.id.fab_add).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), CreateEventActivity.class));
        });

        recyclerView = view.findViewById(R.id.rv_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EventAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadEvents();
    }

    private void loadEvents() {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Event> events = db.eventDao().getAllEvents();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    adapter.updateEvents(events);
                });
            }
        });
    }
}
