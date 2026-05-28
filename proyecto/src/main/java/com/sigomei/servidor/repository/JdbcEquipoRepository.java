package com.sigomei.servidor.repository;

import com.sigomei.api.catalogos.Criticidad;
import com.sigomei.api.catalogos.EstadoOperativo;
import com.sigomei.api.catalogos.EstadoOrden;
import com.sigomei.api.catalogos.TipoEquipo;
import com.sigomei.api.dto.EquipoDTO;
import com.sigomei.servidor.config.ConexionBD;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcEquipoRepository implements EquipoRepository {

    @Override
    public EquipoDTO guardar(EquipoDTO equipo) {
        String sql = """
                INSERT INTO equipo
                (id_equipo, nombre, tipo, marca, modelo, numero_serie, ubicacion_planta,
                 fecha_instalacion, estado_operativo, criticidad)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            llenar(statement, equipo);
            statement.executeUpdate();
            return buscarPorId(equipo.getIdEquipo());
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo guardar el equipo", e);
        }
    }

    @Override
    public EquipoDTO buscarPorId(int idEquipo) {
        String sql = "SELECT * FROM equipo WHERE id_equipo = ?";
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idEquipo);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapear(resultSet);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo buscar el equipo", e);
        }
    }

    @Override
    public List<EquipoDTO> consultarTodos() {
        String sql = "SELECT * FROM equipo ORDER BY id_equipo";
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<EquipoDTO> equipos = new ArrayList<>();
            while (resultSet.next()) {
                equipos.add(mapear(resultSet));
            }
            return equipos;
        } catch (SQLException e) {
            throw new RuntimeException("No se pudieron consultar los equipos", e);
        }
    }

    @Override
    public List<EquipoDTO> filtrar(String nombre, TipoEquipo tipo, Criticidad criticidad) {
        List<EquipoDTO> resultado = new ArrayList<>();
        for (EquipoDTO equipo : consultarTodos()) {
            boolean coincideNombre = nombre == null || nombre.isBlank()
                    || equipo.getNombre().toLowerCase().contains(nombre.toLowerCase());
            boolean coincideTipo = tipo == null || equipo.getTipo() == tipo;
            boolean coincideCriticidad = criticidad == null || equipo.getCriticidad() == criticidad;
            if (coincideNombre && coincideTipo && coincideCriticidad) {
                resultado.add(equipo);
            }
        }
        return resultado;
    }

    @Override
    public EquipoDTO actualizar(EquipoDTO equipo) {
        String sql = """
                UPDATE equipo
                SET nombre = ?, tipo = ?, marca = ?, modelo = ?, numero_serie = ?,
                    ubicacion_planta = ?, fecha_instalacion = ?, estado_operativo = ?, criticidad = ?
                WHERE id_equipo = ?
                """;
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, equipo.getNombre());
            statement.setString(2, equipo.getTipo().name());
            statement.setString(3, equipo.getMarca());
            statement.setString(4, equipo.getModelo());
            statement.setString(5, equipo.getNumeroSerie());
            statement.setString(6, equipo.getUbicacionPlanta());
            statement.setDate(7, Date.valueOf(equipo.getFechaInstalacion()));
            statement.setString(8, equipo.getEstadoOperativo().name());
            statement.setString(9, equipo.getCriticidad().name());
            statement.setInt(10, equipo.getIdEquipo());
            statement.executeUpdate();
            return buscarPorId(equipo.getIdEquipo());
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo actualizar el equipo", e);
        }
    }

    @Override
    public boolean tieneOrdenesRelacionadas(int idEquipo) {
        return existeOrden(idEquipo, null);
    }

    @Override
    public boolean tieneOrdenesActivas(int idEquipo) {
        return existeOrden(idEquipo, "AND estado_orden IN ('PROGRAMADA', 'EN_EJECUCION')");
    }

    @Override
    public void eliminar(int idEquipo) {
        String sql = "DELETE FROM equipo WHERE id_equipo = ?";
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idEquipo);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo eliminar el equipo", e);
        }
    }

    private boolean existeOrden(int idEquipo, String condicionExtra) {
        String sql = "SELECT 1 FROM orden_mantenimiento WHERE id_equipo = ? "
                + (condicionExtra == null ? "" : condicionExtra) + " LIMIT 1";
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idEquipo);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudieron consultar ordenes del equipo", e);
        }
    }

    private void llenar(PreparedStatement statement, EquipoDTO equipo) throws SQLException {
        statement.setInt(1, equipo.getIdEquipo());
        statement.setString(2, equipo.getNombre());
        statement.setString(3, equipo.getTipo().name());
        statement.setString(4, equipo.getMarca());
        statement.setString(5, equipo.getModelo());
        statement.setString(6, equipo.getNumeroSerie());
        statement.setString(7, equipo.getUbicacionPlanta());
        statement.setDate(8, Date.valueOf(equipo.getFechaInstalacion()));
        statement.setString(9, equipo.getEstadoOperativo().name());
        statement.setString(10, equipo.getCriticidad().name());
    }

    private EquipoDTO mapear(ResultSet resultSet) throws SQLException {
        return new EquipoDTO(
                resultSet.getInt("id_equipo"),
                resultSet.getString("nombre"),
                TipoEquipo.valueOf(resultSet.getString("tipo")),
                resultSet.getString("marca"),
                resultSet.getString("modelo"),
                resultSet.getString("numero_serie"),
                resultSet.getString("ubicacion_planta"),
                resultSet.getDate("fecha_instalacion").toLocalDate(),
                EstadoOperativo.valueOf(resultSet.getString("estado_operativo")),
                Criticidad.valueOf(resultSet.getString("criticidad"))
        );
    }
}
