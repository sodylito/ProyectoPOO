package sodyl.proyecto.clases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

/**
 * Simple Pokedex manager: registra Pokémon vistos/desbloqueados por el jugador.
 * Implementación mínima para soportar añadir desde la pantalla de selección.
 */
public class Pokedex {
    private static final Set<String> seen = new HashSet<>();
    private static final List<Pokemon> collected = new ArrayList<>();
    private static final Map<String, Integer> researchLevels = new HashMap<>();

    // Helper to get the save file path for the current user
    private static String getSaveFileName() {
        String user = UserManager.getCurrentUser();
        if (user == null)
            return "guest_pokedex.json";
        return user + "_pokedex.json";
    }

    private static boolean tutorialCompleted = false;

    // Team Management
    private static final List<Pokemon> team = new ArrayList<>();
    public static final int MAX_TEAM_SIZE = 3;

    public static void addSeen(String especie) {
        if (especie == null)
            return;
        seen.add(especie);
        save();
    }

    public static boolean hasSeen(String especie) {
        return especie != null && seen.contains(especie);
    }

    public static Set<String> getSeen() {
        return Collections.unmodifiableSet(seen);
    }

    public static void addCollected(Pokemon pokemon) {
        if (pokemon == null)
            return;

        // Check for duplicate species
        boolean alreadyHasSpecies = false;
        Pokemon existingPokemon = null;
        for (Pokemon p : collected) {
            if (p.getEspecie().equals(pokemon.getEspecie())) {
                alreadyHasSpecies = true;
                existingPokemon = p;
                break;
            }
        }

        if (alreadyHasSpecies) {
            // Subsequent capture: add +2 to current level (max 10)
            addResearchPoints(pokemon.getEspecie(), 2);
            Gdx.app.log("POKEDEX", "Duplicate species caught: " + pokemon.getEspecie() + ". Research level increased.");
            return; // Do not add to collection
        }

        // First capture: set level to 2
        pokemon.setNivel(2);
        pokemon.actualizarAtributos();
        researchLevels.put(pokemon.getEspecie(), 2);

        collected.add(pokemon);
        addSeen(pokemon.getEspecie());

        // Auto-add to team if size <= 3
        if (collected.size() <= MAX_TEAM_SIZE) {
            addToTeam(pokemon);
        }

        save();
    }

    public static List<Pokemon> getCollected() {
        return Collections.unmodifiableList(collected);
    }

    public static void addResearchPoints(String especie, int points) {
        if (especie == null)
            return;
        int current = researchLevels.getOrDefault(especie, 0);
        if (current >= 10)
            return;

        int nuevoNivel = Math.min(10, current + points);
        researchLevels.put(especie, nuevoNivel);
        addSeen(especie);

        for (Pokemon p : collected) {
            if (p.getEspecie().equals(especie)) {
                p.setNivel(nuevoNivel);
                p.actualizarAtributos();
            }
        }
        save();
    }

    public static int getResearchLevel(String especie) {
        return researchLevels.getOrDefault(especie, 0);
    }

    public static Map<String, Integer> getResearchLevels() {
        return Collections.unmodifiableMap(researchLevels);
    }

    public static void clear() {
        seen.clear();
        collected.clear();
        researchLevels.clear();
        save();
    }

    public static boolean addToTeam(Pokemon pokemon) {
        if (pokemon == null)
            return false;
        if (team.size() >= MAX_TEAM_SIZE)
            return false;
        if (isInTeam(pokemon))
            return false;

        team.add(pokemon);
        save();
        return true;
    }

    public static boolean removeFromTeam(Pokemon pokemon) {
        if (pokemon == null)
            return false;
        boolean removed = team.remove(pokemon);
        if (removed)
            save();
        return removed;
    }

    public static boolean isInTeam(Pokemon pokemon) {
        return team.contains(pokemon);
    }

    public static boolean isInTeam(String species) {
        for (Pokemon p : team) {
            if (p.getEspecie().equals(species))
                return true;
        }
        return false;
    }

    public static List<Pokemon> getTeam() {
        if (collected.size() <= MAX_TEAM_SIZE) {
            return Collections.unmodifiableList(collected);
        }
        return Collections.unmodifiableList(team);
    }

    public static void setTeam(List<Pokemon> newTeam) {
        team.clear();
        if (newTeam != null) {
            for (Pokemon p : newTeam) {
                if (team.size() < MAX_TEAM_SIZE) {
                    team.add(p);
                }
            }
        }
        save();
    }

    public static void save() {
        // PERMANENCE DISABLED BY USER
        // Json json = new Json();
        // json.setOutputType(JsonWriter.OutputType.json);
        // PokedexData data = new PokedexData();
        // data.seen = seen;
        // data.collected = collected;
        // data.researchLevels = researchLevels;
        // data.team = team;
        // data.tutorialCompleted = tutorialCompleted;

        // FileHandle file = Gdx.files.local(getSaveFileName());
        // file.writeString(json.toJson(data), false);
        Gdx.app.log("POKEDEX", "Save disabled (Session only)");
    }

    public static void load() {
        // PERMANENCE DISABLED BY USER
        // FileHandle file = Gdx.files.local(getSaveFileName());
        // if (!file.exists())
        // return;

        // try {
        // Json json = new Json();
        // PokedexData data = json.fromJson(PokedexData.class, file.readString());
        // if (data != null) {
        // seen.clear();
        // if (data.seen != null)
        // seen.addAll(data.seen);

        // collected.clear();
        // if (data.collected != null)
        // collected.addAll(data.collected);

        // researchLevels.clear();
        // if (data.researchLevels != null)
        // researchLevels.putAll(data.researchLevels);

        // team.clear();
        // if (data.team != null)
        // team.addAll(data.team);

        // tutorialCompleted = data.tutorialCompleted;
        // }
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        Gdx.app.log("POKEDEX", "Load disabled (Starting fresh session)");
    }

    public static boolean isTutorialCompleted() {
        return tutorialCompleted;
    }

    public static void setTutorialCompleted(boolean completed) {
        tutorialCompleted = completed;
        save();
    }

    public static class PokedexData {
        public Set<String> seen;
        public List<Pokemon> collected;
        public Map<String, Integer> researchLevels;
        public List<Pokemon> team;
        public boolean tutorialCompleted;
    }
}
