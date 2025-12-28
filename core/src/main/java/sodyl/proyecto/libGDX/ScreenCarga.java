package sodyl.proyecto.libGDX;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.function.Supplier;

public class ScreenCarga implements Screen {
    private final Proyecto game;
    private final Supplier<Screen> nextScreenSupplier;
    private SpriteBatch batch;
    private Texture loadingTexture;
    private float timer = 0;
    private boolean isScreenLoaded = false;
    private static final float MIN_SHOW_TIME = 2.0f; // Mostrar al menos 2 segundos

    public ScreenCarga(Proyecto game, Supplier<Screen> nextScreenSupplier) {
        this.game = game;
        this.nextScreenSupplier = nextScreenSupplier;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        // Cargar la imagen de carga
        try {
            loadingTexture = new Texture(Gdx.files.internal("imagenes/pantallaCarga.png"));
        } catch (Exception e) {
            Gdx.app.error("LOADING", "Error al cargar la imagen de pantalla de carga", e);
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (batch != null && loadingTexture != null) {
            batch.begin();
            // Dibujar la imagen escalada a la pantalla completa
            batch.draw(loadingTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.end();
        }

        timer += delta;

        // Esperar el tiempo mínimo antes de cargar la siguiente pantalla
        if (timer >= MIN_SHOW_TIME && !isScreenLoaded) {
            isScreenLoaded = true;
            // Ejecutar el supplier para crear la siguiente pantalla (carga pesada)
            // Se usa postRunnable para asegurar que se ejecute en el hilo principal de
            // renderizado
            // aunque ya estamos en render, es buena práctica cuando se cambia de pantalla.
            Gdx.app.postRunnable(() -> {
                try {
                    Screen nextScreen = nextScreenSupplier.get();
                    game.setScreen(nextScreen);
                    // Importante: No llamamos a dispose() aquí directamente porque setScreen
                    // ocultará esta pantalla
                    // y el dispose se debería manejar externamente o al ocultarse, pero
                    // como Proyecto.setScreen llama a hide(), y luego mostramos la otra...
                    // LibGDX screens management can be tricky. Usually previous screen is hidden.
                    // We can dispose this screen immediately after setting the new one if we don't
                    // plan to reuse it.
                } catch (Exception e) {
                    Gdx.app.error("LOADING", "Error al crear la siguiente pantalla", e);
                }
            });
        }
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
            batch = null;
        }
        if (loadingTexture != null) {
            loadingTexture.dispose();
            loadingTexture = null;
        }
    }
}
