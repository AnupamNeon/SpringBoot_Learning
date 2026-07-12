package com._4jpa_hibernate.service;

import com._4jpa_hibernate.entity.Course;
import com._4jpa_hibernate.entity.Student;
import com._4jpa_hibernate.entity.StudentProfile;
import com._4jpa_hibernate.exception.DuplicateResourceException;
import com._4jpa_hibernate.exception.ResourceNotFoundException;
import com._4jpa_hibernate.repository.CourseRepository;
import com._4jpa_hibernate.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public StudentService(StudentRepository studentRepository, CourseRepository courseRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    public Student getStudentByEmail(String email) {
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with email: " + email));
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student addStudent(Student student) {
        if (student.getEmail() != null && studentRepository.existsByEmail(student.getEmail())) {
            throw new DuplicateResourceException(
                    "Student already exists with email: " + student.getEmail());
        }

        return studentRepository.save(student);
    }

    public Student updateStudent(String email, Student student) {
        Student existing = getStudentByEmail(email);

        if (student.getEmail() != null
                && !student.getEmail().equals(existing.getEmail())
                && studentRepository.existsByEmail(student.getEmail())) {
            throw new DuplicateResourceException(
                    "Student already exists with email: " + student.getEmail());
        }

        existing.setName(student.getName());

        if (student.getEmail() != null) existing.setEmail(student.getEmail());
        if (student.getName() != null) existing.setName(student.getName());
        if (student.getAge() != null) existing.setAge(student.getAge());
        if (student.getAddress() != null) existing.setAddress(student.getAddress());

        return studentRepository.save(existing);
    }

    public void deleteStudent(String email) {
        Student student = getStudentByEmail(email);
        studentRepository.delete(student);
    }

    // Assign / Update Student Profile
    public Student assignProfile(String email, StudentProfile profile) {
        Student student = getStudentByEmail(email);

        profile.setStudent(student);
        student.setProfile(profile);

        return studentRepository.save(student);
    }

    // Enroll Student in a Course by course name
    public Student enrollCourse(String studentEmail, String courseName) {
        Student student = getStudentByEmail(studentEmail);

        Course course = courseRepository.findByName(courseName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course not found with name: " + courseName));

        student.addCourse(course);

        return studentRepository.save(student);
    }

    // Unenroll Student from a Course by course name
    public Student unenrollCourse(String studentEmail, String courseName) {
        Student student = getStudentByEmail(studentEmail);

        Course course = courseRepository.findByName(courseName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course not found with name: " + courseName));

        student.removeCourse(course);

        return studentRepository.save(student);
    }

    // Pagination
    public Page<Student> getStudentsWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return studentRepository.findAll(pageable);
    }

    // Sorting only
    public List<Student> getStudentsSorted(String sortBy) {
        return studentRepository.findAll(Sort.by(Sort.Direction.ASC, sortBy));
    }

    // Pagination + Sorting
    public Page<Student> getStudentsWithPaginationAndSorting(
            int page, int size, String sortBy, String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return studentRepository.findAll(pageable);
    }
}