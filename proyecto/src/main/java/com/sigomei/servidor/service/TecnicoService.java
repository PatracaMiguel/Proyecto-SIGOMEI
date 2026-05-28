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
import com.sigomei.servidor.repository.JdbcTecnicoRepository;
import com.sigomei.servidor.repository.TecnicoRepository;

import java.util.List;

public class TecnicoService {

    private final TecnicoRepository repository;

    public TecnicoService() {
        this(new JdbcTecnicoRepository());
    }

    public TecnicoService(TecnicoRepository repository) {
        this.repository = repository;
    }

    public TecnicoDTO registrarTecnico(TecnicoDTO tecnico)
            throws ValidacionException, ReglaNegocioException {

        validarTecnico(tecnico);

        List<TecnicoDTO> tecnicos = repository.consultarTodos();
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

        TecnicoDTO guardado = repository.guardar(tecnico);
        ServerLog.info("Tecnico registrado id=" + tecnico.getIdTecnico());
        return copiar(guardado);
    }

    public List<TecnicoDTO> consultarTecnicos() {
        ServerLog.info("Consulta de tecnicos");
        return repository.consultarTodos();
    }

    public List<TecnicoDTO> filtrarTecnicos(String nombre, TipoEquipo especialidad, NivelCertificacion nivelCertificacion) {
        ServerLog.info("Filtro de tecnicos especialidad=" + especialidad);
        return repository.filtrar(nombre, especialidad, nivelCertificacion);
    }

    public TecnicoDTO actualizarTecnico(TecnicoDTO tecnico)
            throws ValidacionException, RegistroNoEncontradoException {

        if (tecnico == null || buscarPorId(tecnico.getIdTecnico()) == null) {
            throw new RegistroNoEncontradoException("Tecnico no encontrado");
        }

        validarTecnico(tecnico);
        TecnicoDTO actualizado = repository.actualizar(tecnico);
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
        tecnico = repository.actualizar(tecnico);
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

        repository.eliminar(idTecnico);
        ServerLog.info("Tecnico eliminado id=" + idTecnico);
    }

    private TecnicoDTO buscarPorId(int idTecnico) {
        return repository.buscarPorId(idTecnico);
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
        return repository.tieneOrdenesRelacionadas(idTecnico);
    }

    private boolean tieneOrdenesActivas(int idTecnico) {
        return repository.tieneOrdenesActivas(idTecnico);
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
