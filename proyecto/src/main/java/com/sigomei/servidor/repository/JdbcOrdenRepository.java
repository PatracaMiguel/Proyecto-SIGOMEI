package com.sigomei.servidor.repository;

import com.sigomei.api.catalogos.EstadoOrden;
import com.sigomei.api.catalogos.TipoMantenimiento;
import com.sigomei.api.dto.OrdenDTO;
import com.sigomei.servidor.config.ConexionBD;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcOrdenRepository implements OrdenRepository {

    @Override
    public OrdenDTO guardar(OrdenDTO orden) {
        String sql = """
                INSERT INTO orden_mantenimiento
                (id_orden, id_equipo, id_tecnico, tipo_mantenimiento, fecha_programada,
                 fecha_inicio, fecha_cierre, descripcion_trabajo, costo_estimado, costo_real, estado_orden)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            llenarInsert(statement, orden);
            statement.executeUpdate();
            return buscarPorId(orden.getIdOrden());
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo guardar la orden", e);
        }
    }

    @Override
    public OrdenDTO buscarPorId(int idOrden) {
        String sql = "SELECT * FROM orden_mantenimiento WHERE id_orden = ?";
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idOrden);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapear(resultSet);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo buscar la orden", e);
        }
    }

    @Override
    public List<OrdenDTO> consultarTodas() {
        String sql = "SELECT * FROM orden_mantenimiento ORDER BY id_orden";
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<OrdenDTO> ordenes = new ArrayList<>();
            while (resultSet.next()) {
                ordenes.add(mapear(resultSet));
            }
            return ordenes;
        } catch (SQLException e) {
            throw new RuntimeException("No se pudieron consultar las ordenes", e);
        }
    }

    @Override
    public List<OrdenDTO> filtrar(EstadoOrden estado, java.time.LocalDate fechaInicio, java.time.LocalDate fechaCierre) {
        List<OrdenDTO> resultado = new ArrayList<>();
        for (OrdenDTO orden : consultarTodas()) {
            boolean coincideEstado = estado == null || orden.getEstadoOrden() == estado;
            boolean despuesDeInicio = fechaInicio == null || !orden.getFechaProgramada().isBefore(fechaInicio);
            boolean antesDeCierre = fechaCierre == null || !orden.getFechaProgramada().isAfter(fechaCierre);
            if (coincideEstado && despuesDeInicio && antesDeCierre) {
                resultado.add(orden);
            }
        }
        return resultado;
    }

    @Override
    public OrdenDTO actualizar(OrdenDTO orden) {
        String sql = """
                UPDATE orden_mantenimiento
                SET id_equipo = ?, id_tecnico = ?, tipo_mantenimiento = ?, fecha_programada = ?,
                    fecha_inicio = ?, fecha_cierre = ?, descripcion_trabajo = ?, costo_estimado = ?,
                    costo_real = ?, estado_orden = ?
                WHERE id_orden = ?
                """;
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            llenarUpdate(statement, orden);
            statement.executeUpdate();
            return buscarPorId(orden.getIdOrden());
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo actualizar la orden", e);
        }
    }

    @Override
    public boolean existeOrdenActivaMismaFecha(int idEquipo, java.time.LocalDate fechaProgramada) {
        String sql = """
                SELECT 1 FROM orden_mantenimiento
                WHERE id_equipo = ? AND fecha_programada = ?
                  AND estado_orden IN ('PROGRAMADA', 'EN_EJECUCION')
                LIMIT 1
                """;
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idEquipo);
            statement.setDate(2, Date.valueOf(fechaProgramada));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo validar la orden duplicada", e);
        }
    }

    @Override
    public List<OrdenDTO> consultarHistorial(Integer idEquipo, Integer idTecnico, EstadoOrden estadoOrden) {
        List<OrdenDTO> resultado = new ArrayList<>();
        for (OrdenDTO orden : consultarTodas()) {
            boolean coincideEquipo = idEquipo == null || orden.getIdEquipo() == idEquipo;
            boolean coincideTecnico = idTecnico == null || orden.getIdTecnico() == idTecnico;
            boolean coincideEstado = estadoOrden == null || orden.getEstadoOrden() == estadoOrden;
            if (coincideEquipo && coincideTecnico && coincideEstado) {
                resultado.add(orden);
            }
        }
        return resultado;
    }

    @Override
    public void eliminar(int idOrden) {
        String sql = "DELETE FROM orden_mantenimiento WHERE id_orden = ?";
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idOrden);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo eliminar la orden", e);
        }
    }

    private void llenarInsert(PreparedStatement statement, OrdenDTO orden) throws SQLException {
        statement.setInt(1, orden.getIdOrden());
        statement.setInt(2, orden.getIdEquipo());
        statement.setInt(3, orden.getIdTecnico());
        statement.setString(4, orden.getTipoMantenimiento().name());
        statement.setDate(5, Date.valueOf(orden.getFechaProgramada()));
        setDateOrNull(statement, 6, orden.getFechaInicio());
        setDateOrNull(statement, 7, orden.getFechaCierre());
        statement.setString(8, orden.getDescripcionTrabajo());
        statement.setBigDecimal(9, orden.getCostoEstimado());
        statement.setBigDecimal(10, orden.getCostoReal());
        statement.setString(11, orden.getEstadoOrden().name());
    }

    private void llenarUpdate(PreparedStatement statement, OrdenDTO orden) throws SQLException {
        statement.setInt(1, orden.getIdEquipo());
        statement.setInt(2, orden.getIdTecnico());
        statement.setString(3, orden.getTipoMantenimiento().name());
        statement.setDate(4, Date.valueOf(orden.getFechaProgramada()));
        setDateOrNull(statement, 5, orden.getFechaInicio());
        setDateOrNull(statement, 6, orden.getFechaCierre());
        statement.setString(7, orden.getDescripcionTrabajo());
        statement.setBigDecimal(8, orden.getCostoEstimado());
        statement.setBigDecimal(9, orden.getCostoReal());
        statement.setString(10, orden.getEstadoOrden().name());
        statement.setInt(11, orden.getIdOrden());
    }

    private void setDateOrNull(PreparedStatement statement, int index, java.time.LocalDate date) throws SQLException {
        if (date == null) {
            statement.setDate(index, null);
        } else {
            statement.setDate(index, Date.valueOf(date));
        }
    }

    private OrdenDTO mapear(ResultSet resultSet) throws SQLException {
        Date fechaInicio = resultSet.getDate("fecha_inicio");
        Date fechaCierre = resultSet.getDate("fecha_cierre");
        return new OrdenDTO(
                resultSet.getInt("id_orden"),
                resultSet.getInt("id_equipo"),
                resultSet.getInt("id_tecnico"),
                TipoMantenimiento.valueOf(resultSet.getString("tipo_mantenimiento")),
                resultSet.getDate("fecha_programada").toLocalDate(),
                fechaInicio == null ? null : fechaInicio.toLocalDate(),
                fechaCierre == null ? null : fechaCierre.toLocalDate(),
                resultSet.getString("descripcion_trabajo"),
                resultSet.getBigDecimal("costo_estimado"),
                resultSet.getBigDecimal("costo_real"),
                EstadoOrden.valueOf(resultSet.getString("estado_orden"))
        );
    }
}
