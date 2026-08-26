package br.com.freteflow;

import br.com.freteflow.entity.Driver;
import br.com.freteflow.entity.Store;
import br.com.freteflow.entity.User;
import br.com.freteflow.entity.UserRole;
import br.com.freteflow.entity.Vehicle;
import br.com.freteflow.repository.DriverRepository;
import br.com.freteflow.repository.StoreRepository;
import br.com.freteflow.repository.UserRepository;
import br.com.freteflow.repository.VehicleRepository;
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

import java.math.BigDecimal;

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
    protected DriverRepository driverRepository;

    @Autowired
    protected VehicleRepository vehicleRepository;

    @Autowired
    protected StoreRepository storeRepository;

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

    protected Driver createDriver(String cpf, boolean enabled) {
        Driver driver = Driver.builder()
                .name("Motorista de Teste")
                .phone("11900000000")
                .cpf(cpf)
                .build();

        driver = driverRepository.save(driver);
        driver.setEnabled(enabled);
        return driverRepository.save(driver);
    }

    protected Vehicle createVehicle(String licensePlate, boolean enabled) {
        Vehicle vehicle = Vehicle.builder()
                .licensePlate(licensePlate)
                .type("Truck")
                .model("Modelo de Teste")
                .year(2022)
                .build();

        vehicle = vehicleRepository.save(vehicle);
        vehicle.setEnabled(enabled);
        return vehicleRepository.save(vehicle);
    }

    protected Store createStore(String name, BigDecimal defaultValue, boolean enabled) {
        Store store = Store.builder()
                .name(name)
                .origin("Origem Teste")
                .destination("Destino Teste")
                .defaultValue(defaultValue)
                .build();

        store = storeRepository.save(store);
        store.setEnabled(enabled);
        return storeRepository.save(store);
    }
}