package com.routeoptimizer.dto;

import com.routeoptimizer.model.enums.Role;

public record RegisterRequest(
        String email,
        String password,
        String name,
        String phone,
        Role role) {
}
