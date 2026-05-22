package com.sigomei.servidor.service.unit;

import com.sigomei.api.catalogos.*;
import com.sigomei.api.dto.EquipoDTO;
import com.sigomei.api.dto.OrdenDTO;
import com.sigomei.api.dto.TecnicoDTO;
import com.sigomei.api.excepciones.ReglaNegocioException;
import com.sigomei.servidor.service.EquipoService;
import com.sigomei.servidor.service.OrdenService;
import com.sigomei.servidor.service.TecnicoService;
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
        OrdenDTO orden = crearOrdenConFecha(101, 1, 1, LocalDate.of(2026, 5, 21));

        OrdenDTO resultado = assertDoesNotThrow(() -> ordenService.registrarOrden(orden));

        assertNotNull(resultado);
        assertEquals(1, resultado.getIdEquipo());
        assertEquals(1, resultado.getIdTecnico());
    }

    @Test
    public void rn01_negativo_tecnicoIncompatible() {
        OrdenDTO orden = crearOrdenConFecha(102, 1, 2, LocalDate.of(2026, 5, 21));

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> ordenService.registrarOrden(orden)
        );

        assertTrue(error.getMessage().toLowerCase().contains("especialidad"));
    }

    @Test
    public void rn02_positivo_sinOrdenDuplicada() {
        OrdenDTO orden = crearOrdenConFecha(103, 1, 1, LocalDate.of(2026, 5, 21));

        OrdenDTO resultado = assertDoesNotThrow(() -> ordenService.registrarOrden(orden));

        assertNotNull(resultado);
        assertEquals(LocalDate.of(2026, 5, 21), resultado.getFechaProgramada());
        assertEquals(EstadoOrden.PROGRAMADA, resultado.getEstadoOrden());
    }

    @Test
    public void rn02_negativo_ordenDuplicada() {
        OrdenDTO orden = crearOrdenConFecha(104, 1, 1, LocalDate.of(2026, 5, 20));

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> ordenService.registrarOrden(orden)
        );

        assertTrue(error.getMessage().toLowerCase().contains("duplicada")
                || error.getMessage().toLowerCase().contains("activa"));
    }

    @Test
    public void rn02_positivo_permiteNuevaOrdenCuandoLaPreviaEstaFinalizada() {
        OrdenDTO orden = crearOrdenConFecha(105, 1, 1, LocalDate.of(2026, 5, 24));

        OrdenDTO resultado = assertDoesNotThrow(() -> ordenService.registrarOrden(orden));

        assertNotNull(resultado);
        assertEquals(105, resultado.getIdOrden());
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
    public void rn03_positivo_cambiarEstadoEquipoSinOrdenesRelacionadas() {
        int idEquipoSinOrdenes = 10;

        EquipoDTO resultado = assertDoesNotThrow(() ->
                equipoService.cambiarEstadoEquipo(idEquipoSinOrdenes, EstadoOperativo.INACTIVO)
        );

        assertNotNull(resultado);
        assertEquals(EstadoOperativo.INACTIVO, resultado.getEstadoOperativo());
    }

    @Test
    public void rn03_negativo_cambiarEstadoEquipoConOrdenesRelacionadas() {
        int idEquipoConOrdenes = 1;

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> equipoService.cambiarEstadoEquipo(idEquipoConOrdenes, EstadoOperativo.INACTIVO)
        );

        assertTrue(error.getMessage().toLowerCase().contains("orden"));
    }

    @Test
    public void rn04_positivo_tecnicoActivo() {
        OrdenDTO orden = crearOrdenConFecha(106, 1, 1, LocalDate.of(2026, 5, 22));

        OrdenDTO resultado = assertDoesNotThrow(() -> ordenService.registrarOrden(orden));

        assertNotNull(resultado);
        assertEquals(1, resultado.getIdTecnico());
    }

    @Test
    public void rn04_negativo_tecnicoInactivo() {
        OrdenDTO orden = crearOrdenConFecha(107, 1, 3, LocalDate.of(2026, 5, 22));

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> ordenService.registrarOrden(orden)
        );

        assertTrue(error.getMessage().toLowerCase().contains("inactivo"));
    }

    @Test
    public void rn05_positivo_fechasCorrectas() {
        OrdenDTO orden = crearOrdenConFechas(
                108,
                LocalDate.of(2026, 5, 26),
                LocalDate.of(2026, 5, 27),
                LocalDate.of(2026, 5, 28)
        );

        OrdenDTO resultado = assertDoesNotThrow(() -> ordenService.registrarOrden(orden));

        assertNotNull(resultado);
        assertFalse(resultado.getFechaInicio().isBefore(resultado.getFechaProgramada()));
        assertFalse(resultado.getFechaCierre().isBefore(resultado.getFechaInicio()));
    }

    @Test
    public void rn05_negativo_fechaInicioAntesDeProgramada() {
        OrdenDTO orden = crearOrdenConFechas(
                109,
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
    public void rn05_negativo_fechaCierreAntesDeInicio() {
        OrdenDTO orden = crearOrdenConFechas(
                110,
                LocalDate.of(2026, 5, 20),
                LocalDate.of(2026, 5, 21),
                LocalDate.of(2026, 5, 20)
        );

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> ordenService.registrarOrden(orden)
        );

        assertTrue(error.getMessage().toLowerCase().contains("cierre"));
    }

    @Test
    public void rn06_positivo_finalizarConDatosCompletos() {
        OrdenDTO resultado = assertDoesNotThrow(() ->
                ordenService.cambiarEstadoOrden(
                        2,
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
                        2,
                        EstadoOrden.FINALIZADA,
                        null,
                        null
                )
        );

        assertTrue(error.getMessage().toLowerCase().contains("cierre")
                || error.getMessage().toLowerCase().contains("costo"));
    }

    @Test
    public void rn06_negativo_finalizarSinCostoReal() {
        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> ordenService.cambiarEstadoOrden(
                        2,
                        EstadoOrden.FINALIZADA,
                        LocalDate.of(2026, 5, 25),
                        null
                )
        );

        assertTrue(error.getMessage().toLowerCase().contains("costo"));
    }

    @Test
    public void rn07_positivo_criticidadAltaConNivelII() {
        OrdenDTO orden = crearOrdenConFecha(111, 1, 1, LocalDate.of(2026, 5, 23));

        OrdenDTO resultado = assertDoesNotThrow(() -> ordenService.registrarOrden(orden));

        assertNotNull(resultado);
        assertEquals(1, resultado.getIdEquipo());
        assertEquals(1, resultado.getIdTecnico());
    }

    @Test
    public void rn07_negativo_criticidadAltaConNivelI() {
        OrdenDTO orden = crearOrdenConFecha(112, 1, 4, LocalDate.of(2026, 5, 23));

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> ordenService.registrarOrden(orden)
        );

        assertTrue(error.getMessage().toLowerCase().contains("certificacion"));
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
    public void rn08_positivo_transicionEnEjecucionAFinalizada() {
        OrdenDTO resultado = assertDoesNotThrow(() ->
                ordenService.cambiarEstadoOrden(
                        2,
                        EstadoOrden.FINALIZADA,
                        LocalDate.of(2026, 5, 25),
                        new BigDecimal("2500.00")
                )
        );

        assertEquals(EstadoOrden.FINALIZADA, resultado.getEstadoOrden());
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

        assertTrue(error.getMessage().toLowerCase().contains("transicion"));
    }

    @Test
    public void rn09_positivo_cancelarOrdenProgramada() {
        OrdenDTO resultado = assertDoesNotThrow(() ->
                ordenService.cambiarEstadoOrden(
                        1,
                        EstadoOrden.CANCELADA,
                        null,
                        null
                )
        );

        assertEquals(EstadoOrden.CANCELADA, resultado.getEstadoOrden());
    }

    @Test
    public void rn09_positivo_cancelarOrdenEnEjecucion() {
        OrdenDTO resultado = assertDoesNotThrow(() ->
                ordenService.cambiarEstadoOrden(
                        2,
                        EstadoOrden.CANCELADA,
                        null,
                        null
                )
        );

        assertEquals(EstadoOrden.CANCELADA, resultado.getEstadoOrden());
    }

    @Test
    public void rn09_negativo_cancelarOrdenFinalizada() {
        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> ordenService.cambiarEstadoOrden(
                        3,
                        EstadoOrden.CANCELADA,
                        null,
                        null
                )
        );

        assertTrue(error.getMessage().toLowerCase().contains("cancelar")
                || error.getMessage().toLowerCase().contains("estado"));
    }

    @Test
    public void rn10_positivo_registrarOrdenNoAlteraTecnicoNiEquipo() {
        TecnicoDTO tecnicoAntes = assertDoesNotThrow(() -> tecnicoService.consultarTecnicos().stream()
                .filter(tecnico -> tecnico.getIdTecnico() == 1)
                .findFirst()
                .orElseThrow());
        EquipoDTO equipoAntes = assertDoesNotThrow(() -> equipoService.consultarEquipos().stream()
                .filter(equipo -> equipo.getIdEquipo() == 1)
                .findFirst()
                .orElseThrow());
        OrdenDTO orden = crearOrdenConFecha(113, 1, 1, LocalDate.of(2026, 5, 29));

        assertDoesNotThrow(() -> ordenService.registrarOrden(orden));

        TecnicoDTO tecnicoDespues = assertDoesNotThrow(() -> tecnicoService.consultarTecnicos().stream()
                .filter(tecnico -> tecnico.getIdTecnico() == 1)
                .findFirst()
                .orElseThrow());
        EquipoDTO equipoDespues = assertDoesNotThrow(() -> equipoService.consultarEquipos().stream()
                .filter(equipo -> equipo.getIdEquipo() == 1)
                .findFirst()
                .orElseThrow());

        assertEquals(tecnicoAntes.getNombreCompleto(), tecnicoDespues.getNombreCompleto());
        assertEquals(tecnicoAntes.getEstatus(), tecnicoDespues.getEstatus());
        assertEquals(equipoAntes.getNombre(), equipoDespues.getNombre());
        assertEquals(equipoAntes.getEstadoOperativo(), equipoDespues.getEstadoOperativo());
    }

    private OrdenDTO crearOrdenConFecha(int idOrden, int idEquipo, int idTecnico, LocalDate fechaProgramada) {
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
                EstadoOrden.PROGRAMADA
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
