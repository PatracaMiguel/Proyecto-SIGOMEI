package com.sigomei.api.dto;

import com.sigomei.api.catalogos.EstatusUsuario;
import com.sigomei.api.catalogos.RolUsuario;
import java.io.Serializable;

public class UsuarioDTO implements Serializable {

    private int idUsuario;
    private String nombreUsuario;
    private String contrasena;
    private RolUsuario rol;
    private EstatusUsuario estatus;

    public UsuarioDTO() {
    }

    public UsuarioDTO(int idUsuario, String nombreUsuario, String contrasena, RolUsuario rol, EstatusUsuario estatus) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.rol = rol;
        this.estatus = estatus;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    public EstatusUsuario getEstatus() {
        return estatus;
    }

    public void setEstatus(EstatusUsuario estatus) {
        this.estatus = estatus;
    }
}