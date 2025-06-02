package com.example.javafxgroupassignment;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AssignCoursesController {

    @FXML private ComboBox<User> instructorComboBox;
    @FXML private ComboBox<User> studentComboBox;
    @FXML private ComboBox<Course> courseComboBox;

    @FXML
    public void initialize() {
        loadUsersAndCourses();
    }

    private void loadUsersAndCourses() {
        instructorComboBox.getItems().clear();
        studentComboBox.getItems().clear();
        courseComboBox.getItems().clear();

        try (Connection conn = DBConnection.getConnection()) {
            String userSQL = "SELECT * FROM users";
            Statement userStmt = conn.createStatement();
            ResultSet rs = userStmt.executeQuery(userSQL);

            List<User> instructors = new ArrayList<>();
            List<User> students = new ArrayList<>();

            while (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("username"),
                        rs.getString("role")
                );
                if ("Instructor".equalsIgnoreCase(user.getRole())) instructors.add(user);
                if ("Student".equalsIgnoreCase(user.getRole())) students.add(user);
            }


            instructorComboBox.getItems().addAll(instructors);
            studentComboBox.getItems().addAll(students);

            String courseSQL = "SELECT * FROM courses";
            Statement courseStmt = conn.createStatement();
            ResultSet crs = courseStmt.executeQuery(courseSQL);
            while (crs.next()) {
                courseComboBox.getItems().add(new Course(crs.getInt("id"), crs.getString("course_name"), crs.getString("description")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error loading users or courses: " + e.getMessage());
        }
    }

    @FXML
    private void assignInstructor() {
        User instructor = instructorComboBox.getValue();
        Course course = courseComboBox.getValue();

        if (instructor == null || course == null) {
            showAlert(Alert.AlertType.ERROR, "Please select both an instructor and a course.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String checkSql = "SELECT COUNT(*) FROM instructor_assignments WHERE instructor_id = ? AND course_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, instructor.getId());
            checkStmt.setInt(2, course.getId());
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                showAlert(Alert.AlertType.WARNING, "This instructor is already assigned to this course.");
                return;
            }

            String sql = "INSERT INTO instructor_assignments (instructor_id, course_id) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, instructor.getId());
            stmt.setInt(2, course.getId());
            stmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Instructor assigned successfully!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error assigning instructor: " + e.getMessage());
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
    private void handleBack() {
        try {
            Stage stage = (Stage) instructorComboBox.getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/javafxgroupassignment/admindashboard.fxml")));
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Dashboard");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
