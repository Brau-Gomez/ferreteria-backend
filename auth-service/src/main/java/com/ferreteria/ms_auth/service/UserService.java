package com.ferreteria.ms_auth.service;

import com.ferreteria.ms_auth.model.User;
import com.ferreteria.ms_auth.repository.UserRepository;
import com.ferreteria.ms_auth.repository.RoleRepository;
import com.ferreteria.ms_auth.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(String username, String email, String password, String fullName, String phone) {
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("El email ya está registrado");
        }
        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("El nombre de usuario ya está en uso");
        }

        // Obtener rol CLIENTE por defecto
        var roleCliente = roleRepository.findByName("CLIENTE")
                .orElseThrow(() -> new IllegalStateException("Rol CLIENTE no configurado en BD"));

        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .fullName(fullName)
                .phone(phone)
                .isActive(true)
                .isVerified(false)
                .roles(Set.of(roleCliente.getName()))
                .build();

        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));
    }

    public User updateProfile(Long userId, String fullName, String phone) {
        User user = findById(userId);
        if (fullName != null) user.setFullName(fullName);
        if (phone != null) user.setPhone(phone);
        return userRepository.save(user);
    }

    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = findById(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BadRequestException("Contraseña actual incorrecta");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void migratePassword(Long userId, String plainPassword) {
        User user = findById(userId);
        user.setPasswordHash(passwordEncoder.encode(plainPassword));
        userRepository.save(user);
    }

    public boolean checkPassword(Long userId, String plainPassword) {
        User user = findById(userId);
        return passwordEncoder.matches(plainPassword, user.getPasswordHash());
    }
}