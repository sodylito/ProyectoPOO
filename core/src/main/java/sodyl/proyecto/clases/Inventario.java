package sodyl.proyecto.clases;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import sodyl.proyecto.clases.Objeto.Recipe;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

/**
 * Clase que gestiona los objetos que posee el jugador.
 */
public class Inventario {

    // Límite máximo de unidades que se pueden tener de CADA objeto.
    public static final int MAX_QUANTITY = 99;

    private final Map<Integer, Integer> objetos; // ID del Objeto -> Cantidad

    public Inventario() {
        this.objetos = new HashMap<>();
    }

    /**
     * Añade una cantidad de un objeto al inventario, respetando el límite máximo.
     * 
     * @param id       ID del objeto a añadir.
     * @param quantity Cantidad a añadir.
     * @return La cantidad que NO se pudo añadir (desbordamiento). 0 si se añadió
     *         todo.
     */
    public int addObjeto(int id, int quantity) {
        int currentQuantity = objetos.getOrDefault(id, 0);
        int newQuantity = currentQuantity + quantity;

        if (newQuantity > MAX_QUANTITY) {
            int overflow = newQuantity - MAX_QUANTITY;
            objetos.put(id, MAX_QUANTITY);
            return overflow; // Devuelve la cantidad que no cabe
        } else {
            objetos.put(id, newQuantity);
            return 0; // Se añadió todo sin desbordamiento
        }
    }

    /**
     * Elimina una cantidad de un objeto del inventario.
     * 
     * @param id       ID del objeto a eliminar.
     * @param quantity Cantidad a eliminar.
     * @return true si la eliminación fue exitosa (tenía suficiente cantidad), false
     *         en caso contrario.
     */
    public boolean removeObjeto(int id, int quantity) {
        int currentQuantity = objetos.getOrDefault(id, 0);
        if (currentQuantity >= quantity) {
            int newQuantity = currentQuantity - quantity;
            if (newQuantity <= 0) {
                objetos.remove(id);
            } else {
                objetos.put(id, newQuantity);
            }
            return true;
        }
        return false;
    }

    /**
     * Intenta craftear un objeto usando una receta.
     * 
     * @param recipe La receta a seguir.
     * @return true si el crafteo fue exitoso, false si no hay suficientes
     *         ingredientes o si el resultado supera el límite.
     */
    public boolean craftItem(Recipe recipe) {
        // 1. Verificar si tiene todos los ingredientes
        for (Map.Entry<Integer, Integer> entry : recipe.getIngredients().entrySet()) {
            int ingredientId = entry.getKey();
            int requiredQuantity = entry.getValue();
            if (getQuantity(ingredientId) < requiredQuantity) {
                return false; // No tiene suficiente de este ingrediente
            }
        }

        // 2. Verificar si el objeto resultante tiene espacio (si no supera el
        // MAX_QUANTITY)
        int resultId = recipe.getItemId();
        int resultQuantity = recipe.getQuantity();
        int currentHeld = getQuantity(resultId);

        if (currentHeld + resultQuantity > MAX_QUANTITY) {
            return false; // No hay espacio para el resultado (superaría el límite de 99)
        }

        // 3. Ejecutar la transacción (eliminar ingredientes y añadir el resultado)

        // Eliminar ingredientes
        for (Map.Entry<Integer, Integer> entry : recipe.getIngredients().entrySet()) {
            removeObjeto(entry.getKey(), entry.getValue());
        }

        // Añadir el objeto crafteado
        // Como ya verificamos que no supera el límite, simplemente lo añadimos.
        addObjeto(resultId, resultQuantity);

        return true;
    }

    public int getQuantity(int id) {
        return objetos.getOrDefault(id, 0);
    }

    public Map<Integer, Integer> getAllObjetos() {
        return Collections.unmodifiableMap(objetos);
    }

    public void save(String username) {
        if (username == null)
            return;
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        FileHandle file = Gdx.files.local(username + "_inventory.json");
        file.writeString(json.toJson(objetos), false);
    }

    @SuppressWarnings("unchecked")
    public void load(String username) {
        if (username == null)
            return;
        FileHandle file = Gdx.files.local(username + "_inventory.json");
        if (file.exists()) {
            try {
                Json json = new Json();
                Map<Integer, Integer> loaded = json.fromJson(HashMap.class, file.readString());
                if (loaded != null) {
                    objetos.clear();
                    objetos.putAll(loaded);
                }
            } catch (Exception e) {
                Gdx.app.error("INVENTARIO", "Error loading inventory", e);
            }
        }
    }
}
