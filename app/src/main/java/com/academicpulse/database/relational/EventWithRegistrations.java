package com.academicpulse.database.relational;

import androidx.room.Embedded;
import androidx.room.Relation;
import com.academicpulse.database.entity.Event;
import com.academicpulse.database.entity.Registration;
import java.util.List;

public class EventWithRegistrations {
    @Embedded 
    public Event event;

    @Relation(
        parentColumn = "id",
        entityColumn = "event_id"
    )
    public List<Registration> registrations;
}
