package com._4jpa_hibernate.controller;

import com._4jpa_hibernate.entity.Student;
import com._4jpa_hibernate.entity.StudentProfile;
import com._4jpa_hibernate.service.StudentService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/students")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    // Get student by email
    @GetMapping("/{email}")
    public ResponseEntity<Student> getStudent(@PathVariable("email") String email) {
        return ResponseEntity.ok(service.getStudentByEmail(email));
    }

    // Add student
    @PostMapping("/add")
    public ResponseEntity<Student> addStudent(@RequestBody Student student) {
        Student created = service.addStudent(student);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // Update student
    @PutMapping("/{email}")
    public ResponseEntity<Student> updateStudent(
            @PathVariable("email") String email,
            @RequestBody Student student) {
        return ResponseEntity.ok(service.updateStudent(email, student));
    }

    // Delete student
    @DeleteMapping("/{email}")
    public ResponseEntity<String> deleteStudent(@PathVariable("email") String email) {
        service.deleteStudent(email);
        return ResponseEntity.ok("Student deleted successfully");
    }

    // Assign profile to student
    @PostMapping("/{email}/profile")
    public ResponseEntity<Student> assignProfile(
            @PathVariable("email") String email,
            @RequestBody StudentProfile profile) {
        return ResponseEntity.ok(service.assignProfile(email, profile));
    }

    // Enroll student into a course by course name
    @PostMapping("/{email}/courses/{courseName}")
    public ResponseEntity<Student> enrollCourse(
            @PathVariable("email") String email,
            @PathVariable("courseName") String courseName) {
        return ResponseEntity.ok(service.enrollCourse(email, courseName));
    }

    // Unenroll student from a course by course name
    @DeleteMapping("/{email}/courses/{courseName}")
    public ResponseEntity<Student> unenrollCourse(
            @PathVariable("email") String email,
            @PathVariable("courseName") String courseName) {
        return ResponseEntity.ok(service.unenrollCourse(email, courseName));
    }

    // GET /students?page=0&size=5&sortBy=name&direction=asc
    @GetMapping
    public ResponseEntity<Page<Student>> getAllStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "email") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Page<Student> result = service.getStudentsWithPaginationAndSorting(page, size, sortBy, direction);
        return ResponseEntity.ok(result);
    }
}