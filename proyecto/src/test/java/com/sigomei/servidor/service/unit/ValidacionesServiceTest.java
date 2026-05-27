package com.sigomei.servidor.service.unit;

import com.sigomei.api.catalogos.Criticidad;
import com.sigomei.api.catalogos.EstadoOperativo;
import com.sigomei.api.catalogos.EstadoOrden;
import com.sigomei.api.catalogos.EstadoTecnico;
import com.sigomei.api.catalogos.NivelCertificacion;
import com.sigomei.api.catalogos.TipoEquipo;
import com.sigomei.api.catalogos.TipoMantenimiento;
import com.sigomei.api.dto.EquipoDTO;
import com.sigomei.api.dto.OrdenDTO;
import com.sigomei.api.dto.TecnicoDTO;
import com.sigomei.api.excepciones.AutenticacionException;
import com.sigomei.api.excepciones.ReglaNegocioException;
import com.sigomei.api.excepciones.RegistroNoEncontradoException;
import com.sigomei.api.excepciones.ValidacionException;
import com.sigomei.servidor.service.EquipoService;
import com.sigomei.servidor.service.OrdenService;
import com.sigomei.servidor.service.TecnicoService;
import com.sigomei.servidor.service.UsuarioService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class ValidacionesServiceTest {

    @Test
    public void equipo_validaTodosLosCamposObligatorios() {
        EquipoService service = new EquipoService();

        assertThrows(ValidacionException.class, () -> service.registrarEquipo(null));
        assertThrows(ValidacionException.class, () -> service.registrarEquipo(equipo(0, "Nuevo", "SER-01")));
        assertThrows(ValidacionException.class, () -> service.registrarEquipo(equipo(20, "", "SER-01")));
        assertThrows(ValidacionException.class, () -> service.registrarEquipo(new EquipoDTO(
                20, "Nuevo", null, "Marca", "Modelo", "SER-01", "Planta",
                LocalDate.of(2026, 1, 1), EstadoOperativo.OPERATIVO, Criticidad.MEDIA)));
        assertThrows(ValidacionException.class, () -> service.registrarEquipo(new EquipoDTO(
                20, "Nuevo", TipoEquipo.MECANICO, "", "Modelo", "SER-01", "Planta",
                LocalDate.of(2026, 1, 1), EstadoOperativo.OPERATIVO, Criticidad.MEDIA)));
        assertThrows(ValidacionException.class, () -> service.registrarEquipo(new EquipoDTO(
                20, "Nuevo", TipoEquipo.MECANICO, "Marca", "", "SER-01", "Planta",
                LocalDate.of(2026, 1, 1), EstadoOperativo.OPERATIVO, Criticidad.MEDIA)));
        assertThrows(ValidacionException.class, () -> service.registrarEquipo(new EquipoDTO(
                20, "Nuevo", TipoEquipo.MECANICO, "Marca", "Modelo", "", "Planta",
                LocalDate.of(2026, 1, 1), EstadoOperativo.OPERATIVO, Criticidad.MEDIA)));
        assertThrows(ValidacionException.class, () -> service.registrarEquipo(new EquipoDTO(
                20, "Nuevo", TipoEquipo.MECANICO, "Marca", "Modelo", "SER-01", "",
                LocalDate.of(2026, 1, 1), EstadoOperativo.OPERATIVO, Criticidad.MEDIA)));
        assertThrows(ValidacionException.class, () -> service.registrarEquipo(new EquipoDTO(
                20, "Nuevo", TipoEquipo.MECANICO, "Marca", "Modelo", "SER-01", "Planta",
                null, EstadoOperativo.OPERATIVO, Criticidad.MEDIA)));
        assertThrows(ValidacionException.class, () -> service.registrarEquipo(new EquipoDTO(
                20, "Nuevo", TipoEquipo.MECANICO, "Marca", "Modelo", "SER-01", "Planta",
                LocalDate.of(2026, 1, 1), null, Criticidad.MEDIA)));
        assertThrows(ValidacionException.class, () -> service.registrarEquipo(new EquipoDTO(
                20, "Nuevo", TipoEquipo.MECANICO, "Marca", "Modelo", "SER-01", "Planta",
                LocalDate.of(2026, 1, 1), EstadoOperativo.OPERATIVO, null)));
    }

    @Test
    public void equipo_filtraValidaDuplicadosYRegistrosNoEncontrados() throws Exception {
        EquipoService service = new EquipoService();

        assertEquals(3, service.filtrarEquipos(null, null, null).size());
        assertEquals(3, service.filtrarEquipos("   ", null, null).size());
        assertEquals(1, service.filtrarEquipos("bomba", TipoEquipo.MECANICO, Criticidad.MEDIA).size());
        assertEquals(0, service.filtrarEquipos("no existe", TipoEquipo.HIDRAULICO, Criticidad.ALTA).size());

        assertThrows(ReglaNegocioException.class, () -> service.registrarEquipo(equipo(20, "Duplicado", "EQ-001")));
        assertThrows(RegistroNoEncontradoException.class, () -> service.actualizarEquipo(null));
        assertThrows(RegistroNoEncontradoException.class, () -> service.actualizarEquipo(equipo(999, "No existe", "SER-999")));
        assertThrows(RegistroNoEncontradoException.class, () -> service.cambiarEstadoEquipo(999, EstadoOperativo.INACTIVO));
        assertThrows(RegistroNoEncontradoException.class, () -> service.eliminarEquipo(999));
        assertThrows(ReglaNegocioException.class, () -> service.eliminarEquipo(1));

        EquipoDTO activo = service.cambiarEstadoEquipo(10, EstadoOperativo.OPERATIVO);
        assertEquals(EstadoOperativo.OPERATIVO, activo.getEstadoOperativo());
    }

    @Test
    public void tecnico_validaTodosLosCamposObligatorios() {
        TecnicoService service = new TecnicoService();

        assertThrows(ValidacionException.class, () -> service.registrarTecnico(null));
        assertThrows(ValidacionException.class, () -> service.registrarTecnico(tecnico(0, "Nuevo", "RFC01", "nuevo@mail.com")));
        assertThrows(ValidacionException.class, () -> service.registrarTecnico(tecnico(20, "", "RFC01", "nuevo@mail.com")));
        assertThrows(ValidacionException.class, () -> service.registrarTecnico(new TecnicoDTO(
                20, "Nuevo", "", "555", "nuevo@mail.com", TipoEquipo.MECANICO,
                NivelCertificacion.II, LocalDate.of(2026, 1, 1), EstadoTecnico.ACTIVO)));
        assertThrows(ValidacionException.class, () -> service.registrarTecnico(new TecnicoDTO(
                20, "Nuevo", "RFC01", "", "nuevo@mail.com", TipoEquipo.MECANICO,
                NivelCertificacion.II, LocalDate.of(2026, 1, 1), EstadoTecnico.ACTIVO)));
        assertThrows(ValidacionException.class, () -> service.registrarTecnico(new TecnicoDTO(
                20, "Nuevo", "RFC01", "555", "", TipoEquipo.MECANICO,
                NivelCertificacion.II, LocalDate.of(2026, 1, 1), EstadoTecnico.ACTIVO)));
        assertThrows(ValidacionException.class, () -> service.registrarTecnico(new TecnicoDTO(
                20, "Nuevo", "RFC01", "555", "nuevo@mail.com", null,
                NivelCertificacion.II, LocalDate.of(2026, 1, 1), EstadoTecnico.ACTIVO)));
        assertThrows(ValidacionException.class, () -> service.registrarTecnico(new TecnicoDTO(
                20, "Nuevo", "RFC01", "555", "nuevo@mail.com", TipoEquipo.MECANICO,
                null, LocalDate.of(2026, 1, 1), EstadoTecnico.ACTIVO)));
        assertThrows(ValidacionException.class, () -> service.registrarTecnico(new TecnicoDTO(
                20, "Nuevo", "RFC01", "555", "nuevo@mail.com", TipoEquipo.MECANICO,
                NivelCertificacion.II, null, EstadoTecnico.ACTIVO)));
        assertThrows(ValidacionException.class, () -> service.registrarTecnico(new TecnicoDTO(
                20, "Nuevo", "RFC01", "555", "nuevo@mail.com", TipoEquipo.MECANICO,
                NivelCertificacion.II, LocalDate.of(2026, 1, 1), null)));
    }

    @Test
    public void tecnico_filtraValidaDuplicadosYRegistrosNoEncontrados() throws Exception {
        TecnicoService service = new TecnicoService();

        assertEquals(5, service.filtrarTecnicos(null, null, null).size());
        assertEquals(5, service.filtrarTecnicos("   ", null, null).size());
        assertEquals(1, service.filtrarTecnicos("ana", TipoEquipo.ELECTRICO, NivelCertificacion.II).size());
        assertEquals(0, service.filtrarTecnicos("no existe", TipoEquipo.HIDRAULICO, NivelCertificacion.III).size());

        assertThrows(ReglaNegocioException.class, () -> service.registrarTecnico(tecnico(20, "Duplicado", "LOAA900101AA1", "otro@mail.com")));
        assertThrows(ReglaNegocioException.class, () -> service.registrarTecnico(tecnico(21, "Duplicado", "RFC21", "ana@example.com")));
        assertThrows(RegistroNoEncontradoException.class, () -> service.actualizarTecnico(null));
        assertThrows(RegistroNoEncontradoException.class, () -> service.actualizarTecnico(tecnico(999, "No existe", "RFC999", "x@mail.com")));
        assertThrows(RegistroNoEncontradoException.class, () -> service.cambiarEstatusTecnico(999, EstadoTecnico.INACTIVO));
        assertThrows(RegistroNoEncontradoException.class, () -> service.eliminarTecnico(999));
        assertThrows(ReglaNegocioException.class, () -> service.cambiarEstatusTecnico(1, EstadoTecnico.INACTIVO));

        TecnicoDTO activo = service.cambiarEstatusTecnico(10, EstadoTecnico.ACTIVO);
        assertEquals(EstadoTecnico.ACTIVO, activo.getEstatus());
    }

    @Test
    public void orden_validaCamposObligatoriosYNoEncontrados() {
        OrdenService service = new OrdenService();

        assertThrows(ValidacionException.class, () -> service.registrarOrden(null));
        assertThrows(ValidacionException.class, () -> service.registrarOrden(orden(0, 1, 1, LocalDate.of(2026, 6, 1))));
        assertThrows(ValidacionException.class, () -> service.registrarOrden(orden(20, 0, 1, LocalDate.of(2026, 6, 1))));
        assertThrows(ValidacionException.class, () -> service.registrarOrden(orden(20, 1, 0, LocalDate.of(2026, 6, 1))));
        assertThrows(ValidacionException.class, () -> service.registrarOrden(new OrdenDTO(
                20, 1, 1, null, LocalDate.of(2026, 6, 1), null, null, "Trabajo",
                new BigDecimal("100.00"), null, EstadoOrden.PROGRAMADA)));
        assertThrows(ValidacionException.class, () -> service.registrarOrden(new OrdenDTO(
                20, 1, 1, TipoMantenimiento.PREVENTIVO, null, null, null, "Trabajo",
                new BigDecimal("100.00"), null, EstadoOrden.PROGRAMADA)));
        assertThrows(ValidacionException.class, () -> service.registrarOrden(new OrdenDTO(
                20, 1, 1, TipoMantenimiento.PREVENTIVO, LocalDate.of(2026, 6, 1), null, null, "Trabajo",
                new BigDecimal("100.00"), null, null)));
        assertThrows(ValidacionException.class, () -> service.registrarOrden(new OrdenDTO(
                20, 1, 1, TipoMantenimiento.PREVENTIVO, LocalDate.of(2026, 6, 1), null, null, "Trabajo",
                null, null, EstadoOrden.PROGRAMADA)));
        assertThrows(ValidacionException.class, () -> service.registrarOrden(new OrdenDTO(
                20, 1, 1, TipoMantenimiento.PREVENTIVO, LocalDate.of(2026, 6, 1), null, null, "",
                new BigDecimal("100.00"), null, EstadoOrden.PROGRAMADA)));

        assertThrows(RegistroNoEncontradoException.class, () -> service.registrarOrden(orden(20, 999, 1, LocalDate.of(2026, 6, 1))));
        assertThrows(RegistroNoEncontradoException.class, () -> service.registrarOrden(orden(20, 1, 999, LocalDate.of(2026, 6, 1))));
        assertThrows(RegistroNoEncontradoException.class, () -> service.actualizarOrden(null));
        assertThrows(RegistroNoEncontradoException.class, () -> service.actualizarOrden(orden(999, 1, 1, LocalDate.of(2026, 6, 1))));
        assertThrows(RegistroNoEncontradoException.class, () -> service.cambiarEstadoOrden(999, EstadoOrden.CANCELADA, null, null));
        assertThrows(RegistroNoEncontradoException.class, () -> service.eliminarOrden(999));
    }

    @Test
    public void orden_filtraHistorialYValidaDatosDeFinalizacion() throws Exception {
        OrdenService service = new OrdenService();

        assertEquals(3, service.filtrarOrdenes(null, null, null).size());
        assertEquals(1, service.filtrarOrdenes(EstadoOrden.PROGRAMADA, LocalDate.of(2026, 5, 20), LocalDate.of(2026, 5, 20)).size());
        assertEquals(0, service.filtrarOrdenes(EstadoOrden.CANCELADA, LocalDate.of(2026, 5, 19), LocalDate.of(2026, 5, 21)).size());

        assertEquals(3, service.consultarHistorialOrdenes(null, null, null).size());
        assertEquals(1, service.consultarHistorialOrdenes(1, 1, EstadoOrden.PROGRAMADA).size());
        assertEquals(0, service.consultarHistorialOrdenes(99, 99, EstadoOrden.CANCELADA).size());

        assertThrows(ReglaNegocioException.class, () -> service.registrarOrden(new OrdenDTO(
                20, 1, 1, TipoMantenimiento.PREVENTIVO, LocalDate.of(2026, 6, 1), null,
                LocalDate.of(2026, 6, 2), "Trabajo", new BigDecimal("100.00"), null,
                EstadoOrden.PROGRAMADA)));
        assertThrows(ReglaNegocioException.class, () -> service.registrarOrden(new OrdenDTO(
                21, 1, 1, TipoMantenimiento.PREVENTIVO, LocalDate.of(2026, 6, 2), null,
                null, "Trabajo", new BigDecimal("100.00"), new BigDecimal("90.00"),
                EstadoOrden.PROGRAMADA)));

        OrdenDTO enEjecucion = service.cambiarEstadoOrden(1, EstadoOrden.EN_EJECUCION, null, null);
        assertNotNull(enEjecucion.getFechaInicio());

        service.eliminarOrden(3);
        assertEquals(2, service.consultarOrdenes().size());
    }

    @Test
    public void usuario_cubreAutenticacionCorrectaEIncorrecta() {
        UsuarioService service = new UsuarioService();

        assertDoesNotThrow(() -> service.iniciarSesion("admin", "admin123"));
        assertDoesNotThrow(() -> service.iniciarSesion("consulta", "consulta123"));
        assertThrows(AutenticacionException.class, () -> service.iniciarSesion("noexiste", "admin123"));
        assertThrows(AutenticacionException.class, () -> service.iniciarSesion("admin", "incorrecta"));
        assertDoesNotThrow(() -> service.cerrarSesion(1));
    }

    private EquipoDTO equipo(int id, String nombre, String serie) {
        return new EquipoDTO(id, nombre, TipoEquipo.MECANICO, "Marca", "Modelo", serie, "Planta",
                LocalDate.of(2026, 1, 1), EstadoOperativo.OPERATIVO, Criticidad.MEDIA);
    }

    private TecnicoDTO tecnico(int id, String nombre, String rfc, String correo) {
        return new TecnicoDTO(id, nombre, rfc, "5551234567", correo, TipoEquipo.MECANICO,
                NivelCertificacion.II, LocalDate.of(2026, 1, 1), EstadoTecnico.ACTIVO);
    }

    private OrdenDTO orden(int id, int idEquipo, int idTecnico, LocalDate fechaProgramada) {
        return new OrdenDTO(id, idEquipo, idTecnico, TipoMantenimiento.PREVENTIVO,
                fechaProgramada, null, null, "Trabajo", new BigDecimal("100.00"),
                null, EstadoOrden.PROGRAMADA);
    }
}
