package com.foodtraceqa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodtraceqa.dto.TrazabilidadDTO.*;
import com.foodtraceqa.entities.Usuario;
import com.foodtraceqa.enums.*;
import org.junit.jupiter.api.Test;
import java.util.Date;
import static org.assertj.core.api.Assertions.assertThat;

class RespuestaSeguraTests {
    @Test
    void nuncaSerializaElPasswordDelUsuario() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setUsername("supervisor");
        usuario.setPassword("hash-secreto");
        String json = new ObjectMapper().writeValueAsString(usuario);
        assertThat(json).contains("supervisor").doesNotContain("password", "hash-secreto");
    }

    @Test
    void respuestasDeOperacionesYCalidadNoContienenEntidadesDeUsuario() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String quality = mapper.writeValueAsString(new ControlRespuesta(1L, 2L, null,
                "supervisor", "informe", "Conforme", new Date(), Dictamen.APROBADO));
        String operation = mapper.writeValueAsString(new OperacionRespuesta(2L, "LOTE", Estado.LIBERADO, 10, null));
        assertThat(quality).contains("\"responsable\":\"supervisor\"").doesNotContain("password", "liberadoPor");
        assertThat(operation).doesNotContain("password", "liberadoPor", "usuario");
    }
}
