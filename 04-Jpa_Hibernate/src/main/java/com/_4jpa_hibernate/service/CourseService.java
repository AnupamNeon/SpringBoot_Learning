package com._4jpa_hibernate.service;

import com._4jpa_hibernate.exception.DuplicateResourceException;
import com._4jpa_hibernate.exception.ResourceNotFoundException;
import com._4jpa_hibernate.entity.Course;
import com._4jpa_hibernate.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public Course getCourseById(int id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course not found with id: " + id));
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Course addCourse(Course course) {
        // Check by name (meaningful business key), not auto-generated ID
        if (course.getName() != null
                && courseRepository.existsByName(course.getName())) {
            throw new DuplicateResourceException(
                    "Course already exists with name: " + course.getName());
        }
        return courseRepository.save(course);
    }

    public Course updateCourse(int id, Course course) {
        // Use path variable 'id' for lookup, not request body's ID
        Course existing = getCourseById(id);

        existing.setName(course.getName());
        existing.setTeacher(course.getTeacher());

        return courseRepository.save(existing);
    }

    public void deleteCourse(int id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
    }
}