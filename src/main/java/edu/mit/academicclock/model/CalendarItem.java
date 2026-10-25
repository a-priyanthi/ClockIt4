package edu.mit.academicclock.model;

import java.time.LocalDate;

/**
 * Unit II: Inheritance (Subclass of AcademicItem)
 */
public class CalendarItem extends AcademicItem {

    public CalendarItem() {
        super();
    }

    public CalendarItem(String id, String title, LocalDate date, String type, String detail) {
        super(id, title, date, type, detail);
    }

    @Override
    public String getSummary() {
        return "[" + type.toUpperCase() + "] " + title + " on " + date + " (" + detail + ")";
    }
}
