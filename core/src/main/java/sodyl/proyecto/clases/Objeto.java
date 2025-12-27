package sodyl.proyecto.clases;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Clase estática que define todos los objetos y recetas disponibles en el
 * juego.
 */
public class Objeto {

    // --- ENUMS ---
    public enum Type {
        MATERIAL, // Material base para craftear (ej. Bonguri)
        POKEBALL, // Objeto de captura (ej. Pokeball)
        MEDICINA, // Objeto de curación
        OTRO
    }

    // --- CLASE ANIDADA PARA RECETAS ---
    public static class Recipe {
        private final int itemId; // ID del objeto resultante
        private final int quantity; // Cantidad resultante
        private final Map<Integer, Integer> ingredients; // Ingredientes (ID -> Cantidad)

        public Recipe(int itemId, int quantity, Map<Integer, Integer> ingredients) {
            this.itemId = itemId;
            this.quantity = quantity;
            this.ingredients = Collections.unmodifiableMap(ingredients);
        }

        public int getItemId() {
            return itemId;
        }

        public int getQuantity() {
            return quantity;
        }

        public Map<Integer, Integer> getIngredients() {
            return ingredients;
        }
    }

    // --- CATÁLOGOS ESTÁTICOS ---
    private static final Map<Integer, Objeto> OBJETOS_CATALOG = new HashMap<>();
    private static final Map<Integer, Recipe> RECIPES_CATALOG = new HashMap<>();

    // --- PROPIEDADES DE INSTANCIA ---
    private final int id;
    private final String nombre;
    private final Type tipo;
    private final String texturePath; // Ruta de la textura para Collectible
    private final String description; // Descripción

    public Objeto(int id, String nombre, Type tipo, String texturePath, String description) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.texturePath = texturePath;
        this.description = description;
    }

    // --- GETTERS DE INSTANCIA ---
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Type getTipo() {
        return tipo;
    }

    public String getTexturePath() {
        return texturePath;
    }

    public String getDescription() {
        return description;
    }

    // --- MÉTODOS ESTÁTICOS DE INICIALIZACIÓN ---

    public static void initializeObjetos() {
        // Limpiar catálogos
        OBJETOS_CATALOG.clear();
        RECIPES_CATALOG.clear();

        // 1. Definición de Objetos (Materiales 1-100)
        register(new Objeto(1, "Bonguri", Type.MATERIAL, "spritesObj/bongS.png",
                "Una fruta dura utilizada para craftear Poké Balls."));
        register(new Objeto(2, "Guijarro Rojo", Type.MATERIAL, "spritesObj/guijRojoS.png",
                "Una pequeña piedra con tintes rojizos, necesaria para crear Poké Balls."));
        register(new Objeto(3, "Hierba éter", Type.MATERIAL, "spritesObj/hierbEtS.png",
                "Hierba rara con propiedades curativas. Base para pociones."));
        register(new Objeto(4, "Baya Aranja", Type.MATERIAL, "spritesObj/bayArS.png",
                "Una baya que restaura un poco de PS a un Pokémon."));
        register(new Objeto(5, "Hierba Regia", Type.MATERIAL, "spritesObj/hierbRegS.png",
                "Una hierba mágica utilizada para revivir y restaurar estados."));

        // POKEBALLS
        register(new Objeto(101, "Pokeball", Type.POKEBALL, "spritesObj/pokebS.png",
                "Un dispositivo para capturar y almacenar Pokémon."));
        // MEDICINAS
        register(new Objeto(103, "Poción", Type.MEDICINA, "spritesObj/pocionS.png", "Restaura 20 PS de un Pokémon."));
        register(new Objeto(104, "Superpoción", Type.MEDICINA, "spritesObj/superpS.png",
                "Restaura 50 PS de un Pokémon. Más efectiva que una Poción."));
        register(new Objeto(105, "Revivir", Type.MEDICINA, "spritesObj/revS.png",
                "Revive a un Pokémon con la mitad de sus PS máximos."));
        register(new Objeto(106, "Revivir Máximo", Type.MEDICINA, "spritesObj/revMaxS.png",
                "Revive a un Pokémon restaurando todos sus PS al máximo."));

        // 3. Definición de Recetas

        // Receta de Pokeball: 1 Bonguri (1) + 1 Guijarro Rojo (2)
        Map<Integer, Integer> pokeballIngredients = new HashMap<>();
        pokeballIngredients.put(1, 1);
        pokeballIngredients.put(2, 1);
        registerRecipe(new Recipe(101, 1, pokeballIngredients));

        // Receta de Poción: 1 Hierba éter (3)
        Map<Integer, Integer> potionIngredients = new HashMap<>();
        potionIngredients.put(3, 1);
        registerRecipe(new Recipe(103, 1, potionIngredients));

        // Receta de Superpoción: 2 Hierba éter (3) + 1 Baya Aranja (4)
        Map<Integer, Integer> superpotionIngredients = new HashMap<>();
        superpotionIngredients.put(3, 2);
        superpotionIngredients.put(4, 1);
        registerRecipe(new Recipe(104, 1, superpotionIngredients));

        // Receta de Revivir: 1 Hierba Regia (5) + 1 Guijarro Rojo (2)
        Map<Integer, Integer> reviveIngredients = new HashMap<>();
        reviveIngredients.put(5, 1);
        reviveIngredients.put(2, 1);
        registerRecipe(new Recipe(105, 1, reviveIngredients));

        // Receta de Revivir Máximo: 3 Hierba Regia (5) + 2 Guijarro Rojo (2)
        Map<Integer, Integer> maxReviveIngredients = new HashMap<>();
        maxReviveIngredients.put(5, 3);
        maxReviveIngredients.put(2, 2);
        registerRecipe(new Recipe(106, 1, maxReviveIngredients));
    }

    private static void register(Objeto objeto) {
        OBJETOS_CATALOG.put(objeto.getId(), objeto);
    }

    private static void registerRecipe(Recipe recipe) {
        RECIPES_CATALOG.put(recipe.getItemId(), recipe);
    }

    // --- MÉTODOS ESTÁTICOS DE BÚSQUEDA ---

    public static Objeto getObjeto(int id) {
        return OBJETOS_CATALOG.get(id);
    }

    public static Objeto getObjetoByName(String name) {
        for (Objeto obj : OBJETOS_CATALOG.values()) {
            if (obj.getNombre().equalsIgnoreCase(name)) {
                return obj;
            }
        }
        return null;
    }

    public static Map<Integer, Recipe> getAllRecipes() {
        return Collections.unmodifiableMap(RECIPES_CATALOG);
    }

    public static Map<Integer, Objeto> getAllObjects() {
        return Collections.unmodifiableMap(OBJETOS_CATALOG);
    }
}
