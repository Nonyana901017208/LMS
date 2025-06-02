package com.example.javafxgroupassignment;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Hyperlink registerLink;
    @FXML private Hyperlink homeLink;

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("Admin", "Instructor", "Student");
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleComboBox.getValue();

        if (email.isEmpty() || username.isEmpty() || password.isEmpty() || role == null) {
            showAlert(Alert.AlertType.ERROR, "All fields are required!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Corrected SQL statement with explicit casting for role
            String sql = "SELECT * FROM users WHERE email = ? AND username = ? AND password = ? AND role = ?::user_role";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, username);
            stmt.setString(3, password);
            stmt.setString(4, role);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Construct logged-in user
                User loggedInUser = new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("username"),
                        rs.getString("role")
                );

                loadDashboard(loggedInUser);
            } else {
                showAlert(Alert.AlertType.ERROR, "Invalid credentials or role.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error: " + e.getMessage());
        }
    }

    private void loadDashboard(User user) {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            Parent root = null;

            switch (user.getRole()) {
                case "Admin":
                    root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/javafxgroupassignment/admindashboard.fxml")));
                    break;

                case "Instructor":
                    FXMLLoader loaderInstructor = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/com/example/javafxgroupassignment/instructordashboard.fxml")));
                    root = loaderInstructor.load();

                    InstructorDashboardController instructorController = loaderInstructor.getController();
                    instructorController.setInstructor(user);
                    break;

                case "Student":
                    FXMLLoader loaderStudent = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/com/example/javafxgroupassignment/studentdashboard.fxml")));
                    root = loaderStudent.load();

                    StudentDashboardController studentDashboardController = loaderStudent.getController();
                    studentDashboardController.setStudent(user);
                    // You can also pass student user object if needed in StudentDashboardController
                    break;

                default:
                    showAlert(Alert.AlertType.ERROR, "Unrecognized role: " + user.getRole());
                    return;
            }

            stage.setScene(new Scene(root));
            stage.setTitle(user.getRole() + " Dashboard");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Failed to load dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void goToRegister() {
        try {
            Stage stage = (Stage) registerLink.getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/javafxgroupassignment/register.fxml")));
            stage.setScene(new Scene(root));
            stage.setTitle("User Registration");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Failed to load Registration screen.");
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

    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.showAndWait();
    }
}