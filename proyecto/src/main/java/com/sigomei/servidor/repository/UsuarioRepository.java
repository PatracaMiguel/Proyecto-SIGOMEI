package com.sigomei.servidor.repository;

import com.sigomei.api.dto.UsuarioDTO;

public interface UsuarioRepository {

    UsuarioDTO buscarPorUsuario(String nombreUsuario);

    UsuarioDTO guardar(UsuarioDTO usuario);

    UsuarioDTO actualizar(UsuarioDTO usuario);
}