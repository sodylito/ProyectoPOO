package sodyl.proyecto.libGDX;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all
 * platforms.
 */
public class Proyecto extends Game {
    private SpriteBatch batch;
    private Texture fondoMenu;
    public static final float PANTALLA_W = 1000;
    public static final float PANTALLA_H = 720;
    private Viewport viewport;
    private OrthographicCamera camera;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(PANTALLA_W, PANTALLA_H, camera);
        batch = new SpriteBatch();
        fondoMenu = new Texture("imagenes/fondooo.png");
        setScreen(new BackgroundScreen(this));
    }
}
