package com.sigomei.servidor.rmi;

import com.sigomei.api.dto.EquipoDTO;
import com.sigomei.api.dto.OrdenDTO;
import com.sigomei.api.dto.TecnicoDTO;
import com.sigomei.api.dto.UsuarioDTO;

import com.sigomei.api.catalogos.Criticidad;
import com.sigomei.api.catalogos.EstadoOperativo;
import com.sigomei.api.catalogos.EstadoOrden;
import com.sigomei.api.catalogos.EstadoTecnico;
import com.sigomei.api.catalogos.NivelCertificacion;
import com.sigomei.api.catalogos.TipoEquipo;

import com.sigomei.api.excepciones.AutenticacionException;
import com.sigomei.api.excepciones.ReglaNegocioException;
import com.sigomei.api.excepciones.RegistroNoEncontradoException;
import com.sigomei.api.excepciones.ValidacionException;

import com.sigomei.servidor.service.EquipoService;
import com.sigomei.servidor.service.OrdenService;
import com.sigomei.servidor.service.TecnicoService;
import com.sigomei.servidor.service.UsuarioService;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.util.List;

public class SigomeiRemoteImpl extends UnicastRemoteObject implements SigomeiRemote {

    private UsuarioService usuarioService;
    private EquipoService equipoService;
    private TecnicoService tecnicoService;
    private OrdenService ordenService;

    public SigomeiRemoteImpl() throws RemoteException {
        super(1100);

        this.usuarioService = new UsuarioService();
        this.equipoService = new EquipoService();
        this.tecnicoService = new TecnicoService();
        this.ordenService = new OrdenService();
    }

    @Override
    public UsuarioDTO iniciarSesion(String usuario, String contrasena)
            throws RemoteException, AutenticacionException {
        return usuarioService.iniciarSesion(usuario, contrasena);
    }

    @Override
    public void cerrarSesion(int idUsuario)
            throws RemoteException {
        usuarioService.cerrarSesion(idUsuario);
    }

    @Override
    public EquipoDTO registrarEquipo(EquipoDTO equipo)
            throws RemoteException, ValidacionException, ReglaNegocioException {
        return equipoService.registrarEquipo(equipo);
    }

    @Override
    public List<EquipoDTO> consultarEquipos()
            throws RemoteException {
        return equipoService.consultarEquipos();
    }

    @Override
    public List<EquipoDTO> filtrarEquipos(String nombre, TipoEquipo tipo, Criticidad criticidad)
            throws RemoteException {
        return equipoService.filtrarEquipos(nombre, tipo, criticidad);
    }

    @Override
    public EquipoDTO actualizarEquipo(EquipoDTO equipo)
            throws RemoteException, ValidacionException, RegistroNoEncontradoException {
        return equipoService.actualizarEquipo(equipo);
    }

    @Override
    public EquipoDTO cambiarEstadoEquipo(int idEquipo, EstadoOperativo nuevoEstado)
            throws RemoteException, ReglaNegocioException, RegistroNoEncontradoException {
        return equipoService.cambiarEstadoEquipo(idEquipo, nuevoEstado);
    }

    @Override
    public TecnicoDTO registrarTecnico(TecnicoDTO tecnico)
            throws RemoteException, ValidacionException, ReglaNegocioException {
        return tecnicoService.registrarTecnico(tecnico);
    }

    @Override
    public List<TecnicoDTO> consultarTecnicos()
            throws RemoteException {
        return tecnicoService.consultarTecnicos();
    }

    @Override
    public List<TecnicoDTO> filtrarTecnicos(String nombre, TipoEquipo especialidad, NivelCertificacion nivelCertificacion)
            throws RemoteException {
        return tecnicoService.filtrarTecnicos(nombre, especialidad, nivelCertificacion);
    }

    @Override
    public TecnicoDTO actualizarTecnico(TecnicoDTO tecnico)
            throws RemoteException, ValidacionException, RegistroNoEncontradoException {
        return tecnicoService.actualizarTecnico(tecnico);
    }

    @Override
    public TecnicoDTO cambiarEstatusTecnico(int idTecnico, EstadoTecnico nuevoEstatus)
            throws RemoteException, ReglaNegocioException, RegistroNoEncontradoException {
        return tecnicoService.cambiarEstatusTecnico(idTecnico, nuevoEstatus);
    }

    @Override
    public void eliminarTecnico(int idTecnico)
            throws RemoteException, ReglaNegocioException, RegistroNoEncontradoException {
        tecnicoService.eliminarTecnico(idTecnico);
    }

    @Override
    public OrdenDTO registrarOrden(OrdenDTO orden)
            throws RemoteException, ValidacionException, ReglaNegocioException, RegistroNoEncontradoException {
        return ordenService.registrarOrden(orden);
    }

    @Override
    public List<OrdenDTO> consultarOrdenes()
            throws RemoteException {
        return ordenService.consultarOrdenes();
    }

    @Override
    public List<OrdenDTO> filtrarOrdenes(EstadoOrden estado, LocalDate fechaInicio, LocalDate fechaCierre)
            throws RemoteException {
        return ordenService.filtrarOrdenes(estado, fechaInicio, fechaCierre);
    }

    @Override
    public OrdenDTO actualizarOrden(OrdenDTO orden)
            throws RemoteException, ValidacionException, ReglaNegocioException, RegistroNoEncontradoException {
        return ordenService.actualizarOrden(orden);
    }

    @Override
    public OrdenDTO cambiarEstadoOrden(int idOrden, EstadoOrden nuevoEstado, LocalDate fechaCierre, BigDecimal costoReal)
            throws RemoteException, ReglaNegocioException, RegistroNoEncontradoException {
        return ordenService.cambiarEstadoOrden(idOrden, nuevoEstado, fechaCierre, costoReal);
    }

    @Override
    public List<OrdenDTO> consultarHistorialOrdenes(Integer idEquipo, Integer idTecnico, EstadoOrden estadoOrden)
            throws RemoteException {
        return ordenService.consultarHistorialOrdenes(idEquipo, idTecnico, estadoOrden);
    }
}