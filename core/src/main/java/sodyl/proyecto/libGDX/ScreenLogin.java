package sodyl.proyecto.libGDX;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
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
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import sodyl.proyecto.clases.UserManager;

public class ScreenLogin implements Screen {

    private final Proyecto game;
    private Stage stage;
    private Texture backgroundTexture;
    private BitmapFont fontTitle;
    private BitmapFont fontText;
    private UserManager userManager;

    private TextField userField;
    private TextField passField;
    private Label statusLabel;

    // Texturas para estilos generados dinámicamente
    private Texture fieldBgTexture;
    private Texture cursorTexture;
    private Texture selectionTexture;

    public ScreenLogin(Proyecto game) {
        this.game = game;
        this.userManager = new UserManager();
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(Proyecto.PANTALLA_W, Proyecto.PANTALLA_H));
        Gdx.input.setInputProcessor(stage);

        // Background (reusamos uno existente)
        try {
            backgroundTexture = new Texture(Gdx.files.internal("imagenes/fondoMenu.png"));
        } catch (Exception e) {
            backgroundTexture = new Texture(Gdx.files.internal("imagenes/fondooo.png"));
        }

        Image bgImage = new Image(backgroundTexture);
        bgImage.setFillParent(true);
        stage.addActor(bgImage);

        // Generar Fuentes
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Mapa/ari-w9500-bold.ttf"));

        FreeTypeFontParameter titleParam = new FreeTypeFontParameter();
        titleParam.size = 50;
        titleParam.color = Color.YELLOW;
        titleParam.borderColor = Color.BLACK;
        titleParam.borderWidth = 3;
        fontTitle = generator.generateFont(titleParam);

        FreeTypeFontParameter textParam = new FreeTypeFontParameter();
        textParam.size = 28;
        textParam.color = Color.WHITE;
        textParam.shadowColor = Color.BLACK;
        textParam.shadowOffsetX = 1;
        textParam.shadowOffsetY = 1;
        fontText = generator.generateFont(textParam);

        generator.dispose();

        // Estilos
        LabelStyle titleStyle = new LabelStyle(fontTitle, Color.YELLOW);
        LabelStyle labelStyle = new LabelStyle(fontText, Color.WHITE);

        // Generar texturas para TextField style
        fieldBgTexture = createColorTexture(new Color(0.2f, 0.2f, 0.2f, 0.8f));
        cursorTexture = createColorTexture(Color.WHITE);
        selectionTexture = createColorTexture(new Color(0f, 0f, 1f, 0.5f));

        TextFieldStyle tfStyle = new TextFieldStyle();
        tfStyle.font = fontText;
        tfStyle.fontColor = Color.WHITE;
        tfStyle.background = new TextureRegionDrawable(fieldBgTexture);
        tfStyle.cursor = new TextureRegionDrawable(cursorTexture);
        tfStyle.selection = new TextureRegionDrawable(selectionTexture);

        TextButtonStyle btnStyle = new TextButtonStyle();
        btnStyle.font = fontText;
        btnStyle.fontColor = Color.WHITE;
        btnStyle.downFontColor = Color.GRAY;
        btnStyle.overFontColor = Color.YELLOW;

        // UI Layout
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.center();

        Label titleLabel = new Label("INICIO DE SESIÓN", titleStyle);
        mainTable.add(titleLabel).colspan(2).padBottom(50).row();

        // User Input
        mainTable.add(new Label("Usuario:", labelStyle)).right().padRight(10);
        userField = new TextField("", tfStyle);
        mainTable.add(userField).width(300).padBottom(20).row();

        // Pass Input
        mainTable.add(new Label("Clave:", labelStyle)).right().padRight(10);
        passField = new TextField("", tfStyle);
        passField.setPasswordMode(true);
        passField.setPasswordCharacter('*');
        mainTable.add(passField).width(300).padBottom(30).row();

        // Status Label
        statusLabel = new Label("", labelStyle);
        statusLabel.setColor(Color.RED);
        statusLabel.setAlignment(Align.center);
        mainTable.add(statusLabel).colspan(2).padBottom(20).row();

        // Buttons
        Table btnTable = new Table();
        TextButton loginBtn = new TextButton("Ingresar", btnStyle);
        TextButton registerBtn = new TextButton("Registrarse", btnStyle);
        TextButton exitBtn = new TextButton("Salir", btnStyle);

        loginBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                attemptLogin();
            }
        });

        registerBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                attemptRegister();
            }
        });

        exitBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });

        btnTable.add(loginBtn).padRight(20);
        btnTable.add(registerBtn).padRight(20);
        btnTable.add(exitBtn);

        mainTable.add(btnTable).colspan(2);

        stage.addActor(mainTable);
    }

    private void attemptLogin() {
        String user = userField.getText();
        String pass = passField.getText();

        if (userManager.login(user, pass)) {
            statusLabel.setColor(Color.GREEN);
            statusLabel.setText("¡Bienvenido!");
            // Ir al menú principal
            game.setScreen(new MenuPrincipal(game, true));
        } else {
            statusLabel.setColor(Color.RED);
            statusLabel.setText("Usuario o clave incorrectos.");
        }
    }

    private void attemptRegister() {
        String user = userField.getText();
        String pass = passField.getText();

        String result = userManager.register(user, pass);
        if ("SUCCESS".equals(result)) {
            statusLabel.setColor(Color.GREEN);
            statusLabel.setText("Registro exitoso. ¡Inicia sesión!");
        } else {
            statusLabel.setColor(Color.RED);
            statusLabel.setText(result);
        }
    }

    private Texture createColorTexture(Color color) {
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
        if (stage != null)
            stage.dispose();
        if (backgroundTexture != null)
            backgroundTexture.dispose();
        if (fontTitle != null)
            fontTitle.dispose();
        if (fontText != null)
            fontText.dispose();
        if (fieldBgTexture != null)
            fieldBgTexture.dispose();
        if (cursorTexture != null)
            cursorTexture.dispose();
        if (selectionTexture != null)
            selectionTexture.dispose();
    }
}
