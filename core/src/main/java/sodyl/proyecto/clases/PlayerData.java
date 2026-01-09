package sodyl.proyecto.clases;

/**
 * Clase simple para serializar los datos del jugador (posición y mapa).
 */
public class PlayerData {
    public String currentMap;
    public float x;
    public float y;
    public long lastPlayed;

    // Default constructor for JSON
    public PlayerData() {
    }

    public PlayerData(String currentMap, float x, float y) {
        this.currentMap = currentMap;
        this.x = x;
        this.y = y;
        this.lastPlayed = System.currentTimeMillis();
    }
}
