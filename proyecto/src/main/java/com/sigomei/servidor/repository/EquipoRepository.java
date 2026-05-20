package com.sigomei.servidor.repository;

import com.sigomei.api.catalogos.Criticidad;
import com.sigomei.api.catalogos.TipoEquipo;
import com.sigomei.api.dto.EquipoDTO;

import java.util.List;

public interface EquipoRepository {

    EquipoDTO guardar(EquipoDTO equipo);

    EquipoDTO buscarPorId(int idEquipo);

    List<EquipoDTO> consultarTodos();

    List<EquipoDTO> filtrar(String nombre, TipoEquipo tipo, Criticidad criticidad);

    EquipoDTO actualizar(EquipoDTO equipo);

    boolean tieneOrdenesRelacionadas(int idEquipo);

    boolean tieneOrdenesActivas(int idEquipo);
}