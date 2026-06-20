package com.academicpulse.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.academicpulse.database.entity.Department;
import java.util.List;

@Dao
public interface DepartmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDepartment(Department department);

    @Update
    void updateDepartment(Department department);

    @Query("SELECT * FROM departments")
    List<Department> getAllDepartments();

    @Query("SELECT * FROM departments WHERE id = :id LIMIT 1")
    Department getDepartmentById(int id);

    @Delete
    void deleteDepartment(Department department);
}
