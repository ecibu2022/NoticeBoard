package com.example.noticeboard;

public class UserRegistrationModal {
    public String fullName, regNO, profileImage, faculty, course, year, email, password, role, deviceToken;

    public UserRegistrationModal() {
    }

    public UserRegistrationModal(String fullName, String regNO, String profileImage, String faculty, String course, String year, String email, String password, String role, String deviceToken) {
        this.fullName = fullName;
        this.regNO = regNO;
        this.profileImage = profileImage;
        this.faculty = faculty;
        this.course = course;
        this.year = year;
        this.email = email;
        this.password=password;
        this.role=role;
        this.deviceToken = deviceToken;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRegNO() {
        return regNO;
    }

    public void setRegNO(String regNO) {
        this.regNO = regNO;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
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

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
}
