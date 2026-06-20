package com.academicpulse.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.academicpulse.database.entity.Student;
import java.util.List;

@Dao
public interface StudentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertStudent(Student student);

    @Update
    void updateStudent(Student student);

    @Query("SELECT * FROM students")
    List<Student> getAllStudents();

    @Query("SELECT * FROM students WHERE role != 'admin'")
    List<Student> getAllParticipants();

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    Student getStudentById(String id);

    @Query("SELECT * FROM students WHERE (id = :username OR email = :username) AND password = :password LIMIT 1")
    Student login(String username, String password);

    @Delete
    void deleteStudent(Student student);
}
