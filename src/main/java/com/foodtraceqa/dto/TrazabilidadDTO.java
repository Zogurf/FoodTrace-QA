package com.foodtraceqa.dto;

import com.foodtraceqa.enums.Estado;
import com.foodtraceqa.enums.Dictamen;
import jakarta.validation.constraints.*;
import java.util.Date;
import java.util.List;

public final class TrazabilidadDTO {
    private TrazabilidadDTO() {}

    public record SolicitudRecepcion(@NotNull Long proveedorId, @NotNull Long materiaPrimaId,
                                      @NotNull Date fechaVencimiento, @Positive double cantidad) {}

    public record SolicitudProduccion(@NotNull Long productoId, @NotBlank String codigo,
                                       @Positive double cantidad, @NotNull Date fechaProduccion,
                                       @NotEmpty List<SolicitudConsumo> consumos) {}

    public record SolicitudConsumo(@NotNull Long loteEntradaId, @Positive double cantidadConsumida) {}

    public record SolicitudCalidad(@NotNull Long loteProduccionId, String evidencia,
                                    @NotBlank String observacion, @NotNull Dictamen dictamen) {}

    public record TrazabilidadEntrada(Long loteEntradaId, String materiaPrima, String proveedor,
                                       Date fechaRecepcion, Date fechaVencimiento, double cantidadConsumida) {}

    public record TrazabilidadSalida(Long loteProduccionId, String codigo, String producto,
                                      double cantidad, Estado estado, double cantidadConsumida) {}

    public record TrazabilidadHaciaAtras(Long loteProduccionId, String codigo, String producto,
                                          Estado estado, List<TrazabilidadEntrada> materiasPrimas) {}

    public record TrazabilidadHaciaAdelante(Long loteEntradaId, String materiaPrima, String proveedor,
                                             List<TrazabilidadSalida> lotesProduccion) {}
}
