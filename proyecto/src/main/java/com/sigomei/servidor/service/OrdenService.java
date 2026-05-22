package com.sigomei.servidor.service;

import com.sigomei.api.catalogos.Criticidad;
import com.sigomei.api.catalogos.EstadoOrden;
import com.sigomei.api.catalogos.EstadoTecnico;
import com.sigomei.api.catalogos.NivelCertificacion;
import com.sigomei.api.dto.EquipoDTO;
import com.sigomei.api.dto.OrdenDTO;
import com.sigomei.api.dto.TecnicoDTO;
import com.sigomei.api.excepciones.ReglaNegocioException;
import com.sigomei.api.excepciones.RegistroNoEncontradoException;
import com.sigomei.api.excepciones.ValidacionException;
import com.sigomei.servidor.config.ServerLog;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrdenService {

    public OrdenService() {
        InMemorySigomeiStore.reset();
    }

    public OrdenDTO registrarOrden(OrdenDTO orden)
            throws ValidacionException, ReglaNegocioException, RegistroNoEncontradoException {

        validarOrdenBasica(orden);

        EquipoDTO equipo = buscarEquipo(orden.getIdEquipo());
        TecnicoDTO tecnico = buscarTecnico(orden.getIdTecnico());

        validarTecnicoCompatible(equipo, tecnico);
        validarTecnicoActivo(tecnico);
        validarCertificacionParaCriticidad(equipo, tecnico);
        validarFechas(orden);
        validarFinalizacion(orden.getEstadoOrden(), orden.getFechaCierre(), orden.getCostoReal());
        validarOrdenActivaDuplicada(orden);

        InMemorySigomeiStore.ORDENES.put(orden.getIdOrden(), copiar(orden));
        ServerLog.info("Orden registrada id=" + orden.getIdOrden());
        return copiar(orden);
    }

    public List<OrdenDTO> consultarOrdenes() {
        List<OrdenDTO> ordenes = new ArrayList<>();
        for (OrdenDTO orden : InMemorySigomeiStore.ORDENES.values()) {
            ordenes.add(copiar(orden));
        }
        ServerLog.info("Consulta de ordenes");
        return ordenes;
    }

    public List<OrdenDTO> filtrarOrdenes(EstadoOrden estado, LocalDate fechaInicio, LocalDate fechaCierre) {
        List<OrdenDTO> resultado = new ArrayList<>();

        for (OrdenDTO orden : InMemorySigomeiStore.ORDENES.values()) {
            boolean coincideEstado = estado == null || orden.getEstadoOrden() == estado;
            boolean despuesDeInicio = fechaInicio == null || !orden.getFechaProgramada().isBefore(fechaInicio);
            boolean antesDeCierre = fechaCierre == null || !orden.getFechaProgramada().isAfter(fechaCierre);

            if (coincideEstado && despuesDeInicio && antesDeCierre) {
                resultado.add(copiar(orden));
            }
        }

        ServerLog.info("Filtro de ordenes estado=" + estado);
        return resultado;
    }

    public OrdenDTO actualizarOrden(OrdenDTO orden)
            throws ValidacionException, ReglaNegocioException, RegistroNoEncontradoException {

        if (orden == null || !InMemorySigomeiStore.ORDENES.containsKey(orden.getIdOrden())) {
            throw new RegistroNoEncontradoException("Orden no encontrada");
        }

        validarOrdenBasica(orden);
        buscarEquipo(orden.getIdEquipo());
        buscarTecnico(orden.getIdTecnico());
        validarFechas(orden);
        validarFinalizacion(orden.getEstadoOrden(), orden.getFechaCierre(), orden.getCostoReal());
        validarOrdenActivaDuplicada(orden);

        InMemorySigomeiStore.ORDENES.put(orden.getIdOrden(), copiar(orden));
        ServerLog.info("Orden actualizada id=" + orden.getIdOrden());
        return copiar(orden);
    }

    public OrdenDTO cambiarEstadoOrden(int idOrden, EstadoOrden nuevoEstado, LocalDate fechaCierre, BigDecimal costoReal)
            throws ReglaNegocioException, RegistroNoEncontradoException {

        OrdenDTO orden = InMemorySigomeiStore.ORDENES.get(idOrden);
        if (orden == null) {
            throw new RegistroNoEncontradoException("Orden no encontrada");
        }

        if (!esTransicionValida(orden.getEstadoOrden(), nuevoEstado)) {
            ServerLog.warning("Transicion rechazada idOrden=" + idOrden + " nuevoEstado=" + nuevoEstado);
            throw new ReglaNegocioException("Transicion de estado no permitida");
        }

        validarFinalizacion(nuevoEstado, fechaCierre, costoReal);

        OrdenDTO actualizada = copiar(orden);
        actualizada.setEstadoOrden(nuevoEstado);

        if (nuevoEstado == EstadoOrden.EN_EJECUCION && actualizada.getFechaInicio() == null) {
            actualizada.setFechaInicio(LocalDate.now());
        }

        if (nuevoEstado == EstadoOrden.FINALIZADA) {
            actualizada.setFechaCierre(fechaCierre);
            actualizada.setCostoReal(costoReal);
        }

        InMemorySigomeiStore.ORDENES.put(idOrden, actualizada);
        ServerLog.info("Estado de orden actualizado id=" + idOrden + " estado=" + nuevoEstado);
        return copiar(actualizada);
    }

    public List<OrdenDTO> consultarHistorialOrdenes(Integer idEquipo, Integer idTecnico, EstadoOrden estadoOrden) {
        List<OrdenDTO> resultado = new ArrayList<>();

        for (OrdenDTO orden : InMemorySigomeiStore.ORDENES.values()) {
            boolean coincideEquipo = idEquipo == null || orden.getIdEquipo() == idEquipo;
            boolean coincideTecnico = idTecnico == null || orden.getIdTecnico() == idTecnico;
            boolean coincideEstado = estadoOrden == null || orden.getEstadoOrden() == estadoOrden;

            if (coincideEquipo && coincideTecnico && coincideEstado) {
                resultado.add(copiar(orden));
            }
        }

        ServerLog.info("Consulta historial ordenes");
        return resultado;
    }

    private void validarOrdenBasica(OrdenDTO orden) throws ValidacionException {
        if (orden == null) {
            throw new ValidacionException("La orden tiene datos obligatorios incompletos");
        }
        if (orden.getIdOrden() <= 0 || orden.getIdEquipo() <= 0 || orden.getIdTecnico() <= 0
                || orden.getTipoMantenimiento() == null || orden.getFechaProgramada() == null
                || orden.getEstadoOrden() == null || orden.getCostoEstimado() == null
                || esBlanco(orden.getDescripcionTrabajo())) {
            throw new ValidacionException("La orden tiene datos obligatorios incompletos");
        }
    }

    private EquipoDTO buscarEquipo(int idEquipo) throws RegistroNoEncontradoException {
        EquipoDTO equipo = InMemorySigomeiStore.EQUIPOS.get(idEquipo);
        if (equipo == null) {
            throw new RegistroNoEncontradoException("Equipo no encontrado");
        }
        return equipo;
    }

    private TecnicoDTO buscarTecnico(int idTecnico) throws RegistroNoEncontradoException {
        TecnicoDTO tecnico = InMemorySigomeiStore.TECNICOS.get(idTecnico);
        if (tecnico == null) {
            throw new RegistroNoEncontradoException("Tecnico no encontrado");
        }
        return tecnico;
    }

    private void validarTecnicoCompatible(EquipoDTO equipo, TecnicoDTO tecnico) throws ReglaNegocioException {
        if (equipo.getTipo() != tecnico.getEspecialidad()) {
            ServerLog.warning("Orden rechazada RN-01: tecnico incompatible con equipo");
            throw new ReglaNegocioException("La especialidad del tecnico no coincide con el equipo");
        }
    }

    private void validarTecnicoActivo(TecnicoDTO tecnico) throws ReglaNegocioException {
        if (tecnico.getEstatus() != EstadoTecnico.ACTIVO) {
            ServerLog.warning("Orden rechazada RN-04: tecnico inactivo id=" + tecnico.getIdTecnico());
            throw new ReglaNegocioException("El tecnico esta inactivo");
        }
    }

    private void validarCertificacionParaCriticidad(EquipoDTO equipo, TecnicoDTO tecnico) throws ReglaNegocioException {
        boolean equipoAltaCriticidad = equipo.getCriticidad() == Criticidad.ALTA;
        boolean tecnicoNivelUno = tecnico.getNivelCertificacion() == NivelCertificacion.I;

        if (equipoAltaCriticidad && tecnicoNivelUno) {
            ServerLog.warning("Orden rechazada RN-07: certificacion insuficiente para criticidad alta");
            throw new ReglaNegocioException("La criticidad alta requiere certificacion nivel II o III");
        }
    }

    private void validarFechas(OrdenDTO orden) throws ReglaNegocioException {
        if (orden.getFechaInicio() != null && orden.getFechaInicio().isBefore(orden.getFechaProgramada())) {
            ServerLog.warning("Orden rechazada RN-05: fecha de inicio anterior a programada");
            throw new ReglaNegocioException("La fecha de inicio no respeta el orden cronologico");
        }

        if (orden.getFechaCierre() != null && orden.getFechaInicio() != null
                && orden.getFechaCierre().isBefore(orden.getFechaInicio())) {
            ServerLog.warning("Orden rechazada RN-05: fecha de cierre anterior a inicio");
            throw new ReglaNegocioException("La fecha de cierre no respeta el orden cronologico");
        }
    }

    private void validarFinalizacion(EstadoOrden estado, LocalDate fechaCierre, BigDecimal costoReal)
            throws ReglaNegocioException {

        if (estado == EstadoOrden.FINALIZADA && (fechaCierre == null || costoReal == null)) {
            ServerLog.warning("Orden rechazada RN-06: finalizacion sin fecha de cierre o costo real");
            throw new ReglaNegocioException("La fecha de cierre y el costo real son obligatorios para finalizar");
        }

        if (estado != EstadoOrden.FINALIZADA && (fechaCierre != null || costoReal != null)) {
            ServerLog.warning("Orden rechazada RN-06: datos de cierre en orden no finalizada");
            throw new ReglaNegocioException("Solo una orden finalizada puede tener fecha de cierre y costo real");
        }
    }

    private void validarOrdenActivaDuplicada(OrdenDTO nuevaOrden) throws ReglaNegocioException {
        for (OrdenDTO orden : InMemorySigomeiStore.ORDENES.values()) {
            boolean distintaOrden = orden.getIdOrden() != nuevaOrden.getIdOrden();
            boolean mismoEquipo = orden.getIdEquipo() == nuevaOrden.getIdEquipo();
            boolean mismaFecha = orden.getFechaProgramada().equals(nuevaOrden.getFechaProgramada());

            if (distintaOrden && mismoEquipo && mismaFecha
                    && esActiva(orden.getEstadoOrden()) && esActiva(nuevaOrden.getEstadoOrden())) {
                ServerLog.warning("Orden rechazada RN-02: duplicada para equipo=" + nuevaOrden.getIdEquipo()
                        + " fecha=" + nuevaOrden.getFechaProgramada());
                throw new ReglaNegocioException("Ya existe una orden activa duplicada para el equipo y fecha");
            }
        }
    }

    private boolean esTransicionValida(EstadoOrden actual, EstadoOrden nuevo) {
        if (actual == EstadoOrden.PROGRAMADA) {
            return nuevo == EstadoOrden.EN_EJECUCION || nuevo == EstadoOrden.CANCELADA;
        }
        if (actual == EstadoOrden.EN_EJECUCION) {
            return nuevo == EstadoOrden.FINALIZADA || nuevo == EstadoOrden.CANCELADA;
        }
        return false;
    }

    private boolean esActiva(EstadoOrden estado) {
        return estado == EstadoOrden.PROGRAMADA || estado == EstadoOrden.EN_EJECUCION;
    }

    private boolean esBlanco(String valor) {
        return valor == null || valor.isBlank();
    }

    private OrdenDTO copiar(OrdenDTO orden) {
        return new OrdenDTO(orden.getIdOrden(), orden.getIdEquipo(), orden.getIdTecnico(),
                orden.getTipoMantenimiento(), orden.getFechaProgramada(), orden.getFechaInicio(),
                orden.getFechaCierre(), orden.getDescripcionTrabajo(), orden.getCostoEstimado(),
                orden.getCostoReal(), orden.getEstadoOrden());
    }
}
