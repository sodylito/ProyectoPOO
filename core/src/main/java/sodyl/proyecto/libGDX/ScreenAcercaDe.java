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
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class ScreenAcercaDe implements Screen, InputProcessor {

    private final Proyecto game;
    private Stage stage;
    private Texture backgroundTexture;
    private BitmapFont fontTitle;
    private BitmapFont fontText;
    private BitmapFont fontCredits;

    public ScreenAcercaDe(Proyecto game) {
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
        titleParam.size = 50;
        titleParam.color = Color.YELLOW;
        titleParam.borderColor = Color.BLACK;
        titleParam.borderWidth = 3;
        fontTitle = generator.generateFont(titleParam);

        FreeTypeFontParameter textParam = new FreeTypeFontParameter();
        textParam.size = 28;
        textParam.color = new Color(0.1f, 0.1f, 0.4f, 1f); // Azul oscuro
        textParam.shadowColor = Color.GRAY;
        textParam.shadowOffsetX = 2;
        textParam.shadowOffsetY = 2;
        fontText = generator.generateFont(textParam);

        FreeTypeFontParameter creditsParam = new FreeTypeFontParameter();
        creditsParam.size = 24;
        creditsParam.color = new Color(0.2f, 0.2f, 0.6f, 1f); // Azul un poco más claro pero legible
        creditsParam.shadowColor = Color.GRAY;
        creditsParam.shadowOffsetX = 1;
        creditsParam.shadowOffsetY = 1;
        fontCredits = generator.generateFont(creditsParam);

        generator.dispose();

        // Styles
        LabelStyle titleStyle = new LabelStyle(fontTitle, Color.YELLOW);
        LabelStyle textStyle = new LabelStyle(fontText, Color.WHITE);
        LabelStyle creditsStyle = new LabelStyle(fontCredits, Color.WHITE);

        TextButtonStyle btnStyle = new TextButtonStyle();
        btnStyle.font = fontText;
        // Modificado para aspecto "seleccionado"
        btnStyle.fontColor = Color.YELLOW; // Botón "Volver" amarillo
        btnStyle.downFontColor = Color.ORANGE;
        btnStyle.overFontColor = Color.YELLOW;

        // Content Table
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.pad(20);

        // Title
        Label titleLabel = new Label("ACERCA DE", titleStyle);
        mainTable.add(titleLabel).padBottom(40).row();

        // Game Info
        Label gameNameLabel = new Label("Pokemón Legends: Arceus", textStyle);
        mainTable.add(gameNameLabel).padBottom(10).row();

        Label versionLabel = new Label("Versión 1.0", creditsStyle);
        mainTable.add(versionLabel).padBottom(50).row();

        // Developers
        Label devTitleLabel = new Label("Desarrollado por:", textStyle);
        devTitleLabel.setColor(Color.ORANGE);
        mainTable.add(devTitleLabel).padBottom(20).row();

        String developers = "Sodyl Abreu\nNicolas Brito\nDubraska Rodriguez";
        Label devsLabel = new Label(developers, creditsStyle);
        devsLabel.setAlignment(Align.center);
        mainTable.add(devsLabel).padBottom(50).row();

        // Rights / Additional Info
        Label rightsLabel = new Label("Hecho con LibGDX implementado en Java", creditsStyle);
        rightsLabel.setFontScale(0.8f);
        mainTable.add(rightsLabel).padBottom(20).row();

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
        if (fontCredits != null)
            fontCredits.dispose();
    }
}
