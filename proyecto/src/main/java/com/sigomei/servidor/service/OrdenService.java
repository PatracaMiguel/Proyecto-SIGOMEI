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
import com.sigomei.servidor.repository.EquipoRepository;
import com.sigomei.servidor.repository.OrdenRepository;
import com.sigomei.servidor.repository.TecnicoRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrdenService {

    private final OrdenRepository ordenRepository;
    private final EquipoRepository equipoRepository;
    private final TecnicoRepository tecnicoRepository;

    public OrdenService() {
        InMemorySigomeiStore.reset();
        this.ordenRepository = null;
        this.equipoRepository = null;
        this.tecnicoRepository = null;
    }

    public OrdenService(OrdenRepository ordenRepository, EquipoRepository equipoRepository,
                        TecnicoRepository tecnicoRepository) {
        this.ordenRepository = ordenRepository;
        this.equipoRepository = equipoRepository;
        this.tecnicoRepository = tecnicoRepository;
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

        OrdenDTO guardada;
        if (ordenRepository == null) {
            InMemorySigomeiStore.ORDENES.put(orden.getIdOrden(), copiar(orden));
            guardada = copiar(orden);
        } else {
            guardada = ordenRepository.guardar(orden);
        }
        ServerLog.info("Orden registrada id=" + orden.getIdOrden());
        return copiar(guardada);
    }

    public List<OrdenDTO> consultarOrdenes() {
        if (ordenRepository != null) {
            ServerLog.info("Consulta de ordenes");
            return ordenRepository.consultarTodas();
        }
        List<OrdenDTO> ordenes = new ArrayList<>();
        for (OrdenDTO orden : InMemorySigomeiStore.ORDENES.values()) {
            ordenes.add(copiar(orden));
        }
        ServerLog.info("Consulta de ordenes");
        return ordenes;
    }

    public List<OrdenDTO> filtrarOrdenes(EstadoOrden estado, LocalDate fechaInicio, LocalDate fechaCierre) {
        if (ordenRepository != null) {
            ServerLog.info("Filtro de ordenes estado=" + estado);
            return ordenRepository.filtrar(estado, fechaInicio, fechaCierre);
        }
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

        OrdenDTO existente = orden == null ? null : buscarOrdenPorId(orden.getIdOrden());
        if (existente == null) {
            throw new RegistroNoEncontradoException("Orden no encontrada");
        }

        validarOrdenBasica(orden);
        EquipoDTO equipo = buscarEquipo(orden.getIdEquipo());
        TecnicoDTO tecnico = buscarTecnico(orden.getIdTecnico());
        validarTecnicoCompatible(equipo, tecnico);
        validarTecnicoActivo(tecnico);
        validarCertificacionParaCriticidad(equipo, tecnico);
        if (existente.getEstadoOrden() != orden.getEstadoOrden()
                && !esTransicionValida(existente.getEstadoOrden(), orden.getEstadoOrden())) {
            ServerLog.warning("Transicion rechazada idOrden=" + orden.getIdOrden()
                    + " nuevoEstado=" + orden.getEstadoOrden());
            throw new ReglaNegocioException("Transicion de estado no permitida");
        }
        validarFechas(orden);
        validarFinalizacion(orden.getEstadoOrden(), orden.getFechaCierre(), orden.getCostoReal());
        validarOrdenActivaDuplicada(orden);

        if (existente.getEstadoOrden() != orden.getEstadoOrden()
                && orden.getEstadoOrden() == EstadoOrden.EN_EJECUCION
                && orden.getFechaInicio() == null) {
            orden.setFechaInicio(LocalDate.now());
        }

        OrdenDTO actualizada;
        if (ordenRepository == null) {
            InMemorySigomeiStore.ORDENES.put(orden.getIdOrden(), copiar(orden));
            actualizada = copiar(orden);
        } else {
            actualizada = ordenRepository.actualizar(orden);
        }
        ServerLog.info("Orden actualizada id=" + orden.getIdOrden());
        return copiar(actualizada);
    }

    public OrdenDTO cambiarEstadoOrden(int idOrden, EstadoOrden nuevoEstado, LocalDate fechaCierre, BigDecimal costoReal)
            throws ReglaNegocioException, RegistroNoEncontradoException {

        OrdenDTO orden = buscarOrdenPorId(idOrden);
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

        if (ordenRepository == null) {
            InMemorySigomeiStore.ORDENES.put(idOrden, actualizada);
        } else {
            actualizada = ordenRepository.actualizar(actualizada);
        }
        ServerLog.info("Estado de orden actualizado id=" + idOrden + " estado=" + nuevoEstado);
        return copiar(actualizada);
    }

    public List<OrdenDTO> consultarHistorialOrdenes(Integer idEquipo, Integer idTecnico, EstadoOrden estadoOrden) {
        if (ordenRepository != null) {
            ServerLog.info("Consulta historial ordenes");
            return ordenRepository.consultarHistorial(idEquipo, idTecnico, estadoOrden);
        }
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

    public void eliminarOrden(int idOrden) throws RegistroNoEncontradoException {
        if (buscarOrdenPorId(idOrden) == null) {
            throw new RegistroNoEncontradoException("Orden no encontrada");
        }

        if (ordenRepository == null) {
            InMemorySigomeiStore.ORDENES.remove(idOrden);
        } else {
            ordenRepository.eliminar(idOrden);
        }
        ServerLog.info("Orden eliminada id=" + idOrden);
    }

    private OrdenDTO buscarOrdenPorId(int idOrden) {
        return ordenRepository == null ? InMemorySigomeiStore.ORDENES.get(idOrden) : ordenRepository.buscarPorId(idOrden);
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
        EquipoDTO equipo = equipoRepository == null
                ? InMemorySigomeiStore.EQUIPOS.get(idEquipo)
                : equipoRepository.buscarPorId(idEquipo);
        if (equipo == null) {
            throw new RegistroNoEncontradoException("Equipo no encontrado");
        }
        return equipo;
    }

    private TecnicoDTO buscarTecnico(int idTecnico) throws RegistroNoEncontradoException {
        TecnicoDTO tecnico = tecnicoRepository == null
                ? InMemorySigomeiStore.TECNICOS.get(idTecnico)
                : tecnicoRepository.buscarPorId(idTecnico);
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
        List<OrdenDTO> ordenes = ordenRepository == null
                ? new ArrayList<>(InMemorySigomeiStore.ORDENES.values())
                : ordenRepository.consultarTodas();
        for (OrdenDTO orden : ordenes) {
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
