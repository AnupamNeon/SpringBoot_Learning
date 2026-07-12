package com._4jpa_hibernate.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entity representing a Course. Relationships: 1. Many Courses -> One Teacher
 * 2. Many Courses <-> Many Students
 */
@Entity
@Table(name = "course")
public class Course {

    @Id // Primary key of the course table
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Database auto-generates the ID
    @Column(name = "course_id")
    private Integer id;

    // Course name cannot be null
    @Column(name = "course_name", nullable = false)
    private String name;

    /**
     * MANY courses can be assigned to ONE teacher. FetchType.LAZY: The teacher
     * object is loaded only when getTeacher() is called, improving performance.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", referencedColumnName = "teacher_id") // teacher_id is the foreign key in the course table.
    private Teacher teacher;

    /**
     * MANY courses can have MANY students. mappedBy = "courses" means the
     * Student entity owns the relationship and contains the @JoinTable
     * annotation. This side is the inverse (non-owning) side.
     */
    @ManyToMany(mappedBy = "courses")
    @JsonIgnore  // Prevents infinite recursion during JSON serialization
    private List<Student> students = new ArrayList<>();

    // Required by JPA
    public Course() {
    }

    public Course(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }
}
