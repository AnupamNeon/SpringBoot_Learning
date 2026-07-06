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
    
    public EmployeeRepository(DataSource ds){
        this.dataSource = ds;
    }

    private Employee mapRow(ResultSet rs) throws SQLException {
        return new Employee(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("department"),
                rs.getBigDecimal("salary"),
                rs.getDate("joiningDate").toLocalDate()
        );
    }
    
    // get Employee by id
    public Employee getEmployeeById(Long id){
        String sql = "SELECT * FROM employee WHERE id = ?";
        try(Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setLong(1, id);

            ResultSet  rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    // add Employee
    public Employee addEmployee(Employee employee) {
        String sql = "INSERT INTO employee(id, name, departement, salary, joiningDate) VALUES (?, ?, ?, ?, ?)";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
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

    // update Employee
    public Employee updateEmployee(int id, Employee employee){
        String sql = "UPDATE employee SET name = ?, department = ?, salary = ?, joiningDate =? WHERE id = ?";
        Long e_id = employee.getId();

        Employee e = getEmployeeById(e_id);

        if(e != null) {
            try (
                    Connection conn = dataSource.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)
            ) {
                ps.setString(1, employee.getName());
                ps.setString(2, employee.getDepartment());
                ps.setBigDecimal(3, employee.getSalary());
                ps.setDate(4, java.sql.Date.valueOf(employee.getJoiningDate()));
                ps.setLong(5, employee.getId());

                int rows = ps.executeUpdate();
                if (rows > 0) return employee;

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    // get ALl employee
    public List<Employee> getAllEmployees(){
        List<Employee> result= new ArrayList<>();
        String sql = "SELECT * FROM employee";
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

    // delete employee
    public String deleteEmployee(Long id) {
        String sql = "DELETE FROM employee WHERE id = ?";

        Employee e = getEmployeeById(id);
        if (e != null) {
            try (
                    Connection conn = dataSource.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)
            ) {
                ps.setLong(1, id);

                int rows = ps.executeUpdate();
                if (rows > 0) return "Employee Deleted successfully";
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return "Invalid Employee ID";
    }
}