package sodyl.proyecto.libGDX;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.FitViewport;
import sodyl.proyecto.networking.ConexionCliente;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class ScreenMultiplayer implements Screen {

    private final Proyecto game;
    private Stage stage;
    private ConexionCliente client;
    private Label statusLabel;
    private TextButton startButton;
    private boolean isConnecting = false;
    private int connectedPlayers = 1;
    private boolean gameStarted = false;

    private String serverIP = "localhost";

    public ScreenMultiplayer(Proyecto game) {
        this(game, "localhost");
    }

    public ScreenMultiplayer(Proyecto game, String ip) {
        this.game = game;
        this.serverIP = (ip == null || ip.isEmpty()) ? "localhost" : ip;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(800, 600)); // Ajustar viewport según necesidad
        Gdx.input.setInputProcessor(stage);

        Skin skin = new Skin(Gdx.files.internal("uiskin.json")); // Asumiendo que existe un skin básico

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label titleLabel = new Label("Lobby Multijugador", skin);
        statusLabel = new Label("Conectando al servidor...", skin);

        startButton = new TextButton("Volver al Menu", skin);
        startButton.addListener(event -> {
            if (startButton.isPressed()) {
                disconnectAndReturn();
                return true;
            }
            return false;
        });

        table.add(titleLabel).pad(20).row();
        table.add(statusLabel).pad(20).row();
        table.add(startButton).pad(20).row();

        initConnection();
    }

    private void initConnection() {
        if (isConnecting)
            return;
        isConnecting = true;

        client = new ConexionCliente(serverIP, 5000, message -> {
            Gdx.app.postRunnable(() -> handleMessage(message));
        });
        client.conectar();

        // Timeout simple (visual)
        statusLabel.setText("Esperando jugadores... (Mínimo 2)");
    }

    private void handleMessage(String message) {
        // Parsear mensajes simples del servidor para lobby
        // El servidor debería enviar stats de jugadores, etc.
        // Por ahora, si recibimos "START_GAME", iniciamos.

        // Simple JSON parse attempt
        try {
            if (message.contains("START_GAME")) {
                JsonValue root = new JsonReader().parse(message);
                long seed = root.getLong("seed", 0);
                startGame(seed);
            } else if (message.contains("jugadores_conectados")) {
                JsonValue root = new JsonReader().parse(message);
                int count = root.getInt("count", 0);
                connectedPlayers = count;
                statusLabel.setText("Jugadores en línea: " + count + " (Esperando...)");
            }
        } catch (Exception e) {
            Gdx.app.log("LOBBY", "Msg: " + message);
        }
    }

    private void startGame(long seed) {
        if (gameStarted)
            return;
        gameStarted = true;

        // IMPORTANTE: Pasamos la conexión o flag al Mapa
        // ScreenMapaTiled manejará la conexión "in-game"
        // Pero idealmente reutilizamos la conexión o la cerramos y el mapa abre la
        // suya.
        // Dado el código actual de ScreenMapaTiled, abre su propia conexión en
        // constructor.
        // Así que cerramos esta.

        client.desconectar();

        Gdx.app.postRunnable(() -> {
            game.setScreen(new ScreenMapaTiled(game, serverIP, true, seed));
        });
    }

    private void disconnectAndReturn() {
        if (client != null) {
            client.desconectar();
        }
        game.setScreen(new MenuPrincipal(game));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();

        if (client != null && !client.isConectado() && isConnecting) {
            statusLabel.setText("Error de conexión. Reintentando...");
            // Simple reintento o mensaje de error
        }
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
        stage.dispose();
    }

    @Override
    public void dispose() {
        if (stage != null)
            stage.dispose();
    }
}
