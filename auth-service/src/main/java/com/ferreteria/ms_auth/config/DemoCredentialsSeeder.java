package com.ferreteria.ms_auth.config;

import com.ferreteria.ms_auth.model.User;
import com.ferreteria.ms_auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Aplica contraseñas de usuarios semilla desde el entorno; vacío = no hace nada (ruta segura para tests/CI).
 */
@Component
public class DemoCredentialsSeeder implements ApplicationRunner {

    private static final String ADMIN_EMAIL = "admin@ferreteria.local";
    private static final List<String> DEMO_EMAILS = List.of(
            "admin@duoc.cl", "vendedor@profesor.duoc.cl", "cliente@gmail.com");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminPassword;
    private final String demoPassword;

    public DemoCredentialsSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed.admin-password:}") String adminPassword,
            @Value("${app.seed.demo-password:}") String demoPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminPassword = adminPassword;
        this.demoPassword = demoPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminPassword != null && !adminPassword.isBlank()) {
            actualizar(ADMIN_EMAIL, adminPassword);
        }
        if (demoPassword != null && !demoPassword.isBlank()) {
            DEMO_EMAILS.forEach(email -> actualizar(email, demoPassword));
        }
    }

    private void actualizar(String email, String password) {
        userRepository.findByEmail(email).ifPresent(usuario -> {
            if (!passwordEncoder.matches(password, usuario.getPasswordHash())) {
                usuario.setPasswordHash(passwordEncoder.encode(password));
                userRepository.save(usuario);
            }
        });
    }
}
