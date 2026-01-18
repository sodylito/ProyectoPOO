package sodyl.proyecto.clases;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import sodyl.proyecto.clases.Objeto.Recipe;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

// Clase que gestiona los objetos que posee el jugador
public class Inventario {

    // Límite máximo de unidades que se pueden tener de CADA objeto
    public static final int MAX_QUANTITY = 99;
    private final Map<Integer, Integer> objetos;

    public Inventario() {
        this.objetos = new HashMap<>();
    }

    // Método para verificar si existe un inventario para un usuario
    public static boolean exists(String username) {
        if (username == null)
            return false;
        return Gdx.files.local(username + "_inventory.json").exists();
    }

    public void clear() {
        objetos.clear();
    }

    /**
     * Añade una cantidad de un objeto al inventario, respetando el límite máximo.
     * 
     * id es la ID del objeto a añadir.
     * cantidad es la Cantidad a añadir.
     * 
     */
    public int addObjeto(int id, int cantidad) {
        int currentQuantity = objetos.getOrDefault(id, 0);
        int newQuantity = currentQuantity + cantidad;

        if (newQuantity > MAX_QUANTITY) {
            int overflow = newQuantity - MAX_QUANTITY;
            objetos.put(id, MAX_QUANTITY);
            return overflow; // Devuelve la cantidad que no cabe, puede pasar que quiera añadir 3 objetos y 2
                             // e desborden
        } else {
            objetos.put(id, newQuantity);
            return 0; // Se añadió todo sin desbordamiento
        }
    }

    // Método para eliminar una cantidad de un objeto del inventario
    public boolean removeObjeto(int id, int cantidad) {
        int currentQuantity = objetos.getOrDefault(id, 0);
        if (currentQuantity >= cantidad) {
            int newQuantity = currentQuantity - cantidad;
            if (newQuantity <= 0) {
                objetos.remove(id);
            } else {
                objetos.put(id, newQuantity);
            }
            return true;
        }
        return false;
    }

    public boolean craftItem(Recipe recipe) {
        for (Map.Entry<Integer, Integer> entry : recipe.getIngredients().entrySet()) {
            int ingredientId = entry.getKey();
            int requiredQuantity = entry.getValue();
            if (getQuantity(ingredientId) < requiredQuantity) {
                return false; // No tiene suficiente de este ingrediente
            }
        }

        int resultId = recipe.getItemId();
        int resultQuantity = recipe.getQuantity();
        int currentHeld = getQuantity(resultId);

        if (currentHeld + resultQuantity > MAX_QUANTITY) {
            return false; // No hay espacio para el resultado (superaría el límite de 99)
        }

        // Eliminar ingredientes
        for (Map.Entry<Integer, Integer> entry : recipe.getIngredients().entrySet()) {
            removeObjeto(entry.getKey(), entry.getValue());
        }

        // Añadir el objeto crafteado
        addObjeto(resultId, resultQuantity);

        return true;
    }

    public int getQuantity(int id) {
        return objetos.getOrDefault(id, 0);
    }

    public Map<Integer, Integer> getAllObjetos() {
        return Collections.unmodifiableMap(objetos);
    }

    // Guarda el inventario en un archivo JSON
    public void save(String username) {
        if (username == null)
            return;
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        FileHandle file = Gdx.files.local(username + "_inventory.json");
        file.writeString(json.toJson(objetos), false);
    }

    // Carga el inventario desde un archivo JSON
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void load(String username) {
        if (username == null)
            return;
        FileHandle file = Gdx.files.local(username + "_inventory.json");
        if (file.exists()) {
            try {
                Json json = new Json();
                // We load as a raw Map first because LibGDX JSON might load keys as Strings
                Map loadedRaw = json.fromJson(HashMap.class, file.readString());
                if (loadedRaw != null) {
                    objetos.clear();
                    for (Object key : loadedRaw.keySet()) {
                        try {
                            Integer id;
                            if (key instanceof String) {
                                id = Integer.parseInt((String) key);
                            } else if (key instanceof Integer) {
                                id = (Integer) key;
                            } else if (key instanceof Float) {
                                id = ((Float) key).intValue();
                            } else {
                                continue;
                            }

                            Object val = loadedRaw.get(key);
                            Integer quantity;
                            if (val instanceof Integer) {
                                quantity = (Integer) val;
                            } else if (val instanceof Float) {
                                quantity = ((Float) val).intValue();
                            } else if (val instanceof String) {
                                quantity = Integer.parseInt((String) val);
                            } else {
                                continue;
                            }

                            objetos.put(id, quantity);
                        } catch (Exception e) {
                            Gdx.app.error("INVENTARIO", "Error parsing item: " + key, e);
                        }
                    }
                    Gdx.app.log("INVENTARIO",
                            "Inventory loaded for " + username + ": " + objetos.size() + " types of items.");
                }
            } catch (Exception e) {
                Gdx.app.error("INVENTARIO", "Error loading inventory", e);
            }
        }
    }
}
