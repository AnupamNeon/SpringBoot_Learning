package com._3exception_handling.service;

import com._3exception_handling.exception.DuplicateResourceException;
import com._3exception_handling.exception.ResourceNotFoundException;
import com._3exception_handling.model.Teacher;
import com._3exception_handling.repository.TeacherRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherService {
    private final TeacherRepository teacherRepository;

    public TeacherService(TeacherRepository tr) {
        this.teacherRepository = tr;
    }

    public Teacher getTeacherById(int id) {
        Teacher teacher = teacherRepository.getTeacherById(id);
        if (teacher == null) {
            throw new ResourceNotFoundException("Teacher not found with id: " + id);
        }
        return teacher;
    }

    public List<Teacher> getAllTeacher() {
        return teacherRepository.getAllTeacher();
    }

    public Teacher addTeacher(Teacher teacher) {
        Teacher existing = teacherRepository.getTeacherById(teacher.getTeacher_id());
        if (existing != null) {
            throw new DuplicateResourceException("Teacher already exists with id: " + teacher.getTeacher_id());
        }
        return teacherRepository.addTeacher(teacher);
    }

    public Teacher updateTeacher(int id, Teacher teacher) {
        Teacher existing = teacherRepository.getTeacherById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Teacher not found with id: " + id);
        }
        teacher.setTeacher_id(id);
        return teacherRepository.updateTeacher(teacher);
    }

    public void deleteTeacher(int id) {
        Teacher existing = teacherRepository.getTeacherById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Teacher not found with id: " + id);
        }
        teacherRepository.deleteTeacher(id);
    }
}