package com.hms.security;

import com.hms.HmsException;

/** Thrown when the logged-in user's role is not allowed to do something. */
public class AccessDeniedException extends HmsException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
