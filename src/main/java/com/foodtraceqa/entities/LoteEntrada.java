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
@Table(name = "lotes_entrada")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "liberado_por_id")
    private Usuario liberadoPor;
    private Date fechaLiberacion;
    private String evidencia;

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
