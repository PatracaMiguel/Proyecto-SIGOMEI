package com.sigomei.servidor.service;

import com.sigomei.api.catalogos.*;
import com.sigomei.api.dto.OrdenDTO;
import com.sigomei.api.excepciones.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class ReglasNegocioServiceTest {

    private final OrdenService ordenService = new OrdenService();
    private final TecnicoService tecnicoService = new TecnicoService();
    private final EquipoService equipoService = new EquipoService();

    @Test
    public void rn01_positivo_tecnicoCompatible() {
        OrdenDTO orden = crearOrden(1, 1, 1, EstadoOrden.PROGRAMADA);

        OrdenDTO resultado = assertDoesNotThrow(() -> ordenService.registrarOrden(orden));

        assertNotNull(resultado);
        assertEquals(1, resultado.getIdEquipo());
        assertEquals(1, resultado.getIdTecnico());
    }

    @Test
    public void rn01_negativo_tecnicoIncompatible() {
        OrdenDTO orden = crearOrden(2, 1, 2, EstadoOrden.PROGRAMADA);

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> ordenService.registrarOrden(orden)
        );

        assertTrue(error.getMessage().toLowerCase().contains("especialidad"));
    }

    @Test
    public void rn02_positivo_sinOrdenDuplicada() {
        OrdenDTO orden = crearOrdenConFecha(3, 1, 1, LocalDate.of(2026, 5, 21));

        OrdenDTO resultado = assertDoesNotThrow(() -> ordenService.registrarOrden(orden));

        assertNotNull(resultado);
        assertEquals(LocalDate.of(2026, 5, 21), resultado.getFechaProgramada());
        assertEquals(EstadoOrden.PROGRAMADA, resultado.getEstadoOrden());
    }

    @Test
    public void rn02_negativo_ordenDuplicada() {
        OrdenDTO orden = crearOrdenConFecha(4, 1, 1, LocalDate.of(2026, 5, 20));

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> ordenService.registrarOrden(orden)
        );

        assertTrue(error.getMessage().toLowerCase().contains("duplicada")
                || error.getMessage().toLowerCase().contains("activa"));
    }

    @Test
    public void rn03_positivo_eliminarTecnicoSinOrdenes() {
        int idTecnicoSinOrdenes = 10;

        assertDoesNotThrow(() -> tecnicoService.eliminarTecnico(idTecnicoSinOrdenes));
    }

    @Test
    public void rn03_negativo_eliminarTecnicoConOrdenes() {
        int idTecnicoConOrdenes = 1;

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> tecnicoService.eliminarTecnico(idTecnicoConOrdenes)
        );

        assertTrue(error.getMessage().toLowerCase().contains("orden"));
    }

    @Test
    public void rn04_positivo_tecnicoActivo() {
        OrdenDTO orden = crearOrden(5, 1, 1, EstadoOrden.PROGRAMADA);

        OrdenDTO resultado = assertDoesNotThrow(() -> ordenService.registrarOrden(orden));

        assertNotNull(resultado);
        assertEquals(1, resultado.getIdTecnico());
    }

    @Test
    public void rn04_negativo_tecnicoInactivo() {
        OrdenDTO orden = crearOrden(6, 1, 3, EstadoOrden.PROGRAMADA);

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> ordenService.registrarOrden(orden)
        );

        assertTrue(error.getMessage().toLowerCase().contains("inactivo"));
    }

    @Test
    public void rn05_positivo_fechasCorrectas() {
        OrdenDTO orden = crearOrdenConFechas(
                7,
                LocalDate.of(2026, 5, 20),
                LocalDate.of(2026, 5, 21),
                LocalDate.of(2026, 5, 22)
        );

        OrdenDTO resultado = assertDoesNotThrow(() -> ordenService.registrarOrden(orden));

        assertNotNull(resultado);
        assertFalse(resultado.getFechaInicio().isBefore(resultado.getFechaProgramada()));
        assertFalse(resultado.getFechaCierre().isBefore(resultado.getFechaInicio()));
    }

    @Test
    public void rn05_negativo_fechasIncorrectas() {
        OrdenDTO orden = crearOrdenConFechas(
                8,
                LocalDate.of(2026, 5, 20),
                LocalDate.of(2026, 5, 19),
                LocalDate.of(2026, 5, 21)
        );

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> ordenService.registrarOrden(orden)
        );

        assertTrue(error.getMessage().toLowerCase().contains("fecha"));
    }

    @Test
    public void rn06_positivo_finalizarConDatosCompletos() {
        OrdenDTO resultado = assertDoesNotThrow(() ->
                ordenService.cambiarEstadoOrden(
                        1,
                        EstadoOrden.FINALIZADA,
                        LocalDate.of(2026, 5, 25),
                        new BigDecimal("2500.00")
                )
        );

        assertNotNull(resultado);
        assertEquals(EstadoOrden.FINALIZADA, resultado.getEstadoOrden());
        assertNotNull(resultado.getFechaCierre());
        assertNotNull(resultado.getCostoReal());
    }

    @Test
    public void rn06_negativo_finalizarSinDatos() {
        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> ordenService.cambiarEstadoOrden(
                        1,
                        EstadoOrden.FINALIZADA,
                        null,
                        null
                )
        );

        assertTrue(error.getMessage().toLowerCase().contains("cierre")
                || error.getMessage().toLowerCase().contains("costo"));
    }


    @Test
    public void rn07_positivo_criticidadAltaConNivelII() {
        OrdenDTO orden = crearOrden(9, 1, 1, EstadoOrden.PROGRAMADA);

        OrdenDTO resultado = assertDoesNotThrow(() -> ordenService.registrarOrden(orden));

        assertNotNull(resultado);
        assertEquals(1, resultado.getIdEquipo());
        assertEquals(1, resultado.getIdTecnico());
    }

    @Test
    public void rn07_negativo_criticidadAltaConNivelI() {
        OrdenDTO orden = crearOrden(10, 1, 4, EstadoOrden.PROGRAMADA);

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> ordenService.registrarOrden(orden)
        );

        assertTrue(error.getMessage().toLowerCase().contains("certificación")
                || error.getMessage().toLowerCase().contains("certificacion"));
    }

    @Test
    public void rn08_positivo_transicionValida() {
        OrdenDTO resultado = assertDoesNotThrow(() ->
                ordenService.cambiarEstadoOrden(
                        1,
                        EstadoOrden.EN_EJECUCION,
                        null,
                        null
                )
        );

        assertNotNull(resultado);
        assertEquals(EstadoOrden.EN_EJECUCION, resultado.getEstadoOrden());
    }

    @Test
    public void rn08_negativo_transicionInvalida() {
        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> ordenService.cambiarEstadoOrden(
                        2,
                        EstadoOrden.PROGRAMADA,
                        null,
                        null
                )
        );

        assertTrue(error.getMessage().toLowerCase().contains("transición")
                || error.getMessage().toLowerCase().contains("transicion"));
    }

    private OrdenDTO crearOrden(int idOrden, int idEquipo, int idTecnico, EstadoOrden estado) {
        return crearOrdenConFecha(idOrden, idEquipo, idTecnico, LocalDate.of(2026, 5, 20), estado);
    }

    private OrdenDTO crearOrdenConFecha(int idOrden, int idEquipo, int idTecnico, LocalDate fechaProgramada) {
        return crearOrdenConFecha(idOrden, idEquipo, idTecnico, fechaProgramada, EstadoOrden.PROGRAMADA);
    }

    private OrdenDTO crearOrdenConFecha(int idOrden, int idEquipo, int idTecnico,
                                        LocalDate fechaProgramada, EstadoOrden estado) {
        return new OrdenDTO(
                idOrden,
                idEquipo,
                idTecnico,
                TipoMantenimiento.PREVENTIVO,
                fechaProgramada,
                fechaProgramada,
                null,
                "Mantenimiento preventivo",
                new BigDecimal("1500.00"),
                null,
                estado
        );
    }

    private OrdenDTO crearOrdenConFechas(int idOrden, LocalDate fechaProgramada,
                                         LocalDate fechaInicio, LocalDate fechaCierre) {
        return new OrdenDTO(
                idOrden,
                1,
                1,
                TipoMantenimiento.PREVENTIVO,
                fechaProgramada,
                fechaInicio,
                fechaCierre,
                "Mantenimiento con fechas",
                new BigDecimal("2000.00"),
                new BigDecimal("2100.00"),
                EstadoOrden.FINALIZADA
        );
    }
}