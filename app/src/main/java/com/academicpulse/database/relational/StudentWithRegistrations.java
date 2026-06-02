package com.academicpulse.database.relational;

import androidx.room.Embedded;
import androidx.room.Relation;
import com.academicpulse.database.entity.Registration;
import com.academicpulse.database.entity.Student;
import java.util.List;

public class StudentWithRegistrations {
    @Embedded
    public Student student;

    @Relation(
        entity = Registration.class,
        parentColumn = "id",
        entityColumn = "student_id"
    )
    public List<RegistrationWithEvent> registrations;
}
