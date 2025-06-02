package com.example.javafxgroupassignment;

public class CourseMaterial {
    private int id;
    private String courseName;
    private String materialTitle;
    private String filename;

    public CourseMaterial(int id, String courseName, String materialTitle, String filename) {
        this.id = id;
        this.courseName = courseName;
        this.materialTitle = materialTitle;
        this.filename = filename;
    }

    public int getId() { return id; }
    public String getCourseName() { return courseName; }
    public String getMaterialTitle() { return materialTitle; }
    public String getFilename() { return filename; }
}

