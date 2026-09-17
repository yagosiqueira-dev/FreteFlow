package br.com.freteflow.service;

import br.com.freteflow.dto.user.UserRequestDTO;
import br.com.freteflow.dto.user.UserResponseDTO;
import br.com.freteflow.entity.User;
import br.com.freteflow.entity.UserRole;
import br.com.freteflow.exception.CannotDeactivateSelfException;
import br.com.freteflow.exception.EmailAlreadyExistsException;
import br.com.freteflow.exception.UserNotFoundException;
import br.com.freteflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(String name, String email, String rawPassword, UserRole role) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .enabled(true)
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public UserResponseDTO promoteToAdmin(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setRole(UserRole.ADMIN);
        User updated = userRepository.save(user);

        return UserResponseDTO.fromEntity(updated);
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDTO> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return UserResponseDTO.fromEntity(user);
    }

    @Transactional
    public UserResponseDTO updateUser(UUID id, UserRequestDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (!request.email().equals(user.getEmail())
                && userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        user.setName(request.name());
        user.setEmail(request.email());

        User updated = userRepository.save(user);
        return UserResponseDTO.fromEntity(updated);
    }

    @Transactional
    public void deactivateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (isCurrentUser(id)) {
            throw new CannotDeactivateSelfException();
        }

        user.setEnabled(false);
        userRepository.save(user);
    }

    @Transactional
    public UserResponseDTO activateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setEnabled(true);
        User updated = userRepository.save(user);

        return UserResponseDTO.fromEntity(updated);
    }

    private boolean isCurrentUser(UUID id) {
        User currentUser = (User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        return currentUser.getId().equals(id);
    }
}