package com.example.course_app.dto;

import com.example.course_app.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private long id;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
}