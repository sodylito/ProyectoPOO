package sodyl.proyecto.clases;

//Clase que almacena los datos del jugador
public class PlayerData {
    public String currentMap;
    public float x;
    public float y;
    public long lastPlayed;

    public PlayerData() {
    }

    public PlayerData(String currentMap, float x, float y) {
        this.currentMap = currentMap;
        this.x = x;
        this.y = y;
        this.lastPlayed = System.currentTimeMillis();
    }
}
