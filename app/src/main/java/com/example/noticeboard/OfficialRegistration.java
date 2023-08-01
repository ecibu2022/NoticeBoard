package com.example.noticeboard;

public class OfficialRegistration {
    String imageURL, name, title, department, email, password, role;

    public OfficialRegistration() {
    }

    public OfficialRegistration(String imageURL, String name, String title, String department, String email, String password, String role) {
        this.imageURL = imageURL;
        this.name = name;
        this.title = title;
        this.department = department;
        this.email = email;
        this.password = password;
        this.role=role;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
