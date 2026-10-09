package com.hms.service;

import com.hms.HmsException;

/** Input that breaks a rule, e.g. a 9-digit phone number. The message says what to fix. */
public class ValidationException extends HmsException {

    public ValidationException(String message) {
        super(message);
    }
}
