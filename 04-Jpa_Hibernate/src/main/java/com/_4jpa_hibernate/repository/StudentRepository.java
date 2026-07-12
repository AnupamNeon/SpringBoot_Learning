package com._4jpa_hibernate.repository;

import com._4jpa_hibernate.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {
    boolean existsByEmail(String email);

    Optional<Student> findByEmail(String email);
}