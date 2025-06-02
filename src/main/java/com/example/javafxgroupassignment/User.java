package com.example.javafxgroupassignment;

import javafx.beans.property.*;

public class User {

    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty email;
    private final StringProperty username;
    private final StringProperty role;

    public User(int id, String name, String email, String username, String role) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.email = new SimpleStringProperty(email);
        this.username = new SimpleStringProperty(username);
        this.role = new SimpleStringProperty(role);
    }

    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }

    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }

    public String getEmail() { return email.get(); }
    public StringProperty emailProperty() { return email; }

    public String getUsername() { return username.get(); }
    public StringProperty usernameProperty() { return username; }

    public String getRole() { return role.get(); }
    public StringProperty roleProperty() { return role; }

    @Override
    public String toString() {
        return name.get() + " (" + username.get() + ")";
    }

}
