package com.sigomei.servidor.repository;

import com.sigomei.api.catalogos.EstatusUsuario;
import com.sigomei.api.catalogos.RolUsuario;
import com.sigomei.api.dto.UsuarioDTO;
import com.sigomei.servidor.config.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcUsuarioRepository implements UsuarioRepository {

    @Override
    public UsuarioDTO buscarPorUsuario(String nombreUsuario) {
        String sql = "SELECT * FROM usuario WHERE nombre_usuario = ?";
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nombreUsuario);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapear(resultSet);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo buscar el usuario", e);
        }
    }

    @Override
    public UsuarioDTO guardar(UsuarioDTO usuario) {
        String sql = """
                INSERT INTO usuario (id_usuario, nombre_usuario, contrasena, rol, estatus)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, usuario.getIdUsuario());
            statement.setString(2, usuario.getNombreUsuario());
            statement.setString(3, usuario.getContrasena());
            statement.setString(4, usuario.getRol().name());
            statement.setString(5, usuario.getEstatus().name());
            statement.executeUpdate();
            return buscarPorUsuario(usuario.getNombreUsuario());
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo guardar el usuario", e);
        }
    }

    @Override
    public UsuarioDTO actualizar(UsuarioDTO usuario) {
        String sql = """
                UPDATE usuario
                SET nombre_usuario = ?, contrasena = ?, rol = ?, estatus = ?
                WHERE id_usuario = ?
                """;
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, usuario.getNombreUsuario());
            statement.setString(2, usuario.getContrasena());
            statement.setString(3, usuario.getRol().name());
            statement.setString(4, usuario.getEstatus().name());
            statement.setInt(5, usuario.getIdUsuario());
            statement.executeUpdate();
            return buscarPorUsuario(usuario.getNombreUsuario());
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo actualizar el usuario", e);
        }
    }

    private UsuarioDTO mapear(ResultSet resultSet) throws SQLException {
        return new UsuarioDTO(
                resultSet.getInt("id_usuario"),
                resultSet.getString("nombre_usuario"),
                resultSet.getString("contrasena"),
                RolUsuario.valueOf(resultSet.getString("rol")),
                EstatusUsuario.valueOf(resultSet.getString("estatus"))
        );
    }
}
