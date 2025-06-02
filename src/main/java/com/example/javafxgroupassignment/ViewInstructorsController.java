package com.example.javafxgroupassignment;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.Objects;

public class ViewInstructorsController {

    @FXML private TableView<String[]> instructorTable;
    @FXML private TableColumn<String[], String> nameCol;
    @FXML private TableColumn<String[], String> courseCol;
    @FXML private TextField searchField;
    @FXML private Label countLabel;

    private FilteredList<String[]> filteredInstructors;

    @FXML
    public void initialize() {
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[0]));
        courseCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[1]));

        loadInstructors();
    }

    private void loadInstructors() {
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

            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                filteredInstructors.setPredicate(row -> {
                    String combined = (row[0] + row[1]).toLowerCase();
                    return combined.contains(newVal.toLowerCase());
                });
            });

            countLabel.setText("Total Instructors: " + data.size());

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error loading instructor assignments:\n" + e.getMessage());
        }
    }

    @FXML
    private void exportPDF() {
        // Implement PDF export similar to ViewEnrollmentsController
    }

    @FXML
    private void handleBack() {
        try {
            Stage stage = (Stage) instructorTable.getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/javafxgroupassignment/viewenrollments.fxml")));
            stage.setScene(new Scene(root));
            stage.setTitle("View Enrollments");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error returning to enrollments: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}