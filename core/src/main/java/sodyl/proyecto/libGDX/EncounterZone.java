package sodyl.proyecto.libGDX;

import com.badlogic.gdx.math.Rectangle;
import java.util.List;
import java.util.ArrayList;

//Representa un área invisible en el mapa donde pueden ocurrir encuentros pokemón
public class EncounterZone {
    private Rectangle bounds;
    private List<String> possiblePokemon; // Lista de nombres de pokemón que pueden aparecer
    private float encounterRate;
    private boolean isActive;
    private String name;

    public EncounterZone(float x, float y, float width, float height, String name) {
        this.bounds = new Rectangle(x, y, width, height);
        this.possiblePokemon = new ArrayList<>();
        this.encounterRate = 0.3f; // Se establece un 30% de probabilidad por defecto, luego se cambia este valor
                                   // dependiendo del pokemón que aparezca
        this.isActive = true;
        this.name = name;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void addPokemon(String pokemonName) {
        if (!possiblePokemon.contains(pokemonName)) {
            possiblePokemon.add(pokemonName);
        }
    }

    public List<String> getPossiblePokemon() {
        return new ArrayList<>(possiblePokemon);
    }

    public void setEncounterRate(float rate) {
        this.encounterRate = Math.max(0.0f, Math.min(1.0f, rate));
    }

    public float getEncounterRate() {
        return encounterRate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public String getName() {
        return name;
    }

    // Verifica si el jugador está en un tile de aparición
    public boolean contains(float x, float y) {
        return bounds.contains(x, y);
    }

    public boolean overlaps(Rectangle playerBounds) {
        return bounds.overlaps(playerBounds);
    }
}
