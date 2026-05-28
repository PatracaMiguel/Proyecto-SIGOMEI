package com.sigomei.servidor.service;

import com.sigomei.api.catalogos.Criticidad;
import com.sigomei.api.catalogos.EstadoOperativo;
import com.sigomei.api.catalogos.TipoEquipo;
import com.sigomei.api.dto.EquipoDTO;
import com.sigomei.api.dto.OrdenDTO;
import com.sigomei.api.excepciones.ReglaNegocioException;
import com.sigomei.api.excepciones.RegistroNoEncontradoException;
import com.sigomei.api.excepciones.ValidacionException;
import com.sigomei.servidor.config.ServerLog;
import com.sigomei.servidor.repository.EquipoRepository;

import java.util.ArrayList;
import java.util.List;

public class EquipoService {

    private final EquipoRepository repository;

    public EquipoService() {
        InMemorySigomeiStore.reset();
        this.repository = null;
    }

    public EquipoService(EquipoRepository repository) {
        this.repository = repository;
    }

    public EquipoDTO registrarEquipo(EquipoDTO equipo)
            throws ValidacionException, ReglaNegocioException {

        validarEquipo(equipo);
        validarSerieUnica(equipo);

        EquipoDTO guardado;
        if (repository == null) {
            InMemorySigomeiStore.EQUIPOS.put(equipo.getIdEquipo(), copiar(equipo));
            guardado = copiar(equipo);
        } else {
            guardado = repository.guardar(equipo);
        }
        ServerLog.info("Equipo registrado id=" + equipo.getIdEquipo());
        return copiar(guardado);
    }

    public List<EquipoDTO> consultarEquipos() {
        if (repository != null) {
            ServerLog.info("Consulta de equipos");
            return repository.consultarTodos();
        }
        List<EquipoDTO> equipos = new ArrayList<>();
        for (EquipoDTO equipo : InMemorySigomeiStore.EQUIPOS.values()) {
            equipos.add(copiar(equipo));
        }
        ServerLog.info("Consulta de equipos");
        return equipos;
    }

    public List<EquipoDTO> filtrarEquipos(String nombre, TipoEquipo tipo, Criticidad criticidad) {
        if (repository != null) {
            ServerLog.info("Filtro de equipos tipo=" + tipo);
            return repository.filtrar(nombre, tipo, criticidad);
        }
        List<EquipoDTO> resultado = new ArrayList<>();

        for (EquipoDTO equipo : InMemorySigomeiStore.EQUIPOS.values()) {
            boolean coincideNombre = nombre == null || nombre.isBlank()
                    || equipo.getNombre().toLowerCase().contains(nombre.toLowerCase());
            boolean coincideTipo = tipo == null || equipo.getTipo() == tipo;
            boolean coincideCriticidad = criticidad == null || equipo.getCriticidad() == criticidad;

            if (coincideNombre && coincideTipo && coincideCriticidad) {
                resultado.add(copiar(equipo));
            }
        }

        ServerLog.info("Filtro de equipos tipo=" + tipo);
        return resultado;
    }

    public EquipoDTO actualizarEquipo(EquipoDTO equipo)
            throws ValidacionException, ReglaNegocioException, RegistroNoEncontradoException {

        EquipoDTO existente = equipo == null ? null : buscarPorId(equipo.getIdEquipo());
        if (existente == null) {
            throw new RegistroNoEncontradoException("Equipo no encontrado");
        }

        validarEquipo(equipo);
        validarSerieUnica(equipo);

        if (existente.getEstadoOperativo() != EstadoOperativo.INACTIVO
                && equipo.getEstadoOperativo() == EstadoOperativo.INACTIVO
                && tieneOrdenesRelacionadas(equipo.getIdEquipo())) {
            ServerLog.warning("Equipo rechazado RN-03: equipo relacionado a orden id=" + equipo.getIdEquipo());
            throw new ReglaNegocioException("Este equipo esta relacionado a una orden, no puedes cambiarle el estado");
        }

        EquipoDTO actualizado;
        if (repository == null) {
            InMemorySigomeiStore.EQUIPOS.put(equipo.getIdEquipo(), copiar(equipo));
            actualizado = copiar(equipo);
        } else {
            actualizado = repository.actualizar(equipo);
        }
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
        if (repository != null) {
            equipo = repository.actualizar(equipo);
        }
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

        if (repository == null) {
            InMemorySigomeiStore.EQUIPOS.remove(idEquipo);
        } else {
            repository.eliminar(idEquipo);
        }
        ServerLog.info("Equipo eliminado id=" + idEquipo);
    }

    private EquipoDTO buscarPorId(int idEquipo) {
        return repository == null ? InMemorySigomeiStore.EQUIPOS.get(idEquipo) : repository.buscarPorId(idEquipo);
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

    private void validarSerieUnica(EquipoDTO equipo) throws ReglaNegocioException {
        List<EquipoDTO> equipos = repository == null
                ? new ArrayList<>(InMemorySigomeiStore.EQUIPOS.values())
                : repository.consultarTodos();
        for (EquipoDTO actual : equipos) {
            boolean mismoId = actual.getIdEquipo() == equipo.getIdEquipo();
            boolean mismaSerie = actual.getNumeroSerie().equalsIgnoreCase(equipo.getNumeroSerie());
            if (!mismoId && mismaSerie) {
                ServerLog.warning("Equipo rechazado: numero de serie duplicado " + equipo.getNumeroSerie());
                throw new ReglaNegocioException("Ya existe un equipo con el numero de serie indicado");
            }
        }
    }

    private boolean tieneOrdenesRelacionadas(int idEquipo) {
        if (repository != null) {
            return repository.tieneOrdenesRelacionadas(idEquipo);
        }
        for (OrdenDTO orden : InMemorySigomeiStore.ORDENES.values()) {
            if (orden.getIdEquipo() == idEquipo) {
                return true;
            }
        }
        return false;
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
