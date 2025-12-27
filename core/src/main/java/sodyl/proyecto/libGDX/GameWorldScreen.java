package sodyl.proyecto.libGDX;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import static com.badlogic.gdx.Input.Keys;

public class GameWorldScreen implements Screen, InputProcessor { // CLASE RENOMBRADA

    private final Proyecto game;
    private Stage stage;
    private Stage uiStage;
    private OrthographicCamera camera;

    // --- Pokémon Inicial ---
    private final String initialPokemonName; // Nuevo campo para almacenar el nombre del Pokémon

    // --- Variables de Renderizado y Mapa ---
    private SpriteBatch batch;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private static final float UNIT_SCALE = 1 / 8f; // Asegúrate de que esta constante sea correcta

    // --- Variables de Animación y Personaje ---
    private TextureAtlas atlas;
    private float stateTime;
    private Animation<TextureRegion> walkDownAnimation;
    private Animation<TextureRegion> walkUpAnimation;
    private Animation<TextureRegion> walkLeftAnimation;
    private Animation<TextureRegion> walkRightAnimation;

    private enum Direction {
        DOWN, UP, LEFT, RIGHT
    }

    private Direction lastDirection = Direction.DOWN;
    private Image characterActor;
    private static final float SPEED = 40f;
    private boolean movingUp, movingDown, movingLeft, movingRight;

    // --- VARIABLES DE DIÁLOGO Y ESTADO DE JUEGO ---
    private BitmapFont font;

    private enum GameState {
        FREE_ROAMING,
        PAUSED,
        TRANSITIONING
    }

    private GameState currentState = GameState.FREE_ROAMING;

    // --- VARIABLES DEL MENÚ DE PAUSA ---
    private Table pauseMenuTable;
    private TextButton[] pauseMenuButtons;
    private int selectedIndex = 0;

    // --- VARIABLES DE TRANSICIÓN DE VUELTA AL MENÚ PRINCIPAL ---
    private float transitionTimer = 0f;
    private final float transitionDuration = 0.5f;
    private Texture blackPixelTexture;

    // CONSTRUCTOR ACTUALIZADO: Recibe el nombre del Pokémon
    public GameWorldScreen(Proyecto game, String initialPokemonName) {
        this.game = game;
        this.initialPokemonName = initialPokemonName;
        Gdx.app.log("GAME_START", "Juego iniciado con Pokémon: " + this.initialPokemonName);
    }

    private Animation<TextureRegion> createAnimation(String rowPrefix, float frameDuration) {
        Array<TextureAtlas.AtlasRegion> frames = new Array<>();
        // Asumiendo que esta lógica es para tu Player_Sprites
        String baseName = "Player_Sprites_" + rowPrefix + "_c";
        String suffix = "_processed_by_imagy";

        for (int i = 1; i <= 4; i++) {
            String regionName = baseName + i + suffix;
            TextureAtlas.AtlasRegion region = atlas.findRegion(regionName);
            if (region != null) {
                frames.add(region);
            } else {
                if (i == 1) {
                    Gdx.app.error("ANIMATION", "Error: No se encontró el frame inicial: " + regionName);
                }
                break;
            }
        }

        if (frames.size == 0) {
            Gdx.app.error("ANIMATION", "Error: No se encontraron frames para la fila: " + rowPrefix);
            return new Animation<>(frameDuration, new TextureRegion());
        }

        return new Animation<>(frameDuration, frames, Animation.PlayMode.LOOP);
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();

        // Cargar el Mapa Tiled
        try {
            map = new TmxMapLoader().load("Mapa/Mapa1.tmx");
            renderer = new OrthogonalTiledMapRenderer(map, UNIT_SCALE);
        } catch (Exception e) {
            Gdx.app.error("MAPA", "Error al cargar el mapa Tiled. Verifica la ruta o el archivo .tmx.", e);
        }

        // --- Configuración de Animaciones y Personaje ---
        TextureRegion initialFrame;
        try {
            atlas = new TextureAtlas(Gdx.files.internal("spritesPack/Textures.atlas"));
            float frameDuration = 0.15f;
            walkDownAnimation = createAnimation("r1", frameDuration);
            walkLeftAnimation = createAnimation("r2", frameDuration);
            walkRightAnimation = createAnimation("r3", frameDuration);
            walkUpAnimation = createAnimation("r4", frameDuration);
            initialFrame = walkDownAnimation.getKeyFrame(0);
        } catch (Exception e) {
            Gdx.app.error("ATLAS", "Error al cargar TextureAtlas. Usando respaldo rojo.", e);
            Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.RED);
            pixmap.fill();
            Texture errorTexture = new Texture(pixmap);
            pixmap.dispose();
            initialFrame = new TextureRegion(errorTexture);
        }

        // Textura negra 1x1 para el overlay de la transición
        Pixmap pixmapOverlay = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmapOverlay.setColor(Color.BLACK);
        pixmapOverlay.fill();
        blackPixelTexture = new Texture(pixmapOverlay);
        pixmapOverlay.dispose();

        // --- INICIALIZACIÓN DE LA FUENTE ---
        font = new BitmapFont();
        font.getData().setScale(1.5f);

        characterActor = new Image(initialFrame);
        characterActor.setSize(2f, 2f);

        // Posicionamiento inicial
        float startX = 11f;
        float startY = 38f;

        characterActor.setPosition(startX, startY);

        // Stage y Viewport del MUNDO (sigue la cámara)
        // Se asume que Proyecto.PANTALLA_W y Proyecto.PANTALLA_H son valores definidos
        stage = new Stage(new FitViewport(Proyecto.PANTALLA_W * UNIT_SCALE, Proyecto.PANTALLA_H * UNIT_SCALE, camera));
        stage.addActor(characterActor);

        // Stage y Viewport de la UI (coordenadas de pantalla fija)
        uiStage = new Stage(new ScreenViewport());

        // --- INICIALIZAR MENÚ DE PAUSA ---
        initializePauseMenu();

        // Input Multiplexer: Procesamos la UI primero (para botones), luego el teclado
        // del mundo
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(this); // InputProcessor para el movimiento y ESC
        multiplexer.addProcessor(uiStage); // InputProcessor para los botones de la UI
        Gdx.input.setInputProcessor(multiplexer);

        // Ajustar la cámara inicial
        camera.setToOrtho(false, Proyecto.PANTALLA_W * UNIT_SCALE, Proyecto.PANTALLA_H * UNIT_SCALE);
        camera.viewportWidth = 30; // Valores típicos para un zoom apropiado
        camera.viewportHeight = 25;
        camera.update();
    }

    private void initializePauseMenu() {
        TextButtonStyle buttonStyle = new TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.downFontColor = Color.YELLOW;

        // Texturas para el estilo del botón
        Texture buttonUpTexture = createColoredTexture(new Color(0.2f, 0.2f, 0.2f, 0.7f));
        Texture buttonDownTexture = createColoredTexture(new Color(0.1f, 0.5f, 0.8f, 0.9f));

        buttonStyle.up = new TextureRegionDrawable(buttonUpTexture);
        buttonStyle.down = new TextureRegionDrawable(buttonDownTexture);

        TextButton inventoryButton = new TextButton("Inventario", buttonStyle);
        TextButton pokedexButton = new TextButton("Pokédex", buttonStyle);
        TextButton saveButton = new TextButton("Guardar Partida", buttonStyle);
        TextButton mainMenuButton = new TextButton("Menú Principal", buttonStyle);
        TextButton backButton = new TextButton("Volver (ESC)", buttonStyle);

        pauseMenuButtons = new TextButton[] {
                inventoryButton,
                pokedexButton,
                saveButton,
                mainMenuButton,
                backButton
        };

        pauseMenuTable = new Table();
        pauseMenuTable.setFillParent(true);
        pauseMenuTable.center();

        pauseMenuTable.add(new TextButton("JUEGO PAUSADO", buttonStyle)).width(300).height(80).pad(20).row();

        // Añadir botones al menú
        pauseMenuTable.add(inventoryButton).width(250).height(60).pad(10).row();
        pauseMenuTable.add(pokedexButton).width(250).height(60).pad(10).row();
        pauseMenuTable.add(saveButton).width(250).height(60).pad(10).row();
        pauseMenuTable.add(mainMenuButton).width(250).height(60).pad(10).row();
        pauseMenuTable.add(backButton).width(250).height(60).pad(10).row();

        pauseMenuTable.setVisible(false);
        uiStage.addActor(pauseMenuTable);

        // Listeners para las acciones de los botones
        mainMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (currentState == GameState.PAUSED) {
                    startTransitionToMainMenu();
                }
            }
        });

        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (currentState == GameState.PAUSED) {
                    togglePauseMenu();
                }
            }
        });

        inventoryButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (currentState == GameState.PAUSED)
                    Gdx.app.log("MENU", "Inventario abierto (Pendiente)");
            }
        });

        // Inicializar la selección
        updatePauseMenuSelection(0);
    }

    private Texture createColoredTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void togglePauseMenu() {
        if (currentState == GameState.FREE_ROAMING) {
            currentState = GameState.PAUSED;
            pauseMenuTable.setVisible(true);
            selectedIndex = 0;
            updatePauseMenuSelection(0);
        } else if (currentState == GameState.PAUSED) {
            currentState = GameState.FREE_ROAMING;
            pauseMenuTable.setVisible(false);
        }
    }

    private void updatePauseMenuSelection(int newIndex) {
        if (pauseMenuButtons.length == 0)
            return;

        pauseMenuButtons[selectedIndex].getLabel().setColor(Color.WHITE);

        selectedIndex = (newIndex + pauseMenuButtons.length) % pauseMenuButtons.length;
        pauseMenuButtons[selectedIndex].getLabel().setColor(Color.YELLOW);
    }

    private void executePauseMenuAction(int index) {
        if (currentState != GameState.PAUSED)
            return;

        switch (index) {
            case 0:
                Gdx.app.log("MENU", "Inventario abierto (Pendiente)");
                break;
            case 1:
                Gdx.app.log("MENU", "Pokédex abierto (Pendiente)");
                break;
            case 2:
                Gdx.app.log("MENU", "Partida guardada (Pendiente)");
                break;
            case 3:
                startTransitionToMainMenu(); // Menú Principal
                break;
            case 4:
                togglePauseMenu(); // Volver (ESC)
                break;
        }
    }

    private void startTransitionToMainMenu() {
        currentState = GameState.TRANSITIONING;
        transitionTimer = 0f;
        Gdx.input.setInputProcessor(null); // Bloquea todo input
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        if (currentState == GameState.FREE_ROAMING) {
            handleMovement(delta);
        }

        stateTime += delta;
        updateCharacterAnimation();

        // 1. Centrar la cámara en el personaje
        camera.position.x = characterActor.getX() + characterActor.getWidth() / 2;
        camera.position.y = characterActor.getY() + characterActor.getHeight() / 2;
        camera.update();

        // 2. Renderizar Mapa
        if (renderer != null) {
            renderer.setView(camera);
            renderer.render();
        }

        // 3. Renderizar Personaje y objetos del mundo
        stage.act(delta);
        stage.draw();

        // 4. Renderizar UI (Menú de Pausa)
        uiStage.act(delta);
        uiStage.draw();

        // 5. Lógica de Transición de VUELTA AL MENÚ
        if (currentState == GameState.TRANSITIONING) {
            transitionTimer += delta;
            float alpha = Math.min(1f, transitionTimer / transitionDuration); // Alpha va de 0 a 1

            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            batch.begin();
            batch.setColor(0, 0, 0, alpha); // Color negro con transparencia
            batch.draw(
                    blackPixelTexture,
                    0, 0,
                    uiStage.getViewport().getWorldWidth(), // Cubre toda la pantalla de la UI
                    uiStage.getViewport().getWorldHeight());
            batch.end();

            batch.setColor(Color.WHITE);
            Gdx.gl.glDisable(GL20.GL_BLEND);

            if (alpha >= 1.0f) {
                // Transición completada: Cambiar de pantalla
                game.setScreen(new MenuPrincipal(game, true)); // Suponiendo que MenuPrincipal existe
                return;
            }
        }
    }

    private void updateCharacterAnimation() {
        TextureRegion currentFrame;
        boolean isMoving = (movingUp || movingDown || movingLeft || movingRight)
                && currentState == GameState.FREE_ROAMING;

        if (isMoving) {
            switch (lastDirection) {
                case UP:
                    currentFrame = walkUpAnimation.getKeyFrame(stateTime, true);
                    break;
                case DOWN:
                    currentFrame = walkDownAnimation.getKeyFrame(stateTime, true);
                    break;
                case LEFT:
                    currentFrame = walkLeftAnimation.getKeyFrame(stateTime, true);
                    break;
                case RIGHT:
                    currentFrame = walkRightAnimation.getKeyFrame(stateTime, true);
                    break;
                default:
                    currentFrame = walkDownAnimation.getKeyFrame(stateTime, true);
                    break;
            }
        } else {
            // Frame estático (primer frame de la animación de la última dirección)
            switch (lastDirection) {
                case UP:
                    currentFrame = walkUpAnimation.getKeyFrame(0);
                    break;
                case DOWN:
                    currentFrame = walkDownAnimation.getKeyFrame(0);
                    break;
                case LEFT:
                    currentFrame = walkLeftAnimation.getKeyFrame(0);
                    break;
                case RIGHT:
                    currentFrame = walkRightAnimation.getKeyFrame(0);
                    break;
                default:
                    currentFrame = walkDownAnimation.getKeyFrame(0);
                    break;
            }
        }
        ((Image) characterActor).setDrawable(new TextureRegionDrawable(currentFrame));
    }

    private void handleMovement(float delta) {
        if (currentState != GameState.FREE_ROAMING)
            return;

        float movement = SPEED * delta * UNIT_SCALE;
        float newX = characterActor.getX();
        float newY = characterActor.getY();

        // El movimiento diagonal no está implementado aquí, solo cardinal
        if (movingUp) {
            newY += movement;
            lastDirection = Direction.UP;
        } else if (movingDown) {
            newY -= movement;
            lastDirection = Direction.DOWN;
        } else if (movingLeft) {
            newX -= movement;
            lastDirection = Direction.LEFT;
        } else if (movingRight) {
            newX += movement;
            lastDirection = Direction.RIGHT;
        }

        characterActor.setPosition(newX, newY);
    }

    // --- Implementación de InputProcessor para Teclas ---

    @Override
    public boolean keyDown(int keycode) {
        if (currentState == GameState.TRANSITIONING)
            return false;

        // Manejo de PAUSA / DESPAUSA (ESC)
        if (keycode == Keys.ESCAPE) {
            if (currentState == GameState.FREE_ROAMING || currentState == GameState.PAUSED) {
                togglePauseMenu();
                return true;
            }
        }

        // Manejo del MENÚ DE PAUSA
        if (currentState == GameState.PAUSED) {
            if (keycode == Keys.DOWN) {
                updatePauseMenuSelection(selectedIndex + 1);
                return true;
            }
            if (keycode == Keys.UP) {
                updatePauseMenuSelection(selectedIndex - 1);
                return true;
            }
            if (keycode == Keys.ENTER) {
                executePauseMenuAction(selectedIndex);
                return true;
            }
            return true; // Bloquear movimiento del mundo si estamos en el menú
        }

        // Manejo de MOVIMIENTO (solo si FREE_ROAMING)
        if (currentState == GameState.FREE_ROAMING) {
            if (keycode == Keys.W || keycode == Keys.UP) {
                movingUp = true;
                return true;
            }
            if (keycode == Keys.S || keycode == Keys.DOWN) {
                movingDown = true;
                return true;
            }
            if (keycode == Keys.A || keycode == Keys.LEFT) {
                movingLeft = true;
                return true;
            }
            if (keycode == Keys.D || keycode == Keys.RIGHT) {
                movingRight = true;
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (currentState != GameState.FREE_ROAMING)
            return false;

        if (keycode == Keys.W || keycode == Keys.UP) {
            movingUp = false;
            return true;
        }
        if (keycode == Keys.S || keycode == Keys.DOWN) {
            movingDown = false;
            return true;
        }
        if (keycode == Keys.A || keycode == Keys.LEFT) {
            movingLeft = false;
            return true;
        }
        if (keycode == Keys.D || keycode == Keys.RIGHT) {
            movingRight = false;
            return true;
        }
        return false;
    }

    // --- Métodos de Screen ---

    @Override
    public void resize(int width, int height) {
        // Redimensionar el Stage del Mundo
        stage.getViewport().update(width, height, false);

        // La cámara debe actualizar sus dimensiones, pero mantengo el viewport fijo
        // (30x25 unidades)
        camera.setToOrtho(false, width * UNIT_SCALE, height * UNIT_SCALE);
        camera.viewportWidth = 30;
        camera.viewportHeight = 25;
        camera.update();

        // Redimensionar el Stage de la UI
        uiStage.getViewport().update(width, height, true);
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
        if (batch != null)
            batch.dispose();
        if (stage != null)
            stage.dispose();
        if (uiStage != null)
            uiStage.dispose();
        if (map != null)
            map.dispose();
        if (renderer != null)
            renderer.dispose();
        if (atlas != null)
            atlas.dispose();
        if (font != null)
            font.dispose();
        if (blackPixelTexture != null)
            blackPixelTexture.dispose();
    }

    // Métodos no utilizados de InputProcessor
    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Devolvemos false para permitir que uiStage procese el evento si estamos en
        // pausa
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
