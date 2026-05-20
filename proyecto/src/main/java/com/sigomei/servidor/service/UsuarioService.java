package com.sigomei.servidor.service;

import com.sigomei.api.dto.UsuarioDTO;
import com.sigomei.api.excepciones.AutenticacionException;

public class UsuarioService {

    public UsuarioDTO iniciarSesion(String usuario, String contrasena)
            throws AutenticacionException {

        throw new UnsupportedOperationException("Método iniciarSesion no implementado");
    }

    public void cerrarSesion(int idUsuario) {

        throw new UnsupportedOperationException("Método cerrarSesion no implementado");
    }
}