package com.academicpulse.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "events",
    foreignKeys = @ForeignKey(
        entity = Department.class,
        parentColumns = "id",
        childColumns = "department_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("department_id")}
)
public class Event {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "image_url")
    private String imageUrl;

    @ColumnInfo(name = "category")
    private String category;

    @ColumnInfo(name = "location")
    private String location;

    @ColumnInfo(name = "date")
    private String date;

    @ColumnInfo(name = "time")
    private String time;

    @ColumnInfo(name = "department_id")
    private int departmentId;

    @ColumnInfo(name = "capacity")
    private int capacity;

    @ColumnInfo(name = "status")
    private String status;

    public Event(String title, String description, String imageUrl, String category, 
                 String location, String date, String time, int departmentId, int capacity, String status) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category;
        this.location = location;
        this.date = date;
        this.time = time;
        this.departmentId = departmentId;
        this.capacity = capacity;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
