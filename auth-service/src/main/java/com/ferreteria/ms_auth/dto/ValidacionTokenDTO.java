package com.ferreteria.ms_auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidacionTokenDTO {
    private boolean valido;
    private String username;
    private String email;
    private java.util.Set<String> roles;
}