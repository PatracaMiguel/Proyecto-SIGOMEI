package com.sigomei.servidor.service;

import com.sigomei.api.catalogos.EstadoOrden;
import com.sigomei.api.catalogos.EstadoTecnico;
import com.sigomei.api.catalogos.NivelCertificacion;
import com.sigomei.api.catalogos.TipoEquipo;
import com.sigomei.api.dto.OrdenDTO;
import com.sigomei.api.dto.TecnicoDTO;
import com.sigomei.api.excepciones.ReglaNegocioException;
import com.sigomei.api.excepciones.RegistroNoEncontradoException;
import com.sigomei.api.excepciones.ValidacionException;
import com.sigomei.servidor.config.ServerLog;

import java.util.ArrayList;
import java.util.List;

public class TecnicoService {

    public TecnicoService() {
        InMemorySigomeiStore.reset();
    }

    public TecnicoDTO registrarTecnico(TecnicoDTO tecnico)
            throws ValidacionException, ReglaNegocioException {

        validarTecnico(tecnico);

        for (TecnicoDTO actual : InMemorySigomeiStore.TECNICOS.values()) {
            boolean mismoId = actual.getIdTecnico() == tecnico.getIdTecnico();
            boolean mismoRfc = actual.getRfc().equalsIgnoreCase(tecnico.getRfc());
            if (!mismoId && mismoRfc) {
                ServerLog.warning("Tecnico rechazado: RFC duplicado " + tecnico.getRfc());
                throw new ReglaNegocioException("Ya existe un tecnico con el RFC indicado");
            }
        }

        InMemorySigomeiStore.TECNICOS.put(tecnico.getIdTecnico(), copiar(tecnico));
        ServerLog.info("Tecnico registrado id=" + tecnico.getIdTecnico());
        return copiar(tecnico);
    }

    public List<TecnicoDTO> consultarTecnicos() {
        List<TecnicoDTO> tecnicos = new ArrayList<>();
        for (TecnicoDTO tecnico : InMemorySigomeiStore.TECNICOS.values()) {
            tecnicos.add(copiar(tecnico));
        }
        ServerLog.info("Consulta de tecnicos");
        return tecnicos;
    }

    public List<TecnicoDTO> filtrarTecnicos(String nombre, TipoEquipo especialidad, NivelCertificacion nivelCertificacion) {
        List<TecnicoDTO> resultado = new ArrayList<>();

        for (TecnicoDTO tecnico : InMemorySigomeiStore.TECNICOS.values()) {
            boolean coincideNombre = nombre == null || nombre.isBlank()
                    || tecnico.getNombreCompleto().toLowerCase().contains(nombre.toLowerCase());
            boolean coincideEspecialidad = especialidad == null || tecnico.getEspecialidad() == especialidad;
            boolean coincideNivel = nivelCertificacion == null || tecnico.getNivelCertificacion() == nivelCertificacion;

            if (coincideNombre && coincideEspecialidad && coincideNivel) {
                resultado.add(copiar(tecnico));
            }
        }

        ServerLog.info("Filtro de tecnicos especialidad=" + especialidad);
        return resultado;
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
            ServerLog.warning("Tecnico rechazado RN-03: tiene ordenes activas id=" + idTecnico);
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
            ServerLog.warning("Tecnico rechazado RN-03: tiene ordenes relacionadas id=" + idTecnico);
            throw new ReglaNegocioException("No se puede eliminar un tecnico con ordenes relacionadas");
        }

        InMemorySigomeiStore.TECNICOS.remove(idTecnico);
        ServerLog.info("Tecnico eliminado id=" + idTecnico);
    }

    private void validarTecnico(TecnicoDTO tecnico) throws ValidacionException {
        if (tecnico == null) {
            throw new ValidacionException("El tecnico tiene datos obligatorios incompletos");
        }
        if (tecnico.getIdTecnico() <= 0 || esBlanco(tecnico.getNombreCompleto())
                || esBlanco(tecnico.getRfc()) || esBlanco(tecnico.getTelefono())
                || esBlanco(tecnico.getCorreo()) || tecnico.getEspecialidad() == null
                || tecnico.getNivelCertificacion() == null || tecnico.getFechaIngreso() == null
                || tecnico.getEstatus() == null) {
            throw new ValidacionException("El tecnico tiene datos obligatorios incompletos");
        }
    }

    private boolean tieneOrdenesRelacionadas(int idTecnico) {
        for (OrdenDTO orden : InMemorySigomeiStore.ORDENES.values()) {
            if (orden.getIdTecnico() == idTecnico) {
                return true;
            }
        }
        return false;
    }

    private boolean tieneOrdenesActivas(int idTecnico) {
        for (OrdenDTO orden : InMemorySigomeiStore.ORDENES.values()) {
            boolean esDelTecnico = orden.getIdTecnico() == idTecnico;
            boolean estaActiva = orden.getEstadoOrden() == EstadoOrden.PROGRAMADA
                    || orden.getEstadoOrden() == EstadoOrden.EN_EJECUCION;
            if (esDelTecnico && estaActiva) {
                return true;
            }
        }
        return false;
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
