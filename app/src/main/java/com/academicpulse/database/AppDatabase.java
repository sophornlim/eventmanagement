package com.academicpulse.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.academicpulse.database.dao.*;
import com.academicpulse.database.entity.*;

@Database(
    entities = {
        Department.class,
        Student.class,
        Event.class,
        Registration.class,
        Notification.class
    },
    version = 4,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract DepartmentDao departmentDao();
    public abstract StudentDao studentDao();
    public abstract EventDao eventDao();
    public abstract RegistrationDao registrationDao();
    public abstract NotificationDao notificationDao();

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "academic_pulse_database.db"
                    )
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
