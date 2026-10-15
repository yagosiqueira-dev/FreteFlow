package br.com.freteflow.bootstrap;

import br.com.freteflow.entity.User;
import br.com.freteflow.entity.UserRole;
import br.com.freteflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("bootstrap")
public class AdminBootstrapRunner implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Value("${FRETEFLOW_BOOTSTRAP_ADMIN_NAME:}")
    private String name;

    @Value("${FRETEFLOW_BOOTSTRAP_ADMIN_EMAIL:}")
    private String email;

    @Value("${FRETEFLOW_BOOTSTRAP_ADMIN_PASSWORD:}")
    private String password;

    public AdminBootstrapRunner(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            Environment environment) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        boolean enabled = environment.getProperty(
                "FRETEFLOW_BOOTSTRAP_ADMIN_ENABLED",
                Boolean.class,
                false
        );

        if (!enabled) {
            throw new IllegalStateException(
                    "Bootstrap desativado. Habilite-o explicitamente para executar."
            );
        }

        name = name == null ? "" : name.trim();
        email = email == null ? "" : email.trim().toLowerCase();
        password = password == null ? "" : password;

        if (name.isBlank() || email.isBlank() || password.length() < 12) {
            throw new IllegalStateException(
                    "Informe nome, e-mail e senha com pelo menos 12 caracteres."
            );
        }

        if (userRepository.existsByRole(UserRole.ADMIN)) {
            throw new IllegalStateException(
                    "Já existe um administrador. Bootstrap cancelado."
            );
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException(
                    "O e-mail informado já existe. Nenhuma conta foi alterada."
            );
        }

        User admin = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(UserRole.ADMIN)
                .enabled(true)
                .build();

        userRepository.saveAndFlush(admin);

        System.out.println(
                "Bootstrap concluído: administrador criado para " + email
        );
    }
}
