package com._4jpa_hibernate.controller;

import com._4jpa_hibernate.entity.Course;
import com._4jpa_hibernate.service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    // Get all courses
    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    // Get course by ID
    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable int id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    // Add new course
    @PostMapping
    public ResponseEntity<Course> addCourse(@RequestBody Course course) {
        Course savedCourse = courseService.addCourse(course);
        return new ResponseEntity<>(savedCourse, HttpStatus.CREATED);
    }

    // Update course
    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable int id,
                                               @RequestBody Course course) {
        return ResponseEntity.ok(courseService.updateCourse(id, course));
    }

    // Delete course
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCourse(@PathVariable int id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok("Course deleted successfully");
    }

    // Assign teacher to course
    @PutMapping("/{courseId}/teacher/{teacherId}")
    public ResponseEntity<Course> assignTeacher(@PathVariable int courseId, @PathVariable int teacherId) {
        return ResponseEntity.ok(courseService.assignTeacher(courseId, teacherId));
    }

    // Get courses by Teacher's Department (Uses Custom JPQL)
    @GetMapping("/department/{dept}")
    public ResponseEntity<List<Course>> getCoursesByDepartment(@PathVariable String dept) {
        return ResponseEntity.ok(courseService.getCoursesByDepartment(dept));
    }

    // Get courses enrolled by a specific Student Name (Uses Custom JPQL Join)
    @GetMapping("/student/{name}")
    public ResponseEntity<List<Course>> getCoursesByStudentName(@PathVariable String name) {
        return ResponseEntity.ok(courseService.getCoursesByStudentName(name));
    }

    // Get course by name using Native SQL
    @GetMapping("/search")
    public ResponseEntity<List<Course>> searchCourseByName(@RequestParam String name) {
        return ResponseEntity.ok(courseService.getCourseByNameNative(name));
    }
}