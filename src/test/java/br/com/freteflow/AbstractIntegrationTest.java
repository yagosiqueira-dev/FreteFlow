package br.com.freteflow;

import br.com.freteflow.entity.User;
import br.com.freteflow.entity.UserRole;
import br.com.freteflow.repository.UserRepository;
import br.com.freteflow.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected TokenService tokenService;

    protected String createAdminAndGetToken(String email) {
        User admin = User.builder()
                .name("Admin de Teste")
                .email(email)
                .password(passwordEncoder.encode("senha123"))
                .role(UserRole.ADMIN)
                .enabled(true)
                .build();

        userRepository.save(admin);

        return tokenService.generateToken(admin);
    }

    protected String createOperatorAndGetToken(String email) {
        User operator = User.builder()
                .name("Operador de Teste")
                .email(email)
                .password(passwordEncoder.encode("senha123"))
                .role(UserRole.OPERATOR)
                .enabled(true)
                .build();

        userRepository.save(operator);

        return tokenService.generateToken(operator);
    }
}