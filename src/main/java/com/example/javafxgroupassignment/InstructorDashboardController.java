package com.example.javafxgroupassignment;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class InstructorDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private BarChart<String, Number> instructor_chart;

    private User currentInstructor;

    public void setInstructor(User instructor) {
        this.currentInstructor = instructor;
        updateWelcomeMessage();
        populateChart();
    }

    private void updateWelcomeMessage() {
        if (currentInstructor != null && welcomeLabel != null) {
            welcomeLabel.setText("Welcome,\n " + currentInstructor.getName() +
                    " (" + currentInstructor.getRole() + ")");
        }
    }

    @FXML
    public void initialize() {
        updateWelcomeMessage();
    }

    private void populateChart() {
        if (currentInstructor == null || instructor_chart == null) return;

        ObservableList<XYChart.Data<String, Number>> dataList = FXCollections.observableArrayList();

        String query =
                "SELECT c.course_name, COUNT(e.student_id) AS enrolled_count " +
                        "FROM courses c " +
                        "JOIN instructor_assignments ia ON c.id = ia.course_id " +
                        "LEFT JOIN student_enrollments e ON c.id = e.course_id " +
                        "WHERE ia.instructor_id = ? " +
                        "GROUP BY c.id, c.course_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentInstructor.getId()); // Assuming `User` has getId()

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String courseName = rs.getString("course_name");
                int studentCount = rs.getInt("enrolled_count");
                dataList.add(new XYChart.Data<>(courseName, studentCount));
            }

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Students Enrolled");
            series.setData(dataList);

            instructor_chart.getData().clear();
            instructor_chart.getData().add(series);

        } catch (Exception e) {
            showAlert("Database Error", "Failed to load course data: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    private void handleViewCourses(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/javafxgroupassignment/viewmycourses.fxml"));
            Parent root = loader.load();

            ViewMyCoursesController controller = loader.getController();
            controller.setInstructor(currentInstructor);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("My Courses");
            stage.show();
        } catch (Exception e) {
            showAlert("Navigation Error", "Failed to load courses view: " + e.getMessage());
        }
    }

    @FXML
    private void handleUploadMaterials(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/javafxgroupassignment/uploadmaterials.fxml"));
            Parent root = loader.load();

            UploadMaterialsController controller = loader.getController();
            controller.setInstructor(currentInstructor);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Upload Course Materials");
            stage.show();
        } catch (Exception e) {
            showAlert("Navigation Error", "Failed to load materials upload: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewStudents(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/javafxgroupassignment/viewmycourses.fxml"));
            Parent root = loader.load();

            ViewMyCoursesController controller = loader.getController();
            controller.setInstructor(currentInstructor);
            controller.setSelectionMode(true);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Select Course to View Students");
            stage.show();
        } catch (Exception e) {
            showAlert("Navigation Error", "Failed to load course selection: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/javafxgroupassignment/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (Exception e) {
            showAlert("Logout Error", "Failed to logout: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
