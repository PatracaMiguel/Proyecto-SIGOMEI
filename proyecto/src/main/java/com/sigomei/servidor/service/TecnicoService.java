package com.sigomei.servidor.service;

import com.sigomei.api.catalogos.EstadoTecnico;
import com.sigomei.api.catalogos.NivelCertificacion;
import com.sigomei.api.catalogos.TipoEquipo;
import com.sigomei.api.dto.TecnicoDTO;
import com.sigomei.api.excepciones.ReglaNegocioException;
import com.sigomei.api.excepciones.RegistroNoEncontradoException;
import com.sigomei.api.excepciones.ValidacionException;

import java.util.List;

public class TecnicoService {

    public TecnicoDTO registrarTecnico(TecnicoDTO tecnico)
            throws ValidacionException, ReglaNegocioException {

        throw new UnsupportedOperationException("Método registrarTecnico no implementado");
    }

    public List<TecnicoDTO> consultarTecnicos() {

        throw new UnsupportedOperationException("Método consultarTecnicos no implementado");
    }

    public List<TecnicoDTO> filtrarTecnicos(String nombre, TipoEquipo especialidad, NivelCertificacion nivelCertificacion) {

        throw new UnsupportedOperationException("Método filtrarTecnicos no implementado");
    }

    public TecnicoDTO actualizarTecnico(TecnicoDTO tecnico)
            throws ValidacionException, RegistroNoEncontradoException {

        throw new UnsupportedOperationException("Método actualizarTecnico no implementado");
    }

    public TecnicoDTO cambiarEstatusTecnico(int idTecnico, EstadoTecnico nuevoEstatus)
            throws ReglaNegocioException, RegistroNoEncontradoException {

        throw new UnsupportedOperationException("Método cambiarEstatusTecnico no implementado");
    }

    public void eliminarTecnico(int idTecnico)
            throws ReglaNegocioException, RegistroNoEncontradoException {

        throw new UnsupportedOperationException("Método eliminarTecnico no implementado");
    }
}