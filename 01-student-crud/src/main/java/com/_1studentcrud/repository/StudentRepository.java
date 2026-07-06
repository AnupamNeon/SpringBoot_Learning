package com._1studentcrud.repository;

import com._1studentcrud.model.Student;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class StudentRepository {
    private final DataSource dataSource;

    public StudentRepository(DataSource ds){
        this.dataSource = ds;
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt("student_id"),
                rs.getString("name"),
                rs.getInt("age"),
                rs.getString("address"),
                rs.getString("course")
        );
    }

    // get Student by id
    public Student getStudentById(int id){
        String sql = "SELECT * FROM student WHERE student_id = ?";
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

    // add Student
    public Student addStudent(Student student) {
        String sql = "INSERT INTO student(student_id, name, age, address, course) VALUES (?, ?, ?, ?, ?)";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, student.getStudent_id());
            ps.setString(2, student.getName());
            ps.setInt(3, student.getAge());
            ps.setString(4, student.getAddress());
            ps.setString(5, student.getCourse());

            int rows = ps.executeUpdate();
            if (rows > 0) return student;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // update Student
    public Student updateStudent(int id, Student student){
        String sql = "UPDATE student SET name = ?, age = ?, address = ?, course =? WHERE Student_id = ?";
        int s_id = student.getStudent_id();

        Student s = getStudentById(s_id);

        if(s != null) {
            try (
                    Connection conn = dataSource.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)
            ) {
                ps.setString(1, student.getName());
                ps.setInt(2, student.getAge());
                ps.setString(3, student.getAddress());
                ps.setString(4, student.getCourse());
                ps.setInt(5, student.getStudent_id());

                int rows = ps.executeUpdate();
                if (rows > 0) return student;

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // get ALl Student
    public List<Student> getAllStudents(){
        List<Student>  result= new ArrayList<>();
        String sql = "SELECT * FROM student";
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

    // delete student
    public String deleteStudent(int id) {
        String sql = "DELETE FROM student WHERE student_id = ?";

        Student s = getStudentById(id);
        if (s != null) {
            try (
                    Connection conn = dataSource.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)
            ) {
                ps.setInt(1, id);

                int rows = ps.executeUpdate();
                if (rows > 0) return "Student Deleted successfully";
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return "Invalid Student ID";
    }
}