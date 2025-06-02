package com.example.javafxgroupassignment;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Objects;

public class StudentMaterialsController {

    @FXML private Pagination pagination;
    @FXML private TabPane tabPane;

    private User student;  // currently logged-in student
    private final int coursesPerPage = 3;  // number of courses per pagination page

    private List<Course> studentCourses;

    public void setStudent(User student) {
        this.student = student;
        loadStudentCourses();
    }

    private void loadStudentCourses() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
            SELECT c.id, c.course_name, c.description
            FROM courses c
            JOIN student_enrollments se ON c.id = se.course_id
            WHERE se.student_id = ?
            ORDER BY c.course_name
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, student.getId());
            ResultSet rs = stmt.executeQuery();

            studentCourses = new java.util.ArrayList<>();
            while (rs.next()) {
                studentCourses.add(new Course(rs.getInt("id"), rs.getString("course_name"), rs.getString("description")));
            }

            if (studentCourses.isEmpty()) {
                tabPane.getTabs().clear();
                Tab noMaterialsTab = new Tab("No Materials");
                noMaterialsTab.setContent(new Label("Materials not yet uploaded"));
                tabPane.getTabs().add(noMaterialsTab);
                if (pagination != null) pagination.setPageCount(1);
            } else {
                int pageCount = (int) Math.ceil((double) studentCourses.size() / coursesPerPage);
                pagination.setPageCount(pageCount == 0 ? 1 : pageCount);
                pagination.setCurrentPageIndex(0);
                pagination.setPageFactory(this::createPage);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load student courses.");
        }
    }


    private VBox createPage(int pageIndex) {
        tabPane.getTabs().clear();

        int start = pageIndex * coursesPerPage;
        int end = Math.min(start + coursesPerPage, studentCourses.size());

        for (int i = start; i < end; i++) {
            Course course = studentCourses.get(i);
            Tab courseTab = new Tab(course.getCourseName());
            courseTab.setContent(createMaterialsTable(course));
            tabPane.getTabs().add(courseTab);
        }

        VBox box = new VBox(tabPane);
        return box;
    }

    private TableView<Material> createMaterialsTable(Course course) {
        TableView<Material> table = new TableView<>();
        ObservableList<Material> materials = FXCollections.observableArrayList();
        table.setPrefHeight(200);

        TableColumn<Material, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(200);

        TableColumn<Material, String> filenameCol = new TableColumn<>("Filename");
        filenameCol.setCellValueFactory(new PropertyValueFactory<>("filename"));
        filenameCol.setPrefWidth(200);

        TableColumn<Material, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");

            {
                viewBtn.setOnAction(e -> {
                    Material material = getTableView().getItems().get(getIndex());
                    try {
                        java.io.File file = new java.io.File("materials/" + material.getFilename());
                        if (file.exists()) {
                            // Open the file (Windows example)
                            new ProcessBuilder("explorer.exe", file.getAbsolutePath()).start();
                        } else {
                            showAlert("File Not Found", "The material file does not exist.");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        showAlert("Error", "Failed to open material file.");
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewBtn);
                }
            }
        });
        actionCol.setPrefWidth(90);

        table.getColumns().addAll(titleCol, filenameCol, actionCol);

        // Load materials for the course from DB
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM course_materials WHERE course_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, course.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                materials.add(new Material(
                        rs.getInt("id"),
                        course.getCourseName(),
                        rs.getString("title"),
                        rs.getString("filename"),
                        course.getId()
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        table.setItems(materials);
        return table;
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

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
