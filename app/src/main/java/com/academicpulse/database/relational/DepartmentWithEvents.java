package com.academicpulse.database.relational;

import androidx.room.Embedded;
import androidx.room.Relation;
import com.academicpulse.database.entity.Department;
import com.academicpulse.database.entity.Event;
import java.util.List;

public class DepartmentWithEvents {
    @Embedded
    public Department department;

    @Relation(
        parentColumn = "id",
        entityColumn = "department_id"
    )
    public List<Event> events;
}
