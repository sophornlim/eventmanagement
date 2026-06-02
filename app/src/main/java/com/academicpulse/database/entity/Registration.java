package com.academicpulse.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "registrations",
    foreignKeys = {
        @ForeignKey(
            entity = Student.class,
            parentColumns = "id",
            childColumns = "student_id",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = Event.class,
            parentColumns = "id",
            childColumns = "event_id",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index("student_id"),
        @Index("event_id")
    }
)
public class Registration {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "student_id")
    private String studentId;

    @ColumnInfo(name = "event_id")
    private int eventId;

    @ColumnInfo(name = "registered_at")
    private String registeredAt;

    @ColumnInfo(name = "status")
    private String status;

    public Registration(@NonNull String studentId, int eventId, String registeredAt, String status) {
        this.studentId = studentId;
        this.eventId = eventId;
        this.registeredAt = registeredAt;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @NonNull
    public String getStudentId() { return studentId; }
    public void setStudentId(@NonNull String studentId) { this.studentId = studentId; }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public String getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(String registeredAt) { this.registeredAt = registeredAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
