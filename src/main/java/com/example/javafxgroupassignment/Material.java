package com.example.javafxgroupassignment;


public class Material {
    private int id;
    private String courseName;
    private String title;
    private String filename;
    private int courseId;

    public Material(int id, String courseName, String title, String filename, int courseId) {
        this.id = id;
        this.courseName = courseName;
        this.title = title;
        this.filename = filename;
        this.courseId = courseId;
    }

    public int getId() { return id; }
    public String getCourseName() { return courseName; }
    public String getTitle() { return title; }
    public String getFilename() { return filename; }
    public int getCourseId() { return courseId; }
}

