package com._2jwtauth.repository;

import com._2jwtauth.model.User;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UserRepository {

    private final DataSource dataSource;

    public UserRepository(DataSource ds) {
        this.dataSource = ds;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setEnabled(rs.getBoolean("enabled"));
        return user;
    }

    // get user by id
    public User getUserById(Long id) {
        String sql = "SELECT * FROM user WHERE id = ?";
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

    // add user
    public User addUser(User user) {
        String sql = "INSERT INTO user(username, email, password, enabled) VALUES (?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setBoolean(4, user.isEnabled());

            int rows = ps.executeUpdate();
            if (rows > 0) return user;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // update user
    public User updateUser(Long id, User user) {
        String sql = "UPDATE user SET username = ?, email = ?, password = ?, enabled = ? WHERE id = ?";

        User existing = getUserById(id);

        if (existing != null) {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, user.getUsername());
                ps.setString(2, user.getEmail());
                ps.setString(3, user.getPassword());
                ps.setBoolean(4, user.isEnabled());
                ps.setLong(5, id);

                int rows = ps.executeUpdate();
                if (rows > 0) return user;

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // get all users
    public List<User> getAllUsers() {
        List<User> result = new ArrayList<>();
        String sql = "SELECT * FROM user";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    // delete user
    public String deleteUser(Long id) {
        String sql = "DELETE FROM user WHERE id = ?";

        User user = getUserById(id);
        if (user != null) {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setLong(1, id);

                int rows = ps.executeUpdate();
                if (rows > 0) return "User deleted successfully";

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return "Invalid User ID";
    }

    // find by username
    public User findByUsername(String username){
        String sql = "SELECT * FROM user WHERE username = ?";
        try(Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}