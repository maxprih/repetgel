package com.maxpri.repetgel.dto;

import com.maxpri.repetgel.entity.UserRole;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class UserDTO {
    private String keycloakUserId;
    private String firstName;
    private String lastName;
    private String email;
    private UserRole role;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}