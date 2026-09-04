package com.foodtraceqa.entities;

import com.foodtraceqa.enums.Medida;
import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "materias_prima", indexes = @Index(name = "idx_materia_nombre", columnList = "nombre"))
public class Materia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "materia_prima_id")
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private boolean activo = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Medida unidadMedida;

    @PreRemove
    private void preventPhysicalDeletion() {
        throw new UnsupportedOperationException("Las materias primas usan borrado lógico");
    }
}
