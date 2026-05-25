package com.sigomei.servidor.service.audit;

import com.sigomei.api.catalogos.EstadoOrden;
import com.sigomei.api.catalogos.EstadoTecnico;
import com.sigomei.api.catalogos.NivelCertificacion;
import com.sigomei.api.catalogos.TipoEquipo;
import com.sigomei.api.dto.TecnicoDTO;
import com.sigomei.api.excepciones.ReglaNegocioException;
import com.sigomei.servidor.service.OrdenService;
import com.sigomei.servidor.service.TecnicoService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class AuditoriaSuitePendienteTest {

    private final TecnicoService tecnicoService = new TecnicoService();
    private final OrdenService ordenService = new OrdenService();

    @Disabled("CP-33 pendiente: falta implementar validacion de correo duplicado")
    @Test
    public void cp33_rechazarTecnicoConCorreoDuplicado() {
        TecnicoDTO tecnico = new TecnicoDTO(
                33,
                "Tecnico correo duplicado",
                "DUPE900101AA1",
                "5553333333",
                "ana@example.com",
                TipoEquipo.ELECTRICO,
                NivelCertificacion.II,
                LocalDate.of(2024, 1, 15),
                EstadoTecnico.ACTIVO
        );

        assertThrows(ReglaNegocioException.class, () -> tecnicoService.registrarTecnico(tecnico));
    }

    @Disabled("CP-34 pendiente: prueba propuesta para reforzar RN-08")
    @Test
    public void cp34_rechazarFinalizarOrdenCancelada() throws Exception {
        ordenService.cambiarEstadoOrden(1, EstadoOrden.CANCELADA, null, null);

        assertThrows(ReglaNegocioException.class, () ->
                ordenService.cambiarEstadoOrden(
                        1,
                        EstadoOrden.FINALIZADA,
                        LocalDate.of(2026, 5, 25),
                        new BigDecimal("2500.00")
                )
        );
    }

    @Disabled("CP-35 pendiente: requiere prueba manual apagando el servidor RMI durante una operacion CRUD")
    @Test
    public void cp35_mostrarErrorSiServidorSeDesconectaDuranteCrud() {
        fail("Prueba manual pendiente: iniciar cliente, apagar servidor durante registro y validar mensaje controlado.");
    }
}
