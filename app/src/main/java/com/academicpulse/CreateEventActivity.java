package com.academicpulse;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.academicpulse.database.AppDatabase;
import com.academicpulse.database.entity.Event;

import java.util.Calendar;
import java.util.concurrent.Executors;

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
        String[] categories = {"Academic", "Social", "Sports", "Workshop"};
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories));

        if (isEditMode && eventId != -1) {
            loadEventData();
        }

        findViewById(R.id.btn_save).setOnClickListener(v -> saveEvent());
        findViewById(R.id.btn_cancel).setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            String dateStr = String.format("%04d-%02d-%02d", year, month + 1, day);
            etDate.setText(dateStr);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(this, (view, hour, minute) -> {
            String timeStr = String.format("%02d:%02d", hour, minute);
            etTime.setText(timeStr);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private void loadEventData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Event event = AppDatabase.getInstance(this).eventDao().getEventById(eventId);
            runOnUiThread(() -> {
                if (event != null) {
                    ((EditText) findViewById(R.id.et_title)).setText(event.getTitle());
                    ((EditText) findViewById(R.id.et_description)).setText(event.getDescription());
                    ((EditText) findViewById(R.id.et_location)).setText(event.getLocation());
                    etDate.setText(event.getDate());
                    etTime.setText(event.getTime());

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
        String date = etDate.getText().toString();
        String time = etTime.getText().toString();
        String category = ((Spinner) findViewById(R.id.spinner_category)).getSelectedItem().toString();
        String imageUrl = selectedImageUri != null ? selectedImageUri.toString() : "";

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "សូមបំពេញព័ត៌មានឱ្យបានគ្រប់គ្រាន់", Toast.LENGTH_SHORT).show();
            return;
        }

        Event event = new Event(title, desc, imageUrl, category, location, date, time, 101, 100, "Active");
        if (isEditMode) {
            event.setId(eventId);
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AppDatabase.getInstance(this).eventDao().insertEvent(event);
                runOnUiThread(() -> {
                    String message = isEditMode ? "ព្រឹត្តិការណ៍ត្រូវបានកែប្រែដោយជោគជ័យ" : "ព្រឹត្តិការណ៍ត្រូវបានបង្កើតដោយជោគជ័យ";
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }
}
