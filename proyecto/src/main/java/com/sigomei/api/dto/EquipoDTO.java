package com.sigomei.api.dto;

import com.sigomei.api.catalogos.Criticidad;
import com.sigomei.api.catalogos.EstadoOperativo;
import com.sigomei.api.catalogos.TipoEquipo;
import java.io.Serializable;
import java.time.LocalDate;

public class EquipoDTO implements Serializable {

    private int idEquipo;
    private String nombre;
    private TipoEquipo tipo;
    private String marca;
    private String modelo;
    private String numeroSerie;
    private String ubicacionPlanta;
    private LocalDate fechaInstalacion;
    private EstadoOperativo estadoOperativo;
    private Criticidad criticidad;

    public EquipoDTO() {
    }

    public EquipoDTO(int idEquipo, String nombre, TipoEquipo tipo, String marca, String modelo,
                     String numeroSerie, String ubicacionPlanta, LocalDate fechaInstalacion,
                     EstadoOperativo estadoOperativo, Criticidad criticidad) {
        this.idEquipo = idEquipo;
        this.nombre = nombre;
        this.tipo = tipo;
        this.marca = marca;
        this.modelo = modelo;
        this.numeroSerie = numeroSerie;
        this.ubicacionPlanta = ubicacionPlanta;
        this.fechaInstalacion = fechaInstalacion;
        this.estadoOperativo = estadoOperativo;
        this.criticidad = criticidad;
    }

    public int getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(int idEquipo) {
        this.idEquipo = idEquipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoEquipo getTipo() {
        return tipo;
    }

    public void setTipo(TipoEquipo tipo) {
        this.tipo = tipo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public String getUbicacionPlanta() {
        return ubicacionPlanta;
    }

    public void setUbicacionPlanta(String ubicacionPlanta) {
        this.ubicacionPlanta = ubicacionPlanta;
    }

    public LocalDate getFechaInstalacion() {
        return fechaInstalacion;
    }

    public void setFechaInstalacion(LocalDate fechaInstalacion) {
        this.fechaInstalacion = fechaInstalacion;
    }

    public EstadoOperativo getEstadoOperativo() {
        return estadoOperativo;
    }

    public void setEstadoOperativo(EstadoOperativo estadoOperativo) {
        this.estadoOperativo = estadoOperativo;
    }

    public Criticidad getCriticidad() {
        return criticidad;
    }

    public void setCriticidad(Criticidad criticidad) {
        this.criticidad = criticidad;
    }
}