package edu.mit.academicclock.model;

import java.time.LocalDate;

/**
 * Unit II: Inheritance (Subclass of AcademicItem)
 */
public class CampusEvent extends AcademicItem {
    private String category;
    private String time;
    private String place;
    private String description;
    private boolean registered;

    public CampusEvent() {
        super();
        this.type = "event";
    }

    public CampusEvent(String id, String title, String category, LocalDate date, String time, String place, String description, boolean registered) {
        super(id, title, date, "event", place);
        this.category = category;
        this.time = time;
        this.place = place;
        this.description = description;
        this.registered = registered;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
        this.detail = place;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    @Override
    public String getCategoryOrType() {
        return category != null ? category : "CAMPUS LIFE";
    }

    @Override
    public String getSummary() {
        return "[" + category + "] " + title + " at " + place + " on " + date + " " + time;
    }
}
