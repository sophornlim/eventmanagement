package com.academicpulse;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.academicpulse.database.AppDatabase;
import com.academicpulse.database.entity.Student;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.concurrent.Executors;

public class AccountFragment extends Fragment {

    private ImageView ivProfile;
    private TextView tvName;
    private TextView tvEmail;

    private final ActivityResultLauncher<String[]> pickImage = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    try {
                        requireContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ivProfile.setImageURI(uri);
                    saveProfileImage(uri.toString());
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        ivProfile = view.findViewById(R.id.iv_profile);
        tvName = view.findViewById(R.id.tv_profile_name);
        tvEmail = view.findViewById(R.id.tv_profile_email);

        view.findViewById(R.id.btn_back).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });

        view.findViewById(R.id.iv_notifications).setOnClickListener(v -> startActivity(new Intent(requireContext(), NotificationsActivity.class)));

        view.findViewById(R.id.profile_image_container).setOnClickListener(v -> pickImage.launch(new String[]{"image/*"}));
        view.findViewById(R.id.btn_edit_profile).setOnClickListener(v -> pickImage.launch(new String[]{"image/*"}));

        loadUserData();

        setupMenuItem(view.findViewById(R.id.menu_personal_info), "ព័ត៌មានផ្ទាល់ខ្លួន", "គ្រប់គ្រងទិន្នន័យផ្ទាល់ខ្លួន", android.R.drawable.ic_menu_info_details);
        setupMenuItem(view.findViewById(R.id.menu_registrations), "ការចុះឈ្មោះរបស់ខ្ញុំ", "មើលព្រឹត្តិការណ៍ដែលបានចុះឈ្មោះ", android.R.drawable.ic_menu_agenda);
        setupMenuItem(view.findViewById(R.id.menu_management), "ការគ្រប់គ្រង", "ចូលទៅកាន់ផ្នែកគ្រប់គ្រង", android.R.drawable.ic_menu_manage);
        setupMenuItem(view.findViewById(R.id.menu_dashboard), "ផ្ទាំងគ្រប់គ្រង", "មើលស្ថិតិសរុប", android.R.drawable.ic_dialog_dialer);
        setupMenuItem(view.findViewById(R.id.menu_guide), "របៀបប្រើប្រាស់", "ជំនួយក្នុងការប្រើប្រាស់កម្មវិធី", android.R.drawable.ic_menu_help);

        view.findViewById(R.id.menu_personal_info).setOnClickListener(v -> startActivity(new Intent(requireContext(), EditProfileActivity.class)));

        view.findViewById(R.id.menu_registrations).setOnClickListener(v -> getParentFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, new RegistrationsFragment())
            .addToBackStack(null)
            .commit());

        view.findViewById(R.id.menu_management).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity activity) {
                BottomNavigationView nav = activity.findViewById(R.id.bottom_navigation);
                nav.setSelectedItemId(R.id.navigation_participants);
            }
        });

        view.findViewById(R.id.menu_dashboard).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity activity) {
                BottomNavigationView nav = activity.findViewById(R.id.bottom_navigation);
                nav.setSelectedItemId(R.id.navigation_dashboard);
            }
        });

        view.findViewById(R.id.menu_guide).setOnClickListener(v -> startActivity(new Intent(requireContext(), GuideActivity.class)));

        Button btnLogout = view.findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            new SessionManager(requireContext()).logout();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            if (getActivity() != null) {
                getActivity().finishAffinity();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }

    private void loadUserData() {
        String userId = new SessionManager(requireContext()).getUserId();
        if (userId == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            Student user = AppDatabase.getInstance(requireContext()).studentDao().getStudentById(userId);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (user != null) {
                        tvName.setText(user.getName());
                        tvEmail.setText(user.getEmail());
                        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                            try {
                                ivProfile.setImageURI(Uri.parse(user.getAvatarUrl()));
                            } catch (Exception e) {
                                ivProfile.setImageResource(R.drawable.ic_default_avatar);
                                e.printStackTrace();
                            }
                        } else {
                            ivProfile.setImageResource(R.drawable.ic_default_avatar);
                        }
                    }
                });
            }
        });
    }

    private void saveProfileImage(String uri) {
        String userId = new SessionManager(requireContext()).getUserId();
        if (userId == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            Student user = db.studentDao().getStudentById(userId);
            if (user != null) {
                user.setAvatarUrl(uri);
                db.studentDao().updateStudent(user);
            }
        });
    }

    private void setupMenuItem(View view, String title, String subtitle, int iconRes) {
        TextView tvTitle = view.findViewById(R.id.tv_menu_title);
        TextView tvSubtitle = view.findViewById(R.id.tv_menu_subtitle);
        ImageView ivIcon = view.findViewById(R.id.iv_menu_icon);

        tvTitle.setText(title);
        tvSubtitle.setText(subtitle);
        ivIcon.setImageResource(iconRes);

        if (subtitle.isEmpty()) {
            tvSubtitle.setVisibility(View.GONE);
        }
    }
}
