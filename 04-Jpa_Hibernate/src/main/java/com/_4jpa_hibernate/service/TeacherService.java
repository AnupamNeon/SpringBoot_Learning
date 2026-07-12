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

    public Teacher getTeacherByEmail(String email) {
        return teacherRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Teacher not found with email: " + email));
    }

    public List<Teacher> getAllTeacher() {
        return teacherRepository.findAllWithCourses();
    }


    public Teacher addTeacher(Teacher teacher) {
        if (teacher.getEmail() != null &&
                teacherRepository.existsByEmail(teacher.getEmail())) {

            throw new DuplicateResourceException("Teacher already exists with email: "  + teacher.getEmail());
        }
        return teacherRepository.save(teacher);
    }

    public Teacher updateTeacher(String email, Teacher teacher) {
        Teacher existing = getTeacherByEmail(email);

        if (!existing.getEmail().equals(teacher.getEmail())
                && teacherRepository.existsByEmail(teacher.getEmail())) {

            throw new DuplicateResourceException("Email already exists: " + teacher.getEmail());
        }

        existing.setName(teacher.getName());
        existing.setEmail(teacher.getEmail());
        existing.setDepartment(teacher.getDepartment());

        return teacherRepository.save(existing);
    }

    public void deleteTeacher(String email) {
        Teacher teacher = getTeacherByEmail(email);
        teacherRepository.delete(teacher);
    }
}