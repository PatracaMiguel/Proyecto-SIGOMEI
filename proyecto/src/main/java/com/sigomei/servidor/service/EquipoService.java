package com.sigomei.servidor.service;

import com.sigomei.api.catalogos.Criticidad;
import com.sigomei.api.catalogos.EstadoOperativo;
import com.sigomei.api.catalogos.TipoEquipo;
import com.sigomei.api.dto.EquipoDTO;
import com.sigomei.api.excepciones.ReglaNegocioException;
import com.sigomei.api.excepciones.RegistroNoEncontradoException;
import com.sigomei.api.excepciones.ValidacionException;
import com.sigomei.servidor.config.ServerLog;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class EquipoService {

    public EquipoService() {
        InMemorySigomeiStore.reset();
    }

    public EquipoDTO registrarEquipo(EquipoDTO equipo)
            throws ValidacionException, ReglaNegocioException {

        validarEquipo(equipo);
        boolean serieDuplicada = InMemorySigomeiStore.EQUIPOS.values().stream()
                .anyMatch(actual -> actual.getIdEquipo() != equipo.getIdEquipo()
                        && actual.getNumeroSerie().equalsIgnoreCase(equipo.getNumeroSerie()));
        if (serieDuplicada) {
            throw new ReglaNegocioException("Ya existe un equipo con el numero de serie indicado");
        }
        InMemorySigomeiStore.EQUIPOS.put(equipo.getIdEquipo(), copiar(equipo));
        ServerLog.info("Equipo registrado id=" + equipo.getIdEquipo());
        return copiar(equipo);
    }

    public List<EquipoDTO> consultarEquipos() {
        ServerLog.info("Consulta de equipos");
        return InMemorySigomeiStore.EQUIPOS.values().stream().map(this::copiar).collect(Collectors.toList());
    }

    public List<EquipoDTO> filtrarEquipos(String nombre, TipoEquipo tipo, Criticidad criticidad) {
        String nombreNormalizado = nombre == null ? "" : nombre.toLowerCase(Locale.ROOT);
        ServerLog.info("Filtro de equipos tipo=" + tipo);
        return InMemorySigomeiStore.EQUIPOS.values().stream()
                .filter(equipo -> nombreNormalizado.isBlank()
                        || equipo.getNombre().toLowerCase(Locale.ROOT).contains(nombreNormalizado))
                .filter(equipo -> tipo == null || equipo.getTipo() == tipo)
                .filter(equipo -> criticidad == null || equipo.getCriticidad() == criticidad)
                .map(this::copiar)
                .collect(Collectors.toList());
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
            throw new ReglaNegocioException("Este equipo esta relacionado a una orden, no puedes cambiarle el estado");
        }
        equipo.setEstadoOperativo(nuevoEstado);
        ServerLog.info("Estado equipo actualizado id=" + idEquipo + " estado=" + nuevoEstado);
        return copiar(equipo);
    }

    private void validarEquipo(EquipoDTO equipo) throws ValidacionException {
        if (equipo == null || equipo.getIdEquipo() <= 0 || esBlanco(equipo.getNombre())
                || equipo.getTipo() == null || esBlanco(equipo.getMarca()) || esBlanco(equipo.getModelo())
                || esBlanco(equipo.getNumeroSerie()) || esBlanco(equipo.getUbicacionPlanta())
                || equipo.getFechaInstalacion() == null || equipo.getEstadoOperativo() == null
                || equipo.getCriticidad() == null) {
            throw new ValidacionException("El equipo tiene datos obligatorios incompletos");
        }
    }

    private boolean tieneOrdenesRelacionadas(int idEquipo) {
        return InMemorySigomeiStore.ORDENES.values().stream().anyMatch(orden -> orden.getIdEquipo() == idEquipo);
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
