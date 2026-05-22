package com.sigomei.servidor.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    public static Connection obtenerConexion() throws SQLException {
        String url = AppConfig.get("db.url", "jdbc:mysql://localhost:3306/sigomei_db");
        String usuario = AppConfig.get("db.user", "");
        String contrasena = AppConfig.get("db.password", "");
        return DriverManager.getConnection(url, usuario, contrasena);
    }
}
