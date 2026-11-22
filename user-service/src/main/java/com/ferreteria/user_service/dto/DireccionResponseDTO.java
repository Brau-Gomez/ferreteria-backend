package com.ferreteria.user_service.dto;

import com.ferreteria.user_service.model.Direccion;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DireccionResponseDTO {

    private UUID id;
    private UUID usuarioId;
    private String alias;
    private String calle;
    private String numero;
    private String departamento;
    private String comuna;
    private String region;
    private String codigoPostal;
    private String referencia;
    private Double latitud;
    private Double longitud;
    private Boolean esPrincipal;
    private Boolean esFacturacion;
    private Boolean esDespacho;
    private Instant createdAt;
    private Instant updatedAt;

    public static DireccionResponseDTO fromEntity(Direccion direccion) {
        return DireccionResponseDTO.builder()
                .id(direccion.getId())
                .usuarioId(direccion.getUsuario() != null ? direccion.getUsuario().getId() : null)
                .alias(direccion.getAlias())
                .calle(direccion.getCalle())
                .numero(direccion.getNumero())
                .departamento(direccion.getDepartamento())
                .comuna(direccion.getComuna())
                .region(direccion.getRegion())
                .codigoPostal(direccion.getCodigoPostal())
                .referencia(direccion.getReferencia())
                .latitud(direccion.getLatitud())
                .longitud(direccion.getLongitud())
                .esPrincipal(direccion.getEsPrincipal())
                .esFacturacion(direccion.getEsFacturacion())
                .esDespacho(direccion.getEsDespacho())
                .createdAt(direccion.getCreatedAt())
                .updatedAt(direccion.getUpdatedAt())
                .build();
    }
}