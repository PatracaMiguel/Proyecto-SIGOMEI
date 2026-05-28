from pathlib import Path
import textwrap


OUT = Path(__file__).with_name("exposicion_sigomei.pdf")
PAGE_W, PAGE_H = 612, 792
MARGIN = 48
BOTTOM = 46


def esc(text):
    return text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)")


class PDF:
    def __init__(self):
        self.pages = []
        self.lines = []
        self.y = PAGE_H - MARGIN

    def new_page(self):
        if self.lines:
            self.pages.append("\n".join(self.lines))
        self.lines = []
        self.y = PAGE_H - MARGIN

    def ensure(self, needed=18):
        if self.y - needed < BOTTOM:
            self.new_page()

    def text(self, value, size=10.5, bold=False, indent=0, gap=4):
        max_chars = max(25, int((PAGE_W - 2 * MARGIN - indent) / (size * 0.52)))
        for raw in str(value).splitlines() or [""]:
            parts = textwrap.wrap(raw, width=max_chars) if raw else [""]
            for part in parts:
                self.ensure(size + 5)
                font = "F2" if bold else "F1"
                self.lines.append(f"BT /{font} {size:.1f} Tf {MARGIN + indent:.1f} {self.y:.1f} Td ({esc(part)}) Tj ET")
                self.y -= size + 3
        self.y -= gap

    def bullet(self, value):
        self.text("- " + value, size=10.1, indent=8, gap=2)

    def heading(self, value):
        self.ensure(35)
        self.y -= 4
        self.text(value, size=15.5, bold=True, gap=6)

    def subheading(self, value):
        self.text(value, size=12.2, bold=True, gap=4)

    def table(self, rows, widths=None):
        for row in rows:
            line = " | ".join(str(cell) for cell in row)
            self.text(line, size=9.0, gap=1)
        self.y -= 4

    def build(self):
        self.new_page()
        objects = []
        objects.append("<< /Type /Catalog /Pages 2 0 R >>")
        kids = " ".join(f"{3 + i * 2} 0 R" for i in range(len(self.pages)))
        objects.append(f"<< /Type /Pages /Kids [{kids}] /Count {len(self.pages)} >>")
        for i, content in enumerate(self.pages):
            page_obj = 3 + i * 2
            content_obj = page_obj + 1
            objects.append(
                f"<< /Type /Page /Parent 2 0 R /MediaBox [0 0 {PAGE_W} {PAGE_H}] "
                f"/Resources << /Font << /F1 << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> "
                f"/F2 << /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >> >> >> "
                f"/Contents {content_obj} 0 R >>"
            )
            data = content.encode("latin-1", "replace")
            objects.append(f"<< /Length {len(data)} >>\nstream\n{content}\nendstream")
        chunks = ["%PDF-1.4\n%\xE2\xE3\xCF\xD3\n"]
        offsets = [0]
        pos = len(chunks[0].encode("latin-1", "replace"))
        for n, obj in enumerate(objects, 1):
            offsets.append(pos)
            chunk = f"{n} 0 obj\n{obj}\nendobj\n"
            chunks.append(chunk)
            pos += len(chunk.encode("latin-1", "replace"))
        xref = pos
        chunks.append(f"xref\n0 {len(objects)+1}\n0000000000 65535 f \n")
        for off in offsets[1:]:
            chunks.append(f"{off:010d} 00000 n \n")
        chunks.append(f"trailer\n<< /Size {len(objects)+1} /Root 1 0 R >>\nstartxref\n{xref}\n%%EOF\n")
        OUT.write_bytes("".join(chunks).encode("latin-1", "replace"))


pdf = PDF()
pdf.text("GUIA DE EXPOSICION - PROYECTO SIGOMEI", size=22, bold=True, gap=8)
pdf.text("Arquitectura distribuida con Java RMI, contrato cliente-servidor, ciclo TDD, matriz de trazabilidad, cobertura, demos y preguntas tecnicas.", size=12, gap=12)
pdf.table([
    ("Tecnologia", "Java RMI + JavaFX + JUnit 5 + JaCoCo"),
    ("Servicio remoto", "SIGOMEI publicado por SigomeiServer en el registry RMI"),
    ("Entidades CRUD", "Equipo, Tecnico y Orden"),
    ("Resultado pruebas", "mvn test: 71 pruebas, 0 fallas, 0 errores, 1 omitida manual"),
])

pdf.heading("1. Apertura sugerida")
pdf.text('"Nuestro sistema se llama SIGOMEI y resuelve la gestion de mantenimiento industrial. Lo implementamos como una aplicacion distribuida: el cliente JavaFX invoca un servicio remoto publicado por el servidor mediante Java RMI. La capa de servicio concentra validaciones, reglas de negocio y operaciones CRUD sobre equipos, tecnicos y ordenes."', size=11)

pdf.heading("2. Arquitectura distribuida implementada")
pdf.bullet("Cliente: SigomeiFxApp. Presenta pantallas, captura datos, filtra informacion y llama metodos remotos del contrato SigomeiRemote.")
pdf.bullet("Servidor: SigomeiServer crea el registry RMI, instancia SigomeiRemoteImpl y publica el servicio con el nombre SIGOMEI.")
pdf.bullet("Implementacion remota: SigomeiRemoteImpl extiende UnicastRemoteObject y delega en UsuarioService, EquipoService, TecnicoService y OrdenService.")
pdf.bullet("Servicios: contienen validaciones, reglas de negocio, CRUD e historial. El cliente no accede directamente a la logica interna.")
pdf.bullet("Datos compartidos: DTOs y catalogos en api/dto y api/catalogos viajan entre cliente y servidor.")
pdf.subheading("Lectura rapida")
pdf.text("El cliente solo conoce el contrato. El servidor conserva la autoridad de negocio. Si una regla falla, el servidor lanza una excepcion de dominio y el cliente la muestra al usuario.")

pdf.heading("3. Justificacion tecnica de RMI")
for item in [
    "Encaja con el proyecto porque cliente y servidor estan hechos en Java.",
    "Permite invocar metodos remotos tipados casi como metodos locales.",
    "Evita construir un protocolo manual con sockets: codigos de operacion, parseo, serializacion y manejo de errores.",
    "Transporta DTOs y excepciones de dominio, lo que hace mas claro el contrato.",
    "Su trade-off es que acopla a Java; aun asi, para este proyecto academico Java-Java reduce complejidad y favorece la claridad."
]:
    pdf.bullet(item)

pdf.heading("4. Contrato de comunicacion")
pdf.text("El contrato esta en SigomeiRemote, que extiende Remote. Cada metodo declara RemoteException y, cuando aplica, excepciones de dominio: ValidacionException, ReglaNegocioException, RegistroNoEncontradoException y AutenticacionException.")
pdf.table([
    ("Modulo", "Operaciones principales", "Errores controlados"),
    ("Sesion", "iniciarSesion, cerrarSesion", "Credenciales invalidas o usuario inactivo"),
    ("Equipo", "registrar, consultar, filtrar, actualizar, cambiar estado, eliminar", "Campos obligatorios, serie duplicada, equipo relacionado a orden"),
    ("Tecnico", "registrar, consultar, filtrar, actualizar, cambiar estatus, eliminar", "RFC/correo duplicado, tecnico con ordenes activas"),
    ("Orden", "registrar, consultar, filtrar, actualizar, cambiar estado, historial, eliminar", "Tecnico incompatible, duplicidad, fechas invalidas, transicion no permitida"),
])

pdf.heading("5. Ciclo TDD aplicado")
pdf.table([
    ("Etapa", "Aplicacion en SIGOMEI"),
    ("Plan", "Se identificaron entidades, RF, reglas RN01-RN10 y casos CP01-CP35."),
    ("Diseno", "Se escribieron pruebas positivas y negativas en ReglasNegocioServiceTest y ValidacionesServiceTest."),
    ("ROJO", "La prueba fallaba cuando la regla aun no existia o no lanzaba la excepcion esperada."),
    ("VERDE", "Se implemento la logica minima en servicios para pasar la prueba."),
    ("Ejecucion", "Se corrio mvn test con JUnit 5 y Surefire."),
    ("Reporte", "JaCoCo genero cobertura de la capa com.sigomei.servidor.service."),
])
pdf.text("Frase clave: TDD no fue probar al final; las pruebas definieron que se acepta, que se rechaza y que excepcion debe regresar el servicio.", bold=True)

pdf.heading("6. Matriz de trazabilidad RF/RN - Caso - Estado")
pdf.table([
    ("RF/RN", "Caso", "Estado"),
    ("RF-01 Iniciar sesion", "CP01 + usuario_cubreAutenticacionCorrectaEIncorrecta", "Aprobado"),
    ("RF-02 CRUD Equipo", "CP02-CP04 + CrudServiceTest.crudEquipo", "Aprobado"),
    ("RF-03 CRUD Tecnico", "CP05-CP09 + CrudServiceTest.crudTecnico", "Aprobado"),
    ("RF-04 CRUD Orden", "CP10-CP16 + CrudServiceTest.crudOrden", "Aprobado"),
    ("RN-01 Tecnico compatible", "rn01 positivo/negativo", "Aprobado"),
    ("RN-02 Sin orden activa duplicada", "rn02_negativo_ordenDuplicada + CP23", "Aprobado"),
    ("RN-03 No eliminar/inactivar con ordenes", "rn03 + CP30 + CP31", "Aprobado"),
    ("RN-04 Tecnico activo", "rn04_negativo_tecnicoInactivo + CP24", "Aprobado"),
    ("RN-05 Fechas cronologicas", "rn05 + CP26", "Aprobado"),
    ("RN-06 Finalizacion completa", "rn06 + CP28", "Aprobado"),
    ("RN-07 Criticidad alta", "rn07 + CP25", "Aprobado"),
    ("RN-08 Transiciones de estado", "rn08 + CP27", "Aprobado"),
    ("CP-35 Desconexion durante operacion", "Prueba deshabilitada", "Manual"),
])

pdf.heading("7. Reporte de cobertura")
pdf.table([
    ("Metrica", "Resultado", "Interpretacion"),
    ("Instrucciones", "1839/1858 = 98.98%", "La mayoria de la logica de servicio fue ejecutada"),
    ("Ramas", "296/320 = 92.50%", "Se probaron condiciones positivas y negativas"),
    ("Lineas", "343/346 = 99.13%", "Casi todas las lineas de servicio fueron recorridas"),
    ("Metodos", "51/51 = 100%", "Todos los metodos de servicio incluidos fueron ejercitados"),
    ("Maven", "71 pruebas, 0 fallas, 0 errores, 1 omitida", "La omitida es manual por desconexion del servidor"),
])

pdf.heading("8. Demostracion en vivo recomendada")
pdf.subheading("CRUD remoto de 3 entidades")
for step in [
    "Levantar SigomeiServer desde el IDE o comando configurado.",
    "Abrir SigomeiFxApp y entrar con admin / admin123.",
    "Equipo: registrar, filtrar y actualizar ubicacion o estado.",
    "Tecnico: registrar, editar telefono y eliminar si no tiene ordenes.",
    "Orden: registrar con equipo/tecnico compatibles, actualizar descripcion y cambiar de PROGRAMADA a EN_EJECUCION."
]:
    pdf.bullet(step)
pdf.subheading("Tres casos obligatorios")
pdf.table([
    ("Caso", "Que mostrar", "Mensaje"),
    ("Unitaria con memoria", "mvn -Dtest=ReglasNegocioServiceTest test", "InMemorySigomeiStore aisla la capa de servicio sin depender de MySQL"),
    ("Negativo GUI", "Dejar fecha vacia o mal formada en Equipo", "La GUI captura la excepcion y muestra dialogo de error"),
    ("Regla de negocio", "Orden con tecnico incompatible o duplicada", "El servidor rechaza con ReglaNegocioException"),
])
pdf.subheading("Tres reglas aceptado/rechazado")
pdf.table([
    ("Regla", "Aceptado", "Rechazado"),
    ("RN-01", "Equipo electrico + tecnico electrico", "Equipo electrico + tecnico mecanico"),
    ("RN-02", "Mismo equipo en fecha distinta", "Mismo equipo, misma fecha, ambas activas"),
    ("RN-06", "Finalizada con fecha de cierre y costo real", "Finalizar sin fecha o sin costo"),
])

pdf.heading("9. Analisis critico y recomendaciones")
for item in [
    "Defecto/riesgo: CP-35 queda manual porque depende de apagar el servidor durante una operacion.",
    "Riesgo: la persistencia de prueba usa memoria; conviene ampliar pruebas con MySQL para integracion real.",
    "Riesgo: la GUI captura excepciones generales; seria mejor diferenciar validacion, regla de negocio y conexion.",
    "Valor de TDD: documento reglas como comportamiento verificable, evito regresiones y facilito la matriz de trazabilidad.",
    "Recomendaciones: automatizar desconexion controlada, agregar validaciones visuales previas, separar repositorios por interfaces y mantener JaCoCo en cada entrega."
]:
    pdf.bullet(item)

pdf.heading("10. Preguntas probables con respuestas")
qa = [
    ("Por que RMI y no sockets?", "Porque ambos extremos estan en Java. RMI da invocacion remota tipada, serializacion de DTOs y excepciones sin protocolo manual."),
    ("Cual es el contrato?", "SigomeiRemote. Define metodos, DTOs y excepciones que puede recibir el cliente."),
    ("Que viaja por la red?", "DTOs y parametros simples: IDs, fechas, catalogos, textos y costos; no viajan servicios ni repositorios."),
    ("Donde estan las reglas?", "En la capa service: OrdenService, EquipoService y TecnicoService."),
    ("Como aplicaron TDD?", "Primero pruebas positivas/negativas, luego implementacion minima para pasar de rojo a verde, despues suite completa y JaCoCo."),
    ("Que significa rojo/verde?", "Rojo: la prueba falla porque la regla no existe. Verde: la logica se implementa y la prueba pasa."),
    ("Como saben que el CRUD remoto funciona?", "El contrato expone CRUD de Equipo, Tecnico y Orden; CrudServiceTest lo verifica y la demo JavaFX lo invoca via RMI."),
    ("Que pasa si el servidor no esta disponible?", "La llamada RMI falla con excepcion remota/conexion. CP32 cubre servidor no disponible y CP35 queda manual para desconexion durante operacion."),
    ("Por que DTOs?", "Separan datos de transporte de la logica interna y evitan exponer implementaciones del servidor."),
    ("Que regla es mas importante?", "RN-02 evita duplicidad de ordenes activas y RN-06 obliga cierre completo con fecha y costo real."),
    ("La memoria reemplaza MySQL?", "No. Sirve para pruebas unitarias rapidas y repetibles; MySQL corresponde a persistencia real o integracion."),
    ("Que cubre JaCoCo?", "La capa com.sigomei.servidor.service, donde viven reglas y validaciones principales."),
    ("Limitacion de RMI?", "Acopla a Java y es menos interoperable que HTTP/REST, pero simplifica un proyecto Java-Java."),
    ("Como repartir participacion?", "Arquitectura, contrato RMI, TDD/cobertura, demo CRUD, reglas/analisis; todos deben responder al menos una pregunta."),
]
for q, a in qa:
    pdf.text("P: " + q, bold=True, size=10.2, gap=1)
    pdf.text("R: " + a, size=10.0, indent=10, gap=5)

pdf.heading("11. Reparto sugerido")
pdf.table([
    ("Integrante", "Parte", "Tiempo"),
    ("1", "Problema, arquitectura cliente-servidor y RMI", "2 min"),
    ("2", "Contrato SigomeiRemote, DTOs y excepciones", "2 min"),
    ("3", "Ciclo TDD, pruebas unitarias y cobertura", "3 min"),
    ("4", "Demo CRUD remoto de Equipo, Tecnico y Orden", "4 min"),
    ("5", "Demo de reglas, matriz, analisis y recomendaciones", "3 min"),
])
pdf.text("Cierre: SIGOMEI separa cliente, contrato remoto y servicios; RMI simplifica la comunicacion Java-Java; TDD permitio convertir reglas de negocio en pruebas verificables.", bold=True)

pdf.build()
print(OUT)
