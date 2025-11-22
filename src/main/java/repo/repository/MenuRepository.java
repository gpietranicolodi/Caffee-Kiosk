package repo.repository;

import model.entity.Item;
import repo.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for handling menu item-related database operations.
 */
public class MenuRepository {

    /**
     * Finds all available items for a given category.
     *
     * @param categoryId The ID of the category.
     * @return A list of Items in that category.
     * @throws SQLException if a database access error occurs.
     */
    public List<Item> findItemsByCategoryId(int categoryId) throws SQLException {
        String sql = "SELECT id, name, price, inventory FROM items WHERE category_id = ? AND is_available = TRUE ORDER BY name";
        List<Item> items = new ArrayList<>();

        try (Connection connection = DBConnection.dbConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setInt(1, categoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRowToItem(rs));
                }
            }
        }
        return items;
    }

    /**
     * Finds a single item by its unique ID.
     *
     * @param itemId The ID of the item.
     * @return An Optional containing the Item if found, otherwise empty.
     * @throws SQLException if a database access error occurs.
     */
    public Optional<Item> findItemById(int itemId) throws SQLException {
        String sql = "SELECT id, name, price, inventory FROM items WHERE id = ?";
        try (Connection conn = DBConnection.dbConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToItem(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Updates the inventory for a given item.
     *
     * @param itemId The ID of the item to update.
     * @param quantity The quantity to subtract from the inventory.
     * @throws SQLException if a database access error occurs.
     */
    public void updateInventory(int itemId, int quantity) throws SQLException {
        // The SQL query now includes a check to ensure inventory is sufficient BEFORE updating.
        // This makes the operation atomic and safe from race conditions.
        String sql = "UPDATE items SET inventory = inventory - ? WHERE id = ? AND inventory >= ?";
        try (Connection conn = DBConnection.dbConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, itemId);
            pstmt.setInt(3, quantity);
            int affectedRows = pstmt.executeUpdate();

            // If no rows were affected, it means the condition (inventory >= ?) was not met.
            if (affectedRows == 0) {
                // This will be caught by the CheckoutHandler and displayed to the user.
                throw new SQLException("Insufficient stock for the selected item. The order could not be completed.");
            }
        }
    }

    /**
     * Helper method to map a ResultSet row to an Item object.
     */
    private Item mapRowToItem(ResultSet rs) throws SQLException {
        return new Item(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("price"),
                rs.getInt("inventory")
        );
    }
}