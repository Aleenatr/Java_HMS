package com.hms;

/**
 * Base class for every error the application expects and can explain to the user,
 * for example "That room is no longer available". The UI shows the message in a dialog.
 *
 * <p>Subclasses: ValidationException (bad input), AccessDeniedException (role not allowed)
 * and DataAccessException (database error).
 */
public class HmsException extends RuntimeException {

    public HmsException(String message) {
        super(message);
    }

    public HmsException(String message, Throwable cause) {
        super(message, cause);
    }
}
