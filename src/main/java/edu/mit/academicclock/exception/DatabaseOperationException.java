package edu.mit.academicclock.exception;

/**
 * Unit III: User-defined Exception
 * Wraps low-level JDBC SQL exceptions into a domain-level exception.
 */
public class DatabaseOperationException extends AcademicClockException {
    public DatabaseOperationException(String message, Throwable cause) {
        super(message, cause, 500);
    }
}
