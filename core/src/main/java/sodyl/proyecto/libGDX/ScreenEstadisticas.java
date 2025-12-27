package sodyl.proyecto.libGDX;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import java.util.Map;
import java.util.Set;
import sodyl.proyecto.clases.Inventario;
import sodyl.proyecto.clases.Objeto;
import sodyl.proyecto.clases.Pokedex;
import sodyl.proyecto.clases.Pokemon;
import sodyl.proyecto.clases.UserManager;

public class ScreenEstadisticas implements Screen, InputProcessor {

    private final Proyecto game;
    private Stage stage;
    private BitmapFont font;
    private Texture backgroundTexture;
    private Image backgroundImage;
    private Table mainTable;
    private Table contentTable;

    private Inventario tempInventory;
    private String currentUser;

    public ScreenEstadisticas(Proyecto game) {
        this.game = game;
    }

    @Override
    public void show() {
        font = new BitmapFont();
        stage = new Stage(new FitViewport(Proyecto.PANTALLA_W, Proyecto.PANTALLA_H));
        Gdx.input.setInputProcessor(stage);

        // Load Data
        currentUser = UserManager.getCurrentUser();
        tempInventory = new Inventario();
        tempInventory.load(currentUser);
        Pokedex.load(); // Uses currentUser internally now

        // Background
        backgroundTexture = new Texture(Gdx.files.internal("imagenes/fondoPixelado.png"));
        backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);

        // Styles
        TextButtonStyle buttonStyle = new TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.up = new TextureRegionDrawable(createColoredTexture(new Color(0.2f, 0.2f, 0.2f, 0.8f)));
        buttonStyle.down = new TextureRegionDrawable(createColoredTexture(new Color(0.1f, 0.5f, 0.8f, 0.9f)));

        mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.top();
        mainTable.pad(20);

        Label.LabelStyle titleStyle = new Label.LabelStyle(font, Color.YELLOW);
        Label.LabelStyle textStyle = new Label.LabelStyle(font, Color.WHITE);

        mainTable.add(new Label(
                "--- ESTADÍSTICAS DE " + (currentUser != null ? currentUser.toUpperCase() : "INVITADO") + " ---",
                titleStyle)).padBottom(30).row();

        // Buttons
        TextButton btnPokedex = new TextButton("Ver Pokédex", buttonStyle);
        TextButton btnInventory = new TextButton("Ver Inventario", buttonStyle);
        TextButton btnBack = new TextButton("Volver", buttonStyle);

        Table buttonTable = new Table();
        buttonTable.add(btnPokedex).width(200).pad(10);
        buttonTable.add(btnInventory).width(200).pad(10);

        mainTable.add(buttonTable).padBottom(20).row();

        contentTable = new Table();
        contentTable.setBackground(new TextureRegionDrawable(createColoredTexture(new Color(0, 0, 0, 0.5f))));
        mainTable.add(contentTable).expand().fill().padBottom(20).row();

        mainTable.add(btnBack).width(200).padBottom(20);

        stage.addActor(mainTable);

        // Listeners
        btnBack.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MenuPrincipal(game, true, MenuPrincipal.MenuState.OPTIONS_SUB));
            }
        });

        btnPokedex.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showPokedex();
            }
        });

        btnInventory.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showInventory();
            }
        });

        // Default view
        showPokedex();
    }

    private void showPokedex() {
        contentTable.clear();
        Label.LabelStyle nameStyle = new Label.LabelStyle(font, Color.WHITE);
        Label.LabelStyle seenStyle = new Label.LabelStyle(font, Color.GRAY);

        Table list = new Table();
        Set<String> seen = Pokedex.getSeen();

        if (seen.isEmpty()) {
            list.add(new Label("No has avistado ningún Pokémon.", nameStyle));
        } else {
            for (String species : seen) {
                boolean caught = false;
                for (Pokemon p : Pokedex.getCollected()) {
                    if (p.getEspecie().equals(species)) {
                        caught = true;
                        break;
                    }
                }
                String status = caught ? "[CAPTURADO]" : "[VISTO]";
                Color c = caught ? Color.GREEN : Color.GRAY;

                list.add(new Label(status, new Label.LabelStyle(font, c))).padRight(10);
                list.add(new Label(species, nameStyle)).left();
                int level = Pokedex.getResearchLevel(species);
                list.add(new Label("Inv: " + level, new Label.LabelStyle(font, Color.CYAN))).padLeft(20).row();
            }
        }

        ScrollPane scroll = new ScrollPane(list);
        contentTable.add(scroll).expand().fill();
    }

    private void showInventory() {
        contentTable.clear();
        Label.LabelStyle nameStyle = new Label.LabelStyle(font, Color.WHITE);

        Table list = new Table();
        Map<Integer, Integer> items = tempInventory.getAllObjetos();

        if (items.isEmpty()) {
            list.add(new Label("Tu inventario está vacío.", nameStyle));
        } else {
            for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
                Objeto obj = Objeto.getObjeto(entry.getKey());
                if (obj != null) {
                    list.add(new Label(obj.getNombre(), nameStyle)).left().padRight(20);
                    list.add(new Label("x" + entry.getValue(), new Label.LabelStyle(font, Color.YELLOW))).right().row();
                }
            }
        }

        ScrollPane scroll = new ScrollPane(list);
        contentTable.add(scroll).expand().fill();
    }

    private Texture createColoredTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
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
        stage.dispose();
        font.dispose();
        backgroundTexture.dispose();
    }

    // InputProcessor methods
    @Override

    public boolean keyDown(int keycode) {
        if (keycode == Keys.ESCAPE || keycode == Keys.Z || keycode == Keys.X || keycode == Keys.LEFT
                || keycode == Keys.BACKSPACE) {
            game.setScreen(new MenuPrincipal(game, true, MenuPrincipal.MenuState.OPTIONS_SUB));
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
}
