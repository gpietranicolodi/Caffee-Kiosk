package repo.repository;

import model.dto.UserSessionInfo;
import repo.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Repository for handling authentication-related database operations.
 */
public class AuthRepository {

    /**
     * Finds a user by their login name and retrieves their session information.
     *
     * @param username The login name to search for.
     * @return An Optional containing UserSessionInfo if found, otherwise empty.
     * @throws SQLException if a database access error occurs.
     */
    public Optional<UserSessionInfo> findUserByUsername(String username) throws SQLException {
        // A query usa LOWER() na coluna e o parâmetro também é convertido para minúsculas
        // para garantir uma correspondência insensível a maiúsculas e minúsculas.
        String sql = "SELECT id, first_name || ' ' || last_name AS name, password AS hashedPassword, is_manager FROM employees WHERE LOWER(login) = ?";

        try (Connection connection = DBConnection.dbConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username.toLowerCase());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String hashedPassword = rs.getString("hashedPassword");
                    boolean isManager = rs.getBoolean("is_manager");
                    return Optional.of(new UserSessionInfo(id, name, hashedPassword, isManager));
                }
            }
        }
        return Optional.empty();
    }
}