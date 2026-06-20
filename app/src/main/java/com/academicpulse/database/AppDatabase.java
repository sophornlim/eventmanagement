package com.academicpulse.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

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
    version = 5,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract DepartmentDao departmentDao();
    public abstract StudentDao studentDao();
    public abstract EventDao eventDao();
    public abstract RegistrationDao registrationDao();
    public abstract NotificationDao notificationDao();

    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE students ADD COLUMN gender TEXT DEFAULT 'Other'");
        }
    };

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "academic_pulse_database.db"
                    )
                    .addMigrations(MIGRATION_4_5)
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
