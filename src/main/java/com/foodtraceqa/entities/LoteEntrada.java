package com.foodtraceqa.entities;

import com.foodtraceqa.enums.Estado;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "lotes_entrada", indexes = {
        @Index(name = "idx_entrada_proveedor", columnList = "proveedor_id"),
        @Index(name = "idx_entrada_materia", columnList = "materia_prima_id")
})
public class LoteEntrada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lote_entrada_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor enviadoPor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "materia_prima_id", nullable = false)
    private Materia materiaPrima;

    private Date fechaRecepcion;
    private Date fechaVencimiento;
    @PositiveOrZero
    @Column(nullable = false)
    private double cantidad;
    @PositiveOrZero
    @Column(nullable = false)
    private double cantidadDisponible;
    private boolean completado;
    @Column(nullable = false)
    private boolean activo = true;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado;

    @PreRemove
    private void preventPhysicalDeletion() {
        throw new UnsupportedOperationException("Los lotes de entrada usan borrado lógico");
    }
}
