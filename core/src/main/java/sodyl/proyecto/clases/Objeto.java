package sodyl.proyecto.clases;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

//Clase que define todos los objetos y recetas disponibles en el juego
public class Objeto {

    // Constantes a usar
    public enum Type {
        MATERIAL,
        POKEBALL,
        MEDICINA,
        OTRO
    }

    // Clase interna que define la receta de un objeto, es necesario porque la
    // receta
    // es un objeto que se relaciona con el objeto
    public static class Recipe {
        private final int itemId;
        private final int quantity;
        private final Map<Integer, Integer> ingredients;

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

    // CATÁLOGOS ESTÁTICOS
    private static final Map<Integer, Objeto> OBJETOS_CATALOG = new HashMap<>();
    private static final Map<Integer, Recipe> RECIPES_CATALOG = new HashMap<>();

    // PROPIEDADES
    private final int id;
    private final String nombre;
    private final Type tipo;
    private final String texturePath;
    private final String description;

    public Objeto(int id, String nombre, Type tipo, String texturePath, String description) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.texturePath = texturePath;
        this.description = description;
    }

    // GETTERS
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

    // MÉTODOS DE INICIALIZACIÓN

    public static void initializeObjetos() {
        // Limpiar catálogos
        OBJETOS_CATALOG.clear();
        RECIPES_CATALOG.clear();

        // Objetos (Materiales Primarios)
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

        // Pokebolas
        register(new Objeto(101, "Pokeball", Type.POKEBALL, "spritesObj/pokebS.png",
                "Un dispositivo para capturar y almacenar Pokémon."));
        // Medicinas
        register(new Objeto(103, "Poción", Type.MEDICINA, "spritesObj/pocionS.png", "Restaura 20 PS de un Pokémon."));
        register(new Objeto(104, "Superpoción", Type.MEDICINA, "spritesObj/superpS.png",
                "Restaura 50 PS de un Pokémon. Más efectiva que una Poción."));
        register(new Objeto(105, "Piedra Potenciadora", Type.OTRO, "spritesObj/piedraPotenciadora.png",
                "Aumenta un 50% el daño que hace el pokemón del jugador en una batalla."));
        register(new Objeto(106, "MasterBall", Type.POKEBALL, "spritesObj/masterBall.png",
                "Captura el 100% de las veces, siempre y cuando el pokemón rival esté a la mitad de vida o menos."));

        // Crafteo de Pokeball: 1 Bonguri + 1 Guijarro Rojo
        Map<Integer, Integer> pokeballIngredients = new HashMap<>();
        pokeballIngredients.put(1, 1);
        pokeballIngredients.put(2, 1);
        registerRecipe(new Recipe(101, 1, pokeballIngredients));

        // Crafteo de Poción: 1 Hierba éter
        Map<Integer, Integer> potionIngredients = new HashMap<>();
        potionIngredients.put(3, 1);
        registerRecipe(new Recipe(103, 1, potionIngredients));

        // Crafteo de Superpoción: 2 Hierba éter + 1 Baya Aranja
        Map<Integer, Integer> superpotionIngredients = new HashMap<>();
        superpotionIngredients.put(3, 2);
        superpotionIngredients.put(4, 1);
        registerRecipe(new Recipe(104, 1, superpotionIngredients));

        // Crafteo de Piedra Potenciadora: 2 Guijarro Rojo + 1 Hierba Regia + 2 Baya
        // Aranja
        Map<Integer, Integer> boostIngredients = new HashMap<>();
        boostIngredients.put(2, 2);
        boostIngredients.put(5, 1);
        boostIngredients.put(4, 2);
        registerRecipe(new Recipe(105, 1, boostIngredients));

        // Crafteo de MasterBall: 2 Guijarro Rojo + 2 Hierba Regia + 1 Pokeball
        Map<Integer, Integer> masterballIngredients = new HashMap<>();
        masterballIngredients.put(2, 2);
        masterballIngredients.put(5, 2);
        masterballIngredients.put(101, 1);
        registerRecipe(new Recipe(106, 1, masterballIngredients));
    }

    private static void register(Objeto objeto) {
        OBJETOS_CATALOG.put(objeto.getId(), objeto);
    }

    private static void registerRecipe(Recipe recipe) {
        RECIPES_CATALOG.put(recipe.getItemId(), recipe);
    }

    // MÉTODOS DE BÚSQUEDA

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
