package com.sigomei.servidor.service;

import com.sigomei.api.catalogos.*;
import com.sigomei.api.dto.EquipoDTO;
import com.sigomei.api.dto.OrdenDTO;
import com.sigomei.api.dto.TecnicoDTO;
import com.sigomei.api.dto.UsuarioDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

final class InMemorySigomeiStore {

    static final Map<Integer, EquipoDTO> EQUIPOS = new LinkedHashMap<>();
    static final Map<Integer, TecnicoDTO> TECNICOS = new LinkedHashMap<>();
    static final Map<Integer, OrdenDTO> ORDENES = new LinkedHashMap<>();
    static final Map<Integer, UsuarioDTO> USUARIOS = new LinkedHashMap<>();

    static {
        reset();
    }

    private InMemorySigomeiStore() {
    }

    static synchronized void reset() {
        EQUIPOS.clear();
        TECNICOS.clear();
        ORDENES.clear();
        USUARIOS.clear();

        EQUIPOS.put(1, new EquipoDTO(1, "Compresor electrico", TipoEquipo.ELECTRICO, "Atlas",
                "AX-10", "EQ-001", "Planta Norte", LocalDate.of(2024, 1, 10),
                EstadoOperativo.OPERATIVO, Criticidad.ALTA));
        EQUIPOS.put(2, new EquipoDTO(2, "Bomba mecanica", TipoEquipo.MECANICO, "Flow",
                "BM-22", "EQ-002", "Planta Sur", LocalDate.of(2024, 2, 12),
                EstadoOperativo.OPERATIVO, Criticidad.MEDIA));
        EQUIPOS.put(10, new EquipoDTO(10, "Prensa hidraulica", TipoEquipo.HIDRAULICO, "Hydra",
                "PH-10", "EQ-010", "Planta Oeste", LocalDate.of(2024, 3, 1),
                EstadoOperativo.OPERATIVO, Criticidad.BAJA));

        TECNICOS.put(1, new TecnicoDTO(1, "Ana Lopez", "LOAA900101AA1", "5551000001",
                "ana@example.com", TipoEquipo.ELECTRICO, NivelCertificacion.II,
                LocalDate.of(2022, 1, 10), EstadoTecnico.ACTIVO));
        TECNICOS.put(2, new TecnicoDTO(2, "Bruno Ruiz", "RUBB900101BB1", "5551000002",
                "bruno@example.com", TipoEquipo.MECANICO, NivelCertificacion.II,
                LocalDate.of(2022, 2, 10), EstadoTecnico.ACTIVO));
        TECNICOS.put(3, new TecnicoDTO(3, "Carla Soto", "SOCC900101CC1", "5551000003",
                "carla@example.com", TipoEquipo.ELECTRICO, NivelCertificacion.II,
                LocalDate.of(2022, 3, 10), EstadoTecnico.INACTIVO));
        TECNICOS.put(4, new TecnicoDTO(4, "Diego Mora", "MODD900101DD1", "5551000004",
                "diego@example.com", TipoEquipo.ELECTRICO, NivelCertificacion.I,
                LocalDate.of(2022, 4, 10), EstadoTecnico.ACTIVO));
        TECNICOS.put(10, new TecnicoDTO(10, "Elena Vera", "VEEE900101EE1", "5551000010",
                "elena@example.com", TipoEquipo.HIDRAULICO, NivelCertificacion.III,
                LocalDate.of(2022, 5, 10), EstadoTecnico.ACTIVO));

        ORDENES.put(1, new OrdenDTO(1, 1, 1, TipoMantenimiento.PREVENTIVO,
                LocalDate.of(2026, 5, 20), null, null, "Orden programada base",
                new BigDecimal("1500.00"), null, EstadoOrden.PROGRAMADA));
        ORDENES.put(2, new OrdenDTO(2, 2, 2, TipoMantenimiento.CORRECTIVO,
                LocalDate.of(2026, 5, 19), LocalDate.of(2026, 5, 19), null,
                "Orden en ejecucion base", new BigDecimal("1800.00"), null,
                EstadoOrden.EN_EJECUCION));
        ORDENES.put(3, new OrdenDTO(3, 2, 2, TipoMantenimiento.PREVENTIVO,
                LocalDate.of(2026, 5, 18), LocalDate.of(2026, 5, 18),
                LocalDate.of(2026, 5, 19), "Orden finalizada base",
                new BigDecimal("1200.00"), new BigDecimal("1250.00"), EstadoOrden.FINALIZADA));

        USUARIOS.put(1, new UsuarioDTO(1, "admin", "admin123", RolUsuario.ADMINISTRADOR, EstatusUsuario.ACTIVO));
        USUARIOS.put(2, new UsuarioDTO(2, "consulta", "consulta123", RolUsuario.CONSULTOR, EstatusUsuario.ACTIVO));
        USUARIOS.put(3, new UsuarioDTO(3, "bloqueado", "bloqueado123", RolUsuario.CONSULTOR, EstatusUsuario.INACTIVO));
    }
}
