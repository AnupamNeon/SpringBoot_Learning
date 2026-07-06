package com._1studentcrud.service;

import com._1studentcrud.model.Student;
import com._1studentcrud.repository.StudentRepository;
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
        return studentRepository.getStudentById(id);
    }

    // Add student
    public Student addStudent(Student student) {
        return studentRepository.addStudent(student);
    }

    // Update student
    public Student updateStudent(int id, Student student) {
        return studentRepository.updateStudent(id, student);
    }

    // Get all students
    public List<Student> getAllStudents() {
        return studentRepository.getAllStudents();
    }

    // Delete student
    public String deleteStudent(int id) {
        return studentRepository.deleteStudent(id);
    }
}