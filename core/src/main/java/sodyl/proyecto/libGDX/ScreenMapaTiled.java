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
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.utils.viewport.FitViewport;

// --- IMPORTACIONES DE CLASES DEL PAQUETE 'clases' ---
import sodyl.proyecto.clases.Inventario;
import sodyl.proyecto.clases.Objeto;
import sodyl.proyecto.clases.Objeto.Recipe;
import sodyl.proyecto.clases.Pokemon;
import sodyl.proyecto.clases.Pokemones;
// ASUME la existencia de la clase Collectible
// ----------------------------------------------------

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
import java.util.function.Supplier;
import static com.badlogic.gdx.Input.Keys;

public class ScreenMapaTiled implements Screen, InputProcessor {

    private final Proyecto game;
    private final String initialPokemonName; // si no es null, se usa para inicializar playerPokemon
    private Stage stage;
    public Stage uiStage; // public para acceso desde ScreenBatalla
    private OrthographicCamera camera;

    private SpriteBatch batch;
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
    // Map 3 NPCs
    private Image pennywiseActor, donValerioActor;
    private Texture pennywiseTexture1, pennywiseTexture2, donValerioTexture1, donValerioTexture2;
    private boolean pennywiseDefeated = false, donValerioDefeated = false;
    private float pennywiseAnimTimer = 0, donValerioAnimTimer = 0;
    private boolean pennywiseFrame1 = true, donValerioFrame1 = true;
    // Map 4 Info NPC
    private Texture map4InfoNpcTexture1, map4InfoNpcTexture2;
    private Image map4InfoNpcActor;
    private boolean map4InfoNpcFrame1 = true;
    private float map4InfoNpcAnimTimer = 0;

    // Map 2 Info NPC (Freddy)
    private Texture freddyMap2Texture1, freddyMap2Texture2;
    private Image freddyMap2Actor;
    private boolean freddyMap2Frame1 = true;
    private float freddyMap2AnimTimer = 0;

    // Map 3 Info NPC (Bonnie)
    private Texture bonnieTexture1, bonnieTexture2;
    private Image bonnieActor;
    private boolean bonnieFrame1 = true;
    private float bonnieAnimTimer = 0;

    private static final float SPEED = 80f;
    private boolean movingUp, movingDown, movingLeft, movingRight;
    private static final float UNIT_SCALE = 1 / 8f;

    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private int mapWidthTiles;
    private int mapHeightTiles;

    private Array<Rectangle> collisionRects;
    private static final String COLLISION_LAYER_NAME = "Capa de Objetos 1";
    private static final float COLLISION_PADDING = 0.1f;
    // Listado de Tile IDs para APARICIÓN DE POKÉMON en Mapa 2
    private static final Set<Integer> VALID_POKEMON_TILES_MAP2 = Set.of(
            1033, 522, 778, 1034, 1290, 1546, 523, 1035, 2571, 1292, 524, 780, 1036, 1548, 2060, 2316, 2572, 525, 1549,
            1805, 526, 2574, 527, 2575, 528, 2576, 529, 2577, 530, 2578, 2834, 531, 2579, 2835, 532, 2580, 2836, 533,
            2581, 2837, 534, 2582, 2838, 535, 2583, 2839, 536, 2584, 2840, 537, 2585, 2841, 538, 2586, 2842, 2843, 539,
            2587, 540, 2588, 2844, 1565, 2333, 2589, 1821, 541, 797, 2845, 2077, 1309, 1053, 1566, 542, 1310, 798, 1311,
            543, 799, 1312, 1056, 544, 1057, 545, 1058, 802, 546, 547, 548, 549, 550, 649, 906, 1162, 1418, 1419, 1163,
            907, 651, 908, 1164, 652, 1420, 1932, 2188, 2444, 653, 1677, 1933, 2701, 654, 2702, 2703, 2704, 2706, 1437,
            1949, 2205, 1693, 2717, 2461, 1181, 925, 413, 669, 1182, 670, 414, 1439, 671, 1184, 673, 929, 930, 674, 675,
            676, 677, 678, 679, 8587, 8459, 8331, 8332, 8460, 8589, 8333, 8461, 8590, 8334, 8462, 8591, 8335, 8463,
            8592, 8336, 8464, 8593, 8337, 8465, 8594, 8338, 8466, 8595, 8339, 8467, 8596, 8340, 8468, 8597, 8341,
            8469, 8598, 8342, 8470, 8343, 8471, 8599);

    // Listado de Tile IDs para APARICIÓN DE OBJETOS en Mapa 2
    private static final Set<Integer> VALID_COLLECTIBLE_TILES_MAP2 = Set.of(
            2820, 3076, 3332, 2821, 3077, 3333, 2822, 3078, 3334, 2823, 3079, 3335, 5383, 6152, 3338, 3339, 5899, 5644,
            3341, 5901, 5389, 3342, 5134, 3343, 5647, 3344, 5648, 5904, 3345, 3347, 3348, 6680, 6936, 7192, 7704, 5912,
            3609, 5401, 4890, 4634, 3610, 4378, 4122, 4891, 5147, 4635, 3355, 3867, 4123, 4379, 5148, 4892, 3356, 3868,
            4124, 4380, 4636, 4893, 5149, 3869, 3613, 4125, 4637, 3360, 5933, 2692, 2948, 3204, 2949, 3205, 3461, 2950,
            3206, 2951, 3207, 5511, 4999, 3208, 5000, 3209, 5001, 3210, 5002, 6283, 5771, 5003, 3212, 6284, 5516, 3213,
            6285, 5261, 5005, 3214, 5006, 3215, 6031, 5007, 3216, 6032, 5776, 3217, 3218, 3219, 3221, 3222, 3223, 6808,
            7576, 3224, 5016, 6040, 7833, 3225, 3737, 5529, 4250, 4762, 5018, 5274, 5530, 3226, 3738, 3994, 4506, 4251,
            5019, 4763, 4507, 5275, 5531, 5787, 3227, 3739, 3995, 4764, 5020, 5276, 5532, 4252, 4508, 4765, 3741, 3997,
            4509, 5021, 5277, 5533, 7837, 3485, 3486, 3487, 3231, 3488, 3232,
            // Nuevos tiles agregados
            5642, 5649, 5905, 5651, 5653, 5654, 3351, 5656, 3612, 5150, 5407, 5663, 5919, 6175, 3359, 5408, 6176, 5153,
            5409, 6177, 5410, 5154, 6179, 5156, 5412, 5413, 5157, 5158, 5414, 3111, 2855, 5415, 5159, 6183, 3624, 3368,
            3112, 2600, 5160, 5416, 6184, 5417, 4137, 3625, 6185, 5418, 4394, 4138, 6186, 5163, 4907, 4651, 4395, 4139,
            6188, 5932, 4396, 3884, 5165, 5166, 5934, 2862, 3374, 3630, 5935, 5167, 6192, 5169, 5768, 5514, 6027, 6028,
            6029, 6033, 3478, 3740, 3484, 5278, 5023, 6047, 6304, 5536, 5280, 6305, 5281, 5537, 5025, 6306, 5538, 5282,
            5539, 6308, 5284, 5541, 5285, 5029, 5286, 5542, 3239, 2727, 5543, 5031, 3496, 2984, 2728, 5288, 5544, 5289,
            4265, 4009, 3753, 5290, 4010, 5291, 5035, 5547, 4779, 4523, 4011, 2475, 2221, 2733, 4013, 3757, 5806, 6062,
            2734, 2990, 3246, 3502, 4014, 5807, 6063, 6064, 6576);

    // Coordenadas de spawn para MAPA3 (X,Y)
    private static final Set<String> VALID_SPAWN_COORDS_MAP3 = new HashSet<>(Arrays.asList(
            "1,18", "2,18", "3,18", "4,18", "5,18", "6,18", "7,18", "8,18", "9,18",
            "9,17", "8,17", "7,17", "6,17", "5,17", "4,17", "3,17", "2,17", "1,17",
            "1,16", "1,15", "1,14", "2,14", "2,15", "2,16", "3,16", "3,15", "3,14",
            "4,14", "4,15", "4,16", "5,16", "5,15", "5,14", "6,14", "6,15", "6,16",
            "7,16", "7,15", "7,14", "8,14", "8,15", "8,16", "9,16", "9,15", "9,14",
            "9,13", "9,12", "8,12", "8,13", "7,13", "7,12", "6,12", "6,13", "5,13",
            "5,12", "4,12", "4,13", "3,13", "3,12", "2,12", "2,13", "1,13", "1,12",
            "8,31", "9,31", "10,31", "10,33", "9,33", "8,33", "8,34", "9,34", "10,34",
            "10,35", "9,35", "8,35", "8,36", "9,36", "10,36", "10,37", "9,37", "8,37",
            "8,38", "9,38", "10,38", "28,22", "27,22", "26,22", "25,22", "24,22", "25,21",
            "26,21", "27,21", "27,20", "26,20", "25,20", "25,19", "25,19", "24,19",
            "24,18", "23,18", "25,18", "26,18", "27,18", "28,18", "29,18", "28,19",
            "27,19", "26,19", "26,20", "27,20", "27,22", "27,22", "39,31", "40,31",
            "41,31", "41,32", "40,32", "39,32", "39,34", "40,34", "41,34", "41,35",
            "40,35", "39,35", "39,36", "40,36", "41,36", "41,37", "40,37", "39,37",
            "39,38", "40,38", "41,38"));

    // Coordenadas de spawn para MAPA 4 - ZONA 1 (fondoBatalla.png)
    private static final Set<String> VALID_SPAWN_COORDS_MAP4_ZONE1 = new HashSet<>(Arrays.asList(
            "36,20", "36,19", "35,19", "35,20", "35,21", "35,22", "35,23", "34,23", "34,22", "34,21", "34,20", "34,19",
            "34,24", "34,25", "34,26", "34,27", "34,28", "34,29", "34,29", "34,30", "34,31", "34,32", "34,33", "35,33",
            "35,32", "35,31", "35,30", "35,30", "35,29", "35,28", "35,27", "36,29", "36,30", "36,31", "36,31", "36,32",
            "36,33", "37,32", "37,32", "37,31", "37,30", "37,29", "37,28", "38,29", "38,30", "38,31", "38,31", "38,32",
            "38,33", "39,30", "39,29", "39,28", "41,28", "41,30", "40,30", "40,29", "33,33", "33,33", "33,32", "33,31",
            "33,30", "33,29", "33,28", "33,27", "33,26", "33,25", "33,25", "33,24", "33,23", "33,22", "33,21", "33,20",
            "33,19", "33,19", "33,21", "32,23", "32,24", "32,24", "32,25", "32,26", "32,27", "32,28", "32,29", "32,30",
            "32,31", "32,32", "32,32", "31,33", "31,32", "31,31", "31,31", "31,30", "31,29", "31,28", "31,28", "31,27",
            "31,26", "31,25", "31,24", "30,24", "30,25", "30,26", "30,27", "30,27", "30,28", "30,29", "30,30", "30,31",
            "30,31", "29,32", "29,31", "29,30", "29,29", "29,29", "29,28", "29,27", "28,26", "28,27", "28,28", "28,29",
            "28,30", "28,30", "27,30", "27,29", "27,28", "27,27", "27,27", "26,26", "26,26", "26,27", "26,26", "26,29",
            "25,28", "25,25", "25,28", "25,27", "25,26", "32,19", "30,19", "30,19", "30,19"));

    // Coordenadas de spawn para MAPA 4 - ZONA 2 (fondoBatalla4.png)
    private static final Set<String> VALID_SPAWN_COORDS_MAP4_ZONE2 = new HashSet<>(Arrays.asList(
            "48,31", "48,32", "48,33", "47,31", "47,32", "47,33", "47,33", "47,34", "47,34", "47,35", "47,36", "47,36",
            "46,36", "46,35", "46,34", "46,33", "46,32", "46,36", "46,37", "46,37", "46,38", "46,38", "46,39", "46,40",
            "46,41", "45,41", "45,42", "45,41", "45,40", "45,39", "45,37", "45,36", "45,35", "45,31", "44,31", "43,31",
            "42,31", "42,31", "42,31", "42,32", "41,33", "40,31", "40,32", "41,32", "42,32", "43,32", "43,33", "43,34",
            "43,35", "43,36", "43,37", "43,38", "43,38", "44,38", "44,39", "44,40", "44,41", "44,42", "44,42", "45,42",
            "45,41", "45,40", "45,39", "44,37", "44,37", "44,36", "44,35", "44,35", "42,32", "42,33", "42,34", "42,35",
            "42,36", "42,37", "42,37", "42,38", "41,38", "41,41", "41,39", "41,40", "41,41", "43,42", "42,42", "42,38",
            "41,37", "41,36", "41,36", "41,35", "41,34", "41,33", "41,32", "40,32", "40,31", "40,33", "40,34", "40,34",
            "40,35", "40,36", "40,37", "40,38", "40,39", "40,40", "40,40", "40,41", "39,41", "39,40", "39,39", "39,39",
            "39,38", "39,37", "39,37", "39,36", "39,35", "39,34", "39,34", "38,34", "37,34", "37,35", "38,35", "37,35",
            "38,37", "38,38", "39,40", "39,41", "39,40", "39,39", "37,38", "37,37", "37,37", "37,37", "37,36", "37,35",
            "37,34", "37,35", "37,35", "37,36", "36,37", "36,38", "36,39", "36,40", "36,41", "36,42", "36,43", "36,43",
            "36,43", "37,43", "38,43", "38,42", "37,42", "37,42", "35,43", "35,42", "35,41", "35,40", "35,39", "35,39",
            "35,38", "35,37", "35,37", "34,37", "34,38", "34,39", "34,40", "34,41", "34,41", "34,42", "33,41", "33,42",
            "33,42", "33,42", "33,42", "34,39", "33,39", "32,39", "32,39", "32,39", "33,39", "34,39"));

    // Coordenadas de coleccionables para MAPA3 (X,Y)
    private static final Set<String> VALID_COLLECTIBLE_COORDS_MAP3 = new HashSet<>(Arrays.asList(
            // Coordenadas anteriores
            "26,34", "26,33", "26,32", "26,31", "26,30", "26,29", "26,28", "26,27", "26,26", "26,25", "26,24",
            "27,23", "27,24", "27,25", "27,26", "27,27", "27,28", "27,29", "27,30", "27,31", "27,32", "27,33", "27,34",
            "28,23", "28,24", "28,25", "28,26", "28,27", "28,28", "28,29", "28,30", "28,32", "28,33", "28,34",
            "29,25", "29,26", "29,27", "29,28", "29,29", "29,30", "29,31", "29,33", "29,34",
            "30,24", "30,25", "30,26", "30,27", "30,28", "30,29",
            "31,24", "31,25", "31,26", "31,27", "31,28", "31,29",
            "32,24", "32,25", "32,26", "32,27", "32,28",
            "33,25", "33,26", "33,27", "33,28", "33,29",
            "34,24", "34,25", "34,26", "34,27", "34,28", "34,29", "34,30",
            "35,24", "35,25", "35,26", "35,27", "35,28", "35,29", "35,30",
            "36,24", "36,25", "36,26", "36,27", "36,28", "36,29", "36,30",
            "37,24", "37,25", "37,26", "37,27", "37,28", "37,29",
            "38,22", "38,23", "38,24", "38,25", "38,26", "38,27", "38,28", "38,29", "38,30",
            "39,21", "39,22", "39,23", "39,24", "39,27", "39,28", "39,29",
            "40,21", "40,22", "40,23", "40,24", "40,25", "40,27", "40,28", "40,29",
            "41,21", "41,22", "41,23", "41,24", "41,25", "41,27", "41,28", "41,29",
            "42,21", "42,22", "42,23", "42,24", "42,25", "42,27", "42,28", "42,29", "42,30",
            "43,21", "43,22", "43,23", "43,24", "43,25", "43,27", "43,28", "43,29", "43,30",
            "44,21", "44,22", "44,23", "44,24", "44,25", "44,27", "44,28", "44,29", "44,30",
            "45,21", "45,22", "45,23", "45,24", "45,25", "45,27", "45,29", "45,30",
            // Nuevas coordenadas
            "25,33", "25,32", "25,31", "25,30", "25,29", "25,28", "25,27", "25,26", "25,25", "25,24", "25,23",
            "24,23", "24,34",
            "23,24", "23,26", "23,27", "23,28", "23,29", "23,30", "23,31", "23,32", "23,33", "23,34",
            "22,25", "22,26", "22,27", "22,28", "22,29", "22,30", "22,31", "22,32", "22,33",
            "21,28", "21,29",
            "20,28", "20,29",
            "19,28", "19,29",
            "18,28", "18,29",
            "17,28", "17,29",
            "16,28", "16,29",
            "15,23", "15,24", "15,25", "15,26", "15,27", "15,28", "15,29", "15,30",
            "14,22", "14,23", "14,24", "14,25", "14,26", "14,27", "14,28", "14,29", "14,30",
            "13,20", "13,21", "13,22", "13,23", "13,24", "13,25", "13,26", "13,28", "13,29", "13,30",
            "12,20", "12,21", "12,22", "12,23", "12,24", "12,25", "12,26", "12,27", "12,28", "12,29", "12,30",
            "11,19", "11,20", "11,21", "11,22", "11,23", "11,24", "11,25", "11,26", "11,27", "11,28", "11,29", "11,30",
            "10,22", "10,23", "10,24", "10,25", "10,26", "10,27", "10,28", "10,29",
            "9,23", "9,24", "9,25", "9,26", "9,27", "9,28", "9,29",
            "8,22", "8,23", "8,24", "8,25", "8,26", "8,27", "8,28", "8,29",
            "7,22", "7,23", "7,24", "7,25", "7,26", "7,27", "7,28", "7,29", "7,30",
            "6,19", "6,20", "6,21", "6,22", "6,23", "6,24", "6,25", "6,26", "6,27", "6,28", "6,29", "6,30",
            "5,19", "5,20", "5,21", "5,22", "5,23", "5,24", "5,25", "5,26", "5,27", "5,28", "5,29", "5,30",
            "4,19", "4,20", "4,21", "4,22", "4,23", "4,24", "4,25", "4,26", "4,27", "4,29", "4,30",
            "3,22", "3,23", "3,24", "3,25", "3,26", "3,27", "3,28", "3,29", "3,30",
            "2,22", "2,23", "2,24", "2,25", "2,26", "2,27", "2,28", "2,29", "2,30",
            "1,30"));

    // Coordenadas de coleccionables para MAPA 4 (X,Y)
    private static final Set<String> VALID_COLLECTIBLE_COORDS_MAP4 = new HashSet<>(Arrays.asList(
            "20,0", "19,0", "18,0", "17,0", "17,1", "18,1", "19,1", "20,1", "21,1", "22,1", "23,1", "24,1", "25,1",
            "26,1", "27,1", "28,1", "29,1", "30,0", "30,1", "30,2", "29,2", "28,2", "27,2", "26,2", "25,2", "24,2",
            "22,2",
            "21,2", "20,2", "19,2", "18,2", "17,2", "17,4", "16,4", "18,4", "19,4", "20,4", "21,4", "22,4", "23,4",
            "24,4",
            "25,4", "26,4", "27,4", "28,4", "29,4", "30,4", "31,4", "31,5", "32,5", "33,5", "30,5", "29,5", "28,5",
            "27,5",
            "25,5", "24,5", "23,5", "22,5", "21,5", "20,5", "19,5", "18,5", "17,5", "16,5", "15,5", "14,5", "14,6",
            "13,6",
            "15,6", "16,6", "17,6", "18,6", "19,6", "20,6", "21,6", "22,6", "23,6", "24,6", "25,6", "26,6", "27,6",
            "28,6",
            "29,6", "30,6", "31,6", "32,6", "33,6", "34,6", "13,7", "12,7", "11,7", "14,7", "15,7", "16,7", "17,7",
            "18,7",
            "19,7", "20,7", "21,7", "22,7", "23,7", "24,7", "25,7", "26,7", "27,7", "28,7", "29,7", "30,7", "31,7",
            "32,7",
            "33,7", "34,7", "35,7", "36,7", "36,8", "35,8", "34,8", "33,8", "32,8", "31,8", "30,8", "29,8", "28,8",
            "27,8",
            "26,8", "25,8", "24,8", "23,8", "22,8", "21,8", "20,8", "19,8", "18,8", "17,8", "16,8", "15,8", "14,8",
            "13,8",
            "12,8", "11,8", "11,9", "10,9", "9,9", "8,9", "7,9", "6,9", "5,9", "4,9", "3,9", "2,9", "1,9", "0,9",
            "12,9",
            "13,9", "14,9", "15,9", "16,9", "17,9", "18,9", "19,9", "20,9", "21,9", "22,9", "23,9", "24,9", "25,9",
            "26,9",
            "27,9", "28,9", "29,9", "30,9", "31,9", "32,9", "33,9", "34,9", "35,9", "36,9", "37,9", "38,9", "39,9",
            "40,9",
            "41,9", "42,9", "43,9", "44,9", "45,9", "46,9", "47,9", "48,9", "49,9", "49,10", "48,10", "47,10", "46,10",
            "45,10", "44,10", "43,10", "42,10", "41,10", "40,10", "39,10", "38,10", "37,10", "36,10", "35,10", "34,10",
            "33,10", "32,10", "31,10", "30,10", "29,10", "28,10", "27,10", "26,10", "25,10", "24,10", "23,10", "22,10",
            "21,10", "20,10", "19,10", "18,10", "17,10", "16,10", "15,10", "14,10", "13,10", "12,10", "11,10", "10,10",
            "9,10", "8,10", "7,10", "6,10", "5,10", "4,10", "3,10", "2,10", "1,10", "0,10",
            "0,11", "0,12", "0,13", "0,14", "0,15", "0,18", "0,19", "0,20", "0,21", "0,22", "0,24", "0,25", "0,26",
            "0,27",
            "0,28", "1,15", "1,19", "1,28", "2,14", "2,19", "2,25", "2,26", "2,27", "2,28", "3,14", "3,16", "3,17",
            "3,18",
            "3,19", "3,20", "3,21", "3,22", "3,24", "3,25", "3,26", "3,27", "3,28", "4,13", "4,14", "4,15", "4,16",
            "4,17",
            "4,19", "4,20", "4,23", "4,24", "5,17", "5,18", "6,17", "6,18", "6,21", "6,22", "7,17", "7,18", "7,21",
            "7,22",
            "7,23", "7,24", "7,25", "7,26", "8,13", "8,14", "8,15", "8,16", "9,13", "9,14", "9,15", "9,16", "9,21",
            "9,22",
            "9,25", "9,26", "10,13", "10,14", "10,15", "10,16", "10,20", "10,21", "10,22", "10,23", "10,24", "10,25",
            "10,26", "10,27", "10,28", "11,13", "11,15", "11,16", "11,19", "11,21", "11,22", "11,23", "11,24", "11,25",
            "11,27", "11,28", "12,12", "12,13", "12,14", "12,15", "12,16", "12,19", "12,20", "12,21", "12,22", "12,23",
            "12,24", "12,25", "12,26", "12,27", "12,28", "13,13", "13,14", "13,16", "13,19", "13,20", "13,23", "13,27",
            "13,28", "14,13", "14,14", "14,15", "14,16", "14,19", "14,27", "14,28", "15,13", "15,14", "15,15", "15,16",
            "16,13", "16,14", "16,18", "16,19", "16,26", "16,27", "17,13", "17,14", "17,15", "17,16", "17,18", "17,20",
            "17,21", "17,22", "17,23", "17,24", "17,25", "17,27", "18,12", "18,14", "18,15", "18,16", "18,18", "18,19",
            "18,20", "18,22", "18,23", "18,24", "18,25", "18,26", "19,14", "19,15", "19,16", "19,18", "19,19", "19,20",
            "19,21", "19,22", "19,23", "19,24", "19,25", "19,26", "20,13", "20,14", "20,15", "20,16", "20,23", "20,26",
            "20,27", "21,13", "21,15", "21,16", "21,23", "21,26", "22,13", "22,15", "22,16", "22,23", "22,24", "22,25",
            "22,26", "22,27", "22,28", "23,16", "23,17", "23,18", "23,19", "23,20", "23,21", "23,22", "23,24", "23,25",
            "23,26", "23,27", "23,28", "24,14", "24,15", "24,16", "24,17", "24,18", "24,19", "24,20", "24,21", "24,24",
            "24,27", "24,28", "25,14", "25,15", "25,16", "25,21", "25,24", "26,14", "26,15", "26,16", "26,20", "26,21",
            "26,22", "26,23", "26,24", "27,14", "27,15", "27,16", "27,17", "27,18", "27,19", "27,20", "27,21", "27,24",
            "28,14", "28,15", "28,16", "28,17", "28,18", "28,19", "28,20", "28,22", "28,23", "28,24", "29,14", "29,15",
            "29,16", "30,14", "30,15", "30,16", "30,44", "30,45", "31,14", "31,16", "32,14", "32,15", "32,16", "33,14",
            "33,15", "33,16", "34,14", "34,15", "35,14", "35,15", "35,16", "36,14", "36,15", "36,16", "36,25", "37,14",
            "37,15", "37,16", "37,21", "37,22", "37,23", "37,24", "37,25", "38,14", "38,15", "38,16", "38,19", "38,20",
            "38,21", "38,22", "39,14", "39,15", "39,16", "39,19", "39,20", "40,14", "40,15", "40,16", "40,19", "40,20",
            "40,21", "40,23", "40,24", "40,25", "41,14", "41,15", "41,16", "41,18", "41,19", "41,24", "41,25", "42,15",
            "42,16", "42,17", "42,18", "42,19", "42,24", "42,25", "43,14", "43,15", "43,16", "43,17", "43,18", "43,19",
            "43,24", "43,25", "43,28", "43,29", "44,14", "44,15", "44,16", "44,17", "44,18", "44,19", "44,20", "44,21",
            "44,22", "44,24", "44,27", "44,28", "44,29", "44,30", "45,14", "45,15", "45,18", "45,20", "45,21", "45,22",
            "45,23", "45,24", "45,28", "45,30", "46,14", "46,15", "46,16", "46,17", "46,18", "46,19", "46,21", "46,23",
            "46,24", "46,25", "46,26", "46,27", "46,28", "46,29", "46,30", "47,14", "47,15", "47,16", "47,17", "47,18",
            "47,19", "47,21", "47,23", "47,24", "47,25", "47,26", "47,27", "47,28", "47,30", "48,14", "48,15", "48,16",
            "48,17", "48,18", "48,19", "48,21", "48,23", "48,24", "48,25", "48,27", "48,28", "48,29", "48,30", "49,14",
            "49,15", "49,16", "49,17", "49,18", "49,19", "49,21", "49,22", "49,23", "49,24", "49,25", "49,26", "49,27",
            "49,28", "49,29", "49,30"));

    // --- SISTEMA DE ENCUENTROS ---
    private Array<EncounterZone> encounterZones;

    public Pokemon playerPokemon; // Pokémon del jugador (public para acceso desde ScreenBatalla)
    public boolean canTriggerEncounter = true; // Evita múltiples encuentros seguidos (public para acceso desde
                                               // ScreenBatalla)
    public float encounterCooldown = 0f; // (public para acceso desde ScreenBatalla)
    private final float ENCOUNTER_COOLDOWN_TIME = 3.0f; // Segundos entre encuentros

    private final float PLAYER_START_X = 11f;
    private final float PLAYER_START_Y = 38f;

    // Custom spawn position (used when returning from Pokemon Center)
    private Float customSpawnX = null;
    private Float customSpawnY = null;

    private BitmapFont font;

    public enum GameState { // Se hace public para usar en otras clases si es necesario
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
        POKEMON_INFO
    }

    private float arceusMarkerTimer = 0f;
    private com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer;

    public GameState currentState; // public para acceso desde ScreenBatalla

    private DialogBox dialogBox;
    private Queue<String> dialogueQueue = new LinkedList<>();
    private Pokemon pendingEnemyForBattle; // enemigo pendiente a iniciar cuando el diálogo termine
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
    private boolean isFadingIn = true; // Start with fade-in

    private Texture blackPixelTexture;

    // Professor Yoel Animation Assets
    private Texture yoelTexture;
    private Image yoelImage;

    // **VARIABLE ELIMINADA: private boolean pendingPokemonSelection = false;**
    private String mapPath;
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
    private Array<Collectible> collectibles;
    private Random random;

    // DEBUG: Set para que el usuario pueda grabar IDs de tiles
    private Set<Integer> recordedTiles = new HashSet<>();

    // Receptionist
    private Texture recepcionistaTexture;
    private Image recepcionistaImage;
    private Table choiceBoxTable;
    private int selectedChoiceIndex = 0;
    private Runnable onDialogCompleteAction;

    // Track source map when entering Pokemon Center
    private String sourceMapBeforePokemonCenter = null;

    // --- CIRCUS BABY NPC (ANIMATED) ---
    private Texture circusBabyTexture1;
    private Texture circusBabyTexture2;
    private Image circusBabyActor;
    private float circusBabyAnimTimer = 0;
    private boolean circusBabyFrame1 = true;

    // --- FUNTIME FOXY NPC (ANIMATED) ---
    private Texture funtimeFoxyTexture1;
    private Texture funtimeFoxyTexture2;
    private Image funtimeFoxyActor;
    private float funtimeFoxyAnimTimer = 0;
    private boolean funtimeFoxyFrame1 = true;

    // --- FUNTIME FREDDY NPC (ANIMATED) ---
    private Texture funtimeFreddyTexture1;
    private Texture funtimeFreddyTexture2;
    private Image funtimeFreddyActor;
    private float funtimeFreddyAnimTimer = 0;
    private boolean funtimeFreddyFrame1 = true;

    // --- MAP 2 BATTLE NPCs (ANIMATED) ---
    private Texture giornoTexture1;
    private Texture giornoTexture2;
    private Image giornoActor;
    private float giornoAnimTimer = 0;
    private boolean giornoFrame1 = true;

    private Texture jotaroTexture1;
    private Texture jotaroTexture2;
    private Image jotaroActor;
    private float jotaroAnimTimer = 0;
    private boolean jotaroFrame1 = true;

    private Texture kanekiTexture1;
    private Texture kanekiTexture2;
    private Image kanekiActor;
    private float kanekiAnimTimer = 0;
    private boolean kanekiFrame1 = true;

    // Battle state tracking for Map 2 NPCs
    private boolean giornoDefeated = false;
    private boolean jotaroDefeated = false;
    private boolean kanekiDefeated = false;

    // --- MAP 4 NPCs ---
    private Texture jesucristoTexture1, jesucristoTexture2;
    private Image jesucristoActor;
    private float jesucristoAnimTimer = 0;
    private boolean jesucristoFrame1 = true;
    private boolean jesucristoDefeated = false;

    private Texture aftonTexture1, aftonTexture2;
    private Image aftonActor;
    private float aftonAnimTimer = 0;
    private boolean aftonFrame1 = true;
    private boolean aftonDefeated = false;

    // --- SUBMENUS ---
    private Table inventorySubmenuTable;

    private int selectedInventorySubmenuIndex = 0;

    private Table pokedexActionMenuTable;
    private int selectedPokedexActionIndex = 0;
    private Table pokemonInfoTable;
    private Pokemon selectedPokemonForHealing = null;

    private int lastLogTileX = -1;
    private int lastLogTileY = -1;

    // Debug Tool: List to record tiles
    private List<String> recordedTilesList = new ArrayList<>();

    // Variables para el fondo de batalla dínámico
    private String pendingBattleBackground = null;

    /**
     * Constructor único: siempre inicia en estado DIALOGUE.
     */
    public ScreenMapaTiled(Proyecto game) {
        this(game, "Mapa/mapa11.tmx", null, null, null, GameState.DIALOGUE);
    }

    // Nuevo constructor que acepta el nombre del pokémon inicial (viene de la
    // pantalla de selección)
    public ScreenMapaTiled(Proyecto game, String initialPokemonName) {
        this(game, "Mapa/mapa11.tmx", null, null, initialPokemonName, GameState.DIALOGUE);
    }

    public ScreenMapaTiled(Proyecto game, String mapPath, Inventario inventory, Pokemon playerPokemon,
            String initialPokemonName, GameState initialState) {
        this(game, mapPath, inventory, playerPokemon, initialPokemonName, initialState, null, null);
    }

    // Constructor with custom spawn position
    public ScreenMapaTiled(Proyecto game, String mapPath, Inventario inventory, Pokemon playerPokemon,
            String initialPokemonName, GameState initialState, Float spawnX, Float spawnY) {
        this(game, mapPath, inventory, playerPokemon, initialPokemonName, initialState, spawnX, spawnY, null);
    }

    // Full constructor with custom spawn position and source map
    public ScreenMapaTiled(Proyecto game, String mapPath, Inventario inventory, Pokemon playerPokemon,
            String initialPokemonName, GameState initialState, Float spawnX, Float spawnY, String sourceMap) {
        this.game = game;
        this.mapPath = mapPath;
        this.currentState = initialState;
        this.initialPokemonName = initialPokemonName;
        this.customSpawnX = spawnX;
        this.customSpawnY = spawnY;
        this.sourceMapBeforePokemonCenter = sourceMap;

        if (inventory != null)
            this.playerInventory = inventory;
        if (playerPokemon != null)
            this.playerPokemon = playerPokemon;

        // Iniciar conexión multijugador (si no está ya conectada)
        sodyl.proyecto.net.NetworkClient.getInstance().connect();
    }

    // **CONSTRUCTOR ELIMINADO: public ScreenMapaTiled(Proyecto game, GameState
    // initialState)**

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

    @Override
    public void show() {
        // Si ya estamos inicializados y solo volvemos de una batalla, no reinicializar
        // todo
        if (batch != null && stage != null && camera != null && renderer != null) {
            // Asegurar que el estado esté correcto
            if (currentState == GameState.BATTLE) {
                currentState = GameState.FREE_ROAMING;
            }

            // Limpiar cualquier input processor anterior
            Gdx.input.setInputProcessor(null);

            // Restaurar el input processor
            InputMultiplexer multiplexer = new InputMultiplexer();
            multiplexer.addProcessor(this);
            if (uiStage != null) {
                multiplexer.addProcessor(uiStage);
            }
            Gdx.input.setInputProcessor(multiplexer);

            // Asegurar que la cámara esté actualizada
            if (characterActor != null && camera != null) {
                clampCamera();
                camera.update();
            }

            // Asegurar que los viewports estén actualizados
            if (stage != null && stage.getViewport() != null) {
                stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
            }
            if (uiStage != null && uiStage.getViewport() != null) {
                uiStage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
            }

            // Limpiar las teclas de movimiento para evitar que el jugador se mueva solo
            clearMovementKeys();

            Gdx.app.log("MAP_SCREEN", "Pantalla del mapa restaurada después de batalla. Estado: " + currentState);
            if (game.getScreen() != this) {
                return;
            }
            return;
        }

        // Inicialización normal (primera vez)
        batch = new SpriteBatch();
        shapeRenderer = new com.badlogic.gdx.graphics.glutils.ShapeRenderer();
        camera = new OrthographicCamera();
        random = new Random();

        // --- Carga de Datos ---
        if (playerInventory == null) {
            playerInventory = new Inventario();
            // Cargar inventario del usuario actual
            playerInventory.load(sodyl.proyecto.clases.UserManager.getCurrentUser());
        }

        // Cargar Pokedex
        sodyl.proyecto.clases.Pokedex.load();

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

        try {
            map = new TmxMapLoader().load(mapPath);
            renderer = new OrthogonalTiledMapRenderer(map, UNIT_SCALE);
            mapWidthTiles = map.getProperties().get("width", Integer.class);
            mapHeightTiles = map.getProperties().get("height", Integer.class);
        } catch (Exception e) {
            Gdx.app.error("MAPA", "Error al cargar el mapa Tiled. Verifica la ruta o el archivo .tmx.", e);
            mapWidthTiles = 50;
            mapHeightTiles = 50;
        }

        // --- CARGA DE COLISIONES ---
        collisionRects = new Array<>();

        // Try multiple possible collision layer names, but ONLY from ObjectGroup layers
        String[] possibleCollisionLayerNames = { "Capa de Objetos 1", "Colisiones", "COLISIONES" };
        MapLayer collisionObjectLayer = null;

        // Búsqueda robusta por nombre (ignora mayúsculas si es necesario)
        for (String targetName : possibleCollisionLayerNames) {
            for (MapLayer layer : map.getLayers()) {
                if (layer.getName() != null && layer.getName().equalsIgnoreCase(targetName)) {
                    // Only accept layers that are ObjectGroup layers (contain MapObjects, not
                    // tiles)
                    if (!(layer instanceof com.badlogic.gdx.maps.MapGroupLayer)
                            && !(layer instanceof com.badlogic.gdx.maps.tiled.TiledMapTileLayer)) {
                        collisionObjectLayer = layer;
                        Gdx.app.log("COLLISION", "Collision OBJECT layer found: " + layer.getName());
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
                    // Handle polygon collisions (for diagonal walls, etc.)
                    // For now, we'll skip these but log them
                    Gdx.app.log("COLLISION", "Polygon collision object found, skipping for now");
                }
            }
            Gdx.app.log("COLLISION", "Loaded " + collisionRects.size + " collision rectangles");
        } else {
            Gdx.app.log("COLLISION", "No collision OBJECT layer found in map");
        }

        if (mapPath.equals("Mapa/mapa11.tmx")) {
            // Ensure transition tile is free (Pokemon Center)
            removeCollisionsAt(38, 37);
            removeCollisionsAt(39, 37);

            // Asegurar que la salida a Mapa 2 (tiles 0,17-21) esté libre de colisiones
            // Coordenadas LibGDX: Y = 28 a 32
            for (int y = 28; y <= 33; y++) {
                removeCollisionsAt(0, y);
            }

            // Salida a Mapa 4 (tiles 22-27, 49)
            for (int x = 22; x <= 27; x++) {
                removeCollisionsAt(x, 49);
            }
        }

        if (mapPath.equals("Mapa/MAPA 4.tmx")) {
            // Entrada/Salida desde Mapa 1 (tiles 22-27, 0)
            for (int x = 22; x <= 27; x++) {
                removeCollisionsAt(x, 0);
                removeCollisionsAt(x, 1); // Margen de seguridad
            }
        }

        if (mapPath.equals("Mapa/Mapa2.tmx")) {
            // Asegurar que la salida a Mapa 1 (borde derecho, x=49) esté libre de
            // colisiones si es necesario
            // Simplemente removemos colisiones en el borde derecho en un rango amplio
            for (int y = 0; y < mapHeightTiles; y++) {
                removeCollisionsAt(49, y);
            }

            // Ensure Pokemon Center entrance is free (tile 45, 28 and surrounding)
            removeCollisionsAt(45, 28);
            removeCollisionsAt(44, 28);
            removeCollisionsAt(46, 28);

            // --- AÑADIR COLISIONES MANUALES MAPA 2 ---
            int[][] map2Tiles = {
                    { 47, 28 }, { 43, 28 }, { 44, 30 }, { 44, 31 }, { 45, 31 }, { 46, 31 },
                    { 47, 31 }, { 48, 31 }, { 49, 31 }, { 48, 28 },
                    { 43, 44 }, { 44, 45 }, { 44, 46 }, { 44, 47 }, { 44, 48 }, { 44, 49 }, { 44, 50 }, { 44, 51 },
                    { 44, 52 }, { 44, 53 }, { 44, 54 },
                    { 45, 51 }, { 41, 52 }, { 40, 52 }, { 39, 52 }, { 37, 52 }, { 37, 50 }, { 40, 51 }, { 40, 50 },
                    { 40, 49 }, { 40, 48 }, { 40, 47 },
                    { 40, 46 }, { 40, 45 }, { 40, 45 }, { 40, 44 }, { 38, 44 }, { 38, 43 }, { 38, 42 }, { 38, 41 },
                    { 38, 40 }, { 38, 39 }, { 38, 39 },
                    { 39, 40 }, { 46, 43 }, { 46, 44 }, { 46, 45 }, { 46, 46 }, { 46, 47 }, { 46, 48 }, { 46, 49 },
                    { 46, 50 }, { 46, 51 }, { 46, 52 },
                    { 46, 53 }, { 46, 54 }, { 49, 23 }, { 49, 22 }, { 49, 21 }, { 49, 18 }, { 48, 18 }, { 47, 18 },
                    { 46, 18 }
            };
            for (int[] tile : map2Tiles) {
                addCollisionAt(tile[0], tile[1]);
            }
        }

        // --- Configuración de Animaciones y Personaje ---
        TextureRegion initialFrame;
        try {
            atlas = new TextureAtlas(Gdx.files.internal("sprites/jugador/Textures.atlas"));
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

        // Use custom spawn position if provided
        if (customSpawnX != null && customSpawnY != null) {
            startX = customSpawnX;
            startY = customSpawnY;
        } else if (mapPath.contains("Centro Pokemon interior")) {
            startX = 11f; // Adjusted to center the map view
            startY = 4f; // Spawn at lower tile, exit is above
        }

        characterActor.setPosition(startX, startY);

        stage = new Stage(new FitViewport(30, 25, camera));

        uiStage = new Stage(new FitViewport(Proyecto.PANTALLA_W, Proyecto.PANTALLA_H));

        Pixmap pixmapOverlay = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmapOverlay.setColor(Color.BLACK);
        pixmapOverlay.fill();
        blackPixelTexture = new Texture(pixmapOverlay);
        pixmapOverlay.dispose();

        // --- Load Professor Yoel Texture ---
        try {
            yoelTexture = new Texture(Gdx.files.internal("imagenes/yoel.png"));
            yoelImage = new Image(yoelTexture);
            // Initial position off-screen (right)
            // We'll set size/position when animation starts, but good to have defaults
        } catch (Exception e) {
            Gdx.app.error("ASSETS", "Could not load yoel.png", e);
        }

        // --- INICIALIZACIÓN DE DATOS GLOBALES ---
        Objeto.initializeObjetos();
        Pokemones.initialize(); // Inicializar Pokémon
        if (playerInventory == null)
            playerInventory = new Inventario();

        availableRecipes = new Array<>(Objeto.getAllRecipes().values().toArray(new Recipe[0]));

        loadItemTextures();

        // --- INICIALIZAR POKÉMON DEL JUGADOR ---
        // Asignar automáticamente Rowlet como Pokémon inicial
        if (playerPokemon == null) {
            if (this.initialPokemonName != null) {
                playerPokemon = Pokemones.getPokemon(this.initialPokemonName);
                // Nivel de investigación inicial: 2
                playerPokemon.setNivel(2);
                playerPokemon.actualizarAtributos();
            } else {
                // Dar Rowlet por defecto
                playerPokemon = Pokemones.getPokemon("Rowlet");
                playerPokemon.setNivel(2);
                playerPokemon.actualizarAtributos();
                playerPokemon.setActualHP(playerPokemon.getMaxHp());
            }
        }

        // Registrar en la Pokedex
        try {
            sodyl.proyecto.clases.Pokedex.addSeen(playerPokemon.getEspecie());
            sodyl.proyecto.clases.Pokedex.addCollected(playerPokemon);
        } catch (Exception ignored) {
        }

        // Dar 5 Pokéballs al jugador al inicio
        playerInventory.addObjeto(101, 5); // ID 101 = Pokeball

        // --- CARGAR RECEPCIONISTA (SOLO EN CENTRO POKEMON) ---
        if (mapPath.contains("Centro Pokemon interior")) {
            try {
                recepcionistaTexture = new Texture(Gdx.files.internal("imagenes/recepcionista1.png"));
                recepcionistaImage = new Image(recepcionistaTexture);
                // Tile 7, 7 -> x=7, y=7
                // Nota: Asegúrate de que 7,7 sea correcto. LibGDX suele tener origen (0,0)
                // abajo-izq.
                float tileWidth = map.getProperties().get("tilewidth", Integer.class);
                float tileHeight = map.getProperties().get("tileheight", Integer.class);

                float npcX = 2 * tileWidth * UNIT_SCALE;
                float npcY = 4 * tileHeight * UNIT_SCALE;

                recepcionistaImage.setSize(2f, 2f); // Un poco mas alta
                recepcionistaImage.setPosition(npcX, npcY);
                stage.addActor(recepcionistaImage); // Agregar al stage del mundo

                // Agregar Colisión Manualmente
                Rectangle npcRect = new Rectangle(npcX + 1.0f, npcY, 1.6f, 1.6f);
                collisionRects.add(npcRect);

            } catch (Exception e) {
                Gdx.app.error("ASSETS", "No se pudo cargar recepcionista.png", e);
            }
        }

        // --- CARGAR NPC CIRCUS BABY (Solo en Mapa 1) ---
        if (mapPath.equals("Mapa/mapa11.tmx")) {
            try {
                circusBabyTexture1 = new Texture(Gdx.files.internal("imagenes/CircusBaby.png"));
                circusBabyTexture2 = new Texture(Gdx.files.internal("imagenes/CircusBaby2.png"));

                circusBabyActor = new Image(circusBabyTexture1);

                // Posición Tile 27, 34
                float tileWidth = map.getProperties().get("tilewidth", Integer.class);
                float tileHeight = map.getProperties().get("tileheight", Integer.class);
                float npcX = 26 * tileWidth * UNIT_SCALE;
                float npcY = 34 * tileHeight * UNIT_SCALE;

                // Ajustar tamaño (Asumimos tamaño de personaje estándar o un poco más grande)
                circusBabyActor.setSize(5f, 3f);
                circusBabyActor.setPosition(npcX, npcY);

                stage.addActor(circusBabyActor);

                // Agregar Colisión Manualmente
                Rectangle npcRect = new Rectangle(0f, 0f, 0f, 0f); // Ajustado para bloquear el paso
                collisionRects.add(npcRect);

                Gdx.app.log("NPC", "Circus Baby cargado en (27, 34)");
            } catch (Exception e) {
                Gdx.app.error("NPC", "No se pudo cargar CircusBaby.png", e);
            }

            // --- CARGAR NPC FUNTIME FREDDY (Solo en Mapa 1) ---
            // --- FUNTIME FOXY NPC (ANIMATED) - TILE 16, 24 ---
            try {
                funtimeFoxyTexture1 = new Texture(Gdx.files.internal("imagenes/fntFoxy1.png"));
                funtimeFoxyTexture2 = new Texture(Gdx.files.internal("imagenes/fntFoxy2.png"));

                funtimeFoxyActor = new Image(funtimeFoxyTexture1); // Start with frame 1

                // Posición Tile 16, 24
                float tileWidth = map.getProperties().get("tilewidth", Integer.class);
                float tileHeight = map.getProperties().get("tileheight", Integer.class);

                float npcX = 16 * tileWidth * UNIT_SCALE;
                float npcY = 24 * tileHeight * UNIT_SCALE;

                // Ajustar tamaño para que luzca bien
                funtimeFoxyActor.setSize(5f, 3f);
                // Adjust position manually as requested previously
                funtimeFoxyActor.setPosition(npcX - 1f, npcY);

                stage.addActor(funtimeFoxyActor);

                // Add collision at proper NPC spot
                Rectangle npcRect = new Rectangle(0f, 0f, 0f, 0f);
                collisionRects.add(npcRect);

                Gdx.app.log("NPC", "Funtime Foxy (Animated) loaded at (16, 24)");
            } catch (Exception e) {
                Gdx.app.error("ASSETS", "Could not load FuntimeFoxy images", e);
            }

            // --- FUNTIME FREDDY NPC (ANIMATED) - TILE 23, 13 ---
            try {
                funtimeFreddyTexture1 = new Texture(Gdx.files.internal("imagenes/FuntimeFreddy.png"));
                funtimeFreddyTexture2 = new Texture(Gdx.files.internal("imagenes/FuntimeFreddy2.png"));

                funtimeFreddyActor = new Image(funtimeFreddyTexture1); // Start with frame 1

                // Posición Tile 23, 13
                float tileWidth = map.getProperties().get("tilewidth", Integer.class);
                float tileHeight = map.getProperties().get("tileheight", Integer.class);

                float npcX = 19 * tileWidth * UNIT_SCALE;
                float npcY = 13 * tileHeight * UNIT_SCALE;

                // Ajustar tamaño para que luzca bien
                funtimeFreddyActor.setSize(5f, 3f);
                funtimeFreddyActor.setPosition(npcX, npcY);

                stage.addActor(funtimeFreddyActor);

                // Add collision at proper NPC spot
                Rectangle npcRect = new Rectangle(npcX, npcY, 0f, 0f);
                collisionRects.add(npcRect);

                Gdx.app.log("NPC", "Funtime Freddy (Animated) loaded at (23, 13)");
            } catch (Exception e) {
                Gdx.app.error("ASSETS", "Could not load FuntimeFreddy images", e);
            }
        }

        // --- CARGAR NPCs PARA MAPA 2 (BATTLE NPCs) ---
        if (mapPath.equals("Mapa/Mapa2.tmx")) {
            float tileWidth = map.getProperties().get("tilewidth", Integer.class);
            float tileHeight = map.getProperties().get("tileheight", Integer.class);

            // --- GIORNO NPC (ANIMATED) - TILE 4, 51 ---
            try {
                giornoTexture1 = new Texture(Gdx.files.internal("spritesMapa2/giorno1.png"));
                giornoTexture2 = new Texture(Gdx.files.internal("spritesMapa2/giorno2.png"));

                giornoActor = new Image(giornoTexture1);

                float npcX = 4 * tileWidth * UNIT_SCALE;
                float npcY = 51 * tileHeight * UNIT_SCALE;

                giornoActor.setSize(6f, 4f);
                giornoActor.setPosition(npcX, npcY);

                stage.addActor(giornoActor);

                // Add collision rectangle for the tile
                Rectangle npcRect = new Rectangle(npcX, npcY, tileWidth * UNIT_SCALE, tileHeight * UNIT_SCALE);
                collisionRects.add(npcRect);

                Gdx.app.log("NPC", "Giorno (Battle NPC) loaded at (4, 51)");
            } catch (Exception e) {
                Gdx.app.error("ASSETS", "Could not load Giorno images", e);
            }

            // --- JOTARO NPC (ANIMATED) - TILE 11, 33 ---
            try {
                jotaroTexture1 = new Texture(Gdx.files.internal("spritesMapa2/jotaro1.png"));
                jotaroTexture2 = new Texture(Gdx.files.internal("spritesMapa2/jotaro2.png"));

                jotaroActor = new Image(jotaroTexture1);

                float npcX = 10 * tileWidth * UNIT_SCALE;
                float npcY = 33 * tileHeight * UNIT_SCALE;

                jotaroActor.setSize(6f, 4f);
                jotaroActor.setPosition(npcX, npcY);

                stage.addActor(jotaroActor);

                // Add collision rectangle for the tile
                Rectangle npcRect = new Rectangle(npcX, npcY, tileWidth * UNIT_SCALE, tileHeight * UNIT_SCALE);
                collisionRects.add(npcRect);

                Gdx.app.log("NPC", "Jotaro (Battle NPC) loaded at (10, 33)");
            } catch (Exception e) {
                Gdx.app.error("ASSETS", "Could not load Jotaro images", e);
            }

            // --- KANEKI NPC (ANIMATED) - TILE 42, 44 ---
            try {
                kanekiTexture1 = new Texture(Gdx.files.internal("spritesMapa2/kaneki1.png"));
                kanekiTexture2 = new Texture(Gdx.files.internal("spritesMapa2/kaneki2.png"));

                kanekiActor = new Image(kanekiTexture1);

                float npcX = 41 * tileWidth * UNIT_SCALE;
                float npcY = 44 * tileHeight * UNIT_SCALE;

                kanekiActor.setSize(6f, 4f);
                kanekiActor.setPosition(npcX, npcY);

                stage.addActor(kanekiActor);

                // Add collision rectangle for the tile
                Rectangle npcRect = new Rectangle(npcX, npcY, tileWidth * UNIT_SCALE, tileHeight * UNIT_SCALE);
                collisionRects.add(npcRect);

                Gdx.app.log("NPC", "Kaneki (Battle NPC) loaded at (41, 44)");
            } catch (Exception e) {
                Gdx.app.error("ASSETS", "Could not load Kaneki images", e);
            }

            // --- FREDDY NPC (INFO) - TILE 29, 29 ---
            try {
                freddyMap2Texture1 = new Texture(Gdx.files.internal("spritesMapa2/freddy1.png"));
                freddyMap2Texture2 = new Texture(Gdx.files.internal("spritesMapa2/freddy2.png"));
                freddyMap2Texture1.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                freddyMap2Texture2.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

                freddyMap2Actor = new Image(new TextureRegionDrawable(new TextureRegion(freddyMap2Texture1)));
                freddyMap2Actor.setSize(6f, 4f);

                float fX = 29 * tileWidth * UNIT_SCALE;
                float fY = 29 * tileHeight * UNIT_SCALE;
                freddyMap2Actor.setPosition(fX, fY);

                stage.addActor(freddyMap2Actor);

                // Add collision
                collisionRects.add(new Rectangle(fX, fY, tileWidth * UNIT_SCALE, tileHeight * UNIT_SCALE));
                Gdx.app.log("NPC", "Freddy Map 2 loaded at (29, 29)");

            } catch (Exception e) {
                Gdx.app.error("NPC", "Error loading Freddy Map 2 NPC", e);
            }
        }

        // --- CARGAR NPCs PARA MAPA 3 ---
        if (mapPath.equals("Mapa/MAPA3.tmx")) {
            float tileWidth = map.getProperties().get("tilewidth", Integer.class);
            float tileHeight = map.getProperties().get("tileheight", Integer.class);

            // --- PENNYWISE NPC (ANIMATED) - TILE 14, 23 ---
            try {
                pennywiseTexture1 = new Texture(Gdx.files.internal("spritesMapa3/pennywise1.png"));
                pennywiseTexture2 = new Texture(Gdx.files.internal("spritesMapa3/pennywise2.png"));

                pennywiseActor = new Image(pennywiseTexture1);

                float npcX = 14 * tileWidth * UNIT_SCALE;
                float npcY = 23 * tileHeight * UNIT_SCALE;

                pennywiseActor.setSize(6f, 4f);
                pennywiseActor.setPosition(npcX, npcY);

                stage.addActor(pennywiseActor);

                // Add collision rectangle for the tile
                Rectangle npcRect = new Rectangle(npcX, npcY, tileWidth * UNIT_SCALE, tileHeight * UNIT_SCALE);
                collisionRects.add(npcRect);

                Gdx.app.log("NPC", "Pennywise loaded at (14, 23)");
            } catch (Exception e) {
                Gdx.app.error("ASSETS", "Could not load Pennywise images", e);
            }

            // --- DON VALERIO NPC (ANIMATED) - TILE 44, 30 ---
            try {
                donValerioTexture1 = new Texture(Gdx.files.internal("spritesMapa3/donValerio1.png"));
                donValerioTexture2 = new Texture(Gdx.files.internal("spritesMapa3/donValerio2.png"));

                donValerioActor = new Image(donValerioTexture1);

                float npcX = 44 * tileWidth * UNIT_SCALE;
                float npcY = 30 * tileHeight * UNIT_SCALE;

                donValerioActor.setSize(6f, 4f);
                donValerioActor.setPosition(npcX, npcY);

                stage.addActor(donValerioActor);

                // Add collision rectangle for the tile
                Rectangle npcRect = new Rectangle(npcX, npcY, tileWidth * UNIT_SCALE, tileHeight * UNIT_SCALE);
                collisionRects.add(npcRect);

                Gdx.app.log("NPC", "Don Valerio loaded at (44, 30)");
            } catch (Exception e) {
                Gdx.app.error("ASSETS", "Could not load Don Valerio images", e);
            }

            // --- BONNIE NPC (INFO) - TILE 2, 28 ---
            try {
                bonnieTexture1 = new Texture(Gdx.files.internal("spritesMapa3/bonnie1.png"));
                bonnieTexture2 = new Texture(Gdx.files.internal("spritesMapa3/bonnie2.png"));
                bonnieTexture1.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                bonnieTexture2.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

                bonnieActor = new Image(new TextureRegionDrawable(new TextureRegion(bonnieTexture1)));
                bonnieActor.setSize(6f, 4f);

                float iX = 4 * tileWidth * UNIT_SCALE;
                float iY = 28 * tileHeight * UNIT_SCALE;
                bonnieActor.setPosition(iX, iY);

                stage.addActor(bonnieActor);

                // Add collision
                collisionRects.add(new Rectangle(iX, iY, tileWidth * UNIT_SCALE, tileHeight * UNIT_SCALE));
                Gdx.app.log("NPC", "Bonnie loaded at (3, 28)");

            } catch (Exception e) {
                Gdx.app.error("NPC", "Error loading Bonnie NPC", e);
            }
        }

        // --- MAP 4 NPCS ---
        if (mapPath.equals("Mapa/MAPA 4.tmx")) {
            // Jesucristo - Tile (35, 29)
            try {
                jesucristoTexture1 = new Texture(Gdx.files.internal("spritesMapa4/jesucristo1.png"));
                jesucristoTexture2 = new Texture(Gdx.files.internal("spritesMapa4/jesucristo2.png"));
                // Set filters
                jesucristoTexture1.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                jesucristoTexture2.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

                jesucristoActor = new Image(new TextureRegionDrawable(new TextureRegion(jesucristoTexture1)));
                jesucristoActor.setSize(6f, 4f); // Adjust size relative to UNIT_SCALE

                float tileWidth = map.getProperties().get("tilewidth", Integer.class);
                float tileHeight = map.getProperties().get("tileheight", Integer.class);

                float jX = 0 * tileWidth * UNIT_SCALE;
                float jY = 4 * tileHeight * UNIT_SCALE;
                jesucristoActor.setPosition(jX, jY);

                stage.addActor(jesucristoActor);

                // Add collision
                collisionRects.add(new Rectangle(1 * tileWidth * UNIT_SCALE, 4 * tileHeight * UNIT_SCALE,
                        tileWidth * UNIT_SCALE, tileHeight * UNIT_SCALE));
                collisionRects.add(new Rectangle(1 * tileWidth * UNIT_SCALE, 5 * tileHeight * UNIT_SCALE,
                        tileWidth * UNIT_SCALE, tileHeight * UNIT_SCALE));
                Gdx.app.log("NPC", "Jesucristo loaded at (0, 4) with collisions at (1, 4) and (1, 5)");

            } catch (Exception e) {
                Gdx.app.error("NPC", "Error loading Jesucristo NPC", e);
            }

            // Afton - Tile (44, 38)
            try {
                aftonTexture1 = new Texture(Gdx.files.internal("spritesMapa4/afton1.png"));
                aftonTexture2 = new Texture(Gdx.files.internal("spritesMapa4/afton2.png"));

                aftonTexture1.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                aftonTexture2.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

                aftonActor = new Image(new TextureRegionDrawable(new TextureRegion(aftonTexture1)));
                aftonActor.setSize(6f, 4f);

                float tileWidth = map.getProperties().get("tilewidth", Integer.class);
                float tileHeight = map.getProperties().get("tileheight", Integer.class);

                float aX = 45 * tileWidth * UNIT_SCALE;
                float aY = 4 * tileHeight * UNIT_SCALE;
                aftonActor.setPosition(aX, aY);

                stage.addActor(aftonActor);

                // Add collision
                collisionRects.add(new Rectangle(46 * tileWidth * UNIT_SCALE, 4 * tileHeight * UNIT_SCALE,
                        tileWidth * UNIT_SCALE, tileHeight * UNIT_SCALE));
                collisionRects.add(new Rectangle(46 * tileWidth * UNIT_SCALE, 5 * tileHeight * UNIT_SCALE,
                        tileWidth * UNIT_SCALE, tileHeight * UNIT_SCALE));
                Gdx.app.log("NPC", "Afton loaded at (45, 4) with collisions at (46, 4) and (46, 5)");

            } catch (Exception e) {
                Gdx.app.error("NPC", "Error loading Afton NPC", e);
            }

            // Info NPC - Tile (19, 12)
            try {
                map4InfoNpcTexture1 = new Texture(Gdx.files.internal("spritesMapa4/foxy1.png"));
                map4InfoNpcTexture2 = new Texture(Gdx.files.internal("spritesMapa4/foxy2.png"));
                map4InfoNpcTexture1.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                map4InfoNpcTexture2.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

                map4InfoNpcActor = new Image(new TextureRegionDrawable(new TextureRegion(map4InfoNpcTexture1)));
                map4InfoNpcActor.setSize(6f, 4f);

                float tileWidth = map.getProperties().get("tilewidth", Integer.class);
                float tileHeight = map.getProperties().get("tileheight", Integer.class);

                float iX = 21 * tileWidth * UNIT_SCALE;
                float iY = 12 * tileHeight * UNIT_SCALE;
                map4InfoNpcActor.setPosition(iX, iY);

                stage.addActor(map4InfoNpcActor);

                // Add collision
                collisionRects.add(new Rectangle(iX, iY, tileWidth * UNIT_SCALE, tileHeight * UNIT_SCALE));
                Gdx.app.log("NPC", "Map 4 Info NPC loaded at (20, 12)");

            } catch (Exception e) {
                Gdx.app.error("NPC", "Error loading Map 4 Info NPC", e);
            }
        }

        // --- CARGAR ZONAS DE ENCUENTRO ---
        encounterZones = new Array<>();

        loadEncounterZones();

        // --- INICIALIZACIÓN DE OBJETOS RECOLECTABLES (SPAWN EN TILES DE HIERBA) ---
        collectibles = new Array<>();
        if (currentState != GameState.DIALOGUE)

        {
            spawnCollectiblesOnGrassTiles(60);
        }

        stage.addActor(characterActor);

        // --- INICIALIZAR MENÚ DE PAUSA, DIÁLOGO, CRAFTEO E INVENTARIO ---
        initializePauseMenu();

        initializeCraftingMenu();

        initializeInventoryMenu();

        initializePokedexMenu();

        initializeInventorySubmenu(); // Nuevo Submenú

        initializePokedexActionMenu(); // Nuevo Menú de Acción Pokedex

        initializePokemonInfoTable(); // Tabla de información de Pokemon

        initializeChoiceBox();

        // Inicializar DialogBox
        if (dialogBox == null)

        {
            dialogBox = new DialogBox(uiStage, font);
        }

        // Lógica condicional del diálogo:
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(this);
        multiplexer.addProcessor(uiStage);

        if (currentState == GameState.DIALOGUE && mapPath.equals("Mapa/mapa11.tmx")) {
            // Check if it's the very first dialogue (Professor Yoel intro)
            // We can check if dialogueQueue is empty (it hasn't been filled yet) or just by
            // context
            // Since we are in constructor, it IS the start.
            startProfessorYoelAnimation();
        }

        // El InputProcessor siempre debe activarse para manejar el diálogo o el
        // movimiento
        Gdx.input.setInputProcessor(multiplexer);

        // Ajustar la cámara inicial y centrarla en el personaje
        camera.setToOrtho(false, Proyecto.PANTALLA_W * UNIT_SCALE, Proyecto.PANTALLA_H * UNIT_SCALE);
        camera.viewportWidth = 30;
        camera.viewportHeight = 25;

        clampCamera();
        camera.update();

    }

    private void loadItemTextures() {
        for (Objeto item : Objeto.getAllObjects().values()) {
            try {
                Texture texture = new Texture(Gdx.files.internal(item.getTexturePath()));
                itemTextures.put(item.getId(), texture);
            } catch (Exception e) {
                Gdx.app.error("TEXTURES",
                        "No se pudo cargar la textura para " + item.getNombre() + ": " + item.getTexturePath(), e);
            }
        }
    }

    /**
     * Carga las zonas de encuentro desde tiles de flores en la capa "Nivel 1".
     * Busca tiles de flores con pétalos y crea zonas de encuentro en cada uno.
     */
    private void loadEncounterZones() {
        if (map == null) {
            Gdx.app.error("ENCOUNTER", "El mapa no está cargado. No se pueden cargar zonas de encuentro.");
            return;
        }

        // Buscar la capa "Nivel 1" por nombre
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
            // Intentar por índice si no se encuentra por nombre
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

        // IDs de tiles de flores con pétalos (necesitarás ajustar estos IDs según tu
        // tileset)
        // Por ahora, vamos a buscar todos los tiles que no sean null y crear zonas
        // Puedes ajustar estos IDs después de identificar los tiles de flores
        final List<Integer> FLOWER_TILE_IDS = List.of(154);
        // Si no se especifican IDs, se crearán zonas en todos los tiles no vacíos
        // Para identificar los IDs correctos, puedes imprimir los IDs encontrados

        int flowerCount = 0;
        for (int y = 0; y < flowerLayer.getHeight(); y++) {
            for (int x = 0; x < flowerLayer.getWidth(); x++) {
                Cell cell = flowerLayer.getCell(x, y);

                if (cell != null && cell.getTile() != null) {
                    int tileId = cell.getTile().getId();

                    // Si no hay IDs específicos, crear zona en todos los tiles
                    // O verificar si el ID está en la lista
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

                        // Pokémon por defecto
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
            // Imprimir algunos IDs de tiles encontrados para ayudar a identificar los de
            // flores
            if (flowerLayer.getHeight() > 0 && flowerLayer.getWidth() > 0) {
                Cell sampleCell = flowerLayer.getCell(0, 0);
                if (sampleCell != null && sampleCell.getTile() != null) {
                    Gdx.app.log("ENCOUNTER", "Ejemplo de ID de tile encontrado: " + sampleCell.getTile().getId());
                }
            }
        }
    }

    /**
     * Crea zonas de encuentro por defecto si no hay ninguna en el mapa.
     */
    private void createDefaultEncounterZones() {
        // Crear una zona grande en el área central del mapa (ajustar según tu mapa)
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

    /**
     * Verifica si el jugador está en una zona de encuentro y puede iniciar una
     * batalla.
     */
    private void checkEncounterZones() {
        if (!canTriggerEncounter || currentState != GameState.FREE_ROAMING) {
            return;
        }

        final List<Integer> VALID_POKEMON_TILES = List.of(
                158, 159, 242, 153, 245, 156, 247, 244, 93, 4, 94, 5, 152, 240, 241, 157, 243, 246, 154, 155,
                5592, 5593, 5594, 5685, 5596, 5597, 5598, 5599, 5687, 5686, 5682, 5681, 5680);

        float playerX = characterActor.getX() + characterActor.getWidth() / 2;
        float playerY = characterActor.getY() + characterActor.getHeight() / 2;

        // --- LÓGICA MAPA 2 ---
        if (mapPath.equals("Mapa/Mapa2.tmx")) {
            for (MapLayer mapLayer : map.getLayers()) {
                if (mapLayer instanceof TiledMapTileLayer) {
                    TiledMapTileLayer layer = (TiledMapTileLayer) mapLayer;
                    int tileX = (int) (playerX / (layer.getTileWidth() * UNIT_SCALE));
                    int tileY = (int) (playerY / (layer.getTileHeight() * UNIT_SCALE));
                    Cell cell = layer.getCell(tileX, tileY);

                    if (cell != null && cell.getTile() != null) {
                        int tileId = cell.getTile().getId();
                        // Gdx.app.log("SPAWN_DEBUG", "Mapa 2 - Tile: " + tileId);

                        if (VALID_POKEMON_TILES_MAP2.contains(tileId)) {
                            if (random.nextFloat() < 0.02f) {
                                Gdx.app.log("SPAWN", "¡Encuentro activado en Mapa 2 (Tile " + tileId + ")!");
                                startBattle(null);
                                return; // Stop checking other layers if battle triggered
                            }
                        }
                    }
                }
            }
            return; // Don't run Map 1 logic
        }

        // --- LÓGICA MAPA 3 ---
        if (mapPath.equals("Mapa/MAPA3.tmx")) {
            int tileX = (int) (playerX / (map.getProperties().get("tilewidth", Integer.class) * UNIT_SCALE));
            int tileY = (int) (playerY / (map.getProperties().get("tileheight", Integer.class) * UNIT_SCALE));
            String coords = tileX + "," + tileY;

            if (VALID_SPAWN_COORDS_MAP3.contains(coords)) {
                if (random.nextFloat() < 0.05f) { // 5% chance per step on valid coordinates
                    Gdx.app.log("SPAWN", "¡Encuentro activado en Mapa 3 (Coords " + coords + ")!");
                    startBattle(null);
                }
            }
            return;
        }

        // --- LÓGICA MAPA 4 ---
        if (mapPath.equals("Mapa/MAPA 4.tmx")) {
            int tileX = (int) (playerX / (map.getProperties().get("tilewidth", Integer.class) * UNIT_SCALE));
            int tileY = (int) (playerY / (map.getProperties().get("tileheight", Integer.class) * UNIT_SCALE));
            String coords = tileX + "," + tileY;

            boolean zone1 = VALID_SPAWN_COORDS_MAP4_ZONE1.contains(coords);
            boolean zone2 = VALID_SPAWN_COORDS_MAP4_ZONE2.contains(coords);

            if (zone1 || zone2) {
                if (random.nextFloat() < 0.05f) { // 5% chance per step on valid coordinates
                    if (zone1) {
                        pendingBattleBackground = "Mapa/fondoBatalla.jpg";
                    } else {
                        pendingBattleBackground = "Mapa/fondoBatalla3.png";
                    }
                    Gdx.app.log("SPAWN",
                            "¡Encuentro activado en Mapa 4 (Coords " + coords + ")! BG: " + pendingBattleBackground);
                    startBattle(null);
                }
            }
            return;
        }

        // --- LÓGICA MAPA 1 (EXISTENTE) ---
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

        Cell cell = layer.getCell(tileX, tileY);

        if (cell != null && cell.getTile() != null) {
            int tileId = cell.getTile().getId();

            // Gdx.app.log("SPAWN_DEBUG", "Jugador en Tile ID: " + tileId + " en (" + tileX
            // + ", " + tileY + ")");

            if (VALID_POKEMON_TILES.contains(tileId)) {
                if (random.nextFloat() < 0.02f) {
                    Gdx.app.log("SPAWN", "¡Encuentro activado en Tile ID: " + tileId + "!");
                    startBattle(null);
                }
            }
        }
    }

    /**
     * Inicia una batalla con un Pokémon aleatorio de la zona.
     */
    private void startBattle(EncounterZone zone) {
        if (playerPokemon == null) {
            // No battle if no pokemon
            return;
        }

        List<String> possiblePokemon = getSpawnablePokemon();

        if (possiblePokemon.isEmpty()) {
            // Fallback to common if something goes wrong
            possiblePokemon.add("Rowlet");
        }

        // Seleccionar un Pokémon aleatorio de la lista ponderada
        String enemyPokemonName = possiblePokemon.get(random.nextInt(possiblePokemon.size()));
        Pokemon enemyPokemon = Pokemones.getPokemon(enemyPokemonName);

        if (enemyPokemon == null) {
            Gdx.app.error("BATTLE", "No se pudo crear el Pokémon: " + enemyPokemonName);
            return;
        }

        // Los Pokémon salvajes aparecen en nivel aleatorio entre 1 y 3 (para batallas
        // más justas)
        int randomLevel = 1 + random.nextInt(3); // 1 to 3
        enemyPokemon.setNivel(randomLevel);
        enemyPokemon.actualizarAtributos();
        enemyPokemon.setActualHP(enemyPokemon.getMaxHp());

        // Asegurar que el Pokémon del jugador esté en buen estado
        if (playerPokemon.getActualHP() <= 0) {
            playerPokemon.setActualHP(playerPokemon.getMaxHp());
        }

        // Schedule dialog + pending enemy; actual battle will start after the player
        // closes the dialog
        String msg = "¡Un " + enemyPokemon.getEspecie() + " salvaje ha aparecido!";

        // Store pending enemy until dialog is confirmed
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

        // Determine which map we're on
        if (mapPath.equals("Mapa/mapa11.tmx")) {
            // MAPA 1 (PRINCIPAL)
            // Común: 100 copias
            addCopies(pool, "Rowlet", 100);

            // Poco común: 30 copias
            addCopies(pool, "Ivysaur", 30);
            addCopies(pool, "Pikachu", 30);

            // Raro: 5 copias
            addCopies(pool, "Serperior", 5);

        } else if (mapPath.equals("Mapa/Mapa2.tmx")) {
            // MAPA 2
            // Común: 100 copias
            addCopies(pool, "Oshawott", 100);

            // Poco común: 30 copias
            addCopies(pool, "Vaporeon", 30);
            addCopies(pool, "Sylveon", 30);

            // Muy poco común: 15 copias
            addCopies(pool, "Jolteon", 15);

            // Raro: 5 copias
            addCopies(pool, "Blastoise", 5);

        } else if (mapPath.equals("Mapa/MAPA3.tmx")) {
            // MAPA 3
            // Flareon, Ivysaur, Vaporeon, Jolteon
            addCopies(pool, "Flareon", 25);
            addCopies(pool, "Ivysaur", 25);
            addCopies(pool, "Vaporeon", 25);
            addCopies(pool, "Jolteon", 25);

        } else if (mapPath.equals("Mapa/MAPA 4.tmx")) {
            // MAPA 4 - Cyndaquil, Flareon, Charizard, Gyarados
            // Cyndaquil es común (50)
            addCopies(pool, "Cyndaquil", 50);
            // Flareon es poco comùn (30)
            addCopies(pool, "Flareon", 30);
            // Charizard es raro (10)
            addCopies(pool, "Charizard", 10);
            // Gyarados es muy raro (5)
            addCopies(pool, "Gyarados", 5);

        } else {
            // Default/fallback para otros mapas (Pokemon Center, etc.)
            // Solo usar starters comunes
            addCopies(pool, "Rowlet", 50);
            addCopies(pool, "Oshawott", 50);
        }

        return pool;
    }

    private void addCopies(List<String> pool, String name, int count) {
        for (int i = 0; i < count; i++)
            pool.add(name);
    }

    /** Ejecuta la transición a pantalla de batalla usando el enemigo pendiente. */
    private void proceedWithPendingBattle() {
        if (this.pendingEnemyForBattle == null)
            return;

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

        // Determine background based on map
        String backgroundPath;
        // Map 1 uses fondoBatalla.jpg, Map 2 uses fondoBatalla2.png.png, Map 3 varies
        if (pendingBattleBackground != null) {
            backgroundPath = pendingBattleBackground;
            // Reset for next time
            pendingBattleBackground = null;
        } else if (mapPath.equals("Mapa/Mapa2.tmx")) {
            backgroundPath = "Mapa/fondoBatalla2.png.png";
        } else if (mapPath.equals("Mapa/MAPA3.tmx")) {
            float playerTileY = characterActor.getY() / (16 * UNIT_SCALE);
            if (playerTileY > 25) {
                backgroundPath = "Mapa/fondoBatalla5.png";
            } else {
                backgroundPath = "Mapa/fondoBatalla.jpg";
            }
        } else if (mapPath.equals("Mapa/MAPA 4.tmx")) {
            // Fallback/Default for Map 4 if pendingBattleBackground wasn't set (though it
            // should be)
            backgroundPath = "Mapa/fondoBatalla.jpg";
        } else {
            // Default for Map 1 and other maps
            backgroundPath = "Mapa/fondoBatalla.jpg";
        }

        Gdx.app.log("BATTLE", "Iniciando batalla: " + playerPokemon.getEspecie() + " vs " + enemyPokemon.getEspecie()
                + " (Background: " + backgroundPath + ")");
        try {
            game.setScreen(new ScreenBatalla(game, this, playerPokemon, enemyPokemon, playerInventory, false, false,
                    this.mapPath, backgroundPath, false));
        } catch (Exception e) {
            e.printStackTrace();
            currentState = GameState.FREE_ROAMING;
            canTriggerEncounter = true;
        }
    }

    /** Inicia una batalla con un NPC del Mapa 2 */
    private void startNPCBattle(String pokemonName, String npcName) {
        Pokemon enemyPokemon = Pokemones.getPokemon(pokemonName);

        // Set appropriate level for NPC Pokemon
        if (pokemonName.equals("Jolteon")) {
            enemyPokemon.setNivel(15);
        } else if (pokemonName.equals("Blastoise")) {
            enemyPokemon.setNivel(20);
        } else if (pokemonName.equals("Mewtwo")) {
            enemyPokemon.setNivel(25);
        } else if (pokemonName.equals("Gyarados")) { // Pennywise
            enemyPokemon.setNivel(18);
        } else if (pokemonName.equals("Sylveon")) { // Don Valerio
            enemyPokemon.setNivel(22);
        } else if (pokemonName.equals("Lucario")) { // Jesucristo
            enemyPokemon.setNivel(35); // Adjust level as needed
        } else if (pokemonName.equals("Mewtwo")) { // Afton (override previous Mewtwo check if needed or rely on level
                                                   // set here)
            // Note: There was a previous check for Mewtwo at level 25.
            // If we want Afton's to be stronger, we should check npcName or just letting
            // the last one win?
            // Since this is an else-if chain, we should be careful.
            // Actually, let's refine logic:
            if (npcName.equals("Afton")) {
                enemyPokemon.setNivel(40);
            } else {
                enemyPokemon.setNivel(25); // Default/Old Mewtwo encounter
            }
        }

        enemyPokemon.actualizarAtributos();
        enemyPokemon.setActualHP(enemyPokemon.getMaxHp());

        // Store NPC name for reward handling
        currentBattleNPC = npcName;

        // Limpiar teclas de movimiento antes de la batalla
        clearMovementKeys();

        // Determine background based on map
        String backgroundPath;
        // Map 1 uses fondoBatalla.jpg, Map 2 uses fondoBatalla2.png.png, Map 3 varies
        if (mapPath.equals("Mapa/Mapa2.tmx")) {
            backgroundPath = "Mapa/fondoBatalla2.png.png";
        } else if (mapPath.equals("Mapa/MAPA3.tmx") || mapPath.equals("Mapa/MAPA 4.tmx")) {
            float playerTileY = characterActor.getY() / (16 * UNIT_SCALE);
            if (playerTileY > 25) {
                backgroundPath = "Mapa/fondoBatalla5.png";
            } else {
                backgroundPath = "Mapa/fondoBatalla.jpg";
            }
        } else {
            // Default for Map 1 and other maps
            backgroundPath = "Mapa/fondoBatalla.jpg";
        }

        // Transición a la pantalla de batalla
        currentState = GameState.BATTLE;
        game.setScreen(new ScreenBatalla(game, this, playerPokemon, enemyPokemon, playerInventory, false, false,
                mapPath, backgroundPath, true));
    }

    // Track current battle NPC for reward handling
    private String currentBattleNPC = null;

    /** Called by ScreenBatalla when player wins an NPC battle */
    public void onNPCBattleVictory(String npcName) {
        // Mark NPC as defeated
        if (npcName.equals("Giorno")) {
            giornoDefeated = true;
            // Reward: 3 Pokeballs
            playerInventory.addObjeto(101, 3);
            dialogueQueue = new LinkedList<>();
            dialogueQueue.add("¡Has derrotado a Giorno!");
            dialogueQueue.add("Has recibido 3 Pokéballs como recompensa.");
        } else if (npcName.equals("Jotaro")) {
            jotaroDefeated = true;
            // Reward: 5 Pokeballs
            playerInventory.addObjeto(101, 5);
            dialogueQueue = new LinkedList<>();
            dialogueQueue.add("¡Has derrotado a Jotaro!");
            dialogueQueue.add("Has recibido 5 Pokéballs como recompensa.");
        } else if (npcName.equals("Kaneki")) {
            kanekiDefeated = true;
            // Reward: 10 Pokeballs
            playerInventory.addObjeto(101, 10);
            dialogueQueue = new LinkedList<>();
            dialogueQueue.add("¡Has derrotado a Kaneki!");
            dialogueQueue.add("Has recibido 10 Pokéballs como recompensa.");
        } else if (npcName.equals("Pennywise")) {
            pennywiseDefeated = true;
            dialogueQueue = new LinkedList<>();
            dialogueQueue.add("¡Has derrotado a Pennywise!");
            dialogueQueue.add("La pesadilla ha terminado... por ahora.");
        } else if (npcName.equals("Don Valerio")) {
            donValerioDefeated = true;
            dialogueQueue = new LinkedList<>();
            dialogueQueue.add("¡Has derrotado a Don Valerio!");
            dialogueQueue.add("Has demostrado tu valía, joven.");
        } else if (npcName.equals("Jesucristo")) {
            jesucristoDefeated = true;
            dialogueQueue = new LinkedList<>();
            dialogueQueue.add("Tu fe es fuerte.");
            dialogueQueue.add("Has derrotado a Jesucristo.");
        } else if (npcName.equals("Afton")) {
            aftonDefeated = true;
            dialogueQueue = new LinkedList<>();
            dialogueQueue.add("¡NO! ¡Esto es imposible!");
            dialogueQueue.add("Has derrotado a Afton.");
        }

        currentState = GameState.DIALOGUE;
        processNextDialogueLine();
        currentBattleNPC = null;
    }

    private void startArceusBattle() {
        canTriggerEncounter = false;
        encounterCooldown = 0f;
        currentState = GameState.BATTLE;
        clearMovementKeys();
        Gdx.input.setInputProcessor(null);

        Pokemon arceus = sodyl.proyecto.clases.Pokemones.getPokemon("Arceus");
        // Asegurar nivel 100 o dificultad máxima si se desea, por ahora stats base
        // definidos en Pokemones.java
        // arceus.setNivel(100);
        // arceus.actualizarAtributos();

        if (playerPokemon == null) {
            dialogueQueue.add("¡No tienes Pokémon para luchar!");
            currentState = GameState.DIALOGUE;
            processNextDialogueLine();
            return;
        }

        Gdx.app.log("BATTLE", "Iniciando BATALLA FINAL CONTRA ARCEUS");
        try {
            // Pasamos true para indicar que es la batalla final (isTutorial=false,
            // isFinalBattle=true) y el mapPath
            game.setScreen(
                    new ScreenBatalla(game, this, playerPokemon, arceus, playerInventory, false, true, this.mapPath));
        } catch (Exception e) {
            e.printStackTrace();
            currentState = GameState.FREE_ROAMING;
        }
    }

    /**
     * Genera objetos coleccionables aleatoriamente SÓLO sobre tiles con el ID 2
     * (Hierba).
     */
    private void spawnCollectiblesOnGrassTiles(int totalItemsToSpawn) {
        // Tiles Nivel 1: 417, 505, 5193, 5192, 5194, 1617, 1616, 1618, 5281, 5282, 418
        // Tiles TIERRA: 1

        final List<Integer> VALID_IDS_NIVEL1 = List.of(417, 505, 5193, 5192, 5194, 1617, 1616, 1618, 5281, 5282, 418);
        final int VALID_ID_TIERRA = 1;

        final int TILE_WIDTH = map.getProperties().get("tilewidth", Integer.class);
        final int TILE_HEIGHT = map.getProperties().get("tileheight", Integer.class);

        List<Float[]> validSpawnPositions = new ArrayList<>();

        // --- LOGICA MAPA 2 ---
        if (mapPath.equals("Mapa/Mapa2.tmx")) {
            for (MapLayer mapLayer : map.getLayers()) {
                if (mapLayer instanceof TiledMapTileLayer) {
                    TiledMapTileLayer layer = (TiledMapTileLayer) mapLayer;
                    for (int y = 0; y < layer.getHeight(); y++) {
                        for (int x = 0; x < layer.getWidth(); x++) {
                            Cell cell = layer.getCell(x, y);
                            if (cell != null && cell.getTile() != null) {
                                if (VALID_COLLECTIBLE_TILES_MAP2.contains(cell.getTile().getId())) {
                                    float worldX = x * TILE_WIDTH * UNIT_SCALE;
                                    float worldY = y * TILE_HEIGHT * UNIT_SCALE;
                                    validSpawnPositions.add(new Float[] { worldX, worldY });
                                }
                            }
                        }
                    }
                }
            }
        } else if (mapPath.equals("Mapa/MAPA3.tmx")) {
            // --- LOGICA MAPA 3 (Coordinate-based spawning) ---
            for (String coord : VALID_COLLECTIBLE_COORDS_MAP3) {
                String[] parts = coord.split(",");
                int tileX = Integer.parseInt(parts[0]);
                int tileY = Integer.parseInt(parts[1]);

                float worldX = tileX * TILE_WIDTH * UNIT_SCALE;
                float worldY = tileY * TILE_HEIGHT * UNIT_SCALE;
                validSpawnPositions.add(new Float[] { worldX, worldY });
            }
        } else if (mapPath.equals("Mapa/MAPA 4.tmx")) {
            // --- LOGICA MAPA 4 (Coordinate-based spawning) ---
            for (String coord : VALID_COLLECTIBLE_COORDS_MAP4) {
                String[] parts = coord.split(",");
                int tileX = Integer.parseInt(parts[0]);
                int tileY = Integer.parseInt(parts[1]);

                float worldX = tileX * TILE_WIDTH * UNIT_SCALE;
                float worldY = tileY * TILE_HEIGHT * UNIT_SCALE;
                validSpawnPositions.add(new Float[] { worldX, worldY });
            }
        } else {
            // --- LOGICA EXISTENTE (MAPA 1) ---
            // Check Layer "NIvel 1"
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
                Gdx.app.log("COLECTABLES",
                        "  → Tile (" + tileX + ", " + tileY + "): " + materialName + " x" + quantity);

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
            Gdx.app.log("COLECTABLES",
                    "  → Tile (" + tileX + ", " + tileY + "): " + selectedItemName + " x" + quantity);

            Collectible collectible = new Collectible(
                    adjustedX,
                    adjustedY,
                    item.getId(),
                    quantity,
                    item.getTexturePath());
            collectibles.add(collectible);
            stage.addActor(collectible.getActor());
        }

        Gdx.app.log("COLECTABLES",
                "Se generaron " + collectibles.size + " objetos aleatorios en total.");
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
            showChoiceBox();
            currentState = GameState.WAITING_FOR_CHOICE;
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

        // Crafting button removed from direct access, now via Inventory Submenu

        // Remove craftingButton from array if we want clean index logic,
        // or just ignore it in execution. Let's start fresh with array.
        pauseMenuButtons = new TextButton[] { inventoryButton, pokedexButton, saveButton, mainMenuButton, backButton };

        updatePauseMenuSelection(0);
    }

    // ... (initializeInventoryMenu, showInventoryMenu, updateInventoryDisplay) ...

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

        inventoryMenuTable.add(itemContainer).colspan(4).expand().fill().row();

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

        itemContainer.add(new Label("ICONO", nameStyle)).width(70).padRight(10);
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

    // ... (initializeCraftingMenu, showCraftingMenu, updateCraftingMenu, craftItem)
    // ...

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
                selectedIndex = 0;
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
        if (pauseMenuButtons.length == 0)
            return;
        pauseMenuButtons[selectedIndex].getLabel().setColor(Color.WHITE);
        selectedIndex = (newIndex + pauseMenuButtons.length) % pauseMenuButtons.length;
        pauseMenuButtons[selectedIndex].getLabel().setColor(Color.YELLOW);
    }

    private void executePauseMenuAction(int index) {
        if (currentState != GameState.PAUSED)
            return;

        // Indices changed due to removal of Crafting button
        // 0: Inventory (Submenu)
        // 1: Pokedex
        // 2: Save (Placeholder)
        // 3: Main Menu
        // 4: Back

        if (index == 0)
            showInventorySubmenu(); // Show unified menu
        else if (index == 1)
            showPokedexMenu();
        else if (index == 2) {
            // Guardar Partida
            sodyl.proyecto.clases.Pokedex.save();
            if (playerInventory != null) {
                playerInventory.save(sodyl.proyecto.clases.UserManager.getCurrentUser());
            }

            togglePauseMenu(); // Cerrar pausa

            // Mostrar mensaje
            if (dialogueQueue == null)
                dialogueQueue = new LinkedList<>();
            dialogueQueue.clear();
            dialogueQueue.add("¡Partida guardada correctamente!");
            currentState = GameState.DIALOGUE;
            processNextDialogueLine();
        } else if (index == 3)
            startTransitionToMainMenu();
        else if (index == 4)
            togglePauseMenu();
    }

    // --- NUEVO SUBMENÚ DE INVENTARIO ---

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

        // Highlighting handled in updateInventorySubmenu

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
        // Simple manual highlighting since we didn't recreate buttons with Listeners
        // logic for color change
        // We will just use the index to determine action
        Table table = inventorySubmenuTable;
        // Children: Label (0), Btn1 (1), Btn2 (2), Btn3 (3)

        // Reset colors
        ((TextButton) table.getChildren().get(1)).getLabel().setColor(Color.WHITE);
        ((TextButton) table.getChildren().get(2)).getLabel().setColor(Color.WHITE);
        ((TextButton) table.getChildren().get(3)).getLabel().setColor(Color.WHITE);

        // Highlight selected
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
            // Volver
            currentState = GameState.PAUSED;
            inventorySubmenuTable.setVisible(false);
            pauseMenuTable.setVisible(true);
        }
    }

    // --- NUEVO MENÚ DE ACCIÓN POKEDEX (CURAR) ---

    private void initializePokedexActionMenu() {
        Texture darkBackground = createColoredTexture(new Color(0.1f, 0.1f, 0.1f, 0.95f));
        TextureRegionDrawable backgroundDrawable = new TextureRegionDrawable(darkBackground);

        pokedexActionMenuTable = new Table();
        // Small menu centered
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
        Label.LabelStyle valueStyle = new Label.LabelStyle(font, new Color(0.7f, 1f, 0.7f, 1f)); // Light green
        Label.LabelStyle infoStyle = new Label.LabelStyle(font, new Color(0.9f, 0.9f, 0.6f, 1f)); // Light yellow

        // Title
        pokemonInfoTable.add(new Label("--- INFORMACIÓN DE " + pokemon.getEspecie().toUpperCase() + " ---", titleStyle))
                .colspan(2).padBottom(30).row();

        // Pokemon Type
        pokemonInfoTable.add(new Label("Tipo:", labelStyle)).left().padRight(20);
        pokemonInfoTable.add(new Label(pokemon.getTipo().toString(), valueStyle)).left().row();
        pokemonInfoTable.add().height(10).row();

        // Research Level
        pokemonInfoTable.add(new Label("Nivel de Investigación:", labelStyle)).left().padRight(20);
        pokemonInfoTable.add(new Label(pokemon.getNivel() + "/10", valueStyle)).left().row();
        pokemonInfoTable.add().height(10).row();

        // HP Information
        pokemonInfoTable.add(new Label("HP:", labelStyle)).left().padRight(20);
        String hpText = pokemon.getActualHP() + "/" + pokemon.getMaxHp();
        Color hpColor = pokemon.getActualHP() < pokemon.getMaxHp() * 0.3f ? Color.RED
                : pokemon.getActualHP() < pokemon.getMaxHp() * 0.6f ? Color.ORANGE : Color.GREEN;
        pokemonInfoTable.add(new Label(hpText, new Label.LabelStyle(font, hpColor))).left().row();
        pokemonInfoTable.add().height(20).row();

        // Attacks Section
        pokemonInfoTable.add(new Label("=== ATAQUES ===", titleStyle)).colspan(2).padBottom(15).row();

        // Attack 1
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

        // Attack 2
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

        // Instructions
        pokemonInfoTable.add().height(30).row();
        pokemonInfoTable.add(new Label("Presiona ESC para volver", new Label.LabelStyle(font, Color.GRAY)))
                .colspan(2).row();
    }

    private void showPokedexActionMenu(Pokemon pokemon) {
        selectedPokemonForHealing = pokemon;
        currentState = GameState.POKEDEX_ACTION_MENU;
        selectedPokedexActionIndex = 0;
        pokedexActionMenuTable.setVisible(true);
        // Ensure it's on top
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
            // Cancelar
            currentState = GameState.POKEDEX;
            pokedexActionMenuTable.setVisible(false);
            return;
        }

        if (index == 1) {
            // Ver información
            showPokemonInfo(selectedPokemonForHealing);
            return;
        }

        // Curar logic
        // Check for Potion. Assuming Item ID 102 is Potion based on usual conventions
        // or "Pocion" Name
        // We will look for an object named "Pocion" or "Poción"

        int potionId = -1;
        // Search for item with Potion in name if ID 102 isn't it
        Objeto potionObj = Objeto.getObjetoByName("Pocion");
        if (potionObj == null)
            potionObj = Objeto.getObjetoByName("Poción");

        // Fallback checks
        if (potionObj != null) {
            potionId = potionObj.getId();
        } else {
            // Maybe use a default if not found? Or assume type MEDICINA?
            // Let's iterate all held objects and find a MEDICINA
            for (Integer id : playerInventory.getAllObjetos().keySet()) {
                Objeto o = Objeto.getObjeto(id);
                if (o != null && o.getTipo() == Objeto.Type.MEDICINA) {
                    potionId = id;
                    break;
                }
            }
        }

        if (potionId != -1 && playerInventory.getQuantity(potionId) > 0) {
            // Heal
            selectedPokemonForHealing.setActualHP(selectedPokemonForHealing.getMaxHp());
            playerInventory.removeObjeto(potionId, 1);

            // Show message
            dialogueQueue.add("¡" + selectedPokemonForHealing.getEspecie() + " ha sido curado!");
            dialogueQueue.add("Usaste 1 " + Objeto.getObjeto(potionId).getNombre() + ".");

            // Go to Dialogue to show message, then return to Pokedex?
            // Better: Just show popup or use dialogue system.
            // Using dialogue system will exit Pokedex context usually.
            // Let's close Pokedex menu and go to dialogue for better feedback.
            currentState = GameState.DIALOGUE;
            pokedexActionMenuTable.setVisible(false);
            pokedexMenuTable.setVisible(false);

            // Set callback to return to Pokedex after dialogue?
            // Maybe just return to roam/pause. Let's return to Pokedex.
            final GameState returnState = GameState.POKEDEX;
            onDialogCompleteAction = () -> {
                currentState = returnState;
                pokedexMenuTable.setVisible(true);
            };

            processNextDialogueLine();

        } else {
            // No potions
            currentState = GameState.DIALOGUE;
            pokedexActionMenuTable.setVisible(false);
            pokedexMenuTable.setVisible(false); // Hide underlying menu for clarity

            dialogueQueue.add("No tienes pociones para curar a este Pokémon.");

            onDialogCompleteAction = () -> {
                currentState = GameState.POKEDEX;
                pokedexMenuTable.setVisible(true);
                // Don't reopen action menu
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

    private void startTransitionToPokemonCenter() {
        currentState = GameState.TRANSITIONING;
        transitionTimer = 0f;
        nextScreen = new ScreenCarga(game,
                () -> new ScreenMapaTiled(game, "Mapa/Centro Pokemon interior.tmx", playerInventory, playerPokemon,
                        null, GameState.FREE_ROAMING, null, null, "Mapa/mapa11.tmx"));
        Gdx.input.setInputProcessor(null);
    }

    private void startTransitionBackToMap1() {
        currentState = GameState.TRANSITIONING;
        transitionTimer = 0f;

        // Check which map to return to
        if (sourceMapBeforePokemonCenter != null && sourceMapBeforePokemonCenter.equals("Mapa/Mapa2.tmx")) {
            // Return to Map 2 at the Pokemon Center entrance (tile 45, 27 - one tile below
            // the entrance)
            float returnX = 45f * 16f * UNIT_SCALE;
            float returnY = 27f * 16f * UNIT_SCALE;
            nextScreen = new ScreenCarga(game,
                    () -> new ScreenMapaTiled(game, "Mapa/Mapa2.tmx", playerInventory, playerPokemon,
                            null, GameState.FREE_ROAMING, returnX, returnY));
        } else if (sourceMapBeforePokemonCenter != null && sourceMapBeforePokemonCenter.equals("Mapa/MAPA3.tmx")) {
            // Return to Map 3 (Tile 25, 33 - one tile below entrance)
            float returnX = 25f * 16f * UNIT_SCALE;
            float returnY = 33f * 16f * UNIT_SCALE;
            nextScreen = new ScreenCarga(game,
                    () -> new ScreenMapaTiled(game, "Mapa/MAPA3.tmx", playerInventory, playerPokemon,
                            null, GameState.FREE_ROAMING, returnX, returnY));
        } else {
            // Return to Map 1 at the Pokemon Center entrance (tile 38, 36)
            float returnX = 38f * 16f * UNIT_SCALE;
            float returnY = 36f * 16f * UNIT_SCALE;
            nextScreen = new ScreenCarga(game,
                    () -> new ScreenMapaTiled(game, "Mapa/mapa11.tmx", playerInventory, playerPokemon,
                            null, GameState.FREE_ROAMING, returnX, returnY));
        }

        Gdx.input.setInputProcessor(null);
    }

    private void startTransitionToMap2() {
        currentState = GameState.TRANSITIONING;
        transitionTimer = 0f;

        // Spawn solicitado: Tile 48, 44 (Tiled) -> Y=26 (LibGDX approx)
        float spawnX = 48f * 16f * UNIT_SCALE;
        float spawnY = 26f * 16f * UNIT_SCALE;

        nextScreen = new ScreenCarga(game,
                () -> new ScreenMapaTiled(game, "Mapa/Mapa2.tmx", playerInventory, playerPokemon,
                        null, GameState.FREE_ROAMING, spawnX, spawnY));
        Gdx.input.setInputProcessor(null);
    }

    private void startTransitionBackToMap1FromMap2() {
        currentState = GameState.TRANSITIONING;
        transitionTimer = 0f;
        // Volver a Mapa 1, un tile a la derecha de donde entró (Entrada X=0 -> Salida
        // X=1)
        // Centrado en el path Y=19 (entre 17 y 21)
        float returnX = 1f * 16f * UNIT_SCALE;
        float returnY = 19f * 16f * UNIT_SCALE;

        nextScreen = new ScreenCarga(game,
                () -> new ScreenMapaTiled(game, "Mapa/mapa11.tmx", playerInventory, playerPokemon,
                        null, GameState.FREE_ROAMING, returnX, returnY));
        Gdx.input.setInputProcessor(null);
    }

    private void startTransitionToPokemonCenterFromMap2() {
        currentState = GameState.TRANSITIONING;
        transitionTimer = 0f;
        nextScreen = new ScreenCarga(game,
                () -> new ScreenMapaTiled(game, "Mapa/Centro Pokemon interior.tmx", playerInventory, playerPokemon,
                        null, GameState.FREE_ROAMING, null, null, "Mapa/Mapa2.tmx"));
        Gdx.input.setInputProcessor(null);
    }

    private void startTransitionToMap3() {
        currentState = GameState.TRANSITIONING;
        transitionTimer = 0f;

        // Spawn en Mapa 3: Borde Izquierdo (X=1), Y=23 (Centrado relativo a la salida)
        float spawnX = 1f * 16f * UNIT_SCALE;
        float spawnY = 23f * 16f * UNIT_SCALE;

        nextScreen = new ScreenCarga(game,
                () -> new ScreenMapaTiled(game, "Mapa/MAPA3.tmx", playerInventory, playerPokemon,
                        null, GameState.FREE_ROAMING, spawnX, spawnY));
        Gdx.input.setInputProcessor(null);
    }

    private void startTransitionToPokemonCenterFromMap3() {
        currentState = GameState.TRANSITIONING;
        transitionTimer = 0f;
        nextScreen = new ScreenCarga(game,
                () -> new ScreenMapaTiled(game, "Mapa/Centro Pokemon interior.tmx", playerInventory, playerPokemon,
                        null, GameState.FREE_ROAMING, null, null, "Mapa/MAPA3.tmx"));
        Gdx.input.setInputProcessor(null);
    }

    private void startTransitionBackToMap1FromMap3() {
        currentState = GameState.TRANSITIONING;
        transitionTimer = 0f;
        // Return to Map 1 at right edge (X=48), Y=23 (centered relative to Map 3 exit)
        float returnX = 48f * 16f * UNIT_SCALE;
        float returnY = 23f * 16f * UNIT_SCALE;

        nextScreen = new ScreenCarga(game,
                () -> new ScreenMapaTiled(game, "Mapa/mapa11.tmx", playerInventory, playerPokemon,
                        null, GameState.FREE_ROAMING, returnX, returnY));
        Gdx.input.setInputProcessor(null);
    }

    private void startTransitionToMap4() {
        currentState = GameState.TRANSITIONING;
        transitionTimer = 0f;

        // Aparecer en Mapa 4, Borde Inferior
        float spawnX = 24.5f * 16f * UNIT_SCALE;
        float spawnY = 1.5f * 16f * UNIT_SCALE;

        nextScreen = new ScreenCarga(game,
                () -> new ScreenMapaTiled(game, "Mapa/MAPA 4.tmx", playerInventory, playerPokemon,
                        null, GameState.FREE_ROAMING, spawnX, spawnY));
        Gdx.input.setInputProcessor(null);
    }

    private void startTransitionBackToMap1FromMap4() {
        currentState = GameState.TRANSITIONING;
        transitionTimer = 0f;

        // Regresamos a Mapa 1 cerca de donde entramos (Borde Superior)
        float spawnX = 24.5f * 16f * UNIT_SCALE;
        float spawnY = 47.5f * 16f * UNIT_SCALE;

        nextScreen = new ScreenCarga(game,
                () -> new ScreenMapaTiled(game, "Mapa/mapa11.tmx", playerInventory, playerPokemon,
                        null, GameState.FREE_ROAMING, spawnX, spawnY));
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
        // Safety check: If we are rendering but state is BATTLE, force reset (should
        // have been handled in show)
        if (currentState == GameState.BATTLE) {
            currentState = GameState.FREE_ROAMING;
        }

        ScreenUtils.clear(0, 0, 0, 1);

        if (currentState == GameState.FREE_ROAMING) {
            handleMovement(delta);

            // Actualizar cooldown de encuentros
            if (!canTriggerEncounter) {
                encounterCooldown += delta;
                if (encounterCooldown >= ENCOUNTER_COOLDOWN_TIME) {
                    canTriggerEncounter = true;
                    encounterCooldown = 0f;
                    Gdx.app.log("ENCOUNTER",
                            "Cooldown de encuentros completado. Los encuentros están activos nuevamente.");
                }
            } else {
                // Verificar zonas de encuentro solo si podemos tener encuentros
                // Y solo si el estado NO es BATTLE (por si acaso)
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

        // Update camera position with clamping to prevent seeing outside the map
        clampCamera();
        camera.update();

        if (renderer != null && map != null) {
            try {
                // Dynamically find tile layers to render (excluding visual collision layers)
                com.badlogic.gdx.utils.IntArray layersToRender = new com.badlogic.gdx.utils.IntArray();
                for (int i = 0; i < map.getLayers().getCount(); i++) {
                    MapLayer layer = map.getLayers().get(i);
                    // Only render tile layers, but skip the visual "Colisiones" layer
                    if (layer instanceof com.badlogic.gdx.maps.tiled.TiledMapTileLayer) {
                        String layerName = layer.getName();
                        if (layerName != null && layerName.equals("Colisiones")) {
                            continue;
                        }
                        layersToRender.add(i);
                    }
                }
                renderer.setView(camera);
                renderer.render(layersToRender.toArray());

                // Renderizar marcador de Arceus DESPUES del mapa para que sea visible
                renderArceusMarker(delta);
            } catch (Exception e) {
                Gdx.app.error("RENDER", "Error al renderizar el mapa: " + e.getMessage(), e);
                // Si hay error, intentar reinicializar el renderer
                try {
                    if (map != null) {
                        renderer = new OrthogonalTiledMapRenderer(map, UNIT_SCALE);
                    }
                } catch (Exception e2) {
                    Gdx.app.error("RENDER", "Error al reinicializar renderer: " + e2.getMessage(), e2);
                }
            }

            // Bonnie Animation (Map 3)
            if (mapPath.equals("Mapa/MAPA 3.tmx")) {
                if (bonnieActor != null && bonnieTexture1 != null && bonnieTexture2 != null) {
                    bonnieAnimTimer += delta;
                    if (bonnieAnimTimer >= 0.5f) {
                        bonnieFrame1 = !bonnieFrame1;
                        bonnieActor
                                .setDrawable(new TextureRegionDrawable(bonnieFrame1 ? bonnieTexture1 : bonnieTexture2));
                        bonnieAnimTimer = 0;
                    }
                }
            }
        } else

        {
            Gdx.app.error("RENDER",
                    "Renderer o mapa es null. Renderer: " + (renderer != null) + ", Map: " + (map != null));
        }

        stage.act(delta);

        // Animate Map 2 NPCs
        if (mapPath.equals("Mapa/Mapa2.tmx")) {
            // Giorno animation
            if (giornoActor != null && giornoTexture1 != null && giornoTexture2 != null) {
                giornoAnimTimer += delta;
                if (giornoAnimTimer >= 0.5f) {
                    giornoFrame1 = !giornoFrame1;
                    giornoActor.setDrawable(new TextureRegionDrawable(giornoFrame1 ? giornoTexture1 : giornoTexture2));
                    giornoAnimTimer = 0;
                }
            }

            // Jotaro animation
            if (jotaroActor != null && jotaroTexture1 != null && jotaroTexture2 != null) {
                jotaroAnimTimer += delta;
                if (jotaroAnimTimer >= 0.5f) {
                    jotaroFrame1 = !jotaroFrame1;
                    jotaroActor.setDrawable(new TextureRegionDrawable(jotaroFrame1 ? jotaroTexture1 : jotaroTexture2));
                    jotaroAnimTimer = 0;
                }
            }

            // Kaneki animation
            if (kanekiActor != null && kanekiTexture1 != null && kanekiTexture2 != null) {
                kanekiAnimTimer += delta;
                if (kanekiAnimTimer >= 0.5f) {
                    kanekiFrame1 = !kanekiFrame1;
                    kanekiActor.setDrawable(new TextureRegionDrawable(kanekiFrame1 ? kanekiTexture1 : kanekiTexture2));
                    kanekiAnimTimer = 0;
                }
            }

            // Freddy Map 2 animation
            if (freddyMap2Actor != null && freddyMap2Texture1 != null && freddyMap2Texture2 != null) {
                freddyMap2AnimTimer += delta;
                if (freddyMap2AnimTimer >= 0.5f) {
                    freddyMap2Frame1 = !freddyMap2Frame1;
                    freddyMap2Actor.setDrawable(
                            new TextureRegionDrawable(freddyMap2Frame1 ? freddyMap2Texture1 : freddyMap2Texture2));
                    freddyMap2AnimTimer = 0;
                }
            }
        }

        // Animate Map 3 NPCs
        if (mapPath.equals("Mapa/MAPA3.tmx")) {
            // Pennywise animation
            if (pennywiseActor != null && pennywiseTexture1 != null && pennywiseTexture2 != null) {
                pennywiseAnimTimer += delta;
                if (pennywiseAnimTimer >= 0.5f) {
                    pennywiseFrame1 = !pennywiseFrame1;
                    pennywiseActor.setDrawable(
                            new TextureRegionDrawable(pennywiseFrame1 ? pennywiseTexture1 : pennywiseTexture2));
                    pennywiseAnimTimer = 0;
                }
            }

            // Don Valerio animation
            if (donValerioActor != null && donValerioTexture1 != null && donValerioTexture2 != null) {
                donValerioAnimTimer += delta;
                if (donValerioAnimTimer >= 0.5f) {
                    donValerioFrame1 = !donValerioFrame1;
                    donValerioActor.setDrawable(
                            new TextureRegionDrawable(donValerioFrame1 ? donValerioTexture1 : donValerioTexture2));
                    donValerioAnimTimer = 0;
                }
            }
        }

        // Animate Map 4 NPCs
        if (mapPath.equals("Mapa/MAPA 4.tmx")) {
            // Jesucristo animation
            if (jesucristoActor != null && jesucristoTexture1 != null && jesucristoTexture2 != null) {
                jesucristoAnimTimer += delta;
                if (jesucristoAnimTimer >= 0.5f) {
                    jesucristoFrame1 = !jesucristoFrame1;
                    jesucristoActor.setDrawable(
                            new TextureRegionDrawable(jesucristoFrame1 ? jesucristoTexture1 : jesucristoTexture2));
                    jesucristoAnimTimer = 0;
                }
            }

            // Afton animation
            if (aftonActor != null && aftonTexture1 != null && aftonTexture2 != null) {
                aftonAnimTimer += delta;
                if (aftonAnimTimer >= 0.5f) {
                    aftonFrame1 = !aftonFrame1;
                    aftonActor.setDrawable(new TextureRegionDrawable(aftonFrame1 ? aftonTexture1 : aftonTexture2));
                    aftonAnimTimer = 0;
                }
            }

            // Map 4 Info NPC animation
            if (map4InfoNpcActor != null && map4InfoNpcTexture1 != null && map4InfoNpcTexture2 != null) {
                map4InfoNpcAnimTimer += delta;
                if (map4InfoNpcAnimTimer >= 0.5f) {
                    map4InfoNpcFrame1 = !map4InfoNpcFrame1;
                    map4InfoNpcActor.setDrawable(
                            new TextureRegionDrawable(map4InfoNpcFrame1 ? map4InfoNpcTexture1 : map4InfoNpcTexture2));
                    map4InfoNpcAnimTimer = 0;
                }
            }
        }

        stage.draw();

        batch.begin();

        batch.end();

        uiStage.act(delta);
        uiStage.draw();

        // Lógica de Transición (Fade In / Fade Out)
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

    // Initialize ChoiceBox (Yes/No)
    private void initializeChoiceBox() {
        Texture darkBackground = createColoredTexture(new Color(0.1f, 0.1f, 0.1f, 0.9f));
        TextureRegionDrawable backgroundDrawable = new TextureRegionDrawable(darkBackground);

        choiceBoxTable = new Table();
        choiceBoxTable.setBackground(backgroundDrawable);
        choiceBoxTable.pad(20);

        // Position: Bottom Right, slightly above dialogue or custom
        choiceBoxTable.setSize(150, 100);
        choiceBoxTable.setPosition(uiStage.getWidth() - 170, 200);

        choiceBoxTable.setVisible(false);
        uiStage.addActor(choiceBoxTable);
    }

    // Show Yes/No options
    private void showYesNoChoice() {
        currentState = GameState.WAITING_FOR_CHOICE;
        selectedChoiceIndex = 0;
        choiceBoxTable.setVisible(true);
        updateChoiceBox();
    }

    private void showChoiceBox() {
        choiceBoxTable.setVisible(true);
        selectedChoiceIndex = 0;
        updateChoiceBox();
    }

    private void updateChoiceBox() {
        choiceBoxTable.clearChildren();
        Label.LabelStyle selectedStyle = new Label.LabelStyle(font, Color.YELLOW);
        Label.LabelStyle normalStyle = new Label.LabelStyle(font, Color.WHITE);

        Label yesLabel = new Label("Sí", (selectedChoiceIndex == 0) ? selectedStyle : normalStyle);
        Label noLabel = new Label("No", (selectedChoiceIndex == 1) ? selectedStyle : normalStyle);

        choiceBoxTable.add(yesLabel).pad(5).row();
        choiceBoxTable.add(noLabel).pad(5).row();
    }

    private void handleChoiceInput(int keycode) {
        if (keycode == Keys.UP || keycode == Keys.DOWN) {
            selectedChoiceIndex = (selectedChoiceIndex == 0) ? 1 : 0;
            updateChoiceBox();
        } else if (keycode == Keys.ENTER || keycode == Keys.Z) {
            choiceBoxTable.setVisible(false);
            if (selectedChoiceIndex == 0) {
                // SI
                if (isArceusDialogue) {
                    isArceusDialogue = false;
                    choiceBoxTable.setVisible(false);
                    startArceusBattle();
                    return;
                }

                // SI - Curar todos los Pokemon del equipo
                if (playerPokemon != null) {
                    playerPokemon.restoreStatus();

                    // También curar todos los Pokemon del equipo
                    List<Pokemon> team = sodyl.proyecto.clases.Pokedex.getTeam();
                    for (Pokemon p : team) {
                        if (p != null) {
                            p.restoreStatus();
                        }
                    }

                    dialogueQueue.add("¡Tus Pokémon han sido restaurados exitosamente!");
                } else {
                    dialogueQueue.add("No tienes Pokémon para curar.");
                }
                currentState = GameState.DIALOGUE;
                processNextDialogueLine();
            } else {
                // NO
                if (isArceusDialogue) {
                    isArceusDialogue = false;
                }
                currentState = GameState.FREE_ROAMING;
            }

        }
    }

    private void updateCharacterAnimation() {
        // Update Funtime Foxy Animation (if present)
        if (funtimeFoxyActor != null && funtimeFoxyTexture1 != null && funtimeFoxyTexture2 != null) {
            funtimeFoxyAnimTimer += Gdx.graphics.getDeltaTime();
            if (funtimeFoxyAnimTimer > 0.5f) { // Switch every 0.5 seconds
                funtimeFoxyAnimTimer = 0;
                funtimeFoxyFrame1 = !funtimeFoxyFrame1;

                // We need to use setDrawable for Image actors
                if (funtimeFoxyFrame1) {
                    funtimeFoxyActor.setDrawable(new TextureRegionDrawable(new TextureRegion(funtimeFoxyTexture1)));
                } else {
                    funtimeFoxyActor.setDrawable(new TextureRegionDrawable(new TextureRegion(funtimeFoxyTexture2)));
                }
            }
        }

        // Update Circus Baby Animation (if present)
        if (circusBabyActor != null && circusBabyTexture1 != null && circusBabyTexture2 != null) {
            circusBabyAnimTimer += Gdx.graphics.getDeltaTime();
            if (circusBabyAnimTimer > 0.5f) { // Switch every 0.5 seconds
                circusBabyAnimTimer = 0;
                circusBabyFrame1 = !circusBabyFrame1;

                // We need to use setDrawable for Image actors
                if (circusBabyFrame1) {
                    circusBabyActor.setDrawable(new TextureRegionDrawable(new TextureRegion(circusBabyTexture1)));
                } else {
                    circusBabyActor.setDrawable(new TextureRegionDrawable(new TextureRegion(circusBabyTexture2)));
                }
            }
        }

        // Update Funtime Freddy Animation (if present)
        if (funtimeFreddyActor != null && funtimeFreddyTexture1 != null && funtimeFreddyTexture2 != null) {
            funtimeFreddyAnimTimer += Gdx.graphics.getDeltaTime();
            if (funtimeFreddyAnimTimer > 0.5f) { // Switch every 0.5 seconds
                funtimeFreddyAnimTimer = 0;
                funtimeFreddyFrame1 = !funtimeFreddyFrame1;

                // We need to use setDrawable for Image actors
                if (funtimeFreddyFrame1) {
                    funtimeFreddyActor.setDrawable(new TextureRegionDrawable(new TextureRegion(funtimeFreddyTexture1)));
                } else {
                    funtimeFreddyActor.setDrawable(new TextureRegionDrawable(new TextureRegion(funtimeFreddyTexture2)));
                }
            }
        }

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

    // Variables eliminadas: distanceWalked y selectionEventTriggered ya no son
    // necesarias

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

        if (dx == 0 && dy == 0)
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

            // NPC collision checks removed

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

            // NPC collision checks removed

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

        // Transition from Map 1 to Pokemon Center
        if (mapPath.equals("Mapa/mapa11.tmx") && tileX == 38 && tileY == 37) {
            startTransitionToPokemonCenter();
        }

        // Transition from Pokemon Center back to Map 1 (exit when walking down toward
        // the door)
        if (mapPath.contains("Centro Pokemon interior") && tileX == 6 && tileY == 1) {
            startTransitionBackToMap1();
        }

        // --- TRANSICIÓN MAPA 1 <-> MAPA 2 ---

        // LOG SOLICITADO: Solo imprime el tile actual si CAMBIA
        if (tileX != lastLogTileX || tileY != lastLogTileY) {
            Gdx.app.log("COORD", "Tile: " + tileX + ", " + tileY);
            lastLogTileX = tileX;
            lastLogTileY = tileY;
        }

        // Agregamos un tiempo de gracia (2 segundos) para evitar loops inmediatos al
        // hacer spawn en un tile de transición
        if (stateTime > 2.0f) {
            // Transición de Mapa 1 a Mapa 2 (Tiles 0,17 a 0,21 EXTREMA IZQUIERDA)
            if (mapPath.equals("Mapa/mapa11.tmx") && tileX == 0 && (tileY >= 17 && tileY <= 21)) {
                Gdx.app.log("TRANSITION", "Iniciando transición a Mapa 2 desde X=" + tileX + ", Y=" + tileY);
                startTransitionToMap2();
            }

            // Transición de Mapa 2 a Mapa 1 (Borde Derecho Tile 49)
            // Tiled Y=44 -> LibGDX Y=26. Damos un margen.
            if (mapPath.equals("Mapa/Mapa2.tmx") && tileX == 49 && (tileY >= 24 && tileY <= 28)) {
                Gdx.app.log("TRANSITION", "Iniciando transición a Mapa 1 desde X=" + tileX + ", Y=" + tileY);
                startTransitionBackToMap1FromMap2();
            }

            if (mapPath.equals("Mapa/Mapa2.tmx") && tileX == 45 && tileY == 28) {
                Gdx.app.log("TRANSITION", "Iniciando transición a Centro Pokémon desde Mapa 2");
                startTransitionToPokemonCenterFromMap2();
            }

            // Transición de Mapa 1 a Mapa 3 (Tiles 49, 21-25)
            if (mapPath.equals("Mapa/mapa11.tmx") && tileX == 49 && (tileY >= 21 && tileY <= 25)) {
                Gdx.app.log("TRANSITION", "Iniciando transición a Mapa 3 desde X=" + tileX + ", Y=" + tileY);
                startTransitionToMap3();
            }

            // Transición de Mapa 3 a Centro Pokémon (Tile 25, 34)
            if (mapPath.equals("Mapa/MAPA3.tmx") && tileX == 25 && tileY == 34) {
                Gdx.app.log("TRANSITION", "Iniciando transición a Centro Pokémon desde Mapa 3");
                startTransitionToPokemonCenterFromMap3();
            }

            // Transición de Mapa 3 a Mapa 1 (Borde Izquierdo X=0, Y=22-30)
            if (mapPath.equals("Mapa/MAPA3.tmx") && tileX == 0 && (tileY >= 22 && tileY <= 30)) {
                Gdx.app.log("TRANSITION", "Iniciando transición a Mapa 1 desde Mapa 3");
                startTransitionBackToMap1FromMap3();
            }

            // Transición de Mapa 1 a Mapa 4 (Tiles 22-27, 49)
            if (mapPath.equals("Mapa/mapa11.tmx") && tileY == 49 && (tileX >= 22 && tileX <= 27)) {
                Gdx.app.log("TRANSITION", "Iniciando transición a Mapa 4 desde X=" + tileX + ", Y=" + tileY);
                startTransitionToMap4();
            }

            // Transición de Mapa 4 a Mapa 1 (Borde Inferior Y=0)
            if (mapPath.equals("Mapa/MAPA 4.tmx") && tileY == 0 && (tileX >= 22 && tileX <= 27)) {
                Gdx.app.log("TRANSITION", "Iniciando transición a Mapa 1 desde Mapa 4");
                startTransitionBackToMap1FromMap4();
            }
        }
    }

    /**
     * Clamps the camera position to prevent showing the black void outside the map.
     * When the player reaches the map edge, the camera stops moving but the player
     * can continue.
     */
    private void clampCamera() {
        // Calculate desired camera position (centered on player)
        float desiredCameraX = characterActor.getX() + characterActor.getWidth() / 2;
        float desiredCameraY = characterActor.getY() + characterActor.getHeight() / 2;

        // Calculate map bounds in world units
        float tileWidth = map.getProperties().get("tilewidth", Integer.class);
        float tileHeight = map.getProperties().get("tileheight", Integer.class);
        float mapWidth = mapWidthTiles * tileWidth * UNIT_SCALE;
        float mapHeight = mapHeightTiles * tileHeight * UNIT_SCALE;

        // Calculate camera bounds (half viewport on each side)
        float cameraHalfWidth = camera.viewportWidth / 2;
        float cameraHalfHeight = camera.viewportHeight / 2;

        // Clamp camera position to keep it within the map
        float minCameraX = cameraHalfWidth;
        float maxCameraX = mapWidth - cameraHalfWidth;
        float minCameraY = cameraHalfHeight;
        float maxCameraY = mapHeight - cameraHalfHeight;

        camera.position.x = Math.max(minCameraX, Math.min(desiredCameraX, maxCameraX));
        camera.position.y = Math.max(minCameraY, Math.min(desiredCameraY, maxCameraY));
    }

    private void renderArceusMarker(float delta) {
        if (!mapPath.contains("mapa11.tmx"))
            return;

        arceusMarkerTimer += delta;

        // Efecto de pulso
        float pulse = 0.5f + 0.5f * com.badlogic.gdx.math.MathUtils.sin(arceusMarkerTimer * 3f);
        float scale = 1.0f + 0.3f * pulse;

        // Posición en coordenadas del mundo (Tile 13, 5)
        // Nota: Los tiles son de 32x32 (UNIT_SCALE = 1 si no hay escalado, pero aquí
        // parece que se usa 1)
        // Ajustar según el tamaño real de los tiles y la escala
        float tileWidth = map.getProperties().get("tilewidth", Integer.class);
        float tileHeight = map.getProperties().get("tileheight", Integer.class);

        float worldX = 13 * tileWidth * UNIT_SCALE + (tileWidth * UNIT_SCALE / 2);
        float worldY = 5 * tileHeight * UNIT_SCALE + (tileHeight * UNIT_SCALE / 2);

        shapeRenderer.setProjectionMatrix(camera.combined);
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1f, 0.9f, 0f, 0.4f + 0.3f * pulse); // Amarillo brillante con transparencia variable
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

        // 1. Manejo del Diálogo (ENTER o Z)
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

        // 2. Manejo de PAUSA / DESPAUSA (ESC)
        if (keycode == Keys.ESCAPE) {
            if (currentState == GameState.FREE_ROAMING || currentState == GameState.PAUSED) {
                togglePauseMenu();
                return true;
            } else if (currentState == GameState.INVENTORY_SUBMENU) {
                currentState = GameState.PAUSED;
                inventorySubmenuTable.setVisible(false);
                pauseMenuTable.setVisible(true);
                return true;
            } else if (currentState == GameState.CRAFTING) {
                // Back to Submenu
                currentState = GameState.INVENTORY_SUBMENU;
                craftingMenuTable.setVisible(false);
                inventorySubmenuTable.setVisible(true);
                return true;
            } else if (currentState == GameState.INVENTORY) {
                // Back to Submenu
                currentState = GameState.INVENTORY_SUBMENU;
                inventoryMenuTable.setVisible(false);
                inventorySubmenuTable.setVisible(true);
                return true;

            } else if (currentState == GameState.POKEDEX_SUBMENU) {
                currentState = GameState.PAUSED;
                pokedexSubmenuTable.setVisible(false);
                pauseMenuTable.setVisible(true);
                return true;
            } else if (currentState == GameState.POKEDEX) {
                showPokedexMenu(); // Back to submenu
                return true;
            } else if (currentState == GameState.TEAM_SELECTION) {
                showPokedexMenu(); // Back to submenu
                return true;
            }
        }

        // 3. Manejo del MENÚ DE PAUSA
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

        // 4. Manejo del MENÚ DE CRAFTEO
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

        // 5. Manejo del MENÚ DE INVENTARIO
        if (currentState == GameState.INVENTORY) {
            if (keycode == Keys.ESCAPE || keycode == Keys.ENTER || keycode == Keys.Z) {
                currentState = GameState.PAUSED;
                inventoryMenuTable.setVisible(false);
                pauseMenuTable.setVisible(true);
                return true;
            }
            return false;
        }

        // 5.5 Manejo del SUBMENÚ DE POKEDEX
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
                }
                return true;
            }
            return false;
        }

        // 5.6 Manejo de LISTA DE POKEDEX
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
                    // Find actual pokemon object
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
                        // Not caught, can't heal. Warning?
                        // Just ignore or show generic message
                    }
                }
                return true;
            }
            return false;
        }

        // 5.6.5 Manejo de POKEDEX ACTION MENU
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

        // 5.6.6 Manejo de POKEMON_INFO
        if (currentState == GameState.POKEMON_INFO) {
            if (keycode == Keys.ESCAPE || keycode == Keys.X) {
                currentState = GameState.POKEDEX;
                pokemonInfoTable.setVisible(false);
                pokedexMenuTable.setVisible(true);
                return true;
            }
            return true;
        }

        // 5.6.6 Manejo de INVENTORY_SUBMENU
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

        // 5.7 Manejo de SELECCIÓN DE EQUIPO
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

        // 5. Interacción con ARCEUS (Batalla Final) - Tile 13, 5
        if ((keycode == Keys.ENTER || keycode == Keys.Z) && currentState == GameState.FREE_ROAMING) {
            float tileWidth = map.getProperties().get("tilewidth", Integer.class);
            float tileHeight = map.getProperties().get("tileheight", Integer.class);

            // Usamos el centro del personaje para ser más precisos
            int centerX = (int) ((characterActor.getX() + characterActor.getWidth() / 2) / (tileWidth * UNIT_SCALE));
            int centerY = (int) ((characterActor.getY() + characterActor.getHeight() / 2) / (tileHeight * UNIT_SCALE));

            if (centerX == 13 && centerY == 5 && mapPath.contains("mapa11.tmx")) {
                startArceusBattleDialogue();
                return true;
            }
        }

        // 6. Manejo de INTERACCIÓN (ENTER o Z) - LÓGICA DE RECOLECCIÓN
        if (currentState == GameState.FREE_ROAMING && (keycode == Keys.ENTER || keycode == Keys.Z))

        {
            // Check for NPC interaction
            // NPCs removed by user request

            float playerCenterX = characterActor.getX() + characterActor.getWidth() / 2;
            float playerCenterY = characterActor.getY() + characterActor.getHeight() / 2;

            // Check for Special Event at Tile (21, 48) on "NIvel 1"
            // Player starts at (11, 38), event is 10 tiles right + 10 tiles up = (21, 48)
            int tileX = (int) (playerCenterX / (map.getProperties().get("tilewidth", Integer.class) * UNIT_SCALE));
            int tileY = (int) (playerCenterY / (map.getProperties().get("tileheight", Integer.class) * UNIT_SCALE));

            // Check if player is at the special event coordinates
            if (tileX == 21 && tileY == 48) {
                TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("NIvel 1");
                if (layer != null) {
                    Cell cell = layer.getCell(tileX, tileY);
                    if (cell != null && cell.getTile() != null && cell.getTile().getId() == 5106) {
                        // Trigger Special Event
                        triggerSpecialEvent();
                        return true;
                    }
                }
            }

            boolean collectedSomething = false;

            for (int i = collectibles.size - 1; i >= 0; i--) {
                Collectible collectible = collectibles.get(i);

                if (collectible.isInRange(playerCenterX, playerCenterY)) {

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

                        collectible.getActor().remove();
                        collectibles.removeIndex(i);

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

            // Interaction at Pokemon Center desk (Tile 7, 6)
            if (mapPath.contains("Centro Pokemon interior")) {
                int pTileX = (int) (playerCenterX / (map.getProperties().get("tilewidth", Integer.class) * UNIT_SCALE));
                int pTileY = (int) (playerCenterY
                        / (map.getProperties().get("tileheight", Integer.class) * UNIT_SCALE));

                // User requested interaction at receptionist (3, 4)
                // Check proximity to (3, 4)
                if (Math.abs(pTileX - 3) <= 1 && Math.abs(pTileY - 4) <= 1) {
                    dialogueQueue = new LinkedList<>();
                    dialogueQueue.add("¡Hola! Bienvenido al Centro Pokémon.");
                    dialogueQueue.add("Nos encargamos de curar a tus compañeros heridos.");
                    dialogueQueue.add(
                            "Aqui puedes restaurar la vida y los ataques de tus pokemones. ¿Deseas hacerlo?");

                    currentState = GameState.DIALOGUE;
                    dialogBox.setText(dialogueQueue.poll());

                    // Set callback to show options after text
                    onDialogCompleteAction = new Runnable() {
                        @Override
                        public void run() {
                            showYesNoChoice();
                        }
                    };
                    return true;
                }
            }

            // Interaction with CircusBaby NPC at (27, 34) on Map 1
            if (mapPath.equals("Mapa/mapa11.tmx")) {
                int pTileX = (int) (playerCenterX / (map.getProperties().get("tilewidth", Integer.class) * UNIT_SCALE));
                int pTileY = (int) (playerCenterY
                        / (map.getProperties().get("tileheight", Integer.class) * UNIT_SCALE));

                // If player is close to 27, 34
                if (Math.abs(pTileX - 27) <= 1 && Math.abs(pTileY - 34) <= 1) {
                    dialogueQueue = new LinkedList<>();
                    dialogueQueue.add("¡Hola! Soy Circus Baby. Me encanta este ambiente tan calmado.");
                    dialogueQueue.add("¿Sabías que en este pueblo hay cuatro zonas donde aparecen pokemones?");
                    dialogueQueue.add(
                            "Los pokemones que aparecen son: Rowlet, Yvasur, Serperior y Pikachu. Si buscas otros, tal vez deberías echarle un vistazo a los otros pueblos");
                    dialogueQueue.add("¡Buena suerte en tu aventura!");

                    currentState = GameState.DIALOGUE;
                    processNextDialogueLine();
                    return true;
                }

                // Interaction with Funtime Foxy NPC at (16, 24) on Map 1
                if (Math.abs(pTileX - 16) <= 1 && Math.abs(pTileY - 24) <= 1) {
                    dialogueQueue = new LinkedList<>();
                    dialogueQueue.add(
                            "¡Hola! ¿Cómo estás? Soy Funtime Foxy. ¿Sabías que al norte (donde hay un jardín) aparecen pokemones?");
                    dialogueQueue.add(
                            "Pero, ¡cuidado! No te dirijas sin antes saber esto: los pokemones son criaturas salvajes que quizás no sean tan amigables con las personas del pueblo...es por eso que puede que tengas una pequeña lucha con ellos.");
                    dialogueQueue.add(
                            "En la batalla de pokemones, presiona LUCHAR para elegir el ataque que usará tu pokemón. Los ataques no son infinitos, tienen un límite de uso según su poder.");
                    dialogueQueue.add(
                            "En MOCHILA, puedes elegir CAPTURAR para intentar atrapar a un pokemón (esto funciona solo si el pokemón rival está debilitado). También puedes elegir CURAR para restaurar una parte de la vida de tu pokemón.");
                    dialogueQueue.add(
                            "En POKEMON, tienes la opción de cambiar el pokemón de combate (esto es útil si ves que tu pokemón es débil ante el rival). No se puede cambiar por cualquier pokemón de tu pokédex, sino por un EQUIPO POKEMÓN elegido anteriormente.");
                    dialogueQueue.add(
                            "Para elegir un equipo pokemón, debes poseer al menos 3 pokemones. Finalmente, si crees que no puedes ganar la batalla, tienes la opción de HUIR y evitar ese combate. ¡Buena suerte en tu aventura!");

                    currentState = GameState.DIALOGUE;
                    processNextDialogueLine();
                    return true;
                }

                // Interaction with Funtime Freddy NPC at (23, 13) on Map 1
                if (Math.abs(pTileX - 19) <= 1 && Math.abs(pTileY - 13) <= 1) {
                    dialogueQueue = new LinkedList<>();
                    dialogueQueue.add(
                            "¡Hola, entrenador! Soy Funtime Freddy. ¿Sabías que este pueblo no es el único lugar que puedes explorar?");
                    dialogueQueue.add(
                            "Si te diriges hacia el oeste (izquierda), encontrarás un camino que te llevará a otro mapa lleno de nuevas aventuras y pokemones diferentes.");
                    dialogueQueue.add(
                            "Mientras exploras, no olvides recoger objetos que encuentres en el suelo. Estos materiales son muy útiles para craftear objetos como Pokéballs y pociones.");
                    dialogueQueue.add(
                            "Ah, y si tus pokemones están heridos después de una batalla, visita el Centro Pokémon. Está ubicado al norte de aquí, cerca del jardín. ¡La enfermera curará a todos tus pokemones sin costo alguno!");
                    dialogueQueue.add(
                            "Recuerda: explorar, recolectar y cuidar de tus pokemones son las claves para convertirte en un gran entrenador. ¡Buena suerte!");

                    currentState = GameState.DIALOGUE;
                    processNextDialogueLine();
                    return true;
                }
            }

            // Interaction with Battle NPCs on Map 2
            if (mapPath.equals("Mapa/Mapa2.tmx")) {
                int pTileX = (int) (playerCenterX / (map.getProperties().get("tilewidth", Integer.class) * UNIT_SCALE));
                int pTileY = (int) (playerCenterY
                        / (map.getProperties().get("tileheight", Integer.class) * UNIT_SCALE));

                // Freddy Map 2 Info at (29, 29)
                if (Math.abs(pTileX - 29) <= 1 && Math.abs(pTileY - 29) <= 1) {
                    dialogueQueue = new LinkedList<>();
                    dialogueQueue.add("¡Hola! Soy Freddy.");
                    dialogueQueue.add("Este lugar es el hogar de Pokémon de tipo Agua.");
                    dialogueQueue.add("Podrás encontrar fácilmente a Oshawott.");
                    dialogueQueue.add("También he visto a Vaporeon y Sylveon paseando por la hierba.");
                    dialogueQueue.add("¡Ten cuidado! Un poderoso Blastoise protege esta zona.");
                    dialogueQueue.add("Ah, y a veces aparece Jolteon, ¡es muy rápido!");

                    currentState = GameState.DIALOGUE;
                    processNextDialogueLine();
                    return true;
                }

                // Giorno at (4, 51) - Jolteon
                if (Math.abs(pTileX - 4) <= 1 && Math.abs(pTileY - 51) <= 1) {
                    if (!giornoDefeated) {
                        dialogueQueue = new LinkedList<>();
                        dialogueQueue.add("¡Alto ahí, entrenador!");
                        dialogueQueue.add("Soy Giorno Giovanna, y tengo un sueño...");
                        dialogueQueue.add("¡Demostrar que mi Jolteon es el más rápido de todos!");
                        dialogueQueue.add("¿Aceptas mi desafío?");

                        currentState = GameState.DIALOGUE;
                        processNextDialogueLine();

                        // Set up battle after dialogue
                        onDialogCompleteAction = new Runnable() {

                            @Override
                            public void run() {
                                startNPCBattle("Jolteon", "Giorno");
                            }
                        };
                    } else {
                        dialogueQueue = new LinkedList<>();
                        dialogueQueue.add("¡Increíble batalla! Tu habilidad es admirable.");
                        dialogueQueue.add("Sigue entrenando y alcanzarás tu sueño.");
                        currentState = GameState.DIALOGUE;
                        processNextDialogueLine();
                    }
                    return true;
                }

                // Jotaro at (10, 33) - Blastoise
                if (Math.abs(pTileX - 10) <= 1 && Math.abs(pTileY - 33) <= 1) {
                    if (!jotaroDefeated) {
                        dialogueQueue = new LinkedList<>();
                        dialogueQueue.add("Yare yare daze...");
                        dialogueQueue.add("Parece que tenemos un entrenador valiente aquí.");
                        dialogueQueue.add("Mi Blastoise y yo hemos enfrentado muchos desafíos.");
                        dialogueQueue.add("¿Estás listo para enfrentarnos?");

                        currentState = GameState.DIALOGUE;
                        processNextDialogueLine();

                        onDialogCompleteAction = new Runnable() {
                            @Override
                            public void run() {
                                startNPCBattle("Blastoise", "Jotaro");
                            }
                        };
                    } else {
                        dialogueQueue = new LinkedList<>();
                        dialogueQueue.add("Yare yare... no está mal.");
                        dialogueQueue.add("Tienes potencial. Sigue así.");
                        currentState = GameState.DIALOGUE;
                        processNextDialogueLine();
                    }
                    return true;
                }

                // Kaneki at (41, 44) - Mewtwo
                if (Math.abs(pTileX - 41) <= 1 && Math.abs(pTileY - 44) <= 1) {
                    if (!kanekiDefeated) {
                        dialogueQueue = new LinkedList<>();
                        dialogueQueue.add("Este mundo... está lleno de batallas.");
                        dialogueQueue.add("He aprendido a sobrevivir en la oscuridad.");
                        dialogueQueue.add("Mi Mewtwo y yo somos uno. Nuestro poder es absoluto.");
                        dialogueQueue.add("¿Tienes el coraje de enfrentarnos?");

                        currentState = GameState.DIALOGUE;
                        processNextDialogueLine();

                        onDialogCompleteAction = new Runnable() {

                            @Override
                            public void run() {
                                startNPCBattle("Mewtwo", "Kaneki");
                            }
                        };
                    } else {
                        dialogueQueue = new LinkedList<>();
                        dialogueQueue.add("Eres fuerte... muy fuerte.");
                        dialogueQueue.add("Quizás hay esperanza en este mundo después de todo.");
                        currentState = GameState.DIALOGUE;
                        processNextDialogueLine();
                    }
                    return true;
                }
            }

            // --- MAP 3 NPCS ---
            if (mapPath.equals("Mapa/MAPA3.tmx")) {

                int pTileX = (int) (playerCenterX / (map.getProperties().get("tilewidth", Integer.class) * UNIT_SCALE));
                int pTileY = (int) (playerCenterY
                        / (map.getProperties().get("tileheight", Integer.class) * UNIT_SCALE));

                // Pennywise at (14, 23)
                if (Math.abs(pTileX - 14) <= 1 && Math.abs(pTileY - 23) <= 1) {
                    if (!pennywiseDefeated) {
                        dialogueQueue = new LinkedList<>();
                        dialogueQueue.add("Hola, Georgie...");
                        dialogueQueue.add("¿Quieres un globo?");
                        dialogueQueue.add("¡Aquí abajo todos flotan!");
                        dialogueQueue.add("¡Y tú también flotarás!");

                        currentState = GameState.DIALOGUE;
                        processNextDialogueLine();

                        onDialogCompleteAction = new Runnable() {
                            @Override
                            public void run() {
                                startNPCBattle("Gyarados", "Pennywise");
                            }
                        };
                    } else {
                        dialogueQueue = new LinkedList<>();
                        dialogueQueue.add("¡Imposible! ¡No te tengo miedo!");
                        dialogueQueue.add("Te has vuelto fuerte...");
                        currentState = GameState.DIALOGUE;
                        processNextDialogueLine();
                    }
                    return true;
                }

                // Bonnie at (4, 28)
                if (Math.abs(pTileX - 4) <= 1 && Math.abs(pTileY - 28) <= 1) {
                    dialogueQueue = new LinkedList<>();
                    dialogueQueue.add("¡Hola! ¿Has visto a los Pokémon de aquí?");
                    dialogueQueue.add("Dicen que se pueden encontrar varias evoluciones de Eevee.");
                    dialogueQueue.add("He visto a Vaporeon, Jolteon y Flareon merodeando.");
                    dialogueQueue.add("¡También un Ivysaur suele aparecer por los jardines!");
                    dialogueQueue.add("Buena suerte atrapándolos a todos.");

                    currentState = GameState.DIALOGUE;
                    processNextDialogueLine();
                    return true;
                }

                // Don Valerio at (44, 30)
                if (Math.abs(pTileX - 44) <= 1 && Math.abs(pTileY - 30) <= 1) {
                    if (!donValerioDefeated) {
                        dialogueQueue = new LinkedList<>();
                        dialogueQueue.add("¡Alto ahí, joven!");
                        dialogueQueue.add("Soy Don Valerio, el guardián de estas tierras.");
                        dialogueQueue.add("Mis años de experiencia te enseñarán respeto.");
                        dialogueQueue.add("¡Prepárate para una lección!");

                        currentState = GameState.DIALOGUE;
                        processNextDialogueLine();

                        onDialogCompleteAction = new Runnable() {
                            @Override
                            public void run() {
                                startNPCBattle("Sylveon", "Don Valerio");
                            }
                        };
                    } else {
                        dialogueQueue = new LinkedList<>();
                        dialogueQueue.add("Jojo... no está nada mal para un joven.");
                        dialogueQueue.add("Tienes carácter. Sigue tu camino.");
                        currentState = GameState.DIALOGUE;
                        processNextDialogueLine();
                    }
                    return true;
                }
            }

            // --- MAP 4 NPCS ---
            if (mapPath.equals("Mapa/MAPA 4.tmx")) {
                int pTileX = (int) (playerCenterX / (map.getProperties().get("tilewidth", Integer.class) * UNIT_SCALE));
                int pTileY = (int) (playerCenterY
                        / (map.getProperties().get("tileheight", Integer.class) * UNIT_SCALE));

                // Jesucristo at (2, 4)
                if (Math.abs(pTileX - 2) <= 1 && Math.abs(pTileY - 4) <= 1) {
                    if (!jesucristoDefeated) {
                        dialogueQueue = new LinkedList<>();
                        dialogueQueue.add("Hijo mío, has recorrido un largo camino.");
                        dialogueQueue.add("Pero para alcanzar la salvación, debes probar tu fuerza.");
                        dialogueQueue.add("¿Estás listo para enfrentar la luz?");

                        currentState = GameState.DIALOGUE;
                        processNextDialogueLine();

                        onDialogCompleteAction = new Runnable() {
                            @Override
                            public void run() {
                                startNPCBattle("Lucario", "Jesucristo");
                            }
                        };
                    } else {
                        dialogueQueue = new LinkedList<>();
                        dialogueQueue.add("Ve en paz, hijo mío.");
                        dialogueQueue.add("La luz siempre te acompañará.");
                        currentState = GameState.DIALOGUE;
                        processNextDialogueLine();
                    }
                    return true;
                }

                // Afton at (45, 4)
                if (Math.abs(pTileX - 45) <= 1 && Math.abs(pTileY - 4) <= 1) {
                    if (!aftonDefeated) {
                        dialogueQueue = new LinkedList<>();
                        dialogueQueue.add("No deberías estar aquí...");
                        dialogueQueue.add("He vuelto... siempre vuelvo.");
                        dialogueQueue.add("Ahora, tu alma será mía.");

                        currentState = GameState.DIALOGUE;
                        processNextDialogueLine();

                        onDialogCompleteAction = new Runnable() {
                            @Override
                            public void run() {
                                startNPCBattle("Mewtwo", "Afton");
                            }
                        };
                    } else {
                        dialogueQueue = new LinkedList<>();
                        dialogueQueue.add("Volveré...");
                        dialogueQueue.add("Siempre vuelvo...");
                        currentState = GameState.DIALOGUE;
                        processNextDialogueLine();
                    }
                    return true;
                }

                // Info NPC at (21, 12)
                if (Math.abs(pTileX - 21) <= 1 && Math.abs(pTileY - 12) <= 1) {
                    dialogueQueue = new LinkedList<>();
                    dialogueQueue.add("¡Hola viajero! Este lugar es peligroso.");
                    dialogueQueue.add("En estas tierras volcánicas aparecen Pokémon muy fuertes.");
                    dialogueQueue.add("Podrás encontrar a Cyndaquil, Flareon y Charizard.");
                    dialogueQueue.add("¡Incluso se dice que un Gyarados habita en las zonas de lava!");
                    dialogueQueue.add("Ten mucho cuidado.");

                    currentState = GameState.DIALOGUE;
                    processNextDialogueLine();
                    return true;
                }
            }
        }

        // 7. Manejo de MOVIMIENTO (solo si FREE_ROAMING)
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

            // DEBUG KEYS
            // 'L' to record current tile
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

            // 'P' to print all recorded tiles
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
        // Camera update is handled by viewport.update()

        uiStage.getViewport().update(width, height, true);

        if (dialogBox != null) {
            // Dialog box resizing logic might need adjustment if it depended on screen
            // coordinates
            // But with FitViewport, the virtual size is constant, so this might be
            // redundant or just safe
            dialogBox.resize(uiStage.getWidth(), uiStage.getHeight());
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        // NO destruir recursos si solo estamos yendo a una batalla
        // Solo destruir si realmente estamos saliendo de la pantalla
        if (currentState != GameState.BATTLE) {
            dispose();
        }
    }

    private void initializePokedexMenu() {
        Label.LabelStyle titleStyle = new Label.LabelStyle(font, Color.YELLOW);
        Texture darkBackground = createColoredTexture(new Color(0.1f, 0.1f, 0.1f, 0.9f));
        TextureRegionDrawable backgroundDrawable = new TextureRegionDrawable(darkBackground);

        // --- 1. POKEDEX LIST TABLE ---
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

        // --- 2. SUBMENU TABLE ---
        pokedexSubmenuTable = new Table();
        pokedexSubmenuTable.setFillParent(true);
        pokedexSubmenuTable.setBackground(backgroundDrawable);
        pokedexSubmenuTable.pad(50);
        pokedexSubmenuTable.setVisible(false);
        uiStage.addActor(pokedexSubmenuTable);

        // --- 3. TEAM SELECTION TABLE ---
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
        Label.LabelStyle infoStyle = new Label.LabelStyle(font, new Color(0.9f, 0.9f, 0.6f, 1f)); // Light yellow for
                                                                                                  // stats

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
            // Asegurar que dialogBox y dialogueQueue están inicializados
            if (dialogBox == null) {
                dialogBox = new DialogBox(uiStage, font);
            }
            if (dialogueQueue == null) {
                dialogueQueue = new LinkedList<>();
            }

            dialogueQueue.clear(); // Limpiar cualquier diálogo previo
            dialogueQueue.add("Ya has elegido tu Pokémon inicial.");
            currentState = GameState.DIALOGUE;
            processNextDialogueLine();
            return;
        }

        // Asegurar que dialogBox y dialogueQueue están inicializados
        if (dialogBox == null) {
            dialogBox = new DialogBox(uiStage, font);
        }
        if (dialogueQueue == null) {
            dialogueQueue = new LinkedList<>();
        }

        dialogueQueue.clear(); // Limpiar cualquier diálogo previo
        dialogueQueue.add("¡Alto ahí!");
        dialogueQueue.add("Tenemos una batalla urgente.");
        dialogueQueue.add("¡Elige un Pokémon rápido!");

        currentState = GameState.DIALOGUE;
        processNextDialogueLine();

        // We need to detect when this specific dialogue sequence ends to trigger the
        // selection screen.
        // I'll add a flag or check in endDialogueSequence.
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

    @Override
    public void dispose() {
        // Dispose Map 2 NPC textures
        if (giornoTexture1 != null)
            giornoTexture1.dispose();
        if (giornoTexture2 != null)
            giornoTexture2.dispose();
        if (jotaroTexture1 != null)
            jotaroTexture1.dispose();
        if (jotaroTexture2 != null)
            jotaroTexture2.dispose();
        if (kanekiTexture1 != null)
            kanekiTexture1.dispose();
        if (kanekiTexture2 != null)
            kanekiTexture2.dispose();

        // Dispose other textures
        if (circusBabyTexture1 != null)
            circusBabyTexture1.dispose();
        if (circusBabyTexture2 != null)
            circusBabyTexture2.dispose();
        if (funtimeFoxyTexture1 != null)
            funtimeFoxyTexture1.dispose();
        if (funtimeFoxyTexture2 != null)
            funtimeFoxyTexture2.dispose();
        if (funtimeFreddyTexture1 != null)
            funtimeFreddyTexture1.dispose();
        if (funtimeFreddyTexture2 != null)
            funtimeFreddyTexture2.dispose();
        if (yoelTexture != null)
            yoelTexture.dispose();
        if (recepcionistaTexture != null)
            recepcionistaTexture.dispose();
        if (blackPixelTexture != null)
            blackPixelTexture.dispose();

        // Dispose stages and other resources
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

        // Dispose item textures
        for (Texture texture : itemTextures.values()) {
            texture.dispose();
        }
    }

}
