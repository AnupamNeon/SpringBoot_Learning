package com._4jpa_hibernate.repository;

import com._4jpa_hibernate.entity.Teacher;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Integer> {

    Optional<Teacher> findByEmail(String email);

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = "courses")
    @Query("SELECT DISTINCT t FROM Teacher t")
    List<Teacher> findAllWithCourses();
}