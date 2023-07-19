package com.example.noticeboard;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class PostNoticeModal implements Parcelable {
    private String id;
    private String title;
    private String body;
    private String imageUrl;
    private String fileUrl;
    private String everyone;
    private String faculty;
    private String course;
    private String year;
    private String terms;
    private String submittedBy;
    private String dateTime;
    private String key;
    private int likeCount;

    public PostNoticeModal() {
        // Empty constructor required for Firebase
    }

    public PostNoticeModal(String id, String title, String body, String imageUrl, String fileUrl, String everyone, String faculty, String course, String year, String terms, String submittedBy, String dateTime) {
        this.title = title;
        this.body = body;
        this.imageUrl = imageUrl;
        this.fileUrl = fileUrl;
        this.everyone = everyone;
        this.faculty = faculty;
        this.course = course;
        this.year = year;
        this.terms = terms;
        this.submittedBy = submittedBy;
        this.dateTime = dateTime;
        this.id = id;
    }

    protected PostNoticeModal(Parcel in) {
        title = in.readString();
        body = in.readString();
        submittedBy = in.readString();
        dateTime = in.readString();
        fileUrl = in.readString();
        imageUrl = in.readString();
    }

    public static final Creator<PostNoticeModal> CREATOR = new Creator<PostNoticeModal>() {
        @Override
        public PostNoticeModal createFromParcel(Parcel in) {
            return new PostNoticeModal(in);
        }

        @Override
        public PostNoticeModal[] newArray(int size) {
            return new PostNoticeModal[size];
        }
    };


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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrls(String fileUrls) {
        this.fileUrl = fileUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(body);
        dest.writeString(submittedBy);
        dest.writeString(dateTime);
        dest.writeString(fileUrl);
        dest.writeString(imageUrl);
    }

}
