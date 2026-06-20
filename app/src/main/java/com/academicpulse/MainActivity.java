package com.academicpulse;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.academicpulse.database.AppDatabase;
import com.academicpulse.database.entity.Student;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NotificationHelper.rescheduleAllReminders(this);

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment fragment = getFragmentById(itemId);

            if (fragment != null) {
                loadFragment(fragment, itemId == R.id.navigation_dashboard);
                return true;
            }
            return false;
        });

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof DashboardFragment) {
                bottomNav.getMenu().findItem(R.id.navigation_dashboard).setChecked(true);
            } else if (currentFragment instanceof HomeFragment) {
                bottomNav.getMenu().findItem(R.id.navigation_home).setChecked(true);
            } else if (currentFragment instanceof ParticipantsFragment) {
                bottomNav.getMenu().findItem(R.id.navigation_participants).setChecked(true);
            } else if (currentFragment instanceof AccountFragment) {
                bottomNav.getMenu().findItem(R.id.navigation_account).setChecked(true);
            }
        });

        // Default fragment based on role
        if (savedInstanceState == null) {
            String userId = new SessionManager(this).getUserId();
            if (userId != null) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    Student user = AppDatabase.getInstance(this).studentDao().getStudentById(userId);
                    runOnUiThread(() -> {
                        if (user != null && Objects.equals(user.getRole(), "admin")) {
                            bottomNav.setSelectedItemId(R.id.navigation_dashboard);
                        } else {
                            bottomNav.setSelectedItemId(R.id.navigation_home);
                        }
                    });
                });
            } else {
                bottomNav.setSelectedItemId(R.id.navigation_home);
            }
        }
    }

    private Fragment getFragmentById(int itemId) {
        if (itemId == R.id.navigation_dashboard) {
            return new DashboardFragment();
        } else if (itemId == R.id.navigation_home) {
            return new HomeFragment();
        } else if (itemId == R.id.navigation_participants) {
            return new ParticipantsFragment();
        } else if (itemId == R.id.navigation_account) {
            return new AccountFragment();
        }
        return null;
    }

    private void loadFragment(Fragment fragment, boolean isRoot) {
        String tag = fragment.getClass().getSimpleName();
        FragmentManager fm = getSupportFragmentManager();
        
        // If it's already the current fragment, don't do anything
        Fragment current = fm.findFragmentById(R.id.fragment_container);
        if (current != null && current.getClass().equals(fragment.getClass())) {
            return;
        }

        if (isRoot) {
            // Dashboard is the end. Clear all backstack.
            fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        var transaction = fm.beginTransaction().replace(R.id.fragment_container, fragment, tag);
        
        if (!isRoot) {
            transaction.addToBackStack(tag);
        }
        
        transaction.commit();
    }
}
