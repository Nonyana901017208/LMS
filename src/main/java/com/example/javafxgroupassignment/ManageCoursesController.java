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

public class ManageCoursesController {

    @FXML private TableView<Course> courseTable;
    @FXML private TableColumn<Course, Integer> idColumn;
    @FXML private TableColumn<Course, String> nameColumn;
    @FXML private TableColumn<Course, String> descriptionColumn;

    @FXML private TextField courseNameField;
    @FXML private TextArea courseDescriptionField;
    @FXML private TextField searchField;

    private FilteredList<Course> filteredCourses;
    private ObservableList<Course> courseList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialize table columns
        idColumn.setCellValueFactory(data -> data.getValue().idProperty().asObject());
        nameColumn.setCellValueFactory(data -> data.getValue().courseNameProperty());
        descriptionColumn.setCellValueFactory(data -> data.getValue().descriptionProperty());

        loadCourses();

        // Set up search functionality
        filteredCourses = new FilteredList<>(courseList, p -> true);
        courseTable.setItems(filteredCourses);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredCourses.setPredicate(course -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String filter = newVal.toLowerCase();
                return course.getCourseName().toLowerCase().contains(filter)
                        || course.getDescription().toLowerCase().contains(filter);
            });
        });

        // Set up table selection listener
        courseTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                courseNameField.setText(newSel.getCourseName());
                courseDescriptionField.setText(newSel.getDescription());
            }
        });
    }

    private void loadCourses() {
        courseList.clear();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM courses";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                courseList.add(new Course(
                        rs.getInt("id"),
                        rs.getString("course_name"),
                        rs.getString("description")
                ));
            }
            courseTable.setItems(courseList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error loading courses: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddCourse() {
        if (courseNameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Course name cannot be empty!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO courses (course_name, description) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, courseNameField.getText().trim());
            stmt.setString(2, courseDescriptionField.getText().trim());

            if (stmt.executeUpdate() > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Course added successfully!");
                loadCourses();
                clearFields();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error adding course: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateCourse() {
        Course selected = courseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.ERROR, "No course selected for update.");
            return;
        }

        if (courseNameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Course name cannot be empty!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE courses SET course_name=?, description=? WHERE id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, courseNameField.getText().trim());
            stmt.setString(2, courseDescriptionField.getText().trim());
            stmt.setInt(3, selected.getId());

            if (stmt.executeUpdate() > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Course updated successfully!");
                loadCourses();
                clearFields();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error updating course: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteCourse() {
        Course selected = courseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.ERROR, "No course selected for deletion.");
            return;
        }

        // Show confirmation dialog
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete this course?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "DELETE FROM courses WHERE id=?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, selected.getId());

                int result = stmt.executeUpdate();
                if (result > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Course deleted successfully!");
                    loadCourses();
                    clearFields();
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error deleting course: " + e.getMessage());
            }
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
            Stage stage = (Stage) courseNameField.getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/javafxgroupassignment/admindashboard.fxml")));
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Dashboard");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error returning to dashboard: " + e.getMessage());
        }
    }

    private void clearFields() {
        courseNameField.clear();
        courseDescriptionField.clear();
    }

    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}