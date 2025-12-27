package sodyl.proyecto.libGDX;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Disposable;

/**
 * Representa un objeto interactivo en el mundo del juego que el jugador puede
 * recolectar.
 */
public class Collectible implements Disposable {

    private int itemId;
    private int quantity;
    private float x;
    private float y;
    private boolean isCollected;
    private Image actor;
    private Texture texture;
    private static final float INTERACTION_RANGE = 2.0f; // Radio de interacción

    /**
     * Constructor para crear un objeto recolectable.
     * 
     * @param x           Posición X en el mapa.
     * @param y           Posición Y en el mapa.
     * @param itemId      ID del objeto según el catálogo de Objeto.java.
     * @param quantity    Cantidad de este objeto.
     * @param texturePath Ruta de la textura del icono.
     */
    public Collectible(float x, float y, int itemId, int quantity, String texturePath) {
        this.x = x;
        this.y = y;
        this.itemId = itemId;
        this.quantity = quantity;
        this.isCollected = false;

        this.texture = new Texture(Gdx.files.internal(texturePath));

        // El actor es el objeto visual en el Stage
        this.actor = new Image(this.texture);
        // Ajustamos la posición para centrar el actor en las coordenadas (x, y) del
        // mapa
        this.actor.setSize(1.5f, 1.0f); // Ancho aumentado, alto normal
        this.actor.setPosition(x - this.actor.getWidth() / 2, y - this.actor.getHeight() / 2);
    }

    // --- Métodos de Estado y Lógica ---

    public int getItemId() {
        return itemId;
    }

    public boolean isCollected() {
        return isCollected;
    }

    /**
     * ¡Este es el método que faltaba! Marca el objeto como recolectado y lo remueve
     * del stage.
     * 
     * @param isCollected Estado de recolección.
     */
    public void setCollected(boolean isCollected) {
        this.isCollected = isCollected;
        if (isCollected) {
            this.actor.remove(); // Quitar del escenario
        }
    }

    public int getQuantity() {
        return quantity;
    }

    public Actor getActor() {
        return actor;
    }

    /**
     * Comprueba si la posición central del jugador está dentro del rango de
     * interacción.
     * 
     * @param playerCenterX Centro X del jugador.
     * @param playerCenterY Centro Y del jugador.
     * @return True si está dentro del rango.
     */
    public boolean isInRange(float playerCenterX, float playerCenterY) {
        float dx = playerCenterX - x;
        float dy = playerCenterY - y;
        // Distancia euclidiana (distancia al cuadrado para evitar la raíz cuadrada)
        return (dx * dx + dy * dy) <= (INTERACTION_RANGE * INTERACTION_RANGE);
    }

    /**
     * Procesa la recolección del objeto.
     * 
     * @return El ID del objeto recolectado.
     */
    public int collect() {
        if (!isCollected) {
            // El ScreenMapaTiled es responsable de llamar a setCollected(true) si la
            // adición
            // al inventario fue exitosa (parcial o totalmente).
            return itemId;
        }
        return -1; // Ya recolectado
    }

    @Override
    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
        // El actor se limpia cuando se hace remove() o se elimina el Stage.
    }
}
