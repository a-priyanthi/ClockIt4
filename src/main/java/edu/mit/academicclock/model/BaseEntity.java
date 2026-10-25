package edu.mit.academicclock.model;

import java.time.LocalDateTime;

/**
 * Unit II: Abstract Classes & Inheritance (Superclass)
 * Unit I: Modern Date & Time API (LocalDateTime)
 */
public abstract class BaseEntity implements Identifiable {
    protected String id;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;

    public BaseEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public BaseEntity(String id) {
        this();
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Method overriding demo for Unit II
    @Override
    public String toString() {
        return getClass().getSimpleName() + " [id=" + id + ", createdAt=" + createdAt + "]";
    }
}
