package com.example.noticeboard;

public class Comment {
    private String commentID, comment, userID, userImage, userName;
    private Object timeCommented;

    public Comment() {
    }

    public Comment(String commentID, String comment, String userID, String userImage, String userName, Object timeCommented) {
        this.commentID=commentID;
        this.comment = comment;
        this.userID = userID;
        this.userImage = userImage;
        this.userName = userName;
        this.timeCommented = timeCommented;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Object getTimeCommented() {
        return timeCommented;
    }

    public void setTimeCommented(String timeCommented) {
        this.timeCommented = timeCommented;
    }

    public String getCommentID() {
        return commentID;
    }

    public void setCommentID(String commentID) {
        this.commentID = commentID;
    }
}
