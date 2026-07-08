package com._2jwtauth.repository;

import com._2jwtauth.model.Permission;
import com._2jwtauth.model.Role;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class RoleRepository {

    private final DataSource dataSource;

    public RoleRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Fetch all roles (with their permissions) for a given user
    public List<Role> findRolesByUserId(Long userId) {
        //  Uses a LEFT JOIN so roles with zero permissions are still returned.
        String sql = """
            SELECT r.id AS role_id, r.name AS role_name,
                   p.id AS perm_id, p.name AS perm_name
            FROM role r
            JOIN user_roles ur ON r.id = ur.role_id
            LEFT JOIN role_permissions rp ON r.id = rp.role_id
            LEFT JOIN permission p ON rp.permission_id = p.id
            WHERE ur.user_id = ?
            """;

        // LinkedHashMap preserves insertion order & deduplicates roles by id
        Map<Long, Role> roleMap = new LinkedHashMap<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Long roleId = rs.getLong("role_id");
                String roleName = rs.getString("role_name");

                // Get the existing Role, or create it if this is the first time we see it
                Role role = roleMap.get(roleId);
                if (role == null) {
                    role = new Role(roleId, roleName);
                    roleMap.put(roleId, role);
                }

                Long permId = rs.getLong("perm_id");
                if (permId > 0) {
                    String permName = rs.getString("perm_name");
                    role.getPermissions().add(new Permission(permId, permName));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<>(roleMap.values());
    }

     // Assign a role to a user (e.g., default ROLE_USER on registration).
    public void assignRoleToUser(Long userId, Long roleId) {
        String sql = "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, roleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

     // Remove a role from a user.
    public void removeRoleFromUser(Long userId, Long roleId) {
        String sql = "DELETE FROM user_roles WHERE user_id = ? AND role_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, roleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get all available roles (for admin management UI).
    public List<Role> findAllRoles() {
        String sql = "SELECT * FROM role";
        List<Role> roles = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                roles.add(new Role(rs.getLong("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roles;
    }
}