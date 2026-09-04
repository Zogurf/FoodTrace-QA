package com.foodtraceqa.entities;

import com.foodtraceqa.enums.Estado;
import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "lotes_produccion", indexes = {
        @Index(name = "idx_produccion_codigo", columnList = "codigo", unique = true),
        @Index(name = "idx_produccion_producto", columnList = "producto_id")
})
public class LoteProduccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lote_produccion_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario liberadoPor;

    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String codigo;
    @Positive
    @Column(nullable = false)
    private double cantidad;
    private String evidencia;
    private Date fechaProduccion;
    private Date fechaLiberacion;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado;

    @Column(nullable = false)
    private boolean activo = true;

    @PreRemove
    private void preventPhysicalDeletion() {
        throw new UnsupportedOperationException("Los lotes de producción usan borrado lógico");
    }
}
