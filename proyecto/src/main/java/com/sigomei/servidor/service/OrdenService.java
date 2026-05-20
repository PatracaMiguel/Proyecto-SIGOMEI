package com.sigomei.servidor.service;

import com.sigomei.api.catalogos.EstadoOrden;
import com.sigomei.api.dto.OrdenDTO;
import com.sigomei.api.excepciones.ReglaNegocioException;
import com.sigomei.api.excepciones.RegistroNoEncontradoException;
import com.sigomei.api.excepciones.ValidacionException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class OrdenService {

    public OrdenDTO registrarOrden(OrdenDTO orden)
            throws ValidacionException, ReglaNegocioException, RegistroNoEncontradoException {

        throw new UnsupportedOperationException("Método registrarOrden no implementado");
    }

    public List<OrdenDTO> consultarOrdenes() {

        throw new UnsupportedOperationException("Método consultarOrdenes no implementado");
    }

    public List<OrdenDTO> filtrarOrdenes(EstadoOrden estado, LocalDate fechaInicio, LocalDate fechaCierre) {

        throw new UnsupportedOperationException("Método filtrarOrdenes no implementado");
    }

    public OrdenDTO actualizarOrden(OrdenDTO orden)
            throws ValidacionException, ReglaNegocioException, RegistroNoEncontradoException {

        throw new UnsupportedOperationException("Método actualizarOrden no implementado");
    }

    public OrdenDTO cambiarEstadoOrden(int idOrden, EstadoOrden nuevoEstado, LocalDate fechaCierre, BigDecimal costoReal)
            throws ReglaNegocioException, RegistroNoEncontradoException {

        throw new UnsupportedOperationException("Método cambiarEstadoOrden no implementado");
    }

    public List<OrdenDTO> consultarHistorialOrdenes(Integer idEquipo, Integer idTecnico, EstadoOrden estadoOrden) {

        throw new UnsupportedOperationException("Método consultarHistorialOrdenes no implementado");
    }
}