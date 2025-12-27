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
    private Texture statsButtonTexture;
    private Texture statsButtonSelectedTexture;
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
    private Image continueGameImage;
    private Image optionsImage;
    private Image exitImage;

    // Images for sub-menus
    private Image statsImage;
    private Image helpImage;
    private Image aboutImage;
    private Image backOptionsImage;
    private Image singlePlayerImage;
    private Image multiPlayerImage;
    private Image backNewGameImage;

    // --- Estructura de Menú y Navegación ---
    public enum MenuState {
        MAIN, OPTIONS_SUB, NEW_GAME_SUB
    }

    private MenuState currentState = MenuState.MAIN;
    private MenuState startState = MenuState.MAIN;

    private Button[] currentButtons;
    private Button[] mainMenuButtons;
    private Button[] optionsSubMenuButtons;
    private Button[] newGameButtons;

    private Button exitButton;

    private Table mainTable;
    private Table optionsSubMenuTable;
    private Table newGameTable;
    private Table footerTable;
    private Table headerTable;

    private int selectedIndex = 0;

    // --- Variables de Transición (Anti-Flicker) ---
    private final float fadeDuration = 0.5f;
    private float fadeTimer = 0f;
    private Texture blackPixelTexture;
    private boolean isInputEnabled = false;
    private boolean skipInitialFade = false;
    // ---------------------------------------------

    // --- CONSTANTES DE DISEÑO ---
    private final float BUTTON_HEIGHT = 175f;
    private final float BUTTON_WIDTH = 425f;
    private final float VERTICAL_SOLAPAMIENTO = -35f; // Solapamiento para espaciado equidistante
    private final float HEADER_HEIGHT_SPACE = 250f; // Espacio total para el título fijo
    private final float FOOTER_HEIGHT_SPACE = 50f; // Padding final

    // --- Desplazamiento vertical para subir los botones principales (Ajustado a
    // 50f) ---
    private final float MAIN_MENU_OFFSET_Y = -20f;

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
        font = new BitmapFont();

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
        statsButtonTexture = loadTexture("imagenes/estad.png", new Color(0.8f, 0.5f, 0.1f, 0.8f));
        statsButtonSelectedTexture = loadTexture("imagenes/estadS.png", new Color(1f, 0.8f, 0.5f, 1f));
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

        continueGameImage = new Image(continueGameButtonTexture);
        Button continueGameButton = new Button(defaultStyle);
        continueGameButton.add(continueGameImage).grow();

        optionsImage = new Image(optionsButtonTexture);
        Button optionsButton = new Button(defaultStyle);
        optionsButton.add(optionsImage).grow();

        exitImage = new Image(exitButtonTexture);
        exitButton = new Button(defaultStyle);
        exitButton.add(exitImage).grow();

        // Sub Menu (Opciones)
        statsImage = new Image(statsButtonTexture);
        Button statsButton = new Button(defaultStyle);
        statsButton.add(statsImage).grow();

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
        Button backNewGameButton = new Button(defaultStyle);
        backNewGameButton.add(backNewGameImage).grow();

        // --- Definición de Arrays de Botones ---
        mainMenuButtons = new Button[] { newGameButton, optionsButton, exitButton };
        optionsSubMenuButtons = new Button[] { statsButton, helpButton, aboutButton, backOptionsButton };
        newGameButtons = new Button[] { singlePlayerButton, multiPlayerButton, backNewGameButton };

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
        optionsSubMenuTable.top().left();
        optionsSubMenuTable.padTop(100).padLeft(50).row();

        optionsSubMenuTable.add(statsButton).width(mainButtonWidth).height(mainButtonHeight).padBottom(buttonSpacing)
                .left().row();
        optionsSubMenuTable.add(helpButton).width(mainButtonWidth).height(mainButtonHeight).padBottom(buttonSpacing)
                .left().row();
        optionsSubMenuTable.add(aboutButton).width(mainButtonWidth).height(mainButtonHeight).padBottom(buttonSpacing)
                .left().row();
        optionsSubMenuTable.add(backOptionsButton).width(mainButtonWidth).height(mainButtonHeight)
                .padBottom(buttonSpacing).left().row();

        optionsSubMenuTable.add().growY().row();
        optionsSubMenuTable.setVisible(false);
        stage.addActor(optionsSubMenuTable);

        // ----------------------------------------------------------------------
        // Tablas Sub-Menú (Nuevo Juego)
        // ----------------------------------------------------------------------

        newGameTable = new Table();
        newGameTable.setFillParent(true);
        newGameTable.top().left();
        newGameTable.padTop(150).padLeft(50).row();

        newGameTable.add(singlePlayerButton).width(mainButtonWidth).height(mainButtonHeight).padBottom(buttonSpacing)
                .left().row();
        newGameTable.add(multiPlayerButton).width(mainButtonWidth).height(mainButtonHeight).padBottom(buttonSpacing)
                .left().row();
        newGameTable.add(backNewGameButton).width(mainButtonWidth).height(mainButtonHeight).padBottom(buttonSpacing)
                .left().row();

        newGameTable.add().growY().row();
        newGameTable.setVisible(false);
        stage.addActor(newGameTable);

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

        // Submenu New Game Listeners
        for (int i = 0; i < newGameButtons.length; i++) {
            final int index = i;
            newGameButtons[i].addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (isInputEnabled && currentState == MenuState.NEW_GAME_SUB)
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

    // Método de utilidad para crear Button con Image
    private Button createButtonWithImage(Texture texture, ButtonStyle style) {
        Button button = new Button(style);
        button.add(new Image(texture)).grow();
        return button;
    }

    // Sobrecarga corregida (solo para evitar errores de compilación, aunque no se
    // usa en el código original)
    private Button createButtonWithWithImage(Texture texture, ButtonStyle style) {
        Button button = new Button(style);
        button.add(new Image(texture)).grow();
        return button;
    }

    private void switchMenu(MenuState newState) {
        if (!isInputEnabled)
            return;

        // Ocultamos todas las tablas de contenido (Main, Options, New Game)
        mainTable.setVisible(false);
        optionsSubMenuTable.setVisible(false);
        newGameTable.setVisible(false);

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
        } else if (currentState == MenuState.NEW_GAME_SUB) {
            newGameTable.setVisible(true);
            currentButtons = newGameButtons;
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
        if (currentButtons == null || currentButtons.length == 0) {
            return;
        }

        // Resetear todos los botones a su estado normal
        newGameImage.setDrawable(new TextureRegionDrawable(newGameButtonTexture));
        continueGameImage.setDrawable(new TextureRegionDrawable(continueGameButtonTexture));
        optionsImage.setDrawable(new TextureRegionDrawable(optionsButtonTexture));
        exitImage.setDrawable(new TextureRegionDrawable(exitButtonTexture));

        statsImage.setDrawable(new TextureRegionDrawable(statsButtonTexture));
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
                statsImage.setDrawable(new TextureRegionDrawable(statsButtonSelectedTexture));
            } else if (selectedIndex == 1) {
                helpImage.setDrawable(new TextureRegionDrawable(helpButtonSelectedTexture));
            } else if (selectedIndex == 2) {
                aboutImage.setDrawable(new TextureRegionDrawable(aboutButtonSelectedTexture));
            } else if (selectedIndex == 3) {
                backOptionsImage.setDrawable(new TextureRegionDrawable(backButtonSelectedTexture));
            }
        } else if (currentState == MenuState.NEW_GAME_SUB) {
            if (selectedIndex == 0) {
                singlePlayerImage.setDrawable(new TextureRegionDrawable(singlePlayerButtonSelectedTexture));
            } else if (selectedIndex == 1) {
                multiPlayerImage.setDrawable(new TextureRegionDrawable(multiPlayerButtonSelectedTexture));
            } else if (selectedIndex == 2) {
                backNewGameImage.setDrawable(new TextureRegionDrawable(backButtonSelectedTexture));
            }
        }

        // Resetear colores
        for (int i = 0; i < currentButtons.length; i++) {
            currentButtons[i].setColor(Color.WHITE);
        }
    }

    private void executeAction(int index) {
        if (!isInputEnabled)
            return;

        switch (currentState) {
            case MAIN:
                switch (index) {
                    case 0:
                        switchMenu(MenuState.NEW_GAME_SUB);
                        break; // "Jugar" (Ex Nuevo Juego)
                    case 1:
                        switchMenu(MenuState.OPTIONS_SUB);
                        break; // Opciones
                    case 2:
                        Gdx.app.log("MENU", "Saliendo del juego...");
                        Gdx.app.exit();
                        break; // Salir
                }
                break;
            case NEW_GAME_SUB:
                switch (index) {
                    case 0:
                        Gdx.app.log("MENU", "Transicionando a ScreenMapaTiled (Solitario).");
                        Gdx.input.setInputProcessor(null);
                        // Ir directamente al mapa (sin Pokémon inicial)
                        game.setScreen(new ScreenMapaTiled(game, null));
                        break;
                    case 1:
                        Gdx.app.log("MENU", "Multijugador seleccionado (teclado).");
                        break;
                    case 2:
                        switchMenu(MenuState.MAIN);
                        break; // Volver
                }
                break;
            case OPTIONS_SUB:
                switch (index) {
                    case 0:
                        Gdx.app.log("OPTIONS", "Ver Estadísticas.");
                        game.setScreen(new ScreenEstadisticas(game));
                        break;
                    case 1:
                        Gdx.app.log("OPTIONS", "Ayuda.");
                        game.setScreen(new ScreenAyuda(game));
                        break;
                    case 2:
                        Gdx.app.log("OPTIONS", "Acerca de.");
                        game.setScreen(new ScreenAcercaDe(game));
                        break;
                    case 3:
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

        if (keycode == Keys.DOWN) {
            selectedIndex = (selectedIndex + 1) % currentButtons.length;
            updateSelectionUI();
            return true;
        }

        if (keycode == Keys.UP) {
            selectedIndex = (selectedIndex - 1 + currentButtons.length) % currentButtons.length;
            updateSelectionUI();
            return true;
        }

        if (keycode == Keys.ENTER) {
            executeAction(selectedIndex);
            return true;
        }

        if (keycode == Keys.ESCAPE) {
            if (currentState == MenuState.OPTIONS_SUB || currentState == MenuState.NEW_GAME_SUB) {
                switchMenu(MenuState.MAIN);
                return true;
            } else if (currentState == MenuState.MAIN) {
                if (selectedIndex != currentButtons.length - 1) {
                    selectedIndex = currentButtons.length - 1;
                    updateSelectionUI();
                } else {
                    Gdx.app.exit();
                }
                return true;
            }
        }
        return false;
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
        if (statsButtonTexture != null)
            statsButtonTexture.dispose();
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
        if (statsButtonSelectedTexture != null)
            statsButtonSelectedTexture.dispose();
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
}
