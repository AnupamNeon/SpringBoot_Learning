package com._1studentcrud.service;

import com._1studentcrud.model.Course;
import com._1studentcrud.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public Course getCourseById(int id) {
        return courseRepository.getCourseById(id);
    }

    public List<Course> getAllCourses() {
        return courseRepository.getAllCourse();
    }

    public Course addCourse(Course course) {
        return courseRepository.addCourse(course);
    }

    public Course updateCourse(Course course) {
        return courseRepository.updateCourse(course);
    }

    public String deleteCourse(int id) {
        return courseRepository.deleteCourse(id);
    }
}