package br.com.freteflow.service;

import br.com.freteflow.entity.User;
import br.com.freteflow.entity.UserRole;
import br.com.freteflow.exception.EmailAlreadyExistsException;
import br.com.freteflow.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUserWithHashedPassword() {

        when(userRepository.existsByEmail("joao@freteflow.com")).thenReturn(false);

        when(passwordEncoder.encode("senha123")).thenReturn("senha_criptografada_mock");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        User created = userService.createUser("João", "joao@freteflow.com", "senha123", UserRole.OPERATOR);


        assertThat(created.getPassword()).isEqualTo("senha_criptografada_mock");

        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowWhenEmailAlreadyExists() {

        when(userRepository.existsByEmail("joao@freteflow.com")).thenReturn(true);

        // ACT & ASSERT
        assertThatThrownBy(() ->
                userService.createUser("João", "joao@freteflow.com", "senha123", UserRole.OPERATOR)
        ).isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }
}