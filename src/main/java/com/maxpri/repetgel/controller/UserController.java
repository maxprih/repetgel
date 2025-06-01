package com.maxpri.repetgel.controller;

import com.maxpri.repetgel.dto.StudentDTO;
import com.maxpri.repetgel.dto.UserDTO;
import com.maxpri.repetgel.dto.UserProfileUpdateDTO;
import com.maxpri.repetgel.service.UserService;
import com.maxpri.repetgel.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SecurityUtils securityUtils;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUserProfile() {
        return ResponseEntity.ok(userService.getCurrentUserDTO());
    }

    @PutMapping("/me/profile")
    public ResponseEntity<UserDTO> updateCurrentUserProfile(@Valid @RequestBody UserProfileUpdateDTO profileUpdateDTO) {
        return ResponseEntity.ok(userService.updateCurrentUserProfile(profileUpdateDTO));
    }

    @GetMapping("/students/search")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<List<StudentDTO>> searchStudents(@RequestParam String searchTerm) {
        return ResponseEntity.ok(userService.searchStudents(searchTerm));
    }

    @PostMapping("/students/{studentId}/grant-lives")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<StudentDTO> grantLivesToStudent(
            @PathVariable String studentId,
            @RequestParam int livesToAdd) {
        return ResponseEntity.ok(userService.grantAdditionalLives(studentId, livesToAdd));
    }
}