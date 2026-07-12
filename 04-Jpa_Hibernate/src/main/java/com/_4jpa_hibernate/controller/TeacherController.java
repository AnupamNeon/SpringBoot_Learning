package com._4jpa_hibernate.controller;

import com._4jpa_hibernate.entity.Teacher;
import com._4jpa_hibernate.service.TeacherService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    // Get all teachers
    @GetMapping
    public ResponseEntity<List<Teacher>> getAllTeachers() {
        return ResponseEntity.ok(teacherService.getAllTeacher());
    }

    // Get teacher by email
    @GetMapping("/{email}")
    public ResponseEntity<Teacher> getTeacherByEmail(@PathVariable String email) {
        return ResponseEntity.ok(teacherService.getTeacherByEmail(email));
    }

    // Add teacher
    @PostMapping
    public ResponseEntity<Teacher> addTeacher(@RequestBody Teacher teacher) {
        Teacher savedTeacher = teacherService.addTeacher(teacher);
        return new ResponseEntity<>(savedTeacher, HttpStatus.CREATED);
    }

    // Update teacher
    @PutMapping("/{email}")
    public ResponseEntity<Teacher> updateTeacher(@PathVariable String email,
                                                 @RequestBody Teacher teacher) {
        return ResponseEntity.ok(teacherService.updateTeacher(email, teacher));
    }

    // Delete teacher
    @DeleteMapping("/{email}")
    public ResponseEntity<String> deleteTeacher(@PathVariable String email) {
        teacherService.deleteTeacher(email);
        return ResponseEntity.ok("Teacher deleted successfully");
    }
}