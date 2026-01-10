package sodyl.proyecto.libGDX;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

//Clase que implementa la pantalla de fondo
public class BackgroundScreen implements Screen, InputProcessor {

    private final Proyecto game;

    private SpriteBatch batch;
    private Texture fondoMenu;
    private Viewport viewport;
    private OrthographicCamera camera;

    private Stage stage;
    private Texture backGroundTitle;
    private Image backGroundTitulo;
    private Texture startButtonTexture;
    private Image startButtonImage;

    private BitmapFont font;

    // Variables para la transición
    private boolean isTransitioning = false;
    private final float transitionDuration = 0.5f; // Duración del fade en segundos
    private float elapsedTime = 0f;

    public BackgroundScreen(Proyecto game) {
        this.game = game;
    }

    @Override
    public void show() { // configuración de la pantalla
        game.playMusic("musica/jumpUp.mp3");
        camera = new OrthographicCamera();
        viewport = new FitViewport(Proyecto.PANTALLA_W, Proyecto.PANTALLA_H, camera);
        batch = new SpriteBatch();
        fondoMenu = new Texture(Gdx.files.internal("imagenes/fondooo.png"));

        // CARGAR EL TÍTULO
        backGroundTitle = new Texture(Gdx.files.internal("imagenes/arceus_title.png"));
        backGroundTitulo = new Image(backGroundTitle);

        // CARGAR FUENTE
        font = new BitmapFont();

        // CARGAR EL BOTÓN "INICIAR JUEGO"
        startButtonTexture = new Texture(
                Gdx.files.internal("imagenes/Iniciar-Juego-presiona-un-bot-8-11-2025.gif"));
        startButtonImage = new Image(startButtonTexture);

        // CREAR EL STAGE (escenario donde se dibujan los elementos)
        stage = new Stage(viewport, batch);

        // CREAR LA TABLA (centro de la pantalla)
        Table table = new Table();
        table.setFillParent(true);

        // AÑADIR EL TÍTULO A LA TABLA
        table.add(backGroundTitulo).prefSize(400, 250).padBottom(60).row();

        // AÑADIR EL BOTÓN A LA TABLA
        table.add(startButtonImage).prefSize(300, 70).padBottom(50).row();

        // AÑADIR LA TABLA AL STAGE
        stage.addActor(table);
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) { // dibujar la pantalla
        // Limpiar con negro transparente (el fondo se dibuja a continuación)
        ScreenUtils.clear(0f, 0f, 0f, 1);
        viewport.apply();

        // DIBUJAR FONDO Y UI NORMAL
        if (!isTransitioning || elapsedTime / transitionDuration < 0.8) {
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            batch.draw(fondoMenu, 0, 0, Proyecto.PANTALLA_W, Proyecto.PANTALLA_H);
            batch.end();

            stage.act(delta);
            stage.draw();
        }

        // LÓGICA DE TRANSICIÓN DE FADE OUT
        if (isTransitioning) {
            elapsedTime += delta;

            // Calcular el alpha (de 0.0 a 1.0)
            float alpha = Math.min(1f, elapsedTime / transitionDuration);

            if (alpha >= 1.0f) {
                game.setScreen(new ScreenLogin(game));
                return;
            }

            // Habilitar blending para la transparencia
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            // Dibujar la capa negra de fade con el alpha calculado
            batch.setColor(0, 0, 0, alpha);
            // Usamos fondoMenu solo como una textura para dibujar el rectángulo que cubre
            // la pantalla
            batch.draw(fondoMenu, 0, 0, Proyecto.PANTALLA_W, Proyecto.PANTALLA_H);
            batch.end();

            // Restablecer el color del batch a blanco para el siguiente render
            batch.setColor(Color.WHITE);
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
    }

    @Override
    public void resize(int width, int height) { // ajustar la pantalla
        viewport.update(width, height, true);

        // RECALCULAR LA POSICIÓN DEL LABEL DE CRÉDITOS EN EL REDIMENSIONAMIENTO
        if (stage.getActors().size > 1 && stage.getActors().peek() instanceof Label) {
            Label creditsLabel = (Label) stage.getActors().peek();
            float margin = 10f;

            creditsLabel.setPosition(
                    viewport.getWorldWidth() - creditsLabel.getPrefWidth() - margin - 50,
                    margin);
        }
    }

    @Override
    public boolean keyDown(int keycode) { // keyCode = presionar cualquier tecla
        // Iniciar la transición al presionar cualquier tecla, si no está ya en progreso
        if (!isTransitioning) {
            Gdx.input.setInputProcessor(null);
            isTransitioning = true;
            elapsedTime = 0f;
        }
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Iniciar la transición al tocar la pantalla
        return keyDown(0);
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        batch.dispose();
        fondoMenu.dispose();
        stage.dispose();
        backGroundTitle.dispose();
        if (startButtonTexture != null)
            startButtonTexture.dispose();

        if (font != null)
            font.dispose();
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
