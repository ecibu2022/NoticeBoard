package com.example.noticeboard;

import java.util.List;

public class PostNoticeModal {
    private String title;
    private String body;
    private List<String> imageUrls;
    private List<String> fileUrls;
    private String everyone;
    private String faculty;
    private String course;
    private String year;
    private String terms;
    private String submittedBy;
    private String dateTime;

    public PostNoticeModal() {
        // Empty constructor required for Firebase
    }

    public PostNoticeModal(String title, String body, List<String> imageUrls, List<String> fileUrls, String everyone, String faculty, String course, String year, String terms, String submittedBy, String dateTime) {
        this.title = title;
        this.body = body;
        this.imageUrls = imageUrls;
        this.fileUrls = fileUrls;
        this.everyone = everyone;
        this.faculty = faculty;
        this.course = course;
        this.year = year;
        this.terms = terms;
        this.submittedBy = submittedBy;
        this.dateTime = dateTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }



    public String getEveryone() {
        return everyone;
    }

    public void setEveryone(String everyone) {
        this.everyone = everyone;
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

    public String getTerms() {
        return terms;
    }

    public void setTerms(String terms) {
        this.terms = terms;
    }

    public String getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(String submittedBy) {
        this.submittedBy = submittedBy;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public List<String> getFileUrls() {
        return fileUrls;
    }

    public void setFileUrls(List<String> fileUrls) {
        this.fileUrls = fileUrls;
    }
}
