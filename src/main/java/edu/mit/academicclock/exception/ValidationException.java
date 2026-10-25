package edu.mit.academicclock.exception;

/**
 * Unit III: User-defined Exception
 * Thrown when incoming request fields fail validation rules.
 */
public class ValidationException extends AcademicClockException {
    public ValidationException(String message) {
        super(message, 400);
    }
}
