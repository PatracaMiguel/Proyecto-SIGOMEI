package com.sigomei.servidor.repository;

import com.sigomei.api.catalogos.NivelCertificacion;
import com.sigomei.api.catalogos.TipoEquipo;
import com.sigomei.api.dto.TecnicoDTO;

import java.util.List;

public interface TecnicoRepository {

    TecnicoDTO guardar(TecnicoDTO tecnico);

    TecnicoDTO buscarPorId(int idTecnico);

    List<TecnicoDTO> consultarTodos();

    List<TecnicoDTO> filtrar(String nombre, TipoEquipo especialidad, NivelCertificacion nivelCertificacion);

    TecnicoDTO actualizar(TecnicoDTO tecnico);

    boolean tieneOrdenesRelacionadas(int idTecnico);

    boolean tieneOrdenesActivas(int idTecnico);

    void eliminar(int idTecnico);
}