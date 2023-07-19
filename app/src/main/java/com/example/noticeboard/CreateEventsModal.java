package com.example.noticeboard;

public class CreateEventsModal {
    String eventID, Title, Description, Date, startTime, endTime, Location;

    public CreateEventsModal() {
    }

    public CreateEventsModal(String eventID, String title, String description, String date, String startTime, String endTime, String location) {
        this.eventID = eventID;
        Title = title;
        Description = description;
        Date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        Location = location;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }
}
