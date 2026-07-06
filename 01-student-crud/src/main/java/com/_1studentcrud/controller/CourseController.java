package com._1studentcrud.controller;

import com._1studentcrud.model.Course;
import com._1studentcrud.repository.CourseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseRepository courseRepository;

    public CourseController(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    // Get all courses
    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseRepository.getAllCourse());
    }

    // Get course by ID
    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable int id) {
        Course course = courseRepository.getCourseById(id);

        if (course == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(course);
    }

    // Add new course
    @PostMapping
    public ResponseEntity<Course> addCourse(@RequestBody Course course) {
        Course savedCourse = courseRepository.addCourse(course);

        if (savedCourse == null) {
            return ResponseEntity.badRequest().build();
        }

        return new ResponseEntity<>(savedCourse, HttpStatus.CREATED);
    }

    // Update course
    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable int id,
                                               @RequestBody Course course) {

        if (courseRepository.getCourseById(id) == null) {
            return ResponseEntity.notFound().build();
        }

        course.setCourse_id(id);
        Course updatedCourse = courseRepository.updateCourse(course);

        return ResponseEntity.ok(updatedCourse);
    }

    // Delete course
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCourse(@PathVariable int id) {

        String result = courseRepository.deleteCourse(id);

        if (result == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }
}