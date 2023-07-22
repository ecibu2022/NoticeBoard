package com.example.noticeboard;

public class CreateEventsModal {
    String eventID, Title, Description, Date, Location, startTime, endTime;

    public CreateEventsModal() {
    }


    public CreateEventsModal( String eventID, String title, String description, String date, String location, String startTime, String endTime) {
        this.Title = title;
        this.Description = description;
        this.Date = date;
        this.Location = location;
        this.eventID = eventID;
        this.startTime = startTime;
        this.endTime = endTime;
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

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
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
}
