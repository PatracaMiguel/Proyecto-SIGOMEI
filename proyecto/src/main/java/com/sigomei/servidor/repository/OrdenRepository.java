package com.sigomei.servidor.repository;

import com.sigomei.api.catalogos.EstadoOrden;
import com.sigomei.api.dto.OrdenDTO;

import java.time.LocalDate;
import java.util.List;

public interface OrdenRepository {

    OrdenDTO guardar(OrdenDTO orden);

    OrdenDTO buscarPorId(int idOrden);

    List<OrdenDTO> consultarTodas();

    List<OrdenDTO> filtrar(EstadoOrden estado, LocalDate fechaInicio, LocalDate fechaCierre);

    OrdenDTO actualizar(OrdenDTO orden);

    boolean existeOrdenActivaMismaFecha(int idEquipo, LocalDate fechaProgramada);

    List<OrdenDTO> consultarHistorial(Integer idEquipo, Integer idTecnico, EstadoOrden estadoOrden);

    void eliminar(int idOrden);
}
