package com.academicpulse.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.academicpulse.database.entity.Notification;
import java.util.List;

@Dao
public interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNotification(Notification notification);

    @Update
    void updateNotification(Notification notification);

    @Query("SELECT * FROM notifications WHERE student_id = :studentId ORDER BY id DESC")
    List<Notification> getNotificationsForStudent(String studentId);

    @Query("UPDATE notifications SET is_read = 1 WHERE student_id = :studentId")
    void markAllRead(String studentId);

    @Delete
    void deleteNotification(Notification notification);
}
