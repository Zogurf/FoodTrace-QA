package com.foodtraceqa.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "lotes_consumo", indexes = {
        @Index(name = "idx_consumo_produccion", columnList = "lote_producido_id"),
        @Index(name = "idx_consumo_entrada", columnList = "lote_entrante_id")
})
public class LoteConsumo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lote_consumo_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lote_producido_id", nullable = false)
    private LoteProduccion loteProducido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lote_entrante_id", nullable = false)
    private LoteEntrada loteEntrante;

    @Column(nullable = false)
    private Double cantidadConsumida;

    @PreRemove
    private void preventPhysicalDeletion() {
        throw new UnsupportedOperationException("Los consumos son historial de trazabilidad");
    }
}
