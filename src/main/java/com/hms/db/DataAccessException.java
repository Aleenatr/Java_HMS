package com.hms.db;

import com.hms.HmsException;

import java.sql.SQLException;

/**
 * A database error, wrapped so callers don't need try/catch for SQLException everywhere.
 * The helper methods read MySQL's error codes so services can turn them into plain messages.
 */
public class DataAccessException extends HmsException {

    private static final int DUPLICATE_KEY = 1062;
    private static final int ROW_IS_REFERENCED = 1451;
    private static final int REFERENCED_ROW_MISSING = 1452;
    private static final int CHECK_CONSTRAINT_FAILED = 3819;

    private final int errorCode;

    public DataAccessException(String message, SQLException cause) {
        super(message + ": " + cause.getMessage(), cause);
        this.errorCode = cause.getErrorCode();
    }

    /** A UNIQUE key was violated, e.g. a username or a doctor's time slot is already taken. */
    public boolean isDuplicateKey() {
        return errorCode == DUPLICATE_KEY;
    }

    /** Deleting a row that other rows still point to, e.g. a patient who has appointments. */
    public boolean isRowStillReferenced() {
        return errorCode == ROW_IS_REFERENCED;
    }

    /** Inserting a row that points to something that does not exist. */
    public boolean isMissingReference() {
        return errorCode == REFERENCED_ROW_MISSING;
    }

    public boolean isCheckViolation() {
        return errorCode == CHECK_CONSTRAINT_FAILED;
    }
}
