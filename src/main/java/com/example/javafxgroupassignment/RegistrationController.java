package com.example.javafxgroupassignment;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class RegistrationController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;

    @FXML
    public void initialize() {
        // Ensure ComboBox items match the exact case of the enum in the database
        roleComboBox.getItems().addAll("Admin", "Instructor", "Student");
    }

    @FXML
    private void handleRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleComboBox.getValue();

        if (name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || role == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill all fields.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Make sure the role matches the enum case in the database (Admin, Instructor, Student)
            String formattedRole = role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase();  // Ensure correct case

            String sql = "INSERT INTO users (name, email, username, password, role) VALUES (?, ?, ?, ?, ?::user_role)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, username);
            stmt.setString(4, password);  // You should hash the password in real apps
            stmt.setString(5, formattedRole);  // Pass the correctly formatted role

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "User registered successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Failed", "Registration failed.");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "User already exists or database error.");
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
