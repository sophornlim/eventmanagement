package com.academicpulse;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.academicpulse.database.AppDatabase;
import com.academicpulse.database.entity.Event;
import com.academicpulse.database.entity.Registration;
import com.academicpulse.database.entity.Student;
import com.academicpulse.database.relational.EventWithRegistrations;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class EventDetailActivity extends AppCompatActivity {
    private int eventId;
    private ParticipantsAdapter participantsAdapter;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        eventId = getIntent().getIntExtra("EVENT_ID", -1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView rvParticipants = findViewById(R.id.rv_event_participants);
        rvParticipants.setLayoutManager(new LinearLayoutManager(this));
        participantsAdapter = new ParticipantsAdapter(new ArrayList<>());
        rvParticipants.setAdapter(participantsAdapter);

        btnRegister = findViewById(R.id.btn_register_detail);
        btnRegister.setOnClickListener(v -> registerForEvent());

        loadEventData();
    }

    private void registerForEvent() {
        SessionManager sessionManager = new SessionManager(this);
        String userId = sessionManager.getUserId();

        if (userId == null) {
            Toast.makeText(this, "សូមចូលប្រើប្រាស់ជាមុនសិន", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(this);
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                Registration registration = new Registration(userId, eventId, timestamp, "Registered");
                
                db.registrationDao().insertRegistration(registration);
                
                Event event = db.eventDao().getEventById(eventId);
                NotificationHelper.scheduleEventReminder(this, event);
                
                String notifTitle = "ការចុះឈ្មោះជោគជ័យ";
                String notifMessage = "អ្នកបានចុះឈ្មោះសម្រាប់ព្រឹត្តិការណ៍ \"" + event.getTitle() + "\"។";
                NotificationHelper.saveNotificationToDb(this, notifTitle, notifMessage, "Registration");

                runOnUiThread(() -> {
                    Toast.makeText(this, "ចុះឈ្មោះជោគជ័យ!", Toast.LENGTH_SHORT).show();
                    loadEventData(); // Refresh list
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void loadEventData() {
        if (eventId == -1) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            Event event = db.eventDao().getEventById(eventId);
            EventWithRegistrations eventWithRegs = db.registrationDao().getEventWithRegistrations(eventId);
            
            List<Student> students = new ArrayList<>();
            boolean isAlreadyRegistered = false;
            String currentUserId = new SessionManager(this).getUserId();

            if (eventWithRegs != null && eventWithRegs.registrations != null) {
                for (Registration reg : eventWithRegs.registrations) {
                    Student s = db.studentDao().getStudentById(reg.getStudentId());
                    if (s != null) {
                        students.add(s);
                        if (java.util.Objects.equals(s.getId(), currentUserId)) {
                            isAlreadyRegistered = true;
                        }
                    }
                }
            }

            final boolean registered = isAlreadyRegistered;
            runOnUiThread(() -> {
                if (event != null) {
                    displayEvent(event);
                    participantsAdapter.updateParticipants(students);
                    String participantsLabel = getString(R.string.participants) + " (" + students.size() + ")";
                    ((TextView) findViewById(R.id.tv_participants_label)).setText(participantsLabel);
                    
                    if (registered) {
                        btnRegister.setEnabled(false);
                        btnRegister.setText("បានចុះឈ្មោះរួចហើយ");
                        btnRegister.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.text_grey, null)));
                    }
                }
            });
        });
    }

    private void displayEvent(Event event) {
        ((TextView) findViewById(R.id.tv_title)).setText(event.getTitle());
        ((TextView) findViewById(R.id.tv_description)).setText(event.getDescription());

        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            try {
                ((ImageView) findViewById(R.id.iv_header)).setImageURI(Uri.parse(event.getImageUrl()));
            } catch (Exception e) {
                ((ImageView) findViewById(R.id.iv_header)).setImageResource(R.drawable.ic_launcher_background);
            }
        }

        setupInfoRow(findViewById(R.id.row_date), "កាលបរិច្ឆេទ", event.getDate(), android.R.drawable.ic_menu_today);
        setupInfoRow(findViewById(R.id.row_time), "ម៉ោង", event.getTime(), android.R.drawable.ic_menu_recent_history);
        setupInfoRow(findViewById(R.id.row_location), "ទីតាំង", event.getLocation(), android.R.drawable.ic_menu_mylocation);

        // Update speaker info
        TextView tvSpeakerName = findViewById(R.id.tv_speaker_name);
        TextView tvSpeakerTitle = findViewById(R.id.tv_speaker_title);
        
        if (event.getSpeakerName() != null && !event.getSpeakerName().isEmpty()) {
            tvSpeakerName.setText(event.getSpeakerName());
        } else {
            tvSpeakerName.setText("មិនទាន់មានព័ត៌មានវាគ្មិន");
        }
        
        if (event.getSpeakerRole() != null && !event.getSpeakerRole().isEmpty()) {
            tvSpeakerTitle.setText(event.getSpeakerRole());
        } else {
            tvSpeakerTitle.setText("");
        }

        // Update organizer info
        TextView tvOrganizerName = findViewById(R.id.tv_organizer_name);
        if (tvOrganizerName != null) {
            if (event.getOrganizer() != null && !event.getOrganizer().isEmpty()) {
                tvOrganizerName.setText(event.getOrganizer());
            } else {
                tvOrganizerName.setText("សាកលវិទ្យាល័យបៀលប្រាយ");
            }
        }
    }

    private void setupInfoRow(View view, String label, String value, int iconRes) {
        ((TextView) view.findViewById(R.id.tv_row_label)).setText(label);
        ((TextView) view.findViewById(R.id.tv_row_value)).setText(value);
        ((ImageView) view.findViewById(R.id.iv_row_icon)).setImageResource(iconRes);
    }
}
