package sodyl.proyecto.networking;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.HashMap;
import java.util.Map;

public class PlayerNetworkManager implements ConexionCliente.MessageListener {

    public interface RemotePlayerListener {
        void onRemotePlayerCreated(PlayerState state);

        void onRemotePlayerUpdated(PlayerState state);

        void onRemotePlayerDisconnected(String id);
    }

    private final ConexionCliente conexion;
    private final String myId;
    private final RemotePlayerListener listener;
    private final Map<String, PlayerState> remotePlayers = new HashMap<>();

    private float lastSendTime = 0f;
    private float sendInterval = 0.1f; // 10 veces por segundo

    public PlayerNetworkManager(ConexionCliente conexion, String myId, RemotePlayerListener listener) {
        this.conexion = conexion;
        this.myId = myId;
        this.listener = listener;
    }

    /**
     * Llamar desde tu update(delta) del juego, pasando la posición actual del
     * jugador local.
     */
    public void update(float delta, float myX, float myY) {
        lastSendTime += delta;
        if (lastSendTime >= sendInterval && conexion.isConectado()) {
            lastSendTime = 0f;
            enviarPlayerUpdate(myX, myY);
        }
    }

    private void enviarPlayerUpdate(float x, float y) {
        String msg = "{"
                + "\"tipo\":\"player_update\","
                + "\"id\":\"" + myId + "\","
                + "\"x\":" + x + ","
                + "\"y\":" + y
                + "}";
        conexion.enviarMensaje(msg);
    }

    public void enviarPlayerJoin(float x, float y) {
        String msg = "{"
                + "\"tipo\":\"player_join\","
                + "\"id\":\"" + myId + "\","
                + "\"x\":" + x + ","
                + "\"y\":" + y
                + "}";
        conexion.enviarMensaje(msg);
    }

    @Override
    public void onMessageReceived(String message) {
        try {
            JsonReader reader = new JsonReader();
            JsonValue root = reader.parse(message);

            String tipo = root.getString("tipo", "");
            if (tipo.equals("player_update") || tipo.equals("player_join")) {
                String id = root.getString("id", null);
                if (id == null || id.equals(myId))
                    return; // ignorar mi propio mensaje

                float x = root.getFloat("x", 0f);
                float y = root.getFloat("y", 0f);

                PlayerState state = remotePlayers.get(id);
                if (state == null) {
                    state = new PlayerState(id, x, y);
                    remotePlayers.put(id, state);
                    if (listener != null)
                        listener.onRemotePlayerCreated(state);
                } else {
                    state.x = x;
                    state.y = y;
                    if (listener != null)
                        listener.onRemotePlayerUpdated(state);
                }
            } else if (tipo.equals("desconectar")) {
                String id = root.getString("id", null);
                if (id != null && !id.equals(myId)) {
                    remotePlayers.remove(id);
                    if (listener != null)
                        listener.onRemotePlayerDisconnected(id);
                }
            }

            // Aquí puedes seguir procesando otros tipos: duelos, items, etc.
        } catch (Exception e) {
            Gdx.app.error("NETWORK", "Error procesando mensaje: " + message + " -> " + e.getMessage());
        }
    }

    public Map<String, PlayerState> getRemotePlayers() {
        return remotePlayers;
    }
}
