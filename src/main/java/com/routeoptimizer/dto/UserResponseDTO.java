package com.routeoptimizer.dto;

import com.routeoptimizer.model.enums.Role;

/**
 * Safe DTO for exposing user information without sensitive data like password
 * hashes.
 */
public class UserResponseDTO {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private Role role;

    public UserResponseDTO() {
    }

    public UserResponseDTO(Long id, String email, String name, String phone, Role role) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.role = role;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
