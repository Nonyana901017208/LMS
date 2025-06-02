package com.example.javafxgroupassignment;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ViewCourseStudentsController {

    @FXML private Label courseTitleLabel;
    @FXML private TableView<User> studentsTable;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> usernameColumn;

    private Course selectedCourse;
    private User instructor;

    /**
     * This method is called automatically after the FXML has been loaded.
     */
    @FXML
    public void initialize() {
        // Initialize table columns (bind table columns to User properties)
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
    }

    /**
     * Set the selected course and instructor, then load students for the course.
     */
    public void setCourseAndInstructor(Course course, User instructor) {
        this.selectedCourse = course;
        this.instructor = instructor;

        if (selectedCourse != null) {
            courseTitleLabel.setText("Students in " + selectedCourse.getCourseName());
            loadStudents();
        } else {
            showAlert("Course information is missing. Cannot load students.");
        }
    }

    /**
     * Load the list of students enrolled in the selected course.
     */
    private void loadStudents() {
        if (selectedCourse == null) {
            showAlert("Course is not set. Cannot load students.");
            return;
        }

        ObservableList<User> studentList = FXCollections.observableArrayList();

        String query = """
            SELECT u.id, u.name, u.email, u.username
            FROM student_enrollments se
            JOIN users u ON se.student_id = u.id
            WHERE se.course_id = ? AND u.role = 'Student'
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, selectedCourse.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                User student = new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("username"),
                        "Student"
                );
                studentList.add(student);
            }

            studentsTable.setItems(studentList);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Failed to load students. Please try again later.");
        }
    }

    /**
     * Navigate back to the instructor's course view.
     */
    @FXML
    private void handleBack(ActionEvent event) {
        if (instructor == null) {
            showAlert("Instructor information is missing. Cannot go back.");
            return;
        }

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
            e.printStackTrace();
            showAlert("Failed to navigate back to course view.");
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

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
