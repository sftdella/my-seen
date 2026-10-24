package com.della.myseen.model;

public class Show {
    private int id;
    private String title;
    private String genre;
    private String status;
    private Integer rating;
    private String addedDate;
    private String addedTime;
    private String note;
    private long addedTimestamp;

    public Show() {}

    public Show(String title, String genre, String status, Integer rating,
                String addedDate, String addedTime, String note, long addedTimestamp) {
        this.title = title;
        this.genre = genre;
        this.status = status;
        this.rating = rating;
        this.addedDate = addedDate;
        this.addedTime = addedTime;
        this.note = note;
        this.addedTimestamp = addedTimestamp;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getAddedDate() { return addedDate; }
    public void setAddedDate(String addedDate) { this.addedDate = addedDate; }

    public String getAddedTime() { return addedTime; }
    public void setAddedTime(String addedTime) { this.addedTime = addedTime; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public long getAddedTimestamp() { return addedTimestamp; }
    public void setAddedTimestamp(long addedTimestamp) { this.addedTimestamp = addedTimestamp; }
}
