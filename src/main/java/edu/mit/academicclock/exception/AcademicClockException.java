package edu.mit.academicclock.exception;

/**
 * Unit III: Exception Handling - Base User-Defined Exception
 */
public class AcademicClockException extends RuntimeException {
    private final int statusCode;

    public AcademicClockException(String message) {
        super(message);
        this.statusCode = 500;
    }

    public AcademicClockException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public AcademicClockException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
