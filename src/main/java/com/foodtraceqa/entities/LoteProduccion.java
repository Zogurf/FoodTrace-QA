package com.foodtraceqa.entities;

import com.foodtraceqa.enums.Estado;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id")
    private Proveedor responsable;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario liberadoPor;

    private int cantidad;
    private String evidencia;
    @Column(name = "fecha_producion")
    private Date fechaProducion;
    @Column(name = "fecha_liberacion")
    private Date fechaLiberacion;
    @Enumerated(EnumType.STRING)
    private Estado estado;
}
