# Evidencias de entrega SIGOMEI

Fecha de ejecucion: 2026-05-21  
Ejecutor: Miguel Angel Patraca Lagunes

## 1. Pruebas unitarias E3 en verde

Comando ejecutado desde `proyecto`:

```powershell
mvn clean test
```

Salida relevante:

```text
Running com.sigomei.servidor.service.CrudServiceTest
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
Running com.sigomei.servidor.service.ReglasNegocioServiceTest
Tests run: 26, Failures: 0, Errors: 0, Skipped: 0
Running com.sigomei.servidor.service.SistemaE2Test
Tests run: 32, Failures: 0, Errors: 0, Skipped: 0
Results:
Tests run: 61, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 2. Evidencia RMI

Comandos:

```powershell
java -cp target\classes com.sigomei.servidor.rmi.SigomeiServer
java -cp target\classes com.sigomei.cliente.DemoClienteRmi
```

Salida del cliente:

```text
Equipos remotos: 3
Tecnicos remotos: 5
Ordenes remotas: 3
Orden aceptada via RMI: 300
Regla de negocio rechazada via RMI: La especialidad del tecnico no coincide con el equipo
```

Log generado: `proyecto/logs/server.log`.

## 3. Seguimiento de casos de sistema

| Caso | RF/RN | Descripcion | Estado | Fecha | Ejecutor | Evidencia |
| --- | --- | --- | --- | --- | --- | --- |
| CP-01 | RNF-01 | Cliente opera por RMI sin conexion directa a BD | Aprobado | 2026-05-21 | Miguel | Consola RMI + revision `DemoClienteRmi` |
| CP-02 | RF-06/RF-07 | Registrar equipo | Aprobado | 2026-05-21 | Miguel | `CrudServiceTest.crudEquipo_remotoCompatibleConContrato` |
| CP-03 | RF-09/RF-12 | Consultar equipos | Aprobado | 2026-05-21 | Miguel | Demo RMI: `Equipos remotos: 3` |
| CP-04 | RF-13/RF-16 | Actualizar equipo | Aprobado | 2026-05-21 | Miguel | `CrudServiceTest.crudEquipo_remotoCompatibleConContrato` |
| CP-05 | RF-17/RF-22 | Registrar tecnico | Aprobado | 2026-05-21 | Miguel | `CrudServiceTest.crudTecnico_remotoCompatibleConContrato` |
| CP-06 | RF-25/RF-27 | Consultar/filtrar tecnico | Aprobado | 2026-05-21 | Miguel | Demo RMI: `Tecnicos remotos: 5` |
| CP-07 | RF-28/RF-30 | Actualizar tecnico | Aprobado | 2026-05-21 | Miguel | `CrudServiceTest.crudTecnico_remotoCompatibleConContrato` |
| CP-08 | RF-31/RF-32/RN-04 | Cambiar estatus tecnico | Aprobado | 2026-05-21 | Miguel | RN-04 y servicio `cambiarEstatusTecnico` |
| CP-09 | RF-33/RF-35/RN-03 | Eliminar tecnico sin/con ordenes | Aprobado | 2026-05-21 | Miguel | RN-03 |
| CP-10 | RF-36/RF-47/RN-01/RN-02/RN-04/RN-05/RN-07 | Registrar orden con reglas | Aprobado | 2026-05-21 | Miguel | RN-01, RN-02, RN-04, RN-05, RN-07 |
| CP-11 | RF-50/RF-52 | Consultar ordenes | Aprobado | 2026-05-21 | Miguel | Demo RMI: `Ordenes remotas: 3` |
| CP-12 | RF-53/RF-55/RN-08 | Cambiar estado de orden | Aprobado | 2026-05-21 | Miguel | RN-08 |
| CP-13 | RF-62/RF-64/RN-05/RN-06/RN-08 | Finalizar orden | Aprobado | 2026-05-21 | Miguel | RN-06, RN-08 |
| CP-14 | RF-65/RN-08 | Cancelar orden valida | Aprobado | 2026-05-21 | Miguel | RN-09/cancelacion |
| CP-15 | RF-67/RF-69/RN-08 | Rechazar cancelacion invalida | Aprobado | 2026-05-21 | Miguel | `rn09_negativo_cancelarOrdenFinalizada` |
| CP-16 | RF-71/RF-72 | Consultar historial filtrado | Aprobado | 2026-05-21 | Miguel | `SistemaE2Test.cp16_consultarHistorialFiltrado` |
| CP-17 | RF-73/RF-74/RF-76 | Cambiar tecnico sin ordenes a inactivo | Aprobado | 2026-05-21 | Miguel | `SistemaE2Test.cp17_cambiarTecnicoSinOrdenesAInactivo` |
| CP-18 | RF-77/RF-78/RF-80 | Cambiar equipo sin ordenes a inactivo | Aprobado | 2026-05-21 | Miguel | `SistemaE2Test.cp18_cambiarEquipoSinOrdenesAInactivo` |
| CP-19 | RF-07/RF-08 | Rechazar equipo con nombre vacio | Aprobado | 2026-05-21 | Miguel | `SistemaE2Test.cp19_rechazarEquipoConNombreVacio` |
| CP-20 | RF-22/RF-23 | Rechazar tecnico con correo vacio | Aprobado | 2026-05-21 | Miguel | `SistemaE2Test.cp20_rechazarTecnicoConCorreoVacio` |
| CP-21 | RF-44/RF-45 | Rechazar orden con campo vacio | Aprobado | 2026-05-21 | Miguel | `SistemaE2Test.cp21_rechazarOrdenConCampoVacio` |
| CP-22 | RF-46/RF-47/RN-01 | Rechazar tecnico incompatible | Aprobado | 2026-05-21 | Miguel | `SistemaE2Test.cp22_rechazarOrdenConTecnicoIncompatible` |
| CP-23 | RF-48/RF-49/RN-02 | Rechazar orden duplicada | Aprobado | 2026-05-21 | Miguel | `SistemaE2Test.cp23_rechazarOrdenDuplicadaMismoEquipoYFecha` |
| CP-24 | RF-56/RF-57/RN-04 | Rechazar tecnico inactivo | Aprobado | 2026-05-21 | Miguel | `SistemaE2Test.cp24_rechazarOrdenConTecnicoInactivo` |
| CP-25 | RF-58/RF-59/RN-07 | Rechazar criticidad alta con certificacion I | Aprobado | 2026-05-21 | Miguel | `SistemaE2Test.cp25_rechazarCriticidadAltaConCertificacionI` |
| CP-26 | RF-60/RF-61/RN-05 | Rechazar fecha de inicio anterior a programada | Aprobado | 2026-05-21 | Miguel | `SistemaE2Test.cp26_rechazarFechaInicioAntesDeProgramada` |
| CP-27 | RF-62/RF-64/RN-08 | Rechazar cambio Finalizada a Programada | Aprobado | 2026-05-21 | Miguel | `SistemaE2Test.cp27_rechazarCambioFinalizadaAProgramada` |
| CP-28 | RF-65/RF-66/RN-06 | Rechazar finalizar sin fecha de cierre | Aprobado | 2026-05-21 | Miguel | `SistemaE2Test.cp28_rechazarFinalizarSinFechaCierre` |
| CP-29 | RF-67/RF-69/RN-08 | Rechazar cancelar orden finalizada | Aprobado | 2026-05-21 | Miguel | `SistemaE2Test.cp29_rechazarCancelarOrdenFinalizada` |
| CP-30 | RF-73/RF-75/RN-03 | Rechazar inactivar tecnico con ordenes activas | Aprobado | 2026-05-21 | Miguel | `SistemaE2Test.cp30_rechazarInactivarTecnicoConOrdenesActivas` |
| CP-31 | RF-77/RF-79/RN-03 | Rechazar inactivar equipo relacionado a orden | Aprobado | 2026-05-21 | Miguel | `SistemaE2Test.cp31_rechazarInactivarEquipoRelacionadoAOrden` |
| CP-32 | RNF-07/RNF-09 | Mostrar error cuando servidor RMI no esta disponible | Aprobado | 2026-05-21 | Miguel | `SistemaE2Test.cp32_mostrarErrorCuandoServidorRmiNoDisponible` |

Nota de capturas: el repo deja evidencia reproducible por consola, pruebas y log. Las capturas de pantalla y el video de maximo 3 minutos deben tomarse durante la ejecucion del servidor y `DemoClienteRmi` o del cliente grafico si se agrega UI Swing.

## 4. Bitacora de defectos

| ID | Severidad | Prioridad | Pasos para reproducir | Resultado obtenido | Evidencia | Estado |
| --- | --- | --- | --- | --- | --- | --- |
| DEF-01 | Alta | Alta | Ejecutar `mvn test` antes de implementar servicios | 26 pruebas fallaban por `UnsupportedOperationException` | Surefire previo | Corregido |
| DEF-02 | Alta | Alta | Registrar orden con tecnico incompatible | El sistema no aplicaba RN-01 | `rn01_negativo_tecnicoIncompatible` | Corregido |
| DEF-03 | Media | Alta | Finalizar orden sin fecha/costo | No existia validacion RN-06 | `rn06_negativo_finalizarSinDatos` | Corregido |
| DEF-04 | Media | Media | Revisar `ConexionBD.java` | Credenciales estaban embebidas | `config/app.properties.example` y `AppConfig` | Corregido |
| DEF-05 | Media | Media | Ejecutar demo RMI y revisar logs | No existia bitacora del servidor | `logs/server.log` | Corregido |

## 5. Auditoria de suite unitaria

| Propuesta | RF/RN | Justificacion | Prueba sugerida |
| --- | --- | --- | --- |
| Validar costo real negativo | RN-06 | Una orden finalizada no deberia aceptar costo real menor que cero. | `rn06_negativo_finalizarConCostoRealNegativo` |
| Validar duplicado al actualizar orden | RN-02 | La RN-02 no solo debe aplicar al registrar, tambien al actualizar. | `rn02_negativo_actualizarOrdenGeneraDuplicadoActivo` |
| Validar credenciales de usuario inactivo | RF inicio sesion / Seguridad | Evita que un usuario bloqueado opere el sistema. | `autenticacion_negativo_usuarioInactivo` |
| Validar filtros sin resultados | RF consultas | Garantiza respuestas vacias controladas y reproducibles. | `filtrarOrdenes_sinCoincidencias_retornaListaVacia` |

## 6. Trazabilidad preliminar

| RF/RN | Caso ejecutado | Estado |
| --- | --- | --- |
| RN-01 | CP-10, CP-22, `rn01_*` | Aprobado |
| RN-02 | CP-10, CP-23, `rn02_*` | Aprobado |
| RN-03 | CP-09, CP-30, CP-31, `rn03_*` | Aprobado |
| RN-04 | CP-08, CP-10, CP-24, `rn04_*` | Aprobado |
| RN-05 | CP-10, CP-13, CP-26, `rn05_*` | Aprobado |
| RN-06 | CP-13, CP-28, `rn06_*` | Aprobado |
| RN-07 | CP-10, CP-25, `rn07_*` | Aprobado |
| RN-08 | CP-12, CP-13, CP-14, CP-15, CP-27, CP-29, `rn08_*`, `rn09_*` | Aprobado |
| RF CRUD Equipo | CP-02, CP-03, CP-04 | Aprobado |
| RF CRUD Tecnico | CP-05, CP-06, CP-07, CP-09 | Aprobado |
| RF CRUD Orden | CP-10, CP-11, CP-12, CP-13, CP-14 | Aprobado |
| RNF-01/RNF-04 separacion cliente-servidor | CP-01 | Aprobado |
