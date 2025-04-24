package project;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class RegisterFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField ageField;

    public RegisterFrame() {
        setTitle("Inscription");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(240, 248, 255));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel titleLabel = new JLabel("Créer un compte");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 144, 255));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 0, 20, 0);
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 10, 5, 10);

        JLabel usernameLabel = new JLabel("Nom d'utilisateur:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(usernameLabel, gbc);

        usernameField = new JTextField(15);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        panel.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Mot de passe:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        panel.add(passwordField, gbc);

        JLabel ageLabel = new JLabel("Âge:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(ageLabel, gbc);

        ageField = new JTextField(15);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        panel.add(ageField, gbc);

        JButton registerButton = new JButton("S'inscrire");
        registerButton.setBackground(new Color(30, 144, 255));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setFont(new Font("Tahoma", Font.BOLD, 12));

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 0, 5, 0);
        panel.add(registerButton, gbc);

        JButton backButton = new JButton("Retour");
        backButton.setBackground(Color.LIGHT_GRAY);
        backButton.setFocusPainted(false);

        gbc.gridy = 5;
        panel.add(backButton, gbc);

        add(panel);

        // Action bouton s'inscrire
        registerButton.addActionListener(_ -> register());

        // Action bouton retour
        backButton.addActionListener(_ -> {
            dispose();
            new LoginFrame(); // Retour à la connexion
        });

        setVisible(true);
    }

    private void register() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String ageText = ageField.getText();

        if (username.isEmpty() || password.isEmpty() || ageText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs.", "Erreur", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int age = Integer.parseInt(ageText);

            try (Connection conn = DBConnection.getConnection()) {
                // Vérifie si l'utilisateur existe déjà
                String checkQuery = "SELECT * FROM users WHERE username = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Ce nom d'utilisateur existe déjà.", "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Insère le nouvel utilisateur
                String insertQuery = "INSERT INTO users (username, password, age) VALUES (?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);
                insertStmt.setInt(3, age);
                insertStmt.executeUpdate();

                ResultSet keys = insertStmt.getGeneratedKeys();
                if (keys.next()) {
                    int userId = keys.getInt(1);
                    User user = new User(userId, username, password, age);
                    JOptionPane.showMessageDialog(this, "Inscription réussie !");
                    dispose();
                    new HomeFrame(user); // Redirige vers la page d'accueil
                }

            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "L'âge doit être un nombre entier.", "Erreur", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de l'inscription à la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
