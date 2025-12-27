package sodyl.proyecto.libGDX;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class ScreenAyuda implements Screen, InputProcessor {

    private final Proyecto game;
    private Stage stage;
    private Texture backgroundTexture;
    private BitmapFont fontTitle;
    private BitmapFont fontText;

    public ScreenAyuda(Proyecto game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(Proyecto.PANTALLA_W, Proyecto.PANTALLA_H));

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(this);
        Gdx.input.setInputProcessor(multiplexer);

        // Background
        backgroundTexture = new Texture(Gdx.files.internal("imagenes/fondoAyuda.png"));
        Image bgImage = new Image(backgroundTexture);
        bgImage.setFillParent(true);
        stage.addActor(bgImage);

        // Fonts
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Mapa/ari-w9500-bold.ttf"));

        FreeTypeFontParameter titleParam = new FreeTypeFontParameter();
        titleParam.size = 40;
        titleParam.color = Color.YELLOW;
        titleParam.borderColor = Color.BLACK;
        titleParam.borderWidth = 2;
        fontTitle = generator.generateFont(titleParam);

        FreeTypeFontParameter textParam = new FreeTypeFontParameter();
        textParam.size = 24;
        textParam.color = Color.WHITE; // Blanco base para poder teñir
        textParam.shadowColor = Color.GRAY;
        textParam.shadowOffsetX = 1;
        textParam.shadowOffsetY = 1;
        fontText = generator.generateFont(textParam);

        generator.dispose();

        // Styles
        // Styles
        LabelStyle titleStyle = new LabelStyle(fontTitle, Color.YELLOW);
        LabelStyle headerStyle = new LabelStyle(fontText, Color.ORANGE);
        LabelStyle textStyle = new LabelStyle(fontText, new Color(0.1f, 0.1f, 0.4f, 1f)); // Azul Oscuro

        TextButtonStyle btnStyle = new TextButtonStyle();
        btnStyle.font = fontText;
        // Modificado para que siempre parezca "presionado" o seleccionado (Amarillo)
        btnStyle.fontColor = Color.YELLOW; // Botón "Volver" sigue siendo amarillo para destacar
        btnStyle.downFontColor = Color.ORANGE;
        btnStyle.overFontColor = Color.YELLOW;

        // Content Table
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.pad(20);

        // Title
        Label titleLabel = new Label("AYUDA Y REGLAS", titleStyle);
        mainTable.add(titleLabel).padBottom(20).row();

        // Content Table for ScrollPane
        Table contentTable = new Table();
        contentTable.top().left();

        // CONTROLES
        Label controlesHeader = new Label("CONTROLES:", headerStyle);
        contentTable.add(controlesHeader).padBottom(10).left().row();

        String controlesText = "- Moverse: Flechas de Dirección (Arriba, Abajo, Izquierda, Derecha)\n" +
                "- Interactuar / Confirmar: Z o ENTER\n" +
                "- Cancelar / Menú: X o ESCAPE";
        Label controlesLabel = new Label(controlesText, textStyle);
        contentTable.add(controlesLabel).padBottom(20).left().row();

        // OBJETIVO
        Label objetivoHeader = new Label("OBJETIVO:", headerStyle);
        contentTable.add(objetivoHeader).padBottom(10).left().row();

        String objetivoText = "- Explora el mundo y encuentra Pokémon salvajes.\n" +
                "- Lucha contra ellos para debilitarlos y capturarlos.\n" +
                "- ¡Ten cuidado! Si tus Pokémon se debilitan, perderás objetos.\n" +
                "- Tu misión final es encontrar y desafiar al legendario Arceus.";
        Label objetivoLabel = new Label(objetivoText, textStyle);
        objetivoLabel.setWrap(true);
        objetivoLabel.setAlignment(Align.topLeft);
        contentTable.add(objetivoLabel).width(Proyecto.PANTALLA_W * 0.75f).padBottom(20).left().row();

        // BATALLA
        Label batallaHeader = new Label("BATALLA:", headerStyle);
        contentTable.add(batallaHeader).padBottom(10).left().row();

        String batallaText = "- Cada tipo de Pokémon tiene sus propias fortalezas y debilidades.\n" +
                "- ¡Captura tantos como puedas para completar tu investigación!";
        Label batallaLabel = new Label(batallaText, textStyle);
        batallaLabel.setWrap(true);
        batallaLabel.setAlignment(Align.topLeft);
        contentTable.add(batallaLabel).width(Proyecto.PANTALLA_W * 0.75f).padBottom(20).left().row();

        ScrollPane scrollPane = new ScrollPane(contentTable);
        mainTable.add(scrollPane).grow().width(Proyecto.PANTALLA_W * 0.8f).padBottom(20).row();

        // Back Button
        TextButton backButton = new TextButton("VOLVER", btnStyle);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                goBack();
            }
        });
        mainTable.add(backButton).height(50).row();

        stage.addActor(mainTable);
    }

    private void goBack() {
        game.setScreen(new MenuPrincipal(game, true, MenuPrincipal.MenuState.OPTIONS_SUB)); // Skip fade, go to Options
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Keys.ESCAPE || keycode == Keys.ENTER || keycode == Keys.Z || keycode == Keys.X) {
            goBack();
            return true;
        }
        return false;
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
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
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

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
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
        if (stage != null)
            stage.dispose();
        if (backgroundTexture != null)
            backgroundTexture.dispose();
        if (fontTitle != null)
            fontTitle.dispose();
        if (fontText != null)
            fontText.dispose();
    }
}
