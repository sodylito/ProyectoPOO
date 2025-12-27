package sodyl.proyecto.libGDX;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
// Importación necesaria para el blending (transparencia)
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

public class BackgroundScreen implements Screen, InputProcessor {

    private final Proyecto game;

    private SpriteBatch batch;
    private Texture fondoMenu;
    private Viewport viewport;
    private OrthographicCamera camera;

    private Stage stage;
    // --- ELEMENTOS DE UI ---
    private Texture backGroundTitle;
    private Image backGroundTitulo;
    private Texture startButtonTexture;
    private Image startButtonImage;

    // VARIABLE DE FUENTE
    private BitmapFont font;

    // --- VARIABLES DE TRANSICIÓN ---
    private boolean isTransitioning = false;
    private final float transitionDuration = 0.5f; // Duración del fade en segundos
    private float elapsedTime = 0f;
    // ---------------------------------

    public BackgroundScreen(Proyecto game) {
        this.game = game;
    }

    @Override
    public void show() { // configurar la pantalla para cambiar de escena
        camera = new OrthographicCamera();
        viewport = new FitViewport(Proyecto.PANTALLA_W, Proyecto.PANTALLA_H, camera);
        batch = new SpriteBatch();
        fondoMenu = new Texture(Gdx.files.internal("imagenes/fondooo.png"));

        backGroundTitle = new Texture(Gdx.files.internal("imagenes/arceus_title.png"));
        backGroundTitulo = new Image(backGroundTitle);

        // --- CARGAR FUENTE ---
        font = new BitmapFont();

        // --- CARGAR EL BOTÓN IMAGEN ---
        try {
            startButtonTexture = new Texture(
                    Gdx.files.internal("imagenes/Iniciar-Juego-presiona-un-bot-8-11-2025.gif"));
            startButtonImage = new Image(startButtonTexture);
        } catch (Exception e) {
            Gdx.app.error("UI", "Error al cargar la textura del botón de inicio. Usando fallback.");
            startButtonTexture = new Texture(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGB565);
            startButtonImage = new Image(startButtonTexture);
        }

        stage = new Stage(viewport, batch);

        // --- TABLA PRINCIPAL (CENTRO) ---
        Table table = new Table();
        table.setFillParent(true);

        // TÍTULO
        table.add(backGroundTitulo).prefSize(400, 250).padBottom(60).row();

        // BOTÓN IMAGEN
        table.add(startButtonImage).prefSize(300, 70).padBottom(50).row();

        stage.addActor(table);

        // --- AÑADIR LABEL DE CRÉDITOS (Esquina Inferior Derecha Ajustada) ---
        Label.LabelStyle style = new Label.LabelStyle();
        style.font = font;
        style.fontColor = Color.WHITE;

        // Texto de los créditos
        String creditText = "Desarrollado por:\nSodyl Abreu\nNicolas Brito\nDubraska Rodriguez";
        Label creditsLabel = new Label(creditText, style);

        // Establecer posición en la esquina inferior derecha
        creditsLabel.setFontScale(0.8f); // Escala para que no sea demasiado grande
        creditsLabel.setAlignment(com.badlogic.gdx.utils.Align.bottomRight);

        float margin = 10f;

        // CÁLCULO DE POSICIÓN X: (Ancho total) - (Ancho del Label) - (Margen) -
        // (Desplazamiento a la izquierda)
        creditsLabel.setPosition(
                Proyecto.PANTALLA_W - creditsLabel.getPrefWidth() - margin - 50,
                margin);

        stage.addActor(creditsLabel);

        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) { // dibujar la pantalla
        // Limpiar con negro transparente (el fondo se dibuja a continuación)
        ScreenUtils.clear(0f, 0f, 0f, 1);
        viewport.apply();

        // 1. DIBUJAR FONDO Y UI NORMAL
        if (!isTransitioning || elapsedTime / transitionDuration < 0.8) { // Dibujar normal al inicio
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            batch.draw(fondoMenu, 0, 0, Proyecto.PANTALLA_W, Proyecto.PANTALLA_H);
            batch.end();

            stage.act(delta);
            stage.draw();
        }

        // 2. LÓGICA DE TRANSICIÓN DE FADE OUT
        if (isTransitioning) {
            elapsedTime += delta;

            // Calcular el alpha (de 0.0 a 1.0)
            float alpha = Math.min(1f, elapsedTime / transitionDuration);

            if (alpha >= 1.0f) {
                // Transición terminada, cambiar de pantalla
                // CAMBIO CLAVE: Usar el nuevo constructor para saltar el fade-in en
                // MenuPrincipal
                game.setScreen(new MenuPrincipal(game, true));
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

        // DISPONER LA FUENTE
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
