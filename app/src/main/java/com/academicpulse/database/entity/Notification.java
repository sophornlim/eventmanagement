package com.academicpulse.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "notifications",
    foreignKeys = @ForeignKey(
        entity = Student.class,
        parentColumns = "id",
        childColumns = "student_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("student_id")}
)
public class Notification {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "student_id")
    private String studentId;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "message")
    private String message;

    @ColumnInfo(name = "sent_at")
    private String sentAt;

    @ColumnInfo(name = "is_read")
    private boolean isRead;

    @ColumnInfo(name = "type")
    private String type;

    public Notification(int id, @NonNull String studentId, String title, String message, String sentAt, boolean isRead, String type) {
        this.id = id;
        this.studentId = studentId;
        this.title = title;
        this.message = message;
        this.sentAt = sentAt;
        this.isRead = isRead;
        this.type = type;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @NonNull
    public String getStudentId() { return studentId; }
    public void setStudentId(@NonNull String studentId) { this.studentId = studentId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSentAt() { return sentAt; }
    public void setSentAt(String sentAt) { this.sentAt = sentAt; }

    public boolean isIsRead() { return isRead; }
    public void setIsRead(boolean isRead) { this.isRead = isRead; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
