package com.foodtraceqa.entities;

import jakarta.persistence.*;
import lombok.*;
import com.foodtraceqa.enums.Dictamen;
import jakarta.validation.constraints.NotBlank;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "controles_calidad")
public class ControlCalidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "control_calidad_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lote_produccion_id", nullable = false)
    private LoteProduccion loteProduccion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario responsable;

    private String evidencia;
    @NotBlank
    @Column(nullable = false, length = 1000)
    private String observacion;
    private Date fecha;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Dictamen dictamen;

    @PreRemove
    private void preventPhysicalDeletion() {
        throw new UnsupportedOperationException("Los controles de calidad son historial");
    }
}
