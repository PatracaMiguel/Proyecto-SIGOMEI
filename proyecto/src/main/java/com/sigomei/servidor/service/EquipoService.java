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

import java.util.ArrayList;
import java.util.List;

public class EquipoService {

    public EquipoService() {
        InMemorySigomeiStore.reset();
    }

    public EquipoDTO registrarEquipo(EquipoDTO equipo)
            throws ValidacionException, ReglaNegocioException {

        validarEquipo(equipo);

        for (EquipoDTO actual : InMemorySigomeiStore.EQUIPOS.values()) {
            boolean mismoId = actual.getIdEquipo() == equipo.getIdEquipo();
            boolean mismaSerie = actual.getNumeroSerie().equalsIgnoreCase(equipo.getNumeroSerie());
            if (!mismoId && mismaSerie) {
                ServerLog.warning("Equipo rechazado: numero de serie duplicado " + equipo.getNumeroSerie());
                throw new ReglaNegocioException("Ya existe un equipo con el numero de serie indicado");
            }
        }

        InMemorySigomeiStore.EQUIPOS.put(equipo.getIdEquipo(), copiar(equipo));
        ServerLog.info("Equipo registrado id=" + equipo.getIdEquipo());
        return copiar(equipo);
    }

    public List<EquipoDTO> consultarEquipos() {
        List<EquipoDTO> equipos = new ArrayList<>();
        for (EquipoDTO equipo : InMemorySigomeiStore.EQUIPOS.values()) {
            equipos.add(copiar(equipo));
        }
        ServerLog.info("Consulta de equipos");
        return equipos;
    }

    public List<EquipoDTO> filtrarEquipos(String nombre, TipoEquipo tipo, Criticidad criticidad) {
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
            throws ValidacionException, RegistroNoEncontradoException {

        if (equipo == null || !InMemorySigomeiStore.EQUIPOS.containsKey(equipo.getIdEquipo())) {
            throw new RegistroNoEncontradoException("Equipo no encontrado");
        }

        validarEquipo(equipo);
        InMemorySigomeiStore.EQUIPOS.put(equipo.getIdEquipo(), copiar(equipo));
        ServerLog.info("Equipo actualizado id=" + equipo.getIdEquipo());
        return copiar(equipo);
    }

    public EquipoDTO cambiarEstadoEquipo(int idEquipo, EstadoOperativo nuevoEstado)
            throws ReglaNegocioException, RegistroNoEncontradoException {

        EquipoDTO equipo = InMemorySigomeiStore.EQUIPOS.get(idEquipo);
        if (equipo == null) {
            throw new RegistroNoEncontradoException("Equipo no encontrado");
        }

        if (nuevoEstado == EstadoOperativo.INACTIVO && tieneOrdenesRelacionadas(idEquipo)) {
            ServerLog.warning("Equipo rechazado RN-03: equipo relacionado a orden id=" + idEquipo);
            throw new ReglaNegocioException("Este equipo esta relacionado a una orden, no puedes cambiarle el estado");
        }

        equipo.setEstadoOperativo(nuevoEstado);
        ServerLog.info("Estado equipo actualizado id=" + idEquipo + " estado=" + nuevoEstado);
        return copiar(equipo);
    }

    public void eliminarEquipo(int idEquipo)
            throws ReglaNegocioException, RegistroNoEncontradoException {

        if (!InMemorySigomeiStore.EQUIPOS.containsKey(idEquipo)) {
            throw new RegistroNoEncontradoException("Equipo no encontrado");
        }

        if (tieneOrdenesRelacionadas(idEquipo)) {
            ServerLog.warning("Equipo rechazado RN-03: equipo relacionado a orden id=" + idEquipo);
            throw new ReglaNegocioException("No se puede eliminar un equipo con ordenes relacionadas");
        }

        InMemorySigomeiStore.EQUIPOS.remove(idEquipo);
        ServerLog.info("Equipo eliminado id=" + idEquipo);
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
