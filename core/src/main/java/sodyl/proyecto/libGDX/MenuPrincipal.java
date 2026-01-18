package sodyl.proyecto.libGDX;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import sodyl.proyecto.clases.UserManager;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;

public class MenuPrincipal implements Screen, InputProcessor {

    private final Proyecto juego;
    private Stage escenario;
    private BitmapFont fuente;

    private Texture texturaFondo;
    private Image imagenFondo;
    private Texture texturaTitulo;
    private Image imagenTitulo;

    // TEXTURAS PARA LOS BOTONES PNG
    private Texture texturaBotonNuevaPartida;
    private Texture texturaBotonNuevaPartidaSeleccionado;
    private Texture texturaBotonContinuar;
    private Texture texturaBotonContinuarSeleccionado;
    private Texture texturaBotonOpciones;
    private Texture texturaBotonOpcionesSeleccionado;
    private Texture texturaBotonSalir;
    private Texture texturaBotonSalirSeleccionado;
    private Texture texturaBotonAyuda;
    private Texture texturaBotonAyudaSeleccionado;
    private Texture texturaBotonAcercaDe;
    private Texture texturaBotonAcercaDeSeleccionado;
    private Texture texturaBotonVolver;
    private Texture texturaBotonVolverSeleccionado;
    private Texture texturaBotonSolitario;
    private Texture texturaBotonSolitarioSeleccionado;
    private Texture texturaBotonMultijugador;
    private Texture texturaBotonMultijugadorSeleccionado;

    // Texturas para el fondo de los botones
    private Texture texturaBotonPresionado;

    // Imagenes dentro de los botones para poder cambiar la textura
    private Image imagenNuevaPartida;
    private Image imagenOpciones;
    private Image imagenSalir;
    private Image imagenSolitarioNuevaPartida;
    private Image imagenSolitarioContinuarPartida;

    // Imagenes para sub-menus
    private Image imagenAyuda;
    private Image imagenAcercaDe;
    private Image imagenVolverOpciones;
    private Image imagenSolitario;
    private Image imagenMultijugador;
    private Image imagenVolverNuevaPartida;

    // Etiquetas para el submenu de solitario
    private Label etiquetaSolitarioNuevaPartida;
    private Label etiquetaSolitarioContinuarPartida;
    private Label etiquetaSolitarioVolver;
    private Label etiquetaSolitarioTitulo;

    // Enum para el estado del menu
    public enum MenuState {
        MAIN, OPTIONS_SUB, PLAY_SUB, SOLITARIO_SUB
    }

    private MenuState currentState = MenuState.MAIN;
    private MenuState estadoInicial = MenuState.MAIN;

    private Button[] botonesActuales;
    private Button[] botonesMenuPrincipal;
    private Button[] botonesSubMenuOpciones;
    private Button[] botonesSubMenuJugar;
    private Button[] botonesSubMenuSolitario;

    private Button botonSalir;

    private Table tablaPrincipal;
    private Table tablaSubMenuOpciones;
    private Table tablaSubMenuJugar;
    private Table tablaSubMenuSolitario;
    private Table tablaPie;
    private Table tablaCabecera;

    private int indiceSeleccionado = 0;

    // Variables de Transición
    private final float duracionDesvanecimiento = 1.0f;
    private float temporizadorDesvanecimiento = 0f;
    private Texture texturaPixelNegro;
    private boolean entradaHabilitada = false;
    private boolean entradaDesvanecimientoInicializada = false;
    private boolean saltarDesvanecimientoInicial = false;

    // Variables para Error Overlay
    private Table tablaError;
    private Label etiquetaError;
    private Button botonErrorOk;
    private Image imagenOkError;

    // Variables para IP Dialog (Overlay)
    private Table tablaDialogoIp;
    private TextField campoTextoIp;
    private Button botonIpConectar;
    private Button botonIpCancelar;
    private boolean esVisibleDialogoIp = false;

    public MenuPrincipal(Proyecto juego) {
        this.juego = juego;
    }

    public MenuPrincipal(Proyecto juego, boolean saltarDesvanecimientoInicial) {
        this.juego = juego;
        this.saltarDesvanecimientoInicial = saltarDesvanecimientoInicial;
    }

    public MenuPrincipal(Proyecto juego, boolean saltarDesvanecimientoInicial, MenuState estadoInicial) {
        this.juego = juego;
        this.saltarDesvanecimientoInicial = saltarDesvanecimientoInicial;
        this.estadoInicial = estadoInicial;
    }

    @Override
    public void show() {
        juego.playMusic("musica/jumpUp.mp3");
        // Cargar fuente personalizada "ari-w9500-bold.ttf"
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("Mapa/ari-w9500-bold.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = 28;
        parameter.color = Color.WHITE;
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "áéíóúÁÉÍÓÚñÑ¿¡";
        fuente = generator.generateFont(parameter);
        generator.dispose();

        // INICIALIZACIÓN DE TEXTURAS

        // Texturas de Fondo
        Texture buttonUpTextureDummy = createColoredTexture(Color.CLEAR);
        texturaBotonPresionado = createColoredTexture(new Color(0.1f, 0.5f, 0.8f, 0.7f));

        // Texturas de PNG
        texturaBotonNuevaPartida = loadTexture("imagenes/jugar copy.png", new Color(0.8f, 0.2f, 0.2f, 0.8f));
        texturaBotonNuevaPartidaSeleccionado = loadTexture("imagenes/jugarR.png", new Color(1f, 0.8f, 0.2f, 1f));
        texturaBotonContinuar = loadTexture("imagenes/continuarJuego.png", new Color(0.2f, 0.8f, 0.2f, 0.8f));
        texturaBotonContinuarSeleccionado = loadTexture("imagenes/continuarS.png", new Color(1f, 0.8f, 0.2f, 1f));
        texturaBotonOpciones = loadTexture("imagenes/opciones (2).png", new Color(0.2f, 0.2f, 0.8f, 0.8f));
        texturaBotonOpcionesSeleccionado = loadTexture("imagenes/opcionesS.png", new Color(1f, 0.8f, 0.2f, 1f));
        texturaBotonSalir = loadTexture("imagenes/salir (2).png", new Color(0.8f, 0.8f, 0.2f, 0.8f));
        texturaBotonSalirSeleccionado = loadTexture("imagenes/salirS.png", new Color(1f, 0.8f, 0.2f, 1f));
        texturaBotonAyuda = loadTexture("imagenes/ayud.png", new Color(0.1f, 0.5f, 0.8f, 0.8f));
        texturaBotonAyudaSeleccionado = loadTexture("imagenes/ayudS.png", new Color(0.2f, 0.8f, 1f, 1f));
        texturaBotonAcercaDe = loadTexture("imagenes/acerc.png", new Color(0.5f, 0.1f, 0.8f, 0.8f));
        texturaBotonAcercaDeSeleccionado = loadTexture("imagenes/acercS.png", new Color(0.8f, 0.2f, 1f, 1f));
        texturaBotonVolver = loadTexture("imagenes/volv.png", new Color(0.8f, 0.1f, 0.5f, 0.8f));
        texturaBotonVolverSeleccionado = loadTexture("imagenes/volvS.png", new Color(1f, 0.2f, 0.6f, 1f));
        texturaBotonSolitario = loadTexture("imagenes/solit.png", new Color(0.1f, 0.8f, 0.5f, 0.8f));
        texturaBotonSolitarioSeleccionado = loadTexture("imagenes/solitS.png", new Color(0.2f, 1f, 0.6f, 1f));
        texturaBotonMultijugador = loadTexture("imagenes/multi.png", new Color(0.5f, 0.8f, 0.1f, 0.8f));
        texturaBotonMultijugadorSeleccionado = loadTexture("imagenes/multiS.png", new Color(0.8f, 1f, 0.2f, 1f));

        // Texturas de Fondo y Título

        Pixmap pixmapOverlay = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmapOverlay.setColor(Color.BLACK);
        pixmapOverlay.fill();
        texturaPixelNegro = new Texture(pixmapOverlay);
        pixmapOverlay.dispose();

        texturaFondo = new Texture(Gdx.files.internal("imagenes/fondoPixelado.png"));
        imagenFondo = new Image(texturaFondo);
        texturaTitulo = new Texture(Gdx.files.internal("imagenes/Men-Principal-11-11-2025.png"));
        imagenTitulo = new Image(texturaTitulo);

        // Estilos de Botón Base
        ButtonStyle defaultStyle = new ButtonStyle();
        defaultStyle.up = new TextureRegionDrawable(buttonUpTextureDummy);
        defaultStyle.down = new TextureRegionDrawable(texturaBotonPresionado);

        // CREACIÓN DE BOTONES

        // Menú Principal
        imagenNuevaPartida = new Image(texturaBotonNuevaPartida);
        Button botonNuevaPartida = new Button(defaultStyle);
        botonNuevaPartida.add(imagenNuevaPartida).grow();

        Image imagenContinuarJuego = new Image(texturaBotonContinuar);
        Button botonContinuarJuego = new Button(defaultStyle);
        botonContinuarJuego.add(imagenContinuarJuego).grow();

        imagenOpciones = new Image(texturaBotonOpciones);
        Button botonOpciones = new Button(defaultStyle);
        botonOpciones.add(imagenOpciones).grow();

        imagenSalir = new Image(texturaBotonSalir);
        botonSalir = new Button(defaultStyle);
        botonSalir.add(imagenSalir).grow();

        // Sub-Menu Opciones
        imagenAyuda = new Image(texturaBotonAyuda);
        Button botonAyuda = new Button(defaultStyle);
        botonAyuda.add(imagenAyuda).grow();

        imagenAcercaDe = new Image(texturaBotonAcercaDe);
        Button botonAcercaDe = new Button(defaultStyle);
        botonAcercaDe.add(imagenAcercaDe).grow();

        imagenVolverOpciones = new Image(texturaBotonVolver);
        Button botonVolverOpciones = new Button(defaultStyle);
        botonVolverOpciones.add(imagenVolverOpciones).grow();

        // Sub-Menu Nueva Partida
        imagenSolitario = new Image(texturaBotonSolitario);
        Button botonSolitario = new Button(defaultStyle);
        botonSolitario.add(imagenSolitario).grow();

        imagenMultijugador = new Image(texturaBotonMultijugador);
        Button botonMultijugador = new Button(defaultStyle);
        botonMultijugador.add(imagenMultijugador).grow();

        imagenVolverNuevaPartida = new Image(texturaBotonVolver);
        Button botonVolverNuevaPartida = new Button(defaultStyle);
        botonVolverNuevaPartida.add(imagenVolverNuevaPartida).grow();

        // Sub Menu (Solitario: Nuevo/Continuar)
        imagenSolitarioNuevaPartida = new Image(texturaBotonNuevaPartida);
        Button botonIniciarNuevaPartida = new Button(defaultStyle);
        botonIniciarNuevaPartida.add(imagenSolitarioNuevaPartida).grow();

        imagenSolitarioContinuarPartida = new Image(texturaBotonContinuar);
        Button botonIniciarContinuarPartida = new Button(defaultStyle);
        botonIniciarContinuarPartida.add(imagenSolitarioContinuarPartida).grow();

        Button botonVolverSolitario = new Button(defaultStyle);
        botonVolverSolitario.add(new Image(texturaBotonVolver)).grow();

        // Definición de Arrays de Botones
        botonesMenuPrincipal = new Button[] { botonNuevaPartida, botonOpciones, botonSalir };
        botonesSubMenuOpciones = new Button[] { botonAyuda, botonAcercaDe, botonVolverOpciones };
        botonesSubMenuJugar = new Button[] { botonSolitario, botonMultijugador, botonVolverNuevaPartida };
        botonesSubMenuSolitario = new Button[] { botonIniciarNuevaPartida, botonIniciarContinuarPartida,
                botonVolverSolitario };

        botonesActuales = botonesMenuPrincipal;

        escenario = new Stage(new FitViewport(Proyecto.PANTALLA_W, Proyecto.PANTALLA_H));

        // Configuración del Stage y Tablas

        imagenFondo.setFillParent(true);
        escenario.addActor(imagenFondo);

        // Tabla de Cabecera (Título Fijo)
        tablaCabecera = new Table();
        tablaCabecera.setFillParent(true);
        tablaCabecera.top();
        tablaCabecera.add(imagenTitulo).width(600).height(200).padTop(50).padBottom(0);
        tablaCabecera.setVisible(false);
        escenario.addActor(tablaCabecera);

        // Tabla Principal (Alineada a la izquierda)
        tablaPrincipal = new Table();
        tablaPrincipal.setFillParent(true);

        tablaPrincipal.center().left();
        tablaPrincipal.clearChildren();

        // Padding superior e izquierdo para posicionar los botones más abajo
        tablaPrincipal.padTop(200).padLeft(50).row();

        // Botones principales (tamaño reducido)
        float mainButtonWidth = 300f;
        float mainButtonHeight = 110f;
        float buttonSpacing = 20f;

        tablaPrincipal.add(botonNuevaPartida).width(mainButtonWidth).height(mainButtonHeight).padBottom(buttonSpacing)
                .left().row();
        tablaPrincipal.add(botonOpciones).width(mainButtonWidth).height(mainButtonHeight).padBottom(buttonSpacing)
                .left().row();

        tablaPrincipal.add().growY().row();

        escenario.addActor(tablaPrincipal);

        // Tabla para el botón de SALIR
        tablaPie = new Table();
        tablaPie.setFillParent(true);
        tablaPie.bottom().left();
        tablaPie.add(botonSalir).width(mainButtonWidth).height(mainButtonHeight).pad(20).align(Align.bottomLeft);
        escenario.addActor(tablaPie);

        // Tablas Sub-Menú (Opciones)

        tablaSubMenuOpciones = new Table();
        tablaSubMenuOpciones.setFillParent(true);
        tablaSubMenuOpciones.center().left();
        tablaSubMenuOpciones.padTop(65).padLeft(50).row();

        tablaSubMenuOpciones.add(botonAyuda).width(mainButtonWidth).height(mainButtonHeight).padBottom(buttonSpacing)
                .left().row();
        tablaSubMenuOpciones.add(botonAcercaDe).width(mainButtonWidth).height(mainButtonHeight).padBottom(buttonSpacing)
                .left().row();
        tablaSubMenuOpciones.add(botonVolverOpciones).width(mainButtonWidth).height(mainButtonHeight)
                .padBottom(buttonSpacing).left().row();

        tablaSubMenuOpciones.setVisible(false);
        escenario.addActor(tablaSubMenuOpciones);

        // Tablas Sub-Menú (Jugar)

        tablaSubMenuJugar = new Table();
        tablaSubMenuJugar.setFillParent(true);
        tablaSubMenuJugar.center().left();
        tablaSubMenuJugar.padTop(65).padLeft(50).row();

        tablaSubMenuJugar.add(botonSolitario).width(mainButtonWidth).height(mainButtonHeight).padBottom(buttonSpacing)
                .left().row();
        tablaSubMenuJugar.add(botonMultijugador).width(mainButtonWidth).height(mainButtonHeight)
                .padBottom(buttonSpacing).left().row();
        tablaSubMenuJugar.add(botonVolverNuevaPartida).width(mainButtonWidth).height(mainButtonHeight)
                .padBottom(buttonSpacing).left().row();

        tablaSubMenuJugar.setVisible(false);
        escenario.addActor(tablaSubMenuJugar);

        // Tablas Sub-Menú (Solitario: Nuevo/Continuar)

        LabelStyle estiloTituloSolitario = new LabelStyle(fuente, Color.YELLOW);
        LabelStyle estiloEtiquetaSolitario = new LabelStyle(fuente, Color.WHITE);

        etiquetaSolitarioTitulo = new Label("--- MODO SOLITARIO ---", estiloTituloSolitario);
        etiquetaSolitarioNuevaPartida = new Label("Nuevo Juego", estiloEtiquetaSolitario);
        etiquetaSolitarioContinuarPartida = new Label("Continuar Juego", estiloEtiquetaSolitario);
        etiquetaSolitarioVolver = new Label("Volver", estiloEtiquetaSolitario);

        Texture darkBackground = createColoredTexture(new Color(0.1f, 0.1f, 0.1f, 0.85f));
        TextureRegionDrawable backgroundDrawable = new TextureRegionDrawable(darkBackground);

        tablaSubMenuSolitario = new Table();
        tablaSubMenuSolitario.setBackground(backgroundDrawable);
        tablaSubMenuSolitario.pad(40);

        tablaSubMenuSolitario.add(etiquetaSolitarioTitulo).padBottom(30).row();
        tablaSubMenuSolitario.add(etiquetaSolitarioNuevaPartida).padBottom(20).row();
        tablaSubMenuSolitario.add(etiquetaSolitarioContinuarPartida).padBottom(20).row();
        tablaSubMenuSolitario.add(etiquetaSolitarioVolver).padBottom(10).row();

        tablaSubMenuSolitario.pack();

        tablaSubMenuSolitario.setPosition(
                (Proyecto.PANTALLA_W - tablaSubMenuSolitario.getWidth()) / 2,
                (Proyecto.PANTALLA_H - tablaSubMenuSolitario.getHeight()) / 2);

        tablaSubMenuSolitario.setVisible(false);
        escenario.addActor(tablaSubMenuSolitario);

        // Tabla de Error (Overlay)
        tablaError = new Table();
        tablaError.setBackground(backgroundDrawable);
        tablaError.pad(40);

        LabelStyle estiloEtiquetaError = new LabelStyle(fuente, Color.RED);
        etiquetaError = new Label("Error", estiloEtiquetaError);
        etiquetaError.setWrap(true);
        etiquetaError.setAlignment(Align.center);

        imagenOkError = new Image(texturaBotonVolver);
        botonErrorOk = new Button(defaultStyle);
        botonErrorOk.add(imagenOkError).grow();

        tablaError.add(etiquetaError).width(400).padBottom(30).row();
        tablaError.add(botonErrorOk).width(mainButtonWidth).height(mainButtonHeight).row();

        tablaError.pack();
        tablaError.setPosition(
                (Proyecto.PANTALLA_W - tablaError.getWidth()) / 2,
                (Proyecto.PANTALLA_H - tablaError.getHeight()) / 2);

        tablaError.setVisible(false);
        escenario.addActor(tablaError);

        botonErrorOk.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                closeError();
            }
        });

        // Tabla de IP Input (Overlay)
        tablaDialogoIp = new Table();
        tablaDialogoIp.setBackground(backgroundDrawable);
        tablaDialogoIp.pad(40);

        LabelStyle estiloEtiquetaIp = new LabelStyle(fuente, Color.WHITE);
        Label etiquetaInstruccionIp = new Label("Ingresa la IP del Servidor:", estiloEtiquetaIp);
        etiquetaInstruccionIp.setAlignment(Align.center);

        TextFieldStyle estiloCampoTexto = new TextFieldStyle();
        estiloCampoTexto.font = fuente;
        estiloCampoTexto.fontColor = Color.WHITE;
        Texture pixelBlanco = createColoredTexture(Color.WHITE);
        estiloCampoTexto.cursor = new TextureRegionDrawable(pixelBlanco);
        estiloCampoTexto.selection = new TextureRegionDrawable(
                new TextureRegionDrawable(createColoredTexture(Color.BLUE)));
        estiloCampoTexto.background = new TextureRegionDrawable(createColoredTexture(new Color(0.3f, 0.3f, 0.3f, 1f)));

        campoTextoIp = new TextField("localhost", estiloCampoTexto);
        campoTextoIp.setAlignment(Align.center);

        Label etiquetaConectar = new Label("Conectar", estiloEtiquetaIp);
        botonIpConectar = new Button(defaultStyle);
        botonIpConectar.add(etiquetaConectar).center();

        Label etiquetaCancelar = new Label("Cancelar", estiloEtiquetaIp);
        botonIpCancelar = new Button(defaultStyle);
        botonIpCancelar.add(etiquetaCancelar).center();

        tablaDialogoIp.add(etiquetaInstruccionIp).colspan(2).padBottom(20).center().row();
        tablaDialogoIp.add(campoTextoIp).width(400).height(50).colspan(2).padBottom(30).center().row();
        tablaDialogoIp.add(botonIpConectar).width(200).height(60).padRight(20);
        tablaDialogoIp.add(botonIpCancelar).width(200).height(60).row();

        tablaDialogoIp.pack();
        tablaDialogoIp.setPosition(
                (Proyecto.PANTALLA_W - tablaDialogoIp.getWidth()) / 2,
                (Proyecto.PANTALLA_H - tablaDialogoIp.getHeight()) / 2);

        tablaDialogoIp.setVisible(false);
        escenario.addActor(tablaDialogoIp);

        // Listeners para IP Dialog
        botonIpConectar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String ip = campoTextoIp.getText();
                if (ip == null || ip.trim().isEmpty())
                    ip = "localhost";
                Gdx.app.log("MENU", "Conectando a IP: " + ip);
                juego.setScreen(new ScreenMultiplayer(juego, ip));
            }
        });

        botonIpCancelar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hideIpDialog();
            }
        });

        // Click Listeners para el Modo Solitario
        etiquetaSolitarioNuevaPartida.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                indiceSeleccionado = 0;
                executeAction(0);
            }
        });

        etiquetaSolitarioContinuarPartida.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                indiceSeleccionado = 1;
                executeAction(1);
            }
        });

        etiquetaSolitarioVolver.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                indiceSeleccionado = 2;
                executeAction(2);
            }
        });

        // Lógica de Transición Inicial
        if (saltarDesvanecimientoInicial) {
            temporizadorDesvanecimiento = duracionDesvanecimiento;
        }

        // Listeners (Ajustado el de Nuevo Juego/Solitario)
        for (int i = 0; i < botonesMenuPrincipal.length; i++) {
            final int index = i;
            botonesMenuPrincipal[i].addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (entradaHabilitada && currentState == MenuState.MAIN)
                        executeAction(index);
                }
            });
        }

        botonSalir.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (entradaHabilitada && currentState == MenuState.MAIN) {
                    Gdx.app.log("MENU", "Saliendo del juego...");
                    Gdx.app.exit();
                }
            }
        });

        // Submenu Options Listeners
        for (int i = 0; i < botonesSubMenuOpciones.length; i++) {
            final int index = i;
            botonesSubMenuOpciones[i].addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (entradaHabilitada && currentState == MenuState.OPTIONS_SUB)
                        executeAction(index);
                }
            });
        }

        // Submenu Play Listeners
        for (int i = 0; i < botonesSubMenuJugar.length; i++) {
            final int index = i;
            botonesSubMenuJugar[i].addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (entradaHabilitada && currentState == MenuState.PLAY_SUB)
                        executeAction(index);
                }
            });
        }

        // Submenu Solitario Listeners
        for (int i = 0; i < botonesSubMenuSolitario.length; i++) {
            final int index = i;
            botonesSubMenuSolitario[i].addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (entradaHabilitada && currentState == MenuState.SOLITARIO_SUB)
                        executeAction(index);
                }
            });
        }

        switchMenu(estadoInicial);
    }

    // Método de utilidad para cargar texturas o crear una de color si falla
    private Texture loadTexture(String path, Color fallbackColor) {
        return new Texture(Gdx.files.internal(path));
    }

    // Método para cambiar entre los estados del menú
    private void switchMenu(MenuState newState) {
        if (!entradaHabilitada)
            return;

        // Ocultamos todas las tablas de contenido (Main, Options, Play, Solitario)
        tablaPrincipal.setVisible(false);
        tablaSubMenuOpciones.setVisible(false);
        tablaSubMenuJugar.setVisible(false);
        tablaSubMenuSolitario.setVisible(false);

        // El título siempre está oculto
        tablaCabecera.setVisible(false);
        // El botón de salir solo se ve en el menú principal.
        tablaPie.setVisible(newState == MenuState.MAIN);

        currentState = newState;
        indiceSeleccionado = 0;

        if (currentState == MenuState.MAIN) {
            tablaPrincipal.setVisible(true);
            botonesActuales = botonesMenuPrincipal;
        } else if (currentState == MenuState.OPTIONS_SUB) {
            tablaSubMenuOpciones.setVisible(true);
            botonesActuales = botonesSubMenuOpciones;
        } else if (currentState == MenuState.PLAY_SUB) {
            tablaSubMenuJugar.setVisible(true);
            botonesActuales = botonesSubMenuJugar;
        } else if (currentState == MenuState.SOLITARIO_SUB) {
            tablaSubMenuJugar.setVisible(true);
            tablaSubMenuSolitario.setVisible(true);
            botonesActuales = null;
        }

        updateSelectionUI();
    }

    private Texture createColoredTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    // Actualiza la UI de la selección
    private void updateSelectionUI() {
        if (currentState != MenuState.SOLITARIO_SUB && (botonesActuales == null || botonesActuales.length == 0)) {
            return;
        }

        // Resetear todos los botones a su estado normal
        if (imagenNuevaPartida != null)
            imagenNuevaPartida.setDrawable(new TextureRegionDrawable(texturaBotonNuevaPartida));
        if (imagenSolitarioNuevaPartida != null)
            imagenSolitarioNuevaPartida.setDrawable(new TextureRegionDrawable(texturaBotonNuevaPartida));
        if (imagenSolitarioContinuarPartida != null)
            imagenSolitarioContinuarPartida.setDrawable(new TextureRegionDrawable(texturaBotonContinuar));
        imagenOpciones.setDrawable(new TextureRegionDrawable(texturaBotonOpciones));
        imagenSalir.setDrawable(new TextureRegionDrawable(texturaBotonSalir));

        imagenAyuda.setDrawable(new TextureRegionDrawable(texturaBotonAyuda));
        imagenAcercaDe.setDrawable(new TextureRegionDrawable(texturaBotonAcercaDe));
        imagenVolverOpciones.setDrawable(new TextureRegionDrawable(texturaBotonVolver));

        imagenSolitario.setDrawable(new TextureRegionDrawable(texturaBotonSolitario));
        imagenMultijugador.setDrawable(new TextureRegionDrawable(texturaBotonMultijugador));
        imagenVolverNuevaPartida.setDrawable(new TextureRegionDrawable(texturaBotonVolver));

        // Aplicar textura seleccionada al botón actual
        if (currentState == MenuState.MAIN) {
            if (indiceSeleccionado == 0) {
                imagenNuevaPartida.setDrawable(new TextureRegionDrawable(texturaBotonNuevaPartidaSeleccionado));
            } else if (indiceSeleccionado == 1) {
                imagenOpciones.setDrawable(new TextureRegionDrawable(texturaBotonOpcionesSeleccionado));
            } else if (indiceSeleccionado == 2) {
                imagenSalir.setDrawable(new TextureRegionDrawable(texturaBotonSalirSeleccionado));
            }
        } else if (currentState == MenuState.OPTIONS_SUB) {
            if (indiceSeleccionado == 0) {
                imagenAyuda.setDrawable(new TextureRegionDrawable(texturaBotonAyudaSeleccionado));
            } else if (indiceSeleccionado == 1) {
                imagenAcercaDe.setDrawable(new TextureRegionDrawable(texturaBotonAcercaDeSeleccionado));
            } else if (indiceSeleccionado == 2) {
                imagenVolverOpciones.setDrawable(new TextureRegionDrawable(texturaBotonVolverSeleccionado));
            }
        } else if (currentState == MenuState.PLAY_SUB) {
            if (indiceSeleccionado == 0) {
                imagenSolitario.setDrawable(new TextureRegionDrawable(texturaBotonSolitarioSeleccionado));
            } else if (indiceSeleccionado == 1) {
                imagenMultijugador.setDrawable(new TextureRegionDrawable(texturaBotonMultijugadorSeleccionado));
            } else if (indiceSeleccionado == 2) {
                imagenVolverNuevaPartida.setDrawable(new TextureRegionDrawable(texturaBotonVolverSeleccionado));
            }
        } else if (currentState == MenuState.SOLITARIO_SUB) {
            etiquetaSolitarioNuevaPartida.setColor(Color.WHITE);
            etiquetaSolitarioContinuarPartida.setColor(Color.WHITE);
            etiquetaSolitarioVolver.setColor(Color.WHITE);

            if (indiceSeleccionado == 0)
                etiquetaSolitarioNuevaPartida.setColor(Color.YELLOW);
            else if (indiceSeleccionado == 1)
                etiquetaSolitarioContinuarPartida.setColor(Color.YELLOW);
            else if (indiceSeleccionado == 2)
                etiquetaSolitarioVolver.setColor(Color.YELLOW);
        }

        if (botonesActuales != null) {
            for (int i = 0; i < botonesActuales.length; i++) {
                botonesActuales[i].setColor(Color.WHITE);
            }
        }
    }

    // Ejecuta la acción correspondiente al botón seleccionado
    private void executeAction(int index) {
        if (!entradaHabilitada)
            return;

        switch (currentState) {
            case MAIN:
                switch (index) {
                    case 0:
                        switchMenu(MenuState.PLAY_SUB);
                        break;
                    case 1:
                        switchMenu(MenuState.OPTIONS_SUB);
                        break;
                    case 2:
                        Gdx.app.log("MENU", "Saliendo del juego...");
                        Gdx.app.exit();
                        break;
                }
                break;
            case PLAY_SUB:
                switch (index) {
                    case 0:
                        switchMenu(MenuState.SOLITARIO_SUB);
                        break;
                    case 1:
                        showIpDialog();
                        break;
                    case 2:
                        switchMenu(MenuState.MAIN);
                        break;
                }
                break;
            case SOLITARIO_SUB:
                String usuarioActual = UserManager.getCurrentUser();
                switch (index) {
                    case 0:
                        if (juego.hasSaveData(usuarioActual)) {
                            Gdx.app.log("MENU", "ADVERTENCIA: Se sobrescribirán los datos.");
                        }
                        juego.clearProgress();
                        Gdx.app.log("MENU", "Iniciando Nuevo Juego.");
                        Gdx.input.setInputProcessor(null);
                        juego.setScreen(new ScreenMapaTiled(juego));
                        break;
                    case 1: // Continuar Juego
                        if (juego.hasSaveData(usuarioActual)) {
                            juego.loadProgress(usuarioActual);
                            Gdx.input.setInputProcessor(null);

                            sodyl.proyecto.clases.PlayerData pData = juego.getPlayerData();

                            if (pData != null) {
                                Gdx.app.log("MENU", "Continuando Partida en: " + pData.currentMap + " (" + pData.x + ","
                                        + pData.y + ")");
                                juego.setScreen(new ScreenMapaTiled(juego, pData.currentMap, null, null, null,
                                        ScreenMapaTiled.GameState.FREE_ROAMING, pData.x, pData.y));
                            } else {
                                Gdx.app.log("MENU", "Datos parciales encontrados. Usando inicio por defecto.");
                                juego.setScreen(new ScreenMapaTiled(juego, "Mapa/MAPACOMPLETO.tmx", null, null, null,
                                        ScreenMapaTiled.GameState.FREE_ROAMING));
                            }
                        } else {
                            Gdx.app.log("MENU", "ERROR: No hay datos guardados.");
                            showError("No se encontraron datos guardados para el usuario: " + usuarioActual);
                        }
                        break;
                    case 2:
                        switchMenu(MenuState.PLAY_SUB);
                        break; // Volver
                }
                break;
            case OPTIONS_SUB:
                switch (index) {
                    case 0:
                        Gdx.app.log("OPTIONS", "Ayuda.");
                        juego.setScreen(new ScreenAyuda(juego));
                        break;
                    case 1:
                        Gdx.app.log("OPTIONS", "Acerca de.");
                        juego.setScreen(new ScreenAcercaDe(juego));
                        break;
                    case 2:
                        switchMenu(MenuState.MAIN);
                        break;
                }
                break;
        }
    }

    // Gestiona la entrada del teclado
    @Override
    public boolean keyDown(int keycode) {
        if (!entradaHabilitada)
            return false;

        if (esVisibleDialogoIp) {
            if (keycode == Keys.ESCAPE) {
                hideIpDialog();
                return true;
            }
            if (keycode == Keys.ENTER) {
                String ip = campoTextoIp.getText();
                if (ip == null || ip.trim().isEmpty())
                    ip = "localhost";
                juego.setScreen(new ScreenMultiplayer(juego, ip));
                return true;
            }
            return false;
        }

        int numItems = getNumItemsInCurrentMenu();
        if (numItems == 0)
            return false;

        if (keycode == Keys.DOWN || keycode == Keys.RIGHT) {
            indiceSeleccionado = (indiceSeleccionado + 1) % numItems;
            updateSelectionUI();
            return true;
        }

        if (keycode == Keys.UP || keycode == Keys.LEFT) {
            indiceSeleccionado = (indiceSeleccionado - 1 + numItems) % numItems;
            updateSelectionUI();
            return true;
        }

        if (keycode == Keys.ENTER || keycode == Keys.Z) {
            executeAction(indiceSeleccionado);
            return true;
        }

        if (keycode == Keys.ESCAPE) {
            if (currentState == MenuState.OPTIONS_SUB || currentState == MenuState.PLAY_SUB) {
                switchMenu(MenuState.MAIN);
                return true;
            } else if (currentState == MenuState.SOLITARIO_SUB) {
                switchMenu(MenuState.PLAY_SUB);
                return true;
            } else if (currentState == MenuState.MAIN) {
                if (indiceSeleccionado != numItems - 1) {
                    indiceSeleccionado = numItems - 1;
                    updateSelectionUI();
                } else {
                    Gdx.app.exit();
                }
                return true;
            }
        }
        return false;
    }

    // Obtiene el número de elementos en el menú actual
    private int getNumItemsInCurrentMenu() {
        if (currentState == MenuState.SOLITARIO_SUB)
            return 3;
        if (botonesActuales != null)
            return botonesActuales.length;
        return 0;
    }

    // Renderiza el menú
    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 0f, 1);
        escenario.act(Gdx.graphics.getDeltaTime());
        escenario.draw();

        if (temporizadorDesvanecimiento < duracionDesvanecimiento) {
            temporizadorDesvanecimiento += delta;

            float alpha = 1.0f - Math.min(1f, temporizadorDesvanecimiento / duracionDesvanecimiento);

            if (alpha > 0.01f) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                escenario.getBatch().begin();
                escenario.getBatch().setColor(0, 0, 0, alpha);
                escenario.getBatch().draw(
                        texturaPixelNegro,
                        0, 0,
                        escenario.getViewport().getWorldWidth(),
                        escenario.getViewport().getWorldHeight());
                escenario.getBatch().end();

                escenario.getBatch().setColor(Color.WHITE);
                Gdx.gl.glDisable(GL20.GL_BLEND);
            }
        }

        if (temporizadorDesvanecimiento >= duracionDesvanecimiento && !entradaDesvanecimientoInicializada) {
            entradaDesvanecimientoInicializada = true;
            entradaHabilitada = true;
            InputMultiplexer multiplexer = new InputMultiplexer();
            multiplexer.addProcessor(this); // Para teclado (flechas/enter)
            multiplexer.addProcessor(escenario); // Para clicks
            Gdx.input.setInputProcessor(multiplexer);
        }
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public void resize(int width, int height) {
        escenario.getViewport().update(width, height, true);
        updateSelectionUI();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        escenario.dispose();
        fuente.dispose();
        texturaFondo.dispose();
        texturaTitulo.dispose();

        if (texturaBotonNuevaPartida != null)
            texturaBotonNuevaPartida.dispose();
        if (texturaBotonContinuar != null)
            texturaBotonContinuar.dispose();
        if (texturaBotonOpciones != null)
            texturaBotonOpciones.dispose();
        if (texturaBotonSalir != null)
            texturaBotonSalir.dispose();
        if (texturaBotonAyuda != null)
            texturaBotonAyuda.dispose();
        if (texturaBotonAcercaDe != null)
            texturaBotonAcercaDe.dispose();
        if (texturaBotonVolver != null)
            texturaBotonVolver.dispose();
        if (texturaBotonSolitario != null)
            texturaBotonSolitario.dispose();
        if (texturaBotonMultijugador != null)
            texturaBotonMultijugador.dispose();

        if (texturaBotonPresionado != null)
            texturaBotonPresionado.dispose();
        if (texturaPixelNegro != null)
            texturaPixelNegro.dispose();

        if (texturaBotonNuevaPartidaSeleccionado != null)
            texturaBotonNuevaPartidaSeleccionado.dispose();
        if (texturaBotonContinuarSeleccionado != null)
            texturaBotonContinuarSeleccionado.dispose();
        if (texturaBotonOpcionesSeleccionado != null)
            texturaBotonOpcionesSeleccionado.dispose();
        if (texturaBotonSalirSeleccionado != null)
            texturaBotonSalirSeleccionado.dispose();

        if (texturaBotonAyudaSeleccionado != null)
            texturaBotonAyudaSeleccionado.dispose();
        if (texturaBotonAcercaDeSeleccionado != null)
            texturaBotonAcercaDeSeleccionado.dispose();
        if (texturaBotonVolverSeleccionado != null)
            texturaBotonVolverSeleccionado.dispose();
        if (texturaBotonSolitarioSeleccionado != null)
            texturaBotonSolitarioSeleccionado.dispose();
        if (texturaBotonMultijugadorSeleccionado != null)
            texturaBotonMultijugadorSeleccionado.dispose();
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    private void showError(String message) {
        if (etiquetaError != null && tablaError != null) {
            etiquetaError.setText(message);
            tablaError.setVisible(true);
            tablaError.toFront();
        } else {
            Gdx.app.log("MENU", "Error al mostrar mensaje visual: " + message);
        }
    }

    private void closeError() {
        if (tablaError != null)
            tablaError.setVisible(false);
    }

    // --- IP Dialog Helpers ---
    private void showIpDialog() {
        if (tablaDialogoIp != null) {
            tablaDialogoIp.setVisible(true);
            esVisibleDialogoIp = true;
            // Focus on text field
            escenario.setKeyboardFocus(campoTextoIp);
            if (campoTextoIp != null) {
                campoTextoIp.selectAll();
            }
        }
    }

    private void hideIpDialog() {
        if (tablaDialogoIp != null) {
            tablaDialogoIp.setVisible(false);
        }
        esVisibleDialogoIp = false;
        escenario.setKeyboardFocus(null);
    }
}
