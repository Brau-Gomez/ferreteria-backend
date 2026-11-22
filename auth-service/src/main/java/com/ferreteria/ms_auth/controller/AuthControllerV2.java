package com.ferreteria.ms_auth.controller;

import com.ferreteria.ms_auth.assemblers.AuthProfileModelAssembler;
import com.ferreteria.ms_auth.dto.UsuarioPerfilDTO;
import com.ferreteria.ms_auth.dto.ValidacionTokenDTO;
import com.ferreteria.ms_auth.service.AuthService;
import com.ferreteria.ms_auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/v2")
@RequiredArgsConstructor
public class AuthControllerV2 {

    private final AuthService authService;
    private final UserService userService;
    private final AuthProfileModelAssembler assembler;

    @GetMapping(value = "/me", produces = MediaTypes.HAL_JSON_VALUE)
    public EntityModel<UsuarioPerfilDTO> me(@AuthenticationPrincipal UserDetails userDetails) {
        var user = userService.findByEmail(userDetails.getUsername());
        UsuarioPerfilDTO profile = UsuarioPerfilDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .roles(user.getRoles())
                .build();
        return assembler.toModel(profile);
    }

    @GetMapping(value = "/validate", produces = MediaTypes.HAL_JSON_VALUE)
    public ValidacionTokenDTO validate(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = authHeader != null && authHeader.startsWith("Bearer ")
                ? authHeader.substring(7)
                : "";
        return authService.validateToken(token);
    }
}
