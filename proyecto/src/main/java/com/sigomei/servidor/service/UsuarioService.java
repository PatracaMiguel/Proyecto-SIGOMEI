package com.sigomei.servidor.service;

import com.sigomei.api.catalogos.EstatusUsuario;
import com.sigomei.api.dto.UsuarioDTO;
import com.sigomei.api.excepciones.AutenticacionException;
import com.sigomei.servidor.config.ServerLog;
import com.sigomei.servidor.repository.JdbcUsuarioRepository;
import com.sigomei.servidor.repository.UsuarioRepository;

public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService() {
        this(new JdbcUsuarioRepository());
    }

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public UsuarioDTO iniciarSesion(String usuario, String contrasena)
            throws AutenticacionException {

        UsuarioDTO encontrado = repository.buscarPorUsuario(usuario);
        if (encontrado == null) {
            throw new AutenticacionException("Usuario o contrasena incorrectos");
        }
        if (!encontrado.getContrasena().equals(contrasena) || encontrado.getEstatus() != EstatusUsuario.ACTIVO) {
            throw new AutenticacionException("Usuario o contrasena incorrectos");
        }
        ServerLog.info("Sesion iniciada usuario=" + usuario);
        return new UsuarioDTO(encontrado.getIdUsuario(), encontrado.getNombreUsuario(), null,
                encontrado.getRol(), encontrado.getEstatus());
    }

    public void cerrarSesion(int idUsuario) {
        ServerLog.info("Sesion cerrada idUsuario=" + idUsuario);
    }
}
