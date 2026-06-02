package com.academicpulse;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_dashboard) {
                loadFragment(new DashboardFragment());
                return true;
            } else if (itemId == R.id.navigation_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.navigation_participants) {
                loadFragment(new ParticipantsFragment());
                return true;
            } else if (itemId == R.id.navigation_account) {
                loadFragment(new AccountFragment());
                return true;
            }
            return false;
        });

        // Default fragment
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.navigation_home);
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit();
    }
}
