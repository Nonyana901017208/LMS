package com.example.javafxgroupassignment;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

import java.sql.*;

public class ViewMyCoursesController {

    @FXML private TableView<Course> courseTable;
    @FXML private TableColumn<Course, String> nameColumn;
    @FXML private TableColumn<Course, String> descColumn;
    @FXML private Label courseCountLabel;
    @FXML private Button selectButton;

    private User instructor;
    private boolean selectionMode = false;

    public void setInstructor(User instructor) {
        this.instructor = instructor;
        if (instructor != null) {
            loadAssignedCourses();  // Now safe to call only after instructor is set
        }
    }

    public void setSelectionMode(boolean selectionMode) {
        this.selectionMode = selectionMode;
        if (selectButton != null) {
            selectButton.setVisible(selectionMode);
        }
    }

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(data -> data.getValue().courseNameProperty());
        descColumn.setCellValueFactory(data -> data.getValue().descriptionProperty());

        // Hide the select button by default; will show if selectionMode is enabled later
        if (selectButton != null) {
            selectButton.setVisible(false);
        }
    }

    private void loadAssignedCourses() {
        ObservableList<Course> courseList = FXCollections.observableArrayList();

        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
                SELECT c.id, c.course_name, c.description
                FROM instructor_assignments ia
                JOIN courses c ON ia.course_id = c.id
                WHERE ia.instructor_id = ?
                ORDER BY c.course_name
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, instructor.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                courseList.add(new Course(
                        rs.getInt("id"),
                        rs.getString("course_name"),
                        rs.getString("description")
                ));
            }

            courseTable.setItems(courseList);
            courseCountLabel.setText("Total Courses: " + courseList.size());

        } catch (SQLException e) {

            showAlert("Database Error", "Failed to load courses: " + e.getMessage());
        }
    }

    @FXML
    private void handleSelectCourse() {
        Course selectedCourse = courseTable.getSelectionModel().getSelectedItem();
        if (selectedCourse != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/javafxgroupassignment/viewcoursestudents.fxml"));
                Parent root = loader.load();

                ViewCourseStudentsController controller = loader.getController();
                controller.setCourseAndInstructor(selectedCourse, instructor);

                Stage stage = (Stage) selectButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Students in " + selectedCourse.getCourseName());
                stage.show();
            } catch (Exception e) {
                showAlert("Navigation Error", "Failed to load students view: " + e.getMessage());
            }
        } else {
            showAlert("Selection Error", "Please select a course first");
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/javafxgroupassignment/instructordashboard.fxml"));
            Parent root = loader.load();

            InstructorDashboardController controller = loader.getController();
            controller.setInstructor(instructor);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Instructor Dashboard");
            stage.show();
        } catch (Exception e) {
            showAlert("Navigation Error", "Failed to return to dashboard: " + e.getMessage());
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
