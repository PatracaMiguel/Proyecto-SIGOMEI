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
import com.sigomei.api.dto.UsuarioDTO;
import com.sigomei.servidor.rmi.SigomeiRemote;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SigomeiFxApp extends Application {

    private Stage stage;
    private SigomeiRemote sigomei;
    private UsuarioDTO usuarioActual;
    private String estadoServidor = "Servidor desconectado";
    private final List<EquipoDTO> equiposDemo = new ArrayList<>();
    private final List<TecnicoDTO> tecnicosDemo = new ArrayList<>();
    private final List<OrdenDTO> ordenesDemo = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        conectarServidor();
        mostrarLogin();
        stage.setTitle("SIGOMEI");
        stage.show();
    }

    private void conectarServidor() {
        try {
            String host = System.getProperty("sigomei.rmi.host", "localhost");
            int port = Integer.parseInt(System.getProperty("sigomei.rmi.port", "1099"));
            Registry registry = LocateRegistry.getRegistry(host, port);
            sigomei = (SigomeiRemote) registry.lookup("SIGOMEI");
            estadoServidor = "Servidor conectado RMI:1099";
        } catch (Exception ex) {
            sigomei = null;
            estadoServidor = "Modo demo sin servidor";
            cargarDatosDemo();
        }
    }

    private void cargarDatosDemo() {
        equiposDemo.clear();
        tecnicosDemo.clear();
        ordenesDemo.clear();

        equiposDemo.add(new EquipoDTO(1, "Compresor electrico", TipoEquipo.ELECTRICO, "Atlas", "AX-10",
                "EQ-001", "Planta Norte", LocalDate.of(2024, 1, 10), EstadoOperativo.OPERATIVO, Criticidad.ALTA));
        equiposDemo.add(new EquipoDTO(2, "Bomba mecanica", TipoEquipo.MECANICO, "Flow", "BM-22",
                "EQ-002", "Planta Sur", LocalDate.of(2024, 2, 12), EstadoOperativo.OPERATIVO, Criticidad.MEDIA));
        equiposDemo.add(new EquipoDTO(10, "Prensa hidraulica", TipoEquipo.HIDRAULICO, "Hydra", "PH-10",
                "EQ-010", "Planta Oeste", LocalDate.of(2024, 3, 1), EstadoOperativo.OPERATIVO, Criticidad.BAJA));

        tecnicosDemo.add(new TecnicoDTO(1, "Ana Lopez", "LOAA900101AA1", "5551000001",
                "ana@example.com", TipoEquipo.ELECTRICO, NivelCertificacion.II, LocalDate.of(2022, 1, 10), EstadoTecnico.ACTIVO));
        tecnicosDemo.add(new TecnicoDTO(2, "Bruno Ruiz", "RUBB900101BB1", "5551000002",
                "bruno@example.com", TipoEquipo.MECANICO, NivelCertificacion.II, LocalDate.of(2022, 2, 10), EstadoTecnico.ACTIVO));

        ordenesDemo.add(new OrdenDTO(1, 1, 1, TipoMantenimiento.PREVENTIVO,
                LocalDate.of(2026, 5, 20), null, null, "Orden programada base",
                new BigDecimal("1500.00"), null, EstadoOrden.PROGRAMADA));
        ordenesDemo.add(new OrdenDTO(2, 2, 2, TipoMantenimiento.CORRECTIVO,
                LocalDate.of(2026, 5, 19), LocalDate.of(2026, 5, 19), null,
                "Orden en ejecucion base", new BigDecimal("1800.00"), null, EstadoOrden.EN_EJECUCION));
    }

    private void mostrarLogin() {
        VBox tarjeta = new VBox(14);
        tarjeta.getStyleClass().add("login-card");
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setMaxWidth(620);
        tarjeta.setMaxHeight(700);

        Label titulo = new Label("SIGOMEI");
        titulo.getStyleClass().add("login-title");
        Label subtitulo = new Label("Gestion de ordenes de mantenimiento de equipos\nindustriales");
        subtitulo.getStyleClass().add("login-subtitle");

        TextField usuario = new TextField("admin");
        usuario.getStyleClass().add("login-input");
        PasswordField contrasena = new PasswordField();
        contrasena.setText("admin123");
        contrasena.getStyleClass().add("login-input");

        Label error = new Label();
        error.getStyleClass().add("error-label");

        HBox roles = new HBox(28, botonRol("coordinador", true), botonRol("supervisor", false));
        roles.setAlignment(Pos.CENTER);

        Button entrar = new Button("Iniciar sesion");
        entrar.getStyleClass().add("outline-button");
        entrar.setMaxWidth(Double.MAX_VALUE);
        entrar.setOnAction(event -> {
            try {
                if (sigomei != null) {
                    usuarioActual = sigomei.iniciarSesion(usuario.getText(), contrasena.getText());
                } else {
                    usuarioActual = new UsuarioDTO();
                    usuarioActual.setNombreUsuario(usuario.getText());
                }
                mostrarMenu();
            } catch (Exception ex) {
                error.setText("No se pudo iniciar sesion: " + ex.getMessage());
            }
        });

        Region separadorLogin = new Region();
        separadorLogin.setMinHeight(18);

        tarjeta.getChildren().addAll(
                titulo,
                subtitulo,
                etiqueta("Usuario"),
                usuario,
                etiqueta("Contrasena"),
                contrasena,
                etiqueta("Rol"),
                roles,
                separadorLogin,
                entrar,
                error
        );

        StackPane root = new StackPane(tarjeta);
        root.getStyleClass().add("root-blue");
        root.setPadding(new Insets(28));
        ponerEscena(root, 720, 760);
    }

    private Button botonRol(String texto, boolean activo) {
        Button boton = new Button(texto);
        boton.getStyleClass().add(activo ? "role-button-active" : "role-button");
        return boton;
    }

    private Label etiqueta(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("form-label");
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    private void mostrarMenu() {
        BorderPane root = baseConEncabezado("Menu principal", "Selecciona un modulo");
        VBox contenido = new VBox(46);
        contenido.setPadding(new Insets(52, 28, 28, 28));

        HBox tarjetas = new HBox(28);
        tarjetas.getChildren().addAll(
                tarjetaModulo("EQ", "Equipos industriales", "Registrar, consultar y modificar equipos", contarEquipos(), () -> mostrarEquipos()),
                tarjetaModulo("TE", "Tecnicos", "Gestionar tecnicos y especialidades", contarTecnicos(), () -> mostrarTecnicos()),
                tarjetaModulo("OR", "Ordenes de mantenimiento", "Crear y dar seguimiento a las ordenes", contarOrdenes(), () -> mostrarOrdenes())
        );

        Region linea = new Region();
        linea.getStyleClass().add("separator");

        Button salir = new Button("cerrar sesion");
        salir.getStyleClass().add("outline-small");
        salir.setOnAction(event -> mostrarLogin());
        HBox acciones = new HBox(salir);
        acciones.setAlignment(Pos.CENTER_RIGHT);

        contenido.getChildren().addAll(tarjetas, linea, acciones);
        root.setCenter(contenido);
        ponerEscena(root, 980, 620);
    }

    private BorderPane baseConEncabezado(String titulo, String subtitulo) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-shell");
        root.setPadding(new Insets(24));

        VBox textos = new VBox(10, labelClase(titulo, "page-title"), labelClase(subtitulo, "page-subtitle"));
        VBox estado = new VBox(10,
                labelClase("coordinador: " + nombreUsuario(), "pill"),
                labelClase(estadoServidor, "pill")
        );
        estado.setAlignment(Pos.CENTER_RIGHT);

        HBox header = new HBox(textos, spacer(), estado);
        header.setAlignment(Pos.TOP_CENTER);
        root.setTop(header);
        return root;
    }

    private VBox menuLateral(String activo) {
        VBox menu = new VBox(14);
        menu.getStyleClass().add("side-menu");
        menu.setPrefWidth(145);

        Label marca = labelClase("SIGOMEI", "brand");
        Button equipos = botonMenu("Equipos", activo.equals("equipos"), () -> mostrarEquipos());
        Button tecnicos = botonMenu("Tecnicos", activo.equals("tecnicos"), () -> mostrarTecnicos());
        Button ordenes = botonMenu("Ordenes", activo.equals("ordenes"), () -> mostrarOrdenes());
        Region relleno = new Region();
        VBox.setVgrow(relleno, Priority.ALWAYS);
        Button salir = new Button("Salir");
        salir.getStyleClass().add("outline-small");
        salir.setOnAction(event -> mostrarMenu());

        menu.getChildren().addAll(marca, equipos, tecnicos, ordenes, relleno, salir);
        return menu;
    }

    private Button botonMenu(String texto, boolean activo, Runnable accion) {
        Button boton = new Button(texto);
        boton.getStyleClass().add(activo ? "nav-active" : "nav-button");
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setOnAction(event -> accion.run());
        return boton;
    }

    private VBox tarjetaModulo(String icono, String titulo, String descripcion, int total, Runnable accion) {
        VBox card = new VBox(14);
        card.getStyleClass().add("module-card");
        card.setOnMouseClicked(event -> accion.run());

        Label icon = labelClase(icono, "module-icon");
        Label title = labelClase(titulo, "module-title");
        Label description = labelClase(descripcion, "module-description");
        Label count = labelClase(String.valueOf(total), "module-count");
        card.getChildren().addAll(icon, title, description, count);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private void mostrarEquipos() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-shell");
        root.setLeft(menuLateral("equipos"));

        TableView<EquipoDTO> tabla = tablaEquipos();
        VBox contenido = new VBox(18);
        contenido.setPadding(new Insets(24));
        contenido.getChildren().addAll(
                encabezadoModulo("EQ", "Equipos Industriales", "+ Agregar equipo", () -> mostrarFormularioEquipo(null)),
                resumenEquipos(),
                filtrosEquipos(tabla),
                tabla
        );

        root.setCenter(contenido);
        ponerEscena(root, 980, 660);
    }

    private void mostrarTecnicos() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-shell");
        root.setLeft(menuLateral("tecnicos"));

        TableView<TecnicoDTO> tabla = tablaTecnicos();
        VBox contenido = new VBox(18);
        contenido.setPadding(new Insets(24));
        contenido.getChildren().addAll(
                encabezadoModulo("TE", "Tecnicos", "+ Agregar tecnico", () -> mostrarFormularioTecnico(null)),
                filtrosTecnicos(tabla),
                tabla
        );

        root.setCenter(contenido);
        ponerEscena(root, 980, 660);
    }

    private void mostrarOrdenes() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-shell");
        root.setLeft(menuLateral("ordenes"));

        TableView<OrdenDTO> tabla = tablaOrdenes();
        VBox contenido = new VBox(18);
        contenido.setPadding(new Insets(24));
        contenido.getChildren().addAll(
                encabezadoModulo("OR", "Ordenes de mantenimiento", "+ Agregar orden", () -> mostrarFormularioOrden(null)),
                resumenOrdenes(),
                filtrosOrdenes(tabla),
                tabla
        );

        root.setCenter(contenido);
        ponerEscena(root, 980, 660);
    }

    private HBox encabezadoModulo(String icono, String titulo, String textoBoton, Runnable accion) {
        HBox header = new HBox(18, labelClase(icono, "module-icon"), labelClase(titulo, "page-title"), spacer());
        if (textoBoton != null && !textoBoton.isBlank()) {
            Button agregar = new Button(textoBoton);
            agregar.getStyleClass().add("outline-small");
            agregar.setOnAction(event -> accion.run());
            header.getChildren().add(agregar);
        }
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private HBox resumenEquipos() {
        return new HBox(28,
                stat("Total", String.valueOf(contarEquipos())),
                stat("Operativos", String.valueOf(contarOperativos())),
                stat("Criticidad alta", String.valueOf(contarCriticos())),
                stat("Orden activa", String.valueOf(contarOrdenesActivas()))
        );
    }

    private VBox stat(String titulo, String valor) {
        VBox box = new VBox(4, labelClase(titulo, "stat-title"), labelClase(valor, "stat-value"));
        box.getStyleClass().add("stat-card");
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private HBox resumenOrdenes() {
        return new HBox(28,
                stat("Total", String.valueOf(contarOrdenes())),
                stat("Programadas", String.valueOf(contarOrdenesPorEstado(EstadoOrden.PROGRAMADA))),
                stat("En ejecucion", String.valueOf(contarOrdenesPorEstado(EstadoOrden.EN_EJECUCION))),
                stat("Finalizadas", String.valueOf(contarOrdenesPorEstado(EstadoOrden.FINALIZADA)))
        );
    }

    private HBox filtrosEquipos(TableView<EquipoDTO> tabla) {
        TextField buscar = new TextField();
        buscar.setPromptText("Buscar por nombre ......");
        buscar.getStyleClass().add("compact-input");
        ComboBox<String> tipo = new ComboBox<>(FXCollections.observableArrayList(
                "Tipo: Todos", "Electrico", "Mecanico", "Instrumentacion", "Hidraulico"));
        tipo.getSelectionModel().selectFirst();
        tipo.getStyleClass().add("combo");
        ComboBox<String> criticidad = new ComboBox<>(FXCollections.observableArrayList("Criticidad: Todas", "Alta", "Media", "Baja"));
        criticidad.getSelectionModel().selectFirst();
        criticidad.getStyleClass().add("combo");

        buscar.textProperty().addListener((observable, anterior, nuevo) -> aplicarFiltrosEquipos(tabla, buscar, tipo, criticidad));
        tipo.setOnAction(event -> aplicarFiltrosEquipos(tabla, buscar, tipo, criticidad));
        criticidad.setOnAction(event -> aplicarFiltrosEquipos(tabla, buscar, tipo, criticidad));

        return new HBox(28, buscar, tipo, criticidad);
    }

    private HBox filtrosTecnicos(TableView<TecnicoDTO> tabla) {
        TextField buscar = new TextField();
        buscar.setPromptText("Buscar por nombre ......");
        buscar.getStyleClass().add("compact-input");
        ComboBox<String> especialidad = new ComboBox<>(FXCollections.observableArrayList(
                "Tipo: Todos", "Electrico", "Mecanico", "Instrumentacion", "Hidraulico"));
        especialidad.getSelectionModel().selectFirst();
        especialidad.getStyleClass().add("combo");
        ComboBox<String> certificacion = new ComboBox<>(FXCollections.observableArrayList(
                "Certificacion: Todas", "I", "II", "III"));
        certificacion.getSelectionModel().selectFirst();
        certificacion.getStyleClass().add("combo");
        ComboBox<String> estado = new ComboBox<>(FXCollections.observableArrayList(
                "Estado: Todos", "Activo", "Inactivo"));
        estado.getSelectionModel().selectFirst();
        estado.getStyleClass().add("combo");

        buscar.textProperty().addListener((observable, anterior, nuevo) ->
                aplicarFiltrosTecnicos(tabla, buscar, especialidad, certificacion, estado));
        especialidad.setOnAction(event -> aplicarFiltrosTecnicos(tabla, buscar, especialidad, certificacion, estado));
        certificacion.setOnAction(event -> aplicarFiltrosTecnicos(tabla, buscar, especialidad, certificacion, estado));
        estado.setOnAction(event -> aplicarFiltrosTecnicos(tabla, buscar, especialidad, certificacion, estado));

        return new HBox(22, buscar, especialidad, certificacion, estado);
    }

    private HBox filtrosOrdenes(TableView<OrdenDTO> tabla) {
        TextField buscar = new TextField();
        buscar.setPromptText("Buscar orden ......");
        buscar.getStyleClass().add("compact-input");
        ComboBox<String> estado = new ComboBox<>(FXCollections.observableArrayList(
                "Estado: Todos", "Programada", "En ejecucion", "Finalizada", "Cancelada"));
        estado.getSelectionModel().selectFirst();
        estado.getStyleClass().add("combo");
        ComboBox<String> tipo = new ComboBox<>(FXCollections.observableArrayList(
                "Tipo: Todos", "Preventivo", "Correctivo"));
        tipo.getSelectionModel().selectFirst();
        tipo.getStyleClass().add("combo");

        buscar.textProperty().addListener((observable, anterior, nuevo) -> aplicarFiltrosOrdenes(tabla, buscar, estado, tipo));
        estado.setOnAction(event -> aplicarFiltrosOrdenes(tabla, buscar, estado, tipo));
        tipo.setOnAction(event -> aplicarFiltrosOrdenes(tabla, buscar, estado, tipo));

        return new HBox(28, buscar, estado, tipo);
    }

    private void aplicarFiltrosEquipos(TableView<EquipoDTO> tabla, TextField buscar,
                                       ComboBox<String> tipo, ComboBox<String> criticidad) {
        List<EquipoDTO> filtrados = new ArrayList<>();
        String texto = buscar.getText() == null ? "" : buscar.getText().toLowerCase();
        String tipoSeleccionado = tipo.getValue();
        String criticidadSeleccionada = criticidad.getValue();

        for (EquipoDTO equipo : obtenerEquipos()) {
            boolean coincideTexto = texto.isBlank()
                    || equipo.getNombre().toLowerCase().contains(texto)
                    || equipo.getNumeroSerie().toLowerCase().contains(texto);
            boolean coincideTipo = esTodos(tipoSeleccionado) || equipo.getTipo() == tipoEquipo(tipoSeleccionado);
            boolean coincideCriticidad = esTodos(criticidadSeleccionada)
                    || equipo.getCriticidad() == criticidad(criticidadSeleccionada);

            if (coincideTexto && coincideTipo && coincideCriticidad) {
                filtrados.add(equipo);
            }
        }

        tabla.setItems(FXCollections.observableArrayList(filtrados));
    }

    private void aplicarFiltrosTecnicos(TableView<TecnicoDTO> tabla, TextField buscar,
                                        ComboBox<String> especialidad, ComboBox<String> certificacion,
                                        ComboBox<String> estado) {
        List<TecnicoDTO> filtrados = new ArrayList<>();
        String texto = buscar.getText() == null ? "" : buscar.getText().toLowerCase();
        String especialidadSeleccionada = especialidad.getValue();
        String certificacionSeleccionada = certificacion.getValue();
        String estadoSeleccionado = estado.getValue();

        for (TecnicoDTO tecnico : obtenerTecnicos()) {
            boolean coincideTexto = texto.isBlank()
                    || tecnico.getNombreCompleto().toLowerCase().contains(texto)
                    || tecnico.getRfc().toLowerCase().contains(texto)
                    || tecnico.getCorreo().toLowerCase().contains(texto);
            boolean coincideEspecialidad = esTodos(especialidadSeleccionada)
                    || tecnico.getEspecialidad() == tipoEquipo(especialidadSeleccionada);
            boolean coincideCertificacion = esTodos(certificacionSeleccionada)
                    || tecnico.getNivelCertificacion() == NivelCertificacion.valueOf(certificacionSeleccionada);
            boolean coincideEstado = esTodos(estadoSeleccionado)
                    || tecnico.getEstatus() == estadoTecnico(estadoSeleccionado);

            if (coincideTexto && coincideEspecialidad && coincideCertificacion && coincideEstado) {
                filtrados.add(tecnico);
            }
        }

        tabla.setItems(FXCollections.observableArrayList(filtrados));
    }

    private void aplicarFiltrosOrdenes(TableView<OrdenDTO> tabla, TextField buscar,
                                       ComboBox<String> estado, ComboBox<String> tipo) {
        List<OrdenDTO> filtrados = new ArrayList<>();
        String texto = buscar.getText() == null ? "" : buscar.getText().toLowerCase();
        String estadoSeleccionado = estado.getValue();
        String tipoSeleccionado = tipo.getValue();

        for (OrdenDTO orden : obtenerOrdenes()) {
            boolean coincideTexto = texto.isBlank()
                    || String.valueOf(orden.getIdOrden()).contains(texto)
                    || orden.getDescripcionTrabajo().toLowerCase().contains(texto);
            boolean coincideEstado = esTodos(estadoSeleccionado)
                    || orden.getEstadoOrden() == estadoOrden(estadoSeleccionado);
            boolean coincideTipo = esTodos(tipoSeleccionado)
                    || orden.getTipoMantenimiento() == tipoMantenimiento(tipoSeleccionado);

            if (coincideTexto && coincideEstado && coincideTipo) {
                filtrados.add(orden);
            }
        }

        tabla.setItems(FXCollections.observableArrayList(filtrados));
    }

    private TableView<EquipoDTO> tablaEquipos() {
        TableView<EquipoDTO> tabla = new TableView<>();
        tabla.getStyleClass().add("sigomei-table");
        tabla.setItems(FXCollections.observableArrayList(obtenerEquipos()));
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<EquipoDTO, String> nombre = columna("Nombre", "nombre");
        TableColumn<EquipoDTO, String> id = columna("ID", "idEquipo");
        TableColumn<EquipoDTO, String> tipo = columna("Tipo", "tipo");
        TableColumn<EquipoDTO, String> serie = columna("No. serie", "numeroSerie");
        TableColumn<EquipoDTO, String> ubicacion = columna("Ubicacion", "ubicacionPlanta");
        TableColumn<EquipoDTO, String> criticidad = columna("Criticidad", "criticidad");
        TableColumn<EquipoDTO, String> estado = columna("Estado", "estadoOperativo");
        id.setPrefWidth(60);
        nombre.setPrefWidth(190);
        tipo.setPrefWidth(130);
        serie.setPrefWidth(130);
        ubicacion.setPrefWidth(160);
        criticidad.setPrefWidth(120);
        estado.setPrefWidth(145);
        TableColumn<EquipoDTO, Void> acciones = accionesEquipo();
        acciones.setPrefWidth(160);
        tabla.getColumns().addAll(id, nombre, tipo, serie, ubicacion, criticidad, estado, acciones);
        return tabla;
    }

    private TableView<TecnicoDTO> tablaTecnicos() {
        TableView<TecnicoDTO> tabla = new TableView<>();
        tabla.getStyleClass().add("sigomei-table");
        tabla.setItems(FXCollections.observableArrayList(obtenerTecnicos()));
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<TecnicoDTO, String> id = columna("ID", "idTecnico");
        TableColumn<TecnicoDTO, String> nombre = columna("Nombre", "nombreCompleto");
        TableColumn<TecnicoDTO, String> rfc = columna("RFC", "rfc");
        TableColumn<TecnicoDTO, String> correo = columna("Correo", "correo");
        TableColumn<TecnicoDTO, String> especialidad = columna("Especialidad", "especialidad");
        TableColumn<TecnicoDTO, String> certificacion = columna("Certificacion", "nivelCertificacion");
        TableColumn<TecnicoDTO, String> estatus = columna("Estatus", "estatus");
        id.setPrefWidth(60);
        nombre.setPrefWidth(180);
        rfc.setPrefWidth(140);
        correo.setPrefWidth(190);
        especialidad.setPrefWidth(130);
        certificacion.setPrefWidth(130);
        estatus.setPrefWidth(100);
        TableColumn<TecnicoDTO, Void> acciones = accionesTecnico();
        acciones.setPrefWidth(160);
        tabla.getColumns().addAll(id, nombre, rfc, correo, especialidad, certificacion, estatus, acciones);
        return tabla;
    }

    private TableView<OrdenDTO> tablaOrdenes() {
        TableView<OrdenDTO> tabla = new TableView<>();
        tabla.getStyleClass().add("sigomei-table");
        tabla.setItems(FXCollections.observableArrayList(obtenerOrdenes()));
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<OrdenDTO, String> id = columna("ID", "idOrden");
        TableColumn<OrdenDTO, String> equipo = columna("Equipo", "idEquipo");
        TableColumn<OrdenDTO, String> tecnico = columna("Tecnico", "idTecnico");
        TableColumn<OrdenDTO, String> tipo = columna("Tipo", "tipoMantenimiento");
        TableColumn<OrdenDTO, String> fecha = columna("Fecha programada", "fechaProgramada");
        TableColumn<OrdenDTO, String> costo = columna("Costo estimado", "costoEstimado");
        TableColumn<OrdenDTO, String> estado = columna("Estado", "estadoOrden");
        id.setPrefWidth(60);
        equipo.setPrefWidth(90);
        tecnico.setPrefWidth(90);
        tipo.setPrefWidth(140);
        fecha.setPrefWidth(170);
        costo.setPrefWidth(150);
        estado.setPrefWidth(150);
        TableColumn<OrdenDTO, Void> acciones = accionesOrden();
        acciones.setPrefWidth(160);
        tabla.getColumns().addAll(id, equipo, tecnico, tipo, fecha, costo, estado, acciones);
        return tabla;
    }

    private TableColumn<EquipoDTO, Void> accionesEquipo() {
        TableColumn<EquipoDTO, Void> columna = new TableColumn<>("Acciones");
        columna.setCellFactory(param -> new TableCell<>() {
            private final Button editar = botonAccion("Editar");
            private final Button eliminar = botonAccion("Eliminar");
            private final HBox botones = new HBox(8, editar, eliminar);

            {
                botones.setAlignment(Pos.CENTER_LEFT);
                editar.setOnAction(event -> {
                    EquipoDTO equipo = getTableView().getItems().get(getIndex());
                    mostrarFormularioEquipo(equipo);
                });
                eliminar.setOnAction(event -> {
                    EquipoDTO equipo = getTableView().getItems().get(getIndex());
                    if (eliminarEquipo(equipo)) {
                        getTableView().getItems().remove(equipo);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : botones);
            }
        });
        return columna;
    }

    private TableColumn<TecnicoDTO, Void> accionesTecnico() {
        TableColumn<TecnicoDTO, Void> columna = new TableColumn<>("Acciones");
        columna.setCellFactory(param -> new TableCell<>() {
            private final Button editar = botonAccion("Editar");
            private final Button eliminar = botonAccion("Eliminar");
            private final HBox botones = new HBox(8, editar, eliminar);

            {
                botones.setAlignment(Pos.CENTER_LEFT);
                editar.setOnAction(event -> {
                    TecnicoDTO tecnico = getTableView().getItems().get(getIndex());
                    mostrarFormularioTecnico(tecnico);
                });
                eliminar.setOnAction(event -> {
                    TecnicoDTO tecnico = getTableView().getItems().get(getIndex());
                    if (eliminarTecnico(tecnico)) {
                        getTableView().getItems().remove(tecnico);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : botones);
            }
        });
        return columna;
    }

    private TableColumn<OrdenDTO, Void> accionesOrden() {
        TableColumn<OrdenDTO, Void> columna = new TableColumn<>("Acciones");
        columna.setCellFactory(param -> new TableCell<>() {
            private final Button editar = botonAccion("Editar");
            private final Button eliminar = botonAccion("Eliminar");
            private final HBox botones = new HBox(8, editar, eliminar);

            {
                botones.setAlignment(Pos.CENTER_LEFT);
                editar.setOnAction(event -> {
                    OrdenDTO orden = getTableView().getItems().get(getIndex());
                    mostrarFormularioOrden(orden);
                });
                eliminar.setOnAction(event -> {
                    OrdenDTO orden = getTableView().getItems().get(getIndex());
                    if (eliminarOrden(orden)) {
                        getTableView().getItems().remove(orden);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : botones);
            }
        });
        return columna;
    }

    private Button botonAccion(String texto) {
        Button boton = new Button(texto);
        boton.getStyleClass().add("table-action-button");
        return boton;
    }

    private boolean eliminarEquipo(EquipoDTO equipo) {
        try {
            if (sigomei != null) {
                sigomei.eliminarEquipo(equipo.getIdEquipo());
            } else {
                equiposDemo.removeIf(actual -> actual.getIdEquipo() == equipo.getIdEquipo());
            }
            return true;
        } catch (Exception ex) {
            mostrarError("No se pudo eliminar el equipo", ex.getMessage());
            return false;
        }
    }

    private boolean eliminarTecnico(TecnicoDTO tecnico) {
        try {
            if (sigomei != null) {
                sigomei.eliminarTecnico(tecnico.getIdTecnico());
            } else {
                tecnicosDemo.removeIf(actual -> actual.getIdTecnico() == tecnico.getIdTecnico());
            }
            return true;
        } catch (Exception ex) {
            mostrarError("No se pudo eliminar el tecnico", ex.getMessage());
            return false;
        }
    }

    private boolean eliminarOrden(OrdenDTO orden) {
        try {
            if (sigomei != null) {
                sigomei.eliminarOrden(orden.getIdOrden());
            } else {
                ordenesDemo.removeIf(actual -> actual.getIdOrden() == orden.getIdOrden());
            }
            return true;
        } catch (Exception ex) {
            mostrarError("No se pudo eliminar la orden", ex.getMessage());
            return false;
        }
    }

    private <T> TableColumn<T, String> columna(String titulo, String propiedad) {
        TableColumn<T, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(new PropertyValueFactory<>(propiedad));
        return columna;
    }

    private void mostrarFormularioEquipo(EquipoDTO equipoEditar) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-shell");
        root.setLeft(menuLateral("equipos"));

        boolean editando = equipoEditar != null;
        VBox contenido = new VBox(18);
        contenido.setPadding(new Insets(24));
        contenido.getChildren().add(encabezadoModulo("EQ",
                editando ? "Editar equipo industrial" : "Registrar equipo industrial", "", () -> mensajeDemo()));

        GridPane form = new GridPane();
        form.getStyleClass().add("form-card");
        form.setHgap(42);
        form.setVgap(10);
        form.setPadding(new Insets(24));

        TextField nombre = campo(editando ? equipoEditar.getNombre() : "");
        TextField marca = campo(editando ? equipoEditar.getMarca() : "");
        TextField serie = campo(editando ? equipoEditar.getNumeroSerie() : "");
        TextField fecha = campo(editando ? String.valueOf(equipoEditar.getFechaInstalacion()) : "");
        ComboBox<String> tipo = combo(editando ? textoTipo(equipoEditar.getTipo()) : "Mecanico", "Electrico", "Mecanico", "Instrumentacion", "Hidraulico");
        TextField modelo = campo(editando ? equipoEditar.getModelo() : "");
        TextField ubicacion = campo(editando ? equipoEditar.getUbicacionPlanta() : "");
        ComboBox<String> estado = combo(editando ? textoEstadoEquipo(equipoEditar.getEstadoOperativo()) : "Operativo",
                "Operativo", "En mantenimiento", "Fuera servicio", "Inactivo");
        ComboBox<String> criticidad = combo(editando ? textoCriticidad(equipoEditar.getCriticidad()) : "Alta", "Alta", "Media", "Baja");

        agregarCampo(form, 0, 0, "Nombre", nombre);
        agregarCampo(form, 0, 1, "Marca", marca);
        agregarCampo(form, 0, 2, "Numero de serie", serie);
        agregarCampo(form, 0, 3, "Fecha de instalacion", fecha);
        agregarCampo(form, 0, 4, "Criticidad", criticidad);
        agregarCampo(form, 1, 0, "Tipo", tipo);
        agregarCampo(form, 1, 1, "Modelo", modelo);
        agregarCampo(form, 1, 2, "Ubicacion en planta", ubicacion);
        agregarCampo(form, 1, 3, "Estado operativo", estado);

        Button cancelar = new Button("Cancelar");
        cancelar.getStyleClass().add("solid-button");
        cancelar.setOnAction(event -> mostrarEquipos());
        Button guardar = new Button("Guardar equipo");
        guardar.getStyleClass().add("solid-button");
        guardar.setOnAction(event -> guardarEquipo(equipoEditar, nombre.getText(), marca.getText(), modelo.getText(),
                serie.getText(), ubicacion.getText(), fecha.getText(), tipo.getValue(), estado.getValue(), criticidad.getValue()));
        HBox acciones = new HBox(52, cancelar, guardar);
        acciones.setAlignment(Pos.CENTER);
        form.add(acciones, 0, 6, 2, 1);

        contenido.getChildren().add(form);
        root.setCenter(contenido);
        ponerEscena(root, 980, 760);
    }

    private void mostrarFormularioTecnico(TecnicoDTO tecnicoEditar) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-shell");
        root.setLeft(menuLateral("tecnicos"));

        boolean editando = tecnicoEditar != null;
        VBox contenido = new VBox(18);
        contenido.setPadding(new Insets(24));
        contenido.getChildren().add(encabezadoModulo("TE",
                editando ? "Editar tecnico" : "Registrar tecnico", "", () -> mensajeDemo()));

        GridPane form = new GridPane();
        form.getStyleClass().add("form-card");
        form.setHgap(42);
        form.setVgap(10);
        form.setPadding(new Insets(24));

        TextField nombre = campo(editando ? tecnicoEditar.getNombreCompleto() : "");
        TextField rfc = campo(editando ? tecnicoEditar.getRfc() : "");
        TextField telefono = campo(editando ? tecnicoEditar.getTelefono() : "");
        TextField correo = campo(editando ? tecnicoEditar.getCorreo() : "");
        TextField fecha = campo(editando ? String.valueOf(tecnicoEditar.getFechaIngreso()) : "");
        ComboBox<String> especialidad = combo(editando ? textoTipo(tecnicoEditar.getEspecialidad()) : "Mecanico",
                "Electrico", "Mecanico", "Instrumentacion", "Hidraulico");
        ComboBox<String> certificacion = combo(editando ? tecnicoEditar.getNivelCertificacion().name() : "II", "I", "II", "III");
        ComboBox<String> estatus = combo(editando ? textoEstadoTecnico(tecnicoEditar.getEstatus()) : "Activo", "Activo", "Inactivo");

        agregarCampo(form, 0, 0, "Nombre", nombre);
        agregarCampo(form, 0, 1, "RFC", rfc);
        agregarCampo(form, 0, 2, "Telefono", telefono);
        agregarCampo(form, 0, 3, "Correo", correo);
        agregarCampo(form, 1, 0, "Especialidad", especialidad);
        agregarCampo(form, 1, 1, "Certificacion", certificacion);
        agregarCampo(form, 1, 2, "Fecha de ingreso", fecha);
        agregarCampo(form, 1, 3, "Estatus", estatus);

        Button cancelar = new Button("Cancelar");
        cancelar.getStyleClass().add("solid-button");
        cancelar.setOnAction(event -> mostrarTecnicos());
        Button guardar = new Button("Guardar tecnico");
        guardar.getStyleClass().add("solid-button");
        guardar.setOnAction(event -> guardarTecnico(tecnicoEditar, nombre.getText(), rfc.getText(), telefono.getText(),
                correo.getText(), especialidad.getValue(), certificacion.getValue(), fecha.getText(), estatus.getValue()));
        HBox acciones = new HBox(52, cancelar, guardar);
        acciones.setAlignment(Pos.CENTER);
        form.add(acciones, 0, 5, 2, 1);

        contenido.getChildren().add(form);
        root.setCenter(contenido);
        ponerEscena(root, 980, 720);
    }

    private void mostrarFormularioOrden(OrdenDTO ordenEditar) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-shell");
        root.setLeft(menuLateral("ordenes"));

        boolean editando = ordenEditar != null;
        VBox contenido = new VBox(18);
        contenido.setPadding(new Insets(24));
        contenido.getChildren().add(encabezadoModulo("OR",
                editando ? "Editar orden de mantenimiento" : "Registrar orden de mantenimiento", "", () -> mensajeDemo()));

        GridPane form = new GridPane();
        form.getStyleClass().add("form-card");
        form.setHgap(42);
        form.setVgap(10);
        form.setPadding(new Insets(24));

        TextField equipo = campo(editando ? String.valueOf(ordenEditar.getIdEquipo()) : "");
        TextField tecnico = campo(editando ? String.valueOf(ordenEditar.getIdTecnico()) : "");
        TextField fechaProgramada = campo(editando ? String.valueOf(ordenEditar.getFechaProgramada()) : "");
        TextField fechaInicio = campo(editando && ordenEditar.getFechaInicio() != null ? String.valueOf(ordenEditar.getFechaInicio()) : "");
        TextField fechaCierre = campo(editando && ordenEditar.getFechaCierre() != null ? String.valueOf(ordenEditar.getFechaCierre()) : "");
        TextField descripcion = campo(editando ? ordenEditar.getDescripcionTrabajo() : "");
        TextField costoEstimado = campo(editando ? String.valueOf(ordenEditar.getCostoEstimado()) : "");
        TextField costoReal = campo(editando && ordenEditar.getCostoReal() != null ? String.valueOf(ordenEditar.getCostoReal()) : "");
        ComboBox<String> tipo = combo(editando ? textoTipoMantenimiento(ordenEditar.getTipoMantenimiento()) : "Preventivo", "Preventivo", "Correctivo");
        ComboBox<String> estado = combo(editando ? textoEstadoOrden(ordenEditar.getEstadoOrden()) : "Programada",
                "Programada", "En ejecucion", "Finalizada", "Cancelada");

        agregarCampo(form, 0, 0, "ID equipo", equipo);
        agregarCampo(form, 0, 1, "ID tecnico", tecnico);
        agregarCampo(form, 0, 2, "Tipo", tipo);
        agregarCampo(form, 0, 3, "Fecha programada", fechaProgramada);
        agregarCampo(form, 0, 4, "Fecha inicio", fechaInicio);
        agregarCampo(form, 1, 0, "Fecha cierre", fechaCierre);
        agregarCampo(form, 1, 1, "Descripcion", descripcion);
        agregarCampo(form, 1, 2, "Costo estimado", costoEstimado);
        agregarCampo(form, 1, 3, "Costo real", costoReal);
        agregarCampo(form, 1, 4, "Estado", estado);

        Button cancelar = new Button("Cancelar");
        cancelar.getStyleClass().add("solid-button");
        cancelar.setOnAction(event -> mostrarOrdenes());
        Button guardar = new Button("Guardar orden");
        guardar.getStyleClass().add("solid-button");
        guardar.setOnAction(event -> guardarOrden(ordenEditar, equipo.getText(), tecnico.getText(), tipo.getValue(),
                fechaProgramada.getText(), fechaInicio.getText(), fechaCierre.getText(), descripcion.getText(),
                costoEstimado.getText(), costoReal.getText(), estado.getValue()));
        HBox acciones = new HBox(52, cancelar, guardar);
        acciones.setAlignment(Pos.CENTER);
        form.add(acciones, 0, 6, 2, 1);

        contenido.getChildren().add(form);
        root.setCenter(contenido);
        ponerEscena(root, 980, 760);
    }

    private void agregarCampo(GridPane form, int columna, int fila, String etiqueta, javafx.scene.Node campo) {
        VBox grupo = new VBox(6, labelClase(etiqueta, "field-label"), campo);
        form.add(grupo, columna, fila);
    }

    private TextField campo(String texto) {
        TextField campo = new TextField(texto);
        campo.getStyleClass().add("form-input");
        return campo;
    }

    private ComboBox<String> combo(String seleccionado, String... opciones) {
        ComboBox<String> combo = new ComboBox<>(FXCollections.observableArrayList(opciones));
        combo.getSelectionModel().select(seleccionado);
        combo.getStyleClass().add("form-input");
        return combo;
    }

    private void guardarEquipo(EquipoDTO equipoEditar, String nombre, String marca, String modelo, String serie,
                               String ubicacion, String fecha, String tipo, String estado, String criticidad) {
        try {
            int id = equipoEditar == null ? siguienteIdEquipo() : equipoEditar.getIdEquipo();
            EquipoDTO equipo = new EquipoDTO(id, nombre, tipoEquipo(tipo), marca, modelo, serie,
                    ubicacion, LocalDate.parse(fecha), estadoEquipo(estado), criticidad(criticidad));
            if (sigomei != null) {
                if (equipoEditar == null) {
                    sigomei.registrarEquipo(equipo);
                } else {
                    sigomei.actualizarEquipo(equipo);
                }
            } else if (equipoEditar == null) {
                equiposDemo.add(equipo);
            } else {
                reemplazarEquipoDemo(equipo);
            }
            mostrarEquipos();
        } catch (Exception ex) {
            mostrarError("No se pudo guardar el equipo", ex.getMessage());
        }
    }

    private void guardarTecnico(TecnicoDTO tecnicoEditar, String nombre, String rfc, String telefono, String correo,
                                String especialidad, String certificacion, String fecha, String estatus) {
        try {
            int id = tecnicoEditar == null ? siguienteIdTecnico() : tecnicoEditar.getIdTecnico();
            TecnicoDTO tecnico = new TecnicoDTO(id, nombre, rfc, telefono, correo, tipoEquipo(especialidad),
                    NivelCertificacion.valueOf(certificacion), LocalDate.parse(fecha), estadoTecnico(estatus));
            if (sigomei != null) {
                if (tecnicoEditar == null) {
                    sigomei.registrarTecnico(tecnico);
                } else {
                    sigomei.actualizarTecnico(tecnico);
                }
            } else if (tecnicoEditar == null) {
                tecnicosDemo.add(tecnico);
            } else {
                reemplazarTecnicoDemo(tecnico);
            }
            mostrarTecnicos();
        } catch (Exception ex) {
            mostrarError("No se pudo guardar el tecnico", ex.getMessage());
        }
    }

    private void guardarOrden(OrdenDTO ordenEditar, String idEquipo, String idTecnico, String tipo,
                              String fechaProgramada, String fechaInicio, String fechaCierre, String descripcion,
                              String costoEstimado, String costoReal, String estado) {
        try {
            int idOrden = ordenEditar == null ? siguienteIdOrden() : ordenEditar.getIdOrden();
            OrdenDTO orden = new OrdenDTO(
                    idOrden,
                    Integer.parseInt(idEquipo),
                    Integer.parseInt(idTecnico),
                    tipoMantenimiento(tipo),
                    LocalDate.parse(fechaProgramada),
                    fecha(fechaInicio),
                    fecha(fechaCierre),
                    descripcion,
                    new BigDecimal(costoEstimado),
                    decimal(costoReal),
                    estadoOrden(estado)
            );
            if (sigomei != null) {
                if (ordenEditar == null) {
                    sigomei.registrarOrden(orden);
                } else {
                    sigomei.actualizarOrden(orden);
                }
            } else if (ordenEditar == null) {
                ordenesDemo.add(orden);
            } else {
                reemplazarOrdenDemo(orden);
            }
            mostrarOrdenes();
        } catch (Exception ex) {
            mostrarError("No se pudo guardar la orden", ex.getMessage());
        }
    }

    private void mostrarOrdenEstado() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-shell");
        root.setLeft(menuLateral("ordenes"));

        VBox card = new VBox(18);
        card.getStyleClass().add("form-card");
        card.setPadding(new Insets(26));

        GridPane info = new GridPane();
        info.setHgap(110);
        info.setVgap(8);
        agregarTexto(info, 0, 0, "Equipo", "Tablero T-01");
        agregarTexto(info, 1, 0, "Tecnico asignado", "Ana Lopez - Nivel III");
        agregarTexto(info, 0, 1, "Tipo de mantenimiento", "Correctivo");
        agregarTexto(info, 1, 1, "Fecha programada", "2026-05-18");
        agregarTexto(info, 0, 2, "Fecha de inicio", "2026-05-18");
        agregarTexto(info, 1, 2, "Costo estimado", "$4,200.00");

        HBox flujo = new HBox(14,
                labelClase("Programada", "state-pill"),
                labelClase("->", "state-arrow"),
                labelClase("En ejecucion", "state-pill"),
                labelClase("->", "state-arrow"),
                labelClase("Finalizada", "state-pill"),
                labelClase("Cancelada", "state-pill")
        );
        flujo.setAlignment(Pos.CENTER_LEFT);

        Label aviso = labelClase("Para finalizar la orden se requiere ingresar\nla fecha de cierre y el costo real.", "warning-box");

        HBox campos = new HBox(40, new VBox(6, labelClase("Fecha de cierre", "form-label"), campo("2026-05-19")),
                new VBox(6, labelClase("Costo real", "form-label"), campo("$3,850.00")));

        Button cancelar = new Button("Cancelar");
        cancelar.getStyleClass().add("solid-button");
        cancelar.setOnAction(event -> mostrarMenu());
        Button confirmar = new Button("Confirmar cambio");
        confirmar.getStyleClass().add("solid-button");
        confirmar.setOnAction(event -> cambiarOrdenDemo());
        HBox acciones = new HBox(80, cancelar, confirmar);
        acciones.setAlignment(Pos.CENTER);

        card.getChildren().addAll(labelClase("Informacion de la orden", "section-title"), info,
                labelClase("Flujo de estados", "section-title"), flujo, labelClase("Nuevo estado", "field-label"),
                new HBox(18, labelClase("Finalizada", "state-option"), labelClase("Cancelada", "state-option")),
                aviso, campos, acciones);

        VBox contenido = new VBox(22, encabezadoModulo("OR", "Actualizar estado - Orden OM-002", "", () -> mensajeDemo()), card);
        contenido.setPadding(new Insets(24));
        root.setCenter(contenido);
        ponerEscena(root, 980, 820);
    }

    private void agregarTexto(GridPane grid, int col, int row, String titulo, String valor) {
        VBox box = new VBox(5, labelClase(titulo, "field-label"), labelClase(valor, "info-value"));
        grid.add(box, col, row);
    }

    private void cambiarOrdenDemo() {
        try {
            if (sigomei != null) {
                sigomei.cambiarEstadoOrden(2, EstadoOrden.FINALIZADA, LocalDate.of(2026, 5, 19), new BigDecimal("3850.00"));
            }
            mostrarMenu();
        } catch (Exception ex) {
            mensajeDemo();
        }
    }

    private void mensajeDemo() {
        System.out.println("Accion de demostracion");
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("SIGOMEI");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje == null ? "Operacion rechazada" : mensaje);
        alert.showAndWait();
    }

    private TipoEquipo tipoEquipo(String texto) {
        return TipoEquipo.valueOf(texto.toUpperCase().replace(" ", "_"));
    }

    private boolean esTodos(String texto) {
        return texto == null || texto.toLowerCase().contains("todos") || texto.toLowerCase().contains("todas");
    }

    private EstadoOperativo estadoEquipo(String texto) {
        return EstadoOperativo.valueOf(texto.toUpperCase().replace(" ", "_"));
    }

    private EstadoTecnico estadoTecnico(String texto) {
        return EstadoTecnico.valueOf(texto.toUpperCase());
    }

    private Criticidad criticidad(String texto) {
        return Criticidad.valueOf(texto.toUpperCase());
    }

    private TipoMantenimiento tipoMantenimiento(String texto) {
        return TipoMantenimiento.valueOf(texto.toUpperCase());
    }

    private EstadoOrden estadoOrden(String texto) {
        return EstadoOrden.valueOf(texto.toUpperCase().replace(" ", "_"));
    }

    private LocalDate fecha(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return LocalDate.parse(texto);
    }

    private BigDecimal decimal(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return new BigDecimal(texto);
    }

    private String textoTipo(TipoEquipo tipo) {
        return formatoEnum(tipo.name());
    }

    private String textoEstadoEquipo(EstadoOperativo estado) {
        return formatoEnum(estado.name());
    }

    private String textoEstadoTecnico(EstadoTecnico estado) {
        return formatoEnum(estado.name());
    }

    private String textoCriticidad(Criticidad criticidad) {
        return formatoEnum(criticidad.name());
    }

    private String textoTipoMantenimiento(TipoMantenimiento tipo) {
        return formatoEnum(tipo.name());
    }

    private String textoEstadoOrden(EstadoOrden estado) {
        return formatoEnum(estado.name());
    }

    private String formatoEnum(String texto) {
        String limpio = texto.toLowerCase().replace("_", " ");
        return limpio.substring(0, 1).toUpperCase() + limpio.substring(1);
    }

    private List<EquipoDTO> obtenerEquipos() {
        try {
            if (sigomei != null) {
                return sigomei.consultarEquipos();
            }
        } catch (Exception ignored) {
        }
        return new ArrayList<>(equiposDemo);
    }

    private List<TecnicoDTO> obtenerTecnicos() {
        try {
            if (sigomei != null) {
                return sigomei.consultarTecnicos();
            }
        } catch (Exception ignored) {
        }
        return new ArrayList<>(tecnicosDemo);
    }

    private List<OrdenDTO> obtenerOrdenes() {
        try {
            if (sigomei != null) {
                return sigomei.consultarOrdenes();
            }
        } catch (Exception ignored) {
        }
        return new ArrayList<>(ordenesDemo);
    }

    private int siguienteIdEquipo() {
        int mayor = 99;
        for (EquipoDTO equipo : obtenerEquipos()) {
            if (equipo.getIdEquipo() > mayor) {
                mayor = equipo.getIdEquipo();
            }
        }
        return mayor + 1;
    }

    private int siguienteIdTecnico() {
        int mayor = 99;
        for (TecnicoDTO tecnico : obtenerTecnicos()) {
            if (tecnico.getIdTecnico() > mayor) {
                mayor = tecnico.getIdTecnico();
            }
        }
        return mayor + 1;
    }

    private int siguienteIdOrden() {
        int mayor = 99;
        for (OrdenDTO orden : obtenerOrdenes()) {
            if (orden.getIdOrden() > mayor) {
                mayor = orden.getIdOrden();
            }
        }
        return mayor + 1;
    }

    private void reemplazarEquipoDemo(EquipoDTO equipo) {
        for (int i = 0; i < equiposDemo.size(); i++) {
            if (equiposDemo.get(i).getIdEquipo() == equipo.getIdEquipo()) {
                equiposDemo.set(i, equipo);
                return;
            }
        }
    }

    private void reemplazarTecnicoDemo(TecnicoDTO tecnico) {
        for (int i = 0; i < tecnicosDemo.size(); i++) {
            if (tecnicosDemo.get(i).getIdTecnico() == tecnico.getIdTecnico()) {
                tecnicosDemo.set(i, tecnico);
                return;
            }
        }
    }

    private void reemplazarOrdenDemo(OrdenDTO orden) {
        for (int i = 0; i < ordenesDemo.size(); i++) {
            if (ordenesDemo.get(i).getIdOrden() == orden.getIdOrden()) {
                ordenesDemo.set(i, orden);
                return;
            }
        }
    }

    private int contarEquipos() {
        return obtenerEquipos().size();
    }

    private int contarTecnicos() {
        return obtenerTecnicos().size();
    }

    private int contarOrdenes() {
        return obtenerOrdenes().size();
    }

    private int contarOperativos() {
        int total = 0;
        for (EquipoDTO equipo : obtenerEquipos()) {
            if (equipo.getEstadoOperativo() == EstadoOperativo.OPERATIVO) {
                total++;
            }
        }
        return total;
    }

    private int contarCriticos() {
        int total = 0;
        for (EquipoDTO equipo : obtenerEquipos()) {
            if (equipo.getCriticidad() == Criticidad.ALTA) {
                total++;
            }
        }
        return total;
    }

    private int contarOrdenesActivas() {
        int total = 0;
        for (OrdenDTO orden : obtenerOrdenes()) {
            if (orden.getEstadoOrden() == EstadoOrden.PROGRAMADA || orden.getEstadoOrden() == EstadoOrden.EN_EJECUCION) {
                total++;
            }
        }
        return total;
    }

    private int contarOrdenesPorEstado(EstadoOrden estado) {
        int total = 0;
        for (OrdenDTO orden : obtenerOrdenes()) {
            if (orden.getEstadoOrden() == estado) {
                total++;
            }
        }
        return total;
    }

    private String nombreUsuario() {
        if (usuarioActual == null || usuarioActual.getNombreUsuario() == null) {
            return "coordinador01";
        }
        return usuarioActual.getNombreUsuario();
    }

    private Label labelClase(String texto, String clase) {
        Label label = new Label(texto);
        label.getStyleClass().add(clase);
        return label;
    }

    private Region spacer() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }

    private void ponerEscena(javafx.scene.Parent root, int ancho, int alto) {
        Scene scene = new Scene(root, ancho, alto);
        String css = getClass().getResource("/styles/sigomei.css").toExternalForm();
        scene.getStylesheets().add(css);
        stage.setScene(scene);
    }
}
