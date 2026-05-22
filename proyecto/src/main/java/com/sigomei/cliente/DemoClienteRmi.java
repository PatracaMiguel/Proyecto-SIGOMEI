package com.sigomei.cliente;

import com.sigomei.api.catalogos.EstadoOrden;
import com.sigomei.api.catalogos.TipoMantenimiento;
import com.sigomei.api.dto.OrdenDTO;
import com.sigomei.servidor.rmi.SigomeiRemote;

import java.math.BigDecimal;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;

public class DemoClienteRmi {

    public static void main(String[] args) throws Exception {
        String host = System.getProperty("sigomei.rmi.host", "localhost");
        int port = Integer.parseInt(System.getProperty("sigomei.rmi.port", "1099"));
        Registry registry = LocateRegistry.getRegistry(host, port);
        SigomeiRemote sigomei = (SigomeiRemote) registry.lookup("SIGOMEI");

        System.out.println("Equipos remotos: " + sigomei.consultarEquipos().size());
        System.out.println("Tecnicos remotos: " + sigomei.consultarTecnicos().size());
        System.out.println("Ordenes remotas: " + sigomei.consultarOrdenes().size());

        OrdenDTO aceptada = new OrdenDTO(300, 2, 2, TipoMantenimiento.PREVENTIVO,
                LocalDate.of(2026, 7, 1), null, null, "Demo remoto aceptado",
                new BigDecimal("900.00"), null, EstadoOrden.PROGRAMADA);
        sigomei.registrarOrden(aceptada);
        System.out.println("Orden aceptada via RMI: " + aceptada.getIdOrden());

        OrdenDTO rechazada = new OrdenDTO(301, 1, 2, TipoMantenimiento.PREVENTIVO,
                LocalDate.of(2026, 7, 2), null, null, "Demo remoto rechazado",
                new BigDecimal("900.00"), null, EstadoOrden.PROGRAMADA);
        try {
            sigomei.registrarOrden(rechazada);
        } catch (Exception ex) {
            System.out.println("Regla de negocio rechazada via RMI: " + ex.getMessage());
        }
    }
}
