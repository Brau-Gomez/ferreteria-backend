package com.ferreteria.ms_auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenDTO {
    @NotBlank(message = "El refresh token es obligatorio")
    private String refreshToken;
}