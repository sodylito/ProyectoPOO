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

public class MenuPrincipal implements Screen, InputProcessor {

    private final Proyecto game;
    private Stage stage;
    private BitmapFont font;

    private Texture backgroundTexture;
    private Image backgroundImage;
    private Texture titleTexture;
    private Image titleImage;

    // --- TEXTURAS PARA LOS BOTONES PNG (DEBES TENERLAS EN assets/imagenes/) ---
    private Texture newGameButtonTexture;
    private Texture newGameButtonSelectedTexture;
    private Texture continueGameButtonTexture;
    private Texture continueGameButtonSelectedTexture;
    private Texture optionsButtonTexture;
    private Texture optionsButtonSelectedTexture;
    private Texture exitButtonTexture;
    private Texture exitButtonSelectedTexture;
    private Texture helpButtonTexture;
    private Texture helpButtonSelectedTexture;
    private Texture aboutButtonTexture;
    private Texture aboutButtonSelectedTexture;
    private Texture backButtonTexture;
    private Texture backButtonSelectedTexture;
    private Texture singlePlayerButtonTexture;
    private Texture singlePlayerButtonSelectedTexture;
    private Texture multiPlayerButtonTexture;
    private Texture multiPlayerButtonSelectedTexture;

    // Texturas para el fondo de los botones (Se mantienen para el efecto down)
    private Texture buttonDownTexture;

    // Images dentro de los botones para poder cambiar la textura
    private Image newGameImage;
    private Image optionsImage;
    private Image exitImage;
    private Image solitaireNewGameImage; // Unique field for solitaire button image
    private Image solitaireContinueGameImage; // Unique field for solitaire button image

    // Images for sub-menus
    private Image helpImage;
    private Image aboutImage;
    private Image backOptionsImage;
    private Image singlePlayerImage;
    private Image multiPlayerImage;
    private Image backNewGameImage;

    // Labels for Solitario Submenu (Label-style)
    private Label solitarioNewGameLabel;
    private Label solitarioContinueGameLabel;
    private Label solitarioBackLabel;
    private Label solitarioTitleLabel;

    // --- Estructura de Menú y Navegación ---
    public enum MenuState {
        MAIN, OPTIONS_SUB, PLAY_SUB, SOLITARIO_SUB
    }

    private MenuState currentState = MenuState.MAIN;
    private MenuState startState = MenuState.MAIN;

    private Button[] currentButtons;
    private Button[] mainMenuButtons;
    private Button[] optionsSubMenuButtons;
    private Button[] playSubMenuButtons;
    private Button[] solitarioSubMenuButtons;

    private Button exitButton;

    private Table mainTable;
    private Table optionsSubMenuTable;
    private Table playSubMenuTable;
    private Table solitarioSubMenuTable;
    private Table footerTable;
    private Table headerTable;

    private int selectedIndex = 0;

    // --- Variables de Transición (Anti-Flicker) ---
    private final float fadeDuration = 0.5f;
    private float fadeTimer = 0f;
    private Texture blackPixelTexture;
    private boolean isInputEnabled = false;
    private boolean skipInitialFade = false;

    // --- Variables para Error Overlay ---
    private Table errorTable;
    private Label errorLabel;
    private Button errorOkButton;
    private Image okErrorImage;
    // ---------------------------------------------

    // ---------------------------------------------

    public MenuPrincipal(Proyecto game) {
        this.game = game;
    }

    public MenuPrincipal(Proyecto game, boolean skipInitialFade) {
        this.game = game;
        this.skipInitialFade = skipInitialFade;
    }

    public MenuPrincipal(Proyecto game, boolean skipInitialFade, MenuState startState) {
        this.game = game;
        this.skipInitialFade = skipInitialFade;
        this.startState = startState;
    }

    @Override
    public void show() {
        // Cargar fuente personalizada "ari-w9500-bold.ttf"
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                    Gdx.files.internal("Mapa/ari-w9500-bold.ttf"));
            FreeTypeFontParameter parameter = new FreeTypeFontParameter();
            parameter.size = 28;
            parameter.color = Color.WHITE;
            parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "áéíóúÁÉÍÓÚñÑ¿¡";
            font = generator.generateFont(parameter);
            generator.dispose();
        } catch (Exception e) {
            Gdx.app.error("MENU", "Error al cargar la fuente. Usando BitmapFont por defecto.", e);
            font = new BitmapFont();
        }

        // --- Inicialización de Texturas ---

        // 1. Texturas de Fondo (Solo usamos el Down para el feedback visual)
        Texture buttonUpTextureDummy = createColoredTexture(Color.CLEAR);
        buttonDownTexture = createColoredTexture(new Color(0.1f, 0.5f, 0.8f, 0.7f));

        // 2. Texturas de PNG (simulamos que existen)
        newGameButtonTexture = loadTexture("imagenes/jugar copy.png", new Color(0.8f, 0.2f, 0.2f, 0.8f));
        newGameButtonSelectedTexture = loadTexture("imagenes/jugarR.png", new Color(1f, 0.8f, 0.2f, 1f));
        continueGameButtonTexture = loadTexture("imagenes/continuarJuego.png", new Color(0.2f, 0.8f, 0.2f, 0.8f));
        continueGameButtonSelectedTexture = loadTexture("imagenes/continuarS.png", new Color(1f, 0.8f, 0.2f, 1f));
        optionsButtonTexture = loadTexture("imagenes/opciones (2).png", new Color(0.2f, 0.2f, 0.8f, 0.8f));
        optionsButtonSelectedTexture = loadTexture("imagenes/opcionesS.png", new Color(1f, 0.8f, 0.2f, 1f));
        exitButtonTexture = loadTexture("imagenes/salir (2).png", new Color(0.8f, 0.8f, 0.2f, 0.8f));
        exitButtonSelectedTexture = loadTexture("imagenes/salirS.png", new Color(1f, 0.8f, 0.2f, 1f));
        helpButtonTexture = loadTexture("imagenes/ayud.png", new Color(0.1f, 0.5f, 0.8f, 0.8f));
        helpButtonSelectedTexture = loadTexture("imagenes/ayudS.png", new Color(0.2f, 0.8f, 1f, 1f));
        aboutButtonTexture = loadTexture("imagenes/acerc.png", new Color(0.5f, 0.1f, 0.8f, 0.8f));
        aboutButtonSelectedTexture = loadTexture("imagenes/acercS.png", new Color(0.8f, 0.2f, 1f, 1f));
        backButtonTexture = loadTexture("imagenes/volv.png", new Color(0.8f, 0.1f, 0.5f, 0.8f));
        backButtonSelectedTexture = loadTexture("imagenes/volvS.png", new Color(1f, 0.2f, 0.6f, 1f));
        singlePlayerButtonTexture = loadTexture("imagenes/solit.png", new Color(0.1f, 0.8f, 0.5f, 0.8f));
        singlePlayerButtonSelectedTexture = loadTexture("imagenes/solitS.png", new Color(0.2f, 1f, 0.6f, 1f));
        multiPlayerButtonTexture = loadTexture("imagenes/multi.png", new Color(0.5f, 0.8f, 0.1f, 0.8f));
        multiPlayerButtonSelectedTexture = loadTexture("imagenes/multiS.png", new Color(0.8f, 1f, 0.2f, 1f));

        // 3. Texturas de Fondo y Título

        Pixmap pixmapOverlay = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmapOverlay.setColor(Color.BLACK);
        pixmapOverlay.fill();
        blackPixelTexture = new Texture(pixmapOverlay);
        pixmapOverlay.dispose();

        backgroundTexture = new Texture(Gdx.files.internal("imagenes/fondoPixelado.png"));
        backgroundImage = new Image(backgroundTexture);
        titleTexture = new Texture(Gdx.files.internal("imagenes/Men-Principal-11-11-2025.png"));
        titleImage = new Image(titleTexture);

        // --- Estilos de Botón Base ---
        ButtonStyle defaultStyle = new ButtonStyle();
        defaultStyle.up = new TextureRegionDrawable(buttonUpTextureDummy);
        defaultStyle.down = new TextureRegionDrawable(buttonDownTexture);

        // --- Creación de Botones (Ahora son Button con un Image dentro) ---

        // Menú Principal - Guardamos las referencias a las imágenes
        newGameImage = new Image(newGameButtonTexture);
        Button newGameButton = new Button(defaultStyle);
        newGameButton.add(newGameImage).grow();

        Image continueGameImage = new Image(continueGameButtonTexture); // Local image for continue
        Button continueGameButton = new Button(defaultStyle);
        continueGameButton.add(continueGameImage).grow();

        optionsImage = new Image(optionsButtonTexture);
        Button optionsButton = new Button(defaultStyle);
        optionsButton.add(optionsImage).grow();

        exitImage = new Image(exitButtonTexture);
        exitButton = new Button(defaultStyle);
        exitButton.add(exitImage).grow();

        // Sub Menu (Opciones)
        helpImage = new Image(helpButtonTexture);
        Button helpButton = new Button(defaultStyle);
        helpButton.add(helpImage).grow();

        aboutImage = new Image(aboutButtonTexture);
        Button aboutButton = new Button(defaultStyle);
        aboutButton.add(aboutImage).grow();

        backOptionsImage = new Image(backButtonTexture);
        Button backOptionsButton = new Button(defaultStyle);
        backOptionsButton.add(backOptionsImage).grow();

        // Sub Menu (Nuevo Juego)
        singlePlayerImage = new Image(singlePlayerButtonTexture);
        Button singlePlayerButton = new Button(defaultStyle);
        singlePlayerButton.add(singlePlayerImage).grow();

        multiPlayerImage = new Image(multiPlayerButtonTexture);
        Button multiPlayerButton = new Button(defaultStyle);
        multiPlayerButton.add(multiPlayerImage).grow();

        backNewGameImage = new Image(backButtonTexture);
        Button backPlayButton = new Button(defaultStyle);
        backPlayButton.add(backNewGameImage).grow();

        // Sub Menu (Solitario: Nuevo/Continuar)
        solitaireNewGameImage = new Image(newGameButtonTexture);
        Button startNewGameButton = new Button(defaultStyle);
        startNewGameButton.add(solitaireNewGameImage).grow();

        solitaireContinueGameImage = new Image(continueGameButtonTexture);
        Button startContinueGameButton = new Button(defaultStyle);
        startContinueGameButton.add(solitaireContinueGameImage).grow();

        Button backSolitarioButton = new Button(defaultStyle);
        backSolitarioButton.add(new Image(backButtonTexture)).grow();

        // --- Definición de Arrays de Botones ---
        mainMenuButtons = new Button[] { newGameButton, optionsButton, exitButton };
        optionsSubMenuButtons = new Button[] { helpButton, aboutButton, backOptionsButton };
        playSubMenuButtons = new Button[] { singlePlayerButton, multiPlayerButton, backPlayButton };
        solitarioSubMenuButtons = new Button[] { startNewGameButton, continueGameButton, backSolitarioButton };

        currentButtons = mainMenuButtons;

        stage = new Stage(new FitViewport(Proyecto.PANTALLA_W, Proyecto.PANTALLA_H));

        // --- Configuración del Stage y Tablas ---

        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);

        // --- Tabla de Cabecera (Título Fijo) - OCULTO ---
        headerTable = new Table();
        headerTable.setFillParent(true);
        headerTable.top();
        headerTable.add(titleImage).width(600).height(200).padTop(50).padBottom(0);
        headerTable.setVisible(false); // Título oculto
        stage.addActor(headerTable);

        // Tabla Principal (Alineada a la izquierda)
        mainTable = new Table();
        mainTable.setFillParent(true);

        mainTable.center().left(); // Alineación centrada verticalmente, izquierda horizontal
        mainTable.clearChildren();

        // Padding superior e izquierdo para posicionar los botones más abajo
        mainTable.padTop(200).padLeft(50).row();

        // Botones principales (tamaño reducido)
        float mainButtonWidth = 300f;
        float mainButtonHeight = 110f;
        float buttonSpacing = 20f; // Mayor separación entre botones

        mainTable.add(newGameButton).width(mainButtonWidth).height(mainButtonHeight).padBottom(buttonSpacing).left()
                .row();
        mainTable.add(optionsButton).width(mainButtonWidth).height(mainButtonHeight).padBottom(buttonSpacing).left()
                .row();

        // Agregamos un growY() final para empujar todo hacia arriba y llenar el resto
        // del espacio.
        mainTable.add().growY().row(); // Keep simple growY if needed, but with center() might behave differently.
        // Actually if we use center(), we might not need growY. Let's remove it to let
        // center() work.
        // mainTable.add().growY().row();

        stage.addActor(mainTable);

        // Ya no usamos cursor, la selección se muestra cambiando la textura del botón

        // Tabla para el botón de SALIR (Esquina Inferior Izquierda)
        footerTable = new Table();
        footerTable.setFillParent(true);
        footerTable.bottom().left(); // Cambiado a izquierda
        footerTable.add(exitButton).width(mainButtonWidth).height(mainButtonHeight).pad(20).align(Align.bottomLeft);
        stage.addActor(footerTable);

        // ----------------------------------------------------------------------
        // Tablas Sub-Menú (Opciones)
        // ----------------------------------------------------------------------

        optionsSubMenuTable = new Table();
        optionsSubMenuTable.setFillParent(true);
        optionsSubMenuTable.center().left(); // Centrado verticalmente a la izquierda
        optionsSubMenuTable.padTop(65).padLeft(50).row();

        optionsSubMenuTable.add(helpButton).width(mainButtonWidth).height(mainButtonHeight).padBottom(buttonSpacing)
                .left().row();
        optionsSubMenuTable.add(aboutButton).width(mainButtonWidth).height(mainButtonHeight).padBottom(buttonSpacing)
                .left().row();
        optionsSubMenuTable.add(backOptionsButton).width(mainButtonWidth).height(mainButtonHeight)
                .padBottom(buttonSpacing).left().row();

        optionsSubMenuTable.setVisible(false);
        stage.addActor(optionsSubMenuTable);

        // ----------------------------------------------------------------------
        // Tablas Sub-Menú (Jugar)
        // ----------------------------------------------------------------------

        playSubMenuTable = new Table();
        playSubMenuTable.setFillParent(true);
        playSubMenuTable.center().left();
        playSubMenuTable.padTop(65).padLeft(50).row();

        playSubMenuTable.add(singlePlayerButton).width(mainButtonWidth).height(mainButtonHeight)
                .padBottom(buttonSpacing)
                .left().row();
        playSubMenuTable.add(multiPlayerButton).width(mainButtonWidth).height(mainButtonHeight).padBottom(buttonSpacing)
                .left().row();
        playSubMenuTable.add(backPlayButton).width(mainButtonWidth).height(mainButtonHeight).padBottom(buttonSpacing)
                .left().row();

        playSubMenuTable.setVisible(false);
        stage.addActor(playSubMenuTable);

        // ----------------------------------------------------------------------
        // Tablas Sub-Menú (Solitario: Nuevo/Continuar) - REFACTORIZADO A LABELS
        // ----------------------------------------------------------------------

        LabelStyle solitarioTitleStyle = new LabelStyle(font, Color.YELLOW);
        LabelStyle solitarioLabelStyle = new LabelStyle(font, Color.WHITE);

        solitarioTitleLabel = new Label("--- MODO SOLITARIO ---", solitarioTitleStyle);
        solitarioNewGameLabel = new Label("Nuevo Juego", solitarioLabelStyle);
        solitarioContinueGameLabel = new Label("Continuar Juego", solitarioLabelStyle);
        solitarioBackLabel = new Label("Volver", solitarioLabelStyle);

        // Background semi-transparente para el "overlay"
        Texture darkBackground = createColoredTexture(new Color(0.1f, 0.1f, 0.1f, 0.85f));
        TextureRegionDrawable backgroundDrawable = new TextureRegionDrawable(darkBackground);

        solitarioSubMenuTable = new Table();
        solitarioSubMenuTable.setBackground(backgroundDrawable);
        solitarioSubMenuTable.pad(40);

        solitarioSubMenuTable.add(solitarioTitleLabel).padBottom(30).row();
        solitarioSubMenuTable.add(solitarioNewGameLabel).padBottom(20).row();
        solitarioSubMenuTable.add(solitarioContinueGameLabel).padBottom(20).row();
        solitarioSubMenuTable.add(solitarioBackLabel).padBottom(10).row();

        solitarioSubMenuTable.pack(); // Ajustar al contenido

        // Centrar en pantalla
        solitarioSubMenuTable.setPosition(
                (Proyecto.PANTALLA_W - solitarioSubMenuTable.getWidth()) / 2,
                (Proyecto.PANTALLA_H - solitarioSubMenuTable.getHeight()) / 2);

        solitarioSubMenuTable.setVisible(false);
        stage.addActor(solitarioSubMenuTable);

        // --- Tabla de Error (Overlay) ---
        errorTable = new Table();
        errorTable.setBackground(backgroundDrawable); // Reutilizamos el fondo oscuro
        errorTable.pad(40);

        LabelStyle errorLabelStyle = new LabelStyle(font, Color.RED);
        errorLabel = new Label("Error", errorLabelStyle);
        errorLabel.setWrap(true);
        errorLabel.setAlignment(Align.center);

        // Boton OK para error
        okErrorImage = new Image(backButtonTexture); // Reutilizamos textura de volver
        errorOkButton = new Button(defaultStyle);
        errorOkButton.add(okErrorImage).grow();

        errorTable.add(errorLabel).width(400).padBottom(30).row();
        errorTable.add(errorOkButton).width(mainButtonWidth).height(mainButtonHeight).row();

        errorTable.pack();
        errorTable.setPosition(
                (Proyecto.PANTALLA_W - errorTable.getWidth()) / 2,
                (Proyecto.PANTALLA_H - errorTable.getHeight()) / 2);

        errorTable.setVisible(false);
        stage.addActor(errorTable);

        errorOkButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                closeError();
            }
        });

        // --- Click Listeners para el Modo Solitario ---
        solitarioNewGameLabel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedIndex = 0;
                executeAction(0);
            }
        });

        solitarioContinueGameLabel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedIndex = 1;
                executeAction(1);
            }
        });

        solitarioBackLabel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedIndex = 2;
                executeAction(2);
            }
        });

        // --- Lógica de Transición Inicial ---
        if (skipInitialFade) {
            fadeTimer = fadeDuration;
        }

        // --- Listeners (Ajustado el de Nuevo Juego/Solitario) ---
        for (int i = 0; i < mainMenuButtons.length; i++) {
            final int index = i;
            mainMenuButtons[i].addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (isInputEnabled && currentState == MenuState.MAIN)
                        executeAction(index);
                }
            });
        }

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (isInputEnabled && currentState == MenuState.MAIN) {
                    Gdx.app.log("MENU", "Saliendo del juego...");
                    Gdx.app.exit();
                }
            }
        });

        // Submenu Options Listeners
        for (int i = 0; i < optionsSubMenuButtons.length; i++) {
            final int index = i;
            optionsSubMenuButtons[i].addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (isInputEnabled && currentState == MenuState.OPTIONS_SUB)
                        executeAction(index);
                }
            });
        }

        // Submenu Play Listeners
        for (int i = 0; i < playSubMenuButtons.length; i++) {
            final int index = i;
            playSubMenuButtons[i].addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (isInputEnabled && currentState == MenuState.PLAY_SUB)
                        executeAction(index);
                }
            });
        }

        // Submenu Solitario Listeners
        for (int i = 0; i < solitarioSubMenuButtons.length; i++) {
            final int index = i;
            solitarioSubMenuButtons[i].addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (isInputEnabled && currentState == MenuState.SOLITARIO_SUB)
                        executeAction(index);
                }
            });
        }

        switchMenu(startState);
    }

    // Método de utilidad para cargar texturas o crear una de color si falla.
    private Texture loadTexture(String path, Color fallbackColor) {
        try {
            return new Texture(Gdx.files.internal(path));
        } catch (Exception e) {
            Gdx.app.error("TEXTURE_LOAD", "No se pudo cargar '" + path + "'. Usando color de fallback.", e);
            return createColoredTexture(fallbackColor);
        }
    }

    // Sobrecarga corregida (solo para evitar errores de compilación, aunque no se
    // usa en el código original)

    private void switchMenu(MenuState newState) {
        if (!isInputEnabled)
            return;

        // Ocultamos todas las tablas de contenido (Main, Options, Play, Solitario)
        mainTable.setVisible(false);
        optionsSubMenuTable.setVisible(false);
        playSubMenuTable.setVisible(false);
        // NO ocultamos solitarioSubMenuTable aquí si queremos que overlay trabaje bien,
        // pero switchMenu suele limpiar todo primero.
        solitarioSubMenuTable.setVisible(false);

        // El título siempre está oculto
        headerTable.setVisible(false);
        // El botón de salir solo se ve en el menú principal.
        footerTable.setVisible(newState == MenuState.MAIN);

        currentState = newState;
        selectedIndex = 0;

        if (currentState == MenuState.MAIN) {
            mainTable.setVisible(true);
            currentButtons = mainMenuButtons;
        } else if (currentState == MenuState.OPTIONS_SUB) {
            optionsSubMenuTable.setVisible(true);
            currentButtons = optionsSubMenuButtons;
        } else if (currentState == MenuState.PLAY_SUB) {
            playSubMenuTable.setVisible(true);
            currentButtons = playSubMenuButtons;
        } else if (currentState == MenuState.SOLITARIO_SUB) {
            // "Encima de la pantalla que ya está" (PLAY_SUB)
            playSubMenuTable.setVisible(true);
            solitarioSubMenuTable.setVisible(true);
            currentButtons = null; // No usamos botones estándar para solitario
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

    private void updateSelectionUI() {
        if (currentState != MenuState.SOLITARIO_SUB && (currentButtons == null || currentButtons.length == 0)) {
            return;
        }

        // Resetear todos los botones a su estado normal
        if (newGameImage != null)
            newGameImage.setDrawable(new TextureRegionDrawable(newGameButtonTexture));
        if (solitaireNewGameImage != null)
            solitaireNewGameImage.setDrawable(new TextureRegionDrawable(newGameButtonTexture));
        if (solitaireContinueGameImage != null)
            solitaireContinueGameImage.setDrawable(new TextureRegionDrawable(continueGameButtonTexture));
        optionsImage.setDrawable(new TextureRegionDrawable(optionsButtonTexture));
        exitImage.setDrawable(new TextureRegionDrawable(exitButtonTexture));

        helpImage.setDrawable(new TextureRegionDrawable(helpButtonTexture));
        aboutImage.setDrawable(new TextureRegionDrawable(aboutButtonTexture));
        backOptionsImage.setDrawable(new TextureRegionDrawable(backButtonTexture));

        singlePlayerImage.setDrawable(new TextureRegionDrawable(singlePlayerButtonTexture));
        multiPlayerImage.setDrawable(new TextureRegionDrawable(multiPlayerButtonTexture));
        backNewGameImage.setDrawable(new TextureRegionDrawable(backButtonTexture));

        // Aplicar textura seleccionada al botón actual
        if (currentState == MenuState.MAIN) {
            if (selectedIndex == 0) {
                newGameImage.setDrawable(new TextureRegionDrawable(newGameButtonSelectedTexture));
            } else if (selectedIndex == 1) {
                optionsImage.setDrawable(new TextureRegionDrawable(optionsButtonSelectedTexture));
            } else if (selectedIndex == 2) {
                exitImage.setDrawable(new TextureRegionDrawable(exitButtonSelectedTexture));
            }
        } else if (currentState == MenuState.OPTIONS_SUB) {
            if (selectedIndex == 0) {
                helpImage.setDrawable(new TextureRegionDrawable(helpButtonSelectedTexture));
            } else if (selectedIndex == 1) {
                aboutImage.setDrawable(new TextureRegionDrawable(aboutButtonSelectedTexture));
            } else if (selectedIndex == 2) {
                backOptionsImage.setDrawable(new TextureRegionDrawable(backButtonSelectedTexture));
            }
        } else if (currentState == MenuState.PLAY_SUB) {
            if (selectedIndex == 0) {
                singlePlayerImage.setDrawable(new TextureRegionDrawable(singlePlayerButtonSelectedTexture));
            } else if (selectedIndex == 1) {
                multiPlayerImage.setDrawable(new TextureRegionDrawable(multiPlayerButtonSelectedTexture));
            } else if (selectedIndex == 2) {
                backNewGameImage.setDrawable(new TextureRegionDrawable(backButtonSelectedTexture));
            }
        } else if (currentState == MenuState.SOLITARIO_SUB) {
            // Reset colors
            solitarioNewGameLabel.setColor(Color.WHITE);
            solitarioContinueGameLabel.setColor(Color.WHITE);
            solitarioBackLabel.setColor(Color.WHITE);

            // Highlight selected
            if (selectedIndex == 0)
                solitarioNewGameLabel.setColor(Color.YELLOW);
            else if (selectedIndex == 1)
                solitarioContinueGameLabel.setColor(Color.YELLOW);
            else if (selectedIndex == 2)
                solitarioBackLabel.setColor(Color.YELLOW);
        }

        // Resetear colores
        if (currentButtons != null) {
            for (int i = 0; i < currentButtons.length; i++) {
                currentButtons[i].setColor(Color.WHITE);
            }
        }
    }

    private void executeAction(int index) {
        if (!isInputEnabled)
            return;

        switch (currentState) {
            case MAIN:
                switch (index) {
                    case 0:
                        switchMenu(MenuState.PLAY_SUB);
                        break; // "Jugar"
                    case 1:
                        switchMenu(MenuState.OPTIONS_SUB);
                        break; // Opciones
                    case 2:
                        Gdx.app.log("MENU", "Saliendo del juego...");
                        Gdx.app.exit();
                        break; // Salir
                }
                break;
            case PLAY_SUB:
                switch (index) {
                    case 0:
                        switchMenu(MenuState.SOLITARIO_SUB);
                        break; // En Solitario
                    case 1:
                        Gdx.app.log("MENU", "Iniciando modo Multijugador.");
                        Gdx.input.setInputProcessor(null);
                        game.setScreen(new ScreenMultiplayer(game));
                        break;
                    case 2:
                        switchMenu(MenuState.MAIN);
                        break; // Volver
                }
                break;
            case SOLITARIO_SUB:
                String username = UserManager.getCurrentUser();
                switch (index) {
                    case 0: // Nuevo Juego
                        if (game.hasSaveData(username)) {
                            Gdx.app.log("MENU", "ADVERTENCIA: Se sobrescribirán los datos.");
                            // Aquí se podría mostrar un diálogo visual, por ahora log y proceder como pide
                            // el usuario
                        }
                        game.clearProgress();
                        Gdx.app.log("MENU", "Iniciando Nuevo Juego.");
                        Gdx.input.setInputProcessor(null);
                        game.setScreen(new ScreenMapaTiled(game, null));
                        break;
                    case 1: // Continuar Juego
                        if (game.hasSaveData(username)) {
                            game.loadProgress(username);
                            Gdx.input.setInputProcessor(null);

                            sodyl.proyecto.clases.PlayerData pData = game.getPlayerData();

                            if (pData != null) {
                                Gdx.app.log("MENU", "Continuando Partida en: " + pData.currentMap + " (" + pData.x + ","
                                        + pData.y + ")");
                                game.setScreen(new ScreenMapaTiled(game, pData.currentMap, null, null, null,
                                        ScreenMapaTiled.GameState.FREE_ROAMING, pData.x, pData.y));
                            } else {
                                // Fallback if data is partial (no position data yet)
                                Gdx.app.log("MENU", "Datos parciales encontrados. Usando inicio por defecto.");
                                game.setScreen(new ScreenMapaTiled(game, "Mapa/MAPACOMPLETO.tmx", null, null, null,
                                        ScreenMapaTiled.GameState.FREE_ROAMING));
                            }
                        } else {
                            Gdx.app.log("MENU", "ERROR: No hay datos guardados.");
                            showError("No se encontraron datos guardados para el usuario: " + username);
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
                        game.setScreen(new ScreenAyuda(game));
                        break;
                    case 1:
                        Gdx.app.log("OPTIONS", "Acerca de.");
                        game.setScreen(new ScreenAcercaDe(game));
                        break;
                    case 2:
                        switchMenu(MenuState.MAIN);
                        break; // Volver
                }
                break;
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        if (!isInputEnabled)
            return false;

        int numItems = getNumItemsInCurrentMenu();
        if (numItems == 0)
            return false;

        if (keycode == Keys.DOWN || keycode == Keys.RIGHT) {
            selectedIndex = (selectedIndex + 1) % numItems;
            updateSelectionUI();
            return true;
        }

        if (keycode == Keys.UP || keycode == Keys.LEFT) {
            selectedIndex = (selectedIndex - 1 + numItems) % numItems;
            updateSelectionUI();
            return true;
        }

        if (keycode == Keys.ENTER || keycode == Keys.Z) {
            executeAction(selectedIndex);
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
                if (selectedIndex != numItems - 1) {
                    selectedIndex = numItems - 1;
                    updateSelectionUI();
                } else {
                    Gdx.app.exit();
                }
                return true;
            }
        }
        return false;
    }

    private int getNumItemsInCurrentMenu() {
        if (currentState == MenuState.SOLITARIO_SUB)
            return 3;
        if (currentButtons != null)
            return currentButtons.length;
        return 0;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 0f, 1);
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        if (fadeTimer < fadeDuration) {
            fadeTimer += delta;

            float alpha = 1.0f - Math.min(1f, fadeTimer / fadeDuration);

            if (alpha > 0.01f) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                stage.getBatch().begin();
                stage.getBatch().setColor(0, 0, 0, alpha);
                stage.getBatch().draw(
                        blackPixelTexture,
                        0, 0,
                        stage.getViewport().getWorldWidth(),
                        stage.getViewport().getWorldHeight());
                stage.getBatch().end();

                stage.getBatch().setColor(Color.WHITE);
                Gdx.gl.glDisable(GL20.GL_BLEND);
            }
        }

        if (fadeTimer >= fadeDuration && !isInputEnabled) {
            isInputEnabled = true;
            InputMultiplexer multiplexer = new InputMultiplexer();
            multiplexer.addProcessor(this); // Para teclado (flechas/enter)
            multiplexer.addProcessor(stage); // Para clicks (botones)
            Gdx.input.setInputProcessor(multiplexer);
        }
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
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
        stage.dispose();
        font.dispose();
        backgroundTexture.dispose();
        titleTexture.dispose();

        // Disponer todas las texturas nuevas y antiguas
        if (newGameButtonTexture != null)
            newGameButtonTexture.dispose();
        if (continueGameButtonTexture != null)
            continueGameButtonTexture.dispose();
        if (optionsButtonTexture != null)
            optionsButtonTexture.dispose();
        if (exitButtonTexture != null)
            exitButtonTexture.dispose();
        if (helpButtonTexture != null)
            helpButtonTexture.dispose();
        if (aboutButtonTexture != null)
            aboutButtonTexture.dispose();
        if (backButtonTexture != null)
            backButtonTexture.dispose();
        if (singlePlayerButtonTexture != null)
            singlePlayerButtonTexture.dispose();
        if (multiPlayerButtonTexture != null)
            multiPlayerButtonTexture.dispose();

        if (buttonDownTexture != null)
            buttonDownTexture.dispose();
        if (blackPixelTexture != null)
            blackPixelTexture.dispose();

        // Disponer texturas seleccionadas
        if (newGameButtonSelectedTexture != null)
            newGameButtonSelectedTexture.dispose();
        if (continueGameButtonSelectedTexture != null)
            continueGameButtonSelectedTexture.dispose();
        if (optionsButtonSelectedTexture != null)
            optionsButtonSelectedTexture.dispose();
        if (exitButtonSelectedTexture != null)
            exitButtonSelectedTexture.dispose();

        // Disponer texturas seleccionadas de sub-menús
        if (helpButtonSelectedTexture != null)
            helpButtonSelectedTexture.dispose();
        if (aboutButtonSelectedTexture != null)
            aboutButtonSelectedTexture.dispose();
        if (backButtonSelectedTexture != null)
            backButtonSelectedTexture.dispose();
        if (singlePlayerButtonSelectedTexture != null)
            singlePlayerButtonSelectedTexture.dispose();
        if (multiPlayerButtonSelectedTexture != null)
            multiPlayerButtonSelectedTexture.dispose();
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
        if (errorLabel != null && errorTable != null) {
            errorLabel.setText(message);
            errorTable.setVisible(true);
            errorTable.toFront();
        } else {
            Gdx.app.log("MENU", "Error al mostrar mensaje visual (componentes nulos): " + message);
        }
    }

    private void closeError() {
        if (errorTable != null)
            errorTable.setVisible(false);
    }
}
