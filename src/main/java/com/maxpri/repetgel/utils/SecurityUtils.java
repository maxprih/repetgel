package com.maxpri.repetgel.utils;

import com.maxpri.repetgel.entity.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityUtils {

    public static Optional<String> getCurrentUserKeycloakId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return Optional.ofNullable(jwt.getSubject());
        }
        return Optional.empty();
    }
    
    public static String getCurrentUserKeycloakIdRequired() {
        return getCurrentUserKeycloakId()
                .orElseThrow(() -> new IllegalStateException("Authenticated user Keycloak ID not found in SecurityContext"));
    }

    public static Optional<String> getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return Optional.ofNullable(jwt.getClaimAsString("email"));
        }
        return Optional.empty();
    }
    
    public static String getCurrentUserEmailRequired() {
         return getCurrentUserEmail()
                .orElseThrow(() -> new IllegalStateException("Authenticated user email not found in SecurityContext"));
    }
    
    public  boolean hasRole(UserRole role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        String roleName = "ROLE_" + role.name();
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(roleName::equals);
    }

    public boolean isCurrentUserTutor() {
        return hasRole(UserRole.TUTOR);
    }

    public boolean isCurrentUserStudent() {
        return hasRole(UserRole.STUDENT);
    }
}