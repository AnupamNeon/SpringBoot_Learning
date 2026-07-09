package com._3exception_handling.repository;

import com._3exception_handling.exception.OperationException;
import com._3exception_handling.model.Course;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CourseRepository {
    private final DataSource dataSource;

    public CourseRepository(DataSource ds) {
        this.dataSource = ds;
    }

    private Course mapRow(ResultSet rs) throws SQLException {
        return new Course(
                rs.getInt("course_id"),
                rs.getString("course_name"),
                rs.getInt("teacher_id")
        );
    }

    // get course by id
    public Course getCourseById(int id) {
        String sql = "SELECT * FROM course WHERE course_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
            return null; // Not found — service layer will handle this
        } catch (SQLException e) {
            throw new OperationException("Database error while fetching course with id: " + id);
        }
    }


    // add Course
    public Course addCourse(Course course) {
        String sql = "INSERT INTO course(course_id, course_name, teacher_id) VALUES (?, ?, ?)";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, course.getCourse_id());
            ps.setString(2, course.getCourse_name());
            ps.setInt(3, course.getTeacher_id());

            int rows = ps.executeUpdate();
            if (rows > 0) return course;
            return null;

        } catch (SQLException e) {
            throw new OperationException("Database error while adding course with id: " + course.getCourse_id());
        }
    }

    // update Course
    public Course updateCourse(Course course) {
        String sql = "UPDATE course SET course_name = ?, teacher_id = ? WHERE course_id = ?";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, course.getCourse_name());
            ps.setInt(2, course.getTeacher_id());
            ps.setInt(3, course.getCourse_id());

            int rows = ps.executeUpdate();
            if (rows > 0) return course;
            return null;

        } catch (SQLException e) {
            throw new OperationException("Database error while updating course with id: " + course.getCourse_id());
        }
    }

    // delete Course
    public boolean deleteCourse(int id) {
        String sql = "DELETE FROM course WHERE course_id = ?";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            throw new OperationException("Database error while deleting course with id: " + id);
        }
    }

    // all Courses
    public List<Course> getAllCourse() {
        List<Course> result = new ArrayList<>();
        String sql = "SELECT * FROM course";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new OperationException("Database error while fetching all courses");
        }
        return result;
    }
}