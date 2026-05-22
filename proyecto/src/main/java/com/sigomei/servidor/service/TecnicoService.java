package com.sigomei.servidor.service;

import com.sigomei.api.catalogos.EstadoOrden;
import com.sigomei.api.catalogos.EstadoTecnico;
import com.sigomei.api.catalogos.NivelCertificacion;
import com.sigomei.api.catalogos.TipoEquipo;
import com.sigomei.api.dto.TecnicoDTO;
import com.sigomei.api.excepciones.ReglaNegocioException;
import com.sigomei.api.excepciones.RegistroNoEncontradoException;
import com.sigomei.api.excepciones.ValidacionException;
import com.sigomei.servidor.config.ServerLog;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class TecnicoService {

    public TecnicoService() {
        InMemorySigomeiStore.reset();
    }

    public TecnicoDTO registrarTecnico(TecnicoDTO tecnico)
            throws ValidacionException, ReglaNegocioException {

        validarTecnico(tecnico);
        boolean rfcDuplicado = InMemorySigomeiStore.TECNICOS.values().stream()
                .anyMatch(actual -> actual.getIdTecnico() != tecnico.getIdTecnico()
                        && actual.getRfc().equalsIgnoreCase(tecnico.getRfc()));
        if (rfcDuplicado) {
            throw new ReglaNegocioException("Ya existe un tecnico con el RFC indicado");
        }
        InMemorySigomeiStore.TECNICOS.put(tecnico.getIdTecnico(), copiar(tecnico));
        ServerLog.info("Tecnico registrado id=" + tecnico.getIdTecnico());
        return copiar(tecnico);
    }

    public List<TecnicoDTO> consultarTecnicos() {
        ServerLog.info("Consulta de tecnicos");
        return InMemorySigomeiStore.TECNICOS.values().stream().map(this::copiar).collect(Collectors.toList());
    }

    public List<TecnicoDTO> filtrarTecnicos(String nombre, TipoEquipo especialidad, NivelCertificacion nivelCertificacion) {
        String nombreNormalizado = nombre == null ? "" : nombre.toLowerCase(Locale.ROOT);
        ServerLog.info("Filtro de tecnicos especialidad=" + especialidad);
        return InMemorySigomeiStore.TECNICOS.values().stream()
                .filter(tecnico -> nombreNormalizado.isBlank()
                        || tecnico.getNombreCompleto().toLowerCase(Locale.ROOT).contains(nombreNormalizado))
                .filter(tecnico -> especialidad == null || tecnico.getEspecialidad() == especialidad)
                .filter(tecnico -> nivelCertificacion == null || tecnico.getNivelCertificacion() == nivelCertificacion)
                .map(this::copiar)
                .collect(Collectors.toList());
    }

    public TecnicoDTO actualizarTecnico(TecnicoDTO tecnico)
            throws ValidacionException, RegistroNoEncontradoException {

        if (tecnico == null || !InMemorySigomeiStore.TECNICOS.containsKey(tecnico.getIdTecnico())) {
            throw new RegistroNoEncontradoException("Tecnico no encontrado");
        }
        validarTecnico(tecnico);
        InMemorySigomeiStore.TECNICOS.put(tecnico.getIdTecnico(), copiar(tecnico));
        ServerLog.info("Tecnico actualizado id=" + tecnico.getIdTecnico());
        return copiar(tecnico);
    }

    public TecnicoDTO cambiarEstatusTecnico(int idTecnico, EstadoTecnico nuevoEstatus)
            throws ReglaNegocioException, RegistroNoEncontradoException {

        TecnicoDTO tecnico = InMemorySigomeiStore.TECNICOS.get(idTecnico);
        if (tecnico == null) {
            throw new RegistroNoEncontradoException("Tecnico no encontrado");
        }
        if (nuevoEstatus == EstadoTecnico.INACTIVO && tieneOrdenesActivas(idTecnico)) {
            throw new ReglaNegocioException("Este tecnico tiene ordenes activas, no puedes cambiarlo a inactivo");
        }
        tecnico.setEstatus(nuevoEstatus);
        ServerLog.info("Estatus tecnico actualizado id=" + idTecnico + " estatus=" + nuevoEstatus);
        return copiar(tecnico);
    }

    public void eliminarTecnico(int idTecnico)
            throws ReglaNegocioException, RegistroNoEncontradoException {

        if (!InMemorySigomeiStore.TECNICOS.containsKey(idTecnico)) {
            throw new RegistroNoEncontradoException("Tecnico no encontrado");
        }
        if (tieneOrdenesRelacionadas(idTecnico)) {
            throw new ReglaNegocioException("No se puede eliminar un tecnico con ordenes relacionadas");
        }
        InMemorySigomeiStore.TECNICOS.remove(idTecnico);
        ServerLog.info("Tecnico eliminado id=" + idTecnico);
    }

    private void validarTecnico(TecnicoDTO tecnico) throws ValidacionException {
        if (tecnico == null || tecnico.getIdTecnico() <= 0 || esBlanco(tecnico.getNombreCompleto())
                || esBlanco(tecnico.getRfc()) || esBlanco(tecnico.getTelefono()) || esBlanco(tecnico.getCorreo())
                || tecnico.getEspecialidad() == null || tecnico.getNivelCertificacion() == null
                || tecnico.getFechaIngreso() == null || tecnico.getEstatus() == null) {
            throw new ValidacionException("El tecnico tiene datos obligatorios incompletos");
        }
    }

    private boolean tieneOrdenesRelacionadas(int idTecnico) {
        return InMemorySigomeiStore.ORDENES.values().stream().anyMatch(orden -> orden.getIdTecnico() == idTecnico);
    }

    private boolean tieneOrdenesActivas(int idTecnico) {
        return InMemorySigomeiStore.ORDENES.values().stream()
                .anyMatch(orden -> orden.getIdTecnico() == idTecnico
                        && (orden.getEstadoOrden() == EstadoOrden.PROGRAMADA
                        || orden.getEstadoOrden() == EstadoOrden.EN_EJECUCION));
    }

    private boolean esBlanco(String valor) {
        return valor == null || valor.isBlank();
    }

    private TecnicoDTO copiar(TecnicoDTO tecnico) {
        return new TecnicoDTO(tecnico.getIdTecnico(), tecnico.getNombreCompleto(), tecnico.getRfc(),
                tecnico.getTelefono(), tecnico.getCorreo(), tecnico.getEspecialidad(),
                tecnico.getNivelCertificacion(), tecnico.getFechaIngreso(), tecnico.getEstatus());
    }
}
