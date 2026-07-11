package com._4jpa_hibernate.service;

import com._4jpa_hibernate.exception.DuplicateResourceException;
import com._4jpa_hibernate.exception.ResourceNotFoundException;
import com._4jpa_hibernate.entity.Teacher;
import com._4jpa_hibernate.repository.TeacherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TeacherService {

    private final TeacherRepository teacherRepository;

    public TeacherService(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    public Teacher getTeacherById(int id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Teacher not found with id: " + id));
    }

    public List<Teacher> getAllTeacher() {
        return teacherRepository.findAll();
    }

    public Teacher addTeacher(Teacher teacher) {
        // ✅ Check by email (unique business key), not auto-generated ID
        if (teacher.getEmail() != null
                && teacherRepository.existsByEmail(teacher.getEmail())) {
            throw new DuplicateResourceException(
                    "Teacher already exists with email: " + teacher.getEmail());
        }
        return teacherRepository.save(teacher);
    }

    public Teacher updateTeacher(int id, Teacher teacher) {
        Teacher existing = getTeacherById(id);

        // ✅ Use setId() which actually exists on the entity
        existing.setName(teacher.getName());
        existing.setEmail(teacher.getEmail());
        existing.setDepartment(teacher.getDepartment());

        return teacherRepository.save(existing);
    }

    public void deleteTeacher(int id) {
        if (!teacherRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Teacher not found with id: " + id);
        }
        // ⚠️ Note: Teacher has cascade = CascadeType.ALL, orphanRemoval = true
        // on courses. This will DELETE all associated courses too.
        // If you want to just dissociate instead, change the entity mapping.
        teacherRepository.deleteById(id);
    }
}