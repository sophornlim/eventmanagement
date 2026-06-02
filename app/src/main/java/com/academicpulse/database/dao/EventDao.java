package com.academicpulse.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.academicpulse.database.entity.Event;
import java.util.List;

@Dao
public interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertEvent(Event event);

    @Update
    void updateEvent(Event event);

    @Query("SELECT * FROM events ORDER BY date ASC")
    List<Event> getAllEvents();

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    Event getEventById(int id);

    @Query("SELECT * FROM events WHERE department_id = :deptId")
    List<Event> getEventsByDepartment(int deptId);

    @Delete
    void deleteEvent(Event event);
}
