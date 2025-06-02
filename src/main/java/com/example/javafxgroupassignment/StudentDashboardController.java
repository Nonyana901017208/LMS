package com.example.javafxgroupassignment;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.event.ActionEvent;

import java.util.Objects;

public class StudentDashboardController {

    @FXML
    private Label welcomeLabel;

    private User currentStudent;

    public void setStudent(User student) {
        this.currentStudent = student;
        updateWelcomeMessage();
    }

    private void updateWelcomeMessage() {
        if (currentStudent != null && welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + currentStudent.getName() +
                    " (" + currentStudent.getRole() + ")");
        }
    }

    @FXML
    public void initialize() {

        updateWelcomeMessage();

    }

    @FXML
    private void handleViewCourses(ActionEvent event) {
        loadSceneWithController("/com/example/javafxgroupassignment/studentcourses.fxml", event, "My Enrolled Courses", controller -> {
            ((StudentCoursesController) controller).setStudent(currentStudent);
        });
    }

    @FXML
    private void handleViewMaterials(ActionEvent event) {
        loadSceneWithController("/com/example/javafxgroupassignment/StudentMaterials.fxml", event, "Course Materials", controller -> {
            ((StudentMaterialsController) controller).setStudent(currentStudent);
        });
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/javafxgroupassignment/login.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (Exception e) {
            showAlert("Logout Failed", "Unable to load login screen: " + e.getMessage());
        }
    }

    private void loadSceneWithController(String fxmlPath, ActionEvent event, String title, ControllerInitializer initializer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (initializer != null) {
                initializer.init(controller);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            showAlert("Navigation Error", "Failed to load scene: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FunctionalInterface
    interface ControllerInitializer {
        void init(Object controller);
    }
}
