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

import java.math.BigDecimal;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;

public interface SigomeiRemote extends Remote {

    UsuarioDTO iniciarSesion(String usuario, String contrasena)
            throws RemoteException, AutenticacionException;

    void cerrarSesion(int idUsuario)
            throws RemoteException;

    EquipoDTO registrarEquipo(EquipoDTO equipo)
            throws RemoteException, ValidacionException, ReglaNegocioException;

    List<EquipoDTO> consultarEquipos()
            throws RemoteException;

    List<EquipoDTO> filtrarEquipos(String nombre, TipoEquipo tipo, Criticidad criticidad)
            throws RemoteException;

    EquipoDTO actualizarEquipo(EquipoDTO equipo)
            throws RemoteException, ValidacionException, RegistroNoEncontradoException;

    EquipoDTO cambiarEstadoEquipo(int idEquipo, EstadoOperativo nuevoEstado)
            throws RemoteException, ReglaNegocioException, RegistroNoEncontradoException;

    TecnicoDTO registrarTecnico(TecnicoDTO tecnico)
            throws RemoteException, ValidacionException, ReglaNegocioException;

    List<TecnicoDTO> consultarTecnicos()
            throws RemoteException;

    List<TecnicoDTO> filtrarTecnicos(String nombre, TipoEquipo especialidad, NivelCertificacion nivelCertificacion)
            throws RemoteException;

    TecnicoDTO actualizarTecnico(TecnicoDTO tecnico)
            throws RemoteException, ValidacionException, RegistroNoEncontradoException;

    TecnicoDTO cambiarEstatusTecnico(int idTecnico, EstadoTecnico nuevoEstatus)
            throws RemoteException, ReglaNegocioException, RegistroNoEncontradoException;

    void eliminarTecnico(int idTecnico)
            throws RemoteException, ReglaNegocioException, RegistroNoEncontradoException;

    
    OrdenDTO registrarOrden(OrdenDTO orden)
            throws RemoteException, ValidacionException, ReglaNegocioException, RegistroNoEncontradoException;

    List<OrdenDTO> consultarOrdenes()
            throws RemoteException;

    List<OrdenDTO> filtrarOrdenes(EstadoOrden estado, LocalDate fechaInicio, LocalDate fechaCierre)
            throws RemoteException;

    OrdenDTO actualizarOrden(OrdenDTO orden)
            throws RemoteException, ValidacionException, ReglaNegocioException, RegistroNoEncontradoException;

    OrdenDTO cambiarEstadoOrden(int idOrden, EstadoOrden nuevoEstado, LocalDate fechaCierre, BigDecimal costoReal)
            throws RemoteException, ReglaNegocioException, RegistroNoEncontradoException;

    List<OrdenDTO> consultarHistorialOrdenes(Integer idEquipo, Integer idTecnico, EstadoOrden estadoOrden)
            throws RemoteException;
}