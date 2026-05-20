package com.sigomei.servidor.config;

import java.sql.Connection;

public class PruebaConexion {

    public static void main(String[] args) {
        try {
            Connection conexion = ConexionBD.obtenerConexion();
            System.out.println("Conexión exitosa a la base de datos SIGOMEI");
            conexion.close();
        } catch (Exception e) {
            System.out.println("Error al conectar con la base de datos");
            e.printStackTrace();
        }
    }
}