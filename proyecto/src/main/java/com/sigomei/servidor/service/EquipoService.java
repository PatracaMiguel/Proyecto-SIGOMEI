package com.sigomei.servidor.service;

import com.sigomei.api.catalogos.Criticidad;
import com.sigomei.api.catalogos.EstadoOperativo;
import com.sigomei.api.catalogos.TipoEquipo;
import com.sigomei.api.dto.EquipoDTO;
import com.sigomei.api.excepciones.ReglaNegocioException;
import com.sigomei.api.excepciones.RegistroNoEncontradoException;
import com.sigomei.api.excepciones.ValidacionException;
import com.sigomei.servidor.config.ServerLog;
import com.sigomei.servidor.repository.EquipoRepository;
import com.sigomei.servidor.repository.JdbcEquipoRepository;

import java.util.List;

public class EquipoService {

    private final EquipoRepository repository;

    public EquipoService() {
        this(new JdbcEquipoRepository());
    }

    public EquipoService(EquipoRepository repository) {
        this.repository = repository;
    }

    public EquipoDTO registrarEquipo(EquipoDTO equipo)
            throws ValidacionException, ReglaNegocioException {

        validarEquipo(equipo);

        List<EquipoDTO> equipos = repository.consultarTodos();
        for (EquipoDTO actual : equipos) {
            boolean mismoId = actual.getIdEquipo() == equipo.getIdEquipo();
            boolean mismaSerie = actual.getNumeroSerie().equalsIgnoreCase(equipo.getNumeroSerie());
            if (!mismoId && mismaSerie) {
                ServerLog.warning("Equipo rechazado: numero de serie duplicado " + equipo.getNumeroSerie());
                throw new ReglaNegocioException("Ya existe un equipo con el numero de serie indicado");
            }
        }

        EquipoDTO guardado = repository.guardar(equipo);
        ServerLog.info("Equipo registrado id=" + equipo.getIdEquipo());
        return copiar(guardado);
    }

    public List<EquipoDTO> consultarEquipos() {
        ServerLog.info("Consulta de equipos");
        return repository.consultarTodos();
    }

    public List<EquipoDTO> filtrarEquipos(String nombre, TipoEquipo tipo, Criticidad criticidad) {
        ServerLog.info("Filtro de equipos tipo=" + tipo);
        return repository.filtrar(nombre, tipo, criticidad);
    }

    public EquipoDTO actualizarEquipo(EquipoDTO equipo)
            throws ValidacionException, RegistroNoEncontradoException {

        if (equipo == null || buscarPorId(equipo.getIdEquipo()) == null) {
            throw new RegistroNoEncontradoException("Equipo no encontrado");
        }

        validarEquipo(equipo);
        EquipoDTO actualizado = repository.actualizar(equipo);
        ServerLog.info("Equipo actualizado id=" + equipo.getIdEquipo());
        return copiar(actualizado);
    }

    public EquipoDTO cambiarEstadoEquipo(int idEquipo, EstadoOperativo nuevoEstado)
            throws ReglaNegocioException, RegistroNoEncontradoException {

        EquipoDTO equipo = buscarPorId(idEquipo);
        if (equipo == null) {
            throw new RegistroNoEncontradoException("Equipo no encontrado");
        }

        if (nuevoEstado == EstadoOperativo.INACTIVO && tieneOrdenesRelacionadas(idEquipo)) {
            ServerLog.warning("Equipo rechazado RN-03: equipo relacionado a orden id=" + idEquipo);
            throw new ReglaNegocioException("Este equipo esta relacionado a una orden, no puedes cambiarle el estado");
        }

        equipo.setEstadoOperativo(nuevoEstado);
        equipo = repository.actualizar(equipo);
        ServerLog.info("Estado equipo actualizado id=" + idEquipo + " estado=" + nuevoEstado);
        return copiar(equipo);
    }

    public void eliminarEquipo(int idEquipo)
            throws ReglaNegocioException, RegistroNoEncontradoException {

        if (buscarPorId(idEquipo) == null) {
            throw new RegistroNoEncontradoException("Equipo no encontrado");
        }

        if (tieneOrdenesRelacionadas(idEquipo)) {
            ServerLog.warning("Equipo rechazado RN-03: equipo relacionado a orden id=" + idEquipo);
            throw new ReglaNegocioException("No se puede eliminar un equipo con ordenes relacionadas");
        }

        repository.eliminar(idEquipo);
        ServerLog.info("Equipo eliminado id=" + idEquipo);
    }

    private EquipoDTO buscarPorId(int idEquipo) {
        return repository.buscarPorId(idEquipo);
    }

    private void validarEquipo(EquipoDTO equipo) throws ValidacionException {
        if (equipo == null) {
            throw new ValidacionException("El equipo tiene datos obligatorios incompletos");
        }
        if (equipo.getIdEquipo() <= 0 || esBlanco(equipo.getNombre()) || equipo.getTipo() == null
                || esBlanco(equipo.getMarca()) || esBlanco(equipo.getModelo())
                || esBlanco(equipo.getNumeroSerie()) || esBlanco(equipo.getUbicacionPlanta())
                || equipo.getFechaInstalacion() == null || equipo.getEstadoOperativo() == null
                || equipo.getCriticidad() == null) {
            throw new ValidacionException("El equipo tiene datos obligatorios incompletos");
        }
    }

    private boolean tieneOrdenesRelacionadas(int idEquipo) {
        return repository.tieneOrdenesRelacionadas(idEquipo);
    }

    private boolean esBlanco(String valor) {
        return valor == null || valor.isBlank();
    }

    private EquipoDTO copiar(EquipoDTO equipo) {
        return new EquipoDTO(equipo.getIdEquipo(), equipo.getNombre(), equipo.getTipo(), equipo.getMarca(),
                equipo.getModelo(), equipo.getNumeroSerie(), equipo.getUbicacionPlanta(),
                equipo.getFechaInstalacion(), equipo.getEstadoOperativo(), equipo.getCriticidad());
    }
}
