package sodyl.proyecto.libGDX;

import com.badlogic.gdx.Game;
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

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all
 * platforms.
 */
public class Proyecto extends Game {
    private SpriteBatch batch;
    private PlayerData playerData;
    private Texture fondoMenu;
    public static final float PANTALLA_W = 1000;
    public static final float PANTALLA_H = 720;
    private Viewport viewport;
    private OrthographicCamera camera;
    private Map<String, Array<Collectible>> mapCollectibles = new HashMap<>();
    private Set<String> defeatedNPCs = new HashSet<>();

    public Map<String, Array<Collectible>> getMapCollectibles() {
        return mapCollectibles;
    }

    public Set<String> getDefeatedNPCs() {
        return defeatedNPCs;
    }

    public boolean hasSaveData(String username) {
        if (username == null)
            return false;
        return Pokedex.exists(username) || Inventario.exists(username)
                || Gdx.files.local(username + "_npcs.json").exists()
                || Gdx.files.local(username + "_player.json").exists();
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    public void saveProgress(Inventario inventory, String currentMap, float x, float y) {
        String user = UserManager.getCurrentUser();
        if (user == null)
            return;

        // Save Player Data
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);

        // Update local cache
        this.playerData = new PlayerData(currentMap, x, y);

        FileHandle filePlayer = Gdx.files.local(user + "_player.json");
        filePlayer.writeString(json.prettyPrint(this.playerData), false);

        // Save Pokedex
        Pokedex.save();

        // Save Inventory
        if (inventory != null) {
            inventory.save(user);
        }

        // Save NPCs
        FileHandle fileNpcs = Gdx.files.local(user + "_npcs.json");
        fileNpcs.writeString(json.prettyPrint(defeatedNPCs), false);

        Gdx.app.log("PROYECTO", "Progreso global guardado para el usuario: " + user + " en Mapa: " + currentMap);
    }

    // Deprecated override kept for compatibility if needed, but should be avoided
    public void saveProgress(Inventario inventory) {
        // Warning: This saves without position data!
        // Using default/null for map data if called legacy way
        saveProgress(inventory, "Mapa/MAPACOMPLETO.tmx", 0, 0);
    }

    public void loadProgress(String username) {
        if (username == null)
            return;
        UserManager.setCurrentUser(username);

        // Load Pokedex
        Pokedex.load();

        // Load NPCs
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

        // Load Player Data
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
    public void create() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(PANTALLA_W, PANTALLA_H, camera);
        batch = new SpriteBatch();
        fondoMenu = new Texture("imagenes/fondooo.png");
        setScreen(new BackgroundScreen(this));
    }
}
