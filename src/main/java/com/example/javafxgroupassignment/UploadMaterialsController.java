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
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UploadMaterialsController {

    @FXML private ComboBox<Course> courseComboBox;
    @FXML private TextField materialTitleField;
    @FXML private TextField filePathField;
    @FXML private Label statusLabel;
    @FXML private TableView<Material> materialView_table;
    @FXML private TableColumn<Material, String> MCourse_col;
    @FXML private TableColumn<Material, String> MMaterial_col;
    @FXML private TableColumn<Material, Void> MAction_col;

    private User instructor;
    private File selectedFile;
    private ObservableList<Material> materialList = FXCollections.observableArrayList();

    public void setInstructor(User instructor) {
        this.instructor = instructor;
        loadInstructorCourses();
    }

    private void loadInstructorCourses() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
                SELECT c.id, c.course_name, c.description
                FROM courses c
                JOIN instructor_assignments ia ON c.id = ia.course_id
                WHERE ia.instructor_id = ?
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, instructor.getId());
            ResultSet rs = stmt.executeQuery();

            ObservableList<Course> courses = FXCollections.observableArrayList();
            while (rs.next()) {
                Course course = new Course(rs.getInt("id"), rs.getString("course_name"), rs.getString("description"));
                courses.add(course);
            }
            courseComboBox.setItems(courses);
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading courses.");
        }
    }

    @FXML
    private void handleSelectCourse() {
        loadCourseMaterials();
    }

    private void loadCourseMaterials() {
        materialList.clear();
        Course course = courseComboBox.getValue();
        if (course == null) return;

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM course_materials WHERE course_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, course.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                materialList.add(new Material(
                        rs.getInt("id"),
                        course.getCourseName(),
                        rs.getString("title"),
                        rs.getString("filename"),
                        course.getId()
                ));
            }

            MCourse_col.setCellValueFactory(new PropertyValueFactory<>("courseName"));
            MMaterial_col.setCellValueFactory(new PropertyValueFactory<>("title"));
            materialView_table.setItems(materialList);
            addActionButtons();

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Failed to load materials.");
        }
    }

    private void addActionButtons() {
        MAction_col.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button deleteBtn = new Button("Delete");
            private final Button updateBtn = new Button("Update");

            {
                viewBtn.setOnAction(e -> {
                    Material material = getTableView().getItems().get(getIndex());
                    try {
                        File file = new File("materials/" + material.getFilename());
                        if (file.exists()) {
                            new ProcessBuilder("explorer.exe", file.getAbsolutePath()).start();
                        } else {
                            showAlert("File Not Found", "The material file does not exist.");
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });

                deleteBtn.setOnAction(e -> {
                    Material material = getTableView().getItems().get(getIndex());
                    deleteMaterial(material);
                });

                updateBtn.setOnAction(e -> {
                    Material material = getTableView().getItems().get(getIndex());
                    materialTitleField.setText(material.getTitle());
                    filePathField.setText("materials/" + material.getFilename());
                    selectedFile = new File("materials/" + material.getFilename());
                    courseComboBox.getSelectionModel().select(new Course(material.getCourseId(), material.getCourseName(), ""));
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, viewBtn, updateBtn, deleteBtn);
                    setGraphic(buttons);
                }
            }
        });
    }

    private void deleteMaterial(Material material) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM course_materials WHERE id = ?");
            stmt.setInt(1, material.getId());
            stmt.executeUpdate();

            File file = new File("materials/" + material.getFilename());
            if (file.exists()) file.delete();

            materialList.remove(material);
            statusLabel.setText("Material deleted.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Delete Failed", "Could not delete material.");
        }
    }

    @FXML
    private void handleBrowse() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Course Material");
        selectedFile = chooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            filePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void handleUpload() {
        Course course = courseComboBox.getValue();
        String title = materialTitleField.getText();

        if (course == null || selectedFile == null || title == null || title.isEmpty()) {
            statusLabel.setText("All fields are required.");
            return;
        }

        try {
            File destDir = new File("materials");
            destDir.mkdirs();
            File dest = new File(destDir, selectedFile.getName());

            try (FileInputStream in = new FileInputStream(selectedFile);
                 FileOutputStream out = new FileOutputStream(dest)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }

            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("""
                        INSERT INTO course_materials (course_id, title, filename)
                        VALUES (?, ?, ?)
                        """);
                stmt.setInt(1, course.getId());
                stmt.setString(2, title);
                stmt.setString(3, selectedFile.getName());
                stmt.executeUpdate();

                statusLabel.setText("Upload successful!");
                materialTitleField.clear();
                filePathField.clear();
                selectedFile = null;
                loadCourseMaterials();
            }

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Upload failed.");
        }
    }

    @FXML
    private void handleBackToDashboard(ActionEvent event) {
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
            e.printStackTrace();
            showAlert("Navigation Error", "Could not return to dashboard: " + e.getMessage());
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

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
