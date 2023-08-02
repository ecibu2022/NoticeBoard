package com.example.noticeboard;

public class SuggestionsModal {
    String id, title, body, userID;

    public SuggestionsModal() {
    }

    public SuggestionsModal(String id, String title, String body, String userID) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.userID=userID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
