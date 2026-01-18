package sodyl.proyecto.libGDX;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Disposable;

// Clase que representa un objeto recolectable en el mapa
public class Collectible implements Disposable {

    private int itemId;
    private int quantity;
    private float x;
    private float y;
    private int networkId = -1;
    private boolean isCollected;
    private long collectionTime = 0;
    private Image actor;
    private Texture texture;
    private static final float INTERACTION_RANGE = 2.0f;

    public Collectible(float x, float y, int itemId, int quantity, String texturePath) {
        this(x, y, itemId, quantity, texturePath, -1);
    }

    // Constructor para crear objetos que se pueden recolectar en el mapa
    public Collectible(float x, float y, int itemId, int quantity, String texturePath, int networkId) {
        this.x = x;
        this.y = y;
        this.itemId = itemId;
        this.quantity = quantity;
        this.networkId = networkId;
        this.isCollected = false;

        this.texture = new Texture(Gdx.files.internal(texturePath));
        this.actor = new Image(this.texture);
        this.actor.setSize(1.5f, 1.0f);
        this.actor.setPosition(x - this.actor.getWidth() / 2, y - this.actor.getHeight() / 2);
    }

    public int getItemId() {
        return itemId;
    }

    public boolean isCollected() {
        return isCollected;
    }

    // Método que marca el objeto como recolectado y lo quita del stage (mapa)
    public void setCollected(boolean isCollected) {
        this.isCollected = isCollected;
        if (isCollected) {
            this.collectionTime = System.currentTimeMillis();
            this.actor.remove();
        } else {
            this.collectionTime = 0;
        }
    }

    public long getCollectionTime() {
        return collectionTime;
    }

    public int getQuantity() {
        return quantity;
    }

    public Actor getActor() {
        return actor;
    }

    public int getNetworkId() {
        return networkId;
    }

    // Método que valida si el usuario está en rango de tomar el objeto
    public boolean isInRange(float playerCenterX, float playerCenterY) {
        float dx = playerCenterX - x;
        float dy = playerCenterY - y;
        return (dx * dx + dy * dy) <= (INTERACTION_RANGE * INTERACTION_RANGE);
    }

    public int collect() {
        if (!isCollected) {
            return itemId;
        }
        return -1;
    }

    @Override
    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}
