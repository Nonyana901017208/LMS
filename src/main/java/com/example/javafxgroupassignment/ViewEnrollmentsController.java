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
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Objects;

public class ViewEnrollmentsController {

    @FXML private TableView<String[]> instructorTable;
    @FXML private TableColumn<String[], String> instNameCol;
    @FXML private TableColumn<String[], String> instCourseCol;

    @FXML private TableView<String[]> studentTable;
    @FXML private TableColumn<String[], String> studentNameCol;
    @FXML private TableColumn<String[], String> studentCourseCol;

    @FXML private TextField instructorSearchField, studentSearchField;
    @FXML private Label instructorCountLabel, studentCountLabel;
    private FilteredList<String[]> filteredInstructors;
    private FilteredList<String[]> filteredStudents;

    @FXML
    private void exportInstructorPDF() {
        exportTableToPDF("Instructor Assignments",
                instructorTable.getItems(),
                "instructor_assignments.pdf",
                new String[]{"Instructor", "Assigned Course"});
    }

    @FXML
    private void exportStudentPDF() {
        exportTableToPDF("Student Enrollments",
                studentTable.getItems(),
                "student_enrollments.pdf",
                new String[]{"Student", "Enrolled Course"});
    }

    @FXML
    private void viewAllInstructors() {
        try {
            Stage stage = (Stage) instructorTable.getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/javafxgroupassignment/viewinstructors.fxml")));
            stage.setScene(new Scene(root));
            stage.setTitle("All Instructor Assignments");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error loading instructor view: " + e.getMessage());
        }
    }

    @FXML
    private void viewAllStudents() {
        try {
            Stage stage = (Stage) studentTable.getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/javafxgroupassignment/viewstudents.fxml")));
            stage.setScene(new Scene(root));
            stage.setTitle("All Student Enrollments");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error loading student view: " + e.getMessage());
        }
    }

    private void exportTableToPDF(String title, ObservableList<String[]> data,
                                  String filename, String[] headers) {
        try {
            // Create document and page
            PDDocument document = new PDDocument();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // Create content stream
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Set up fonts and write title
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText(title);
            contentStream.endText();

            // Table parameters
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            float margin = 50;
            float yStart = 700;
            float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
            float yPosition = yStart;
            float rowHeight = 20;
            float tableBottom = 50;

            // Draw headers
            drawTableRow(contentStream, margin, yPosition, tableWidth, headers, true);
            yPosition -= rowHeight;

            // Draw data rows
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            for (String[] row : data) {
                if (yPosition <= tableBottom) {
                    // New page if we reach the bottom
                    contentStream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    yPosition = yStart;
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                }
                drawTableRow(contentStream, margin, yPosition, tableWidth, row, false);
                yPosition -= rowHeight;
            }

            contentStream.close();

            // Save to user's documents folder
            String userHome = System.getProperty("user.home");
            File documentsDir = new File(userHome, "Documents");
            if (!documentsDir.exists()) {
                documentsDir.mkdirs();
            }

            File file = new File(documentsDir, filename);
            document.save(file);
            document.close();

            showAlert(Alert.AlertType.INFORMATION, "PDF successfully exported to:\n" + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Failed to export PDF:\n" + e.getMessage());
        }
    }

    private void drawTableRow(PDPageContentStream contentStream, float x, float y,
                              float tableWidth, String[] cells, boolean isHeader)
            throws IOException {
        float colWidth = tableWidth / cells.length;

        // Draw cell backgrounds for headers
        if (isHeader) {
            contentStream.setNonStrokingColor(200, 200, 200); // Light gray
            contentStream.addRect(x, y - 15, tableWidth, 20);
            contentStream.fill();
            contentStream.setNonStrokingColor(0, 0, 0); // Black
        }

        // Draw horizontal line
        contentStream.setLineWidth(0.5f);
        contentStream.moveTo(x, y);
        contentStream.lineTo(x + tableWidth, y);
        contentStream.stroke();

        // Draw cell content
        contentStream.beginText();
        contentStream.newLineAtOffset(x + 5, y - 12);
        for (int i = 0; i < cells.length; i++) {
            contentStream.showText(cells[i] != null ? cells[i] : "");
            contentStream.newLineAtOffset(colWidth, 0);
        }
        contentStream.endText();

        // Draw vertical lines
        for (int i = 0; i <= cells.length; i++) {
            contentStream.moveTo(x + i * colWidth, y);
            contentStream.lineTo(x + i * colWidth, y - 15);
            contentStream.stroke();
        }
    }

    @FXML
    public void initialize() {
        // Set up cell value factories
        instNameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[0]));
        instCourseCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[1]));

        studentNameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[0]));
        studentCourseCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[1]));

        // Load data
        loadInstructorAssignments();
        loadStudentEnrollments();
    }

    private void loadInstructorAssignments() {
        ObservableList<String[]> data = FXCollections.observableArrayList();

        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
                     SELECT u.name, c.course_name
                     FROM instructor_assignments ia
                     JOIN users u ON ia.instructor_id = u.id
                     JOIN courses c ON ia.course_id = c.id
                     """;

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                data.add(new String[]{
                        rs.getString("name"),
                        rs.getString("course_name")
                });
            }

            filteredInstructors = new FilteredList<>(data, p -> true);
            instructorTable.setItems(filteredInstructors);

            instructorSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
                filteredInstructors.setPredicate(row -> {
                    String combined = (row[0] + row[1]).toLowerCase();
                    return combined.contains(newVal.toLowerCase());
                });
            });

            instructorCountLabel.setText("Instructors Assigned: " + data.size());

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error loading instructor assignments:\n" + e.getMessage());
        }
    }

    private void loadStudentEnrollments() {
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

            studentSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
                filteredStudents.setPredicate(row -> {
                    String combined = (row[0] + row[1]).toLowerCase();
                    return combined.contains(newVal.toLowerCase());
                });
            });

            studentCountLabel.setText("Students Enrolled: " + data.size());

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error loading student enrollments:\n" + e.getMessage());
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
            Stage stage = (Stage) instructorTable.getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/javafxgroupassignment/admindashboard.fxml")));
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Dashboard");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error returning to dashboard:\n" + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}