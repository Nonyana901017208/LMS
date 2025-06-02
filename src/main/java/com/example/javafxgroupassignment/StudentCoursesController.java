package com.example.javafxgroupassignment;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

import java.sql.*;
import java.util.Objects;

public class StudentCoursesController {

    @FXML private TableView<Course> courseTable;
    @FXML private TableColumn<Course, String> courseNameColumn;
    @FXML private TableColumn<Course, String> descColumn;
    @FXML private TableColumn<User, String> instructorColumn;

    private User student;

    public void setStudent(User student) {
        this.student = student;
        if (student != null) {
            loadCourses();
        } else {
            System.err.println("Student is null in setStudent()");
        }
    }

    @FXML
    public void initialize() {
        courseNameColumn.setCellValueFactory(data -> data.getValue().courseNameProperty());
        descColumn.setCellValueFactory(data -> data.getValue().descriptionProperty());
        //instructorColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        loadCourses();

    }

    private void loadCourses() {
        if (student == null) {
            System.err.println("Student is null in loadCourses()");
            return;
        }

        ObservableList<Course> courses = FXCollections.observableArrayList();
        //ObservableList<User> users = FXCollections.observableArrayList();

        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
                SELECT c.id, c.course_name, c.description, u.name
                FROM courses c
                JOIN student_enrollments se  ON se.course_id = c.id
                JOIN users u ON u.id = se.student_id WHERE se.student_id = ?
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, student.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                courses.add(new Course(
                        rs.getInt("id"),
                        rs.getString("course_name"),
                        rs.getString("description")
                ));
//                users.add(new User(
//                        rs.getInt("id"),
//                        rs.getString("name"),
//                        rs.getString("email"),
//                        rs.getString("username"),
//                        rs.getString("role")
//                ));
            }
            System.out.println(courses);
            courseTable.setItems(courses);
            System.out.println("courses loaded.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/javafxgroupassignment/studentdashboard.fxml"));
            Parent root = loader.load();

            StudentDashboardController controller= loader.getController();
            controller.setStudent(student);

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
        loadSceneWithController("/com/example/javafxgroupassignment/studentcourses.fxml", event, "My Enrolled Courses", controller -> {
            ((StudentCoursesController) controller).setStudent(student);
        });
    }

    @FXML
    private void handleViewMaterials(ActionEvent event) {
        loadSceneWithController("/com/example/javafxgroupassignment/StudentMaterials.fxml", event, "Course Materials", controller -> {
            ((StudentMaterialsController) controller).setStudent(student);
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

    private void loadSceneWithController(String fxmlPath, ActionEvent event, String title, StudentDashboardController.ControllerInitializer initializer) {
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
}
