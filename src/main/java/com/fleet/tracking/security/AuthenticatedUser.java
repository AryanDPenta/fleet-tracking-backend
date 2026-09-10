package com.fleet.tracking.security;

import com.fleet.tracking.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

// Lightweight principal placed into the SecurityContext by JwtAuthFilter.
// Controllers pull this via @AuthenticationPrincipal instead of re-parsing the token.
@Getter
@AllArgsConstructor
public class AuthenticatedUser {
    private final Long id;
    private final Role role;
    private final String name;
}
