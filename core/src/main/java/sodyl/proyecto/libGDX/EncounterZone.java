package sodyl.proyecto.libGDX;

import com.badlogic.gdx.math.Rectangle;
import java.util.List;
import java.util.ArrayList;

/**
 * Representa un área invisible en el mapa donde pueden ocurrir encuentros con Pokémon.
 * Los Pokémon no son visibles, solo se activa la batalla al entrar en contacto.
 */
public class EncounterZone {
    private Rectangle bounds;
    private List<String> possiblePokemon; // Lista de nombres de Pokémon que pueden aparecer
    private float encounterRate; // Probabilidad de encuentro (0.0 a 1.0)
    private boolean isActive; // Si la zona está activa (puede generar encuentros)
    private String name; // Nombre identificador de la zona

    public EncounterZone(float x, float y, float width, float height, String name) {
        this.bounds = new Rectangle(x, y, width, height);
        this.possiblePokemon = new ArrayList<>();
        this.encounterRate = 0.3f; // 30% de probabilidad por defecto
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

    /**
     * Verifica si un punto (jugador) está dentro de esta zona de encuentro.
     */
    public boolean contains(float x, float y) {
        return bounds.contains(x, y);
    }

    /**
     * Verifica si un rectángulo (jugador) se superpone con esta zona.
     */
    public boolean overlaps(Rectangle playerBounds) {
        return bounds.overlaps(playerBounds);
    }
}

