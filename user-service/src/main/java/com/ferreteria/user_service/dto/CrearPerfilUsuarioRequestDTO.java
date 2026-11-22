package com.ferreteria.user_service.dto;

import com.ferreteria.user_service.model.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearPerfilUsuarioRequestDTO {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    private String password;

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    private String nombreCompleto;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^\\+?[0-9\\s-]{8,20}$", message = "Formato de teléfono inválido")
    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono;

    @Pattern(regexp = "^[0-9]{7,8}-[0-9kK]{1}$", message = "Formato de RUT inválido (ej: 12345678-9)")
    @Size(max = 12, message = "El RUT no puede exceder 12 caracteres")
    private String rut;

    private Usuario.Rol rol = Usuario.Rol.CLIENTE;
}
