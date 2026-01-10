package sodyl.proyecto.libGDX;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import sodyl.proyecto.clases.UserManager;
import sodyl.proyecto.clases.Pokedex;
import sodyl.proyecto.clases.Inventario;
import sodyl.proyecto.clases.PlayerData;

public class Proyecto extends Game { // extiende de game, o sea es la clase que inicia nuestro juego
    // VARIABLES GLOBALES (variables que se usan en todo el código)
    private SpriteBatch batch;
    private PlayerData playerData;
    private Texture fondoMenu;
    public static final float PANTALLA_W = 1000;
    public static final float PANTALLA_H = 720;
    private Viewport viewport;
    private OrthographicCamera camera;
    private Map<String, Array<Collectible>> mapCollectibles = new HashMap<>(); // esto es un mapa que rastrea los
                                                                               // objetos que hay en cada zona
    private Set<String> defeatedNPCs = new HashSet<>(); // esto es un set que rastrea los NPCs que han sido derrotados
    private Music currentMusic;
    private String currentMusicPath;

    public void playMusic(String path) {
        if (currentMusicPath != null && currentMusicPath.equals(path)) {
            return;
        }

        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
        }

        currentMusicPath = path;
        currentMusic = Gdx.audio.newMusic(Gdx.files.internal(path));
        currentMusic.setLooping(true);
        currentMusic.play();
    }

    public Map<String, Array<Collectible>> getMapCollectibles() {
        return mapCollectibles;
    }

    public Set<String> getDefeatedNPCs() {
        return defeatedNPCs;
    }

    public boolean hasSaveData(String username) { // función para saber si existe un usuario
        if (username == null)
            return false;
        return Pokedex.exists(username) || Inventario.exists(username)
                || Gdx.files.local(username + "_npcs.json").exists()
                || Gdx.files.local(username + "_player.json").exists();
    }

    public PlayerData getPlayerData() { // función para obtener los datos del jugador
        return playerData;
    }

    public void saveProgress(Inventario inventory, String currentMap, float x, float y) { // función para guardar el
                                                                                          // progreso del jugador
        String user = UserManager.getCurrentUser();
        if (user == null)
            return;

        // Guardar datos del jugador
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);

        // Actualizar cache local
        this.playerData = new PlayerData(currentMap, x, y);

        FileHandle filePlayer = Gdx.files.local(user + "_player.json");
        filePlayer.writeString(json.prettyPrint(this.playerData), false);

        // Guardar Pokédex
        Pokedex.save();

        // Guardar Inventario
        if (inventory != null) {
            inventory.save(user);
        }

        // Guardar NPCs
        FileHandle fileNpcs = Gdx.files.local(user + "_npcs.json");
        fileNpcs.writeString(json.prettyPrint(defeatedNPCs), false);

        Gdx.app.log("PROYECTO", "Progreso global guardado para el usuario: " + user + " en Mapa: " + currentMap);
    }

    public void saveProgress(Inventario inventory) {
        saveProgress(inventory, "Mapa/MAPACOMPLETO.tmx", 0, 0);
    }

    public void loadProgress(String username) { // función para cargar el progreso del jugador
        if (username == null)
            return;
        UserManager.setCurrentUser(username);

        // Cargar Pokédex
        Pokedex.load();

        // Cargar NPCs
        FileHandle file = Gdx.files.local(username + "_npcs.json");
        if (file.exists()) {
            try {
                Json json = new Json();
                @SuppressWarnings("unchecked")
                Set<String> loaded = json.fromJson(HashSet.class, String.class, file.readString());
                if (loaded != null) {
                    defeatedNPCs.clear();
                    defeatedNPCs.addAll(loaded);
                }
            } catch (Exception e) {
                Gdx.app.error("PROYECTO", "Error al cargar NPCs", e);
            }
        }

        // Cargar datos del jugador
        FileHandle filePlayer = Gdx.files.local(username + "_player.json");
        if (filePlayer.exists()) {
            try {
                Json json = new Json();
                playerData = json.fromJson(PlayerData.class, filePlayer.readString());
                Gdx.app.log("PROYECTO", "Datos del jugador cargados: " + playerData.currentMap);
            } catch (Exception e) {
                Gdx.app.error("PROYECTO", "Error al cargar datos del jugador", e);
            }
        }
        Gdx.app.log("PROYECTO", "Progreso global cargado para el usuario: " + username);
    }

    public void clearProgress() {
        String user = UserManager.getCurrentUser();
        defeatedNPCs.clear();
        mapCollectibles.clear();
        Pokedex.clear();

        if (user != null) {
            Gdx.files.local(user + "_pokedex.json").delete();
            Gdx.files.local(user + "_inventory.json").delete();
            Gdx.files.local(user + "_npcs.json").delete();
            Gdx.files.local(user + "_player.json").delete();
            Gdx.app.log("PROYECTO", "Archivos eliminados para el usuario: " + user);
        }
    }

    @Override
    public void create() { // función que se ejecuta al crear el juego
        camera = new OrthographicCamera();
        viewport = new FitViewport(PANTALLA_W, PANTALLA_H, camera);
        batch = new SpriteBatch();
        fondoMenu = new Texture("imagenes/fondooo.png");
        setScreen(new BackgroundScreen(this)); // aquí pasamos al siguiente screen
    }

    @Override
    public void dispose() {
        super.dispose();
        if (currentMusic != null) {
            currentMusic.dispose();
        }
        if (batch != null) {
            batch.dispose();
        }
        if (fondoMenu != null) {
            fondoMenu.dispose();
        }
    }
}
