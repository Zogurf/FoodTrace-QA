package com.foodtraceqa.entities;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "proveedores")
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "proveedor_id")
    private Long id;

    @NotNull
    @Positive
    @Column(unique = true, nullable = false)
    private Long ruc;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String nombre;

    @Min(0)
    @Max(5)
    private int calificacion;

    @Column(nullable = false)
    private boolean activo = true;

    @PreRemove
    private void preventPhysicalDeletion() {
        throw new UnsupportedOperationException("Los proveedores usan borrado lógico");
    }
}
