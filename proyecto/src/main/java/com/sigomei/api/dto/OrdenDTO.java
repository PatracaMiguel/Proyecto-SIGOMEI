package com.sigomei.api.dto;

import com.sigomei.api.catalogos.EstadoOrden;
import com.sigomei.api.catalogos.TipoMantenimiento;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class OrdenDTO implements Serializable {

    private int idOrden;
    private int idEquipo;
    private int idTecnico;
    private TipoMantenimiento tipoMantenimiento;
    private LocalDate fechaProgramada;
    private LocalDate fechaInicio;
    private LocalDate fechaCierre;
    private String descripcionTrabajo;
    private BigDecimal costoEstimado;
    private BigDecimal costoReal;
    private EstadoOrden estadoOrden;

    public OrdenDTO() {
    }

    public OrdenDTO(int idOrden, int idEquipo, int idTecnico, TipoMantenimiento tipoMantenimiento,
                    LocalDate fechaProgramada, LocalDate fechaInicio, LocalDate fechaCierre,
                    String descripcionTrabajo, BigDecimal costoEstimado, BigDecimal costoReal,
                    EstadoOrden estadoOrden) {
        this.idOrden = idOrden;
        this.idEquipo = idEquipo;
        this.idTecnico = idTecnico;
        this.tipoMantenimiento = tipoMantenimiento;
        this.fechaProgramada = fechaProgramada;
        this.fechaInicio = fechaInicio;
        this.fechaCierre = fechaCierre;
        this.descripcionTrabajo = descripcionTrabajo;
        this.costoEstimado = costoEstimado;
        this.costoReal = costoReal;
        this.estadoOrden = estadoOrden;
    }

    public int getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(int idOrden) {
        this.idOrden = idOrden;
    }

    public int getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(int idEquipo) {
        this.idEquipo = idEquipo;
    }

    public int getIdTecnico() {
        return idTecnico;
    }

    public void setIdTecnico(int idTecnico) {
        this.idTecnico = idTecnico;
    }

    public TipoMantenimiento getTipoMantenimiento() {
        return tipoMantenimiento;
    }

    public void setTipoMantenimiento(TipoMantenimiento tipoMantenimiento) {
        this.tipoMantenimiento = tipoMantenimiento;
    }

    public LocalDate getFechaProgramada() {
        return fechaProgramada;
    }

    public void setFechaProgramada(LocalDate fechaProgramada) {
        this.fechaProgramada = fechaProgramada;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDate fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public String getDescripcionTrabajo() {
        return descripcionTrabajo;
    }

    public void setDescripcionTrabajo(String descripcionTrabajo) {
        this.descripcionTrabajo = descripcionTrabajo;
    }

    public BigDecimal getCostoEstimado() {
        return costoEstimado;
    }

    public void setCostoEstimado(BigDecimal costoEstimado) {
        this.costoEstimado = costoEstimado;
    }

    public BigDecimal getCostoReal() {
        return costoReal;
    }

    public void setCostoReal(BigDecimal costoReal) {
        this.costoReal = costoReal;
    }

    public EstadoOrden getEstadoOrden() {
        return estadoOrden;
    }

    public void setEstadoOrden(EstadoOrden estadoOrden) {
        this.estadoOrden = estadoOrden;
    }
}