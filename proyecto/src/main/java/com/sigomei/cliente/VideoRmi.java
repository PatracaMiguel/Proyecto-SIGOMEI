package com.sigomei.cliente;

import com.sigomei.api.catalogos.Criticidad;
import com.sigomei.api.catalogos.EstadoOperativo;
import com.sigomei.api.catalogos.EstadoOrden;
import com.sigomei.api.catalogos.EstadoTecnico;
import com.sigomei.api.catalogos.NivelCertificacion;
import com.sigomei.api.catalogos.TipoEquipo;
import com.sigomei.api.catalogos.TipoMantenimiento;
import com.sigomei.api.dto.EquipoDTO;
import com.sigomei.api.dto.OrdenDTO;
import com.sigomei.api.dto.TecnicoDTO;
import com.sigomei.servidor.rmi.SigomeiRemote;

import java.math.BigDecimal;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;

public class VideoRmi {

    public static void main(String[] args) throws Exception {
        String host = System.getProperty("sigomei.rmi.host", "localhost");
        int port = Integer.parseInt(System.getProperty("sigomei.rmi.port", "1099"));
        Registry registry = LocateRegistry.getRegistry(host, port);
        SigomeiRemote sigomei = (SigomeiRemote) registry.lookup("SIGOMEI");

        System.out.println("video CRUD y reglas de negocio ");
        demoCrudEquipo(sigomei);
        demoCrudTecnico(sigomei);
        demoCrudOrden(sigomei);

        System.out.println();
        demoReglasNegocio(sigomei);
    }

    private static void demoCrudEquipo(SigomeiRemote sigomei) throws Exception {
        System.out.println();
        System.out.println("equipos ");

        EquipoDTO equipo = new EquipoDTO(900, "Equipo demo RMI", TipoEquipo.MECANICO,
                "Demo", "M-900", "SER-900", "Planta Demo",
                LocalDate.of(2025, 1, 10), EstadoOperativo.OPERATIVO, Criticidad.MEDIA);
        sigomei.registrarEquipo(equipo);
        System.out.println("crear equipo id=900");

        System.out.println("cosultar el total de equipos =" + sigomei.consultarEquipos().size());

        equipo.setNombre("Equipo demo RMI actualizado");
        sigomei.actualizarEquipo(equipo);
        System.out.println("actuaizar equipo id=900");

        sigomei.cambiarEstadoEquipo(900, EstadoOperativo.INACTIVO);
        System.out.println("cambio de estado el equipo id=900 a estado inactivo");

        sigomei.eliminarEquipo(900);
        System.out.println("borrar equipo id=900");
    }

    private static void demoCrudTecnico(SigomeiRemote sigomei) throws Exception {
        System.out.println();
        System.out.println("Tecnicos ");

        TecnicoDTO tecnico = new TecnicoDTO(900, "Tecnico Demo RMI",
                "DEMO900101AA1", "5559000000", "demo.tecnico@example.com",
                TipoEquipo.MECANICO, NivelCertificacion.II,
                LocalDate.of(2024, 1, 15), EstadoTecnico.ACTIVO);
        sigomei.registrarTecnico(tecnico);
        System.out.println("crear tecnico id=900");

        System.out.println("cosultar el total de tecnicos =" + sigomei.consultarTecnicos().size());

        tecnico.setTelefono("5559001111");
        sigomei.actualizarTecnico(tecnico);
        System.out.println("actuaizar tecnico id=900");

        sigomei.cambiarEstatusTecnico(900, EstadoTecnico.INACTIVO);
        System.out.println("cambio de estatus el tecnico id=900 estatus= inactivo");

        sigomei.eliminarTecnico(900);
        System.out.println("borrar tecnico id=900");
    }

    private static void demoCrudOrden(SigomeiRemote sigomei) throws Exception {
        System.out.println();
        System.out.println("Ordenes ");

        OrdenDTO orden = new OrdenDTO(900, 2, 2, TipoMantenimiento.PREVENTIVO,
                LocalDate.of(2026, 8, 1), null, null,
                "Orden demo RMI", new BigDecimal("900.00"),
                null, EstadoOrden.PROGRAMADA);
        sigomei.registrarOrden(orden);
        System.out.println("crear orden id=900");

        System.out.println("consultar el total de ordenes =" + sigomei.consultarOrdenes().size());

        orden.setDescripcionTrabajo("Orden demo RMI actualizada");
        sigomei.actualizarOrden(orden);
        System.out.println("actuaizar orden id=900");

        sigomei.cambiarEstadoOrden(900, EstadoOrden.EN_EJECUCION, null, null);
        System.out.println("cambio de estado en la  orden id=900 estado en ejecucion");

        sigomei.cambiarEstadoOrden(900, EstadoOrden.FINALIZADA,
                LocalDate.of(2026, 8, 2), new BigDecimal("950.00"));
        System.out.println("cambio de estado en la orden id=900 estado=FINALIZADA");

        sigomei.eliminarOrden(900);
        System.out.println("borrar orden id=900");
    }

    private static void demoReglasNegocio(SigomeiRemote sigomei) throws Exception {
        ejecutarAceptado("RN-01 positivo: tecnico compatible con equipo", () -> {
            OrdenDTO orden = new OrdenDTO(910, 2, 2, TipoMantenimiento.PREVENTIVO,
                    LocalDate.of(2026, 8, 10), null, null,
                    "RN-01 aceptada", new BigDecimal("1000.00"),
                    null, EstadoOrden.PROGRAMADA);
            sigomei.registrarOrden(orden);
        });

        ejecutarRechazado("RN-01 negativo: tecnico incompatible con equipo", () -> {
            OrdenDTO orden = new OrdenDTO(911, 1, 2, TipoMantenimiento.PREVENTIVO,
                    LocalDate.of(2026, 8, 11), null, null,
                    "RN-01 rechazada", new BigDecimal("1000.00"),
                    null, EstadoOrden.PROGRAMADA);
            sigomei.registrarOrden(orden);
        });

        ejecutarRechazado("RN-04 negativo: tecnico inactivo", () -> {
            OrdenDTO orden = new OrdenDTO(912, 1, 3, TipoMantenimiento.PREVENTIVO,
                    LocalDate.of(2026, 8, 12), null, null,
                    "RN-04 rechazada", new BigDecimal("1000.00"),
                    null, EstadoOrden.PROGRAMADA);
            sigomei.registrarOrden(orden);
        });

        ejecutarRechazado("RN-06 negativo: finalizar sin fecha ni costo real", () ->
                sigomei.cambiarEstadoOrden(2, EstadoOrden.FINALIZADA, null, null));

        ejecutarRechazado("RN-07 negativo: criticidad alta con certificacion I", () -> {
            OrdenDTO orden = new OrdenDTO(913, 1, 4, TipoMantenimiento.PREVENTIVO,
                    LocalDate.of(2026, 8, 13), null, null,
                    "RN-07 rechazada", new BigDecimal("1000.00"),
                    null, EstadoOrden.PROGRAMADA);
            sigomei.registrarOrden(orden);
        });

        ejecutarRechazado("RN-08 negativo: transicion finalizada a programada", () ->
                sigomei.cambiarEstadoOrden(3, EstadoOrden.PROGRAMADA, null, null));
    }

    private static void ejecutarAceptado(String nombre, Operacion operacion) {
        try {
            operacion.ejecutar();
            System.out.println(nombre + " -> aceptado");
        } catch (Exception ex) {
            System.out.println(nombre + " -> ERROR: " + ex.getMessage());
        }
    }

    private static void ejecutarRechazado(String nombre, Operacion operacion) {
        try {
            operacion.ejecutar();
            System.out.println(nombre + " -> ERROR: debio rechazarse");
        } catch (Exception ex) {
            System.out.println(nombre + " -> rechazado: " + ex.getMessage());
        }
    }

    private interface Operacion {
        void ejecutar() throws Exception;
    }
}
