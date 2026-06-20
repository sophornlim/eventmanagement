package com.academicpulse;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.academicpulse.database.AppDatabase;
import com.academicpulse.database.entity.Event;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

public class CreateEventActivity extends AppCompatActivity {

    private int eventId = -1;
    private boolean isEditMode = false;
    private Uri selectedImageUri = null;

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

        Spinner spinner = findViewById(R.id.spinner_category);
        String[] displayCategories = {"ការសិក្សា", "សង្គម", "កីឡា", "សិក្ខាសាលា"};
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, displayCategories));

        if (isEditMode && eventId != -1) {
            loadEventData();
        }

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
        String category = ((Spinner) findViewById(R.id.spinner_category)).getSelectedItem().toString();
        String imageUrl = selectedImageUri != null ? selectedImageUri.toString() : "";

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "សូមបំពេញព័ត៌មានឱ្យបានគ្រប់គ្រាន់", Toast.LENGTH_SHORT).show();
            return;
        }

        int capacityValue = 0;
        try {
            capacityValue = Integer.parseInt(capacityStr);
        } catch (NumberFormatException ignored) {}

        Event event = new Event(title, desc, imageUrl, category, location, date, time, 101, capacityValue, "Active");
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
