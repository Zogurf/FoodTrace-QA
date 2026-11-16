package com.foodtraceqa;

import com.foodtraceqa.dto.TrazabilidadDTO.*;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;

class ConsumosValidationTests {
    @Test
    void validaElementosNulosIdsYCantidadesDeCadaConsumo() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            for (List<SolicitudConsumo> consumos : List.of(
                    Collections.<SolicitudConsumo>singletonList(null),
                    List.of(new SolicitudConsumo(null, 1)),
                    List.of(new SolicitudConsumo(0L, 1)),
                    List.of(new SolicitudConsumo(1L, 0)),
                    List.of(new SolicitudConsumo(1L, -2)))) {
                var request = new SolicitudProduccion(1L, "PRUEBA", 1, new Date(), consumos);
                assertThat(factory.getValidator().validate(request)).isNotEmpty();
            }
            var valid = new SolicitudProduccion(1L, "PRUEBA", 1, new Date(),
                    List.of(new SolicitudConsumo(1L, 2), new SolicitudConsumo(2L, 3)));
            assertThat(factory.getValidator().validate(valid)).isEmpty();
        }
    }
}
