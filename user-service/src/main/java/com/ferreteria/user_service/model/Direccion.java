package com.ferreteria.user_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "direcciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "alias", nullable = false, length = 50)
    private String alias; // ej: "Casa", "Oficina", "Bodega"

    @Column(name = "calle", nullable = false, length = 200)
    private String calle;

    @Column(name = "numero", nullable = false, length = 20)
    private String numero;

    @Column(name = "departamento", length = 20)
    private String departamento;

    @Column(name = "comuna", nullable = false, length = 100)
    private String comuna;

    @Column(name = "region", nullable = false, length = 100)
    private String region;

    @Column(name = "codigo_postal", length = 10)
    private String codigoPostal;

    @Column(name = "referencia", length = 500)
    private String referencia; // punto de referencia adicional

    // Double, no BigDecimal: la precisión decimal declarada en @Column no aplica a tipos de coma flotante.
    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;

    @Column(name = "es_principal", nullable = false)
    private Boolean esPrincipal = false;

    @Column(name = "es_facturacion", nullable = false)
    private Boolean esFacturacion = false;

    @Column(name = "es_despacho", nullable = false)
    private Boolean esDespacho = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}