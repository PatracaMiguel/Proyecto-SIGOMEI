package com.sigomei.servidor.service;

import com.sigomei.api.catalogos.*;
import com.sigomei.api.dto.EquipoDTO;
import com.sigomei.api.dto.OrdenDTO;
import com.sigomei.api.dto.TecnicoDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class CrudServiceTest {

    private final EquipoService equipoService = new EquipoService();
    private final TecnicoService tecnicoService = new TecnicoService();
    private final OrdenService ordenService = new OrdenService();

    @Test
    public void crudEquipo_remotoCompatibleConContrato() throws Exception {
        EquipoDTO equipo = new EquipoDTO(20, "Turbina T20", TipoEquipo.MECANICO, "Siemens",
                "T20", "EQ-020", "Planta Central", LocalDate.of(2025, 1, 10),
                EstadoOperativo.OPERATIVO, Criticidad.MEDIA);

        EquipoDTO registrado = equipoService.registrarEquipo(equipo);
        registrado.setUbicacionPlanta("Planta Norte");
        EquipoDTO actualizado = equipoService.actualizarEquipo(registrado);
        EquipoDTO estado = equipoService.cambiarEstadoEquipo(20, EstadoOperativo.EN_MANTENIMIENTO);

        assertTrue(equipoService.consultarEquipos().stream().anyMatch(actual -> actual.getIdEquipo() == 20));
        assertEquals("Planta Norte", actualizado.getUbicacionPlanta());
        assertEquals(EstadoOperativo.EN_MANTENIMIENTO, estado.getEstadoOperativo());
    }

    @Test
    public void crudTecnico_remotoCompatibleConContrato() throws Exception {
        TecnicoDTO tecnico = new TecnicoDTO(20, "Fabian Cruz", "CUFF900101FF1", "5551000020",
                "fabian@example.com", TipoEquipo.HIDRAULICO, NivelCertificacion.II,
                LocalDate.of(2024, 1, 15), EstadoTecnico.ACTIVO);

        TecnicoDTO registrado = tecnicoService.registrarTecnico(tecnico);
        registrado.setTelefono("5559999999");
        TecnicoDTO actualizado = tecnicoService.actualizarTecnico(registrado);
        tecnicoService.eliminarTecnico(20);

        assertEquals("5559999999", actualizado.getTelefono());
        assertTrue(tecnicoService.consultarTecnicos().stream().noneMatch(actual -> actual.getIdTecnico() == 20));
    }

    @Test
    public void crudOrden_remotoCompatibleConContrato() throws Exception {
        OrdenDTO orden = new OrdenDTO(220, 2, 2, TipoMantenimiento.PREVENTIVO,
                LocalDate.of(2026, 6, 1), null, null, "Revision mensual",
                new BigDecimal("1200.00"), null, EstadoOrden.PROGRAMADA);

        OrdenDTO registrada = ordenService.registrarOrden(orden);
        registrada.setDescripcionTrabajo("Revision mensual ajustada");
        OrdenDTO actualizada = ordenService.actualizarOrden(registrada);
        OrdenDTO enEjecucion = ordenService.cambiarEstadoOrden(220, EstadoOrden.EN_EJECUCION, null, null);

        assertTrue(ordenService.consultarOrdenes().stream().anyMatch(actual -> actual.getIdOrden() == 220));
        assertEquals("Revision mensual ajustada", actualizada.getDescripcionTrabajo());
        assertEquals(EstadoOrden.EN_EJECUCION, enEjecucion.getEstadoOrden());
    }
}
