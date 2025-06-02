package com.example.javafxgroupassignment;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.net.URL;
import java.sql.*;
import java.util.Objects;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML private PieChart userRolePieChart;
    @FXML private BarChart<String, Number> courseStudentBarChart;
    @FXML private CategoryAxis xAxisCourse;
    @FXML private NumberAxis yAxisCourse;
    @FXML private BarChart<String, Number> courseBarChart;
    @FXML private CategoryAxis xAxisDummy;
    @FXML private NumberAxis yAxisDummy;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadUserRolePieChart();
        loadCourseStudentBarChart();

    }

    private void loadUserRolePieChart() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT role, COUNT(*) as count FROM users GROUP BY role");
            while (rs.next()) {
                String role = rs.getString("role");
                int count = rs.getInt("count");
                userRolePieChart.getData().add(new PieChart.Data(role, count));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCourseStudentBarChart() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery(
                    "SELECT c.course_name, COUNT(se.student_id) AS student_count " +
                            "FROM courses c " +
                            "LEFT JOIN student_enrollments se ON c.id = se.course_id " +
                            "GROUP BY c.course_name"
            );

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Enrollments");

            while (rs.next()) {
                String course = rs.getString("course_name");
                int count = rs.getInt("student_count");
                series.getData().add(new XYChart.Data<>(course, count));
            }

            courseStudentBarChart.getData().add(series);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClose(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        // Implement your delete logic here — for now, we'll show a simple alert
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Delete");
        alert.setHeaderText(null);
        alert.setContentText("Delete action triggered.");
        alert.showAndWait();
    }

    @FXML
    private void handleAbout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("About This System");
        alert.setContentText("Admin Management Dashboard\nVersion 1.0\nDeveloped by:\n\n" +
                "Nonyana Mohale" +
                "\nLefa Rapopo" +
                "\nSandile Nhlapo.");
        alert.showAndWait();
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
}
