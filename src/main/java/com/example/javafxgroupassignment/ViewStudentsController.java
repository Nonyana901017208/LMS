package com.example.javafxgroupassignment;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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

public class ViewStudentsController {

    @FXML private TableView<String[]> studentTable;
    @FXML private TableColumn<String[], String> nameCol;
    @FXML private TableColumn<String[], String> courseCol;
    @FXML private TextField searchField;
    @FXML private Label countLabel;

    private User instructor;
    private FilteredList<String[]> filteredStudents;

    public void setInstructor(User instructor) {
        this.instructor = instructor;
    }

    @FXML
    public void initialize() {
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[0]));
        courseCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[1]));

        loadStudents();
    }

    private void loadStudents() {
        ObservableList<String[]> data = FXCollections.observableArrayList();

        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
                     SELECT u.name, c.course_name
                     FROM student_enrollments se
                     JOIN users u ON se.student_id = u.id
                     JOIN courses c ON se.course_id = c.id
                     """;

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                data.add(new String[]{
                        rs.getString("name"),
                        rs.getString("course_name")
                });
            }

            filteredStudents = new FilteredList<>(data, p -> true);
            studentTable.setItems(filteredStudents);

            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                filteredStudents.setPredicate(row -> {
                    String combined = (row[0] + row[1]).toLowerCase();
                    return combined.contains(newVal.toLowerCase());
                });
            });

            countLabel.setText("Total Students: " + data.size());

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(String.valueOf(Alert.AlertType.ERROR), "Error loading student enrollments:\n" + e.getMessage());
        }
    }

    @FXML
    private void exportPDF() {
        // Implement PDF export similar to ViewEnrollmentsController
    }

    @FXML
    private void handleBack() {
        try {
            Stage stage = (Stage) studentTable.getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/javafxgroupassignment/viewenrollments.fxml")));
            stage.setScene(new Scene(root));
            stage.setTitle("View Enrollments");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(String.valueOf(Alert.AlertType.ERROR), "Error returning to enrollments: " + e.getMessage());
        }
    }


    @FXML
    private void handleViewStudents(ActionEvent event) {
        try {
            // First navigate to course selection
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/javafxgroupassignment/viewmycourses.fxml"));
            Parent root = loader.load();

            ViewMyCoursesController controller = loader.getController();
            controller.setInstructor(instructor);
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
    private void handleViewCourses(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/javafxgroupassignment/viewmycourses.fxml"));
            Parent root = loader.load();

            ViewMyCoursesController controller = loader.getController();
            controller.setInstructor(instructor);

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
            controller.setInstructor(instructor);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Upload Course Materials");
            stage.show();
        } catch (Exception e) {
            showAlert("Navigation Error", "Failed to load materials upload: " + e.getMessage());
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

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}