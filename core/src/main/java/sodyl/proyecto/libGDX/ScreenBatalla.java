package sodyl.proyecto.libGDX;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.Input.Keys;
import sodyl.proyecto.clases.Batalla;
import sodyl.proyecto.clases.Pokemon;
import sodyl.proyecto.clases.Inventario;
import sodyl.proyecto.clases.Objeto;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Pantalla de batalla Pokémon - Estilo Esmeralda
 */
public class ScreenBatalla implements Screen, InputProcessor {
    private final Proyecto game;
    private final ScreenMapaTiled previousScreen;
    private final Batalla batalla;
    private final Inventario inventario;

    private Stage stage;
    private SpriteBatch batch;
    private BitmapFont font;

    private Texture backgroundTexture;
    private Texture battleBoxTexture;
    private Texture selectionTexture;

    private String mapPath;
    private String customBackgroundPath;

    // Battle Assets
    private Texture pokemonPlayerTexture;
    private Texture pokemonEnemyTexture;

    // Animation
    private Animation<TextureRegion> playerAnimation;
    private float playerAnimationTimer = 0f;
    private Animation<TextureRegion> enemyAnimation;
    private float enemyAnimationTimer = 0f;

    private Runnable onAttackAnimFinished;
    private boolean isAttackAnimActive = false;

    // Flash Effect Variables (for healing and capture)
    private boolean isFlashActive = false;
    private float flashTimer = 0f;
    private Color flashColor = Color.WHITE;
    private int flashCount = 0;
    private final int MAX_FLASHES = 2;
    private final float FLASH_DURATION = 0.15f;
    private Runnable onFlashComplete = null;

    // --- UI Elements ---
    private Table rootTable;
    private Table enemyInfoTable;
    private Table playerInfoTable;
    private Table bottomMenuTable;

    // Labels
    private Label enemyNameLabel;
    private Label enemyHPLabel;
    private Label playerNameLabel;
    private Label playerHPLabel;
    private Label messageLabel; // Texto principal (izquierda abajo)

    // Menu Options Labels
    private Label fightLabel, bagLabel, pokemonLabel, runLabel;

    // --- State Machine ---
    // UNIDAD 9: PATRONES DE DISEÑO - STATE (Estado)
    // El uso de un Enum para controlar el flujo de la batalla es una implementación
    // simplificada del patrón State, donde el comportamiento de la pantalla
    // (input/render)
    // cambia totalmente según el estado actual.
    private enum BattleState {
        INTRO,
        MAIN_MENU,
        FIGHT_MENU,
        BAG_MENU,
        POKEMON_MENU,
        MESSAGE_WAIT,
        ENEMY_TURN,
        BATTLE_END,
        BAG_CAPTURE_MENU,
        BAG_HEAL_MENU
    }

    private BattleState currentState = BattleState.INTRO;

    // Selection Indices
    private int mainMenuIndex = 0; // 0:Fight, 1:Bag, 2:Pokemon, 3:Run
    private int fightMenuIndex = 0; // 0:Move1, 1:Move2, 2:Cancel
    private int bagMenuIndex = 0; // 0:Capture, 1:Heal, 2:PowerUp, 3:Cancel
    private int bagCaptureMenuIndex = 0; // 0:Pokeball, 1:Masterball, 2:Cancel
    private int bagHealMenuIndex = 0; // 0:Potion, 1:SuperPotion, 2:Cancel
    private int pokemonMenuIndex = 0; // Index for team selection

    private float animationTimer = 0f;
    // Logic Variables
    private float stateTimer = 0f;
    private String pendingMessage = "";
    private Runnable onMessageFinished = null; // Callback para cuando termina un mensaje

    private int switchesUsed = 0;
    private final int MAX_SWITCHES = 2;

    private boolean isTutorial = false;
    private boolean isFinalBattle = false;
    private boolean isNPCBattle = false;

    // Temporal para ver tamaños
    private java.util.List<String> debugPokemonList;
    private int debugIndex = 0;
    private int debugPlayerIndex = 0;

    public ScreenBatalla(Proyecto game, ScreenMapaTiled previousScreen, Pokemon pokemonJugador, Pokemon pokemonEnemigo,
            Inventario inventario) {
        this(game, previousScreen, pokemonJugador, pokemonEnemigo, inventario, false, false, "Mapa/MAPACOMPLETO.tmx",
                false);
    }

    public ScreenBatalla(Proyecto game, ScreenMapaTiled previousScreen, Pokemon pokemonJugador, Pokemon pokemonEnemigo,
            Inventario inventario, boolean isTutorial) {
        this(game, previousScreen, pokemonJugador, pokemonEnemigo, inventario, isTutorial, false,
                "Mapa/MAPACOMPLETO.tmx",
                false);
    }

    public ScreenBatalla(Proyecto game, ScreenMapaTiled previousScreen, Pokemon pokemonJugador, Pokemon pokemonEnemigo,
            Inventario inventario, boolean isTutorial, boolean isFinalBattle) {
        this(game, previousScreen, pokemonJugador, pokemonEnemigo, inventario, isTutorial, isFinalBattle,
                "Mapa/MAPACOMPLETO.tmx", false);
    }

    public ScreenBatalla(Proyecto game, ScreenMapaTiled previousScreen, Pokemon pokemonJugador, Pokemon pokemonEnemigo,
            Inventario inventario, boolean isTutorial, boolean isFinalBattle, String mapPath) {
        this(game, previousScreen, pokemonJugador, pokemonEnemigo, inventario, isTutorial, isFinalBattle, mapPath,
                null, false);
    }

    public ScreenBatalla(Proyecto game, ScreenMapaTiled previousScreen, Pokemon pokemonJugador, Pokemon pokemonEnemigo,
            Inventario inventario, boolean isTutorial, boolean isFinalBattle, String mapPath, boolean isNPCBattle) {
        this(game, previousScreen, pokemonJugador, pokemonEnemigo, inventario, isTutorial, isFinalBattle, mapPath,
                null, isNPCBattle);
    }

    public ScreenBatalla(Proyecto game, ScreenMapaTiled previousScreen, Pokemon pokemonJugador, Pokemon pokemonEnemigo,
            Inventario inventario, boolean isTutorial, boolean isFinalBattle, String mapPath,
            String customBackgroundPath) {
        this(game, previousScreen, pokemonJugador, pokemonEnemigo, inventario, isTutorial, isFinalBattle, mapPath,
                customBackgroundPath, false);
    }

    // UNIDAD 3: SOBRE CARGA DE CONSTRUCTORES (Constructor Overloading)
    // Tenemos múltiples versiones del mismo constructor con diferentes parámetros.
    // Esto permite crear la pantalla de batalla con diferentes configuraciones
    // (si es tutorial, si es batalla final, etc.), demostrando flexibilidad.
    public ScreenBatalla(Proyecto game, ScreenMapaTiled previousScreen, Pokemon pokemonJugador, Pokemon pokemonEnemigo,
            Inventario inventario, boolean isTutorial, boolean isFinalBattle, String mapPath,
            String customBackgroundPath, boolean isNPCBattle) {
        this.game = game;
        this.previousScreen = previousScreen;
        this.batalla = new Batalla(pokemonJugador, pokemonEnemigo, inventario);
        this.inventario = inventario;
        this.isTutorial = isTutorial;
        this.isFinalBattle = isFinalBattle;
        this.mapPath = mapPath;
        this.customBackgroundPath = customBackgroundPath;
        this.isNPCBattle = isNPCBattle;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        // Cargar fuente personalizada "Donuts Chocolate"
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("Mapa/ari-w9500-bold.ttf"));

        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = 20; // Tamaño ajustado para dialogos
        parameter.color = Color.WHITE;
        // Incluir caracteres en español (tildes y ñ)
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "áéíóúÁÉÍÓÚñÑ¿¡";

        font = generator.generateFont(parameter);
        generator.dispose(); // Liberar el generador después de crear la fuente

        stage = new Stage(new FitViewport(Proyecto.PANTALLA_W, Proyecto.PANTALLA_H));

        createTextures();
        createUI();

        // Mensaje inicial
        showMessage("¡Un " + batalla.getPokemonEnemigo().getEspecie() + " salvaje apareció!", () -> {
            currentState = BattleState.MAIN_MENU;
            updateMenuVisuals();
        });

        Gdx.input.setInputProcessor(this);
    }

    private void createTextures() {
        // 1. Background Logic
        try {
            String bgPath = "imagenes/fondoBatalla.jpg"; // Default

            if (isFinalBattle) {
                bgPath = "Mapa/fondoRocoso1.png";
            } else if (customBackgroundPath != null) {
                // Custom background takes precedence over mapPath logic
                bgPath = customBackgroundPath;
            } else if (mapPath != null) {
                if (mapPath.contains("MAPACOMPLETO")) {
                    bgPath = "Mapa/fondoBatalla.jpg";
                }
            }

            // Fix double extension if user fixed it, or try both
            if (!Gdx.files.internal(bgPath).exists()) {
                if (bgPath.endsWith(".png.png")) {
                    bgPath = bgPath.replace(".png.png", ".png");
                }
            }

            backgroundTexture = new Texture(Gdx.files.internal(bgPath));
        } catch (Exception e) {
            Gdx.app.error("ScreenBatalla", "Fondo no encontrado: " + e.getMessage());
            backgroundTexture = createPlaceholderTexture(new Color(0.2f, 0.3f, 0.5f, 1f));
        }

        // 2. Pokemon Textures
        // Player (Back)
        loadSprite(batalla.getPokemonJugador().getSpriteBack(), true);

        // Enemy (Front)
        loadSprite(batalla.getPokemonEnemigo().getSpriteFront(), false);

        // UI Textures
        battleBoxTexture = createPlaceholderTexture(new Color(0.1f, 0.1f, 0.1f, 0.9f));
        selectionTexture = createPlaceholderTexture(new Color(1f, 1f, 0f, 0.3f));

        // 3. Attack Animations - REMOVED per user request
        // Replaced with Blink Effect

    }

    private Texture createPlaceholderTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void createUI() {
        LabelStyle style = new LabelStyle(font, Color.WHITE);
        LabelStyle titleStyle = new LabelStyle(font, Color.YELLOW);

        rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        // --- 1. TOP AREA (Enemy Info) ---
        // Top-Left alignment
        Table topArea = new Table();

        enemyInfoTable = new Table();
        enemyInfoTable.setBackground(new TextureRegionDrawable(battleBoxTexture));
        enemyInfoTable.pad(15);

        enemyNameLabel = new Label("", titleStyle);
        enemyHPLabel = new Label("", style);

        enemyInfoTable.add(enemyNameLabel).left().row();
        enemyInfoTable.add(enemyHPLabel).left().padTop(5);

        topArea.add(enemyInfoTable).left().top().padLeft(50).padTop(30);
        topArea.add().expandX(); // Push to left

        rootTable.add(topArea).expandX().fillX().top().height(Proyecto.PANTALLA_H * 0.3f).row();

        // --- 2. MIDDLE AREA (Battle Scene Placeholder) ---
        rootTable.add().expand().fill().row();

        // --- 3. BOTTOM AREA (Player Info + Menu) ---
        Table bottomArea = new Table();

        // 3a. Player Info (Floating above menu, Right side)
        playerInfoTable = new Table();
        playerInfoTable.setBackground(new TextureRegionDrawable(battleBoxTexture));
        playerInfoTable.pad(15);

        playerNameLabel = new Label("", titleStyle);
        playerHPLabel = new Label("", style);

        playerInfoTable.add(playerNameLabel).right().row();
        playerInfoTable.add(playerHPLabel).right().padTop(5);

        // Add Player Info to a container that aligns it Bottom-Right
        Table playerInfoContainer = new Table();
        playerInfoContainer.add().expandX(); // Push right
        playerInfoContainer.add(playerInfoTable).right().padRight(50).padBottom(10);

        bottomArea.add(playerInfoContainer).expandX().fillX().bottom().row();

        // 3b. Main Menu / Text Box (Fixed Height at bottom)
        bottomMenuTable = new Table();
        bottomMenuTable.setBackground(new TextureRegionDrawable(battleBoxTexture));

        // Layout: Left side (Message/Prompt), Right side (Options Grid)
        // Split 60% / 40%

        // Left Side: Message
        messageLabel = new Label("", style);
        messageLabel.setWrap(true);
        bottomMenuTable.add(messageLabel).width(Proyecto.PANTALLA_W * 0.6f).pad(30).left().top().expandY();

        // Right Side: Options Grid
        Table optionsTable = new Table();
        optionsTable.pad(20);

        fightLabel = new Label("LUCHAR", style);
        bagLabel = new Label("MOCHILA", style);
        pokemonLabel = new Label("POKEMON", style);
        runLabel = new Label("HUIR", style);

        optionsTable.add(fightLabel).pad(15).left();
        optionsTable.add(bagLabel).pad(15).left().row();
        optionsTable.add(pokemonLabel).pad(15).left();
        optionsTable.add(runLabel).pad(15).left();

        bottomMenuTable.add(optionsTable).width(Proyecto.PANTALLA_W * 0.4f).expandY().fill();

        bottomArea.add(bottomMenuTable).expandX().fillX().height(200); // Altura fija para el menú

        rootTable.add(bottomArea).expandX().fillX().bottom();

        updateInfoLabels();
    }

    private void updateInfoLabels() {
        Pokemon enemy = batalla.getPokemonEnemigo();
        Pokemon player = batalla.getPokemonJugador();

        enemyNameLabel.setText(enemy.getEspecie() + " Nv." + enemy.getNivel());
        enemyHPLabel.setText("HP: " + enemy.getActualHP() + "/" + enemy.getMaxHp());

        playerNameLabel.setText(player.getEspecie() + " Nv." + player.getNivel());
        playerHPLabel.setText("HP: " + player.getActualHP() + "/" + player.getMaxHp());
    }

    private void updateMenuVisuals() {
        // Reset all colors
        fightLabel.setColor(Color.GRAY);
        bagLabel.setColor(Color.GRAY);
        pokemonLabel.setColor(Color.GRAY);
        runLabel.setColor(Color.GRAY);

        // Hide/Show logic based on state
        Table optionsTable = (Table) bottomMenuTable.getChildren().get(1);

        if (currentState == BattleState.MAIN_MENU) {
            optionsTable.setVisible(true);
            messageLabel.setText("¿Qué hará " + batalla.getPokemonJugador().getEspecie() + "?");

            switch (mainMenuIndex) {
                case 0:
                    fightLabel.setColor(Color.RED);
                    break;
                case 1:
                    bagLabel.setColor(Color.YELLOW);
                    break;
                case 2:
                    pokemonLabel.setColor(Color.GREEN);
                    break;
                case 3:
                    runLabel.setColor(Color.CYAN);
                    break;
            }

            // Restore original text
            fightLabel.setText("LUCHAR");
            bagLabel.setText("MOCHILA");
            pokemonLabel.setText("POKEMON");
            runLabel.setText("HUIR");

        } else if (currentState == BattleState.FIGHT_MENU) {
            optionsTable.setVisible(true);
            messageLabel.setText("Elige un ataque:");

            Pokemon p = batalla.getPokemonJugador();
            fightLabel.setText(p.getMovimiento1().nombre);
            bagLabel.setText(p.getMovimiento2().nombre);
            pokemonLabel.setText("---");
            runLabel.setText("ATRÁS");

            fightLabel.setColor(fightMenuIndex == 0 ? Color.RED : Color.GRAY);
            bagLabel.setColor(fightMenuIndex == 1 ? Color.RED : Color.GRAY);
            runLabel.setColor(fightMenuIndex == 3 ? Color.YELLOW : Color.GRAY);

        } else if (currentState == BattleState.BAG_MENU) {
            optionsTable.setVisible(true);
            messageLabel.setText("Elige una acción:");

            fightLabel.setText(isNPCBattle ? "---" : "CAPTURAR");
            bagLabel.setText("CURAR");
            pokemonLabel.setText("POTENCIAR");
            runLabel.setText("ATRÁS");

            fightLabel.setColor(bagMenuIndex == 0 ? Color.RED : Color.GRAY);
            bagLabel.setColor(bagMenuIndex == 1 ? Color.GREEN : Color.GRAY);
            pokemonLabel.setColor(bagMenuIndex == 2 ? Color.ORANGE : Color.GRAY);
            runLabel.setColor(bagMenuIndex == 3 ? Color.YELLOW : Color.GRAY);

        } else if (currentState == BattleState.BAG_CAPTURE_MENU) {
            optionsTable.setVisible(true);
            messageLabel.setText("Elige una Pokéball:");

            fightLabel.setText("POKEBALL"); // ID 101
            bagLabel.setText("MASTERBALL"); // ID 106
            pokemonLabel.setText("---");
            runLabel.setText("ATRÁS");

            fightLabel.setColor(bagCaptureMenuIndex == 0 ? Color.RED : Color.GRAY);
            bagLabel.setColor(bagCaptureMenuIndex == 1 ? Color.MAGENTA : Color.GRAY);
            runLabel.setColor(bagCaptureMenuIndex == 3 ? Color.YELLOW : Color.GRAY);

        } else if (currentState == BattleState.BAG_HEAL_MENU) {
            optionsTable.setVisible(true);
            messageLabel.setText("Elige una Poción:");

            fightLabel.setText("POCIÓN"); // ID 103
            bagLabel.setText("SUPERPOCIÓN"); // ID 104
            pokemonLabel.setText("---");
            runLabel.setText("ATRÁS");

            fightLabel.setColor(bagHealMenuIndex == 0 ? Color.GREEN : Color.GRAY);
            bagLabel.setColor(bagHealMenuIndex == 1 ? Color.CYAN : Color.GRAY);
            runLabel.setColor(bagHealMenuIndex == 3 ? Color.YELLOW : Color.GRAY);

        } else if (currentState == BattleState.POKEMON_MENU) {
            optionsTable.setVisible(true);
            messageLabel.setText("Elige un Pokémon (Cambios: " + switchesUsed + "/" + MAX_SWITCHES + "):");

            java.util.List<Pokemon> team = sodyl.proyecto.clases.Pokedex.getTeam();

            // Map slots to labels
            fightLabel.setText(team.size() > 0 ? team.get(0).getEspecie() : "---");
            bagLabel.setText(team.size() > 1 ? team.get(1).getEspecie() : "---");
            pokemonLabel.setText(team.size() > 2 ? team.get(2).getEspecie() : "---");
            runLabel.setText("ATRÁS");

            fightLabel.setColor(pokemonMenuIndex == 0 ? Color.GREEN : Color.GRAY);
            bagLabel.setColor(pokemonMenuIndex == 1 ? Color.GREEN : Color.GRAY);
            pokemonLabel.setColor(pokemonMenuIndex == 2 ? Color.GREEN : Color.GRAY);
            runLabel.setColor(pokemonMenuIndex == 3 ? Color.YELLOW : Color.GRAY);

        } else {
            // In message mode or enemy turn, hide options
            optionsTable.setVisible(false);
        }
    }

    private void showMessage(String text, Runnable onFinished) {
        currentState = BattleState.MESSAGE_WAIT;
        messageLabel.setText(text);
        this.onMessageFinished = onFinished;
        // Hide options
        Table optionsTable = (Table) bottomMenuTable.getChildren().get(1);
        optionsTable.setVisible(false);
    }

    // --- INPUT HANDLING ---

    @Override
    public boolean keyDown(int keycode) {
        if (currentState == BattleState.MESSAGE_WAIT) {
            if (keycode == Keys.Z || keycode == Keys.ENTER) {
                if (onMessageFinished != null) {
                    onMessageFinished.run();
                }
            }
            return true;
        }

        if (keycode == Keys.L) {
            cyclePokemonDebug();
            return true;
        }

        if (keycode == Keys.P) {
            cyclePlayerPokemonDebug();
            return true;
        }

        if (currentState == BattleState.MAIN_MENU) {
            handleMainMenuInput(keycode);
        } else if (currentState == BattleState.FIGHT_MENU) {
            handleFightMenuInput(keycode);
        } else if (currentState == BattleState.BAG_MENU) {
            handleBagMenuInput(keycode);
        } else if (currentState == BattleState.BAG_CAPTURE_MENU) {
            handleBagCaptureMenuInput(keycode);
        } else if (currentState == BattleState.BAG_HEAL_MENU) {
            handleBagHealMenuInput(keycode);
        } else if (currentState == BattleState.POKEMON_MENU) {
            handlePokemonMenuInput(keycode);
        }

        return true;
    }

    private void cyclePokemonDebug() {
        if (debugPokemonList == null) {
            debugPokemonList = sodyl.proyecto.clases.Pokemones.getAllSpecies();
        }
        if (debugPokemonList.isEmpty())
            return;

        debugIndex = (debugIndex + 1) % debugPokemonList.size();
        String name = debugPokemonList.get(debugIndex);
        Pokemon p = sodyl.proyecto.clases.Pokemones.getPokemon(name);
        if (p != null) {
            batalla.setPokemonEnemigo(p);
            loadSprite(p.getSpriteFront(), false);
            updateInfoLabels();
            showMessage("DEBUG RIVAL: (" + (debugIndex + 1) + "/" + debugPokemonList.size() + ") - " + name, () -> {
                currentState = BattleState.MAIN_MENU;
                updateMenuVisuals();
            });
        }
    }

    private void cyclePlayerPokemonDebug() {
        if (debugPokemonList == null) {
            debugPokemonList = sodyl.proyecto.clases.Pokemones.getAllSpecies();
        }
        if (debugPokemonList.isEmpty())
            return;

        debugPlayerIndex = (debugPlayerIndex + 1) % debugPokemonList.size();
        String name = debugPokemonList.get(debugPlayerIndex);
        Pokemon p = sodyl.proyecto.clases.Pokemones.getPokemon(name);
        if (p != null) {
            batalla.setPokemonJugador(p);
            loadSprite(p.getSpriteBack(), true);
            updateInfoLabels();
            showMessage("DEBUG JUGADOR: (" + (debugPlayerIndex + 1) + "/" + debugPokemonList.size() + ") - " + name,
                    () -> {
                        currentState = BattleState.MAIN_MENU;
                        updateMenuVisuals();
                    });
        }
    }

    private void handleMainMenuInput(int keycode) {
        if (keycode == Keys.UP && mainMenuIndex >= 2)
            mainMenuIndex -= 2;
        if (keycode == Keys.DOWN && mainMenuIndex <= 1)
            mainMenuIndex += 2;
        if (keycode == Keys.LEFT && (mainMenuIndex % 2) != 0)
            mainMenuIndex--;
        if (keycode == Keys.RIGHT && (mainMenuIndex % 2) == 0)
            mainMenuIndex++;

        if (keycode == Keys.Z || keycode == Keys.ENTER) {
            switch (mainMenuIndex) {
                case 0: // FIGHT
                    currentState = BattleState.FIGHT_MENU;
                    fightMenuIndex = 0;
                    break;
                case 1: // BAG
                    currentState = BattleState.BAG_MENU;
                    bagMenuIndex = 0;
                    break;
                case 2: // POKEMON
                    // Check if player has more than 1 Pokemon
                    if (sodyl.proyecto.clases.Pokedex.getTeam().size() <= 1) {
                        showMessage("¡Solo tienes un Pokémon!", () -> {
                            currentState = BattleState.MAIN_MENU;
                            updateMenuVisuals();
                        });
                    } else {
                        currentState = BattleState.POKEMON_MENU;
                        pokemonMenuIndex = 0;
                    }
                    break;
                case 3: // RUN
                    if (isTutorial) {
                        showMessage("¡No puedes huir!", () -> {
                            currentState = BattleState.MAIN_MENU;
                            updateMenuVisuals();
                        });
                    } else if (isFinalBattle) {
                        showMessage("¡No hay escapatoria! Es matar o morir.", () -> {
                            currentState = BattleState.MAIN_MENU;
                            updateMenuVisuals();
                        });
                    } else {
                        showMessage("¡Escapaste sin problemas!", this::returnToMap);
                    }
                    break;
            }
        }
        updateMenuVisuals();
    }

    private void handleFightMenuInput(int keycode) {
        if (keycode == Keys.UP && fightMenuIndex >= 2)
            fightMenuIndex -= 2;
        if (keycode == Keys.DOWN && fightMenuIndex <= 1)
            fightMenuIndex += 2;
        if (keycode == Keys.LEFT && (fightMenuIndex % 2) != 0)
            fightMenuIndex--;
        if (keycode == Keys.RIGHT && (fightMenuIndex % 2) == 0)
            fightMenuIndex++;

        if (keycode == Keys.X || keycode == Keys.ESCAPE) {
            currentState = BattleState.MAIN_MENU;
            updateMenuVisuals();
            return;
        }

        if (keycode == Keys.Z || keycode == Keys.ENTER) {
            if (fightMenuIndex == 3) { // ATRAS
                currentState = BattleState.MAIN_MENU;
            } else if (fightMenuIndex == 0 || fightMenuIndex == 1) {
                // Execute Attack
                int moveIndex = fightMenuIndex + 1;
                performPlayerAttack(moveIndex);
            }
        }
        updateMenuVisuals();
    }

    private void handleBagMenuInput(int keycode) {
        if (keycode == Keys.UP && bagMenuIndex >= 2)
            bagMenuIndex -= 2;
        if (keycode == Keys.DOWN && bagMenuIndex <= 1)
            bagMenuIndex += 2;
        if (keycode == Keys.LEFT && (bagMenuIndex % 2) != 0)
            bagMenuIndex--;
        if (keycode == Keys.RIGHT && (bagMenuIndex % 2) == 0)
            bagMenuIndex++;

        if (keycode == Keys.X || keycode == Keys.ESCAPE) {
            currentState = BattleState.MAIN_MENU;
            updateMenuVisuals();
            return;
        }

        if (keycode == Keys.Z || keycode == Keys.ENTER) {
            if (bagMenuIndex == 0) { // CAPTURAR
                if (isNPCBattle) {
                    showMessage("¡No puedes capturar el Pokémon de otro entrenador!", () -> {
                        currentState = BattleState.BAG_MENU;
                        updateMenuVisuals();
                    });
                    return;
                }
                currentState = BattleState.BAG_CAPTURE_MENU;
                bagCaptureMenuIndex = 0;
            } else if (bagMenuIndex == 1) { // CURAR
                currentState = BattleState.BAG_HEAL_MENU;
                bagHealMenuIndex = 0;
            } else if (bagMenuIndex == 2) { // POTENCIAR
                if (inventario.getQuantity(105) <= 0) {
                    showMessage("¡No tienes Piedras Potenciadoras!", () -> {
                        currentState = BattleState.BAG_MENU;
                        updateMenuVisuals();
                    });
                    return;
                }
                inventario.removeObjeto(105, 1);
                batalla.activatePowerUp();
                triggerFlash(Color.ORANGE, () -> {
                    showMessage("¡Daño aumentado en un 50%!", () -> {
                        startEnemyTurn();
                    });
                });
            } else if (bagMenuIndex == 3) { // ATRÁS
                currentState = BattleState.MAIN_MENU;
            }
        }
        updateMenuVisuals();
    }

    private void handleBagCaptureMenuInput(int keycode) {
        if (keycode == Keys.UP && bagCaptureMenuIndex >= 2)
            bagCaptureMenuIndex -= 2;
        if (keycode == Keys.DOWN && bagCaptureMenuIndex <= 1)
            bagCaptureMenuIndex += 2;
        if (keycode == Keys.LEFT && (bagCaptureMenuIndex % 2) != 0)
            bagCaptureMenuIndex--;
        if (keycode == Keys.RIGHT && (bagCaptureMenuIndex % 2) == 0)
            bagCaptureMenuIndex++;

        if (keycode == Keys.X || keycode == Keys.ESCAPE) {
            currentState = BattleState.BAG_MENU;
            updateMenuVisuals();
            return;
        }

        if (keycode == Keys.Z || keycode == Keys.ENTER) {
            if (bagCaptureMenuIndex == 0) { // POKEBALL
                attemptCapture(101);
            } else if (bagCaptureMenuIndex == 1) { // MASTERBALL
                attemptCapture(106);
            } else if (bagCaptureMenuIndex == 3) { // ATRÁS
                currentState = BattleState.BAG_MENU;
            }
        }
        updateMenuVisuals();
    }

    private void handleBagHealMenuInput(int keycode) {
        if (keycode == Keys.UP && bagHealMenuIndex >= 2)
            bagHealMenuIndex -= 2;
        if (keycode == Keys.DOWN && bagHealMenuIndex <= 1)
            bagHealMenuIndex += 2;
        if (keycode == Keys.LEFT && (bagHealMenuIndex % 2) != 0)
            bagHealMenuIndex--;
        if (keycode == Keys.RIGHT && (bagHealMenuIndex % 2) == 0)
            bagHealMenuIndex++;

        if (keycode == Keys.X || keycode == Keys.ESCAPE) {
            currentState = BattleState.BAG_MENU;
            updateMenuVisuals();
            return;
        }

        if (keycode == Keys.Z || keycode == Keys.ENTER) {
            if (bagHealMenuIndex == 0) { // POCIÓN
                useHealItem(103, 20);
            } else if (bagHealMenuIndex == 1) { // SUPERPOCIÓN
                useHealItem(104, 50);
            } else if (bagHealMenuIndex == 3) { // ATRÁS
                currentState = BattleState.BAG_MENU;
            }
        }
        updateMenuVisuals();
    }

    private void useHealItem(int itemId, int healAmount) {
        Objeto item = Objeto.getObjeto(itemId);
        if (inventario.getQuantity(itemId) <= 0) {
            showMessage("¡No tienes " + item.getNombre() + "!", () -> {
                currentState = BattleState.BAG_HEAL_MENU;
                updateMenuVisuals();
            });
            return;
        }

        Pokemon player = batalla.getPokemonJugador();

        // Check if Pokemon is already at full HP
        if (player.getActualHP() >= player.getMaxHp()) {
            showMessage("¡" + player.getEspecie() + " ya tiene toda su vida!", () -> {
                currentState = BattleState.BAG_HEAL_MENU;
                updateMenuVisuals();
            });
            return;
        }

        // Use item
        inventario.removeObjeto(itemId, 1);
        int oldHP = player.getActualHP();
        player.setActualHP(Math.min(player.getActualHP() + healAmount, player.getMaxHp()));
        int actualHealed = player.getActualHP() - oldHP;

        // Trigger green flash effect
        triggerFlash(new Color(0f, 1f, 0f, 0.7f), () -> {
            updateInfoLabels();
            showMessage(
                    "¡" + player.getEspecie() + " recuperó " + actualHealed + " PS usando " + item.getNombre() + "!",
                    () -> {
                        startEnemyTurn();
                    });
        });
    }

    // --- BATTLE LOGIC ---

    private void handlePokemonMenuInput(int keycode) {
        if (keycode == Keys.UP && pokemonMenuIndex >= 2)
            pokemonMenuIndex -= 2;
        if (keycode == Keys.DOWN && pokemonMenuIndex <= 1)
            pokemonMenuIndex += 2;
        if (keycode == Keys.LEFT && (pokemonMenuIndex % 2) != 0)
            pokemonMenuIndex--;
        if (keycode == Keys.RIGHT && (pokemonMenuIndex % 2) == 0)
            pokemonMenuIndex++;

        // Clamp index based on team size
        java.util.List<Pokemon> team = sodyl.proyecto.clases.Pokedex.getTeam();

        if (keycode == Keys.X || keycode == Keys.ESCAPE) {
            currentState = BattleState.MAIN_MENU;
            updateMenuVisuals();
            return;
        }

        if (keycode == Keys.Z || keycode == Keys.ENTER) {
            if (pokemonMenuIndex == 3) { // ATRAS
                currentState = BattleState.MAIN_MENU;
            } else {
                if (pokemonMenuIndex < team.size()) {
                    switchPokemon(team.get(pokemonMenuIndex));
                }
            }
        }
        updateMenuVisuals();
    }

    private void switchPokemon(Pokemon newPokemon) {
        if (switchesUsed >= MAX_SWITCHES) {
            showMessage("¡Ya no puedes cambiar más Pokémon (Máx 2)!", () -> {
                currentState = BattleState.POKEMON_MENU;
                updateMenuVisuals();
            });
            return;
        }

        if (newPokemon == batalla.getPokemonJugador()) {
            showMessage("¡" + newPokemon.getEspecie() + " ya está en combate!", () -> {
                currentState = BattleState.POKEMON_MENU;
                updateMenuVisuals();
            });
            return;
        }

        if (newPokemon.getActualHP() <= 0) {
            showMessage("¡" + newPokemon.getEspecie() + " está debilitado!", () -> {
                currentState = BattleState.POKEMON_MENU;
                updateMenuVisuals();
            });
            return;
        }

        switchesUsed++;
        batalla.setPokemonJugador(newPokemon);

        // Update Texture
        // Update Texture
        loadSprite(newPokemon.getSpriteBack(), true);

        updateInfoLabels();

        showMessage("¡Adelante " + newPokemon.getEspecie() + "!", () -> {
            startEnemyTurn(); // Switching takes a turn
        });
    }

    private void performPlayerAttack(int moveIndex) {
        Pokemon player = batalla.getPokemonJugador();
        int pp = (moveIndex == 1) ? player.getppM1() : player.getppM2();

        if (pp <= 0) {
            showMessage("¡No quedan PP!", () -> {
                currentState = BattleState.FIGHT_MENU;
                updateMenuVisuals();
            });
            return;
        }

        // 1. Determine Enemy Move (Random)
        int enemyMoveIndex = (Math.random() < 0.5) ? 1 : 2;

        // 2. Determine Order
        boolean playerFirst = batalla.playerAttacksFirst(moveIndex, enemyMoveIndex);

        if (playerFirst) {
            executeTurnSequence(batalla.getPokemonJugador(), moveIndex, batalla.getPokemonEnemigo(), enemyMoveIndex);
        } else {
            executeTurnSequence(batalla.getPokemonEnemigo(), enemyMoveIndex, batalla.getPokemonJugador(), moveIndex);
        }
    }

    private void executeTurnSequence(Pokemon first, int firstMove, Pokemon second, int secondMove) {
        // Execute first attack
        String msg1 = batalla.executeAttack(first, second, firstMove);
        // Do NOT update HP visuals yet

        showMessage(msg1, () -> {
            boolean isPlayerFirst = (first == batalla.getPokemonJugador());
            triggerAttackAnim(isPlayerFirst, first, firstMove, () -> {
                updateInfoLabels(); // Update visuals now

                if (second.getActualHP() <= 0) {
                    handleFaint(second);
                } else {
                    // Execute second attack
                    String msg2 = batalla.executeAttack(second, first, secondMove);
                    showMessage(msg2, () -> {
                        boolean isPlayerSecond = (second == batalla.getPokemonJugador());
                        triggerAttackAnim(isPlayerSecond, second, secondMove, () -> {
                            updateInfoLabels();

                            if (first.getActualHP() <= 0) {
                                handleFaint(first);
                            } else {
                                // End of turn
                                currentState = BattleState.MAIN_MENU;
                                updateMenuVisuals();
                            }
                        });
                    });
                }
            });
        });
    }

    private void triggerAttackAnim(boolean isPlayerAttacking, Pokemon attacker, int moveIndex, Runnable onComplete) {
        isAttackAnimActive = true;
        animationTimer = 0f;
        onAttackAnimFinished = onComplete;
        // No more sprite setup, logic handled in render()
    }

    /**
     * Triggers a screen flash effect with the specified color
     * 
     * @param color      The color of the flash (e.g., green for healing, orange for
     *                   capture)
     * @param onComplete Callback to run after flash completes
     */
    private void triggerFlash(Color color, Runnable onComplete) {
        isFlashActive = true;
        flashTimer = 0f;
        flashColor = new Color(color);
        flashCount = 0;
        onFlashComplete = onComplete;
    }

    private void handleFaint(Pokemon fainted) {
        boolean isPlayer = (fainted == batalla.getPokemonJugador());

        if (isPlayer) {
            // LOSS CONDITION
            // 1. Find a crafted item to remove (POKEBALL or MEDICINA)
            int itemsRemovedCount = 0;

            Map<Integer, Integer> items = inventario.getAllObjetos();
            List<Integer> craftedItems = new ArrayList<>();

            for (Integer itemId : items.keySet()) {
                Objeto obj = Objeto.getObjeto(itemId);
                if (obj != null && (obj.getTipo() == Objeto.Type.POKEBALL || obj.getTipo() == Objeto.Type.MEDICINA)) {
                    craftedItems.add(itemId);
                }
            }

            if (!craftedItems.isEmpty()) {
                int itemsToRemove = isFinalBattle ? (new Random().nextInt(3) + 3) : 1; // 3-5 items for final battle

                for (int i = 0; i < itemsToRemove; i++) {
                    if (craftedItems.isEmpty())
                        break;

                    int itemToRemoveId = craftedItems.get(new Random().nextInt(craftedItems.size()));
                    inventario.removeObjeto(itemToRemoveId, 1);

                    // Update list if run out
                    if (inventario.getQuantity(itemToRemoveId) <= 0) {
                        craftedItems.remove((Integer) itemToRemoveId);
                    }
                    itemsRemovedCount++;
                }
            }

            String lossMsg = "¡" + fainted.getEspecie() + " se debilitó! Perdiste...";
            if (isFinalBattle) {
                lossMsg = "¡Has sido derrotado por Arceus! Perdiste " + itemsRemovedCount + " objetos...";
            } else if (itemsRemovedCount > 0) {
                lossMsg = "¡" + fainted.getEspecie() + " se debilitó! Perdiste algún objeto...";
            }

            showMessage(lossMsg, this::returnToMap);

        } else {
            // WIN CONDITION
            // Enemy fainted
            if (isFinalBattle) {
                sodyl.proyecto.clases.Pokedex.addResearchPoints(fainted.getEspecie(), 10);
                showMessage("¡HAS DERROTADO A ARCEUS! ¡OBJETIVO COMPLETADO!", () -> {
                    showMessage("¡Investigación legendaria al máximo! ¡Eres una leyenda!", this::returnToMap);
                });
            } else {
                sodyl.proyecto.clases.Pokedex.addResearchPoints(fainted.getEspecie(), 1);

                // Reward Logic
                int rewardId;
                if (isNPCBattle) {
                    // High Value Reward for NPC Battles: Revivir Máximo (106)
                    rewardId = 106;
                } else {
                    // Standard Reward for Wild Pokemon: Random Material (ID 1-5)
                    rewardId = new Random().nextInt(5) + 1; // 1 to 5
                }

                Objeto rewardObj = Objeto.getObjeto(rewardId);
                inventario.addObjeto(rewardId, 1);

                // LVL Logic: +1 to winner
                Pokemon winner = batalla.getPokemonJugador();
                int oldLevel = winner.getNivel();
                winner.setNivel(oldLevel + 1);
                winner.actualizarAtributos();
                String lvlMsg = (winner.getNivel() > oldLevel) ? " ¡Subió a Nv." + winner.getNivel() + "!" : "";

                showMessage(
                        "¡" + fainted.getEspecie() + " se debilitó! (+1 Inv. y " + rewardObj.getNombre() + ")" + lvlMsg,
                        () -> {
                            showMessage("¡Ganaste la batalla!", this::returnToMap);
                        });
            }
        }
    }

    private void startEnemyTurn() {
        currentState = BattleState.ENEMY_TURN;
        // Small delay before enemy attacks
        stateTimer = 0f;
    }

    private void attemptCapture(int itemId) {
        if (isNPCBattle) {
            showMessage("¡No puedes capturar el Pokémon de otro entrenador!", () -> {
                currentState = BattleState.BAG_MENU;
                updateMenuVisuals();
            });
            return;
        }
        if (inventario.getQuantity(itemId) <= 0) {
            String itemName = (itemId == 106) ? "Masterballs" : "Pokéballs";
            showMessage("¡No tienes " + itemName + "!", () -> {
                currentState = BattleState.BAG_CAPTURE_MENU;
                updateMenuVisuals();
            });
            return;
        }

        // Trigger capture flash
        Color flashColor = (itemId == 106) ? Color.MAGENTA : new Color(1f, 0.65f, 0f, 0.8f);

        triggerFlash(flashColor, () -> {
            boolean captured = batalla.intentarCaptura(itemId);

            if (captured) {
                // addCollected already adds +2 research points internally
                sodyl.proyecto.clases.Pokedex.addCollected(batalla.getPokemonEnemigo());

                if (isTutorial) {
                    sodyl.proyecto.clases.Pokedex.setTutorialCompleted(true);
                    showMessage("¡Tutorial completado! ¡Bien hecho!", this::returnToMap);
                } else if (isFinalBattle) {
                    // Capture Win
                    sodyl.proyecto.clases.Pokedex.addResearchPoints(batalla.getPokemonEnemigo().getEspecie(), 10);
                    showMessage("¡HAS CAPTURADO A ARCEUS! ¡INCREÍBLE!", () -> {
                        showMessage("¡Investigación completada! ¡Has dominado el juego!", this::returnToMap);
                    });
                } else {
                    // Winner +1 level
                    Pokemon winner = batalla.getPokemonJugador();
                    int oldLvl = winner.getNivel();
                    winner.setNivel(oldLvl + 1);
                    winner.actualizarAtributos();
                    String winnerLvlMsg = (winner.getNivel() > oldLvl) ? " (+1 Nivel para " + winner.getEspecie() + ")"
                            : "";

                    showMessage("¡" + batalla.getPokemonEnemigo().getEspecie() + " capturado! (+2 Inv.)" + winnerLvlMsg,
                            this::returnToMap);
                }
            } else {
                String failureMsg = "¡El Pokémon escapó! (Demasiada vida)";
                if (itemId == 101 && batalla.getPokemonEnemigo().getEspecie().equalsIgnoreCase("Arceus")) {
                    failureMsg = "¡La Pokéball es inútil contra Arceus!";
                } else if (itemId == 106) {
                    failureMsg = "¡La MasterBall falló! (Rival con demasiada vida)";
                }
                showMessage(failureMsg, this::startEnemyTurn);
            }
        });
    }

    private void returnToMap() {
        if (previousScreen != null) {
            // Check if player won an NPC battle
            if (isNPCBattle && batalla.getPokemonEnemigo().getActualHP() <= 0) {
                previousScreen.onNPCBattleVictory(previousScreen.getCurrentBattleNPC());
                game.setScreen(previousScreen);
                return;
            }

            previousScreen.clearCurrentBattleNPC();
            previousScreen.currentState = ScreenMapaTiled.GameState.FREE_ROAMING;
            previousScreen.canTriggerEncounter = false;
            previousScreen.encounterCooldown = 0f;
            game.setScreen(previousScreen);
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        // Apply viewport and update camera
        stage.getViewport().apply();
        batch.setProjectionMatrix(stage.getCamera().combined);

        batch.begin();
        // 1. Background
        batch.draw(backgroundTexture, 0, 0, Proyecto.PANTALLA_W, Proyecto.PANTALLA_H);

        // 2. Pokemon Placeholders
        float enemyScale = 100f; // Tamaño base
        String enemyName = batalla.getPokemonEnemigo().getEspecie();

        if (enemyName.equals("Rowlet")) {
            enemyScale = 65f;
        } else if (enemyName.equals("Oshawott")) {
            enemyScale = 75f;
        } else if (enemyName.equals("Sylveon")) {
            enemyScale = 160f; // Disminuido de 200f
        } else if (enemyName.equals("Arceus")) {
            enemyScale = 310f; // Disminuido de 350f
        } else if (enemyName.equals("Mewtwo")) {
            enemyScale = 160f;
        } else if (enemyName.equals("Charizard")) {
            enemyScale = 170f;
        } else if (enemyName.equals("Blastoise")) {
            enemyScale = 170f;
        } else if (enemyName.equals("Lucario")) {
            enemyScale = 140f;
        } else if (enemyName.equals("Serperior")) {
            enemyScale = 180f;
        } else if (enemyName.equals("Gyarados")) {
            enemyScale = 260f;
        }

        float enemyX = Proyecto.PANTALLA_W * 0.60f; // Posición base
        float enemyY = Proyecto.PANTALLA_H * 0.50f;

        if (enemyName.equals("Arceus")) {
            enemyX = Proyecto.PANTALLA_W * 0.50f;
            enemyY = Proyecto.PANTALLA_H * 0.40f; // Subido (era 0.35f)
        } else if (enemyName.equals("Gyarados")) {
            enemyX = Proyecto.PANTALLA_W * 0.48f; // Un poco a la derecha (era 0.45)
            enemyY = Proyecto.PANTALLA_H * 0.46f; // Un poco más abajo (era 0.50)
        } else if (enemyName.equals("Sylveon")) {
            enemyX = Proyecto.PANTALLA_W * 0.53f; // Un poco a la derecha (era 0.50)
            enemyY = Proyecto.PANTALLA_H * 0.46f; // Un poco más abajo (era 0.50)
        } else if (enemyName.equals("Serperior")) {
            enemyX = Proyecto.PANTALLA_W * 0.53f; // Un poco a la derecha (era 0.50)
            enemyY = Proyecto.PANTALLA_H * 0.46f; // Un poco más abajo (era 0.50)
        } else if (enemyName.equals("Blastoise")) {
            enemyX = Proyecto.PANTALLA_W * 0.52f; // Rodado a la izquierda (era 0.60 base)
        }

        if (enemyAnimation != null) {
            enemyAnimationTimer += delta;
            TextureRegion currentFrame = enemyAnimation.getKeyFrame(enemyAnimationTimer, true);
            batch.draw(currentFrame, enemyX, enemyY, enemyScale, enemyScale);
        } else {
            batch.draw(pokemonEnemyTexture, enemyX, enemyY, enemyScale, enemyScale);
        }

        // Player (Bottom Left)
        float playerScale = 100f;
        String playerName = batalla.getPokemonJugador().getEspecie();

        if (playerName.equals("Rowlet")) {
            playerScale = 65f;
        } else if (playerName.equals("Oshawott")) {
            playerScale = 75f;
        } else if (playerName.equals("Arceus")) {
            playerScale = 310f; // Mismo tamaño que el rival
        } else if (playerName.equals("Sylveon")) {
            playerScale = 160f; // Disminuido de 200f
        } else if (playerName.equals("Mewtwo")) {
            playerScale = 160f;
        } else if (playerName.equals("Charizard")) {
            playerScale = 170f;
        } else if (playerName.equals("Blastoise")) {
            playerScale = 170f;
        } else if (playerName.equals("Lucario")) {
            playerScale = 140f;
        } else if (playerName.equals("Serperior")) {
            playerScale = 180f;
        } else if (playerName.equals("Gyarados")) {
            playerScale = 260f;
        }

        float playerX = Proyecto.PANTALLA_W * 0.25f;
        if (playerName.equals("Sylveon")) {
            playerX = Proyecto.PANTALLA_W * 0.15f; // Más a la izquierda
        }
        float playerY = Proyecto.PANTALLA_H * 0.20f;

        if (playerAnimation != null) {
            playerAnimationTimer += delta;
            TextureRegion currentFrame = playerAnimation.getKeyFrame(playerAnimationTimer, true);
            batch.draw(currentFrame, playerX, playerY, playerScale, playerScale);
        } else {
            batch.draw(pokemonPlayerTexture, playerX, playerY, playerScale, playerScale);
        }

        // 3. Attack Animation (Blink Effect)
        if (isAttackAnimActive) {
            animationTimer += delta;

            // Simple "Blink" Effect: Flash White twice
            // Cycle: 0.0 -> 0.1 (White), 0.1 -> 0.2 (Clear), 0.2 -> 0.3 (White), 0.3 -> End

            float alpha = 0f;
            if (animationTimer < 0.1f) {
                alpha = 0.7f; // Flash on
            } else if (animationTimer < 0.2f) {
                alpha = 0f; // Flash off
            } else if (animationTimer < 0.3f) {
                alpha = 0.7f; // Flash on
            } else if (animationTimer > 0.4f) {
                // Done
                isAttackAnimActive = false;
                if (onAttackAnimFinished != null) {
                    onAttackAnimFinished.run();
                }
            }

            if (alpha > 0) {
                batch.end(); // End SpriteBatch to draw shapes

                Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,
                        com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);

                com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer = new com.badlogic.gdx.graphics.glutils.ShapeRenderer();
                shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
                shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(1f, 1f, 1f, alpha);
                shapeRenderer.rect(0, 0, Proyecto.PANTALLA_W, Proyecto.PANTALLA_H);
                shapeRenderer.end();
                shapeRenderer.dispose(); // Important to dispose if creating new instance

                Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);

                batch.begin(); // Restart SpriteBatch
            }
        }

        // 4. Flash Effect (for healing and capture)
        if (isFlashActive) {
            flashTimer += delta;

            // Determine if we should show the flash (on) or hide it (off)
            // Each flash cycle: FLASH_DURATION on, FLASH_DURATION off
            float cycleTime = flashTimer % (FLASH_DURATION * 2);
            boolean showFlash = cycleTime < FLASH_DURATION;

            // Count completed flashes
            int completedFlashes = (int) (flashTimer / (FLASH_DURATION * 2));

            if (completedFlashes >= MAX_FLASHES) {
                // Flash animation complete
                isFlashActive = false;
                if (onFlashComplete != null) {
                    onFlashComplete.run();
                    onFlashComplete = null;
                }
            } else if (showFlash) {
                // Draw flash overlay
                batch.end(); // End SpriteBatch to draw shapes

                Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,
                        com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);

                com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer = new com.badlogic.gdx.graphics.glutils.ShapeRenderer();
                shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
                shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(flashColor);
                shapeRenderer.rect(0, 0, Proyecto.PANTALLA_W, Proyecto.PANTALLA_H);
                shapeRenderer.end();
                shapeRenderer.dispose();

                Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);

                batch.begin(); // Restart SpriteBatch
            }
        }

        batch.end();

        // Logic for Enemy Turn Delay
        if (currentState == BattleState.ENEMY_TURN) {
            stateTimer += delta;
            if (stateTimer > 1.0f) {
                String msg = batalla.ataqueEnemigo();
                updateInfoLabels();
                showMessage(msg, () -> {
                    if (batalla.getPokemonJugador().getActualHP() <= 0) {
                        showMessage("¡" + batalla.getPokemonJugador().getEspecie() + " se debilitó! Perdiste...",
                                this::returnToMap);
                    } else {
                        currentState = BattleState.MAIN_MENU;
                        updateMenuVisuals();
                    }
                });
                stateTimer = 0f; // Reset to avoid multiple calls
                currentState = BattleState.MESSAGE_WAIT; // Wait for user to read attack msg
            }
        }

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        batch.dispose();
        font.dispose();
        backgroundTexture.dispose();
        battleBoxTexture.dispose();
        selectionTexture.dispose();
        if (pokemonPlayerTexture != null)
            pokemonPlayerTexture.dispose();
        if (pokemonEnemyTexture != null)
            pokemonEnemyTexture.dispose();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
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
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
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

    private void loadSprite(String path, boolean isPlayer) {
        // Dispose previous
        if (isPlayer) {
            if (pokemonPlayerTexture != null) {
                pokemonPlayerTexture.dispose();
                pokemonPlayerTexture = null;
            }
            playerAnimation = null;
            playerAnimationTimer = 0f;
        } else {
            if (pokemonEnemyTexture != null) {
                pokemonEnemyTexture.dispose();
                pokemonEnemyTexture = null;
            }
            enemyAnimation = null;
            enemyAnimationTimer = 0f;
        }

        if (path == null) {
            if (isPlayer)
                pokemonPlayerTexture = createPlaceholderTexture(Color.BLUE);
            else
                pokemonEnemyTexture = createPlaceholderTexture(Color.RED);
            return;
        }

        try {
            if (path.toLowerCase().endsWith(".gif")) {
                Animation<TextureRegion> gifAnim = GifDecoder.loadGIFAnimation(Animation.PlayMode.LOOP,
                        Gdx.files.internal(path).read());
                if (gifAnim != null) {
                    if (isPlayer) {
                        playerAnimation = gifAnim;
                        if (gifAnim.getKeyFrames().length > 0)
                            pokemonPlayerTexture = gifAnim.getKeyFrames()[0].getTexture();
                    } else {
                        enemyAnimation = gifAnim;
                        if (gifAnim.getKeyFrames().length > 0)
                            pokemonEnemyTexture = gifAnim.getKeyFrames()[0].getTexture();
                    }
                    return;
                }
            }

            // Check for multi-file animation (separated by ;)
            if (path.contains(";")) {
                String[] paths = path.split(";");
                Array<TextureRegion> frames = new Array<>();
                Texture firstTexture = null;

                for (int i = 0; i < paths.length; i++) {
                    Texture tex = new Texture(Gdx.files.internal(paths[i].trim()));
                    frames.add(new TextureRegion(tex));
                    if (i == 0)
                        firstTexture = tex;
                }

                Animation<TextureRegion> anim = new Animation<>(0.5f, frames, Animation.PlayMode.LOOP);

                if (isPlayer) {
                    playerAnimation = anim;
                    pokemonPlayerTexture = firstTexture; // Keep reference to avoid immediate disposal issues if needed,
                                                         // though we draw animation
                } else {
                    enemyAnimation = anim;
                    pokemonEnemyTexture = firstTexture;
                }
                return;
            }

            Texture tempTexture = new Texture(Gdx.files.internal(path));

            // Check for strip animation (Heuristic: Width >= Height * 2)
            if (tempTexture.getWidth() >= tempTexture.getHeight() * 2) {
                int frameHeight = tempTexture.getHeight();
                int frameWidth = frameHeight; // Assume square frames
                int frameCount = tempTexture.getWidth() / frameWidth;

                TextureRegion[][] tmp = TextureRegion.split(tempTexture, frameWidth, frameHeight);
                TextureRegion[] frames = new TextureRegion[frameCount];
                for (int i = 0; i < frameCount; i++) {
                    frames[i] = tmp[0][i];
                }

                Animation<TextureRegion> anim = new Animation<>(0.15f, frames);
                anim.setPlayMode(Animation.PlayMode.LOOP);

                if (isPlayer) {
                    playerAnimation = anim;
                    pokemonPlayerTexture = tempTexture;
                } else {
                    enemyAnimation = anim;
                    pokemonEnemyTexture = tempTexture;
                }
            } else {
                if (isPlayer)
                    pokemonPlayerTexture = tempTexture;
                else
                    pokemonEnemyTexture = tempTexture;
            }

        } catch (Exception e) {
            Gdx.app.error("ScreenBatalla", "Sprite no encontrado/Error al cargar: " + path + " - " + e.getMessage());
            if (isPlayer)
                pokemonPlayerTexture = createPlaceholderTexture(Color.BLUE);
            else
                pokemonEnemyTexture = createPlaceholderTexture(Color.RED);
        }
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
