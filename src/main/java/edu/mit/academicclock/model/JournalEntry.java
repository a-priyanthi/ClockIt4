package edu.mit.academicclock.model;

import java.time.LocalDate;

/**
 * Unit I & II: Scrapbook Journal Entry Model
 */
public class JournalEntry extends BaseEntity {
    private String title;
    private String text;
    private String mood; // "😔", "😐", "🙂", "😊", "🌱"
    private LocalDate entryDate;

    public JournalEntry() {
        super();
        this.entryDate = LocalDate.now();
    }

    public JournalEntry(String id, String title, String text, String mood, LocalDate entryDate) {
        super(id);
        this.title = title;
        this.text = text;
        this.mood = mood;
        this.entryDate = entryDate != null ? entryDate : LocalDate.now();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }
}
