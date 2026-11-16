package com.foodtraceqa;

import com.foodtraceqa.dto.TrazabilidadDTO.*;
import com.foodtraceqa.entities.*;
import com.foodtraceqa.enums.*;
import com.foodtraceqa.repositories.*;
import com.foodtraceqa.services.CalidadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CalidadServiceTests {
    private final ControlCalidadRepository controles = mock(ControlCalidadRepository.class);
    private final LoteProduccionRepository producciones = mock(LoteProduccionRepository.class);
    private final LoteEntradaRepository entradas = mock(LoteEntradaRepository.class);
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final CalidadService service = new CalidadService(controles, producciones, entradas, usuarios);
    private LoteProduccion lote;

    @BeforeEach
    void preparar() {
        Usuario supervisor = new Usuario();
        supervisor.setUsername("calidad");
        supervisor.setRol(Rol.SUPERVISOR);
        when(usuarios.findByUsernameAndActivoTrue("calidad")).thenReturn(Optional.of(supervisor));
        lote = new LoteProduccion();
        lote.setId(1L);
        lote.setActivo(true);
        lote.setEstado(Estado.CUARENTENA);
        when(producciones.findByIdForUpdate(1L)).thenReturn(Optional.of(lote));
        when(controles.save(any(ControlCalidad.class))).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void aprobarExigeEvidenciaSinModificarElLote() {
        assertThatThrownBy(() -> service.create(new SolicitudCalidad(1L, "  ", "Revisado", Dictamen.APROBADO), "calidad"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("evidencia");
        assertThat(lote.getEstado()).isEqualTo(Estado.CUARENTENA);
        verify(controles, never()).save(any());
    }

    @Test
    void rechazarLimpiaLiberacionYConservaAmbosControles() {
        ControlRespuesta aprobado = service.create(new SolicitudCalidad(1L, "informe-1", "Conforme", Dictamen.APROBADO), "calidad");
        assertThat(lote.getFechaLiberacion()).isNotNull();
        assertThat(lote.getLiberadoPor().getUsername()).isEqualTo("calidad");
        assertThat(lote.getEvidencia()).isEqualTo("informe-1");
        service.create(new SolicitudCalidad(1L, null, "Nueva inspección: rechazado", Dictamen.RECHAZADO), "calidad");
        assertThat(lote.getEstado()).isEqualTo(Estado.RECHAZADO);
        assertThat(lote.getFechaLiberacion()).isNull();
        assertThat(lote.getLiberadoPor()).isNull();
        assertThat(lote.getEvidencia()).isNull();
        assertThat(aprobado.evidencia()).isEqualTo("informe-1");
        verify(controles, times(2)).save(any());
    }

    @Test
    void entradaVencidaNoPuedeLiberarse() {
        LoteEntrada entrada = new LoteEntrada();
        entrada.setActivo(true);
        entrada.setEstado(Estado.CUARENTENA);
        entrada.setFechaVencimiento(new Date(0));
        when(entradas.findByIdForUpdate(2L)).thenReturn(Optional.of(entrada));
        assertThatThrownBy(() -> service.createEntrada(new SolicitudCalidadEntrada(2L, "informe", "Inspección", Dictamen.APROBADO), "calidad"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("vencido");
        verify(controles, never()).save(any());
    }

    @Test
    void entradaVigenteSeLiberaConResponsableFechaYEvidencia() {
        LoteEntrada entrada = new LoteEntrada();
        entrada.setId(2L);
        entrada.setActivo(true);
        entrada.setEstado(Estado.CUARENTENA);
        entrada.setFechaVencimiento(new Date(System.currentTimeMillis() + 86_400_000L));
        when(entradas.findByIdForUpdate(2L)).thenReturn(Optional.of(entrada));
        ControlRespuesta respuesta = service.createEntrada(new SolicitudCalidadEntrada(2L, "informe", "Conforme", Dictamen.APROBADO), "calidad");
        assertThat(entrada.getEstado()).isEqualTo(Estado.LIBERADO);
        assertThat(entrada.getFechaLiberacion()).isNotNull();
        assertThat(entrada.getLiberadoPor().getUsername()).isEqualTo("calidad");
        assertThat(respuesta.loteEntradaId()).isEqualTo(2L);
        assertThat(respuesta.loteProduccionId()).isNull();
    }
}
