package com._3exception_handling.repository;

import com._3exception_handling.exception.OperationException;
import com._3exception_handling.model.Course;
import com._3exception_handling.model.Teacher;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TeacherRepository {

    private final DataSource dataSource;
    private final CourseRepository courseRepository;

    public TeacherRepository(DataSource dataSource, CourseRepository cr) {
        this.dataSource = dataSource;
        this.courseRepository = cr;
    }

    private Teacher mapRow(ResultSet rs) throws SQLException {
        return new Teacher(
                rs.getInt("teacher_id"),
                rs.getString("teacher_name"),
                rs.getString("email"),
                rs.getString("department")
        );
    }

    // get teacher by id
    public Teacher getTeacherById(int id) {
        String sql = "SELECT * FROM teacher WHERE teacher_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
            return null;
        } catch (SQLException e) {
            throw new OperationException("Database error while fetching teacher with id: " + id);
        }
    }

    // add teacher
    public Teacher addTeacher(Teacher teacher) {
        String sql = "INSERT INTO teacher(teacher_id, teacher_name, email, department) VALUES(?, ?, ?, ?)";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, teacher.getTeacher_id());
            ps.setString(2, teacher.getTeacher_name());
            ps.setString(3, teacher.getEmail());
            ps.setString(4, teacher.getDepartment());

            int rows = ps.executeUpdate();
            if (rows > 0) return teacher;
            return null;

        } catch (SQLException e) {
            throw new OperationException("Database error while adding teacher with id: " + teacher.getTeacher_id());
        }
    }

    // update Teacher
    public Teacher updateTeacher(Teacher teacher) {
        String sql = "UPDATE teacher SET teacher_name = ?, email = ?, department = ? WHERE teacher_id = ?";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, teacher.getTeacher_name());
            ps.setString(2, teacher.getEmail());
            ps.setString(3, teacher.getDepartment());
            ps.setInt(4, teacher.getTeacher_id());

            int rows = ps.executeUpdate();
            if (rows > 0) return teacher;
            return null;

        } catch (SQLException e) {
            throw new OperationException("Database error while updating teacher with id: " + teacher.getTeacher_id());
        }
    }

    // delete Teacher
    public void deleteTeacher(int id) {
        String deleteSql = "DELETE FROM teacher WHERE teacher_id = ?";
        String querySql = "SELECT * FROM course WHERE teacher_id = ?";

        try {
            Connection conn = dataSource.getConnection();

            // Dissociate ALL courses associated with this teacher
            PreparedStatement queryPs = conn.prepareStatement(querySql);
            queryPs.setInt(1, id);
            ResultSet rs = queryPs.executeQuery();
            while (rs.next()) {
                int c_id = rs.getInt("course_id");
                Course c = courseRepository.getCourseById(c_id);
                if (c != null) {
                    c.setTeacher_id(null);
                    courseRepository.updateCourse(c);
                }
            }

            // Delete the teacher
            PreparedStatement deletePs = conn.prepareStatement(deleteSql);
            deletePs.setInt(1, id);
            deletePs.executeUpdate();

            conn.close();
        } catch (SQLException e) {
            throw new OperationException("Database error while deleting teacher with id: " + id);
        }
    }

    // all Teachers
    public List<Teacher> getAllTeacher() {
        List<Teacher> result = new ArrayList<>();
        String sql = "SELECT * FROM teacher";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new OperationException("Database error while fetching all teachers");
        }
        return result;
    }
}