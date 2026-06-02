package com.academicpulse.database.relational;

import androidx.room.Embedded;
import androidx.room.Relation;
import com.academicpulse.database.entity.Event;
import com.academicpulse.database.entity.Registration;

public class RegistrationWithEvent {
    @Embedded
    public Registration registration;

    @Relation(
        parentColumn = "event_id",
        entityColumn = "id"
    )
    public Event event;
}
