package project;

import java.sql.*;
import java.util.*;

public class RecommendationEngine {

    public static List<Integer> getRecommendedProductIds(int userId, int age, Connection conn) throws SQLException {
        Set<Integer> recommended = new LinkedHashSet<>();

        // 1. Produits populaires (achetés par le plus d'utilisateurs)
        String sqlTrend = "SELECT product_id FROM user_purchases GROUP BY product_id ORDER BY COUNT(*) DESC LIMIT 5";
        ResultSet rs = conn.createStatement().executeQuery(sqlTrend);
        while (rs.next()) recommended.add(rs.getInt("product_id"));

        // 2. Produits achetés par des gens du même âge
        String sqlAge = "SELECT up.product_id FROM users u JOIN user_purchases up ON u.id = up.user_id WHERE u.age = ?";
        PreparedStatement stmtAge = conn.prepareStatement(sqlAge);
        stmtAge.setInt(1, age);
        rs = stmtAge.executeQuery();
        while (rs.next()) recommended.add(rs.getInt("product_id"));

        // 3. Produits achetés par les amis
        String sqlFriends = "SELECT up.product_id FROM friends f JOIN user_purchases up ON f.friend_id = up.user_id WHERE f.user_id = ?";
        PreparedStatement stmtFriends = conn.prepareStatement(sqlFriends);
        stmtFriends.setInt(1, userId);
        rs = stmtFriends.executeQuery();
        while (rs.next()) recommended.add(rs.getInt("product_id"));

        // 4. Produits jamais achetés (pour nouvel utilisateur)
        String sqlAll = "SELECT id FROM products WHERE id NOT IN (SELECT product_id FROM user_purchases WHERE user_id = ?) LIMIT 5";
        PreparedStatement stmtNew = conn.prepareStatement(sqlAll);
        stmtNew.setInt(1, userId);
        rs = stmtNew.executeQuery();
        while (rs.next()) recommended.add(rs.getInt("id"));

        return new ArrayList<>(recommended);
    }
}
