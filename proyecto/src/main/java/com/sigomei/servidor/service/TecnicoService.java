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
import com.sigomei.servidor.repository.TecnicoRepository;

import java.util.ArrayList;
import java.util.List;

public class TecnicoService {

    private final TecnicoRepository repository;

    public TecnicoService() {
        InMemorySigomeiStore.reset();
        this.repository = null;
    }

    public TecnicoService(TecnicoRepository repository) {
        this.repository = repository;
    }

    public TecnicoDTO registrarTecnico(TecnicoDTO tecnico)
            throws ValidacionException, ReglaNegocioException {

        validarTecnico(tecnico);
        validarDatosUnicos(tecnico);

        TecnicoDTO guardado;
        if (repository == null) {
            InMemorySigomeiStore.TECNICOS.put(tecnico.getIdTecnico(), copiar(tecnico));
            guardado = copiar(tecnico);
        } else {
            guardado = repository.guardar(tecnico);
        }
        ServerLog.info("Tecnico registrado id=" + tecnico.getIdTecnico());
        return copiar(guardado);
    }

    public List<TecnicoDTO> consultarTecnicos() {
        if (repository != null) {
            ServerLog.info("Consulta de tecnicos");
            return repository.consultarTodos();
        }
        List<TecnicoDTO> tecnicos = new ArrayList<>();
        for (TecnicoDTO tecnico : InMemorySigomeiStore.TECNICOS.values()) {
            tecnicos.add(copiar(tecnico));
        }
        ServerLog.info("Consulta de tecnicos");
        return tecnicos;
    }

    public List<TecnicoDTO> filtrarTecnicos(String nombre, TipoEquipo especialidad, NivelCertificacion nivelCertificacion) {
        if (repository != null) {
            ServerLog.info("Filtro de tecnicos especialidad=" + especialidad);
            return repository.filtrar(nombre, especialidad, nivelCertificacion);
        }
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
            throws ValidacionException, ReglaNegocioException, RegistroNoEncontradoException {

        TecnicoDTO existente = tecnico == null ? null : buscarPorId(tecnico.getIdTecnico());
        if (existente == null) {
            throw new RegistroNoEncontradoException("Tecnico no encontrado");
        }

        validarTecnico(tecnico);
        validarDatosUnicos(tecnico);

        if (existente.getEstatus() != EstadoTecnico.INACTIVO
                && tecnico.getEstatus() == EstadoTecnico.INACTIVO
                && tieneOrdenesActivas(tecnico.getIdTecnico())) {
            ServerLog.warning("Tecnico rechazado RN-03: tiene ordenes activas id=" + tecnico.getIdTecnico());
            throw new ReglaNegocioException("Este tecnico tiene ordenes activas, no puedes cambiarlo a inactivo");
        }

        TecnicoDTO actualizado;
        if (repository == null) {
            InMemorySigomeiStore.TECNICOS.put(tecnico.getIdTecnico(), copiar(tecnico));
            actualizado = copiar(tecnico);
        } else {
            actualizado = repository.actualizar(tecnico);
        }
        ServerLog.info("Tecnico actualizado id=" + tecnico.getIdTecnico());
        return copiar(actualizado);
    }

    public TecnicoDTO cambiarEstatusTecnico(int idTecnico, EstadoTecnico nuevoEstatus)
            throws ReglaNegocioException, RegistroNoEncontradoException {

        TecnicoDTO tecnico = buscarPorId(idTecnico);
        if (tecnico == null) {
            throw new RegistroNoEncontradoException("Tecnico no encontrado");
        }

        if (nuevoEstatus == EstadoTecnico.INACTIVO && tieneOrdenesActivas(idTecnico)) {
            ServerLog.warning("Tecnico rechazado RN-03: tiene ordenes activas id=" + idTecnico);
            throw new ReglaNegocioException("Este tecnico tiene ordenes activas, no puedes cambiarlo a inactivo");
        }

        tecnico.setEstatus(nuevoEstatus);
        if (repository != null) {
            tecnico = repository.actualizar(tecnico);
        }
        ServerLog.info("Estatus tecnico actualizado id=" + idTecnico + " estatus=" + nuevoEstatus);
        return copiar(tecnico);
    }

    public void eliminarTecnico(int idTecnico)
            throws ReglaNegocioException, RegistroNoEncontradoException {

        if (buscarPorId(idTecnico) == null) {
            throw new RegistroNoEncontradoException("Tecnico no encontrado");
        }

        if (tieneOrdenesRelacionadas(idTecnico)) {
            ServerLog.warning("Tecnico rechazado RN-03: tiene ordenes relacionadas id=" + idTecnico);
            throw new ReglaNegocioException("No se puede eliminar un tecnico con ordenes relacionadas");
        }

        if (repository == null) {
            InMemorySigomeiStore.TECNICOS.remove(idTecnico);
        } else {
            repository.eliminar(idTecnico);
        }
        ServerLog.info("Tecnico eliminado id=" + idTecnico);
    }

    private TecnicoDTO buscarPorId(int idTecnico) {
        return repository == null ? InMemorySigomeiStore.TECNICOS.get(idTecnico) : repository.buscarPorId(idTecnico);
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

    private void validarDatosUnicos(TecnicoDTO tecnico) throws ReglaNegocioException {
        List<TecnicoDTO> tecnicos = repository == null
                ? new ArrayList<>(InMemorySigomeiStore.TECNICOS.values())
                : repository.consultarTodos();
        for (TecnicoDTO actual : tecnicos) {
            boolean mismoId = actual.getIdTecnico() == tecnico.getIdTecnico();
            boolean mismoRfc = actual.getRfc().equalsIgnoreCase(tecnico.getRfc());
            boolean mismoCorreo = actual.getCorreo().equalsIgnoreCase(tecnico.getCorreo());
            if (!mismoId && mismoRfc) {
                ServerLog.warning("Tecnico rechazado: RFC duplicado " + tecnico.getRfc());
                throw new ReglaNegocioException("Ya existe un tecnico con el RFC indicado");
            }
            if (!mismoId && mismoCorreo) {
                ServerLog.warning("Tecnico rechazado: correo duplicado " + tecnico.getCorreo());
                throw new ReglaNegocioException("Ya existe un tecnico con el correo indicado");
            }
        }
    }

    private boolean tieneOrdenesRelacionadas(int idTecnico) {
        if (repository != null) {
            return repository.tieneOrdenesRelacionadas(idTecnico);
        }
        for (OrdenDTO orden : InMemorySigomeiStore.ORDENES.values()) {
            if (orden.getIdTecnico() == idTecnico) {
                return true;
            }
        }
        return false;
    }

    private boolean tieneOrdenesActivas(int idTecnico) {
        if (repository != null) {
            return repository.tieneOrdenesActivas(idTecnico);
        }
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
