package com._3exception_handling.service;

import com._3exception_handling.exception.DuplicateResourceException;
import com._3exception_handling.exception.ResourceNotFoundException;
import com._3exception_handling.model.Student;
import com._3exception_handling.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // Get student by ID
    public Student getStudentById(int id) {
        Student student = studentRepository.getStudentById(id);
        if (student == null)
            throw new ResourceNotFoundException("Student not found with id: " + id);
        return student;
    }

    // Add student
    public Student addStudent(Student student) {
        Student existing = studentRepository.getStudentById(student.getStudent_id());
        if (existing != null)
            throw new DuplicateResourceException("Student already exists with id: " + student.getStudent_id());
        return studentRepository.addStudent(student);
    }

    // Update student
    public Student updateStudent(int id, Student student) {
        Student existing = studentRepository.getStudentById(id);
        if (existing == null)
            throw new ResourceNotFoundException("Student not found with id: " + id);

        return studentRepository.updateStudent(id, student);
    }

    // Get all students
    public List<Student> getAllStudents() {
        return studentRepository.getAllStudents();
    }

    // Delete student
    public void deleteStudent(int id) {
        Student existing = studentRepository.getStudentById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Student not found with id: " + id);
        }
        studentRepository.deleteStudent(id);
    }
}