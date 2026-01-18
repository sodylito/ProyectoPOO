package sodyl.proyecto.libGDX;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.utils.viewport.FitViewport;

import sodyl.proyecto.clases.Inventario;
import sodyl.proyecto.clases.Objeto;
import sodyl.proyecto.clases.Objeto.Recipe;
import sodyl.proyecto.clases.Pokemon;
import sodyl.proyecto.clases.Pokemones;
import sodyl.proyecto.networking.ConexionCliente;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonReader;
import sodyl.proyecto.networking.SharedResourceManager;
import sodyl.proyecto.networking.DuelManager;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Map;
import java.util.List;
import java.util.Collections;
import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import static com.badlogic.gdx.Input.Keys;

// Clase principal que maneja la lógica del mapa, el movimiento del jugador y la interacción multijugador
public class ScreenMapaTiled implements Screen, InputProcessor {

    protected final Proyecto game; // Instancia de la clase Proyecto
    private final String initialPokemonName;
    protected Stage stage;
    public Stage uiStage;
    protected OrthographicCamera camera;

    protected ConexionCliente conexion;
    protected boolean isMultiplayer = false;
    protected Map<String, OtherPlayer> otherPlayers = new HashMap<>();
    protected float syncTimer = 0;
    protected static final float SYNC_INTERVAL = 0.05f;
    protected SharedResourceManager sharedResourceManager;
    protected DuelManager duelManager;
    protected Long multiplayerSeed = null;
    private String serverIP = "localhost";

    // Representa a otros jugadores conectados en el mundo multijugador
    public class OtherPlayer {
        public Image actor;
        public String id;

        public OtherPlayer(String id, float x, float y, Direction direction, boolean isMoving) {
            this.id = id;
            TextureRegion frame = (walkDownAnimation != null) ? walkDownAnimation.getKeyFrame(0) : null;
            if (frame == null) {
                Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
                pixmap.setColor(Color.CYAN);
                pixmap.fill();
                frame = new TextureRegion(new Texture(pixmap));
                pixmap.dispose();
            }

            this.actor = new Image(frame);
            this.actor.setSize(2f, 2f);
            this.actor.setPosition(x, y);

            if (stage != null) {
                stage.addActor(this.actor);
                Gdx.app.log("MULTI", "OtherPlayer added to stage: " + id + " at " + x + "," + y);
            } else {
                Gdx.app.error("MULTI", "CRITICAL: Stage is null when creating OtherPlayer " + id);
            }
        }

        public void update(float x, float y, Direction direction, boolean isMoving) {
            this.actor.setPosition(x, y);
            TextureRegion currentFrame = null;

            if (isMoving) {
                switch (direction) {
                    case UP:
                        currentFrame = (walkUpAnimation != null) ? walkUpAnimation.getKeyFrame(stateTime, true) : null;
                        break;
                    case DOWN:
                        currentFrame = (walkDownAnimation != null) ? walkDownAnimation.getKeyFrame(stateTime, true)
                                : null;
                        break;
                    case LEFT:
                        currentFrame = (walkLeftAnimation != null) ? walkLeftAnimation.getKeyFrame(stateTime, true)
                                : null;
                        break;
                    case RIGHT:
                        currentFrame = (walkRightAnimation != null) ? walkRightAnimation.getKeyFrame(stateTime, true)
                                : null;
                        break;
                    default:
                        currentFrame = (walkDownAnimation != null) ? walkDownAnimation.getKeyFrame(stateTime, true)
                                : null;
                        break;
                }
            } else {
                switch (direction) {
                    case UP:
                        currentFrame = (walkUpAnimation != null) ? walkUpAnimation.getKeyFrame(0) : null;
                        break;
                    case DOWN:
                        currentFrame = (walkDownAnimation != null) ? walkDownAnimation.getKeyFrame(0) : null;
                        break;
                    case LEFT:
                        currentFrame = (walkLeftAnimation != null) ? walkLeftAnimation.getKeyFrame(0) : null;
                        break;
                    case RIGHT:
                        currentFrame = (walkRightAnimation != null) ? walkRightAnimation.getKeyFrame(0) : null;
                        break;
                    default:
                        currentFrame = (walkDownAnimation != null) ? walkDownAnimation.getKeyFrame(0) : null;
                        break;
                }
            }

            if (currentFrame != null) {
                this.actor.setDrawable(new TextureRegionDrawable(currentFrame));
            }
        }

        public void remove() {
            if (actor != null)
                actor.remove();
        }
    }

    // Actualiza o crea la representación visual de otro jugador basado en los datos
    // de la red
    public void updateOtherPlayer(String id, float x, float y, String dirStr, boolean moving) {
        Gdx.app.log("MULTI", "Actualizando jugador " + id + " a " + x + "," + y + " dir=" + dirStr + " move=" + moving);
        Direction dir = Direction.DOWN;
        try {
            dir = Direction.valueOf(dirStr);
        } catch (Exception e) {
            dir = Direction.DOWN;
        }

        if (!otherPlayers.containsKey(id)) {
            Gdx.app.log("MULTI", "Creando nuevo actor para " + id);
            OtherPlayer nuevo = new OtherPlayer(id, x, y, dir, moving);
            otherPlayers.put(id, nuevo);
        } else {
            otherPlayers.get(id).update(x, y, dir, moving);
        }
    }

    // Envía la posición y dirección actual del jugador local al servidor
    private void sendMovementUpdate(float x, float y, String dir, boolean moving) {
        String msg = "{"
                + "\"tipo\":\"mover\","
                + "\"id\":\"" + sodyl.proyecto.clases.UserManager.getCurrentUser() + "\","
                + "\"x\":" + x + ","
                + "\"y\":" + y + ","
                + "\"dir\":\"" + dir + "\","
                + "\"moving\":" + moving
                + "}";
        if (conexion != null) {
            conexion.enviarMensaje(msg);
        }
    }

    public void removeOtherPlayer(String id) {
        if (otherPlayers.containsKey(id)) {
            otherPlayers.get(id).remove();
            otherPlayers.remove(id);
        }
    }

    protected SpriteBatch batch;
    protected TextureAtlas atlas;
    protected float stateTime;
    protected Animation<TextureRegion> walkDownAnimation;
    protected Animation<TextureRegion> walkUpAnimation;
    protected Animation<TextureRegion> walkLeftAnimation;
    protected Animation<TextureRegion> walkRightAnimation;

    protected enum Direction {
        DOWN, UP, LEFT, RIGHT
    }

    protected Direction lastDirection = Direction.DOWN;
    protected Image characterActor;

    protected static final float SPEED = 80f;
    protected boolean movingUp, movingDown, movingLeft, movingRight;
    protected static final float UNIT_SCALE = 1 / 8f;

    protected TiledMap map;
    protected OrthogonalTiledMapRenderer renderer;
    protected int mapWidthTiles;
    protected int mapHeightTiles;

    protected Array<Rectangle> collisionRects;
    private static final String COLLISION_LAYER_NAME = "Capa de Objetos 1";
    private static final float COLLISION_PADDING = 0.1f;

    private Array<EncounterZone> encounterZones;

    public Pokemon playerPokemon;
    public boolean canTriggerEncounter = true;
    public float encounterCooldown = 0f;
    private final float ENCOUNTER_COOLDOWN_TIME = 3.0f;

    private final float PLAYER_START_X = 11f;
    private final float PLAYER_START_Y = 38f;

    private Float customSpawnX = null;
    private Float customSpawnY = null;

    private BitmapFont font;

    public enum GameState {
        FREE_ROAMING,
        PAUSED,
        DIALOGUE,
        TRANSITIONING,
        CRAFTING,
        INVENTORY,
        BATTLE,
        POKEDEX,
        POKEDEX_SUBMENU,
        TEAM_SELECTION,
        WAITING_FOR_CHOICE,
        INVENTORY_SUBMENU,
        POKEDEX_ACTION_MENU,
        POKEMON_INFO,
        POKEMONES_MENU,
        TYPES_MENU,
        ATTACKS_MENU
    }

    private float arceusMarkerTimer = 0f;
    private com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer;

    public GameState currentState;

    private DialogBox dialogBox;
    private Queue<String> dialogueQueue = new LinkedList<>();
    private Pokemon pendingEnemyForBattle;
    private final String[] initialDialogs = {
            "¡Hijo hijo hijo! No sabia que merodeabas por la zona. Bienvenido al mundo Pokemon. Soy el Profesor Yoel, te estare guiando a lo largo de toda tu aventura.",
            "En tu recorrido, te integraras con criaturas extrañas llamadas pokemon, tu mision es investigarlas hasta saber todos sobre ellas. Capturar a un pokemon no es tarea sencilla: necesitas debilitarlo en su estado salvaje y luego lanzarle una PokeBall.",
            "Tambien puedes recoger materiales primarios en el mundo, estos se almacenaran de forma inmediata en tu inventario. Ademas, puedes usarlos para craftear otros objetos utiles, como las PokeBalls, ¡Ja, ja, ja!",
            "¡Ahora, elige tu primer Pokemon!"
    };

    private Table pauseMenuTable;
    private TextButton[] pauseMenuButtons;
    private int selectedIndex = 0;

    private float transitionTimer = 0f;
    private final float transitionDuration = 0.5f;
    private boolean isFadingIn = false;

    private Texture blackPixelTexture;

    private Texture yoelTexture;
    private Image yoelImage;
    protected String mapPath;
    private Screen nextScreen;

    public Inventario playerInventory;
    private Table craftingMenuTable;
    private Table inventoryMenuTable;
    private Table pokedexMenuTable;
    private Table pokedexSubmenuTable;
    private Table teamSelectionTable;
    private int selectedSubmenuIndex = 0;
    private int selectedTeamIndex = 0;

    private Array<Recipe> availableRecipes;
    private int selectedRecipeIndex = 0;
    private Label craftingStatusLabel;

    private Map<Integer, Texture> itemTextures = new HashMap<>();
    protected Array<Collectible> collectibles;
    protected Random random;

    private Set<Integer> recordedTiles = new HashSet<>();

    private Texture recepcionistaTexture;
    private Image recepcionistaImage;
    private Table choiceBoxTable;
    private int selectedChoiceIndex = 0;
    private Runnable onDialogCompleteAction;
    private Runnable onYesAction;
    private Runnable onNoAction;

    private String sourceMapBeforePokemonCenter = null;
    private Float returnTileXBeforePokemonCenter = null;
    private Float returnTileYBeforePokemonCenter = null;
    private Texture kaneki1Texture, kaneki2Texture;
    private Image kanekiImage;
    private Texture jesucristo1Texture, jesucristo2Texture;
    private Image jesucristoImage;
    private Texture afton1Texture, afton2Texture;
    private Image aftonImage;
    private Texture pennywise1Texture, pennywise2Texture;
    private Image pennywiseImage;
    private Texture jotaro1Texture, jotaro2Texture;
    private Image jotaroImage;
    private Texture donValerio1Texture, donValerio2Texture;
    private Image donValerioImage;
    private Texture giorno1Texture, giorno2Texture;
    private Image giornoImage;

    private Texture bonnie1Texture, bonnie2Texture;
    private Image bonnieImage;
    private Texture fntFoxy1Texture, fntFoxy2Texture;
    private Image fntFoxyImage;
    private Texture circusBaby1Texture, circusBaby2Texture;
    private Image circusBabyImage;
    private Texture foxy1Texture, foxy2Texture;
    private Image foxyImage;
    private Texture freddy1Texture, freddy2Texture;
    private Image freddyImage;

    private float npcAnimationTimer = 0f;
    private final float NPC_FRAME_DURATION = 0.5f;

    private Table inventorySubmenuTable;

    private int selectedInventorySubmenuIndex = 0;

    private Table pokedexActionMenuTable;
    private int selectedPokedexActionIndex = 0;
    private Table pokemonInfoTable;
    private Pokemon selectedPokemonForHealing = null;

    private Table pokemonesMenuTable;
    private Table typesMenuTable;
    private Table attacksMenuTable;
    private int selectedPokemonesMenuIndex = 0;
    private TextButton[] pokemonesMenuButtons;

    private int lastLogTileX = -1;
    private int lastLogTileY = -1;

    private List<String> recordedTilesList = new ArrayList<>();

    private String pendingBattleBackground = null;

    private static final Set<String> SPECIAL_ENCOUNTER_TILES_MAP1 = new HashSet<>(Arrays.asList(
            "70,34", "70,33", "69,33", "68,33", "67,33", "66,33", "65,33", "64,33", "63,33", "63,32", "64,32", "65,32",
            "66,32", "67,32", "68,32", "69,32", "70,32",
            "70,31", "69,31", "68,31", "67,31", "66,31", "65,31", "64,31", "63,31", "63,30", "64,30", "65,30", "66,30",
            "67,30", "68,30", "69,30", "70,30",
            "70,29", "69,29", "68,29", "67,29", "66,29", "65,29", "64,29", "63,29", "63,28", "64,28", "65,28", "66,28",
            "67,28", "68,28", "69,28", "70,28",
            "83,40", "84,40", "83,39", "84,39", "85,39", "86,39", "83,38", "84,38", "85,38", "86,38", "87,38", "88,38",
            "89,38", "90,38", "90,37", "89,37", "88,37", "87,37", "86,37", "85,37", "84,37",
            "84,23", "84,24", "84,25", "84,26", "84,27", "85,28", "85,27", "85,26", "85,25", "85,24", "85,23", "85,22",
            "85,21", "86,21", "86,22", "86,23", "86,24", "86,25", "86,26", "86,27", "86,28",
            "87,29", "87,28", "87,27", "87,26", "87,25", "87,24", "87,22", "87,21", "88,21", "88,22", "88,23", "88,24",
            "88,25", "88,26", "88,27", "88,28", "88,29",
            "89,29", "89,28", "89,27", "89,26", "89,25", "89,24", "89,23", "89,22", "89,21", "90,21", "90,22", "90,23",
            "90,24", "90,25", "90,26", "90,27", "90,28",
            "91,27", "91,26", "91,25", "91,24", "91,23", "91,22", "84,10", "85,10", "86,10", "87,10", "88,10", "89,10",
            "90,10", "91,10",
            "91,9", "90,9", "89,9", "88,9", "87,9", "86,9", "85,9", "84,9",
            "84,8", "85,8", "86,8", "87,8", "88,8", "89,8", "90,8", "91,8"));

    private static final Set<String> SPECIAL_ENCOUNTER_TILES_MAP1_ZONE2 = new HashSet<>(Arrays.asList(
            "129,57", "129,58", "129,59", "130,59", "130,58", "130,57", "131,57", "131,58", "131,59", "132,59",
            "132,58", "132,57", "133,57", "133,58", "133,59", "133,60", "132,60", "134,60", "134,59", "134,58",
            "134,57", "135,57", "135,56", "136,57", "136,58", "135,58", "135,59", "135,60", "135,61", "135,62",
            "136,62", "136,61", "136,60", "136,59", "136,58", "137,58", "137,59", "137,60", "137,61", "137,62",
            "138,62", "138,61", "138,60", "139,60", "139,61", "139,62", "96,63", "96,62", "96,61", "96,60", "96,59",
            "96,58", "96,57", "96,56", "96,55", "96,54", "97,55", "97,56", "97,57", "97,58", "97,59", "97,60", "97,61",
            "97,62", "97,63", "98,63", "98,62", "98,61", "98,60", "98,59", "98,58", "98,57", "98,56", "98,55", "98,54",
            "98,53", "98,52", "97,51", "96,52", "96,53", "99,51", "98,52", "98,53", "98,54", "98,55", "98,56", "98,57",
            "98,58", "98,59", "98,60", "98,61", "98,62", "99,62", "99,61", "99,60", "99,59", "99,58", "99,57", "99,56",
            "99,55", "99,54", "99,53", "99,52", "99,51", "100,51", "100,52", "100,53", "100,54", "100,55", "100,56",
            "101,51", "101,52", "101,53", "101,54", "101,55", "101,56", "102,57", "102,58", "102,59", "102,60",
            "102,61", "102,62", "101,63", "100,63", "100,62", "100,61", "100,60", "101,60", "101,61", "101,62",
            "103,63", "103,62", "103,61", "103,60", "103,59", "103,58", "103,57", "103,56", "103,55", "103,54",
            "103,53", "103,52", "103,51", "104,52", "104,53", "104,54", "104,55", "104,56", "104,57", "104,58",
            "104,59", "104,60", "104,61", "104,62", "104,63", "105,52", "105,53", "105,54", "105,55", "105,56",
            "105,58", "105,59", "105,60", "105,61", "105,62", "106,62", "106,61", "106,60", "106,59", "106,58",
            "106,57", "106,56", "106,55", "106,54", "106,53", "107,53", "107,54", "107,55", "107,56", "107,57",
            "107,58", "107,59", "107,60", "107,61", "107,62", "107,63", "108,63", "109,63", "110,63", "111,63",
            "112,63", "113,63", "113,62", "112,62", "111,62", "110,62", "110,61", "111,61", "112,61", "113,61",
            "113,60", "113,59", "112,59", "111,59", "110,59", "110,58", "111,58", "112,58", "113,58", "113,57",
            "113,56", "112,56", "111,56", "110,56", "110,55", "111,55", "112,55", "113,55", "113,54", "112,54",
            "111,54", "110,53", "113,53", "114,53", "115,53", "115,54", "115,55", "115,56", "115,57", "115,58",
            "117,58", "117,59", "117,60", "117,61", "117,62", "117,63", "118,63", "119,63", "120,63", "120,62",
            "119,62", "118,61", "119,60", "120,60", "120,59", "119,59", "118,59", "118,58", "119,58", "120,58",
            "120,57", "121,56", "121,55", "121,54", "121,53", "121,52", "121,51", "120,52", "120,53", "119,54",
            "118,54", "118,55", "117,55", "117,56", "118,56", "119,56", "120,56", "120,55", "120,54", "120,53",
            "120,52", "119,52", "118,52", "117,52", "116,52", "117,53", "118,53", "119,53", "118,55", "117,56",
            "116,56", "116,57", "116,58", "114,58", "114,57", "114,56", "114,55", "113,55", "112,55", "111,54"));

    private static final Set<String> SPECIAL_ENCOUNTER_TILES_MAP1_ZONE3 = new HashSet<>(Arrays.asList(
            "26,44", "25,44", "24,44", "23,44", "22,44", "21,44", "20,44", "19,44", "18,44", "17,44",
            "17,45", "16,45", "15,45", "15,44", "14,44", "13,44", "12,44", "11,44", "10,44", "11,45",
            "11,46", "12,46", "13,46", "14,46", "15,46", "16,46", "17,46", "18,46", "19,46", "20,46",
            "21,46", "22,46", "23,46", "24,46", "25,46", "26,46", "27,46", "28,46", "28,47", "27,47",
            "26,47", "25,47", "24,47", "23,47", "22,47", "21,47", "20,47", "19,47", "18,47", "17,47",
            "16,47", "15,47", "14,47", "13,47", "12,47", "11,47", "11,48", "11,49", "11,50", "11,51",
            "11,52", "11,53", "10,54", "9,54", "8,54", "8,55", "8,56", "8,57", "8,58", "8,59",
            "8,60", "8,61", "8,62", "9,63", "10,63", "11,63", "12,63", "13,63", "14,63", "13,62",
            "12,62", "11,62", "10,62", "9,61", "10,60", "11,60", "12,60", "13,60", "11,59", "10,59",
            "10,58", "11,58", "12,58", "11,57", "10,57", "10,56", "11,56", "12,57", "11,55", "10,55",
            "9,55", "9,54", "10,54", "11,54", "11,53", "11,52", "11,51", "11,50", "11,49", "12,46",
            "13,46", "19,45", "20,45", "21,45", "22,45", "23,45", "24,45", "25,45", "26,45", "27,45",
            "28,46", "28,47", "28,48", "28,49", "28,50", "28,51", "28,52", "28,53", "28,54", "28,55",
            "28,56", "28,57", "28,58", "28,59", "28,60", "28,61", "28,62", "29,62", "29,61", "29,60",
            "29,59", "29,58", "29,57", "29,56", "29,55", "29,54", "27,54", "27,55", "27,56", "27,57",
            "27,58", "27,59", "27,60", "26,61", "26,62", "26,63", "25,62", "24,62", "23,62", "22,62",
            "21,62", "20,62", "19,62", "18,62", "17,62", "16,62", "15,62", "14,62", "14,61", "13,61",
            "13,62", "12,63", "11,63", "10,63", "9,62", "10,61", "13,61", "32,62", "32,61", "32,60",
            "32,59", "32,58", "33,58", "33,59", "33,60", "33,61", "33,62", "33,63", "34,58", "34,59",
            "34,60", "34,61", "34,62", "34,63", "35,62", "35,61", "37,61", "37,62", "38,63", "38,62",
            "38,61", "38,60", "39,62", "39,61", "39,60", "39,59", "39,58", "35,58", "36,58", "37,58",
            "38,58", "39,58", "39,59", "38,59", "37,59", "36,59", "35,59", "35,60", "36,60", "37,60",
            "38,60", "39,60", "20,48", "19,48", "19,47", "18,47", "17,47", "16,47", "13,47", "12,47",
            "10,48", "10,49", "10,51", "10,52", "9,52", "9,53", "10,53", "13,59", "13,60", "14,60", "15,61"));

    private static final Set<String> SPECIAL_ENCOUNTER_TILES_MAP1_ZONE4 = new HashSet<>(Arrays.asList(
            "101,14", "101,13", "101,12", "101,11", "101,10", "101,9", "102,9", "102,10", "102,11", "102,12",
            "102,13", "102,14", "103,14", "103,13", "103,12", "103,11", "103,9", "104,9", "104,10", "104,11",
            "104,12", "104,13", "104,14", "105,14", "105,13", "105,12", "105,11", "105,8", "105,9", "105,10",
            "106,13", "106,12", "106,11", "106,10", "106,9", "107,9", "107,10", "107,11", "107,12", "107,13",
            "107,14", "108,14", "108,13", "108,12", "108,11", "108,10", "108,9", "109,9", "109,10", "109,11",
            "109,12", "109,13", "124,14", "125,14", "126,14", "127,14", "128,14", "129,14", "128,15", "127,15",
            "126,15", "125,15", "124,15", "125,16", "125,17", "125,18", "124,18", "125,18", "126,18", "127,18",
            "128,18", "127,17", "126,17", "125,17", "125,16", "126,16", "127,16", "108,26", "108,27", "108,28",
            "108,29", "108,30", "108,31", "108,32", "108,33", "109,34", "109,33", "109,32", "109,31", "109,30",
            "109,29", "109,28", "109,27", "110,27", "110,28", "110,29", "110,30", "110,31", "110,32", "110,33",
            "110,34", "139,26", "139,27", "139,28", "139,29", "139,30", "139,31", "139,32", "139,33", "139,34",
            "140,33", "140,32", "140,31", "140,30", "140,29", "140,28", "140,27", "140,26", "141,26", "141,27",
            "141,28", "141,29", "141,30", "141,31", "141,32", "141,33", "141,34"));

    private static final Set<String> PRIMARY_MATERIAL_TILES_MAP1 = new HashSet<>(Arrays.asList(
            "4,41", "4,42", "4,43", "4,44", "4,45", "5,40", "5,41", "5,42", "5,43", "5,44",
            "5,45", "6,16", "6,17", "6,18", "6,19", "6,20", "6,21", "6,22", "6,40", "6,41",
            "6,42", "6,43", "6,44", "7,16", "7,17", "7,18", "7,19", "7,20", "7,21", "7,22",
            "7,40", "7,41", "8,16", "8,17", "8,18", "8,19", "8,20", "8,21", "8,40", "8,41",
            "9,16", "9,17", "9,18", "9,19", "9,20", "9,21", "9,22", "9,40", "9,41", "10,16",
            "10,17", "10,18", "10,19", "10,20", "10,21", "10,22", "10,23", "10,40", "10,41", "11,16",
            "11,17", "11,18", "11,19", "11,20", "11,21", "11,22", "11,23", "11,40", "11,41", "12,16",
            "12,17", "12,18", "12,19", "12,20", "12,21", "12,22", "12,40", "12,41", "13,16", "13,17",
            "13,18", "13,19", "13,20", "13,21", "13,22", "13,23", "13,40", "13,41", "14,16", "14,17",
            "14,18", "14,19", "14,20", "14,21", "14,22", "14,40", "14,41", "15,16", "15,17", "15,18",
            "15,19", "15,20", "15,21", "15,22", "15,40", "15,41", "16,16", "16,17", "16,18", "16,19",
            "16,20", "16,21", "16,40", "16,41", "17,16", "17,17", "17,22", "17,23", "17,39", "17,40",
            "17,41", "18,16", "18,17", "18,18", "18,22", "18,23", "18,39", "18,40", "18,41", "19,16",
            "19,17", "19,18", "19,22", "19,23", "19,39", "19,40", "20,16", "20,17", "20,18", "20,22",
            "20,23", "20,39", "21,16", "21,17", "21,18", "21,19", "21,22", "21,23", "21,38", "21,39",
            "21,40", "22,17", "22,18", "22,19", "22,20", "22,21", "22,22", "22,23", "22,38", "22,39",
            "22,40", "23,5", "23,6", "23,8", "23,11", "24,5", "24,6", "24,7", "24,8", "24,10",
            "24,11", "24,12", "24,13", "24,14", "24,15", "24,16", "24,17", "24,18", "24,19", "24,20",
            "24,21", "24,22", "24,23", "24,24", "24,25", "24,26", "24,27", "24,28", "24,29", "24,30",
            "24,31", "24,32", "24,33", "24,34", "24,35", "24,36", "24,37", "24,38", "24,39", "25,5",
            "25,6", "25,7", "25,8", "25,10", "25,11", "25,12", "25,13", "25,14", "25,15", "25,16",
            "25,17", "25,18", "25,19", "25,20", "25,21", "25,22", "25,23", "25,24", "25,25", "25,26",
            "25,27", "25,28", "25,29", "25,30", "25,31", "25,32", "25,33", "25,34", "25,35", "25,36",
            "25,37", "25,38", "25,39", "26,5", "26,6", "26,7", "26,8", "26,10", "26,11", "26,12",
            "26,13", "26,14", "26,15", "26,16", "26,17", "26,18", "26,19", "26,20", "26,21", "26,22",
            "26,23", "26,24", "26,25", "26,26", "26,27", "26,28", "26,29", "26,30", "26,31", "26,32",
            "26,33", "26,34", "26,35", "26,36", "26,37", "26,38", "26,39", "27,5", "27,6", "27,7",
            "27,8", "27,10", "27,11", "27,12", "27,13", "27,14", "27,15", "27,16", "27,17", "27,18",
            "27,19", "27,20", "27,21", "27,22", "27,23", "27,24", "27,25", "27,26", "27,27", "27,28",
            "27,29", "27,30", "27,31", "27,32", "27,33", "27,34", "27,35", "27,36", "27,37", "27,38",
            "28,5", "28,6", "28,7", "28,8", "28,10", "28,11", "28,12", "28,13", "28,14", "28,15",
            "28,16", "28,17", "28,18", "28,19", "28,20", "28,21", "28,22", "28,23", "28,24", "28,25",
            "28,26", "28,27", "28,28", "28,29", "28,30", "28,31", "28,32", "28,33", "28,34", "28,35",
            "28,36", "28,37", "28,38", "28,39", "29,15", "29,17", "30,16", "30,17", "30,18", "30,19",
            "30,20", "30,21", "30,22", "30,23", "30,24", "30,25", "30,26", "31,16", "31,17", "31,18",
            "31,23", "31,24", "31,25", "31,26", "32,16", "32,17", "32,18", "32,22", "32,23", "32,24",
            "32,25", "32,26", "33,16", "33,17", "33,18", "33,22", "33,23", "33,24", "33,25", "33,26",
            "34,16", "34,17", "34,22", "34,23", "34,24", "34,25", "34,26", "35,16", "35,17", "35,18",
            "35,22", "35,23", "35,24", "35,25", "35,26", "36,16", "36,18", "36,22", "36,23", "36,24",
            "37,16", "37,18", "37,22", "37,23", "37,24", "37,25", "38,16", "38,17", "38,18", "38,22",
            "38,24", "39,16", "39,17", "39,18", "39,22", "39,23", "40,16", "40,17", "40,18", "40,23",
            "41,15", "41,16", "41,17", "41,18", "41,19", "41,20", "41,21", "41,22", "41,23", "42,16",
            "42,17", "42,18", "42,19", "42,20", "42,21", "42,22", "42,23", "43,17", "43,18", "43,19",
            "43,20", "43,21", "43,22", "43,23", "44,16", "44,17", "44,18", "44,19", "44,20", "44,21",
            "44,22", "44,23", "45,16", "45,18", "45,19", "45,20", "45,22", "45,23", "46,12", "46,13",
            "46,14", "46,15", "46,16", "46,17", "46,18", "46,19", "46,20", "46,21", "46,22", "46,23",
            "46,24", "47,12", "47,13", "47,14", "47,15", "47,16", "47,17", "47,18", "47,19", "47,20",
            "47,21", "47,22", "47,23", "47,24", "47,25", "48,13", "48,14", "48,15", "48,16", "48,17",
            "48,18", "49,13", "49,14", "49,15", "49,16", "49,17", "50,14", "50,15", "50,16", "50,17",
            "50,55", "50,56", "50,57", "50,58", "50,59", "50,60", "50,61", "50,63", "51,13", "51,14",
            "51,15", "51,16", "51,17", "51,55", "51,56", "51,57", "51,58", "51,59", "51,60", "51,61",
            "51,63", "52,13", "52,14", "52,15", "52,16", "52,17", "52,55", "52,56", "52,57", "52,58",
            "52,59", "52,60", "52,61", "52,63", "53,13", "53,14", "53,15", "53,16", "53,17", "53,55",
            "53,56", "53,57", "53,58", "53,59", "53,60", "53,61", "53,63", "54,13", "54,14", "54,15",
            "54,16", "54,17", "54,55", "54,56", "54,57", "54,58", "54,59", "54,60", "54,61", "54,63",
            "55,13", "55,14", "55,15", "55,16", "55,17", "55,55", "55,56", "55,57", "55,58", "56,13",
            "56,14", "56,15", "56,16", "56,17", "56,55", "56,56", "56,57", "56,58", "57,13", "57,14",
            "57,15", "57,16", "57,17", "57,55", "57,56", "57,57", "57,58", "58,13", "58,14", "58,15",
            "58,16", "58,17", "58,55", "58,56", "58,57", "58,58", "58,59", "58,60", "58,61", "58,62",
            "58,63", "59,13", "59,14", "59,15", "59,16", "59,55", "59,56", "59,57", "59,58", "59,59",
            "59,60", "59,61", "59,62", "59,63", "60,13", "60,14", "60,15", "60,16", "60,17", "60,55",
            "60,56", "60,57", "60,62", "60,63", "61,14", "61,15", "61,16", "61,17", "61,53", "61,54",
            "61,55", "61,56", "61,57", "61,62", "61,63", "62,13", "62,14", "62,15", "62,16", "62,17",
            "62,53", "62,54", "62,55", "62,56", "62,57", "62,62", "62,63", "63,13", "63,14", "63,15",
            "63,16", "63,17", "63,20", "63,52", "63,53", "63,54", "63,55", "63,56", "63,57", "63,62",
            "63,63", "64,13", "64,14", "64,15", "64,16", "64,17", "64,18", "64,19", "64,20", "64,51",
            "64,52", "64,53", "64,54", "64,55", "64,56", "64,57", "64,62", "64,63", "65,13", "65,14",
            "65,15", "65,16", "65,17", "65,18", "65,19", "65,20", "65,51", "65,52", "65,53", "65,54",
            "65,55", "65,56", "65,57", "65,62", "65,63", "66,13", "66,14", "66,15", "66,16", "66,17",
            "66,18", "66,19", "66,50", "66,51", "66,52", "66,53", "66,54", "66,55", "66,56", "66,57",
            "66,62", "66,63", "67,14", "67,15", "67,16", "67,17", "67,18", "67,19", "67,46", "67,47",
            "67,48", "67,49", "67,50", "67,51", "67,52", "67,53", "67,54", "67,55", "67,56", "67,57",
            "67,62", "67,63", "68,15", "68,16", "68,17", "68,18", "68,19", "68,46", "68,47", "68,48",
            "68,49", "68,50", "68,51", "68,52", "68,53", "68,54", "68,55", "68,56", "68,57", "68,58",
            "68,59", "68,60", "68,62", "68,63", "69,2", "69,3", "69,4", "69,5", "69,6", "69,7",
            "69,8", "69,9", "69,14", "69,15", "69,16", "69,17", "69,18", "69,19", "69,20", "69,46",
            "69,47", "69,48", "69,49", "69,50", "69,51", "69,52", "69,53", "69,54", "69,55", "69,56",
            "69,57", "69,58", "69,59", "69,60", "69,62", "69,63", "70,2", "70,3", "70,4", "70,5",
            "70,6", "70,7", "70,8", "70,9", "70,10", "70,11", "70,12", "70,14", "70,15", "70,16",
            "70,17", "70,18", "70,19", "70,20", "70,21", "70,47", "70,48", "70,49", "70,50", "70,51",
            "70,52", "70,53", "70,57", "70,58", "70,59", "70,60", "70,62", "70,63", "71,3", "71,4",
            "71,5", "71,6", "71,7", "71,8", "71,9", "71,11", "71,12", "71,13", "71,14", "71,15",
            "71,16", "71,18", "71,19", "71,20", "71,46", "71,47", "71,48", "71,49", "71,50", "71,51",
            "71,52", "71,53", "71,57", "71,58", "71,59", "71,60", "71,62", "71,63", "72,2", "72,3",
            "72,4", "72,5", "72,6", "72,7", "72,8", "72,11", "72,13", "72,14", "72,15", "72,16",
            "72,17", "72,18", "72,19", "72,20", "72,21", "72,22", "72,23", "72,43", "72,44", "72,47",
            "72,48", "72,49", "72,50", "72,51", "72,52", "72,53", "72,56", "72,57", "72,58", "72,59",
            "72,60", "72,62", "72,63", "73,2", "73,3", "73,4", "73,5", "73,6", "73,7", "73,8",
            "73,9", "73,11", "73,12", "73,13", "73,14", "73,15", "73,16", "73,17", "73,18", "73,19",
            "73,20", "73,21", "73,22", "73,23", "73,24", "73,25", "73,26", "73,27", "73,28", "73,29",
            "73,30", "73,31", "73,32", "73,33", "73,34", "73,35", "73,36", "73,37", "73,38", "73,39",
            "73,40", "73,41", "73,42", "73,43", "73,46", "73,47", "73,48", "73,49", "73,50", "73,51",
            "73,52", "73,53", "73,54", "73,55", "73,56", "73,57", "73,58", "73,59", "73,60", "73,62",
            "73,63", "74,2", "74,3", "74,4", "74,5", "74,6", "74,7", "74,8", "74,9", "74,10",
            "74,11", "74,13", "74,14", "74,15", "74,16", "74,17", "74,18", "74,19", "74,20", "74,21",
            "74,22", "74,25", "74,26", "74,27", "74,28", "74,29", "74,30", "74,31", "74,32", "74,34",
            "74,35", "74,36", "74,38", "74,39", "74,40", "74,41", "74,42", "74,43", "74,44", "74,46",
            "74,47", "74,48", "74,49", "74,50", "74,51", "74,52", "74,53", "74,54", "74,56", "74,57",
            "74,58", "74,62", "74,63", "75,8", "75,9", "75,10", "75,13", "75,14", "75,19", "75,20",
            "75,21", "75,22", "75,23", "75,24", "75,25", "75,26", "75,27", "75,28", "75,29", "75,30",
            "75,47", "75,48", "75,49", "75,50", "75,52", "75,53", "75,54", "75,55", "75,56", "75,57",
            "75,58", "75,62", "75,63", "76,8", "76,9", "76,10", "76,11", "76,12", "76,13", "76,14",
            "76,15", "76,19", "76,20", "76,21", "76,22", "76,23", "76,24", "76,25", "76,26", "76,27",
            "76,28", "76,29", "76,30", "76,47", "76,48", "76,49", "76,50", "76,51", "76,52", "76,53",
            "76,54", "76,55", "76,56", "76,57", "76,58", "76,59", "76,61", "76,62", "76,63", "77,8",
            "77,9", "77,10", "77,12", "77,14", "77,15", "77,19", "77,20", "77,21", "77,22", "77,23",
            "77,24", "77,25", "77,26", "77,27", "77,28", "77,29", "77,46", "77,47", "77,48", "77,49",
            "77,50", "77,51", "77,52", "77,56", "77,57", "77,58", "77,59", "77,61", "77,62", "77,63",
            "78,8", "78,9", "78,10", "78,12", "78,13", "78,14", "78,15", "78,18", "78,19", "78,20",
            "78,21", "78,22", "78,23", "78,24", "78,25", "78,26", "78,27", "78,28", "78,29", "78,47",
            "78,48", "78,49", "78,50", "78,51", "78,52", "78,56", "78,57", "78,63", "79,8", "79,9",
            "79,10", "79,13", "79,14", "79,15", "79,16", "79,17", "79,18", "79,19", "79,20", "79,21",
            "79,22", "79,23", "79,24", "79,25", "79,26", "79,27", "79,28", "79,29", "79,46", "79,47",
            "79,48", "79,49", "79,50", "80,8", "80,9", "80,10", "80,15", "80,16", "80,17", "80,18",
            "80,19", "80,20", "80,21", "80,22", "80,23", "80,24", "80,25", "80,26", "80,48", "80,49",
            "80,50", "80,51", "81,8", "81,9", "81,10", "81,16", "81,17", "81,18", "81,19", "81,20",
            "81,21", "81,22", "81,23", "81,24", "81,25", "81,26", "81,27", "81,48", "81,49", "81,50",
            "81,51", "82,8", "82,9", "82,10", "82,15", "82,16", "82,17", "82,18", "82,19", "82,20",
            "82,21", "82,22", "82,23", "82,24", "82,25", "82,26", "82,27", "82,48", "82,49", "82,50",
            "82,51", "82,52", "83,18", "83,19", "83,20", "83,28", "83,29", "83,30", "83,48", "83,49",
            "83,50", "83,51", "84,17", "84,18", "84,19", "84,29", "84,30", "84,48", "84,49", "84,50",
            "84,51", "85,17", "85,18", "85,19", "85,29", "85,30", "85,31", "85,48", "85,49", "85,50",
            "85,51", "86,17", "86,18", "86,19", "86,30", "86,31", "86,48", "86,49", "86,50", "86,51",
            "87,17", "87,18", "87,19", "87,30", "87,31", "87,48", "87,49", "87,50", "87,51", "88,17",
            "88,18", "88,19", "88,31", "88,48", "88,49", "88,50", "88,51", "89,17", "89,18", "89,19",
            "89,48", "89,49", "89,50", "89,51", "90,17", "90,18", "90,19", "90,48", "90,49", "90,50",
            "90,51", "91,17", "91,18", "91,19", "91,20", "91,48", "91,49", "91,50", "91,51", "92,17",
            "92,18", "92,19", "92,20", "92,21", "92,48", "92,49", "92,50", "92,51", "93,17", "93,18",
            "93,19", "93,21", "93,48", "93,49", "93,50", "93,51", "94,17", "94,18", "94,21", "94,48",
            "94,49", "94,50", "94,51", "95,17", "95,18", "95,19", "95,20", "95,21", "95,48", "95,49",
            "96,17", "96,18", "96,20", "96,21", "96,48", "96,49", "96,51", "97,17", "97,19", "97,21",
            "97,48", "97,49", "98,17", "98,18", "98,19", "98,20", "98,21", "98,48", "98,49", "99,48",
            "99,49", "101,19", "101,20", "101,21", "101,22", "101,23", "101,24", "101,25", "102,18", "102,19",
            "102,20", "102,21", "102,22", "102,23", "102,24", "102,25", "102,26", "103,19", "103,20", "103,21",
            "103,22", "103,23", "103,24", "103,25", "103,26", "104,17", "104,18", "104,19", "104,20", "104,21",
            "104,22", "104,23", "104,24", "104,25", "104,26", "105,17", "105,18", "105,19", "105,20", "105,21",
            "105,22", "105,23", "105,24", "105,25", "105,26", "106,17", "106,18", "106,19", "106,20", "106,21",
            "106,22", "106,23", "106,24", "106,25", "107,18", "107,19", "107,20", "107,21", "107,22", "107,23",
            "107,24", "107,25", "107,26", "108,19", "108,20", "108,21", "108,22", "108,23", "108,24", "109,18",
            "109,19", "109,20", "109,21", "109,22", "109,23", "109,24", "109,25", "110,18", "110,19", "110,20",
            "110,21", "110,22", "110,23", "110,24", "110,25", "111,15", "111,16", "111,18", "111,19", "111,20",
            "111,21", "111,22", "111,23", "111,25", "111,26", "112,16", "112,17", "112,18", "112,19", "112,20",
            "112,21", "112,22", "112,23", "112,24", "112,25", "112,26", "113,17", "113,18", "113,19", "113,20",
            "114,19", "114,20", "114,21", "114,22", "114,23", "114,24", "114,25", "114,26", "115,19", "115,20",
            "115,21", "115,22", "115,23", "115,24", "115,25", "115,26", "116,24", "116,25", "117,24", "117,25",
            "118,24", "118,25", "119,24", "119,25", "120,24", "120,25", "121,24", "121,25", "122,20", "122,21",
            "122,22", "122,23", "122,24", "122,25", "122,28", "122,29", "122,30", "123,20", "123,21", "123,22",
            "123,23", "123,24", "123,25", "123,26", "123,27", "123,28", "123,29", "123,30", "124,21", "124,22",
            "124,23", "124,24", "124,25", "124,26", "124,28", "124,29", "124,30", "125,20", "125,21", "125,22",
            "125,23", "125,24", "125,25", "125,26", "125,28", "125,29", "125,30", "126,20", "126,21", "126,22",
            "126,23", "126,24", "126,25", "126,26", "126,28", "126,29", "126,30", "127,20", "127,21", "127,22",
            "127,23", "127,24", "127,25", "127,26", "127,28", "127,29", "127,30", "128,20", "128,21", "128,22",
            "128,23", "128,24", "128,25", "128,26", "128,28", "128,29", "128,30", "129,21", "129,22", "129,23",
            "129,24", "129,25", "129,27", "129,28", "129,29", "130,21", "130,22", "130,23", "130,24", "130,25",
            "131,21", "131,22", "131,23", "131,24", "131,25", "132,20", "132,21", "132,22", "132,23", "132,24",
            "132,25", "133,20", "133,21", "133,22", "133,23", "133,24", "133,25", "134,20", "134,21", "134,22",
            "134,23", "134,24", "134,25", "134,26", "135,20", "135,21", "135,22", "135,23", "135,24", "135,25",
            "135,26", "136,21", "136,22", "136,23", "136,24", "136,25", "136,26", "137,19", "137,20", "137,21",
            "137,22", "137,23", "137,24", "137,25", "138,18", "138,19", "138,20", "138,21", "138,22", "138,23",
            "138,24", "138,25", "138,26", "139,16", "139,17", "139,18", "139,19", "139,20", "139,21", "139,22",
            "139,23", "139,24", "139,25", "140,18", "140,19", "140,20", "140,21", "140,22", "140,23", "140,24",
            "140,25", "141,17", "141,18", "141,19", "141,20", "141,21", "141,22", "141,23", "141,24", "141,25",
            "142,17", "142,18", "142,19", "142,20", "142,21", "142,22", "142,23", "142,24", "142,25", "142,26",
            "143,17", "143,18", "143,19", "143,20", "143,21", "143,22", "143,23", "143,24", "143,25", "144,18",
            "144,19", "144,20", "144,21", "144,22", "144,23", "144,24", "144,25", "144,26", "145,17", "145,19",
            "145,20", "145,21", "145,22", "145,23", "145,24", "145,25"));

    public ScreenMapaTiled(Proyecto game) {
        this(game, "Mapa/MAPACOMPLETO.tmx", null, null, null, GameState.DIALOGUE);
    }

    public ScreenMapaTiled(Proyecto game, String initialPokemonName) {
        this(game, "Mapa/MAPACOMPLETO.tmx", null, null, initialPokemonName, GameState.DIALOGUE);
    }

    public ScreenMapaTiled(Proyecto game, boolean isMultiplayer) {
        this(game, "localhost", isMultiplayer);
    }

    public ScreenMapaTiled(Proyecto game, String serverIP, boolean isMultiplayer) {
        this(game);
        this.serverIP = (serverIP == null || serverIP.isEmpty()) ? "localhost" : serverIP;
        this.isMultiplayer = isMultiplayer;
        if (isMultiplayer) {
            this.currentState = GameState.FREE_ROAMING;
            initNetworking();
        }
    }

    public ScreenMapaTiled(Proyecto game, boolean isMultiplayer, long seed) {
        this(game, "localhost", isMultiplayer, seed);
    }

    public ScreenMapaTiled(Proyecto game, String serverIP, boolean isMultiplayer, long seed) {
        this(game);
        this.serverIP = (serverIP == null || serverIP.isEmpty()) ? "localhost" : serverIP;
        this.isMultiplayer = isMultiplayer;
        this.multiplayerSeed = seed;
        if (isMultiplayer) {
            this.currentState = GameState.FREE_ROAMING;
            initNetworking();
        }
    }

    public ScreenMapaTiled(Proyecto game, String mapPath, Inventario inventory, Pokemon playerPokemon,
            String initialPokemonName, GameState initialState) {
        this(game, mapPath, inventory, playerPokemon, initialPokemonName, initialState, null, null, null, null, null);
    }

    public ScreenMapaTiled(Proyecto game, String mapPath, Inventario inventory, Pokemon playerPokemon,
            String initialPokemonName, GameState initialState, Float spawnX, Float spawnY) {
        this(game, mapPath, inventory, playerPokemon, initialPokemonName, initialState, spawnX, spawnY, null, null,
                null);
    }

    public ScreenMapaTiled(Proyecto game, String mapPath, Inventario inventory, Pokemon playerPokemon,
            String initialPokemonName, GameState initialState, Float spawnX, Float spawnY, String sourceMap) {
        this(game, mapPath, inventory, playerPokemon, initialPokemonName, initialState, spawnX, spawnY, sourceMap, null,
                null);
    }

    public ScreenMapaTiled(Proyecto game, String mapPath, Inventario inventory, Pokemon playerPokemon,
            String initialPokemonName, GameState initialState, Float spawnX, Float spawnY, String sourceMap,
            Float returnX, Float returnY) {
        this.game = game;
        this.mapPath = mapPath;
        this.currentState = initialState;
        this.initialPokemonName = initialPokemonName;
        this.customSpawnX = spawnX;
        this.customSpawnY = spawnY;
        this.sourceMapBeforePokemonCenter = sourceMap;
        this.returnTileXBeforePokemonCenter = returnX;
        this.returnTileYBeforePokemonCenter = returnY;

        if (inventory != null)
            this.playerInventory = inventory;
        if (playerPokemon != null)
            this.playerPokemon = playerPokemon;

        if (this.isMultiplayer) {
            initNetworking();
        }
    }

    // Inicializa la conexión de red y define los observadores para mensajes y
    // duelos
    private void initNetworking() {
        Gdx.app.log("NETWORK", "Iniciando red en IP: " + serverIP);
        conexion = new ConexionCliente(serverIP, 5000, message -> {
            Gdx.app.postRunnable(() -> handleNetworkMessage(message));
        });

        sharedResourceManager = new SharedResourceManager();
        duelManager = new DuelManager(conexion, new DuelManager.DuelUIListener() {
            @Override
            public void onDuelRequested(String challengerId) {
                Gdx.app.postRunnable(() -> {
                    dialogueQueue.clear();
                    dialogueQueue.add("¡" + challengerId + " te ha retado a un duelo!");
                    dialogueQueue.add("¿Aceptas el desafío?");
                    currentState = GameState.DIALOGUE;
                    dialogBox.setText(dialogueQueue.poll());
                    onDialogCompleteAction = () -> {
                        showYesNoChoice("Aceptar", "Rechazar", () -> {
                            duelManager.acceptDuel(challengerId, playerPokemon);
                        }, () -> {
                        });
                    };
                });
            }

            @Override
            public void onDuelAccepted() {
                Gdx.app.postRunnable(() -> {
                    dialogueQueue.clear();
                    dialogueQueue.add("¡Duelo aceptado! ¡Prepárate!");
                    currentState = GameState.DIALOGUE;
                    dialogBox.setText(dialogueQueue.poll());
                });
            }

            @Override
            public void onDuelUpdate(String log) {
                Gdx.app.postRunnable(() -> {
                    if (dialogueQueue == null)
                        dialogueQueue = new LinkedList<>();
                    dialogueQueue.add(log);
                    if (currentState != GameState.DIALOGUE) {
                        currentState = GameState.DIALOGUE;
                        if (dialogBox != null)
                            dialogBox.setText(dialogueQueue.poll());
                    }
                });
            }

            @Override
            public void onDuelEnd(boolean won) {
                Gdx.app.postRunnable(() -> {
                    if (won) {
                        dialogueQueue.add("¡Has ganado el duelo!");
                        dialogueQueue.add("Tu " + playerPokemon.getEspecie() + " gana +1 punto de progreso.");
                    } else {
                        dialogueQueue.add("Has perdido el duelo...");
                    }
                    currentState = GameState.DIALOGUE;
                    if (dialogBox != null && !dialogueQueue.isEmpty())
                        dialogBox.setText(dialogueQueue.poll());
                });
            }
        });

        conexion.conectar();
    }

    // Procesa los mensajes JSON recibidos del servidor para actualizar el estado
    // del mundo multijugador
    private void handleNetworkMessage(String message) {
        try {
            JsonValue root = new JsonReader().parse(message);
            String tipo = root.getString("tipo", "");
            String id = root.getString("id", "");

            if (id.equals(sodyl.proyecto.clases.UserManager.getCurrentUser()))
                return;

            Gdx.app.log("NETWORK", "Mensaje recibido: " + tipo + " de " + id);

            switch (tipo) {

                case "player_join": {
                    float x = root.getFloat("x");
                    float y = root.getFloat("y");
                    updateOtherPlayer(id, x, y, "DOWN", false);
                    break;
                }

                case "mover": {
                    float x = root.getFloat("x");
                    float y = root.getFloat("y");
                    String dirStr = root.getString("dir", "DOWN");
                    boolean moving = root.getBoolean("moving", false);

                    updateOtherPlayer(id, x, y, dirStr, moving);
                    break;
                }

                case "desconectar": {
                    removeOtherPlayer(id);
                    break;
                }
                default:
                    if (tipo.startsWith("duel_")) {
                        String sender = root.has("id") ? root.getString("id")
                                : (root.has("target") ? root.getString("target") : "");
                        float dmg = root.has("damage") ? root.getFloat("damage") : 0;
                        duelManager.processMessage(tipo, sender, dmg);
                    }

                    else if ("item_collected".equals(tipo)) {
                        String itemId = root.getString("itemId", "");
                        if (!itemId.isEmpty()) {
                            sharedResourceManager.markResourceDepleted(itemId);

                            for (Collectible c : collectibles) {
                                String cId = mapPath + "_" + (int) c.getActor().getX() + "_"
                                        + (int) c.getActor().getY();
                                if (cId.equals(itemId) && !c.isCollected()) {
                                    c.setCollected(true);
                                    Gdx.app.log("MULTI", "Recurso agotado por otro jugador: " + itemId);
                                }
                            }
                        }
                    }
                    break;
            }

        } catch (Exception e) {
            Gdx.app.error("NETWORK", "Error parsing message: " + message, e);
        }
    }

    private Animation<TextureRegion> createAnimation(String rowPrefix, float frameDuration) {
        Array<TextureAtlas.AtlasRegion> frames = new Array<>();
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

    private Texture createColoredTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    // Configura y carga todos los elementos necesarios cuando se muestra la
    // pantalla del mapa
    @Override
    public void show() {
        if (mapPath.contains("Centro Pokemon interior")) {
            game.playMusic("musica/rosalina.mp3");
        } else {
            game.playMusic("musica/mainMusic.mp3");
        }

        if (isMultiplayer) {
            initNetworking();
        }

        if (batch != null && stage != null && camera != null && renderer != null) {
            if (currentState == GameState.BATTLE) {
                currentState = GameState.FREE_ROAMING;
            }

            Gdx.input.setInputProcessor(null);

            InputMultiplexer multiplexer = new InputMultiplexer();
            multiplexer.addProcessor(this);
            if (uiStage != null) {
                multiplexer.addProcessor(uiStage);
            }
            Gdx.input.setInputProcessor(multiplexer);

            if (characterActor != null && camera != null) {
                clampCamera();
                camera.update();
            }
            if (stage != null && stage.getViewport() != null) {
                stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
            }
            if (uiStage != null && uiStage.getViewport() != null) {
                uiStage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
            }

            clearMovementKeys();

            Gdx.app.log("MAP_SCREEN", "Pantalla del mapa restaurada después de batalla. Estado: " + currentState);
            if (game.getScreen() != this) {
                return;
            }
            return;
        }

        batch = new SpriteBatch();
        shapeRenderer = new com.badlogic.gdx.graphics.glutils.ShapeRenderer();
        camera = new OrthographicCamera();

        if (isMultiplayer && multiplayerSeed != null) {
            random = new Random(multiplayerSeed);
            Gdx.app.log("RANDOM", "Inicializado con semilla compartida: " + multiplayerSeed);
        } else {
            random = new Random();
        }

        if (playerInventory == null) {
            String currentUser = sodyl.proyecto.clases.UserManager.getCurrentUser();
            Gdx.app.log("MAP_SCREEN", "Carga inicial. Usuario actual: " + currentUser);
            playerInventory = new Inventario();
            playerInventory.load(currentUser);
            Gdx.app.log("MAP_SCREEN",
                    "Carga de inventario completa. Tamaño: " + playerInventory.getAllObjetos().size());
        }

        // Cargar Pokedex
        sodyl.proyecto.clases.Pokedex.load();

        // Cargar font
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("Mapa/ari-w9500-bold.ttf"));

        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = 20; // Tamaño para dialogos
        parameter.color = Color.WHITE;
        // Incluir caracteres en español
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "áéíóúÁÉÍÓÚñÑ¿¡";

        font = generator.generateFont(parameter);
        generator.dispose();

        try {
            TmxMapLoader.Parameters params = new TmxMapLoader.Parameters();
            params.textureMinFilter = Texture.TextureFilter.Nearest;
            params.textureMagFilter = Texture.TextureFilter.Nearest;

            map = new TmxMapLoader().load(mapPath, params);
            renderer = new OrthogonalTiledMapRenderer(map, UNIT_SCALE);
            mapWidthTiles = map.getProperties().get("width", Integer.class);
            mapHeightTiles = map.getProperties().get("height", Integer.class);
        } catch (Exception e) {
            Gdx.app.error("MAPA", "Error al cargar el mapa Tiled. Verifica la ruta o el archivo .tmx.", e);
            mapWidthTiles = 50;
            mapHeightTiles = 50;
        }

        // Carga de colisiones
        collisionRects = new Array<>();

        String[] possibleCollisionLayerNames = { "Capa de Objetos 1", "Colisiones", "COLISIONES" };
        MapLayer collisionObjectLayer = null;

        for (String targetName : possibleCollisionLayerNames) {
            for (MapLayer layer : map.getLayers()) {
                if (layer.getName() != null && layer.getName().equalsIgnoreCase(targetName)) {
                    if (!(layer instanceof com.badlogic.gdx.maps.MapGroupLayer)
                            && !(layer instanceof com.badlogic.gdx.maps.tiled.TiledMapTileLayer)) {
                        collisionObjectLayer = layer;
                        Gdx.app.log("COLLISION", "Capa de OBJETOS de colisión encontrada: " + layer.getName());
                        break;
                    }
                }
            }
            if (collisionObjectLayer != null)
                break;
        }

        if (collisionObjectLayer != null) {
            for (MapObject object : collisionObjectLayer.getObjects()) {
                if (object instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) object).getRectangle();

                    Rectangle scaledRect = new Rectangle(
                            rect.x * UNIT_SCALE,
                            rect.y * UNIT_SCALE,
                            rect.width * UNIT_SCALE,
                            rect.height * UNIT_SCALE);

                    float offset = COLLISION_PADDING / 2.0f;
                    float reducedWidth = scaledRect.width - COLLISION_PADDING;
                    float reducedHeight = scaledRect.height - COLLISION_PADDING;

                    Rectangle finalRect = new Rectangle(
                            scaledRect.x + offset,
                            scaledRect.y + offset,
                            reducedWidth,
                            reducedHeight);

                    collisionRects.add(finalRect);
                } else if (object instanceof PolygonMapObject) {
                    Gdx.app.log("COLLISION", "Objeto de colisión poligonal encontrado, omitiendo por ahora");
                }
            }
            Gdx.app.log("COLLISION", "Cargados " + collisionRects.size + " rectángulos de colisión");
        } else {
            Gdx.app.log("COLLISION", "No se encontró capa de OBJETOS de colisión en el mapa");
        }

        if (mapPath != null && mapPath.contains("MAPACOMPLETO.tmx")) {
            removeCollisionsAt(38, 37);
            removeCollisionsAt(39, 37);

            for (int y = 17; y <= 21; y++) {
                addCollisionAt(0, y);
            }

        }

        // Configuración de animaciones y personaje
        TextureRegion initialFrame;
        try {
            atlas = new TextureAtlas(Gdx.files.internal("sprites/jugador/Textures.atlas"));
            for (Texture t : atlas.getTextures()) {
                t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            }
            float frameDuration = 0.15f;
            walkDownAnimation = createAnimation("r1", frameDuration);
            walkLeftAnimation = createAnimation("r2", frameDuration);
            walkRightAnimation = createAnimation("r3", frameDuration);
            walkUpAnimation = createAnimation("r4", frameDuration);
            initialFrame = walkDownAnimation.getKeyFrame(0);
        } catch (Exception e) {
            Gdx.app.error("ATLAS", "Error al cargar TextureAtlas. Usando respaldo rojo.");
            Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.RED);
            pixmap.fill();
            Texture errorTexture = new Texture(pixmap);
            pixmap.dispose();
            initialFrame = new TextureRegion(errorTexture);
        }

        characterActor = new Image(initialFrame);
        characterActor.setSize(2f, 2f);

        float startX = PLAYER_START_X;
        float startY = PLAYER_START_Y;

        if (customSpawnX != null && customSpawnY != null) {
            startX = customSpawnX;
            startY = customSpawnY;
        } else if (mapPath.contains("Centro Pokemon interior")) {
            startX = 11f;
            startY = 4f;
        } else if (mapPath != null && mapPath.contains("MAPACOMPLETO.tmx")) {
            float tileWidth = map.getProperties().get("tilewidth", Integer.class);
            float tileHeight = map.getProperties().get("tileheight", Integer.class);
            startX = 51 * tileWidth * UNIT_SCALE;
            startY = 16 * tileHeight * UNIT_SCALE;
        }

        characterActor.setPosition(startX, startY);

        if (isMultiplayer && conexion != null) {
            String myId = sodyl.proyecto.clases.UserManager.getCurrentUser();
            String joinMsg = "{"
                    + "\"tipo\":\"player_join\","
                    + "\"id\":\"" + myId + "\","
                    + "\"x\":" + startX + ","
                    + "\"y\":" + startY
                    + "}";
            conexion.enviarMensaje(joinMsg);
        }

        stage = new Stage(new FitViewport(30, 25, camera));

        uiStage = new Stage(new FitViewport(Proyecto.PANTALLA_W, Proyecto.PANTALLA_H));

        Pixmap pixmapOverlay = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmapOverlay.setColor(Color.BLACK);
        pixmapOverlay.fill();
        blackPixelTexture = new Texture(pixmapOverlay);
        pixmapOverlay.dispose();

        try {
            yoelTexture = new Texture(Gdx.files.internal("imagenes/yoel.png"));
            yoelImage = new Image(yoelTexture);
        } catch (Exception e) {
            Gdx.app.error("ASSETS", "No se pudo cargar yoel.png", e);
        }

        Objeto.initializeObjetos();
        Pokemones.initialize();

        availableRecipes = new Array<>(Objeto.getAllRecipes().values().toArray(new Recipe[0]));

        loadItemTextures();

        if (playerPokemon == null) {
            java.util.List<Pokemon> team = sodyl.proyecto.clases.Pokedex.getTeam();

            if (this.initialPokemonName != null) {
                for (Pokemon p : team) {
                    if (p.getEspecie().equals(this.initialPokemonName)) {
                        playerPokemon = p;
                        break;
                    }
                }
                if (playerPokemon == null) {
                    playerPokemon = Pokemones.getPokemon(this.initialPokemonName);
                    if (playerPokemon != null) {
                        playerPokemon.setNivel(0);
                        playerPokemon.actualizarAtributos();
                    }
                }
            } else {
                if (!team.isEmpty()) {
                    playerPokemon = team.get(0);
                } else {
                    playerPokemon = Pokemones.getPokemon("Rowlet");
                    playerPokemon.setNivel(0);
                    playerPokemon.actualizarAtributos();
                    playerPokemon.setActualHP(playerPokemon.getMaxHp());
                }
            }
        }

        // Registrar en la Pokedex y dar items iniciales solo si es un juego nuevo y se
        // ha elegido un pokemon explícito
        if (currentState == GameState.DIALOGUE) {
            if (this.initialPokemonName != null) {
                try {
                    sodyl.proyecto.clases.Pokedex.addSeen(playerPokemon.getEspecie());
                    sodyl.proyecto.clases.Pokedex.addCollected(playerPokemon);
                } catch (Exception ignored) {
                }

                // Dar 5 Pokéballs al jugador al inicio
                playerInventory.addObjeto(101, 5);
            }
        }

        if (mapPath.contains("Centro Pokemon interior")) {
            try {
                recepcionistaTexture = new Texture(Gdx.files.internal("imagenes/recepcionista1.png"));
                recepcionistaImage = new Image(recepcionistaTexture);
                float tileWidth = map.getProperties().get("tilewidth", Integer.class);
                float tileHeight = map.getProperties().get("tileheight", Integer.class);

                float npcX = 2 * tileWidth * UNIT_SCALE;
                float npcY = 4 * tileHeight * UNIT_SCALE;

                recepcionistaImage.setSize(2f, 2f);
                recepcionistaImage.setPosition(npcX, npcY);
                stage.addActor(recepcionistaImage);

                Rectangle npcRect = new Rectangle(npcX + 1.0f, npcY, 1.6f, 1.6f);
                collisionRects.add(npcRect);

            } catch (Exception e) {
                Gdx.app.error("ASSETS", "No se pudo cargar recepcionista.png", e);
            }
        }

        encounterZones = new Array<>();

        loadEncounterZones();
        if (game.getMapCollectibles().containsKey(mapPath)) {
            collectibles = game.getMapCollectibles().get(mapPath);
            Gdx.app.log("COLLECTIBLES",
                    "Cargados " + collectibles.size + " coleccionables persistentes para " + mapPath);
        } else {
            collectibles = new Array<>();
            game.getMapCollectibles().put(mapPath, collectibles);
            if (!(mapPath != null && mapPath.contains("MAPACOMPLETO.tmx"))) {
                spawnCollectiblesOnGrassTiles(100);
            } else {
                spawnPrimaryMaterialsInZones(20);
            }
            Gdx.app.log("COLLECTIBLES", "Generados " + collectibles.size + " nuevos coleccionables para " + mapPath);
        }

        for (Collectible c : collectibles) {
            if (!c.isCollected()) {
                stage.addActor(c.getActor());
            }
        }

        stage.addActor(characterActor);

        if (otherPlayers != null) {
            for (OtherPlayer op : otherPlayers.values()) {
                if (op.actor.getStage() == null) {
                    stage.addActor(op.actor);
                }
                if (walkDownAnimation != null && op.actor.getDrawable() == null) {
                    op.actor.setDrawable(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                            walkDownAnimation.getKeyFrame(0)));
                }
            }
        }

        initializeDialogueNPCs();
        initializePauseMenu();
        initializeInventoryMenu();
        initializeCraftingMenu();
        initializePokedexMenu();
        initializePokemonesMenu();

        initializeInventorySubmenu();

        initializePokedexActionMenu();

        initializePokemonInfoTable();

        initializeChoiceBox();

        if (dialogBox == null) {
            dialogBox = new DialogBox(uiStage, font);
        }

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(this);
        multiplexer.addProcessor(uiStage);

        if (currentState == GameState.DIALOGUE && !isMultiplayer && mapPath != null
                && mapPath.contains("MAPACOMPLETO.tmx")) {
            startProfessorYoelAnimation();
        }

        Gdx.input.setInputProcessor(multiplexer);

        camera.setToOrtho(false, Proyecto.PANTALLA_W * UNIT_SCALE, Proyecto.PANTALLA_H * UNIT_SCALE);
        camera.viewportWidth = 30;
        camera.viewportHeight = 25;

        clampCamera();
        loadBattleNPCs();
        camera.update();

    }

    // Cargar los NPCs de combate
    private void loadBattleNPCs() {
        if (mapPath == null || !mapPath.contains("MAPACOMPLETO.tmx"))
            return;

        kaneki1Texture = new Texture(Gdx.files.internal("spritesMapa2/kaneki1.png"));
        kaneki1Texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        kaneki2Texture = new Texture(Gdx.files.internal("spritesMapa2/kaneki2.png"));
        kaneki2Texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        kanekiImage = new Image(kaneki1Texture);
        kanekiImage.setSize(45 * UNIT_SCALE, 24 * UNIT_SCALE);
        kanekiImage.setPosition(41 * 16 * UNIT_SCALE, 38 * 16 * UNIT_SCALE);
        stage.addActor(kanekiImage);

        jesucristo1Texture = new Texture(Gdx.files.internal("spritesMapa4/jesucristo1.png"));
        jesucristo1Texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        jesucristo2Texture = new Texture(Gdx.files.internal("spritesMapa4/jesucristo2.png"));
        jesucristo2Texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        jesucristoImage = new Image(jesucristo1Texture);
        jesucristoImage.setSize(45 * UNIT_SCALE, 24 * UNIT_SCALE);
        jesucristoImage.setPosition(50 * 16 * UNIT_SCALE, 50 * 16 * UNIT_SCALE);
        stage.addActor(jesucristoImage);

        afton1Texture = new Texture(Gdx.files.internal("spritesMapa4/afton1.png"));
        afton1Texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        afton2Texture = new Texture(Gdx.files.internal("spritesMapa4/afton2.png"));
        afton2Texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        aftonImage = new Image(afton1Texture);
        aftonImage.setSize(45 * UNIT_SCALE, 24 * UNIT_SCALE);
        aftonImage.setPosition(86 * 16 * UNIT_SCALE, 51 * 16 * UNIT_SCALE);
        stage.addActor(aftonImage);

        pennywise1Texture = new Texture(Gdx.files.internal("spritesMapa3/pennywise1.png"));
        pennywise1Texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pennywise2Texture = new Texture(Gdx.files.internal("spritesMapa3/pennywise2.png"));
        pennywise2Texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pennywiseImage = new Image(pennywise1Texture);
        pennywiseImage.setSize(45 * UNIT_SCALE, 24 * UNIT_SCALE);
        pennywiseImage.setPosition(99 * 16 * UNIT_SCALE, 26 * 16 * UNIT_SCALE);
        stage.addActor(pennywiseImage);

        jotaro1Texture = new Texture(Gdx.files.internal("spritesMapa2/jotaro1.png"));
        jotaro1Texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        jotaro2Texture = new Texture(Gdx.files.internal("spritesMapa2/jotaro2.png"));
        jotaro2Texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        jotaroImage = new Image(jotaro1Texture);
        jotaroImage.setSize(45 * UNIT_SCALE, 24 * UNIT_SCALE);
        jotaroImage.setPosition(10 * 16 * UNIT_SCALE, 27 * 16 * UNIT_SCALE);
        stage.addActor(jotaroImage);

        donValerio1Texture = new Texture(Gdx.files.internal("spritesMapa3/donValerio1.png"));
        donValerio1Texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        donValerio2Texture = new Texture(Gdx.files.internal("spritesMapa3/donValerio2.png"));
        donValerio2Texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        donValerioImage = new Image(donValerio1Texture);
        donValerioImage.setSize(45 * UNIT_SCALE, 24 * UNIT_SCALE);
        donValerioImage.setPosition(143 * 16 * UNIT_SCALE, 22 * 16 * UNIT_SCALE);
        stage.addActor(donValerioImage);

        giorno1Texture = new Texture(Gdx.files.internal("spritesMapa2/giorno1.png"));
        giorno1Texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        giorno2Texture = new Texture(Gdx.files.internal("spritesMapa2/giorno2.png"));
        giorno2Texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        giornoImage = new Image(giorno1Texture);
        giornoImage.setSize(45 * UNIT_SCALE, 24 * UNIT_SCALE);
        giornoImage.setPosition(27 * 16 * UNIT_SCALE, 38 * 16 * UNIT_SCALE);
        stage.addActor(giornoImage);

        // Rectángulos de colisión
        collisionRects.add(new Rectangle(41 * 16 * UNIT_SCALE, 38 * 16 * UNIT_SCALE, 16 * UNIT_SCALE, 16 * UNIT_SCALE));
        collisionRects.add(new Rectangle(51 * 16 * UNIT_SCALE, 50 * 16 * UNIT_SCALE, 16 * UNIT_SCALE, 16 * UNIT_SCALE));
        collisionRects.add(new Rectangle(87 * 16 * UNIT_SCALE, 51 * 16 * UNIT_SCALE, 16 * UNIT_SCALE, 16 * UNIT_SCALE));
        collisionRects
                .add(new Rectangle(100 * 16 * UNIT_SCALE, 26 * 16 * UNIT_SCALE, 16 * UNIT_SCALE, 16 * UNIT_SCALE));
        collisionRects.add(new Rectangle(10 * 16 * UNIT_SCALE, 27 * 16 * UNIT_SCALE, 16 * UNIT_SCALE, 16 * UNIT_SCALE));
        collisionRects
                .add(new Rectangle(144 * 16 * UNIT_SCALE, 22 * 16 * UNIT_SCALE, 16 * UNIT_SCALE, 16 * UNIT_SCALE));
        collisionRects.add(new Rectangle(28 * 16 * UNIT_SCALE, 38 * 16 * UNIT_SCALE, 16 * UNIT_SCALE, 16 * UNIT_SCALE));
    }

    // Cargar los NPC de diálogo
    private void initializeDialogueNPCs() {
        if (!(mapPath != null && mapPath.contains("MAPACOMPLETO.tmx")))
            return;

        bonnie1Texture = new Texture(Gdx.files.internal("spritesMapa2/bonnie1.png"));
        bonnie2Texture = new Texture(Gdx.files.internal("spritesMapa2/bonnie2.png"));
        bonnieImage = new Image(bonnie1Texture);
        bonnieImage.setSize(45 * UNIT_SCALE, 24 * UNIT_SCALE);
        bonnieImage.setPosition(30 * 16 * UNIT_SCALE, 26 * 16 * UNIT_SCALE);
        stage.addActor(bonnieImage);
        collisionRects.add(new Rectangle(31 * 16 * UNIT_SCALE, 26 * 16 * UNIT_SCALE, 16 * UNIT_SCALE, 16 * UNIT_SCALE));

        fntFoxy1Texture = new Texture(Gdx.files.internal("imagenes/fntFoxy1.png"));
        fntFoxy2Texture = new Texture(Gdx.files.internal("imagenes/fntFoxy2.png"));
        fntFoxyImage = new Image(fntFoxy1Texture);
        fntFoxyImage.setSize(45 * UNIT_SCALE, 24 * UNIT_SCALE);
        fntFoxyImage.setPosition(64 * 16 * UNIT_SCALE, 26 * 16 * UNIT_SCALE);
        stage.addActor(fntFoxyImage);
        collisionRects.add(new Rectangle(65 * 16 * UNIT_SCALE, 26 * 16 * UNIT_SCALE, 16 * UNIT_SCALE, 16 * UNIT_SCALE));

        circusBaby1Texture = new Texture(Gdx.files.internal("imagenes/CircusBaby.png"));
        circusBaby2Texture = new Texture(Gdx.files.internal("imagenes/CircusBaby2.png"));
        circusBabyImage = new Image(circusBaby1Texture);
        circusBabyImage.setSize(45 * UNIT_SCALE, 24 * UNIT_SCALE);
        circusBabyImage.setPosition(75 * 16 * UNIT_SCALE, 30 * 16 * UNIT_SCALE);
        stage.addActor(circusBabyImage);
        collisionRects.add(new Rectangle(76 * 16 * UNIT_SCALE, 30 * 16 * UNIT_SCALE, 16 * UNIT_SCALE, 16 * UNIT_SCALE));

        foxy1Texture = new Texture(Gdx.files.internal("spritesMapa4/foxy1.png"));
        foxy2Texture = new Texture(Gdx.files.internal("spritesMapa4/foxy2.png"));
        foxyImage = new Image(foxy1Texture);
        foxyImage.setSize(45 * UNIT_SCALE, 24 * UNIT_SCALE);
        foxyImage.setPosition(77 * 16 * UNIT_SCALE, 50 * 16 * UNIT_SCALE);
        stage.addActor(foxyImage);
        collisionRects.add(new Rectangle(78 * 16 * UNIT_SCALE, 50 * 16 * UNIT_SCALE, 16 * UNIT_SCALE, 16 * UNIT_SCALE));

        freddy1Texture = new Texture(Gdx.files.internal("spritesMapa3/freddy1.png"));
        freddy2Texture = new Texture(Gdx.files.internal("spritesMapa3/freddy2.png"));
        freddyImage = new Image(freddy1Texture);
        freddyImage.setSize(45 * UNIT_SCALE, 24 * UNIT_SCALE);
        freddyImage.setPosition(93 * 16 * UNIT_SCALE, 20 * 16 * UNIT_SCALE);
        stage.addActor(freddyImage);
        collisionRects.add(new Rectangle(94 * 16 * UNIT_SCALE, 20 * 16 * UNIT_SCALE, 16 * UNIT_SCALE, 16 * UNIT_SCALE));
    }

    private void loadItemTextures() {
        for (Objeto item : Objeto.getAllObjects().values()) {
            Texture texture = new Texture(Gdx.files.internal(item.getTexturePath()));
            itemTextures.put(item.getId(), texture);
        }
    }

    // Define las zonas donde pueden aparecer pokemones (tiles registrados en la
    // declaración, al inicio de la clase)
    private void loadEncounterZones() {
        if (map == null) {
            Gdx.app.error("ENCOUNTER", "El mapa no está cargado. No se pueden cargar zonas de encuentro.");
            return;
        }

        TiledMapTileLayer flowerLayer = null;
        for (int i = 0; i < map.getLayers().getCount(); i++) {
            MapLayer layer = map.getLayers().get(i);
            if (layer.getName() != null && layer.getName().equals("NIvel 1")) {
                if (layer instanceof TiledMapTileLayer) {
                    flowerLayer = (TiledMapTileLayer) layer;
                    break;
                }
            }
        }

        if (flowerLayer == null) {
            Gdx.app.log("ENCOUNTER", "No se encontró la capa 'Nivel 1'. Buscando por índice 1...");
            MapLayer layer = map.getLayers().get(1);
            if (layer instanceof TiledMapTileLayer) {
                flowerLayer = (TiledMapTileLayer) layer;
            }
        }

        if (flowerLayer == null) {
            Gdx.app.error("ENCOUNTER",
                    "No se encontró la capa 'Nivel 1' como TiledMapTileLayer. Se crearán zonas por defecto.");
            createDefaultEncounterZones();
            return;
        }

        final int TILE_WIDTH = map.getProperties().get("tilewidth", Integer.class);
        final int TILE_HEIGHT = map.getProperties().get("tileheight", Integer.class);
        final List<Integer> FLOWER_TILE_IDS = List.of(154);

        int flowerCount = 0;
        for (int y = 0; y < flowerLayer.getHeight(); y++) {
            for (int x = 0; x < flowerLayer.getWidth(); x++) {
                Cell cell = flowerLayer.getCell(x, y);

                if (cell != null && cell.getTile() != null) {
                    int tileId = cell.getTile().getId();
                    if (FLOWER_TILE_IDS.isEmpty() || FLOWER_TILE_IDS.contains(tileId)) {
                        float worldX = x * TILE_WIDTH * UNIT_SCALE;
                        float worldY = y * TILE_HEIGHT * UNIT_SCALE;
                        float zoneWidth = TILE_WIDTH * UNIT_SCALE;
                        float zoneHeight = TILE_HEIGHT * UNIT_SCALE;

                        EncounterZone zone = new EncounterZone(
                                worldX,
                                worldY,
                                zoneWidth,
                                zoneHeight,
                                "Flor_" + flowerCount);
                        zone.addPokemon("Rowlet");
                        zone.addPokemon("Cyndaquil");
                        zone.addPokemon("Oshawott");
                        zone.setEncounterRate(0.3f);

                        encounterZones.add(zone);
                        flowerCount++;
                    }
                }
            }
        }

        if (encounterZones.size == 0) {
            Gdx.app.log("ENCOUNTER", "No se encontraron tiles de flores. Creando zonas por defecto.");
            createDefaultEncounterZones();
        } else {
            Gdx.app.log("ENCOUNTER", "Se cargaron " + encounterZones.size + " zonas de encuentro en tiles de flores.");
            if (flowerLayer.getHeight() > 0 && flowerLayer.getWidth() > 0) {
                Cell sampleCell = flowerLayer.getCell(0, 0);
                if (sampleCell != null && sampleCell.getTile() != null) {

                }
            }
        }
    }

    private void createDefaultEncounterZones() {
        float mapWidth = mapWidthTiles * map.getProperties().get("tilewidth", Integer.class) * UNIT_SCALE;
        float mapHeight = mapHeightTiles * map.getProperties().get("tileheight", Integer.class) * UNIT_SCALE;

        // Zona central del mapa
        EncounterZone defaultZone = new EncounterZone(
                mapWidth * 0.2f,
                mapHeight * 0.2f,
                mapWidth * 0.6f,
                mapHeight * 0.6f,
                "Zona_Default");
        defaultZone.addPokemon("Rowlet");
        defaultZone.addPokemon("Cyndaquil");
        defaultZone.addPokemon("Oshawott");
        defaultZone.setEncounterRate(0.3f);

        encounterZones.add(defaultZone);
    }

    // Verifica si el jugador se encuentra en un tile donde aparezca un pokemón
    private void checkEncounterZones() {
        if (!canTriggerEncounter || currentState != GameState.FREE_ROAMING) {
            return;
        }

        final List<Integer> VALID_POKEMON_TILES = List.of(
                158, 159, 242, 153, 245, 156, 247, 244, 93, 4, 94, 5, 152, 240, 241, 157, 243, 246, 154, 155,
                5592, 5593, 5594, 5685, 5596, 5597, 5598, 5599, 5687, 5686, 5682, 5681, 5680);

        float playerX = characterActor.getX() + characterActor.getWidth() / 2;
        float playerY = characterActor.getY() + characterActor.getHeight() / 2;

        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("NIvel 1");
        if (layer == null) {
            layer = (TiledMapTileLayer) map.getLayers().get("Nivel 1");
        }
        if (layer == null) {
            if (map.getLayers().getCount() > 1) {
                MapLayer mapLayer = map.getLayers().get(1);
                if (mapLayer instanceof TiledMapTileLayer) {
                    layer = (TiledMapTileLayer) mapLayer;
                }
            }
        }

        if (layer == null) {
            return;
        }

        int tileX = (int) (playerX / (layer.getTileWidth() * UNIT_SCALE));
        int tileY = (int) (playerY / (layer.getTileHeight() * UNIT_SCALE));

        if (mapPath != null && mapPath.contains("MAPACOMPLETO.tmx")) {
            if (!checkTeamHealth()) {
                Gdx.app.log("ENCOUNTER_DEBUG", "No hay Pokémon con salud para encuentros. Debes curarlos.");
                return;
            }

            String coordKey = tileX + "," + tileY;

            if (SPECIAL_ENCOUNTER_TILES_MAP1.contains(coordKey)) {
                if (random.nextFloat() < 0.05f) {

                    startSpecialBattle(1);
                    return;
                }
            } else if (SPECIAL_ENCOUNTER_TILES_MAP1_ZONE2.contains(coordKey)) {
                if (random.nextFloat() < 0.05f) {

                    startSpecialBattle(2);
                    return;
                }
            } else if (SPECIAL_ENCOUNTER_TILES_MAP1_ZONE3.contains(coordKey)) {
                if (random.nextFloat() < 0.05f) {

                    startSpecialBattle(3);
                    return;
                }
            } else if (SPECIAL_ENCOUNTER_TILES_MAP1_ZONE4.contains(coordKey)) {
                if (random.nextFloat() < 0.05f) {

                    startSpecialBattle(4);
                    return;
                }
            }
        }

        Cell cell = layer.getCell(tileX, tileY);

        if (cell != null && cell.getTile() != null) {
            int tileId = cell.getTile().getId();

            if (VALID_POKEMON_TILES.contains(tileId)) {
                if (!checkTeamHealth())
                    return;
                if (random.nextFloat() < 0.02f) {

                    startBattle(null);
                }
            }
        }
    }

    // Inicia una batalla con un pokemón
    private void startBattle(EncounterZone zone) {
        if (playerPokemon == null) {
            return;
        }

        List<String> possiblePokemon = getSpawnablePokemon();

        if (possiblePokemon.isEmpty()) {
            possiblePokemon.add("Rowlet");
        }

        String enemyPokemonName = possiblePokemon.get(random.nextInt(possiblePokemon.size()));
        Pokemon enemyPokemon = Pokemones.getPokemon(enemyPokemonName);

        if (enemyPokemon == null) {
            Gdx.app.error("BATTLE", "No se pudo crear el Pokémon: " + enemyPokemonName);
            return;
        }

        enemyPokemon.setNivel(2);
        enemyPokemon.actualizarAtributos();
        enemyPokemon.setActualHP(enemyPokemon.getMaxHp());

        String msg = "¡Un " + enemyPokemon.getEspecie() + " salvaje ha aparecido!";

        this.pendingEnemyForBattle = enemyPokemon;

        if (dialogueQueue == null)
            dialogueQueue = new LinkedList<>();
        dialogueQueue.clear();
        dialogueQueue.add(msg);
        dialogBox.setText(dialogueQueue.poll());
        currentState = GameState.DIALOGUE;
    }

    private void startSpecialBattle(int zoneNumber) {
        if (playerPokemon == null)
            return;

        canTriggerEncounter = false;
        encounterCooldown = 0f;

        // Aquí se establecen los % de aparición de los pokemones
        List<String> pool = new ArrayList<>();
        if (zoneNumber == 1) {
            addCopies(pool, "Rowlet", 45);
            addCopies(pool, "Ivysaur", 40);
            addCopies(pool, "Pikachu", 35);
            addCopies(pool, "Serperior", 25);
            pendingBattleBackground = "Mapa/fondoBatalla.jpg";
        } else if (zoneNumber == 2) {
            addCopies(pool, "Cyndaquil", 45);
            addCopies(pool, "Flareon", 40);
            addCopies(pool, "Charizard", 25);
            addCopies(pool, "Gyarados", 20);
            pendingBattleBackground = "Mapa/fondoBatalla3.png";
        } else if (zoneNumber == 3) {
            addCopies(pool, "Oshawott", 45);
            addCopies(pool, "Vaporeon", 40);
            addCopies(pool, "Sylveon", 30);
            addCopies(pool, "Blastoise", 25);
            pendingBattleBackground = "Mapa/fondoBatalla2.png.png";
        } else if (zoneNumber == 4) {
            addCopies(pool, "Flareon", 30);
            addCopies(pool, "Ivysaur", 30);
            addCopies(pool, "Vaporeon", 30);
            addCopies(pool, "Jolteon", 30);
            pendingBattleBackground = "Mapa/fondoBatalla5.png";
        }

        String enemyPokemonName = pool.get(random.nextInt(pool.size()));
        Pokemon enemyPokemon = Pokemones.getPokemon(enemyPokemonName);

        if (enemyPokemon == null)
            return;
        enemyPokemon.setNivel(2);
        enemyPokemon.actualizarAtributos();
        enemyPokemon.setActualHP(enemyPokemon.getMaxHp());

        String msg = "¡Un " + enemyPokemon.getEspecie() + " salvaje ha aparecido!";
        this.pendingEnemyForBattle = enemyPokemon;

        if (dialogueQueue == null)
            dialogueQueue = new LinkedList<>();
        dialogueQueue.clear();
        dialogueQueue.add(msg);
        dialogBox.setText(dialogueQueue.poll());
        currentState = GameState.DIALOGUE;
    }

    private List<String> getSpawnablePokemon() {
        List<String> pool = new ArrayList<>();

        if (mapPath != null && mapPath.contains("MAPACOMPLETO.tmx")) {
            addCopies(pool, "Rowlet", 100);
            addCopies(pool, "Ivysaur", 30);
            addCopies(pool, "Pikachu", 30);
            addCopies(pool, "Serperior", 5);

        } else {
            addCopies(pool, "Rowlet", 50);
            addCopies(pool, "Oshawott", 50);
        }

        return pool;
    }

    private void addCopies(List<String> pool, String name, int count) {
        for (int i = 0; i < count; i++)
            pool.add(name);
    }

    // Verifica la salud de un pokemón, esto se hizo porq si se entraba a una
    // batalla con un pokemón debilitado, este se recuperaba al 100%
    private boolean checkTeamHealth() {
        if (playerPokemon != null && playerPokemon.getActualHP() > 0) {
            return true;
        }

        java.util.List<Pokemon> team = sodyl.proyecto.clases.Pokedex.getTeam();
        for (Pokemon p : team) {
            if (p != null && p.getActualHP() > 0) {
                this.playerPokemon = p;
                Gdx.app.log("BATTLE", "Auto-cambio a: " + p.getEspecie());
                return true;
            }
        }

        return false;
    }

    private void proceedWithPendingBattle() {
        if (this.pendingEnemyForBattle == null)
            return;

        if (!checkTeamHealth()) {
            if (dialogueQueue == null)
                dialogueQueue = new LinkedList<>();
            dialogueQueue.clear();
            dialogueQueue.add("¡Error! Todos tus Pokémon están debilitados.");
            dialogueQueue.add("No puedes iniciar la batalla.");

            this.pendingEnemyForBattle = null;
            this.canTriggerEncounter = false;
            this.encounterCooldown = 0f;

            currentState = GameState.DIALOGUE;
            processNextDialogueLine();
            return;
        }

        Pokemon enemyPokemon = this.pendingEnemyForBattle;
        this.pendingEnemyForBattle = null;

        canTriggerEncounter = false;
        encounterCooldown = 0f;
        currentState = GameState.BATTLE;
        clearMovementKeys();
        Gdx.input.setInputProcessor(null);

        if (playerPokemon == null) {
            Gdx.app.error("BATTLE", "No hay Pokémon del jugador para iniciar la batalla.");
            currentState = GameState.FREE_ROAMING;
            canTriggerEncounter = true;
            return;
        }

        String backgroundPath;
        if (pendingBattleBackground != null) {
            backgroundPath = pendingBattleBackground;
            pendingBattleBackground = null;
        } else {
            backgroundPath = "Mapa/fondoBatalla.jpg";
        }

        Gdx.app.log("BATTLE", "Iniciando batalla: " + playerPokemon.getEspecie() + " vs " + enemyPokemon.getEspecie()
                + " (Fondo: " + backgroundPath + ")");
        try {
            game.setScreen(new ScreenBatalla(game, this, playerPokemon, enemyPokemon, playerInventory, false, false,
                    this.mapPath, backgroundPath, currentBattleNPC != null));
        } catch (Exception e) {
            e.printStackTrace();
            currentState = GameState.FREE_ROAMING;
            canTriggerEncounter = true;
        }
    }

    // Iniciar una batalla con un NPC
    private void startNPCBattle(String npcName, String pokemonName) {
        if (!checkTeamHealth()) {
            if (dialogueQueue == null)
                dialogueQueue = new LinkedList<>();
            dialogueQueue.clear();
            dialogueQueue.add("¡Error! Todos tus Pokémon están debilitados.");
            dialogueQueue.add("No puedes luchar contra " + npcName + " en este estado.");
            currentState = GameState.DIALOGUE;
            processNextDialogueLine();
            return;
        }

        Pokemon enemyPokemon = Pokemones.getPokemon(pokemonName);

        if (enemyPokemon == null) {
            Gdx.app.error("NPC_BATTLE", "No se pudo crear el Pokemon " + pokemonName);
            return;
        }

        if (dialogueQueue == null)
            dialogueQueue = new LinkedList<>();
        dialogueQueue.clear();

        if (npcName.equals("Jotaro") || npcName.equals("Don Valerio")) {
            dialogBox.setBorderColor(com.badlogic.gdx.graphics.Color.BLUE);
        } else if (npcName.equals("Giorno") || npcName.equals("Jesucristo")) {
            dialogBox.setBorderColor(com.badlogic.gdx.graphics.Color.YELLOW);
        } else if (npcName.equals("Kaneki") || npcName.equals("Pennywise")) {
            dialogBox.setBorderColor(com.badlogic.gdx.graphics.Color.RED);
        } else if (npcName.equals("William Afton")) {
            dialogBox.setBorderColor(com.badlogic.gdx.graphics.Color.PURPLE);
        }

        // Aquí se establecen los diálogos que dice cada NPC antes de una batalla
        if (npcName.equals("Kaneki")) {
            enemyPokemon.setNivel(5);
            dialogueQueue.add("Kaneki: ... ¿Alguna vez has sentido la verdadera tragedia en tus huesos?");
            dialogueQueue.add("Kaneki: Este mundo está equivocado... podrido hasta la médula.");
            dialogueQueue.add("Kaneki: No vine aquí para pelear por odio, sino por la amarga necesidad de sobrevivir.");
            dialogueQueue
                    .add("Kaneki: Mi Charizard ha visto el abismo conmigo. ¿Estás listo para enfrentar la oscuridad?");
            dialogueQueue.add("Kaneki: ¡No tendré piedad con tu debilidad! ¡Aprende lo que es el dolor!");
        } else if (npcName.equals("Jesucristo")) {
            enemyPokemon.setNivel(5);
            dialogueQueue.add("Jesucristo: Bienaventurados los que buscan la paz en medio de la tormenta...");
            dialogueQueue.add(
                    "Jesucristo: Pero hoy, hijo mío, el destino requiere que ponga a prueba la fuerza de tu espíritu.");
            dialogueQueue.add("Jesucristo: No temas a la derrota, pues cada caída es una lección divina para el alma.");
            dialogueQueue.add(
                    "Jesucristo: Mi Lucario pelea con fe y rectitud. ¿Está tu corazón preparado para este juicio?");
            dialogueQueue.add("Jesucristo: ¡Que la luz eterna ilumine nuestro campo de batalla! ¡AMÉN!");
        } else if (npcName.equals("William Afton")) {
            enemyPokemon.setNivel(5);
            dialogueQueue.add("William Afton: ¿De verdad crees que unas simples criaturas pueden detenerme? ¡ILUSO!");
            dialogueQueue.add(
                    "William Afton: He escapado de la muerte tantas veces que ya he perdido la cuenta. ¡SIEMPRE VUELVO!");
            dialogueQueue.add(
                    "William Afton: He visto horrores que harían que tu frágil cordura se desvaneciera en un instante.");
            dialogueQueue.add(
                    "William Afton: Tus Pokémon no son más que herramientas... piezas de mi gran legado mecánico.");
            dialogueQueue.add("William Afton: ¡Es hora de que formes parte de mi experimento final! ¡JAJAJAJA!");
        } else if (npcName.equals("Pennywise")) {
            enemyPokemon.setNivel(5);
            dialogueQueue.add("Pennywise: ¡Hola, pequeño! ¡Qué Pokémon tan bonito tienes! ¿Quieres un globo?");
            dialogueQueue.add("Pennywise: ¡Todos flotamos aquí abajo! Y pronto... ¡tú también lo harás!");
            dialogueQueue.add("Pennywise: Tu miedo... ¡Oh, es tan dulce! Puedo saborearlo vibrando en el aire...");
            dialogueQueue.add("Pennywise: ¿Sabes lo que pasa cuando dejas de correr? El verdadero juego comienza.");
            dialogueQueue.add("Pennywise: ¡Ven a jugar con Pennywise! ¡No te dolerá... mucho!");
        } else if (npcName.equals("Jotaro")) {
            enemyPokemon.setNivel(5);
            dialogueQueue.add("Jotaro: Yare yare daze... otro entrenador novato con ganas de fastidiar.");
            dialogueQueue.add("Jotaro: No necesito moverme de mi sitio para aplastar tus ambiciones de un solo golpe.");
            dialogueQueue
                    .add("Jotaro: Mi Blastoise no conoce la palabra derrota, tiene la misma resolución que mi alma.");
            dialogueQueue.add("Jotaro: Has cometido un grave error al cruzarte en mi camino... un error imperdonable.");
            dialogueQueue.add("Jotaro: ¡Prepárate para recibir un castigo estrepitoso! ¡ORA ORA ORA ORA ORA!");
        } else if (npcName.equals("Don Valerio")) {
            enemyPokemon.setNivel(5);
            dialogueQueue.add("Don Valerio: ... El viento susurra tu llegada. ¿Qué buscas en este santuario olvidado?");
            dialogueQueue.add(
                    "Don Valerio: Mis guardianas feéricas te enseñarán el respeto que le debes a la madre tierra.");
            dialogueQueue.add(
                    "Don Valerio: La naturaleza es sabia y generosa, pero también implacable con los corazones impuros.");
            dialogueQueue
                    .add("Don Valerio: El brillo de mi Sylveon guiará tu alma hacia la redención o hacia el olvido.");
            dialogueQueue.add("Don Valerio: ¡Que la danza de los espíritus del bosque comience ahora mismo!");
        } else if (npcName.equals("Giorno")) {
            enemyPokemon.setNivel(5);
            dialogueQueue.add("Giorno: Yo, Giorno Giovanna, tengo un sueño... y no permitiré que nadie lo ensucie.");
            dialogueQueue.add("Giorno: Mi sueño es traer justicia y orden a este mundo que se ha perdido en el caos.");
            dialogueQueue.add("Giorno: Mi Serperior posee la elegancia y la resolución de un verdadero monarca.");
            dialogueQueue
                    .add("Giorno: No busco la pelea, pero si eres un obstáculo para mi destino, deberé eliminarte.");
            dialogueQueue
                    .add("Giorno: ¡Siente la vitalidad de la justicia fluyendo en cada ataque! ¡MUDA MUDA MUDA MUDA!");
        } else {
            enemyPokemon.setNivel(5);
            dialogueQueue.add(npcName + " quiere luchar!");
        }

        enemyPokemon.actualizarAtributos();
        enemyPokemon.setActualHP(enemyPokemon.getMaxHp());
        currentBattleNPC = npcName;
        clearMovementKeys();
        if (!dialogueQueue.isEmpty()) {
            dialogBox.setText(dialogueQueue.poll());
        }

        this.pendingEnemyForBattle = enemyPokemon;
        this.pendingBattleBackground = "Mapa/fondoBatalla.jpg";
        currentState = GameState.DIALOGUE;
    }

    private void showArceusChallenge(String npcName, String message) {
        if (dialogueQueue == null)
            dialogueQueue = new LinkedList<>();
        dialogueQueue.clear();

        if (npcName.equals("Jotaro") || npcName.equals("Don Valerio")) {
            dialogBox.setBorderColor(com.badlogic.gdx.graphics.Color.BLUE);
        } else if (npcName.equals("Giorno") || npcName.equals("Jesucristo")) {
            dialogBox.setBorderColor(com.badlogic.gdx.graphics.Color.YELLOW);
        } else if (npcName.equals("Kaneki") || npcName.equals("Pennywise")) {
            dialogBox.setBorderColor(com.badlogic.gdx.graphics.Color.RED);
        } else if (npcName.equals("William Afton")) {
            dialogBox.setBorderColor(com.badlogic.gdx.graphics.Color.PURPLE);
        }

        dialogueQueue.add(npcName + ": " + message);

        currentState = GameState.DIALOGUE;
        if (!dialogueQueue.isEmpty()) {
            dialogBox.setText(dialogueQueue.poll());
        }
    }

    private void startDialogueNPCInteraction(String npcName) {
        if (dialogueQueue == null)
            dialogueQueue = new LinkedList<>();
        dialogueQueue.clear();

        if (npcName.equals("Bonnie")) {
            dialogBox.setBorderColor(com.badlogic.gdx.graphics.Color.BLUE);
        } else if (npcName.equals("Freddy")) {
            dialogBox.setBorderColor(com.badlogic.gdx.graphics.Color.YELLOW);
        } else if (npcName.equals("Foxy")) {
            dialogBox.setBorderColor(com.badlogic.gdx.graphics.Color.RED);
        } else if (npcName.equals("Circus Baby") || npcName.equals("Funtime Foxy")) {
            dialogBox.setBorderColor(com.badlogic.gdx.graphics.Color.PURPLE);
        }

        // Aquí se establecen os diálogos de los NPC
        if (npcName.equals("Bonnie")) {
            dialogueQueue.add("Bonnie: ¡Brrr! ¡Vaya frío hace por aquí! ¿No te parece fascinante este páramo helado?");
            dialogueQueue.add(
                    "Bonnie: Este es el mapa de nieve... un lugar tan hermoso como traicionero si no estás bien abrigado.");
            dialogueQueue.add(
                    "Bonnie: He visto a Oshawott chapoteando en el hielo y a Sylveon buscando bayas entre la nieve.");
            dialogueQueue
                    .add("Bonnie: También dicen que Blastoise y Vaporeon se esconden en las cuevas más profundas...");
            dialogueQueue.add(
                    "Bonnie: Mantente alerta, joven entrenador. Los NPC de combate aquí no son tan amables como yo.");
            dialogueQueue
                    .add("Bonnie: ¡Muchos son fanáticos obsesivos de personajes que solo existen en leyendas o anime!");
        } else if (npcName.equals("Funtime Foxy")) {
            dialogueQueue
                    .add("Funtime Foxy: ¡Bienvenidos damas y caballeros, niños y niñas, al ESPECTÁCULO del COMBATE!");
            dialogueQueue.add(
                    "Funtime Foxy: ¡No hay nada como la adrenalina de una batalla bajo los reflectores de la victoria!");
            dialogueQueue.add(
                    "Funtime Foxy: En la batalla, puedes seleccionar LUCHAR para atacar al pokemón enemigo, eligiendo un ataque; MOCHILA para capturar, potenciar o curar al pokemón; POKEMON para cambiar de combatiente dentro de tu equipo pokemón; HUIR para escapar del combate. ");
            dialogueQueue.add(
                    "Funtime Foxy: ¿Ves a alguien que encaje en tu equipo? ¡Lánzale una PokeBall desde tu inventario!");
            dialogueQueue.add(
                    "Funtime Foxy: ¡Recuerda, el público exige emoción! ¡Haz que cada movimiento cuente en el escenario!");
            dialogueQueue.add("Funtime Foxy: ¡Que comience la función! ¡IT'S SHOWTIME!");
        } else if (npcName.equals("Circus Baby")) {
            dialogueQueue.add(
                    "Circus Baby: Acércate... el mundo ha cambiado drásticamente en los últimos tiempos, ¿no crees?");
            dialogueQueue.add(
                    "Circus Baby: Desde desiertos abrasadores hasta cumbres que tocan el cielo, hay mucho que explorar.");
            dialogueQueue.add(
                    "Circus Baby: Se rumorea que Rowlet e Ivysaur prefieren la maleza, mientras Pikachu corre por los campos.");
            dialogueQueue.add(
                    "Circus Baby: Pero ten cuidado... el cansancio es un enemigo silencioso que puede llevarte a la derrota.");
            dialogueQueue.add(
                    "Circus Baby: No olvides que los Centros Pokémon son refugios de paz en este mundo tan caótico.");
            dialogueQueue.add("Circus Baby: Cuida de tus amigos, y ellos cuidarán de ti cuando las luces se apaguen.");
        } else if (npcName.equals("Foxy")) {
            dialogueQueue.add("Foxy: ¡Arrr, grumete! ¡Saca el pecho y prepárate para el calor que se avecina!");
            dialogueQueue.add(
                    "Foxy: Si sigues a la derecha, llegarás a las tierras del fuego, donde la lava fluye como el ron.");
            dialogueQueue.add(
                    "Foxy: Allí verás a Cyndaquil y Flareon bailando entre las llamas, ¡es un espectáculo digno de ver!");
            dialogueQueue.add(
                    "Foxy: ¡Incluso dicen que el gran Charizard y el temible Gyarados rondan esas peligrosas aguas térmicas!");
            dialogueQueue.add(
                    "Foxy: Pero no te confíes, pequeño marinero... los enemigos allí son más duros que el casco de un bergantín.");
            dialogueQueue.add(
                    "Foxy: ¡Solo un verdadero capitán con un equipo fuerte puede conquistar el volcán y salir con vida!");
        } else if (npcName.equals("Freddy")) {
            dialogueQueue.add(
                    "Freddy: Saludos, joven investigador. Es un placer ver caras nuevas explorando estos territorios.");
            dialogueQueue.add(
                    "Freddy: Como experto local, he documentado avistamientos de Flareon, Vaporeon y Jolteon muy cerca de aquí.");
            dialogueQueue.add(
                    "Freddy: Parece que la diversidad de especies ha aumentado gracias a las nuevas corrientes de energía.");
            dialogueQueue.add(
                    "Freddy: Mi consejo es simple: habla con todos los que encuentres, cada NPC tiene un fragmento de sabiduría.");
            dialogueQueue.add(
                    "Freddy: Algunos te darán consejos tácticos, otros simplemente te contarán historias de este vasto mundo.");
            dialogueQueue
                    .add("Freddy: ¡Disfruta de tu aventura, sé curioso y que la buena suerte te acompañe siempre!");
        }

        currentState = GameState.DIALOGUE;
        if (!dialogueQueue.isEmpty()) {
            dialogBox.setText(dialogueQueue.poll());
        }
    }

    private String currentBattleNPC = null;

    public String getCurrentBattleNPC() {
        return currentBattleNPC;
    }

    public void clearCurrentBattleNPC() {
        this.currentBattleNPC = null;
    }

    public void onNPCBattleVictory(String npcName) {
        game.getDefeatedNPCs().add(npcName);
        Gdx.app.log("BATTLE", "NPC " + npcName + " ha sido derrotado y registrado.");

        currentState = GameState.DIALOGUE;
        processNextDialogueLine();
        currentBattleNPC = null;
    }

    private void startArceusBattle() {
        if (!checkTeamHealth()) {
            if (dialogueQueue == null)
                dialogueQueue = new LinkedList<>();
            dialogueQueue.clear();
            dialogueQueue.add("¡Todos tus Pokémon están debilitados!");
            dialogueQueue.add("No puedes desafiar a Arceus en este estado.");
            currentState = GameState.DIALOGUE;
            isArceusDialogue = false;
            processNextDialogueLine();
            return;
        }

        canTriggerEncounter = false;
        encounterCooldown = 0f;
        currentState = GameState.BATTLE;
        clearMovementKeys();
        Gdx.input.setInputProcessor(null);

        Pokemon arceus = sodyl.proyecto.clases.Pokemones.getPokemon("Arceus");
        arceus.setNivel(10);
        arceus.actualizarAtributos();

        if (playerPokemon == null) {
            dialogueQueue.add("¡No tienes Pokémon para luchar!");
            currentState = GameState.DIALOGUE;
            processNextDialogueLine();
            return;
        }

        Gdx.app.log("BATTLE", "Iniciando BATALLA FINAL CONTRA ARCEUS");
        try {
            game.setScreen(
                    new ScreenBatalla(game, this, playerPokemon, arceus, playerInventory, false, true, this.mapPath));
        } catch (Exception e) {
            e.printStackTrace();
            currentState = GameState.FREE_ROAMING;
        }
    }

    // Genera objetos aleatorios en el tile de la hierba
    protected void spawnCollectiblesOnGrassTiles(int totalItemsToSpawn) {
        final List<Integer> VALID_IDS_NIVEL1 = List.of(417, 505, 5193, 5192, 5194, 1617, 1616, 1618, 5281, 5282, 418);
        final int VALID_ID_TIERRA = 1;

        final int TILE_WIDTH = map.getProperties().get("tilewidth", Integer.class);
        final int TILE_HEIGHT = map.getProperties().get("tileheight", Integer.class);

        List<Float[]> validSpawnPositions = new ArrayList<>();

        TiledMapTileLayer layerNivel1 = (TiledMapTileLayer) map.getLayers().get("NIvel 1");
        if (layerNivel1 != null) {
            for (int y = 0; y < layerNivel1.getHeight(); y++) {
                for (int x = 0; x < layerNivel1.getWidth(); x++) {
                    Cell cell = layerNivel1.getCell(x, y);
                    if (cell != null && cell.getTile() != null) {
                        if (VALID_IDS_NIVEL1.contains(cell.getTile().getId())) {
                            float worldX = x * TILE_WIDTH * UNIT_SCALE;
                            float worldY = y * TILE_HEIGHT * UNIT_SCALE;
                            validSpawnPositions.add(new Float[] { worldX, worldY });
                        }
                    }
                }
            }
        }

        // Check Layer "TIERRA"
        TiledMapTileLayer layerTierra = (TiledMapTileLayer) map.getLayers().get("TIERRA");
        if (layerTierra != null) {
            for (int y = 0; y < layerTierra.getHeight(); y++) {
                for (int x = 0; x < layerTierra.getWidth(); x++) {
                    Cell cell = layerTierra.getCell(x, y);
                    if (cell != null && cell.getTile() != null) {
                        if (cell.getTile().getId() == VALID_ID_TIERRA) {
                            float worldX = x * TILE_WIDTH * UNIT_SCALE;
                            float worldY = y * TILE_HEIGHT * UNIT_SCALE;
                            validSpawnPositions.add(new Float[] { worldX, worldY });
                        }
                    }
                }
            }
        }

        if (validSpawnPositions.isEmpty()) {
            Gdx.app.log("SPAWN", "No se encontraron tiles válidos para generar objetos.");
            return;
        }

        // 2. Definir pesos de rareza para objetos
        // Bonguri (comun), Hierba eter (comun) -> Peso 40
        // Guijarro Rojo (poco comun), Baya aranja (poco comun) -> Peso 20
        // Hierba regia (raro) -> Peso 5

        Map<String, Integer> itemWeights = new HashMap<>();
        itemWeights.put("Bonguri", 40);
        itemWeights.put("Hierba éter", 40);
        itemWeights.put("Guijarro Rojo", 20);
        itemWeights.put("Baya Aranja", 20);
        itemWeights.put("Hierba Regia", 5);

        int totalWeight = 0;
        for (int w : itemWeights.values())
            totalWeight += w;

        // 3. Generar objetos
        float collectibleSize = 1f;

        // 3. Garantizar mínimo 5 de cada material primario (25 objetos garantizados)
        String[] primaryMaterials = { "Bonguri", "Hierba éter", "Guijarro Rojo", "Baya Aranja", "Hierba Regia" };
        int guaranteedPerMaterial = 5;
        int guaranteedTotal = primaryMaterials.length * guaranteedPerMaterial; // 25

        Gdx.app.log("COLECTABLES",
                "Generando " + guaranteedTotal + " objetos garantizados (5 de cada tipo)...");

        // Fase 1: Generar objetos garantizados
        for (String materialName : primaryMaterials) {
            for (int j = 0; j < guaranteedPerMaterial; j++) {
                if (validSpawnPositions.isEmpty())
                    break;

                Objeto item = Objeto.getObjetoByName(materialName);
                if (item == null)
                    continue;

                int randomIndex = random.nextInt(validSpawnPositions.size());
                Float[] pos = validSpawnPositions.remove(randomIndex);

                float worldX = pos[0];
                float worldY = pos[1];

                float adjustedX = worldX + (TILE_WIDTH * UNIT_SCALE - collectibleSize) / 2f;
                float adjustedY = worldY + (TILE_HEIGHT * UNIT_SCALE - collectibleSize) / 2f;

                int quantity = random.nextInt(3) + 1;

                // Convert world position back to tile coordinates for logging
                int tileX = (int) (worldX / (TILE_WIDTH * UNIT_SCALE));
                int tileY = (int) (worldY / (TILE_HEIGHT * UNIT_SCALE));

                Collectible collectible = new Collectible(
                        adjustedX,
                        adjustedY,
                        item.getId(),
                        quantity,
                        item.getTexturePath());
                collectibles.add(collectible);
                stage.addActor(collectible.getActor());
            }
        }

        // Fase 2: Generar objetos restantes con probabilidades (35 objetos)
        int remainingToSpawn = totalItemsToSpawn - guaranteedTotal;
        Gdx.app.log("COLECTABLES",
                "Generando " + remainingToSpawn + " objetos adicionales con probabilidades...");

        for (int i = 0; i < remainingToSpawn; i++) {
            if (validSpawnPositions.isEmpty())
                break;

            // Seleccionar objeto basado en peso
            int roll = random.nextInt(totalWeight);
            String selectedItemName = "Bonguri"; // Default
            int currentWeight = 0;

            for (Map.Entry<String, Integer> entry : itemWeights.entrySet()) {
                currentWeight += entry.getValue();
                if (roll < currentWeight) {
                    selectedItemName = entry.getKey();
                    break;
                }
            }

            Objeto item = Objeto.getObjetoByName(selectedItemName);
            if (item == null)
                continue;

            int randomIndex = random.nextInt(validSpawnPositions.size());
            Float[] pos = validSpawnPositions.remove(randomIndex);

            float worldX = pos[0];
            float worldY = pos[1];

            float adjustedX = worldX + (TILE_WIDTH * UNIT_SCALE - collectibleSize) / 2f;
            float adjustedY = worldY + (TILE_HEIGHT * UNIT_SCALE - collectibleSize) / 2f;

            int quantity = random.nextInt(3) + 1;

            // Convert world position back to tile coordinates for logging
            int tileX = (int) (worldX / (TILE_WIDTH * UNIT_SCALE));
            int tileY = (int) (worldY / (TILE_HEIGHT * UNIT_SCALE));

            Collectible collectible = new Collectible(
                    adjustedX,
                    adjustedY,
                    item.getId(),
                    quantity,
                    item.getTexturePath());
            collectibles.add(collectible);
        }

        Gdx.app.log("COLECTABLES",
                "Se generaron " + collectibles.size + " objetos aleatorios en total.");
    }

    protected void spawnPrimaryMaterialsInZones(int itemsPerType) {
        if (!(mapPath != null && mapPath.contains("MAPACOMPLETO.tmx")))
            return;

        final int TILE_WIDTH = map.getProperties().get("tilewidth", Integer.class);
        final int TILE_HEIGHT = map.getProperties().get("tileheight", Integer.class);
        float collectibleSize = 1f;

        String[] primaryMaterials = { "Bonguri", "Hierba éter", "Guijarro Rojo", "Baya Aranja", "Hierba Regia" };
        List<String> tiles = new ArrayList<>(PRIMARY_MATERIAL_TILES_MAP1);

        for (String materialName : primaryMaterials) {
            for (int i = 0; i < itemsPerType; i++) {
                if (tiles.isEmpty())
                    break;

                Objeto item = Objeto.getObjetoByName(materialName);
                if (item == null)
                    continue;

                int randomIndex = random.nextInt(tiles.size());
                String coord = tiles.remove(randomIndex);
                String[] parts = coord.split(",");
                int tileX = Integer.parseInt(parts[0]);
                int tileY = Integer.parseInt(parts[1]);

                float worldX = tileX * TILE_WIDTH * UNIT_SCALE;
                float worldY = tileY * TILE_HEIGHT * UNIT_SCALE;
                float adjustedX = worldX + (TILE_WIDTH * UNIT_SCALE - collectibleSize) / 2f;
                float adjustedY = worldY + (TILE_HEIGHT * UNIT_SCALE - collectibleSize) / 2f;

                int quantity = random.nextInt(3) + 1;

                Collectible collectible = new Collectible(
                        adjustedX,
                        adjustedY,
                        item.getId(),
                        quantity,
                        item.getTexturePath());
                collectibles.add(collectible);
                stage.addActor(collectible.getActor());
            }
        }
        Gdx.app.log("SPAWN", "Se generaron materiales primarios en zonas específicas del Mapa 1.");
    }

    private void startProfessorYoelAnimation() {
        // Initialize dialogBox here to prevent NPE in render/input methods
        if (dialogBox == null) {
            dialogBox = new DialogBox(uiStage, font);
            dialogBox.hide(); // Hide it initially
        }

        if (yoelImage == null) {
            initializeDialogueSequence();
            return;
        }

        // 1. Setup Yoel Image
        // Scale it reasonably (e.g., height of 300px?)
        float yoelHeight = 400f;
        float scale = yoelHeight / yoelImage.getHeight();
        float yoelWidth = yoelImage.getWidth() * scale;

        yoelImage.setSize(yoelWidth, yoelHeight);

        // Start Position: Off-screen Right
        float startX = uiStage.getWidth() + 50;
        float targetX = uiStage.getWidth() - yoelWidth - 50; // Position on Right side of screen
        // Wait, user said: "generarse en la derecha ... y moverse rapidamente a la
        // izquierda ... cuando esté en la izquierda, se colocan los dialogos"
        // "moverse a la izquierda" implies moving TOWARDS the left, or ending up ON the
        // left?
        // "cuando esté en la izquierda" -> Ends up on the left side.

        // Let's interpret: Starts Right (offscreen) -> Moves to Left side of screen.
        targetX = 50; // 50px from left edge

        float yPos = 0; // Bottom of screen (behind dialogue box potentially)

        yoelImage.setPosition(startX, yPos);

        // Ensure it's on stage
        uiStage.addActor(yoelImage);

        // 2. Animate
        // Move fast (rapidamenet)
        float duration = 1.0f; // 1 second

        yoelImage.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo(targetX, yPos, duration,
                        com.badlogic.gdx.math.Interpolation.pow2Out),
                com.badlogic.gdx.scenes.scene2d.actions.Actions.run(() -> {
                    initializeDialogueSequence();
                })));
    }

    private void initializeDialogueSequence() {
        if (dialogBox == null) {
            dialogBox = new DialogBox(uiStage, font);
        }
        // Ensure dialog box is on top of Yoel
        dialogBox.toFront();

        dialogueQueue = new LinkedList<>();
        for (String line : initialDialogs) {
            dialogueQueue.add(line);
        }
        currentState = GameState.DIALOGUE;
        isSpecialEventDialogue = true; // Flag to trigger transition after dialogue
        processNextDialogueLine();
    }

    private void processNextDialogueLine() {
        if (!dialogueQueue.isEmpty()) {
            dialogBox.setText(dialogueQueue.poll());
        } else {
            endDialogueSequence();
        }
    }

    // **FUNCIÓN CORREGIDA: Pasa a FREE_ROAMING directamente**
    private void startArceusBattleDialogue() {
        dialogueQueue.clear();
        dialogueQueue.add("Una luz cegadora emana del centro del santuario...");
        dialogueQueue.add("¡Es ARCEUS, el Pokémon Creador!");
        dialogueQueue.add("Su poder es abrumador. Esta será tu prueba final.");
        dialogueQueue.add("¿Estás listo para desafiar a la leyenda? No podrás huir de la batalla.");

        isArceusDialogue = true; // Flag para manejar la respuesta
        currentState = GameState.DIALOGUE;
        processNextDialogueLine();
    }

    private boolean isArceusDialogue = false;

    private void endDialogueSequence() {
        dialogBox.hide();
        clearMovementKeys();

        if (isArceusDialogue) {
            showYesNoChoice("Sí", "No", () -> {
                isArceusDialogue = false;
                startArceusBattle();
            }, () -> {
                isArceusDialogue = false;
                currentState = GameState.FREE_ROAMING;
            });
            return;
        }

        // Always remove Yoel's image when dialogue ends
        if (yoelImage != null) {
            yoelImage.remove();
        }

        if (isSpecialEventDialogue) {
            isSpecialEventDialogue = false;
            // Go to Selection Screen
            game.setScreen(new SeleccionPokemon(game, this));
            return;
        }

        // Si hay una batalla pendiente, proceder a iniciarla en lugar de volver al modo
        // libre
        if (this.pendingEnemyForBattle != null) {
            proceedWithPendingBattle();
            return;
        }

        // Si hay acción pendiente post-diálogo (ej. opciones Si/No)
        if (onDialogCompleteAction != null) {
            Runnable action = onDialogCompleteAction;
            onDialogCompleteAction = null;
            action.run();
            return;
        }

        currentState = GameState.FREE_ROAMING;
    }

    // **FUNCIÓN ELIMINADA: private void startTransitionToPokemonSelection()**
    // **FUNCIÓN ELIMINADA: private void startTransitionToMainMenu()**

    private void initializePauseMenu() {
        TextButtonStyle buttonStyle = new TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.downFontColor = Color.YELLOW;

        Texture buttonUpTexture = createColoredTexture(new Color(0.2f, 0.2f, 0.2f, 0.7f));
        Texture buttonDownTexture = createColoredTexture(new Color(0.1f, 0.5f, 0.8f, 0.9f));

        buttonStyle.up = new TextureRegionDrawable(buttonUpTexture);
        buttonStyle.down = new TextureRegionDrawable(buttonDownTexture);

        TextButton inventoryButton = new TextButton("Inventario", buttonStyle);

        TextButton pokedexButton = new TextButton("Pokédex", buttonStyle);
        TextButton pokemonesDataButton = new TextButton("Pokemones", buttonStyle);
        TextButton saveButton = new TextButton("Guardar Partida", buttonStyle);
        TextButton mainMenuButton = new TextButton("Menú Principal", buttonStyle);
        TextButton backButton = new TextButton("Volver (ESC)", buttonStyle);

        pauseMenuTable = new Table();
        pauseMenuTable.setFillParent(true);
        pauseMenuTable.center();

        pauseMenuTable.add(new TextButton("JUEGO PAUSADO", buttonStyle)).width(300).height(80).pad(20).row();
        pauseMenuTable.add(inventoryButton).width(250).height(60).pad(10).row();
        // pauseMenuTable.add(craftingButton).width(250).height(60).pad(10).row(); //
        // REMOVIDO
        pauseMenuTable.add(pokedexButton).width(250).height(60).pad(10).row();
        pauseMenuTable.add(pokemonesDataButton).width(250).height(60).pad(10).row();
        pauseMenuTable.add(saveButton).width(250).height(60).pad(10).row();
        pauseMenuTable.add(mainMenuButton).width(250).height(60).pad(10).row();
        pauseMenuTable.add(backButton).width(250).height(60).pad(10).row();

        pauseMenuTable.setVisible(false);
        uiStage.addActor(pauseMenuTable);

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
                if (currentState == GameState.PAUSED) {
                    showInventoryMenu();
                }
            }
        });



        pokemonesDataButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (currentState == GameState.PAUSED) {
                    showPokemonesMenu();
                }
            }
        });

        pauseMenuButtons = new TextButton[] { inventoryButton, pokedexButton, pokemonesDataButton, saveButton,
                mainMenuButton, backButton };

        updatePauseMenuSelection(0);
    }



    private void initializeInventoryMenu() {
        Label.LabelStyle titleStyle = new Label.LabelStyle(font, Color.YELLOW);

        Texture darkBackground = createColoredTexture(new Color(0.1f, 0.1f, 0.1f, 0.9f));
        TextureRegionDrawable backgroundDrawable = new TextureRegionDrawable(darkBackground);

        inventoryMenuTable = new Table();
        inventoryMenuTable.setFillParent(true);
        inventoryMenuTable.setBackground(backgroundDrawable);
        inventoryMenuTable.pad(50);

        inventoryMenuTable.add(new Label("--- INVENTARIO DEL JUGADOR ---", titleStyle)).colspan(4).padBottom(30).row();

        Table itemContainer = new Table();
        itemContainer.setName("itemContainer");
        itemContainer.top().left();
        itemContainer.pad(10);

        ScrollPane scrollPane = new ScrollPane(itemContainer);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setName("inventoryScrollPane");

        inventoryMenuTable.add(scrollPane).colspan(4).expand().fill().row();

        TextButton backButton = new TextButton("Volver (ESC)",
                new TextButton.TextButtonStyle(new TextureRegionDrawable(darkBackground), null, null, font));
        backButton.getLabel().setFontScale(1.0f);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (currentState == GameState.INVENTORY) {
                    currentState = GameState.PAUSED;
                    inventoryMenuTable.setVisible(false);
                    pauseMenuTable.setVisible(true);
                    updatePauseMenuSelection(selectedIndex);
                }
            }
        });
        inventoryMenuTable.add(backButton).colspan(4).width(200).height(50).padTop(20).row();

        inventoryMenuTable.setVisible(false);
        uiStage.addActor(inventoryMenuTable);
    }

    private void showInventoryMenu() {
        currentState = GameState.INVENTORY;
        pauseMenuTable.setVisible(false);
        inventoryMenuTable.setVisible(true);
        updateInventoryDisplay();
    }

    private void updateInventoryDisplay() {
        Table itemContainer = (Table) inventoryMenuTable.findActor("itemContainer");
        itemContainer.clearChildren();

        Label.LabelStyle nameStyle = new Label.LabelStyle(font, Color.WHITE);
        Label.LabelStyle descStyle = new Label.LabelStyle(font, Color.LIGHT_GRAY);
        Label.LabelStyle qtyStyle = new Label.LabelStyle(font, Color.YELLOW);

        itemContainer.add(new Label("ICONO", nameStyle)).width(60).padRight(10);
        itemContainer.add(new Label("NOMBRE / DESCRIPCIÓN", nameStyle)).expandX().fillX();
        itemContainer.add(new Label("CANTIDAD", nameStyle)).width(150).row();
        itemContainer.add().colspan(3).height(5).row();

        Map<Integer, Integer> heldItems = playerInventory.getAllObjetos();

        for (Map.Entry<Integer, Objeto> entry : Objeto.getAllObjects().entrySet()) {
            Objeto item = entry.getValue();
            int itemId = entry.getKey();
            int quantity = heldItems.getOrDefault(itemId, 0);

            if (quantity <= 0)
                continue;

            Texture itemTexture = itemTextures.get(itemId);
            if (itemTexture != null) {
                Image icon = new Image(itemTexture);

                itemContainer.add(icon).width(60).height(60).padRight(10).center();
            } else {
                itemContainer.add(new Label("?", nameStyle)).width(60).height(60).padRight(10).center();
            }

            Table descriptionTable = new Table();
            descriptionTable.left().top();

            Label nameLabel = new Label(item.getNombre() + " (" + item.getTipo().name() + ")", nameStyle);
            nameLabel.setFontScale(1.1f);
            descriptionTable.add(nameLabel).left().row();

            Label descLabel = new Label(item.getDescription(), descStyle);
            descLabel.setWrap(true);
            descLabel.setFontScale(1.0f);
            descriptionTable.add(descLabel).width(uiStage.getWidth() * 0.5f).left().row();

            itemContainer.add(descriptionTable).expandX().fillX().left().pad(5);

            Label qtyLabel = new Label(
                    String.format("%d / %d", quantity, Inventario.MAX_QUANTITY),
                    qtyStyle);
            qtyLabel.setFontScale(1.5f);
            itemContainer.add(qtyLabel).width(150).center().row();

            itemContainer.add().colspan(3).height(2).row();
        }

        if (heldItems.isEmpty()) {
            itemContainer.add(new Label("Tu inventario está vacío. ¡Recolecta materiales en el mapa!", nameStyle))
                    .colspan(3).padTop(50);
        }
    }




    private void initializeCraftingMenu() {
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        Label.LabelStyle successStyle = new Label.LabelStyle(font, Color.GREEN);

        Texture darkBackground = createColoredTexture(new Color(0.1f, 0.1f, 0.1f, 0.9f));
        TextureRegionDrawable backgroundDrawable = new TextureRegionDrawable(darkBackground);

        craftingMenuTable = new Table();
        craftingMenuTable.setFillParent(true);
        craftingMenuTable.setBackground(backgroundDrawable);
        craftingMenuTable.pad(50);

        craftingMenuTable.add(new Label("--- CENTRO DE CRAFTEO ---", labelStyle)).colspan(2).padBottom(20).row();

        Table recipeList = new Table();
        recipeList.setName("recipeList");
        recipeList.top().left();
        recipeList.pad(10);

        craftingMenuTable.add(recipeList).expandY().fillY().width(uiStage.getWidth() * 0.45f).padRight(20);

        Table recipeDetails = new Table();
        recipeDetails.setName("recipeDetails");
        recipeDetails.top().left();

        craftingMenuTable.add(recipeDetails).expandY().fillY().width(uiStage.getWidth() * 0.45f).row();

        craftingStatusLabel = new Label("", successStyle);
        craftingStatusLabel.setFontScale(1.0f);
        craftingStatusLabel.setVisible(false);
        craftingMenuTable.add(craftingStatusLabel).colspan(2).padTop(10).height(20).row();

        TextButton backButton = new TextButton("Volver (ESC)",
                new TextButton.TextButtonStyle(new TextureRegionDrawable(darkBackground), null, null, font));
        backButton.getLabel().setFontScale(1.0f);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (currentState == GameState.CRAFTING) {
                    currentState = GameState.PAUSED;
                    craftingMenuTable.setVisible(false);
                    pauseMenuTable.setVisible(true);
                    updatePauseMenuSelection(selectedIndex);
                }
            }
        });
        craftingMenuTable.add(backButton).colspan(2).width(200).height(50).padTop(20).row();

        craftingMenuTable.setVisible(false);
        uiStage.addActor(craftingMenuTable);
    }

    private void showCraftingMenu() {
        currentState = GameState.CRAFTING;
        pauseMenuTable.setVisible(false);
        craftingMenuTable.setVisible(true);
        selectedRecipeIndex = 0;
        craftingStatusLabel.setVisible(false);
        updateCraftingMenu(true);
    }

    private void updateCraftingMenu(boolean refreshAll) {
        if (availableRecipes.isEmpty())
            return;

        Label.LabelStyle selectedStyle = new Label.LabelStyle(font, Color.YELLOW);
        Label.LabelStyle unselectedStyle = new Label.LabelStyle(font, Color.WHITE);
        Label.LabelStyle detailStyle = new Label.LabelStyle(font, Color.LIGHT_GRAY);

        Table recipeList = (Table) craftingMenuTable.findActor("recipeList");
        Table recipeDetails = (Table) craftingMenuTable.findActor("recipeDetails");

        if (refreshAll) {
            recipeList.clearChildren();
            for (int i = 0; i < availableRecipes.size; i++) {
                Recipe recipe = availableRecipes.get(i);
                Objeto result = Objeto.getObjeto(recipe.getItemId());

                Label recipeLabel = new Label(result.getNombre() + " x" + recipe.getQuantity(),
                        (i == selectedRecipeIndex) ? unselectedStyle : unselectedStyle);
                recipeLabel.setFontScale(1.2f);
                recipeLabel.setName("recipe_" + i);
                recipeList.add(recipeLabel).left().pad(5).row();
            }
        }

        for (int i = 0; i < availableRecipes.size; i++) {
            Label label = (Label) recipeList.findActor("recipe_" + i);
            if (label != null) {
                label.setStyle((i == selectedRecipeIndex) ? selectedStyle : unselectedStyle);
            }
        }

        recipeDetails.clearChildren();
        Recipe currentRecipe = availableRecipes.get(selectedRecipeIndex);
        Objeto resultItem = Objeto.getObjeto(currentRecipe.getItemId());

        recipeDetails.add(new Label("Objeto a Craftear:", selectedStyle)).left().row();
        recipeDetails.add(new Label(resultItem.getNombre() + " x" + currentRecipe.getQuantity(), selectedStyle)).left()
                .padBottom(15).row();

        recipeDetails.add(new Label("--- Ingredientes Requeridos ---", detailStyle)).left().padBottom(10).row();

        for (Map.Entry<Integer, Integer> entry : currentRecipe.getIngredients().entrySet()) {
            Objeto ingredient = Objeto.getObjeto(entry.getKey());
            int required = entry.getValue();
            int held = playerInventory.getQuantity(entry.getKey());

            Color color = (held >= required) ? Color.GREEN : Color.RED;

            Label ingredientLabel = new Label(
                    String.format("%s: %d / %d", ingredient.getNombre(), held, required),
                    new Label.LabelStyle(font, color));
            ingredientLabel.setFontScale(1.1f);
            recipeDetails.add(ingredientLabel).left().padLeft(10).row();
        }

        TextButton craftButton = new TextButton("CRAFTEAR (ENTER)",
                new TextButton.TextButtonStyle(null, null, null, font));
        craftButton.getLabel().setFontScale(1.2f);
        craftButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                craftItem(currentRecipe);
            }
        });

        recipeDetails.add(craftButton).padTop(30).width(250).height(60).row();
    }

    private void craftItem(Recipe recipe) {
        Objeto resultItem = Objeto.getObjeto(recipe.getItemId());
        craftingStatusLabel.setVisible(true);

        if (playerInventory.craftItem(recipe)) {
            craftingStatusLabel.setStyle(new Label.LabelStyle(font, Color.GREEN));
            craftingStatusLabel
                    .setText("¡CRAFTEO EXITOSO! Obtuviste " + recipe.getQuantity() + "x " + resultItem.getNombre());
        } else {
            if (playerInventory.getQuantity(resultItem.getId()) + recipe.getQuantity() > Inventario.MAX_QUANTITY) {
                craftingStatusLabel.setStyle(new Label.LabelStyle(font, Color.ORANGE));
                craftingStatusLabel.setText("CRAFTEO FALLIDO: El inventario está lleno (" + Inventario.MAX_QUANTITY
                        + ") para " + resultItem.getNombre());
            } else {
                craftingStatusLabel.setStyle(new Label.LabelStyle(font, Color.RED));
                craftingStatusLabel
                        .setText("CRAFTEO FALLIDO: Ingredientes insuficientes para " + resultItem.getNombre());
            }
        }
        updateCraftingMenu(false);
    }

    private void togglePauseMenu() {
        if (currentState == GameState.FREE_ROAMING || currentState == GameState.DIALOGUE) {
            if (currentState == GameState.FREE_ROAMING || dialogueQueue.isEmpty()) {
                currentState = GameState.PAUSED;
                pauseMenuTable.setVisible(true);
                updatePauseMenuSelection(0);
            } else {
                Gdx.app.log("PAUSA", "Diálogo pendiente. No se puede pausar.");
            }
        } else if (currentState == GameState.PAUSED) {
            currentState = GameState.FREE_ROAMING;
            pauseMenuTable.setVisible(false);
            clearMovementKeys();
        }
    }

    private void clearMovementKeys() {
        movingUp = false;
        movingDown = false;
        movingLeft = false;
        movingRight = false;
    }

    private void updatePauseMenuSelection(int newIndex) {
        if (pauseMenuButtons == null || pauseMenuButtons.length == 0)
            return;


        for (TextButton btn : pauseMenuButtons) {
            if (btn != null && btn.getLabel() != null) {
                btn.getLabel().setColor(Color.WHITE);
            }
        }

        selectedIndex = (newIndex + pauseMenuButtons.length) % pauseMenuButtons.length;


        if (pauseMenuButtons[selectedIndex] != null && pauseMenuButtons[selectedIndex].getLabel() != null) {
            pauseMenuButtons[selectedIndex].getLabel().setColor(Color.YELLOW);
        }
    }

    private void executePauseMenuAction(int index) {
        if (currentState != GameState.PAUSED)
            return;








        if (index == 0)
            showInventorySubmenu();
        else if (index == 1)
            showPokedexMenu();
        else if (index == 2)
            showPokemonesMenu();
        else if (index == 3) {

            game.saveProgress(playerInventory);
            togglePauseMenu();


            if (dialogueQueue == null)
                dialogueQueue = new LinkedList<>();
            dialogueQueue.clear();
            dialogueQueue.add("¡Partida guardada correctamente!");
            currentState = GameState.DIALOGUE;
            processNextDialogueLine();
        } else if (index == 4)
            startTransitionToMainMenu();
        else if (index == 5)
            togglePauseMenu();
    }



    private void initializeInventorySubmenu() {
        Texture darkBackground = createColoredTexture(new Color(0.1f, 0.1f, 0.1f, 0.9f));
        TextureRegionDrawable backgroundDrawable = new TextureRegionDrawable(darkBackground);

        inventorySubmenuTable = new Table();
        inventorySubmenuTable.setFillParent(true);
        inventorySubmenuTable.setBackground(backgroundDrawable);
        inventorySubmenuTable.pad(50);

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(new TextureRegionDrawable(darkBackground),
                null, null, font);

        TextButton viewItemsBtn = new TextButton("Ver Objetos", style);
        TextButton craftItemsBtn = new TextButton("Craftear", style);
        TextButton backBtn = new TextButton("Volver", style);



        inventorySubmenuTable.add(new Label("--- OPCIONES DE INVENTARIO ---", new Label.LabelStyle(font, Color.CYAN)))
                .padBottom(30).row();
        inventorySubmenuTable.add(viewItemsBtn).pad(10).row();
        inventorySubmenuTable.add(craftItemsBtn).pad(10).row();
        inventorySubmenuTable.add(backBtn).pad(10).row();

        inventorySubmenuTable.setVisible(false);
        uiStage.addActor(inventorySubmenuTable);
    }

    private void showInventorySubmenu() {
        currentState = GameState.INVENTORY_SUBMENU;
        pauseMenuTable.setVisible(false);
        inventorySubmenuTable.setVisible(true);
        selectedInventorySubmenuIndex = 0;
        updateInventorySubmenuUI();
    }

    private void updateInventorySubmenuUI() {



        Table table = inventorySubmenuTable;



        ((TextButton) table.getChildren().get(1)).getLabel().setColor(Color.WHITE);
        ((TextButton) table.getChildren().get(2)).getLabel().setColor(Color.WHITE);
        ((TextButton) table.getChildren().get(3)).getLabel().setColor(Color.WHITE);


        ((TextButton) table.getChildren().get(selectedInventorySubmenuIndex + 1)).getLabel().setColor(Color.YELLOW);
    }

    private void executeInventorySubmenuAction(int index) {
        if (index == 0) {
            inventorySubmenuTable.setVisible(false);
            showInventoryMenu();
        } else if (index == 1) {
            inventorySubmenuTable.setVisible(false);
            showCraftingMenu();
        } else if (index == 2) {

            currentState = GameState.PAUSED;
            inventorySubmenuTable.setVisible(false);
            pauseMenuTable.setVisible(true);
            updatePauseMenuSelection(selectedIndex);
        }
    }



    private void initializePokedexActionMenu() {
        Texture darkBackground = createColoredTexture(new Color(0.1f, 0.1f, 0.1f, 0.95f));
        TextureRegionDrawable backgroundDrawable = new TextureRegionDrawable(darkBackground);

        pokedexActionMenuTable = new Table();

        pokedexActionMenuTable.setSize(300, 200);
        pokedexActionMenuTable.setPosition((uiStage.getWidth() - 300) / 2, (uiStage.getHeight() - 200) / 2);
        pokedexActionMenuTable.setBackground(backgroundDrawable);

        pokedexActionMenuTable.setVisible(false);
        uiStage.addActor(pokedexActionMenuTable);
    }

    private void initializePokemonInfoTable() {
        Texture darkBackground = createColoredTexture(new Color(0.1f, 0.1f, 0.1f, 0.95f));
        TextureRegionDrawable backgroundDrawable = new TextureRegionDrawable(darkBackground);

        pokemonInfoTable = new Table();
        pokemonInfoTable.setFillParent(true);
        pokemonInfoTable.setBackground(backgroundDrawable);
        pokemonInfoTable.pad(50);
        pokemonInfoTable.setVisible(false);
        uiStage.addActor(pokemonInfoTable);
    }

    private void showPokemonInfo(Pokemon pokemon) {
        currentState = GameState.POKEMON_INFO;
        pokedexActionMenuTable.setVisible(false);
        pokedexMenuTable.setVisible(false);
        pokemonInfoTable.setVisible(true);
        pokemonInfoTable.toFront();
        updatePokemonInfoDisplay(pokemon);
    }

    private void updatePokemonInfoDisplay(Pokemon pokemon) {
        pokemonInfoTable.clearChildren();

        Label.LabelStyle titleStyle = new Label.LabelStyle(font, Color.YELLOW);
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        Label.LabelStyle valueStyle = new Label.LabelStyle(font, new Color(0.7f, 1f, 0.7f, 1f));
        Label.LabelStyle infoStyle = new Label.LabelStyle(font, new Color(0.9f, 0.9f, 0.6f, 1f));


        pokemonInfoTable.add(new Label("--- INFORMACIÓN DE " + pokemon.getEspecie().toUpperCase() + " ---", titleStyle))
                .colspan(2).padBottom(30).row();


        pokemonInfoTable.add(new Label("Tipo:", labelStyle)).left().padRight(20);
        pokemonInfoTable.add(new Label(pokemon.getTipo().toString(), valueStyle)).left().row();
        pokemonInfoTable.add().height(10).row();


        pokemonInfoTable.add(new Label("Nivel de Investigación:", labelStyle)).left().padRight(20);
        pokemonInfoTable.add(new Label(pokemon.getNivel() + "/10", valueStyle)).left().row();
        pokemonInfoTable.add().height(10).row();


        pokemonInfoTable.add(new Label("HP:", labelStyle)).left().padRight(20);
        String hpText = pokemon.getActualHP() + "/" + pokemon.getMaxHp();
        Color hpColor = pokemon.getActualHP() < pokemon.getMaxHp() * 0.3f ? Color.RED
                : pokemon.getActualHP() < pokemon.getMaxHp() * 0.6f ? Color.ORANGE : Color.GREEN;
        pokemonInfoTable.add(new Label(hpText, new Label.LabelStyle(font, hpColor))).left().row();
        pokemonInfoTable.add().height(20).row();


        pokemonInfoTable.add(new Label("=== ATAQUES ===", titleStyle)).colspan(2).padBottom(15).row();


        if (pokemon.getMovimiento1() != null) {
            int attack1Damage = pokemon.getMovimiento1().danoBase + (pokemon.getNivel() * 2);
            pokemonInfoTable.add(new Label("Ataque 1:", labelStyle)).left().padRight(20);
            pokemonInfoTable.add(new Label(pokemon.getMovimiento1().nombre, valueStyle)).left().row();

            pokemonInfoTable.add(new Label("  Tipo:", labelStyle)).left().padRight(20);
            pokemonInfoTable.add(new Label(pokemon.getMovimiento1().tipo.toString(), infoStyle)).left().row();

            pokemonInfoTable.add(new Label("  Daño:", labelStyle)).left().padRight(20);
            pokemonInfoTable.add(new Label(String.valueOf(attack1Damage), infoStyle)).left().row();

            pokemonInfoTable.add(new Label("  PP:", labelStyle)).left().padRight(20);
            pokemonInfoTable.add(new Label(pokemon.getppM1() + "/" + pokemon.getMovimiento1().PP, infoStyle)).left()
                    .row();
            pokemonInfoTable.add().height(15).row();
        }


        if (pokemon.getMovimiento2() != null) {
            int attack2Damage = pokemon.getMovimiento2().danoBase + (pokemon.getNivel() * 2);
            pokemonInfoTable.add(new Label("Ataque 2:", labelStyle)).left().padRight(20);
            pokemonInfoTable.add(new Label(pokemon.getMovimiento2().nombre, valueStyle)).left().row();

            pokemonInfoTable.add(new Label("  Tipo:", labelStyle)).left().padRight(20);
            pokemonInfoTable.add(new Label(pokemon.getMovimiento2().tipo.toString(), infoStyle)).left().row();

            pokemonInfoTable.add(new Label("  Daño:", labelStyle)).left().padRight(20);
            pokemonInfoTable.add(new Label(String.valueOf(attack2Damage), infoStyle)).left().row();

            pokemonInfoTable.add(new Label("  PP:", labelStyle)).left().padRight(20);
            pokemonInfoTable.add(new Label(pokemon.getppM2() + "/" + pokemon.getMovimiento2().PP, infoStyle)).left()
                    .row();
        }


        pokemonInfoTable.add().height(30).row();
        pokemonInfoTable.add(new Label("Presiona ESC para volver", new Label.LabelStyle(font, Color.GRAY)))
                .colspan(2).row();
    }

    private void showPokedexActionMenu(Pokemon pokemon) {
        selectedPokemonForHealing = pokemon;
        currentState = GameState.POKEDEX_ACTION_MENU;
        selectedPokedexActionIndex = 0;
        pokedexActionMenuTable.setVisible(true);

        pokedexActionMenuTable.toFront();
        updatePokedexActionMenuUI();
    }

    private void updatePokedexActionMenuUI() {
        pokedexActionMenuTable.clearChildren();
        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);
        Label.LabelStyle selectedStyle = new Label.LabelStyle(font, Color.YELLOW);

        pokedexActionMenuTable.add(new Label("Opciones para " + selectedPokemonForHealing.getEspecie(), style))
                .padBottom(20).row();

        pokedexActionMenuTable.add(new Label("Curar", selectedPokedexActionIndex == 0 ? selectedStyle : style)).pad(10)
                .row();
        pokedexActionMenuTable
                .add(new Label("Ver información", selectedPokedexActionIndex == 1 ? selectedStyle : style)).pad(10)
                .row();
        pokedexActionMenuTable.add(new Label("Cancelar", selectedPokedexActionIndex == 2 ? selectedStyle : style))
                .pad(10).row();
    }

    private void executePokedexAction(int index) {
        if (index == 2) {

            currentState = GameState.POKEDEX;
            pokedexActionMenuTable.setVisible(false);
            return;
        }

        if (index == 1) {

            showPokemonInfo(selectedPokemonForHealing);
            return;
        }






        int potionId = -1;

        Objeto potionObj = Objeto.getObjetoByName("Pocion");
        if (potionObj == null)
            potionObj = Objeto.getObjetoByName("Poción");


        if (potionObj != null) {
            potionId = potionObj.getId();
        } else {


            for (Integer id : playerInventory.getAllObjetos().keySet()) {
                Objeto o = Objeto.getObjeto(id);
                if (o != null && o.getTipo() == Objeto.Type.MEDICINA) {
                    potionId = id;
                    break;
                }
            }
        }

        if (potionId != -1 && playerInventory.getQuantity(potionId) > 0) {

            selectedPokemonForHealing.setActualHP(selectedPokemonForHealing.getMaxHp());
            playerInventory.removeObjeto(potionId, 1);


            dialogueQueue.add("¡" + selectedPokemonForHealing.getEspecie() + " ha sido curado!");
            dialogueQueue.add("Usaste 1 " + Objeto.getObjeto(potionId).getNombre() + ".");





            currentState = GameState.DIALOGUE;
            pokedexActionMenuTable.setVisible(false);
            pokedexMenuTable.setVisible(false);



            final GameState returnState = GameState.POKEDEX;
            onDialogCompleteAction = () -> {
                currentState = returnState;
                pokedexMenuTable.setVisible(true);
            };

            processNextDialogueLine();

        } else {

            currentState = GameState.DIALOGUE;
            pokedexActionMenuTable.setVisible(false);
            pokedexMenuTable.setVisible(false);

            dialogueQueue.add("No tienes pociones para curar a este Pokémon.");

            onDialogCompleteAction = () -> {
                currentState = GameState.POKEDEX;
                pokedexMenuTable.setVisible(true);

            };
            processNextDialogueLine();
        }
    }

    private void startTransitionToMainMenu() {
        currentState = GameState.TRANSITIONING;
        transitionTimer = 0f;
        nextScreen = new MenuPrincipal(game, true);
        Gdx.input.setInputProcessor(null);
    }

    private void startTransitionToPokemonCenter(float returnTileX, float returnTileY) {
        currentState = GameState.TRANSITIONING;
        transitionTimer = 0f;
        nextScreen = new ScreenMapaTiled(game, "Mapa/Centro Pokemon interior.tmx", playerInventory, playerPokemon,
                null, GameState.FREE_ROAMING, null, null, "Mapa/MAPACOMPLETO.tmx", returnTileX, returnTileY);
        Gdx.input.setInputProcessor(null);
    }

    private void startTransitionBackToMap1() {
        currentState = GameState.TRANSITIONING;
        transitionTimer = 0f;


        {

            float tileX = 38f;
            float tileY = 36f;


            if (returnTileXBeforePokemonCenter != null && returnTileYBeforePokemonCenter != null) {
                tileX = returnTileXBeforePokemonCenter;
                tileY = returnTileYBeforePokemonCenter;
            }

            float returnX = tileX * 16f * UNIT_SCALE;
            float returnY = tileY * 16f * UNIT_SCALE;
            nextScreen = new ScreenMapaTiled(game, "Mapa/MAPACOMPLETO.tmx", playerInventory, playerPokemon,
                    null, GameState.FREE_ROAMING, returnX, returnY);
        }

        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {


        if (currentState == GameState.BATTLE) {
            currentState = GameState.FREE_ROAMING;
        }

        ScreenUtils.clear(0, 0, 0, 1);

        if (currentState == GameState.FREE_ROAMING) {
            handleMovement(delta);



            if (isMultiplayer && conexion != null) {
                syncTimer += delta;
                if (syncTimer >= SYNC_INTERVAL) {
                    syncTimer = 0;
                    boolean isMoving = (movingUp || movingDown || movingLeft || movingRight);
                    sendMovementUpdate(characterActor.getX(), characterActor.getY(), lastDirection.name(), isMoving);
                }
            }


            if (collectibles != null) {
                for (Collectible c : collectibles) {
                    if (c.isCollected()) {
                        long elapsed = System.currentTimeMillis() - c.getCollectionTime();
                        if (elapsed >= 300000) {
                            c.setCollected(false);
                            stage.addActor(c.getActor());
                            Gdx.app.log("COLLECTIBLES",
                                    "Objeto " + sodyl.proyecto.clases.Objeto.getObjeto(c.getItemId()).getNombre()
                                            + " ha reaparecido.");
                        }
                    }
                }
            }


            if (!canTriggerEncounter) {
                encounterCooldown += delta;
                if (encounterCooldown >= ENCOUNTER_COOLDOWN_TIME) {
                    canTriggerEncounter = true;
                    encounterCooldown = 0f;
                    Gdx.app.log("ENCOUNTER",
                            "Cooldown de encuentros completado. Los encuentros están activos nuevamente.");
                }
            } else {


                if (currentState != GameState.BATTLE) {
                    checkEncounterZones();
                }
            }
        }

        if (currentState == GameState.DIALOGUE) {
            dialogBox.update(delta);
        }

        stateTime += delta;
        updateCharacterAnimation();


        clampCamera();
        camera.update();


        if (mapPath != null && mapPath.contains("MAPACOMPLETO.tmx")) {
            npcAnimationTimer += delta;
            if (npcAnimationTimer >= NPC_FRAME_DURATION * 2) {
                npcAnimationTimer = 0;
            }

            boolean useFrame2 = npcAnimationTimer >= NPC_FRAME_DURATION;
            com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable d1 = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                    new com.badlogic.gdx.graphics.g2d.TextureRegion(useFrame2 ? kaneki2Texture : kaneki1Texture));
            com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable d2 = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                    new com.badlogic.gdx.graphics.g2d.TextureRegion(
                            useFrame2 ? jesucristo2Texture : jesucristo1Texture));
            com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable d3 = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                    new com.badlogic.gdx.graphics.g2d.TextureRegion(useFrame2 ? afton2Texture : afton1Texture));
            com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable d4 = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                    new com.badlogic.gdx.graphics.g2d.TextureRegion(useFrame2 ? pennywise2Texture : pennywise1Texture));
            com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable d5 = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                    new com.badlogic.gdx.graphics.g2d.TextureRegion(useFrame2 ? jotaro2Texture : jotaro1Texture));
            com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable d6 = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                    new com.badlogic.gdx.graphics.g2d.TextureRegion(
                            useFrame2 ? donValerio2Texture : donValerio1Texture));
            com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable d7 = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                    new com.badlogic.gdx.graphics.g2d.TextureRegion(useFrame2 ? giorno2Texture : giorno1Texture));


            com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable d8 = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                    new com.badlogic.gdx.graphics.g2d.TextureRegion(useFrame2 ? bonnie2Texture : bonnie1Texture));
            com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable d9 = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                    new com.badlogic.gdx.graphics.g2d.TextureRegion(useFrame2 ? fntFoxy2Texture : fntFoxy1Texture));
            com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable d10 = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                    new com.badlogic.gdx.graphics.g2d.TextureRegion(
                            useFrame2 ? circusBaby2Texture : circusBaby1Texture));
            com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable d11 = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                    new com.badlogic.gdx.graphics.g2d.TextureRegion(useFrame2 ? foxy2Texture : foxy1Texture));
            com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable d12 = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                    new com.badlogic.gdx.graphics.g2d.TextureRegion(useFrame2 ? freddy2Texture : freddy1Texture));

            if (kanekiImage != null)
                kanekiImage.setDrawable(d1);
            if (jesucristoImage != null)
                jesucristoImage.setDrawable(d2);
            if (aftonImage != null)
                aftonImage.setDrawable(d3);
            if (pennywiseImage != null)
                pennywiseImage.setDrawable(d4);
            if (jotaroImage != null)
                jotaroImage.setDrawable(d5);
            if (donValerioImage != null)
                donValerioImage.setDrawable(d6);
            if (giornoImage != null)
                giornoImage.setDrawable(d7);

            if (bonnieImage != null)
                bonnieImage.setDrawable(d8);
            if (fntFoxyImage != null)
                fntFoxyImage.setDrawable(d9);
            if (circusBabyImage != null)
                circusBabyImage.setDrawable(d10);
            if (foxyImage != null)
                foxyImage.setDrawable(d11);
            if (freddyImage != null)
                freddyImage.setDrawable(d12);
        }

        if (renderer != null && map != null) {
            try {


                com.badlogic.gdx.utils.IntArray layersToRender = new com.badlogic.gdx.utils.IntArray();
                for (int i = 0; i < map.getLayers().getCount(); i++) {
                    MapLayer layer = map.getLayers().get(i);


                    if (layer instanceof com.badlogic.gdx.maps.tiled.TiledMapTileLayer) {
                        String layerName = layer.getName();
                        if (layerName != null && (layerName.equals("Colisiones") || layerName.equals("BASE"))) {
                            continue;
                        }
                        layersToRender.add(i);
                    }
                }
                renderer.setView(camera);
                renderer.render(layersToRender.toArray());


                renderArceusMarker(delta);
            } catch (Exception e) {
                Gdx.app.error("RENDER", "Error al renderizar el mapa: " + e.getMessage(), e);

                try {
                    if (map != null) {
                        renderer = new OrthogonalTiledMapRenderer(map, UNIT_SCALE);
                    }
                } catch (Exception e2) {
                    Gdx.app.error("RENDER", "Error al reinicializar renderer: " + e2.getMessage(), e2);
                }
            }

        } else

        {
            Gdx.app.error("RENDER",
                    "Renderer o mapa es null. Renderer: " + (renderer != null) + ", Map: " + (map != null));
        }

        stage.act(delta);

        stage.draw();

        batch.begin();

        batch.end();

        uiStage.act(delta);
        uiStage.draw();


        if (isFadingIn) {
            transitionTimer += delta;
            float alpha = 1.0f - Math.min(1f, transitionTimer / transitionDuration);

            if (alpha > 0) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                batch.setProjectionMatrix(camera.combined);
                batch.begin();
                batch.setColor(0, 0, 0, alpha);
                batch.draw(
                        blackPixelTexture,
                        camera.position.x - camera.viewportWidth / 2,
                        camera.position.y - camera.viewportHeight / 2,
                        camera.viewportWidth,
                        camera.viewportHeight);
                batch.end();

                batch.setColor(Color.WHITE);
                Gdx.gl.glDisable(GL20.GL_BLEND);
            } else {
                isFadingIn = false;
                transitionTimer = 0f;
            }
        } else if (currentState == GameState.TRANSITIONING) {
            transitionTimer += delta;
            float alpha = Math.min(1f, transitionTimer / transitionDuration);

            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            batch.setColor(0, 0, 0, alpha);
            batch.draw(
                    blackPixelTexture,
                    camera.position.x - camera.viewportWidth / 2,
                    camera.position.y - camera.viewportHeight / 2,
                    camera.viewportWidth,
                    camera.viewportHeight);
            batch.end();

            batch.setColor(Color.WHITE);
            Gdx.gl.glDisable(GL20.GL_BLEND);

            if (alpha >= 1.0f) {
                if (nextScreen != null) {
                    game.setScreen(nextScreen);
                } else {
                    game.setScreen(new MenuPrincipal(game, true));
                }
                return;
            }
        }
    }

    private void initializeChoiceBox() {
        Texture darkBackground = createColoredTexture(new Color(0.1f, 0.1f, 0.1f, 0.9f));
        TextureRegionDrawable backgroundDrawable = new TextureRegionDrawable(darkBackground);

        choiceBoxTable = new Table();
        choiceBoxTable.setBackground(backgroundDrawable);
        choiceBoxTable.pad(20);

        choiceBoxTable.setSize(150, 100);
        choiceBoxTable.setPosition(uiStage.getWidth() - 170, 200);

        choiceBoxTable.setVisible(false);
        uiStage.addActor(choiceBoxTable);
    }

    private void handleChoiceInput(int keycode) {
        if (keycode == Keys.UP || keycode == Keys.DOWN) {
            selectedChoiceIndex = (selectedChoiceIndex == 0) ? 1 : 0;
            updateChoiceBox();
        } else if (keycode == Keys.ENTER || keycode == Keys.Z) {
            choiceBoxTable.setVisible(false);
            if (selectedChoiceIndex == 0) {

                if (onYesAction != null) {
                    onYesAction.run();
                }
            } else {

                if (onNoAction != null) {
                    onNoAction.run();
                }
            }

            onYesAction = null;
            onNoAction = null;
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
        float currentX = characterActor.getX();
        float currentY = characterActor.getY();
        float nextX = currentX;
        float nextY = currentY;
        float playerWidth = characterActor.getWidth();
        float playerHeight = characterActor.getHeight();

        float dx = 0;
        float dy = 0;

        if (movingUp) {
            dy = movement;
            lastDirection = Direction.UP;
        } else if (movingDown) {
            dy = -movement;
            lastDirection = Direction.DOWN;
        } else if (movingLeft) {
            dx = -movement;
            lastDirection = Direction.LEFT;
        } else if (movingRight) {
            dx = movement;
            lastDirection = Direction.RIGHT;
        }

        boolean isMoving = (dx != 0 || dy != 0);
        if (!isMoving)
            return;

        if (dx != 0) {
            Rectangle testXBounds = new Rectangle(currentX + dx, currentY, playerWidth, playerHeight);
            boolean collidedX = false;

            for (Rectangle wallRect : collisionRects) {
                if (testXBounds.overlaps(wallRect)) {
                    collidedX = true;
                    break;
                }
            }



            if (!collidedX) {
                nextX += dx;
            }
        }

        if (dy != 0) {
            Rectangle testYBounds = new Rectangle(nextX, currentY + dy, playerWidth, playerHeight);
            boolean collidedY = false;

            for (Rectangle wallRect : collisionRects) {
                if (testYBounds.overlaps(wallRect)) {
                    collidedY = true;
                    break;
                }
            }



            if (!collidedY) {
                nextY += dy;
            }
        }

        float tileWidth = map.getProperties().get("tilewidth", Integer.class);
        float tileHeight = map.getProperties().get("tileheight", Integer.class);
        float mapWidth = mapWidthTiles * tileWidth * UNIT_SCALE;
        float mapHeight = mapHeightTiles * tileHeight * UNIT_SCALE;

        nextX = Math.max(0, Math.min(nextX, mapWidth - playerWidth));
        nextY = Math.max(0, Math.min(nextY, mapHeight - playerHeight));

        characterActor.setPosition(nextX, nextY);

        int tileX = (int) ((nextX + playerWidth / 2) / (tileWidth * UNIT_SCALE));
        int tileY = (int) ((nextY + playerHeight / 2) / (tileHeight * UNIT_SCALE));


        if (mapPath != null && mapPath.contains("MAPACOMPLETO.tmx")) {
            if (tileX == 38 && tileY == 37) {
                startTransitionToPokemonCenter(38, 36);
            } else if (tileX == 5 && tileY == 45) {
                startTransitionToPokemonCenter(5, 44);
            } else if (tileX == 87 && tileY == 32) {
                startTransitionToPokemonCenter(87, 31);
            } else if (tileX == 125 && tileY == 30) {
                startTransitionToPokemonCenter(125, 29);
            }
        }



        if (mapPath.contains("Centro Pokemon interior") && tileX == 6 && tileY == 1) {
            startTransitionBackToMap1();
        }







        if (stateTime > 2.0f) {

        }
    }


    private void clampCamera() {

        float desiredCameraX = characterActor.getX() + characterActor.getWidth() / 2;
        float desiredCameraY = characterActor.getY() + characterActor.getHeight() / 2;


        float tileWidth = map.getProperties().get("tilewidth", Integer.class);
        float tileHeight = map.getProperties().get("tileheight", Integer.class);
        float mapWidth = mapWidthTiles * tileWidth * UNIT_SCALE;
        float mapHeight = mapHeightTiles * tileHeight * UNIT_SCALE;


        float cameraHalfWidth = camera.viewportWidth / 2;
        float cameraHalfHeight = camera.viewportHeight / 2;


        float minCameraX = cameraHalfWidth;
        float maxCameraX = mapWidth - cameraHalfWidth;
        float minCameraY = cameraHalfHeight;
        float maxCameraY = mapHeight - cameraHalfHeight;

        camera.position.x = Math.max(minCameraX, Math.min(desiredCameraX, maxCameraX));
        camera.position.y = Math.max(minCameraY, Math.min(desiredCameraY, maxCameraY));
    }

    private void renderArceusMarker(float delta) {
        if (!mapPath.contains("MAPACOMPLETO.tmx"))
            return;

        arceusMarkerTimer += delta;


        float pulse = 0.5f + 0.5f * com.badlogic.gdx.math.MathUtils.sin(arceusMarkerTimer * 3f);
        float scale = 1.0f + 0.3f * pulse;





        float tileWidth = map.getProperties().get("tilewidth", Integer.class);
        float tileHeight = map.getProperties().get("tileheight", Integer.class);

        float worldX = 47 * tileWidth * UNIT_SCALE + (tileWidth * UNIT_SCALE / 2);
        float worldY = 12 * tileHeight * UNIT_SCALE + (tileHeight * UNIT_SCALE / 2);

        shapeRenderer.setProjectionMatrix(camera.combined);
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1f, 0.9f, 0f, 0.4f + 0.3f * pulse);
        shapeRenderer.circle(worldX, worldY, 16 * UNIT_SCALE * scale);
        shapeRenderer.end();

        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
    }

    @Override
    public boolean keyDown(int keycode) {
        if (currentState == GameState.TRANSITIONING)
            return false;

        if (currentState == GameState.WAITING_FOR_CHOICE) {
            handleChoiceInput(keycode);
            return true;
        }


        if (currentState == GameState.DIALOGUE) {
            if (keycode == Keys.ENTER || keycode == Keys.Z) {
                if (dialogBox.isTyping()) {
                    dialogBox.advance();
                } else if (dialogBox.isTextFinished()) {
                    if (!dialogueQueue.isEmpty()) {
                        processNextDialogueLine();
                    } else {
                        endDialogueSequence();
                    }
                }
                return true;
            }
            return true;
        }


        if (keycode == Keys.ESCAPE) {
            if (currentState == GameState.FREE_ROAMING || currentState == GameState.PAUSED) {
                togglePauseMenu();
                return true;
            } else if (currentState == GameState.INVENTORY_SUBMENU) {
                currentState = GameState.PAUSED;
                inventorySubmenuTable.setVisible(false);
                pauseMenuTable.setVisible(true);
                return true;
            } else if (currentState == GameState.POKEMONES_MENU) {
                currentState = GameState.PAUSED;
                pokemonesMenuTable.setVisible(false);
                pauseMenuTable.setVisible(true);
                updatePauseMenuSelection(selectedIndex);
                return true;
            } else if (currentState == GameState.TYPES_MENU) {
                currentState = GameState.POKEMONES_MENU;
                typesMenuTable.setVisible(false);
                pokemonesMenuTable.setVisible(true);
                return true;
            } else if (currentState == GameState.ATTACKS_MENU) {
                currentState = GameState.POKEMONES_MENU;
                attacksMenuTable.setVisible(false);
                pokemonesMenuTable.setVisible(true);
                return true;
            } else if (currentState == GameState.CRAFTING) {

                currentState = GameState.INVENTORY_SUBMENU;
                craftingMenuTable.setVisible(false);
                inventorySubmenuTable.setVisible(true);
                return true;
            } else if (currentState == GameState.INVENTORY) {

                currentState = GameState.INVENTORY_SUBMENU;
                inventoryMenuTable.setVisible(false);
                inventorySubmenuTable.setVisible(true);
                return true;

            } else if (currentState == GameState.POKEDEX_SUBMENU) {
                currentState = GameState.PAUSED;
                pokedexSubmenuTable.setVisible(false);
                pauseMenuTable.setVisible(true);
                updatePauseMenuSelection(selectedIndex);
                return true;
            } else if (currentState == GameState.POKEDEX) {
                showPokedexMenu();
                return true;
            } else if (currentState == GameState.TEAM_SELECTION) {
                showPokedexMenu();
                return true;
            }
        }


        if (currentState == GameState.PAUSED) {
            if (keycode == Keys.DOWN) {
                updatePauseMenuSelection(selectedIndex + 1);
                return true;
            }
            if (keycode == Keys.UP) {
                updatePauseMenuSelection(selectedIndex - 1);
                return true;
            }
            if (keycode == Keys.ENTER || keycode == Keys.Z) {
                executePauseMenuAction(selectedIndex);
                return true;
            }
            return true;
        }


        if (currentState == GameState.CRAFTING) {
            if (availableRecipes.isEmpty())
                return false;

            if (keycode == Keys.DOWN) {
                selectedRecipeIndex = (selectedRecipeIndex + 1) % availableRecipes.size;
                updateCraftingMenu(false);
                return true;
            }
            if (keycode == Keys.UP) {
                selectedRecipeIndex = (selectedRecipeIndex - 1 + availableRecipes.size) % availableRecipes.size;
                updateCraftingMenu(false);
                return true;
            }
            if (keycode == Keys.ENTER || keycode == Keys.Z) {
                craftItem(availableRecipes.get(selectedRecipeIndex));
                return true;
            }
            return true;
        }


        if (currentState == GameState.INVENTORY) {
            if (keycode == Keys.UP || keycode == Keys.DOWN) {
                ScrollPane scrollPane = (ScrollPane) inventoryMenuTable.findActor("inventoryScrollPane");
                if (scrollPane != null) {
                    float scrollAmount = 50f;
                    if (keycode == Keys.UP) {
                        scrollPane.setScrollY(scrollPane.getScrollY() - scrollAmount);
                    } else {
                        scrollPane.setScrollY(scrollPane.getScrollY() + scrollAmount);
                    }
                }
                return true;
            }

            if (keycode == Keys.ESCAPE || keycode == Keys.ENTER || keycode == Keys.Z) {
                currentState = GameState.PAUSED;
                inventoryMenuTable.setVisible(false);
                pauseMenuTable.setVisible(true);
                updatePauseMenuSelection(selectedIndex);
                return true;
            }
            return false;
        }


        if (currentState == GameState.POKEDEX_SUBMENU) {
            if (keycode == Keys.UP) {
                selectedSubmenuIndex = (selectedSubmenuIndex - 1 + 3) % 3;
                updatePokedexSubmenu();
                return true;
            }
            if (keycode == Keys.DOWN) {
                selectedSubmenuIndex = (selectedSubmenuIndex + 1) % 3;
                updatePokedexSubmenu();
                return true;
            }
            if (keycode == Keys.ENTER || keycode == Keys.Z) {
                if (selectedSubmenuIndex == 0) {
                    showPokedexList();
                } else if (selectedSubmenuIndex == 1) {
                    if (sodyl.proyecto.clases.Pokedex.getCollected().size() > 3) {
                        showTeamSelectionMenu();
                    }
                } else if (selectedSubmenuIndex == 2) {
                    currentState = GameState.PAUSED;
                    pokedexSubmenuTable.setVisible(false);
                    pauseMenuTable.setVisible(true);
                    updatePauseMenuSelection(selectedIndex);
                }
                return true;
            }
            return false;
        }


        if (currentState == GameState.POKEMONES_MENU) {
            if (keycode == Keys.UP) {
                selectedPokemonesMenuIndex = (selectedPokemonesMenuIndex - 1 + 3) % 3;
                updatePokemonesMenuUI();
                return true;
            }
            if (keycode == Keys.DOWN) {
                selectedPokemonesMenuIndex = (selectedPokemonesMenuIndex + 1) % 3;
                updatePokemonesMenuUI();
                return true;
            }
            if (keycode == Keys.ENTER || keycode == Keys.Z) {
                if (selectedPokemonesMenuIndex == 0) {
                    showTypesDisplay();
                } else if (selectedPokemonesMenuIndex == 1) {
                    showAttacksDisplay();
                } else if (selectedPokemonesMenuIndex == 2) {
                    currentState = GameState.PAUSED;
                    pokemonesMenuTable.setVisible(false);
                    pauseMenuTable.setVisible(true);
                    updatePauseMenuSelection(selectedIndex);
                }
                return true;
            }
            return false;
        }


        if (currentState == GameState.TYPES_MENU) {
            if (keycode == Keys.UP || keycode == Keys.DOWN) {
                ScrollPane scrollPane = (ScrollPane) typesMenuTable.findActor("typesScrollPane");
                if (scrollPane != null) {
                    float scrollAmount = 50f;
                    if (keycode == Keys.UP) {
                        scrollPane.setScrollY(scrollPane.getScrollY() - scrollAmount);
                    } else {
                        scrollPane.setScrollY(scrollPane.getScrollY() + scrollAmount);
                    }
                }
                return true;
            }
            if (keycode == Keys.ENTER || keycode == Keys.Z || keycode == Keys.ESCAPE || keycode == Keys.X) {
                currentState = GameState.POKEMONES_MENU;
                typesMenuTable.setVisible(false);
                pokemonesMenuTable.setVisible(true);
                return true;
            }
            return false;
        }


        if (currentState == GameState.ATTACKS_MENU) {
            if (keycode == Keys.UP || keycode == Keys.DOWN) {
                ScrollPane scrollPane = (ScrollPane) attacksMenuTable.findActor("attacksScrollPane");
                if (scrollPane != null) {
                    float scrollAmount = 50f;
                    if (keycode == Keys.UP) {
                        scrollPane.setScrollY(scrollPane.getScrollY() - scrollAmount);
                    } else {
                        scrollPane.setScrollY(scrollPane.getScrollY() + scrollAmount);
                    }
                }
                return true;
            }
            if (keycode == Keys.ENTER || keycode == Keys.Z || keycode == Keys.ESCAPE || keycode == Keys.X) {
                currentState = GameState.POKEMONES_MENU;
                attacksMenuTable.setVisible(false);
                pokemonesMenuTable.setVisible(true);
                return true;
            }
            return false;
        }


        if (currentState == GameState.POKEDEX) {
            if (keycode == Keys.UP) {
                if (!currentPokedexList.isEmpty()) {
                    selectedPokedexIndex = (selectedPokedexIndex - 1 + currentPokedexList.size())
                            % currentPokedexList.size();
                    updatePokedexDisplay();
                }
                return true;
            }
            if (keycode == Keys.DOWN) {
                if (!currentPokedexList.isEmpty()) {
                    selectedPokedexIndex = (selectedPokedexIndex + 1) % currentPokedexList.size();
                    updatePokedexDisplay();
                }
                return true;
            }
            if (keycode == Keys.ENTER || keycode == Keys.Z) {
                if (!currentPokedexList.isEmpty()) {
                    String species = currentPokedexList.get(selectedPokedexIndex);

                    List<Pokemon> caught = sodyl.proyecto.clases.Pokedex.getCollected();
                    Pokemon target = null;
                    for (Pokemon p : caught) {
                        if (p.getEspecie().equals(species)) {
                            target = p;
                            break;
                        }
                    }

                    if (target != null) {
                        showPokedexActionMenu(target);
                    } else {


                    }
                }
                return true;
            }
            return false;
        }


        if (currentState == GameState.POKEDEX_ACTION_MENU) {
            if (keycode == Keys.UP || keycode == Keys.DOWN) {
                if (keycode == Keys.UP) {
                    selectedPokedexActionIndex = (selectedPokedexActionIndex - 1 + 3) % 3;
                } else {
                    selectedPokedexActionIndex = (selectedPokedexActionIndex + 1) % 3;
                }
                updatePokedexActionMenuUI();
                return true;
            }
            if (keycode == Keys.ENTER || keycode == Keys.Z) {
                executePokedexAction(selectedPokedexActionIndex);
                return true;
            }
            if (keycode == Keys.ESCAPE) {
                currentState = GameState.POKEDEX;
                pokedexActionMenuTable.setVisible(false);
                return true;
            }
            return true;
        }


        if (currentState == GameState.POKEMON_INFO) {
            if (keycode == Keys.ESCAPE || keycode == Keys.X) {
                currentState = GameState.POKEDEX;
                pokemonInfoTable.setVisible(false);
                pokedexMenuTable.setVisible(true);
                return true;
            }
            return true;
        }


        if (currentState == GameState.INVENTORY_SUBMENU) {
            if (keycode == Keys.UP) {
                selectedInventorySubmenuIndex = (selectedInventorySubmenuIndex - 1 + 3) % 3;
                updateInventorySubmenuUI();
                return true;
            }
            if (keycode == Keys.DOWN) {
                selectedInventorySubmenuIndex = (selectedInventorySubmenuIndex + 1) % 3;
                updateInventorySubmenuUI();
                return true;
            }
            if (keycode == Keys.ENTER || keycode == Keys.Z) {
                executeInventorySubmenuAction(selectedInventorySubmenuIndex);
                return true;
            }
            return true;
        }


        if (currentState == GameState.TEAM_SELECTION) {
            List<Pokemon> all = sodyl.proyecto.clases.Pokedex.getCollected();
            if (all.isEmpty())
                return true;

            if (keycode == Keys.UP) {
                selectedTeamIndex = (selectedTeamIndex - 1 + all.size()) % all.size();
                updateTeamSelectionDisplay();
                return true;
            }
            if (keycode == Keys.DOWN) {
                selectedTeamIndex = (selectedTeamIndex + 1) % all.size();
                updateTeamSelectionDisplay();
                return true;
            }
            if (keycode == Keys.ENTER || keycode == Keys.Z) {
                toggleTeamSelection();
                return true;
            }
            return false;
        }


        if ((keycode == Keys.ENTER || keycode == Keys.Z) && currentState == GameState.FREE_ROAMING) {
            float tileWidth = map.getProperties().get("tilewidth", Integer.class);
            float tileHeight = map.getProperties().get("tileheight", Integer.class);


            int centerX = (int) ((characterActor.getX() + characterActor.getWidth() / 2) / (tileWidth * UNIT_SCALE));
            int centerY = (int) ((characterActor.getY() + characterActor.getHeight() / 2) / (tileHeight * UNIT_SCALE));

            if (centerX == 47 && centerY == 12 && mapPath.contains("MAPACOMPLETO.tmx")) {

                int level10Count = 0;
                for (Pokemon p : sodyl.proyecto.clases.Pokedex.getCollected()) {
                    if (p.getNivel() >= 10) {
                        level10Count++;
                    }
                }

                if (level10Count < 5) {
                    dialogueQueue.clear();
                    dialogueQueue.add("Escuchas una voz en tu mente...");
                    dialogueQueue.add("\"No eres lo suficientemente fuerte para desafiar a la leyenda.\"");
                    dialogueQueue.add("\"Necesitas al menos 5 Pokémon al nivel 10 para entrar al santuario.\"");
                    dialogueQueue.add("(Actualmente tienes " + level10Count + " Pokémon de nivel 10)");
                    currentState = GameState.DIALOGUE;
                    processNextDialogueLine();
                } else {
                    startArceusBattleDialogue();
                }
                return true;
            }
        }


        if (currentState == GameState.FREE_ROAMING && (keycode == Keys.ENTER || keycode == Keys.Z))

        {



            float playerCenterX = characterActor.getX() + characterActor.getWidth() / 2;
            float playerCenterY = characterActor.getY() + characterActor.getHeight() / 2;



            int tileX = (int) (playerCenterX / (map.getProperties().get("tilewidth", Integer.class) * UNIT_SCALE));
            int tileY = (int) (playerCenterY / (map.getProperties().get("tileheight", Integer.class) * UNIT_SCALE));


            if (mapPath != null && mapPath.contains("MAPACOMPLETO.tmx")) {
                if (tileX >= 40 && tileX <= 42 && tileY >= 37 && tileY <= 39) {
                    if (game.getDefeatedNPCs().contains("Kaneki")) {
                        showArceusChallenge("Kaneki",
                                "... Arceus te espera en el santuario. Sólo él puede terminar con esta tragedia constante.");
                    } else {
                        startNPCBattle("Kaneki", "Charizard");
                    }
                    return true;
                } else if (tileX >= 50 && tileX <= 52 && tileY >= 48 && tileY <= 50) {
                    if (game.getDefeatedNPCs().contains("Jesucristo")) {
                        showArceusChallenge("Jesucristo",
                                "Hijo mío, tu fe es fuerte. Ahora, ve y enfrenta el juicio final ante Arceus.");
                    } else {
                        startNPCBattle("Jesucristo", "Lucario");
                    }
                    return true;
                } else if (tileX >= 85 && tileX <= 87 && tileY >= 50 && tileY <= 52) {
                    if (game.getDefeatedNPCs().contains("William Afton")) {
                        showArceusChallenge("William Afton",
                                "He visto el fin... y Arceus está en el centro. Ve, ¡él te espera para tu experimento final!");
                    } else {
                        startNPCBattle("William Afton", "Mewtwo");
                    }
                    return true;
                } else if (tileX >= 98 && tileX <= 100 && tileY >= 25 && tileY <= 27) {
                    if (game.getDefeatedNPCs().contains("Pennywise")) {
                        showArceusChallenge("Pennywise",
                                "¡Ji ji ji! ¡Arceus quiere jugar contigo arriba! ¡Todos flotamos con él!");
                    } else {
                        startNPCBattle("Pennywise", "Gyarados");
                    }
                    return true;
                } else if (tileX >= 9 && tileX <= 11 && tileY >= 26 && tileY <= 28) {
                    if (game.getDefeatedNPCs().contains("Jotaro")) {
                        showArceusChallenge("Jotaro",
                                "Ya no tengo nada que enseñarte. Arceus es el único que queda. Ve por él... yare yare daze.");
                    } else {
                        startNPCBattle("Jotaro", "Blastoise");
                    }
                    return true;
                } else if (tileX >= 142 && tileX <= 144 && tileY >= 21 && tileY <= 23) {
                    if (game.getDefeatedNPCs().contains("Don Valerio")) {
                        showArceusChallenge("Don Valerio",
                                "Los espíritus dicen que tu destino está sellado con Arceus. El santuario te llama.");
                    } else {
                        startNPCBattle("Don Valerio", "Sylveon");
                    }
                    return true;
                } else if (tileX >= 26 && tileX <= 28 && tileY >= 37 && tileY <= 39) {
                    if (game.getDefeatedNPCs().contains("Giorno")) {
                        showArceusChallenge("Giorno",
                                "Has demostrado tener una resolución dorada. Tu camino termina con Arceus. Ve y cumple tu sueño.");
                    } else {
                        startNPCBattle("Giorno", "Serperior");
                    }
                    return true;
                }


                if (tileX >= 29 && tileX <= 31 && tileY >= 25 && tileY <= 27) {
                    startDialogueNPCInteraction("Bonnie");
                    return true;
                } else if (tileX >= 64 && tileX <= 66 && tileY >= 25 && tileY <= 27) {
                    startDialogueNPCInteraction("Funtime Foxy");
                    return true;
                } else if (tileX >= 74 && tileX <= 76 && tileY >= 29 && tileY <= 31) {
                    startDialogueNPCInteraction("Circus Baby");
                    return true;
                } else if (tileX >= 76 && tileX <= 78 && tileY >= 49 && tileY <= 51) {
                    startDialogueNPCInteraction("Foxy");
                    return true;
                } else if (tileX >= 92 && tileX <= 94 && tileY >= 19 && tileY <= 21) {
                    startDialogueNPCInteraction("Freddy");
                    return true;
                }
            }


            if (tileX == 21 && tileY == 48) {
                TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("NIvel 1");
                if (layer != null) {
                    Cell cell = layer.getCell(tileX, tileY);
                    if (cell != null && cell.getTile() != null && cell.getTile().getId() == 5106) {

                        triggerSpecialEvent();
                        return true;
                    }
                }
            }

            boolean collectedSomething = false;

            for (int i = collectibles.size - 1; i >= 0; i--) {
                Collectible collectible = collectibles.get(i);


                boolean isAvailable = true;
                if (isMultiplayer && sharedResourceManager != null) {
                    String cId = mapPath + "_" + (int) collectible.getActor().getX() + "_"
                            + (int) collectible.getActor().getY();
                    if (!sharedResourceManager.isResourceAvailable(cId)) {
                        isAvailable = false;
                    }
                }

                if (!collectible.isCollected() && isAvailable && collectible.isInRange(playerCenterX, playerCenterY)) {

                    int itemId = collectible.getItemId();
                    int quantityToCollect = collectible.getQuantity();

                    int overflow = playerInventory.addObjeto(itemId, quantityToCollect);
                    int collectedQuantity = quantityToCollect - overflow;

                    if (collectedQuantity > 0) {

                        if (dialogueQueue == null) {
                            dialogueQueue = new LinkedList<>();
                        }
                        dialogueQueue.add("¡Encontraste " + collectedQuantity + "x "
                                + Objeto.getObjeto(itemId).getNombre() + "!");

                        if (overflow > 0) {
                            dialogueQueue.add(Objeto.getObjeto(itemId).getNombre()
                                    + " no cabe, ¡tu inventario está lleno (" + Inventario.MAX_QUANTITY + ")!");
                        }

                        collectible.setCollected(true);


                        if (isMultiplayer && conexion != null) {

                            String colId = mapPath + "_" + (int) collectible.getActor().getX() + "_"
                                    + (int) collectible.getActor().getY();

                            Map<String, Object> data = new HashMap<>();
                            data.put("tipo", "item_collected");
                            data.put("itemId", colId);
                            data.put("id", sodyl.proyecto.clases.UserManager.getCurrentUser());
                            conexion.enviar(data);

                            sharedResourceManager.markResourceDepleted(colId);
                        }

                        collectedSomething = true;
                    } else {
                        if (dialogueQueue == null) {
                            dialogueQueue = new LinkedList<>();
                        }
                        dialogueQueue.add("Tu inventario está lleno (" + Inventario.MAX_QUANTITY + ") de "
                                + Objeto.getObjeto(itemId).getNombre() + ". No puedes recoger más.");
                        collectedSomething = true;

                    }

                    break;
                }
            }

            if (collectedSomething) {
                clearMovementKeys();
                currentState = GameState.DIALOGUE;
                processNextDialogueLine();
                return true;
            }

            Gdx.app.log("INTERACCION", "Intento de interacción en Free Roaming (sin objeto cerca)");


            if (mapPath.contains("Centro Pokemon interior")) {
                int pTileX = (int) (playerCenterX / (map.getProperties().get("tilewidth", Integer.class) * UNIT_SCALE));
                int pTileY = (int) (playerCenterY
                        / (map.getProperties().get("tileheight", Integer.class) * UNIT_SCALE));



                if (Math.abs(pTileX - 3) <= 1 && Math.abs(pTileY - 4) <= 1) {
                    dialogueQueue = new LinkedList<>();
                    dialogueQueue.add("¡Hola! Bienvenido al Centro Pokémon.");
                    dialogueQueue.add("Nos encargamos de curar a tus compañeros heridos.");
                    dialogueQueue.add(
                            "Aqui puedes restaurar la vida y los ataques de tus pokemones. ¿Deseas hacerlo?");

                    currentState = GameState.DIALOGUE;
                    dialogBox.setText(dialogueQueue.poll());


                    onDialogCompleteAction = new Runnable() {
                        @Override
                        public void run() {
                            showYesNoChoice();
                        }
                    };
                    return true;
                }
            }

        }


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



            if (keycode == Keys.L) {

                float tileWidth = map.getProperties().get("tilewidth", Integer.class);
                float tileHeight = map.getProperties().get("tileheight", Integer.class);
                int tX = (int) ((characterActor.getX() + characterActor.getWidth() / 2) / (tileWidth * UNIT_SCALE));
                int tY = (int) ((characterActor.getY() + characterActor.getHeight() / 2) / (tileHeight * UNIT_SCALE));

                String record = tX + ", "
                        + tY;
                recordedTilesList.add(record);
                Gdx.app.log("DEBUG_TOOL", "Tile Recorded: " + record);
                return true;
            }


            if (keycode == Keys.P) {
                StringBuilder sb = new StringBuilder();
                sb.append("Recorded Tiles: ");
                for (int i = 0; i < recordedTilesList.size(); i++) {
                    sb.append(recordedTilesList.get(i));
                    if (i < recordedTilesList.size() - 1) {
                        sb.append(", ");
                    }
                }
                Gdx.app.log("DEBUG_TOOL", sb.toString());
                return true;
            }

        }


        if (currentState == GameState.FREE_ROAMING && (keycode == Keys.F)) {
            if (isMultiplayer && duelManager != null) {


                float closestDist = 3.0f * 16 * UNIT_SCALE;
                String targetId = null;
                float px = characterActor.getX();
                float py = characterActor.getY();

                for (OtherPlayer op : otherPlayers.values()) {
                    float dx = op.actor.getX() - px;
                    float dy = op.actor.getY() - py;
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    if (dist < closestDist) {
                        closestDist = dist;
                        targetId = op.id;
                    }
                }

                if (targetId != null) {
                    duelManager.requestDuel(targetId, playerPokemon);
                    dialogueQueue.clear();
                    dialogueQueue.add("Enviando desafío a " + targetId + "...");
                    currentState = GameState.DIALOGUE;
                    dialogBox.setText(dialogueQueue.poll());
                } else {
                    Gdx.app.log("DUEL", "No hay jugadores cerca para retar.");
                }
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

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, false);


        uiStage.getViewport().update(width, height, true);

        if (dialogBox != null) {




            dialogBox.resize(uiStage.getWidth(), uiStage.getHeight());
        }
    }

    private void initializePokedexMenu() {
        Label.LabelStyle titleStyle = new Label.LabelStyle(font, Color.YELLOW);
        Texture darkBackground = createColoredTexture(new Color(0.1f, 0.1f, 0.1f, 0.9f));
        TextureRegionDrawable backgroundDrawable = new TextureRegionDrawable(darkBackground);


        pokedexMenuTable = new Table();
        pokedexMenuTable.setFillParent(true);
        pokedexMenuTable.setBackground(backgroundDrawable);
        pokedexMenuTable.pad(50);
        pokedexMenuTable.add(new Label("--- POKÉDEX ---", titleStyle)).colspan(3).padBottom(30).row();
        Table listContainer = new Table();
        listContainer.setName("listContainer");
        listContainer.top().left();
        pokedexMenuTable.add(listContainer).expand().fill().row();
        pokedexMenuTable.setVisible(false);
        uiStage.addActor(pokedexMenuTable);


        pokedexSubmenuTable = new Table();
        pokedexSubmenuTable.setFillParent(true);
        pokedexSubmenuTable.setBackground(backgroundDrawable);
        pokedexSubmenuTable.pad(50);
        pokedexSubmenuTable.setVisible(false);
        uiStage.addActor(pokedexSubmenuTable);


        teamSelectionTable = new Table();
        teamSelectionTable.setFillParent(true);
        teamSelectionTable.setBackground(backgroundDrawable);
        teamSelectionTable.pad(50);
        teamSelectionTable.setVisible(false);
        uiStage.addActor(teamSelectionTable);
    }

    private void showPokedexMenu() {
        currentState = GameState.POKEDEX_SUBMENU;
        pauseMenuTable.setVisible(false);
        pokedexMenuTable.setVisible(false);
        teamSelectionTable.setVisible(false);
        pokedexSubmenuTable.setVisible(true);
        selectedSubmenuIndex = 0;
        updatePokedexSubmenu();
    }

    private void updatePokedexSubmenu() {
        pokedexSubmenuTable.clearChildren();
        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);
        Label.LabelStyle selectedStyle = new Label.LabelStyle(font, Color.YELLOW);
        Label.LabelStyle disabledStyle = new Label.LabelStyle(font, Color.GRAY);

        String[] options = { "Ver Pokédex", "Gestionar Equipo", "Volver" };
        boolean canManageTeam = sodyl.proyecto.clases.Pokedex.getCollected().size() > 3;

        pokedexSubmenuTable.add(new Label("--- MENÚ POKÉDEX ---", new Label.LabelStyle(font, Color.CYAN)))
                .padBottom(30).row();

        for (int i = 0; i < options.length; i++) {
            Label.LabelStyle currentStyle = (i == selectedSubmenuIndex) ? selectedStyle : style;
            String text = options[i];

            if (i == 1 && !canManageTeam) {
                currentStyle = disabledStyle;
                text += " (Requiere > 3 Pokémon)";
            }

            pokedexSubmenuTable.add(new Label((i == selectedSubmenuIndex ? "> " : "  ") + text, currentStyle)).pad(10)
                    .row();
        }
    }

    private void showPokedexList() {
        currentState = GameState.POKEDEX;
        pokedexSubmenuTable.setVisible(false);
        pokedexMenuTable.setVisible(true);
        selectedPokedexIndex = 0;
        updatePokedexDisplay();
    }

    private void showTeamSelectionMenu() {
        currentState = GameState.TEAM_SELECTION;
        pokedexSubmenuTable.setVisible(false);
        teamSelectionTable.setVisible(true);
        selectedTeamIndex = 0;
        updateTeamSelectionDisplay();
    }

    private int selectedPokedexIndex = 0;
    private List<String> currentPokedexList = new ArrayList<>();

    private void updatePokedexDisplay() {
        Table listContainer = (Table) pokedexMenuTable.findActor("listContainer");
        listContainer.clearChildren();

        Label.LabelStyle nameStyle = new Label.LabelStyle(font, Color.WHITE);
        Label.LabelStyle seenStyle = new Label.LabelStyle(font, Color.GRAY);
        Label.LabelStyle researchStyle = new Label.LabelStyle(font, Color.CYAN);
        Label.LabelStyle teamStyle = new Label.LabelStyle(font, Color.GREEN);

        Set<String> seenPokemon = sodyl.proyecto.clases.Pokedex.getSeen();
        Map<String, Integer> researchLevels = sodyl.proyecto.clases.Pokedex.getResearchLevels();

        if (seenPokemon.isEmpty()) {
            listContainer.add(new Label("No has visto ningún Pokémon aún.", nameStyle)).colspan(3).padTop(50);
            return;
        }

        currentPokedexList = new ArrayList<>(seenPokemon);
        Collections.sort(currentPokedexList);

        for (int i = 0; i < currentPokedexList.size(); i++) {
            String species = currentPokedexList.get(i);
            boolean isCaught = false;
            Pokemon caughtInstance = null;

            for (Pokemon p : sodyl.proyecto.clases.Pokedex.getCollected()) {
                if (p.getEspecie().equals(species)) {
                    isCaught = true;
                    caughtInstance = p;
                    break;
                }
            }

            String statusIcon = isCaught ? "[O]" : "[ ]";
            int researchLevel = researchLevels.getOrDefault(species, 0);

            boolean inTeam = false;
            if (caughtInstance != null) {
                inTeam = sodyl.proyecto.clases.Pokedex.isInTeam(caughtInstance);
            }

            Label statusLabel = new Label(statusIcon, isCaught ? nameStyle : seenStyle);
            Label nameLabel = new Label(species + (inTeam ? " [EQUIPO]" : ""),
                    inTeam ? teamStyle : (isCaught ? nameStyle : seenStyle));
            Label researchLabel = new Label("Inv: " + researchLevel + "/10", researchStyle);

            if (i == selectedPokedexIndex) {
                statusLabel.setColor(Color.YELLOW);
                nameLabel.setColor(Color.YELLOW);
                researchLabel.setColor(Color.YELLOW);
                listContainer.add(new Label(">", new Label.LabelStyle(font, Color.YELLOW))).width(20);
            } else {
                listContainer.add(new Label("", nameStyle)).width(20);
            }

            listContainer.add(statusLabel).width(50);
            listContainer.add(nameLabel).expandX().left();
            listContainer.add(researchLabel).width(150).right().row();

            listContainer.add().colspan(4).height(5).row();
        }
    }

    private void updateTeamSelectionDisplay() {
        teamSelectionTable.clearChildren();
        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);
        Label.LabelStyle selectedStyle = new Label.LabelStyle(font, Color.YELLOW);
        Label.LabelStyle teamStyle = new Label.LabelStyle(font, Color.GREEN);

        List<Pokemon> allPokemon = sodyl.proyecto.clases.Pokedex.getCollected();

        teamSelectionTable
                .add(new Label("--- SELECCIONAR EQUIPO (Max 3) ---", new Label.LabelStyle(font, Color.CYAN)))
                .padBottom(20).row();
        teamSelectionTable.add(new Label("Presiona ENTER para añadir/quitar", new Label.LabelStyle(font, Color.GRAY)))
                .padBottom(20).row();

        if (allPokemon.isEmpty()) {
            teamSelectionTable.add(new Label("No tienes Pokémon.", style));
            return;
        }

        for (int i = 0; i < allPokemon.size(); i++) {
            Pokemon p = allPokemon.get(i);
            boolean inTeam = sodyl.proyecto.clases.Pokedex.isInTeam(p);

            String prefix = (i == selectedTeamIndex) ? "> " : "  ";
            String status = inTeam ? "[EQUIPO] " : "[      ] ";

            Label label = new Label(prefix + status + p.getEspecie() + " Nv." + p.getNivel(),
                    inTeam ? teamStyle : (i == selectedTeamIndex ? selectedStyle : style));
            teamSelectionTable.add(label).left().pad(5).row();
        }
    }

    private void initializePokemonesMenu() {
        Texture darkBackground = createColoredTexture(new Color(0.1f, 0.1f, 0.1f, 0.9f));
        TextureRegionDrawable backgroundDrawable = new TextureRegionDrawable(darkBackground);


        pokemonesMenuTable = new Table();
        pokemonesMenuTable.setFillParent(true);
        pokemonesMenuTable.setBackground(backgroundDrawable);
        pokemonesMenuTable.pad(50);
        pokemonesMenuTable.setVisible(false);
        uiStage.addActor(pokemonesMenuTable);


        typesMenuTable = new Table();
        typesMenuTable.setFillParent(true);
        typesMenuTable.setBackground(backgroundDrawable);
        typesMenuTable.pad(50);
        typesMenuTable.setVisible(false);
        uiStage.addActor(typesMenuTable);


        attacksMenuTable = new Table();
        attacksMenuTable.setFillParent(true);
        attacksMenuTable.setBackground(backgroundDrawable);
        attacksMenuTable.pad(50);
        attacksMenuTable.setVisible(false);
        uiStage.addActor(attacksMenuTable);
    }

    private void showYesNoChoice() {
        showYesNoChoice("Sí", "No", () -> {

            if (playerPokemon != null) {
                playerPokemon.restoreStatus();
                for (Pokemon p : sodyl.proyecto.clases.Pokedex.getTeam()) {
                    if (p != null)
                        p.restoreStatus();
                }
                dialogueQueue.add("¡Tus Pokémon han sido curados!");
            } else {
                dialogueQueue.add("No tienes Pokémon para curar.");
            }
            dialogueQueue.add("¡Vuelve pronto!");
            currentState = GameState.DIALOGUE;
            processNextDialogueLine();
        }, () -> {
            dialogueQueue.add("Entendido. ¡Ten cuidado fuera!");
            currentState = GameState.DIALOGUE;
            processNextDialogueLine();
        });
    }

    private void showYesNoChoice(String yesText, String noText, Runnable onYes, Runnable onNo) {
        this.onYesAction = onYes;
        this.onNoAction = onNo;
        selectedChoiceIndex = 0;
        choiceBoxTable.setVisible(true);
        currentState = GameState.WAITING_FOR_CHOICE;
        updateChoiceBox(yesText, noText);
    }

    private void updateChoiceBox() {
        updateChoiceBox("Sí", "No");
    }

    private void updateChoiceBox(String yesText, String noText) {
        choiceBoxTable.clearChildren();
        Label.LabelStyle selectedStyle = new Label.LabelStyle(font, Color.YELLOW);
        Label.LabelStyle normalStyle = new Label.LabelStyle(font, Color.WHITE);

        Label yesLabel = new Label(yesText, (selectedChoiceIndex == 0) ? selectedStyle : normalStyle);
        Label noLabel = new Label(noText, (selectedChoiceIndex == 1) ? selectedStyle : normalStyle);


        yesLabel.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                choiceBoxTable.setVisible(false);
                if (onYesAction != null)
                    onYesAction.run();
                onYesAction = null;
                onNoAction = null;
            }
        });

        noLabel.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                choiceBoxTable.setVisible(false);
                if (onNoAction != null)
                    onNoAction.run();
                onYesAction = null;
                onNoAction = null;
            }
        });

        choiceBoxTable.add(yesLabel).pad(5).row();
        choiceBoxTable.add(noLabel).pad(5).row();
        choiceBoxTable.pack();
    }

    private void showPokemonesMenu() {
        currentState = GameState.POKEMONES_MENU;
        pauseMenuTable.setVisible(false);
        pokemonesMenuTable.setVisible(true);
        selectedPokemonesMenuIndex = 0;
        updatePokemonesMenuUI();
    }

    private void updatePokemonesMenuUI() {
        pokemonesMenuTable.clearChildren();
        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);
        Label.LabelStyle selectedStyle = new Label.LabelStyle(font, Color.YELLOW);

        pokemonesMenuTable.add(new Label("--- DATOS POKÉMON ---", new Label.LabelStyle(font, Color.CYAN)))
                .padBottom(30).row();

        String[] options = { "Tipos", "Ataques", "Volver" };
        for (int i = 0; i < options.length; i++) {
            Label.LabelStyle currentStyle = (i == selectedPokemonesMenuIndex) ? selectedStyle : style;
            pokemonesMenuTable
                    .add(new Label((i == selectedPokemonesMenuIndex ? "> " : "  ") + options[i], currentStyle))
                    .pad(10).row();
        }
    }

    private void showTypesDisplay() {
        currentState = GameState.TYPES_MENU;
        pokemonesMenuTable.setVisible(false);
        typesMenuTable.setVisible(true);
        updateTypesDisplay();
    }

    private void updateTypesDisplay() {
        typesMenuTable.clearChildren();
        Label.LabelStyle titleStyle = new Label.LabelStyle(font, Color.YELLOW);
        Label.LabelStyle typeStyle = new Label.LabelStyle(font, Color.CYAN);
        Label.LabelStyle infoStyle = new Label.LabelStyle(font, Color.WHITE);
        Label.LabelStyle strongStyle = new Label.LabelStyle(font, Color.GREEN);
        Label.LabelStyle weakStyle = new Label.LabelStyle(font, Color.RED);

        typesMenuTable.add(new Label("--- TABLA DE TIPOS ---", titleStyle)).padBottom(20).row();

        Table content = new Table();
        content.top().left();

        for (sodyl.proyecto.clases.TiposPokemon type : sodyl.proyecto.clases.TiposPokemon.values()) {
            content.add(new Label("[" + type.name() + "]", typeStyle)).left().padTop(15).row();


            StringBuilder strongBuilder = new StringBuilder("Fuerte contra: ");
            boolean firstStrong = true;

            StringBuilder weakBuilder = new StringBuilder("Débil contra: ");
            boolean firstWeak = true;

            for (sodyl.proyecto.clases.TiposPokemon other : sodyl.proyecto.clases.TiposPokemon.values()) {
                double mult = sodyl.proyecto.clases.TablaEficacia.getMultiplicador(type, other);
                if (mult > 1.0) {
                    if (!firstStrong)
                        strongBuilder.append(", ");
                    strongBuilder.append(other.name());
                    firstStrong = false;
                } else if (mult < 1.0) {
                    if (!firstWeak)
                        weakBuilder.append(", ");
                    weakBuilder.append(other.name());
                    firstWeak = false;
                }
            }

            if (firstStrong)
                strongBuilder.append("Nada");
            if (firstWeak)
                weakBuilder.append("Nada");

            content.add(new Label(strongBuilder.toString(), strongStyle)).left().padLeft(20).row();
            content.add(new Label(weakBuilder.toString(), weakStyle)).left().padLeft(20).row();
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        scroll.setName("typesScrollPane");
        typesMenuTable.add(scroll).expand().fill().row();

        typesMenuTable.add(new Label("Presiona Z o ENTER para volver", infoStyle)).padTop(10).row();
    }

    private void showAttacksDisplay() {
        currentState = GameState.ATTACKS_MENU;
        pokemonesMenuTable.setVisible(false);
        attacksMenuTable.setVisible(true);
        updateAttacksDisplay();
    }

    private void updateAttacksDisplay() {
        attacksMenuTable.clearChildren();
        Label.LabelStyle titleStyle = new Label.LabelStyle(font, Color.YELLOW);
        Label.LabelStyle headerStyle = new Label.LabelStyle(font, Color.CYAN);
        Label.LabelStyle contentStyle = new Label.LabelStyle(font, Color.WHITE);

        attacksMenuTable.add(new Label("--- LISTA DE ATAQUES ---", titleStyle)).padBottom(20).row();

        Table content = new Table();
        content.top().left();


        content.add(new Label("NOMBRE", headerStyle)).width(250).left();
        content.add(new Label("TIPO", headerStyle)).width(150).left();
        content.add(new Label("DAÑO", headerStyle)).width(100).right().row();
        content.add().colspan(3).height(10).row();

        java.util.List<sodyl.proyecto.clases.TiposAtaque> attacks = sodyl.proyecto.clases.Pokemones.getAllAttacks();

        for (sodyl.proyecto.clases.TiposAtaque atk : attacks) {
            content.add(new Label(atk.nombre, contentStyle)).left();
            content.add(new Label(atk.tipo.name(), contentStyle)).left();
            content.add(new Label(String.valueOf(atk.danoBase), contentStyle)).right().row();
            content.add().colspan(3).height(5).row();
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        scroll.setName("attacksScrollPane");
        attacksMenuTable.add(scroll).expand().fill().row();

        attacksMenuTable.add(new Label("Presiona Z o ENTER para volver", contentStyle)).padTop(10).row();
    }

    private void toggleTeamSelection() {
        List<Pokemon> allPokemon = sodyl.proyecto.clases.Pokedex.getCollected();
        if (allPokemon.isEmpty())
            return;

        Pokemon selected = allPokemon.get(selectedTeamIndex);

        if (sodyl.proyecto.clases.Pokedex.isInTeam(selected)) {
            sodyl.proyecto.clases.Pokedex.removeFromTeam(selected);
        } else {
            if (sodyl.proyecto.clases.Pokedex.getTeam().size() < sodyl.proyecto.clases.Pokedex.MAX_TEAM_SIZE) {
                sodyl.proyecto.clases.Pokedex.addToTeam(selected);
            }
        }
        updateTeamSelectionDisplay();
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (currentState == GameState.DIALOGUE) {
            if (dialogBox.isTyping()) {
                dialogBox.advance();
            } else if (dialogBox.isTextFinished()) {
                if (!dialogueQueue.isEmpty()) {
                    processNextDialogueLine();
                }
            }
            return true;
        }
        return false;
    }

    private void triggerSpecialEvent() {
        if (playerPokemon != null) {

            if (dialogBox == null) {
                dialogBox = new DialogBox(uiStage, font);
            }
            if (dialogueQueue == null) {
                dialogueQueue = new LinkedList<>();
            }

            dialogueQueue.clear();
            dialogueQueue.add("Ya has elegido tu Pokémon inicial.");
            currentState = GameState.DIALOGUE;
            processNextDialogueLine();
            return;
        }


        if (dialogBox == null) {
            dialogBox = new DialogBox(uiStage, font);
        }
        if (dialogueQueue == null) {
            dialogueQueue = new LinkedList<>();
        }

        dialogueQueue.clear();
        dialogueQueue.add("¡Alto ahí!");
        dialogueQueue.add("Tenemos una batalla urgente.");
        dialogueQueue.add("¡Elige un Pokémon rápido!");

        currentState = GameState.DIALOGUE;
        processNextDialogueLine();




        isSpecialEventDialogue = true;
    }

    private boolean isSpecialEventDialogue = false;

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

    private void removeCollisionsAt(int tileX, int tileY) {
        float tileW = map.getProperties().get("tilewidth", Integer.class) * UNIT_SCALE;
        float tileH = map.getProperties().get("tileheight", Integer.class) * UNIT_SCALE;
        float x = tileX * tileW;
        float y = tileY * tileH;
        Rectangle tileRect = new Rectangle(x + tileW * 0.1f, y + tileH * 0.1f, tileW * 0.8f, tileH * 0.8f);

        for (int i = collisionRects.size - 1; i >= 0; i--) {
            if (collisionRects.get(i).overlaps(tileRect)) {
                collisionRects.removeIndex(i);
            }
        }
    }

    private void addCollisionAt(int tileX, int tileY) {
        float tileW = map.getProperties().get("tilewidth", Integer.class) * UNIT_SCALE;
        float tileH = map.getProperties().get("tileheight", Integer.class) * UNIT_SCALE;
        float x = tileX * tileW;
        float y = tileY * tileH;
        collisionRects.add(new Rectangle(x, y, tileW, tileH));
    }

    public void setPlayerPokemon(Pokemon p) {
        this.playerPokemon = p;
        if (p != null) {
            sodyl.proyecto.clases.Pokedex.addCollected(p);
        }
    }

    public void saveGame() {
        if (characterActor == null)
            return;
        game.saveProgress(playerInventory, mapPath, characterActor.getX(), characterActor.getY());
    }

    @Override
    public void pause() {
        saveGame();
    }

    @Override
    public void hide() {
        saveGame();


        if (currentState != GameState.BATTLE) {
            dispose();
        }
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {


        if (yoelTexture != null)
            yoelTexture.dispose();
        if (recepcionistaTexture != null)
            recepcionistaTexture.dispose();
        if (bonnie1Texture != null)
            bonnie1Texture.dispose();
        if (bonnie2Texture != null)
            bonnie2Texture.dispose();
        if (fntFoxy1Texture != null)
            fntFoxy1Texture.dispose();
        if (fntFoxy2Texture != null)
            fntFoxy2Texture.dispose();
        if (circusBaby1Texture != null)
            circusBaby1Texture.dispose();
        if (circusBaby2Texture != null)
            circusBaby2Texture.dispose();
        if (foxy1Texture != null)
            foxy1Texture.dispose();
        if (foxy2Texture != null)
            foxy2Texture.dispose();
        if (freddy1Texture != null)
            freddy1Texture.dispose();
        if (freddy2Texture != null)
            freddy2Texture.dispose();

        if (blackPixelTexture != null)
            blackPixelTexture.dispose();


        if (kaneki1Texture != null)
            kaneki1Texture.dispose();
        if (kaneki2Texture != null)
            kaneki2Texture.dispose();
        if (jesucristo1Texture != null)
            jesucristo1Texture.dispose();
        if (jesucristo2Texture != null)
            jesucristo2Texture.dispose();
        if (afton1Texture != null)
            afton1Texture.dispose();
        if (afton2Texture != null)
            afton2Texture.dispose();
        if (pennywise1Texture != null)
            pennywise1Texture.dispose();
        if (pennywise2Texture != null)
            pennywise2Texture.dispose();
        if (jotaro1Texture != null)
            jotaro1Texture.dispose();
        if (jotaro2Texture != null)
            jotaro2Texture.dispose();
        if (donValerio1Texture != null)
            donValerio1Texture.dispose();
        if (donValerio2Texture != null)
            donValerio2Texture.dispose();
        if (giorno1Texture != null)
            giorno1Texture.dispose();
        if (giorno2Texture != null)
            giorno2Texture.dispose();


        if (stage != null)
            stage.dispose();
        if (uiStage != null)
            uiStage.dispose();
        if (batch != null)
            batch.dispose();
        if (atlas != null)
            atlas.dispose();
        if (renderer != null)
            renderer.dispose();
        if (map != null)
            map.dispose();
        if (font != null)
            font.dispose();
        if (shapeRenderer != null)
            shapeRenderer.dispose();
        if (dialogBox != null)
            dialogBox.dispose();


        for (Texture texture : itemTextures.values()) {
            texture.dispose();
        }
        itemTextures.clear();


        if (isMultiplayer && conexion != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("tipo", "desconectar");
            data.put("id", sodyl.proyecto.clases.UserManager.getCurrentUser());
            conexion.enviar(data);
            conexion.desconectar();
            conexion = null;
        }


        batch = null;
        stage = null;
        uiStage = null;
        camera = null;
        renderer = null;
        map = null;
        font = null;
        shapeRenderer = null;
        dialogBox = null;
    }

}
