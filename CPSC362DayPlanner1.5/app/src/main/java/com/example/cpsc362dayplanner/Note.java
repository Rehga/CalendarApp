package com.example.cpsc362dayplanner;

public class Note {
    private String title;
    private String content;
    private String date;
    private String time;
    private boolean isUrgent;  // Add this field

    public boolean isUrgent() {
        return isUrgent;
    }

    //getter for Date
    public String getDate() {
        return date;
    }
    // setter for Date
    public void setDate(String date) {
        this.date = date;
    }
    // getter for Time
    public String getTime() {
        return time;
    }
    // setter for Time
    public void setTime(String time) {
        this.time = time;
    }
    public Note(String title, String content) {
        this.title = title;
        this.content = content;
    }
    // Getter for 'title'
    public String getTitle() {
        return title;
    }

    // Setter for 'title'
    public void setTitle(String title) {
        this.title = title;
    }

    // Getter for 'content'
    public String getContent() {
        return content;
    }
    // Setter for 'content'
    public void setContent(String content) {
        this.content = content;
    }
    //Setter for 'Urgent'
    public void setUrgent(boolean urgent) {
        isUrgent = urgent;
    }

}
