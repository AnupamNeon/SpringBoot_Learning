package com._2jwtauth.repository;

import com._2jwtauth.model.Employee;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class EmployeeRepository {

    private final DataSource dataSource;

    public EmployeeRepository(DataSource ds) {
        this.dataSource = ds;
    }

    // Maps a ResultSet row to an Employee object
    private Employee mapRow(ResultSet rs) throws SQLException {
        return new Employee(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("department"),
                rs.getBigDecimal("salary"),
                rs.getDate("joiningDate").toLocalDate()
        );
    }

    public Employee getEmployeeById(Long id) {
        String sql = "SELECT * FROM employee WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Employee addEmployee(Employee employee) {
        String sql = "INSERT INTO employee(id, name, department, salary, joiningDate) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, employee.getId());
            ps.setString(2, employee.getName());
            ps.setString(3, employee.getDepartment());
            ps.setBigDecimal(4, employee.getSalary());
            ps.setDate(5, java.sql.Date.valueOf(employee.getJoiningDate()));
            int rows = ps.executeUpdate();
            if (rows > 0) return employee;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ← Changed parameter type from int to Long — consistent with the rest of the codebase
    public Employee updateEmployee(Long id, Employee employee) {
        String sql = "UPDATE employee SET name = ?, department = ?, salary = ?, joiningDate = ? WHERE id = ?";

        // Guard: make sure the employee exists before updating
        if (getEmployeeById(id) == null) return null;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, employee.getName());
            ps.setString(2, employee.getDepartment());
            ps.setBigDecimal(3, employee.getSalary());
            ps.setDate(4, java.sql.Date.valueOf(employee.getJoiningDate()));
            ps.setLong(5, id);
            int rows = ps.executeUpdate();
            if (rows > 0) return employee;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Employee> getAllEmployees() {
        List<Employee> result = new ArrayList<>();
        String sql = "SELECT * FROM employee";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String deleteEmployee(Long id) {
        String sql = "DELETE FROM employee WHERE id = ?";
        if (getEmployeeById(id) == null) return "Invalid Employee ID";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) return "Employee Deleted successfully";
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Invalid Employee ID";
    }
}