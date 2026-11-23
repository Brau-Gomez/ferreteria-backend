package com.ferreteria.user_service.config;

import com.ferreteria.user_service.model.Usuario;
import com.ferreteria.user_service.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Aplica la contraseña demo a los perfiles semilla; vacío = no hace nada (ruta segura para tests/CI).
 */
@Component
public class DemoCredentialsSeeder implements ApplicationRunner {

    private static final List<String> DEMO_EMAILS = List.of(
            "admin@duoc.cl", "vendedor@profesor.duoc.cl", "cliente@gmail.com",
            "contratista@gmail.com", "maria.soto@gmail.com", "pedro.ramirez@gmail.com");

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String demoPassword;

    public DemoCredentialsSeeder(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed.demo-password:}") String demoPassword) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.demoPassword = demoPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (demoPassword == null || demoPassword.isBlank()) {
            return;
        }
        DEMO_EMAILS.forEach(email -> actualizar(email, demoPassword));
    }

    private void actualizar(String email, String password) {
        usuarioRepository.findByEmail(email).ifPresent(usuario -> {
            if (!passwordEncoder.matches(password, usuario.getPasswordHash())) {
                usuario.setPasswordHash(passwordEncoder.encode(password));
                usuarioRepository.save(usuario);
            }
        });
    }
}
