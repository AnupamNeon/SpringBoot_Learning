package com._1studentcrud.controller;

import com._1studentcrud.model.Teacher;
import com._1studentcrud.repository.TeacherRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teachers")
public class TeacherController {

    private final TeacherRepository teacherRepository;

    public TeacherController(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    // Get all teachers
    @GetMapping
    public ResponseEntity<List<Teacher>> getAllTeachers() {
        return ResponseEntity.ok(teacherRepository.getAllTeacher());
    }

    // Get teacher by ID
    @GetMapping("/{id}")
    public ResponseEntity<Teacher> getTeacherById(@PathVariable int id) {
        Teacher teacher = teacherRepository.getTeacherById(id);

        if (teacher == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(teacher);
    }

    // Add teacher
    @PostMapping
    public ResponseEntity<Teacher> addTeacher(@RequestBody Teacher teacher) {
        Teacher savedTeacher = teacherRepository.addTeacher(teacher);

        if (savedTeacher == null) {
            return ResponseEntity.badRequest().build();
        }

        return new ResponseEntity<>(savedTeacher, HttpStatus.CREATED);
    }

    // Update teacher
    @PutMapping("/{id}")
    public ResponseEntity<Teacher> updateTeacher(@PathVariable int id,
                                                 @RequestBody Teacher teacher) {

        if (teacherRepository.getTeacherById(id) == null) {
            return ResponseEntity.notFound().build();
        }

        teacher.setTeacher_id(id);
        Teacher updatedTeacher = teacherRepository.updateTeacher(teacher);

        return ResponseEntity.ok(updatedTeacher);
    }

    // Delete teacher
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTeacher(@PathVariable int id) {

        String result = teacherRepository.deleteTeacher(id);

        if ("Invalid teacher id".equals(result)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }
}