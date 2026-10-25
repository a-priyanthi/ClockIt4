package edu.mit.academicclock.exception;

/**
 * Unit III: User-defined Exception
 * Thrown when an entity (Subject, CalendarItem, Event) cannot be found.
 */
public class ResourceNotFoundException extends AcademicClockException {
    public ResourceNotFoundException(String message) {
        super(message, 404);
    }
}
