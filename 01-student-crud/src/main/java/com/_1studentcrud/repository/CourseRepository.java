package com._1studentcrud.repository;

import com._1studentcrud.model.Course;
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

    public CourseRepository(DataSource ds){
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
    public Course getCourseById(int id){
        String sql = "SELECT * FROM course WHERE course_id = ?";
        try(Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }


    // add Course
    public Course addCourse(Course course){
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

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // update Course
    public Course updateCourse(Course course){
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

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // delete Course
    public String deleteCourse(int id) {
        String sql = "DELETE FROM course WHERE course_id = ?";
        Course c = getCourseById(id);
        if (c != null) {
            try (
                    Connection conn = dataSource.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)
            ) {
                ps.setInt(1, id);

                int rows = ps.executeUpdate();
                if (rows > 0) return "Course Deleted successfully";

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // all Courses
    public List<Course> getAllCourse(){
        List<Course>  result= new ArrayList<>();
        String sql = "SELECT * FROM course";
        try(Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ResultSet  rs = ps.executeQuery();
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return result;
    }
}

