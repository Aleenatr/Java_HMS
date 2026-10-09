package com.hms.model;

import com.hms.security.Role;

import java.time.LocalDateTime;

/**
 * A login account. The password hash is deliberately not part of this record,
 * so it can never reach the screens. displayName is the linked doctor's or
 * patient's name (or the username for administrators).
 */
public record User(int id, String username, Role role, boolean active, String displayName,
                   LocalDateTime createdAt) {
}
