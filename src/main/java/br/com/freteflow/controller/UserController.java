package br.com.freteflow.controller;

import br.com.freteflow.dto.user.UserResponseDTO;
import br.com.freteflow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PatchMapping("/{id}/promote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> promoteToAdmin(@PathVariable UUID id) {
        UserResponseDTO promoted = userService.promoteToAdmin(id);
        return ResponseEntity.ok(promoted);
    }
}