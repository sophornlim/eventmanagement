package com.academicpulse;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.academicpulse.database.AppDatabase;
import com.academicpulse.database.dao.DepartmentDao;
import com.academicpulse.database.entity.Department;
import com.academicpulse.database.entity.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

public class CreateEventActivity extends AppCompatActivity {

    private int eventId = -1;
    private boolean isEditMode = false;
    private Uri selectedImageUri = null;
    private List<Department> departments = new ArrayList<>();

    private ImageView ivPreview;
    private View layoutPlaceholder;
    private EditText etDate;
    private EditText etTime;

    private final ActivityResultLauncher<String[]> pickImage = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    try {
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    selectedImageUri = uri;
                    ivPreview.setImageURI(uri);
                    ivPreview.setVisibility(View.VISIBLE);
                    layoutPlaceholder.setVisibility(View.GONE);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        ivPreview = findViewById(R.id.iv_event_preview);
        layoutPlaceholder = findViewById(R.id.layout_image_placeholder);
        etDate = findViewById(R.id.et_date);
        etTime = findViewById(R.id.et_time);

        eventId = getIntent().getIntExtra("EVENT_ID", -1);
        isEditMode = getIntent().getBooleanExtra("EDIT_MODE", false);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        findViewById(R.id.card_image_picker).setOnClickListener(v -> pickImage.launch(new String[]{"image/*"}));

        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());

        loadDepartments();

        findViewById(R.id.tv_manage_departments).setOnClickListener(v -> showDepartmentManager());

        findViewById(R.id.btn_save).setOnClickListener(v -> saveEvent());
        findViewById(R.id.btn_cancel).setOnClickListener(v -> finish());

        checkNotificationPermission();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            @SuppressLint("DefaultLocale") String dateStr = String.format("%04d-%02d-%02d", year, month + 1, day);
            etDate.setText(dateStr);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(this, (view, hour, minute) -> {
            Calendar time = Calendar.getInstance();
            time.set(Calendar.HOUR_OF_DAY, hour);
            time.set(Calendar.MINUTE, minute);
            @SuppressLint("DefaultLocale") String timeStr = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(time.getTime());
            etTime.setText(timeStr);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
    }

    private void loadDepartments() {
        Executors.newSingleThreadExecutor().execute(() -> {
            departments = AppDatabase.getInstance(this).departmentDao().getAllDepartments();
            runOnUiThread(() -> {
                populateDepartmentSpinner();
                // Populate fields only after the spinner is ready so the saved category can be reselected
                if (isEditMode && eventId != -1) {
                    loadEventData();
                }
            });
        });
    }

    private void populateDepartmentSpinner() {
        Spinner spinner = findViewById(R.id.spinner_category);
        List<String> names = new ArrayList<>();
        for (Department department : departments) {
            names.add(department.getName());
        }
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names));
    }

    private void selectDepartmentById(int id) {
        Spinner spinner = findViewById(R.id.spinner_category);
        for (int i = 0; i < departments.size(); i++) {
            if (departments.get(i).getId() == id) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    // ---- Department management (create / modify / view) ----

    private void showDepartmentManager() {
        if (departments == null) departments = new ArrayList<>();
        String[] items = new String[departments.size() + 1];
        for (int i = 0; i < departments.size(); i++) {
            Department d = departments.get(i);
            items[i] = d.getName() + " (" + d.getCode() + ")";
        }
        items[departments.size()] = "➕ បន្ថែមប្រភេទព្រឹត្តិការណ៍ថ្មី";

        new AlertDialog.Builder(this)
                .setTitle("គ្រប់គ្រងប្រភេទព្រឹត្តិការណ៍")
                .setItems(items, (dialog, which) -> {
                    if (which == departments.size()) {
                        showDepartmentEditor(null);
                    } else {
                        showDepartmentEditor(departments.get(which));
                    }
                })
                .setNegativeButton("បិទ", null)
                .show();
    }

    private void showDepartmentEditor(Department existing) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad / 2, pad, 0);

        EditText etName = new EditText(this);
        etName.setHint("ប្រភេទព្រឹត្តិការណ៍");
        EditText etCode = new EditText(this);
        etCode.setHint("លេខកូដ");
        if (existing != null) {
            etName.setText(existing.getName());
            etCode.setText(existing.getCode());
        }
        layout.addView(etName);
        layout.addView(etCode);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(existing == null ? "បន្ថែមប្រភេទព្រឹត្តិការណ៍" : "កែប្រែប្រភេទព្រឹត្តិការណ៍")
                .setView(layout)
                .setPositiveButton("រក្សាទុក", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String code = etCode.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "សូមបញ្ចូលប្រភេទព្រឹត្តិការណ៍", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    saveDepartment(existing, name, code);
                })
                .setNegativeButton("បោះបង់", null);

        if (existing != null) {
            builder.setNeutralButton("លុប", (d, w) -> confirmDeleteDepartment(existing));
        }
        builder.show();
    }

    private void saveDepartment(Department existing, String name, String code) {
        Executors.newSingleThreadExecutor().execute(() -> {
            DepartmentDao dao = AppDatabase.getInstance(this).departmentDao();
            int targetId;
            if (existing == null) {
                // Plain (non auto-generated) primary key, so derive the next free id.
                int maxId = 100;
                for (Department d : dao.getAllDepartments()) {
                    if (d.getId() > maxId) maxId = d.getId();
                }
                targetId = maxId + 1;
                dao.insertDepartment(new Department(targetId, name, code));
            } else {
                existing.setName(name);
                existing.setCode(code);
                dao.updateDepartment(existing);
                targetId = existing.getId();
            }
            departments = dao.getAllDepartments();
            runOnUiThread(() -> {
                populateDepartmentSpinner();
                selectDepartmentById(targetId);
                Toast.makeText(this, existing == null ? "បានបន្ថែមប្រភេទព្រឹត្តិការណ៍" : "បានកែប្រែប្រភេទព្រឹត្តិការណ៍", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void confirmDeleteDepartment(Department dept) {
        new AlertDialog.Builder(this)
                .setTitle("លុបប្រភេទព្រឹត្តិការណ៍")
                .setMessage("តើអ្នកប្រាកដជាចង់លុប \"" + dept.getName() + "\" មែនទេ? ព្រឹត្តិការណ៍ដែលជាប់ទាក់ទងនឹងត្រូវលុបផងដែរ។")
                .setPositiveButton("លុប", (d, w) -> deleteDepartment(dept))
                .setNegativeButton("បោះបង់", null)
                .show();
    }

    private void deleteDepartment(Department dept) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                DepartmentDao dao = AppDatabase.getInstance(this).departmentDao();
                dao.deleteDepartment(dept);
                departments = dao.getAllDepartments();
                runOnUiThread(() -> {
                    populateDepartmentSpinner();
                    Toast.makeText(this, "បានលុបប្រភេទព្រឹត្តិការណ៍", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                String msg = "មិនអាចលុបប្រភេទព្រឹត្តិការណ៍បានទេ";
                runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_LONG).show());
            }
        });
    }

    private void loadEventData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Event event = AppDatabase.getInstance(this).eventDao().getEventById(eventId);
            runOnUiThread(() -> {
                if (event != null) {
                    ((EditText) findViewById(R.id.et_title)).setText(event.getTitle());
                    ((EditText) findViewById(R.id.et_description)).setText(event.getDescription());
                    ((EditText) findViewById(R.id.et_location)).setText(event.getLocation());
                    String capacityText = "" + event.getCapacity();
                    ((EditText) findViewById(R.id.et_capacity)).setText(capacityText);
                    ((EditText) findViewById(R.id.et_speaker_name)).setText(event.getSpeakerName());
                    ((EditText) findViewById(R.id.et_speaker_role)).setText(event.getSpeakerRole());
                    ((EditText) findViewById(R.id.et_organizer)).setText(event.getOrganizer());
                    etDate.setText(event.getDate());
                    etTime.setText(event.getTime());

                    // Set spinner selection
                    Spinner spinner = findViewById(R.id.spinner_category);
                    ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
                    int pos = adapter.getPosition(event.getCategory());
                    if (pos != -1) spinner.setSelection(pos);

                    if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
                        try {
                            selectedImageUri = Uri.parse(event.getImageUrl());
                            ivPreview.setImageURI(selectedImageUri);
                            ivPreview.setVisibility(View.VISIBLE);
                            layoutPlaceholder.setVisibility(View.GONE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        });
    }

    private void saveEvent() {
        String title = ((EditText) findViewById(R.id.et_title)).getText().toString();
        String desc = ((EditText) findViewById(R.id.et_description)).getText().toString();
        String location = ((EditText) findViewById(R.id.et_location)).getText().toString();
        String capacityStr = ((EditText) findViewById(R.id.et_capacity)).getText().toString();
        String speakerName = ((EditText) findViewById(R.id.et_speaker_name)).getText().toString();
        String speakerRole = ((EditText) findViewById(R.id.et_speaker_role)).getText().toString();
        String organizer = ((EditText) findViewById(R.id.et_organizer)).getText().toString();
        String date = etDate.getText().toString();
        String time = etTime.getText().toString();
        Spinner spinner = findViewById(R.id.spinner_category);
        int selectedPos = spinner.getSelectedItemPosition();
        String category = spinner.getSelectedItem() != null ? spinner.getSelectedItem().toString() : "";
        int departmentId = (selectedPos >= 0 && selectedPos < departments.size())
                ? departments.get(selectedPos).getId() : 101;
        String imageUrl = selectedImageUri != null ? selectedImageUri.toString() : "";

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "សូមបំពេញព័ត៌មានឱ្យបានគ្រប់គ្រាន់", Toast.LENGTH_SHORT).show();
            return;
        }

        int capacityValue = 0;
        try {
            capacityValue = Integer.parseInt(capacityStr);
        } catch (NumberFormatException ignored) {}

        Event event = new Event(title, desc, imageUrl, category, location, date, time, departmentId, capacityValue, "Active");
        event.setSpeakerName(speakerName);
        event.setSpeakerRole(speakerRole);
        event.setOrganizer(organizer);
        if (isEditMode) {
            event.setId(eventId);
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                long id = AppDatabase.getInstance(this).eventDao().insertEvent(event);
                event.setId((int) id);
                
                NotificationHelper.scheduleEventReminder(this, event);
                
                String notifTitle = isEditMode ? "ព្រឹត្តិការណ៍ត្រូវបានកែប្រែ" : "ព្រឹត្តិការណ៍ត្រូវបានបង្កើត";
                String notifMessage = "ព្រឹត្តិការណ៍ \"" + event.getTitle() + "\" ត្រូវបានរក្សាទុកដោយជោគជ័យ។";
                NotificationHelper.saveNotificationToDb(this, notifTitle, notifMessage, "System");

                runOnUiThread(() -> {
                    String message = isEditMode ? "ព្រឹត្តិការណ៍ត្រូវបានកែប្រែដោយជោគជ័យ" : "ព្រឹត្តិការណ៍ត្រូវបានបង្កើតដោយជោគជ័យ";
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (Exception e) {
                e.printStackTrace();
                String errorMsg = "Error: " + e.getMessage();
                runOnUiThread(() -> Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show());
            }
        });
    }
}
