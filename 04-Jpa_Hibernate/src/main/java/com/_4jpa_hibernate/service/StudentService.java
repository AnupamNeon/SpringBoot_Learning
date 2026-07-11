package com._4jpa_hibernate.service;

import com._4jpa_hibernate.exception.ResourceNotFoundException;
import com._4jpa_hibernate.entity.Student;
import com._4jpa_hibernate.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student getStudentById(int id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with id: " + id));
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student addStudent(Student student) {
        //    Add a business-key check here if needed (e.g., name + address)
        return studentRepository.save(student);
    }

    public Student updateStudent(int id, Student student) {
        Student existing = getStudentById(id);

        existing.setName(student.getName());
        existing.setAge(student.getAge());
        existing.setAddress(student.getAddress());

        return studentRepository.save(existing);
    }

    public void deleteStudent(int id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Student not found with id: " + id);
        }
        studentRepository.deleteById(id);
    }
}