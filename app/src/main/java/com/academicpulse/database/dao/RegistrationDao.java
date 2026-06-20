package com.academicpulse.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import com.academicpulse.database.entity.Registration;
import com.academicpulse.database.relational.EventWithRegistrations;
import com.academicpulse.database.relational.RegistrationWithEvent;
import java.util.List;

@Dao
public interface RegistrationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertRegistration(Registration registration);

    @Update
    void updateRegistration(Registration registration);

    @Query("SELECT * FROM registrations WHERE student_id = :studentId")
    List<Registration> getRegistrationsForStudent(String studentId);

    @Query("SELECT * FROM registrations")
    List<Registration> getAllRegistrations();

    @Query("SELECT * FROM registrations WHERE event_id = :eventId")
    List<Registration> getRegistrationsForEvent(int eventId);

    @Transaction
    @Query("SELECT * FROM registrations WHERE student_id = :studentId")
    List<RegistrationWithEvent> getRegistrationsWithEventsForStudent(String studentId);

    @Transaction
    @Query("SELECT * FROM events WHERE id = :eventId")
    EventWithRegistrations getEventWithRegistrations(int eventId);

    @Query("DELETE FROM registrations WHERE event_id = :eventId AND student_id = :studentId")
    void cancelRegistration(int eventId, String studentId);

    @Delete
    void deleteRegistration(Registration registration);
}
