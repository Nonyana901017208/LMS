package com.example.javafxgroupassignment;

import javafx.beans.property.*;

public class Course {

    private IntegerProperty id;
    private StringProperty courseName;
    private StringProperty description;

    public Course(int id, String courseName, String description) {
        this.id = new SimpleIntegerProperty(id);
        this.courseName = new SimpleStringProperty(courseName);
        this.description = new SimpleStringProperty(description);
    }

    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }

    public String getCourseName() { return courseName.get(); }
    public StringProperty courseNameProperty() { return courseName; }

    public String getDescription() { return description.get(); }
    public StringProperty descriptionProperty() { return description; }

    @Override
    public String toString() {
        return courseName.get();
    }
}