package com.sigomei.servidor.service.system;

import com.sigomei.api.catalogos.*;
import com.sigomei.api.dto.EquipoDTO;
import com.sigomei.api.dto.OrdenDTO;
import com.sigomei.api.dto.TecnicoDTO;
import com.sigomei.api.dto.UsuarioDTO;
import com.sigomei.api.excepciones.AutenticacionException;
import com.sigomei.api.excepciones.ReglaNegocioException;
import com.sigomei.api.excepciones.ValidacionException;
import com.sigomei.servidor.service.EquipoService;
import com.sigomei.servidor.service.OrdenService;
import com.sigomei.servidor.service.TecnicoService;
import com.sigomei.servidor.service.UsuarioService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.rmi.registry.LocateRegistry;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SistemaE2Test {

    private final UsuarioService usuarioService = new UsuarioService();
    private final EquipoService equipoService = new EquipoService();
    private final TecnicoService tecnicoService = new TecnicoService();
    private final OrdenService ordenService = new OrdenService();

    @Test
    public void cp01_clienteConectaYAccedeModulos() {
        UsuarioDTO usuario = assertDoesNotThrow(() -> usuarioService.iniciarSesion("admin", "admin123"));

        assertNotNull(usuario);
        assertFalse(equipoService.consultarEquipos().isEmpty());
        assertFalse(tecnicoService.consultarTecnicos().isEmpty());
        assertFalse(ordenService.consultarOrdenes().isEmpty());
    }

    @Test
    public void cp02_registrarEquipoValido() {
        EquipoDTO resultado = assertDoesNotThrow(() -> equipoService.registrarEquipo(crearEquipo(20)));

        assertEquals(20, resultado.getIdEquipo());
        assertTrue(equipoService.consultarEquipos().stream().anyMatch(equipo -> equipo.getIdEquipo() == 20));
    }

    @Test
    public void cp03_filtrarEquiposMecanicos() {
        List<EquipoDTO> resultado = equipoService.filtrarEquipos(null, TipoEquipo.MECANICO, null);

        assertFalse(resultado.isEmpty());
        assertTrue(resultado.stream().allMatch(equipo -> equipo.getTipo() == TipoEquipo.MECANICO));
    }

    @Test
    public void cp04_actualizarEquipoRegistrado() {
        EquipoDTO equipo = equipoService.consultarEquipos().stream()
            .filter(actual -> actual.getIdEquipo() == 3)
                .findFirst()
                .orElseThrow();
        equipo.setUbicacionPlanta("Planta Actualizada");

        EquipoDTO resultado = assertDoesNotThrow(() -> equipoService.actualizarEquipo(equipo));

        assertEquals("Planta Actualizada", resultado.getUbicacionPlanta());
    }

    @Test
    public void cp05_registrarTecnicoValido() {
        TecnicoDTO resultado = assertDoesNotThrow(() -> tecnicoService.registrarTecnico(crearTecnico(20)));

        assertEquals(20, resultado.getIdTecnico());
        assertTrue(tecnicoService.consultarTecnicos().stream().anyMatch(tecnico -> tecnico.getIdTecnico() == 20));
    }

    @Test
    public void cp06_filtrarTecnicosMecanicos() {
        List<TecnicoDTO> resultado = tecnicoService.filtrarTecnicos(null, TipoEquipo.MECANICO, null);

        assertFalse(resultado.isEmpty());
        assertTrue(resultado.stream().allMatch(tecnico -> tecnico.getEspecialidad() == TipoEquipo.MECANICO));
    }

    @Test
    public void cp07_actualizarTecnicoRegistrado() {
        TecnicoDTO tecnico = tecnicoService.consultarTecnicos().stream()
            .filter(actual -> actual.getIdTecnico() == 5)
                .findFirst()
                .orElseThrow();
        tecnico.setTelefono("5559999999");

        TecnicoDTO resultado = assertDoesNotThrow(() -> tecnicoService.actualizarTecnico(tecnico));

        assertEquals("5559999999", resultado.getTelefono());
    }

    @Test
    public void cp08_cambiarEstatusTecnicoActivoAInactivo() {
        TecnicoDTO resultado = assertDoesNotThrow(() ->
                tecnicoService.cambiarEstatusTecnico(4, EstadoTecnico.INACTIVO));

        assertEquals(EstadoTecnico.INACTIVO, resultado.getEstatus());
    }

    @Test
    public void cp09_eliminarTecnicoSinOrdenes() {
        assertDoesNotThrow(() -> tecnicoService.eliminarTecnico(4));

        assertTrue(tecnicoService.consultarTecnicos().stream().noneMatch(tecnico -> tecnico.getIdTecnico() == 4));
    }

    @Test
    public void cp10_registrarOrdenConEquipoYTecnicoCompatibles() {
        OrdenDTO orden = crearOrden(200, 2, 2, LocalDate.of(2026, 6, 1));

        OrdenDTO resultado = assertDoesNotThrow(() -> ordenService.registrarOrden(orden));

        assertEquals(200, resultado.getIdOrden());
    }

    @Test
    public void cp11_filtrarOrdenesProgramadas() {
        List<OrdenDTO> resultado = ordenService.filtrarOrdenes(EstadoOrden.PROGRAMADA, null, null);

        assertFalse(resultado.isEmpty());
        assertTrue(resultado.stream().allMatch(orden -> orden.getEstadoOrden() == EstadoOrden.PROGRAMADA));
    }

    @Test
    public void cp12_actualizarOrdenRegistrada() {
        OrdenDTO orden = ordenService.consultarOrdenes().stream()
                .filter(actual -> actual.getIdOrden() == 1)
                .findFirst()
                .orElseThrow();
        orden.setDescripcionTrabajo("Descripcion actualizada");

        OrdenDTO resultado = assertDoesNotThrow(() -> ordenService.actualizarOrden(orden));

        assertEquals("Descripcion actualizada", resultado.getDescripcionTrabajo());
    }

    @Test
    public void cp13_cambiarOrdenProgramadaAEnEjecucion() {
        OrdenDTO resultado = assertDoesNotThrow(() ->
                ordenService.cambiarEstadoOrden(1, EstadoOrden.EN_EJECUCION, null, null));

        assertEquals(EstadoOrden.EN_EJECUCION, resultado.getEstadoOrden());
    }

    @Test
    public void cp14_finalizarOrdenEnEjecucionConDatos() {
        OrdenDTO resultado = assertDoesNotThrow(() ->
                ordenService.cambiarEstadoOrden(2, EstadoOrden.FINALIZADA,
                        LocalDate.of(2026, 5, 25), new BigDecimal("2500.00")));

        assertEquals(EstadoOrden.FINALIZADA, resultado.getEstadoOrden());
        assertNotNull(resultado.getFechaCierre());
        assertNotNull(resultado.getCostoReal());
    }

    @Test
    public void cp15_cancelarOrdenProgramada() {
        OrdenDTO resultado = assertDoesNotThrow(() ->
                ordenService.cambiarEstadoOrden(1, EstadoOrden.CANCELADA, null, null));

        assertEquals(EstadoOrden.CANCELADA, resultado.getEstadoOrden());
    }

    @Test
    public void cp16_consultarHistorialFiltrado() {
        List<OrdenDTO> resultado = ordenService.consultarHistorialOrdenes(2, 2, EstadoOrden.EN_EJECUCION);

        assertEquals(1, resultado.size());
        assertEquals(2, resultado.get(0).getIdOrden());
    }

    @Test
    public void cp17_cambiarTecnicoSinOrdenesAInactivo() {
        TecnicoDTO resultado = assertDoesNotThrow(() ->
                tecnicoService.cambiarEstatusTecnico(5, EstadoTecnico.INACTIVO));

        assertEquals(EstadoTecnico.INACTIVO, resultado.getEstatus());
    }

    @Test
    public void cp18_cambiarEquipoSinOrdenesAInactivo() {
        EquipoDTO resultado = assertDoesNotThrow(() ->
                equipoService.cambiarEstadoEquipo(3, EstadoOperativo.INACTIVO));

        assertEquals(EstadoOperativo.INACTIVO, resultado.getEstadoOperativo());
    }

    @Test
    public void cp19_rechazarEquipoConNombreVacio() {
        EquipoDTO equipo = crearEquipo(21);
        equipo.setNombre("");

        assertThrows(ValidacionException.class, () -> equipoService.registrarEquipo(equipo));
    }

    @Test
    public void cp20_rechazarTecnicoConCorreoVacio() {
        TecnicoDTO tecnico = crearTecnico(21);
        tecnico.setCorreo("");

        assertThrows(ValidacionException.class, () -> tecnicoService.registrarTecnico(tecnico));
    }

    @Test
    public void cp21_rechazarOrdenConCampoVacio() {
        OrdenDTO orden = crearOrden(201, 2, 2, LocalDate.of(2026, 6, 2));
        orden.setDescripcionTrabajo("");

        assertThrows(ValidacionException.class, () -> ordenService.registrarOrden(orden));
    }

    @Test
    public void cp22_rechazarOrdenConTecnicoIncompatible() {
        OrdenDTO orden = crearOrden(202, 2, 1, LocalDate.of(2026, 6, 3));

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> ordenService.registrarOrden(orden));

        assertTrue(error.getMessage().toLowerCase().contains("especialidad")
                || error.getMessage().toLowerCase().contains("coincide"));
    }

    @Test
    public void cp23_rechazarOrdenDuplicadaMismoEquipoYFecha() {
        OrdenDTO orden = crearOrden(203, 1, 1, LocalDate.of(2026, 5, 20));

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> ordenService.registrarOrden(orden));

        assertTrue(error.getMessage().toLowerCase().contains("duplicada"));
    }

    @Test
    public void cp24_rechazarOrdenConTecnicoInactivo() {
        OrdenDTO orden = crearOrden(204, 1, 3, LocalDate.of(2026, 6, 4));

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> ordenService.registrarOrden(orden));

        assertTrue(error.getMessage().toLowerCase().contains("inactivo"));
    }

    @Test
    public void cp25_rechazarCriticidadAltaConCertificacionI() {
        OrdenDTO orden = crearOrden(205, 1, 4, LocalDate.of(2026, 6, 5));

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> ordenService.registrarOrden(orden));

        assertTrue(error.getMessage().toLowerCase().contains("certificacion"));
    }

    @Test
    public void cp26_rechazarFechaInicioAntesDeProgramada() {
        OrdenDTO orden = crearOrden(206, 1, 1, LocalDate.of(2026, 6, 6));
        orden.setFechaInicio(LocalDate.of(2026, 6, 5));

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> ordenService.registrarOrden(orden));

        assertTrue(error.getMessage().toLowerCase().contains("fecha"));
    }

    @Test
    public void cp27_rechazarCambioFinalizadaAProgramada() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> ordenService.cambiarEstadoOrden(3, EstadoOrden.PROGRAMADA, null, null));

        assertTrue(error.getMessage().toLowerCase().contains("transicion"));
    }

    @Test
    public void cp28_rechazarFinalizarSinFechaCierre() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> ordenService.cambiarEstadoOrden(2, EstadoOrden.FINALIZADA, null, new BigDecimal("2500.00")));

        assertTrue(error.getMessage().toLowerCase().contains("cierre"));
    }

    @Test
    public void cp29_rechazarCancelarOrdenFinalizada() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> ordenService.cambiarEstadoOrden(3, EstadoOrden.CANCELADA, null, null));

        assertTrue(error.getMessage().toLowerCase().contains("transicion"));
    }

    @Test
    public void cp30_rechazarInactivarTecnicoConOrdenesActivas() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> tecnicoService.cambiarEstatusTecnico(1, EstadoTecnico.INACTIVO));

        assertTrue(error.getMessage().toLowerCase().contains("ordenes activas"));
    }

    @Test
    public void cp31_rechazarInactivarEquipoRelacionadoAOrden() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> equipoService.cambiarEstadoEquipo(1, EstadoOperativo.INACTIVO));

        assertTrue(error.getMessage().toLowerCase().contains("orden"));
    }

    @Test
    public void cp32_mostrarErrorCuandoServidorRmiNoDisponible() {
        Exception error = assertThrows(Exception.class, () ->
                LocateRegistry.getRegistry("localhost", 65000).lookup("SIGOMEI"));

        assertNotNull(error.getMessage());
    }

    @Test
    public void cp33_rechazarTecnicoConCorreoDuplicado() {
        TecnicoDTO tecnico = crearTecnico(33);
        tecnico.setRfc("NUEV900101AA1");
        tecnico.setCorreo("ana@example.com");

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> tecnicoService.registrarTecnico(tecnico));

        assertTrue(error.getMessage().toLowerCase().contains("correo")
                || error.getMessage().toLowerCase().contains("duplic"));
    }

    @Test
    public void cp34_rechazarFinalizarOrdenCancelada() {
        assertDoesNotThrow(() -> ordenService.cambiarEstadoOrden(1, EstadoOrden.CANCELADA, null, null));

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> ordenService.cambiarEstadoOrden(1, EstadoOrden.FINALIZADA,
                        LocalDate.of(2026, 5, 25), new BigDecimal("2500.00")));

        assertTrue(error.getMessage().toLowerCase().contains("transicion"));
    }

    @Disabled("CP-35 requiere ejecucion manual: apagar el servidor durante una operacion con el cliente activo")
    @Test
    public void cp35_mostrarErrorSiServidorSeDesconectaDuranteOperacion() {
        Exception error = assertThrows(Exception.class, () ->
                LocateRegistry.getRegistry("localhost", 65000).lookup("SIGOMEI"));

        assertNotNull(error.getMessage());
    }

    private EquipoDTO crearEquipo(int idEquipo) {
        return new EquipoDTO(idEquipo, "Equipo " + idEquipo, TipoEquipo.MECANICO, "Marca",
                "Modelo", "SERIE-" + idEquipo, "Planta", LocalDate.of(2025, 1, 10),
                EstadoOperativo.OPERATIVO, Criticidad.MEDIA);
    }

    private TecnicoDTO crearTecnico(int idTecnico) {
        return new TecnicoDTO(idTecnico, "Tecnico " + idTecnico, "RFC" + idTecnico + "900101",
                "55510000" + idTecnico, "tecnico" + idTecnico + "@example.com",
                TipoEquipo.HIDRAULICO, NivelCertificacion.II, LocalDate.of(2024, 1, 15),
                EstadoTecnico.ACTIVO);
    }

    private OrdenDTO crearOrden(int idOrden, int idEquipo, int idTecnico, LocalDate fechaProgramada) {
        return new OrdenDTO(idOrden, idEquipo, idTecnico, TipoMantenimiento.PREVENTIVO,
                fechaProgramada, null, null, "Trabajo de sistema",
                new BigDecimal("1200.00"), null, EstadoOrden.PROGRAMADA);
    }
}
