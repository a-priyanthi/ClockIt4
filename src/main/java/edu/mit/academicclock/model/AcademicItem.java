package edu.mit.academicclock.model;

import java.time.LocalDate;

/**
 * Unit II: Inheritance (Subclass of BaseEntity, Implementing Schedulable)
 * Polymorphic base for CalendarItems and CampusEvents.
 */
public abstract class AcademicItem extends BaseEntity implements Schedulable {
    protected String title;
    protected LocalDate date;
    protected String type;
    protected String detail;

    public AcademicItem() {
        super();
    }

    public AcademicItem(String id, String title, LocalDate date, String type, String detail) {
        super(id);
        this.title = title;
        this.date = date;
        this.type = type;
        this.detail = detail;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    @Override
    public String getCategoryOrType() {
        return type;
    }

    public abstract String getSummary();
}
