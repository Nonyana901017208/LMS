package com.example.javafxgroupassignment;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.Objects;

public class ManageUsersController {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> roleColumn;

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;

    private ObservableList<User> userList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("Admin", "Instructor", "Student");

        idColumn.setCellValueFactory(data -> data.getValue().idProperty().asObject());
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        emailColumn.setCellValueFactory(data -> data.getValue().emailProperty());
        usernameColumn.setCellValueFactory(data -> data.getValue().usernameProperty());
        roleColumn.setCellValueFactory(data -> data.getValue().roleProperty());

        loadUsers();

        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                nameField.setText(newSel.getName());
                emailField.setText(newSel.getEmail());
                usernameField.setText(newSel.getUsername());
                passwordField.setText(""); // Never load real passwords!
                roleComboBox.setValue(newSel.getRole());
            }
        });
    }

    private void loadUsers() {
        userList.clear();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM users";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                userList.add(new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("username"),
                        rs.getString("role")
                ));
            }
            System.out.println(userList);
            userTable.setItems(userList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error loading users: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddUser() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO users (name, email, username, password, role) " +
                    "VALUES (?, ?, ?, ?, ?::user_role)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nameField.getText().trim());
            stmt.setString(2, emailField.getText().trim());
            stmt.setString(3, usernameField.getText().trim());
            stmt.setString(4, passwordField.getText().trim());
            stmt.setString(5, roleComboBox.getValue());  // Make sure the ComboBox contains values like "Admin", "Instructor", "Student"

            if (stmt.executeUpdate() > 0) {
                showAlert(Alert.AlertType.INFORMATION, "User added successfully!");
                loadUsers();
                clearFields();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error adding user: " + e.getMessage());
        }
    }


    @FXML
    private void handleUpdateUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.ERROR, "No user selected for update.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE users SET name=?, email=?, username=?, password=?, role=? WHERE id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nameField.getText().trim());
            stmt.setString(2, emailField.getText().trim());
            stmt.setString(3, usernameField.getText().trim());
            stmt.setString(4, passwordField.getText().trim());
            stmt.setString(5, roleComboBox.getValue());
            stmt.setInt(6, selected.getId());

            if (stmt.executeUpdate() > 0) {
                showAlert(Alert.AlertType.INFORMATION, "User updated successfully!");
                loadUsers();
                clearFields();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error updating user: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.ERROR, "No user selected for deletion.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "DELETE FROM users WHERE id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, selected.getId());

            if (stmt.executeUpdate() > 0) {
                showAlert(Alert.AlertType.INFORMATION, "User deleted successfully!");
                loadUsers();
                clearFields();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error deleting user: " + e.getMessage());
        }
    }

    @FXML
    private void handleAssignInstructors(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/javafxgroupassignment/assigncourses.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Assign Instructors to Courses");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleEnrollStudents(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/javafxgroupassignment/enrollstudents.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Enroll Students");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleManageUsers(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/javafxgroupassignment/manageusers.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Manage Users");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleManageCourses(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/javafxgroupassignment/managecourses.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Manage Courses");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewEnrollments(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/javafxgroupassignment/viewenrollments.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("View Enrollments");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/javafxgroupassignment/login.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("User Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            Stage stage = (Stage) nameField.getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/javafxgroupassignment/admindashboard.fxml")));
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Dashboard");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        nameField.clear();
        emailField.clear();
        usernameField.clear();
        passwordField.clear();
        roleComboBox.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
