package com.sigomei.servidor.repository;

import com.sigomei.api.catalogos.EstadoOrden;
import com.sigomei.api.catalogos.EstadoTecnico;
import com.sigomei.api.catalogos.NivelCertificacion;
import com.sigomei.api.catalogos.TipoEquipo;
import com.sigomei.api.dto.TecnicoDTO;
import com.sigomei.servidor.config.ConexionBD;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTecnicoRepository implements TecnicoRepository {

    @Override
    public TecnicoDTO guardar(TecnicoDTO tecnico) {
        String sql = """
                INSERT INTO tecnico
                (id_tecnico, nombre_completo, rfc, telefono, correo, especialidad,
                 nivel_certificacion, fecha_ingreso, estatus)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            llenar(statement, tecnico);
            statement.executeUpdate();
            return buscarPorId(tecnico.getIdTecnico());
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo guardar el tecnico", e);
        }
    }

    @Override
    public TecnicoDTO buscarPorId(int idTecnico) {
        String sql = "SELECT * FROM tecnico WHERE id_tecnico = ?";
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idTecnico);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapear(resultSet);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo buscar el tecnico", e);
        }
    }

    @Override
    public List<TecnicoDTO> consultarTodos() {
        String sql = "SELECT * FROM tecnico ORDER BY id_tecnico";
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<TecnicoDTO> tecnicos = new ArrayList<>();
            while (resultSet.next()) {
                tecnicos.add(mapear(resultSet));
            }
            return tecnicos;
        } catch (SQLException e) {
            throw new RuntimeException("No se pudieron consultar los tecnicos", e);
        }
    }

    @Override
    public List<TecnicoDTO> filtrar(String nombre, TipoEquipo especialidad, NivelCertificacion nivelCertificacion) {
        List<TecnicoDTO> resultado = new ArrayList<>();
        for (TecnicoDTO tecnico : consultarTodos()) {
            boolean coincideNombre = nombre == null || nombre.isBlank()
                    || tecnico.getNombreCompleto().toLowerCase().contains(nombre.toLowerCase());
            boolean coincideEspecialidad = especialidad == null || tecnico.getEspecialidad() == especialidad;
            boolean coincideNivel = nivelCertificacion == null
                    || tecnico.getNivelCertificacion() == nivelCertificacion;
            if (coincideNombre && coincideEspecialidad && coincideNivel) {
                resultado.add(tecnico);
            }
        }
        return resultado;
    }

    @Override
    public TecnicoDTO actualizar(TecnicoDTO tecnico) {
        String sql = """
                UPDATE tecnico
                SET nombre_completo = ?, rfc = ?, telefono = ?, correo = ?, especialidad = ?,
                    nivel_certificacion = ?, fecha_ingreso = ?, estatus = ?
                WHERE id_tecnico = ?
                """;
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tecnico.getNombreCompleto());
            statement.setString(2, tecnico.getRfc());
            statement.setString(3, tecnico.getTelefono());
            statement.setString(4, tecnico.getCorreo());
            statement.setString(5, tecnico.getEspecialidad().name());
            statement.setString(6, tecnico.getNivelCertificacion().name());
            statement.setDate(7, Date.valueOf(tecnico.getFechaIngreso()));
            statement.setString(8, tecnico.getEstatus().name());
            statement.setInt(9, tecnico.getIdTecnico());
            statement.executeUpdate();
            return buscarPorId(tecnico.getIdTecnico());
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo actualizar el tecnico", e);
        }
    }

    @Override
    public boolean tieneOrdenesRelacionadas(int idTecnico) {
        return existeOrden(idTecnico, null);
    }

    @Override
    public boolean tieneOrdenesActivas(int idTecnico) {
        return existeOrden(idTecnico, "AND estado_orden IN ('PROGRAMADA', 'EN_EJECUCION')");
    }

    @Override
    public void eliminar(int idTecnico) {
        String sql = "DELETE FROM tecnico WHERE id_tecnico = ?";
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idTecnico);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo eliminar el tecnico", e);
        }
    }

    private boolean existeOrden(int idTecnico, String condicionExtra) {
        String sql = "SELECT 1 FROM orden_mantenimiento WHERE id_tecnico = ? "
                + (condicionExtra == null ? "" : condicionExtra) + " LIMIT 1";
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idTecnico);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudieron consultar ordenes del tecnico", e);
        }
    }

    private void llenar(PreparedStatement statement, TecnicoDTO tecnico) throws SQLException {
        statement.setInt(1, tecnico.getIdTecnico());
        statement.setString(2, tecnico.getNombreCompleto());
        statement.setString(3, tecnico.getRfc());
        statement.setString(4, tecnico.getTelefono());
        statement.setString(5, tecnico.getCorreo());
        statement.setString(6, tecnico.getEspecialidad().name());
        statement.setString(7, tecnico.getNivelCertificacion().name());
        statement.setDate(8, Date.valueOf(tecnico.getFechaIngreso()));
        statement.setString(9, tecnico.getEstatus().name());
    }

    private TecnicoDTO mapear(ResultSet resultSet) throws SQLException {
        return new TecnicoDTO(
                resultSet.getInt("id_tecnico"),
                resultSet.getString("nombre_completo"),
                resultSet.getString("rfc"),
                resultSet.getString("telefono"),
                resultSet.getString("correo"),
                TipoEquipo.valueOf(resultSet.getString("especialidad")),
                NivelCertificacion.valueOf(resultSet.getString("nivel_certificacion")),
                resultSet.getDate("fecha_ingreso").toLocalDate(),
                EstadoTecnico.valueOf(resultSet.getString("estatus"))
        );
    }
}
