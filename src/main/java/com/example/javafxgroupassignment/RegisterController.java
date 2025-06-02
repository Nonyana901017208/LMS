package com.example.javafxgroupassignment;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public class RegisterController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private Hyperlink loginLink;
    @FXML
    private Hyperlink homeLink;
    @FXML private ProgressBar reg_process;

    @FXML
    private void goToLogin() {
        try {
            Stage stage = (Stage) loginLink.getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/javafxgroupassignment/login.fxml")));
            stage.setScene(new Scene(root));
            stage.setTitle("User Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Failed to load Login screen.");
        }
    }

    @FXML
    private void goToHome() {
        try {
            Stage stage = (Stage) homeLink.getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/javafxgroupassignment/home.fxml")));
            stage.setScene(new Scene(root));
            stage.setTitle("Home");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Failed to load Home screen.");
        }
    }

    @FXML
    private void handleRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        String role = roleComboBox.getValue();

        if (name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || role == null) {
            showAlert(Alert.AlertType.ERROR, "All fields are required!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Passwords do not match!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Corrected SQL statement with explicit casting
            String sql = "INSERT INTO users (name, email, username, password, role) VALUES (?, ?, ?, ?, ?::user_role)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, username);
            stmt.setString(4, password);
            stmt.setString(5, role);

            int result = stmt.executeUpdate();
            if (result > 0) {
                showAlert(Alert.AlertType.INFORMATION, "User registered successfully!");
                switchToLogin();
            } else {
                showAlert(Alert.AlertType.ERROR, "Registration failed.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error: " + e.getMessage());
        }
    }

    private void switchToLogin() {
        try {
            Stage stage = (Stage) nameField.getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/javafxgroupassignment/login.fxml")));
            stage.setScene(new Scene(root));
            stage.setTitle("User Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Failed to open login screen.");
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        double progress = 0.0;

        // Step 1: Name
        if (!nameField.getText().isEmpty()) progress += 0.15;

        // Step 2: Email
        if (!emailField.getText().isEmpty() && emailField.getText().contains("@")) progress += 0.15;

        // Step 3: Username
        if (!usernameField.getText().isEmpty()) progress += 0.15;

        // Step 4: Passwords match and not empty
        String pass = passwordField.getText();
        String confirmPass = confirmPasswordField.getText();
        if (!pass.isEmpty() && pass.equals(confirmPass)) progress += 0.2;

        // Step 5: Role Selected
        if (roleComboBox.getValue() != null) progress += 0.15;

        // Optional: Final check if everything is filled correctly
        if (progress >= 0.8) {
            progress += 0.2; // Simulate completion
            // You can add actual registration logic here
            System.out.println("All fields valid. Registering...");
        }

        // Update the progress bar
        reg_process.setProgress(progress);

        // Show alert based on progress
        if (progress < 1.0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Incomplete Form");
            alert.setHeaderText(null);
            alert.setContentText("Please fill in all required fields correctly.");
            alert.show();
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registration");
            alert.setHeaderText(null);
            alert.setContentText("Registration successful!");
            alert.show();
        }
    }

    public void initialize() {

        roleComboBox.getItems().addAll("Admin", "Student", "Instructor");


        // Listeners for fields
        nameField.textProperty().addListener((obs, oldText, newText) -> updateProgress());
        emailField.textProperty().addListener((obs, oldText, newText) -> updateProgress());
        usernameField.textProperty().addListener((obs, oldText, newText) -> updateProgress());
        passwordField.textProperty().addListener((obs, oldText, newText) -> updateProgress());
        confirmPasswordField.textProperty().addListener((obs, oldText, newText) -> updateProgress());
        roleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateProgress());
    }

    private void updateProgress() {
        double progress = 0.0;
        if (!nameField.getText().isEmpty()) progress += 0.2;
        if (!emailField.getText().isEmpty() && emailField.getText().contains("@")) progress += 0.2;
        if (!usernameField.getText().isEmpty()) progress += 0.2;
        if (!passwordField.getText().isEmpty() && passwordField.getText().equals(confirmPasswordField.getText())) progress += 0.2;
        if (roleComboBox.getValue() != null) progress += 0.2;

        reg_process.setProgress(progress);
    }



    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.showAndWait();
    }
}