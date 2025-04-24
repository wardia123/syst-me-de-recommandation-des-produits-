import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.*;
import java.util.List;

public class HomeFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private JPanel productsPanel;
    private User user;
    private JTextField searchField;

    public HomeFrame(User user) {
        this.user = user;
        setTitle("Accueil - Recommandations Produits");
        setSize(1000, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        // Panel recherche
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchPanel.setBackground(new Color(52, 73, 94));
        searchField = new JTextField(25);
        JButton searchButton = new JButton("Rechercher");
        stylizeButton(searchButton);

        searchButton.addActionListener(this::performSearch);
        JLabel searchLabel = new JLabel("Rechercher :");
        searchLabel.setForeground(Color.WHITE);
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        add(searchPanel, BorderLayout.NORTH);

        // Panel Produits
        productsPanel = new JPanel();
        productsPanel.setLayout(new BoxLayout(productsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(productsPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Menu
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");

        JMenuItem historyLikes = new JMenuItem("Historique Likes");
        JMenuItem historyPurchases = new JMenuItem("Historique Achats");
        JMenuItem logout = new JMenuItem("Déconnexion");

        historyLikes.addActionListener(this::showLikeHistory);
        historyPurchases.addActionListener(this::showPurchaseHistory);
        logout.addActionListener(_ -> {
            dispose();
            new LoginFrame();
        });

        menu.add(historyLikes);
        menu.add(historyPurchases);
        menu.add(logout);
        menuBar.add(menu);
        setJMenuBar(menuBar);

        displayRecommendedProducts();
        setVisible(true);
    }

    private void stylizeButton(JButton button) {
        button.setBackground(new Color(41, 128, 185));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
    }

    private void performSearch(ActionEvent e) {
        String searchTerm = searchField.getText().trim().toLowerCase();
        if (searchTerm.isEmpty()) {
            displayRecommendedProducts();
        } else {
            displaySearchResults(searchTerm);
        }
    }

    private void displaySearchResults(String searchTerm) {
        productsPanel.removeAll();

        List<Product> allProducts = new ArrayList<>();
        allProducts.addAll(getLikedProducts(user));
        allProducts.addAll(getSimilarToLikedProducts(user));
        allProducts.addAll(getSimilarToPurchasedProducts(user));
        allProducts.addAll(getFriendsPurchasedProducts(user));
        allProducts.addAll(getTrendingProducts());
        allProducts.addAll(getRandomProducts());

        Set<Integer> seenIds = new HashSet<>();
        for (Product p : allProducts) {
            if (seenIds.add(p.getId()) && (p.getName().toLowerCase().contains(searchTerm) || p.getCategory().toLowerCase().contains(searchTerm))) {
                productsPanel.add(createProductPanel(p));
            }
        }

        productsPanel.revalidate();
        productsPanel.repaint();
    }

    private void showLikeHistory(ActionEvent e) {
        showHistory("Vos produits likés", getLikedProducts(user));
    }

    private void showPurchaseHistory(ActionEvent e) {
        showHistory("Vos produits achetés", getPurchasedProducts(user));
    }

    private void showHistory(String title, List<Product> products) {
        JFrame frame = new JFrame(title);
        frame.setSize(400, 400);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        for (Product p : products) {
            panel.add(new JLabel(p.getName() + " | " + p.getCategory() + " | " + p.getPrice() + "€"));
        }
        frame.add(new JScrollPane(panel));
        frame.setVisible(true);
    }

    private void displayRecommendedProducts() {
        productsPanel.removeAll();

        List<Product> allProducts = new ArrayList<>();
        allProducts.addAll(getLikedProducts(user));
        allProducts.addAll(getSimilarToLikedProducts(user));
        allProducts.addAll(getSimilarToPurchasedProducts(user));
        allProducts.addAll(getFriendsPurchasedProducts(user));
        allProducts.addAll(getTrendingProducts());
        allProducts.addAll(getRandomProducts());

        Set<Integer> seenIds = new HashSet<>();
        for (Product p : allProducts) {
            if (seenIds.add(p.getId())) {
                productsPanel.add(createProductPanel(p));
            }
        }

        productsPanel.revalidate();
        productsPanel.repaint();
    }

    private JPanel createProductPanel(Product p) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5),
            BorderFactory.createLineBorder(Color.GRAY)
        ));
        panel.setBackground(Color.WHITE);

        JLabel label = new JLabel(p.getName() + " | Catégorie: " + p.getCategory() + " | Prix: " + p.getPrice() + "€");
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(label, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        JButton likeBtn = new JButton("Like");
        JButton buyBtn = new JButton("Acheter");
        stylizeButton(likeBtn);
        stylizeButton(buyBtn);

        likeBtn.addActionListener(_ -> likeProduct(p));
        buyBtn.addActionListener(_ -> acheterProduit(p));

        buttonPanel.add(likeBtn);
        buttonPanel.add(buyBtn);

        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    private void likeProduct(Product p) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM user_likes WHERE user_id = ? AND product_id = ?");
            stmt.setInt(1, user.getId());
            stmt.setInt(2, p.getId());
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                stmt = conn.prepareStatement("INSERT INTO user_likes(user_id, product_id) VALUES (?, ?)");
                stmt.setInt(1, user.getId());
                stmt.setInt(2, p.getId());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        displayRecommendedProducts();
    }

    private void acheterProduit(Product p) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement checkStmt = conn.prepareStatement("SELECT * FROM user_purchases WHERE user_id = ? AND product_id = ?");
            checkStmt.setInt(1, user.getId());
            checkStmt.setInt(2, p.getId());
            ResultSet rs = checkStmt.executeQuery();
            if (!rs.next()) {
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO user_purchases(user_id, product_id, purchase_date) VALUES (?, ?, NOW())");
                stmt.setInt(1, user.getId());
                stmt.setInt(2, p.getId());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        displayRecommendedProducts();
    }

    // Méthodes de récupération de produits
    private List<Product> getLikedProducts(User user) {
        return getProductsFromQuery("SELECT p.id, p.name, p.category, p.price FROM products p JOIN user_likes ul ON p.id = ul.product_id WHERE ul.user_id = ?", user.getId());
    }

    private List<Product> getPurchasedProducts(User user) {
        return getProductsFromQuery("SELECT p.id, p.name, p.category, p.price FROM products p JOIN user_purchases up ON p.id = up.product_id WHERE up.user_id = ?", user.getId());
    }

    private List<Product> getSimilarToLikedProducts(User user) {
        return getProductsFromQuery("SELECT DISTINCT p2.id, p2.name, p2.category, p2.price FROM products p1 JOIN user_likes ul ON p1.id = ul.product_id JOIN products p2 ON p1.category = p2.category WHERE ul.user_id = ?", user.getId());
    }

    private List<Product> getSimilarToPurchasedProducts(User user) {
        return getProductsFromQuery("SELECT DISTINCT p2.id, p2.name, p2.category, p2.price FROM products p1 JOIN user_purchases up ON p1.id = up.product_id JOIN products p2 ON p1.category = p2.category WHERE up.user_id = ?", user.getId());
    }

    private List<Product> getFriendsPurchasedProducts(User user) {
        return getProductsFromQuery("SELECT DISTINCT p.id, p.name, p.category, p.price FROM products p JOIN user_purchases up ON p.id = up.product_id WHERE up.user_id != ?", user.getId());
    }

    private List<Product> getTrendingProducts() {
        return getProductsFromQuery("SELECT p.id, p.name, p.category, p.price FROM products p JOIN user_purchases up ON p.id = up.product_id GROUP BY p.id ORDER BY COUNT(up.id) DESC LIMIT 5", -1);
    }

    private List<Product> getRandomProducts() {
        return getProductsFromQuery("SELECT id, name, category, price FROM products ORDER BY RAND() LIMIT 5", -1);
    }

    private List<Product> getProductsFromQuery(String query, int userId) {
        List<Product> products = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(query);
            if (userId != -1) stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Product product = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price")
                );
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }
}
