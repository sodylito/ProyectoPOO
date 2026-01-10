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
import com.badlogic.gdx.utils.viewport.FitViewport;

public class ScreenAyuda implements Screen, InputProcessor {

    private final Proyecto game;
    private Stage stage;
    private Texture backgroundTexture;
    private BitmapFont fontTitle;
    private BitmapFont fontText;
    private ScrollPane scrollPane;
    private Texture shadowTexture;

    public ScreenAyuda(Proyecto game) {
        this.game = game;
    }

    @Override
    public void show() {
        game.playMusic("musica/jumpUp.mp3");
        stage = new Stage(new FitViewport(Proyecto.PANTALLA_W, Proyecto.PANTALLA_H));

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(this);
        Gdx.input.setInputProcessor(multiplexer);

        // Background
        backgroundTexture = new Texture(Gdx.files.internal("imagenes/pantallaCarga.jpg"));
        Image bgImage = new Image(backgroundTexture);
        bgImage.setFillParent(true);
        stage.addActor(bgImage);

        // Shadow Overlay for contrast
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1,
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0, 0, 0, 0.6f));
        pixmap.fill();
        shadowTexture = new Texture(pixmap);
        pixmap.dispose();
        Image shadowOverlay = new Image(shadowTexture);
        shadowOverlay.setFillParent(true);
        stage.addActor(shadowOverlay);

        // Fonts
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Mapa/ari-w9500-bold.ttf"));

        FreeTypeFontParameter titleParam = new FreeTypeFontParameter();
        titleParam.size = 40;
        titleParam.color = Color.RED;
        titleParam.borderWidth = 0;
        fontTitle = generator.generateFont(titleParam);

        FreeTypeFontParameter textParam = new FreeTypeFontParameter();
        textParam.size = 24;
        textParam.color = Color.WHITE;
        textParam.borderColor = Color.BLACK;
        textParam.borderWidth = 1.2f;
        fontText = generator.generateFont(textParam);

        generator.dispose();

        // Styles
        // Styles
        LabelStyle titleStyle = new LabelStyle(fontTitle, Color.RED);
        LabelStyle headerStyle = new LabelStyle(fontText, Color.RED);
        LabelStyle textStyle = new LabelStyle(fontText, Color.WHITE);

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
        contentTable.pad(20);

        float textWidth = Proyecto.PANTALLA_W * 0.75f;

        // 1. ¿QUÉ ES ESTE JUEGO?
        contentTable.add(new Label("¿QUÉ ES ESTE JUEGO?", headerStyle)).padBottom(10).left().row();
        String introText = "Este es un juego de aventura y estrategia donde exploras un mundo lleno de criaturas llamadas Pokémon. "
                +
                "Tu meta es convertirte en un gran entrenador, capturando Pokémon y fortaleciéndolos para el desafío final.";
        Label introLabel = new Label(introText, textStyle);
        introLabel.setWrap(true);
        contentTable.add(introLabel).width(textWidth).padBottom(20).left().row();

        // 2. CONTROLES
        contentTable.add(new Label("CONTROLES BÁSICOS:", headerStyle)).padBottom(10).left().row();
        String controlesText = "- MOVERSE: Usa las FLECHAS del teclado para caminar por el mapa.\n" +
                "- INTERACTUAR: Presiona Z o ENTER para hablar con personas o recoger objetos.\n" +
                "- MENÚ/ATRÁS: Presiona X o ESCAPE para abrir el menú de pausa o volver atrás.\n" +
                "- SCROLL DE AYUDA: En esta pantalla, usa las FLECHAS ARRIBA/ABAJO para leer todo.";
        Label controlesLabel = new Label(controlesText, textStyle);
        controlesLabel.setWrap(true);
        contentTable.add(controlesLabel).width(textWidth).padBottom(20).left().row();

        // 3. EXPLORACIÓN Y OBJETIVOS
        contentTable.add(new Label("EXPLORACIÓN Y OBJETIVOS:", headerStyle)).padBottom(10).left().row();
        String exploracionText = "1. BUSCA OBJETOS: Camina sobre flores o arbustos para encontrar ítems útiles.\n" +
                "2. ENCUENTROS SALVAJES: Al caminar por zonas con flores, pueden aparecer Pokémon salvajes por sorpresa.\n"
                +
                "3. EL SANTUARIO: Tu objetivo final es llegar al Santuario de Arceus (marcado en el mapa) cuando seas lo suficientemente fuerte.";
        Label exploracionLabel = new Label(exploracionText, textStyle);
        exploracionLabel.setWrap(true);
        contentTable.add(exploracionLabel).width(textWidth).padBottom(20).left().row();

        // 4. EL ARTE DEL COMBATE
        contentTable.add(new Label("EL SISTEMA DE COMBATE:", headerStyle)).padBottom(10).left().row();
        String batallaText = "Las batallas se juegan por turnos. En tu turno puedes:\n" +
                "- LUCHAR: Elige un ataque para reducir la vida (HP) del oponente.\n" +
                "- BOLSA: Usa objetos para curar a tu Pokémon o lanzar Pokéballs para capturar al rival.\n" +
                "- POKÉMON: Cambia a otro miembro de tu equipo.\n" +
                "- HUIR: Intenta escapar de una batalla salvaje (solo contra Pokémon salvajes).";
        Label batallaLabel = new Label(batallaText, textStyle);
        batallaLabel.setWrap(true);
        contentTable.add(batallaLabel).width(textWidth).padBottom(20).left().row();

        // 5. TIPOS Y ESTRATEGIA
        contentTable.add(new Label("TIPOS Y ESTRATEGIA:", headerStyle)).padBottom(10).left().row();
        String tiposText = "Cada Pokémon tiene 'Tipos' (Fuego, Agua, Planta, etc.).\n" +
                "- El Fuego vence a la Planta.\n" +
                "- El Agua vence al Fuego.\n" +
                "- Planta vence al Agua.\n" +
                "¡Usa ataques que sean efectivos contra el tipo del rival para hacer DOBLE de daño!";
        Label tiposLabel = new Label(tiposText, textStyle);
        tiposLabel.setWrap(true);
        contentTable.add(tiposLabel).width(textWidth).padBottom(20).left().row();

        // 6. ENTRENADORES Y NPCs
        contentTable.add(new Label("ENTRENADORES (NPCs):", headerStyle)).padBottom(10).left().row();
        String npcText = "Encontrarás personas en el mundo. Algunos te darán consejos, pero otros te retarán a un combate.\n"
                +
                "- Los combates contra entrenadores son más difíciles. No puedes huir de ellos.\n" +
                "- Una vez que vences a un entrenador, no volverá a pelear contigo.";
        Label npcLabel = new Label(npcText, textStyle);
        npcLabel.setWrap(true);
        contentTable.add(npcLabel).width(textWidth).padBottom(20).left().row();

        // 7. CRECIMIENTO (NIVELES)
        contentTable.add(new Label("SUBIR DE NIVEL:", headerStyle)).padBottom(10).left().row();
        String nivelText = "- Ganar combates aumenta el nivel de tu Pokémon, mejorando su daño y vida.\n" +
                "- El nivel máximo es 10. ¡Un Pokémon nivel 10 es un 'Pokémon Superior'!\n" +
                "- Capturar a un Pokémon que ya tienes también le da un gran empuje de nivel.";
        Label nivelLabel = new Label(nivelText, textStyle);
        nivelLabel.setWrap(true);
        contentTable.add(nivelLabel).width(textWidth).padBottom(40).left().row();

        this.scrollPane = new ScrollPane(contentTable);
        scrollPane.setFadeScrollBars(false);
        mainTable.add(scrollPane).grow().width(Proyecto.PANTALLA_W * 0.85f).padBottom(20).row();

        // Back Button
        TextButton backButton = new TextButton("VOLVER (ESC)", btnStyle);
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
        if (keycode == Keys.UP || keycode == Keys.DOWN) {
            float scrollAmount = 50f;
            if (keycode == Keys.UP) {
                scrollPane.setScrollY(scrollPane.getScrollY() - scrollAmount);
            } else {
                scrollPane.setScrollY(scrollPane.getScrollY() + scrollAmount);
            }
            return true;
        }
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
        if (shadowTexture != null)
            shadowTexture.dispose();
        if (fontTitle != null)
            fontTitle.dispose();
        if (fontText != null)
            fontText.dispose();
    }
}
