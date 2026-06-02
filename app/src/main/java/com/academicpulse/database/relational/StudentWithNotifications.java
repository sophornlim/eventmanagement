package com.academicpulse.database.relational;

import androidx.room.Embedded;
import androidx.room.Relation;
import com.academicpulse.database.entity.Notification;
import com.academicpulse.database.entity.Student;
import java.util.List;

public class StudentWithNotifications {
    @Embedded
    public Student student;

    @Relation(
        parentColumn = "id",
        entityColumn = "student_id"
    )
    public List<Notification> notifications;
}
