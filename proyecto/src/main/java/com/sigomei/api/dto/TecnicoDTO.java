package com.sigomei.api.dto;

import com.sigomei.api.catalogos.EstadoTecnico;
import com.sigomei.api.catalogos.NivelCertificacion;
import com.sigomei.api.catalogos.TipoEquipo;
import java.io.Serializable;
import java.time.LocalDate;

public class TecnicoDTO implements Serializable {

    private int idTecnico;
    private String nombreCompleto;
    private String rfc;
    private String telefono;
    private String correo;
    private TipoEquipo especialidad;
    private NivelCertificacion nivelCertificacion;
    private LocalDate fechaIngreso;
    private EstadoTecnico estatus;

    public TecnicoDTO() {
    }

    public TecnicoDTO(int idTecnico, String nombreCompleto, String rfc, String telefono,
                      String correo, TipoEquipo especialidad, NivelCertificacion nivelCertificacion,
                      LocalDate fechaIngreso, EstadoTecnico estatus) {
        this.idTecnico = idTecnico;
        this.nombreCompleto = nombreCompleto;
        this.rfc = rfc;
        this.telefono = telefono;
        this.correo = correo;
        this.especialidad = especialidad;
        this.nivelCertificacion = nivelCertificacion;
        this.fechaIngreso = fechaIngreso;
        this.estatus = estatus;
    }

    public int getIdTecnico() {
        return idTecnico;
    }

    public void setIdTecnico(int idTecnico) {
        this.idTecnico = idTecnico;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getRfc() {
        return rfc;
    }

    public void setRfc(String rfc) {
        this.rfc = rfc;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public TipoEquipo getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(TipoEquipo especialidad) {
        this.especialidad = especialidad;
    }

    public NivelCertificacion getNivelCertificacion() {
        return nivelCertificacion;
    }

    public void setNivelCertificacion(NivelCertificacion nivelCertificacion) {
        this.nivelCertificacion = nivelCertificacion;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public EstadoTecnico getEstatus() {
        return estatus;
    }

    public void setEstatus(EstadoTecnico estatus) {
        this.estatus = estatus;
    }
}