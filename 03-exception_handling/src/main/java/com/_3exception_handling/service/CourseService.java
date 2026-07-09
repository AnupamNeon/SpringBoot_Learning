package com._3exception_handling.service;

import com._3exception_handling.exception.DuplicateResourceException;
import com._3exception_handling.exception.ResourceNotFoundException;
import com._3exception_handling.model.Course;
import com._3exception_handling.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public Course getCourseById(int id) {
        Course course = courseRepository.getCourseById(id);

        if (course == null)
            throw new ResourceNotFoundException("Course not found with id: " + id);
        return course;
    }

    public List<Course> getAllCourses() {
        return courseRepository.getAllCourse();
    }

    public Course addCourse(Course course) {
        Course existing = courseRepository.getCourseById(course.getCourse_id());

        if (existing != null)
            throw new DuplicateResourceException("Course already exists with id: " + course.getCourse_id());
        return courseRepository.addCourse(course);
    }

    public Course updateCourse(int id, Course course) {
        Course existing = courseRepository.getCourseById(course.getCourse_id());

        if (existing == null)
            throw new ResourceNotFoundException("Course not found with id: " + course.getCourse_id());

        return courseRepository.updateCourse(course);
    }

    public void deleteCourse(int id) {
        Course existing = courseRepository.getCourseById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Course not found with id: " + id);
        }
        courseRepository.deleteCourse(id);
    }
}