package com.example.javafxgroupassignment;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EnrollStudentsController {

    @FXML private ComboBox<User> studentComboBox;
    @FXML private ComboBox<Course> courseComboBox;

    @FXML
    public void initialize() {
        loadUsersAndCourses();
    }

    private void loadUsersAndCourses() {
        studentComboBox.getItems().clear();
        courseComboBox.getItems().clear();

        try (Connection conn = DBConnection.getConnection()) {
            // Load students
            String studentSQL = "SELECT * FROM users WHERE role = 'Student'";
            Statement studentStmt = conn.createStatement();
            ResultSet rs = studentStmt.executeQuery(studentSQL);

            List<User> students = new ArrayList<>();
            while (rs.next()) {
                students.add(new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("username"),
                        rs.getString("role")
                ));
            }
            studentComboBox.getItems().addAll(students);

            // Load courses
            String courseSQL = "SELECT * FROM courses";
            Statement courseStmt = conn.createStatement();
            ResultSet crs = courseStmt.executeQuery(courseSQL);
            while (crs.next()) {
                courseComboBox.getItems().add(new Course(
                        crs.getInt("id"),
                        crs.getString("course_name"),
                        crs.getString("description")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error loading data: " + e.getMessage());
        }
    }

    @FXML
    private void enrollStudent() {
        User student = studentComboBox.getValue();
        Course course = courseComboBox.getValue();

        if (student == null || course == null) {
            showAlert(Alert.AlertType.ERROR, "Please select both a student and a course.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Check for existing enrollment
            String checkSql = "SELECT COUNT(*) FROM student_enrollments WHERE student_id = ? AND course_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, student.getId());
            checkStmt.setInt(2, course.getId());
            ResultSet rs = checkStmt.executeQuery();
            rs.next();

            if (rs.getInt(1) > 0) {
                showAlert(Alert.AlertType.WARNING, "This student is already enrolled in this course.");
                return;
            }

            // Create new enrollment
            String sql = "INSERT INTO student_enrollments (student_id, course_id) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, student.getId());
            stmt.setInt(2, course.getId());
            stmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Student enrolled successfully!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error enrolling student: " + e.getMessage());
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
            Stage stage = (Stage) studentComboBox.getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/javafxgroupassignment/admindashboard.fxml")));
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Dashboard");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error returning to dashboard: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.showAndWait();
    }
}