package com._4jpa_hibernate.repository;

import com._4jpa_hibernate.entity.Course;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {

    boolean existsByName(String name);

    // Added because StudentService enrolls/unenrolls by course name
    Optional<Course> findByName(String name);

    @Query("SELECT c FROM Course c WHERE c.teacher.department = :dept")
    List<Course> findByTeacherDepartment(@Param("dept") String department);

    @Query("SELECT c FROM Course c JOIN c.students s WHERE s.name = :studentName")
    List<Course> findCoursesByStudentName(@Param("studentName") String name);

    @Query("""
           SELECT c.teacher.name, COUNT(c)
           FROM Course c
           WHERE c.teacher IS NOT NULL
           GROUP BY c.teacher.id, c.teacher.name
           """)
    List<Object[]> countCoursesByTeacher();

    @Override
    @EntityGraph(attributePaths = {"teacher", "students"})
    List<Course> findAll();

    @Query(value = "SELECT * FROM course WHERE course_name = :name", nativeQuery = true)
    List<Course> findByNameNative(@Param("name") String name);
}