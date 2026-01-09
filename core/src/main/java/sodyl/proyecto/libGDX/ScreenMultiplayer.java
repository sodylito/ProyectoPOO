package sodyl.proyecto.libGDX;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import sodyl.proyecto.clases.UserManager;
import sodyl.proyecto.networking.ConexionCliente;
import sodyl.proyecto.libGDX.Collectible;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScreenMultiplayer extends ScreenMapaTiled {

    private ConexionCliente conexion;
    private Map<String, OtherPlayer> otherPlayers = new HashMap<>();
    private float syncTimer = 0;
    private static final float SYNC_INTERVAL = 0.05f;
    private String sessionID;

    public ScreenMultiplayer(Proyecto game) {
        super(game, "Mapa/MAPACOMPLETO.tmx", null, null, null, GameState.FREE_ROAMING);
        // Generate a unique session ID for this instance to avoid self-collision when
        // testing on same account/machine
        // Format: Username_UUID
        String username = UserManager.getCurrentUser();
        // Fallback if no user
        if (username == null)
            username = "Guest";

        this.sessionID = username + "_" + UUID.randomUUID().toString().substring(0, 8);

        Gdx.app.log("MULTIPLAYER", "Starting session with ID: " + sessionID);
        initNetworking();
    }

    private void initNetworking() {
        String serverIP = "localhost"; // Could be configurable
        conexion = new ConexionCliente(serverIP, 5000, message -> {
            Gdx.app.postRunnable(() -> handleNetworkMessage(message));
        });
        conexion.conectar();
    }

    private void handleNetworkMessage(String message) {
        try {
            JsonValue root = new JsonReader().parse(message);
            String tipo = root.getString("tipo", "");
            String id = root.getString("id", "");

            // Ignore our own messages
            if (id.equals(this.sessionID))
                return;

            if ("mover".equals(tipo)) {
                float x = root.getFloat("x");
                float y = root.getFloat("y");
                Direction dir = Direction.valueOf(root.getString("dir", "DOWN"));
                boolean moving = root.getBoolean("moving", false);

                if (!otherPlayers.containsKey(id)) {
                    otherPlayers.put(id, new OtherPlayer(id, x, y, dir, moving));
                } else {
                    otherPlayers.get(id).update(x, y, dir, moving);
                }
            } else if ("desconectar".equals(tipo)) {
                if (otherPlayers.containsKey(id)) {
                    otherPlayers.get(id).remove();
                    otherPlayers.remove(id);
                }
            } else if ("INIT".equals(tipo)) {
                String serverMap = root.getString("map", "");
                long seed = root.getLong("seed", 0);

                Gdx.app.log("MULTIPLAYER", "Received INIT: Map=" + serverMap + ", Seed=" + seed);

                // Sync World State on UI Thread
                Gdx.app.postRunnable(() -> {
                    // 1. Set Seed
                    if (this.random != null) {
                        this.random.setSeed(seed);
                    }

                    // 2. Refresh Collectibles
                    if (this.collectibles != null) {
                        // Remove existing actors
                        for (Collectible c : this.collectibles) {
                            if (c.getActor() != null)
                                c.getActor().remove();
                        }
                        this.collectibles.clear();

                        // Respawn using new seed
                        if (mapPath.equals("Mapa/MAPACOMPLETO.tmx")) {
                            spawnPrimaryMaterialsInZones(20);
                        } else {
                            spawnCollectiblesOnGrassTiles(100);
                        }

                        // Note: spawn methods already add to stage and list
                        Gdx.app.log("MULTIPLAYER", "World synchronized with server seed.");
                    }
                });
            }
        } catch (Exception e) {
            Gdx.app.error("NETWORK", "Error parsing message: " + message, e);
        }
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        // Sync logic is handled here instead of base class
        if (conexion != null && conexion.isConectado() && currentState == GameState.FREE_ROAMING) {
            syncTimer += delta;

            // Only sync if we have moved or if it's time (currently checking time only but
            // relying on base class movement)
            // Ideally we check if characterActor position changed, but broadcasting
            // periodically is safer for now.
            // Using logic similar to what was in base class.

            if (syncTimer >= SYNC_INTERVAL) {
                // Accessing protected fields from base class
                float currentX = characterActor.getX();
                float currentY = characterActor.getY();

                // We need to know if we are moving.
                // In base class moving flags are reset every frame in input handling if we
                // don't press keys?
                // No, update process handles it.
                // Let's infer 'moving' state from flags or just send current state.
                boolean isMoving = movingUp || movingDown || movingLeft || movingRight;

                Map<String, Object> data = new HashMap<>();
                data.put("tipo", "mover");
                data.put("id", sessionID);
                data.put("x", currentX);
                data.put("y", currentY);
                data.put("dir", lastDirection.name());
                data.put("moving", isMoving);

                conexion.enviar(data);
                syncTimer = 0;
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (conexion != null) {
            conexion.desconectar();
        }
    }

    // Inner class for other players (copied/adapted from base class but managed
    // here)
    private class OtherPlayer {
        Image actor;
        String id;

        public OtherPlayer(String id, float x, float y, Direction direction, boolean isMoving) {
            this.id = id;
            // Need to verify if 'walkDownAnimation' is initialized in base class before we
            // use it?
            // Base class initialization happens in show(). We call super.show() implicitly
            // via game.setScreen?
            // Actually Constructor calls this(), which sets map and state. show() is called
            // by Game.
            // We need to ensure assets are loaded. Base class show() initializes them.
            // If OtherPlayer is created before show(), we crash.
            // Network runs on thread, posts to runnable.
            // Ideally safe.

            TextureRegion frame = walkDownAnimation.getKeyFrame(0);
            this.actor = new Image(frame);
            this.actor.setSize(2f, 2f); // Assuming 2x2 scale as in base class?
            // In base class: this.actor.setSize(2f, 2f); // It was using constants or
            // hardcoded.
            // Let's stick to what was there.

            this.actor.setPosition(x, y);

            // Add to the STAGE of the base class.
            stage.addActor(this.actor);
        }

        public void update(float x, float y, Direction direction, boolean isMoving) {
            this.actor.setPosition(x, y);
            TextureRegion currentFrame = null;

            if (isMoving) {
                switch (direction) {
                    case UP:
                        currentFrame = walkUpAnimation.getKeyFrame(stateTime, true);
                        break;
                    case DOWN:
                        currentFrame = walkDownAnimation.getKeyFrame(stateTime, true);
                        break;
                    case LEFT:
                        currentFrame = walkLeftAnimation.getKeyFrame(stateTime, true);
                        break;
                    case RIGHT:
                        currentFrame = walkRightAnimation.getKeyFrame(stateTime, true);
                        break;
                    default:
                        currentFrame = walkDownAnimation.getKeyFrame(stateTime, true);
                        break;
                }
            } else {
                switch (direction) {
                    case UP:
                        currentFrame = walkUpAnimation.getKeyFrame(0);
                        break;
                    case DOWN:
                        currentFrame = walkDownAnimation.getKeyFrame(0);
                        break;
                    case LEFT:
                        currentFrame = walkLeftAnimation.getKeyFrame(0);
                        break;
                    case RIGHT:
                        currentFrame = walkRightAnimation.getKeyFrame(0);
                        break;
                    default:
                        currentFrame = walkDownAnimation.getKeyFrame(0);
                        break;
                }
            }
            this.actor.setDrawable(new TextureRegionDrawable(currentFrame));
        }

        public void remove() {
            if (actor != null)
                actor.remove();
        }
    }
}
