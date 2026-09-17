package br.com.freteflow.controller;

import br.com.freteflow.dto.user.UserRequestDTO;
import br.com.freteflow.dto.user.UserResponseDTO;
import br.com.freteflow.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDTO>> list(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<UserResponseDTO> users = userService.listUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable UUID id) {
        UserResponseDTO user = userService.findById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> update(
            @PathVariable UUID id, @Valid @RequestBody UserRequestDTO request) {

        UserResponseDTO updated = userService.updateUser(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> activate(@PathVariable UUID id) {
        UserResponseDTO activated = userService.activateUser(id);
        return ResponseEntity.ok(activated);
    }

    @PatchMapping("/{id}/promote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> promoteToAdmin(@PathVariable UUID id) {
        UserResponseDTO promoted = userService.promoteToAdmin(id);
        return ResponseEntity.ok(promoted);
    }
}