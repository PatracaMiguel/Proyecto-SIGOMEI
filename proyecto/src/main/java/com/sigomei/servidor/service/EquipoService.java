package com.sigomei.servidor.service;

import com.sigomei.api.catalogos.Criticidad;
import com.sigomei.api.catalogos.EstadoOperativo;
import com.sigomei.api.catalogos.TipoEquipo;
import com.sigomei.api.dto.EquipoDTO;
import com.sigomei.api.excepciones.ReglaNegocioException;
import com.sigomei.api.excepciones.RegistroNoEncontradoException;
import com.sigomei.api.excepciones.ValidacionException;

import java.util.List;

public class EquipoService {

    public EquipoDTO registrarEquipo(EquipoDTO equipo)
            throws ValidacionException, ReglaNegocioException {

        throw new UnsupportedOperationException("Método registrarEquipo no implementado");
    }

    public List<EquipoDTO> consultarEquipos() {

        throw new UnsupportedOperationException("Método consultarEquipos no implementado");
    }

    public List<EquipoDTO> filtrarEquipos(String nombre, TipoEquipo tipo, Criticidad criticidad) {

        throw new UnsupportedOperationException("Método filtrarEquipos no implementado");
    }

    public EquipoDTO actualizarEquipo(EquipoDTO equipo)
            throws ValidacionException, RegistroNoEncontradoException {

        throw new UnsupportedOperationException("Método actualizarEquipo no implementado");
    }

    public EquipoDTO cambiarEstadoEquipo(int idEquipo, EstadoOperativo nuevoEstado)
            throws ReglaNegocioException, RegistroNoEncontradoException {

        throw new UnsupportedOperationException("Método cambiarEstadoEquipo no implementado");
    }
}