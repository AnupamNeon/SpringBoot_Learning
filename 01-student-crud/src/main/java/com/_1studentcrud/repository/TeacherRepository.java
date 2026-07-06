package com._1studentcrud.repository;

import com._1studentcrud.model.Course;
import com._1studentcrud.model.Teacher;
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
    public Teacher getTeacherById(int id){
        String sql = "SELECT * FROM teacher WHERE teacher_id = ?";
        try(Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, id);

            ResultSet  rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return null;
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

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // update Teacher
    public Teacher updateTeacher(Teacher teacher){
        String sql = "UPDATE teacher SET teacher_name = ?, email = ?, department = ? WHERE teacher_id = ?";
        int id = teacher.getTeacher_id();

        Teacher t = getTeacherById(id);

        if(t != null) {
            try (
                    Connection conn = dataSource.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)
            ) {
                ps.setString(1, teacher.getTeacher_name());
                ps.setString(2, teacher.getEmail());
                ps.setString(3, teacher.getDepartment());
                ps.setInt(4, id);

                int rows = ps.executeUpdate();
                if (rows > 0) return teacher;

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // delete Teacher
    public String deleteTeacher(int id) {
        String sql = "DELETE FROM teacher WHERE teacher_id = ?";

        // check is teacher associated with a course
        String query = "SELECT * FROM course WHERE teacher_id = ?";

        // is Valid teacher
        Teacher t = getTeacherById(id);
        if (t != null) {
            try {
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setInt(1, id);

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int c_id = rs.getInt("course_id");
                    Course c = courseRepository.getCourseById(c_id);
                    c.setTeacher_id(null);
                    Course updated = courseRepository.updateCourse(c);
                }

                PreparedStatement deletePs = conn.prepareStatement(sql);
                deletePs.setInt(1, id);
                deletePs.executeUpdate();
                
                conn.close();
                return "Teacher Deleted successfully";
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return "Invalid teacher id";
    }

    // all Teacher
    public List<Teacher> getAllTeacher(){
        List<Teacher>  result= new ArrayList<>();
        String sql = "SELECT * FROM teacher";
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
