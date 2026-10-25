package edu.mit.academicclock.model;

import java.time.LocalDate;

/**
 * Unit II: Interfaces & Unit I: java.time API
 * Represents any calendar or academic item that occurs on a given date.
 */
public interface Schedulable {
    LocalDate getDate();
    String getTitle();
    String getCategoryOrType();
}
