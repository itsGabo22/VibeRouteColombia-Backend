package com.routeoptimizer.dto;

/**
 * Enhanced authentication response including safe user details.
 */
public record AuthenticationResponse(
        String token,
        UserResponseDTO user) {
}
