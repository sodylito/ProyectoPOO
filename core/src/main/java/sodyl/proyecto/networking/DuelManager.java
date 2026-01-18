package sodyl.proyecto.networking;

import com.badlogic.gdx.Gdx;
import java.util.HashMap;
import java.util.Map;
import sodyl.proyecto.clases.Pokemon;

public class DuelManager {

    public enum DuelState {
        NONE, WAITING_ACCEPT, IN_PROGRESS, FINISHED
    }

    private DuelState currentState = DuelState.NONE;
    private String opponentId;
    private Pokemon myPokemon;
    private Pokemon opponentPokemon;
    private boolean isMyTurn;

    public interface DuelUIListener {
        void onDuelRequested(String challengerId);

        void onDuelAccepted();

        void onDuelUpdate(String log);

        void onDuelEnd(boolean won);
    }

    private DuelUIListener listener;
    private ConexionCliente client;

    public DuelManager(ConexionCliente client, DuelUIListener listener) {
        this.client = client;
        this.listener = listener;
    }

    public void requestDuel(String targetId, Pokemon myPoke) {
        this.opponentId = targetId;
        this.myPokemon = myPoke;
        this.currentState = DuelState.WAITING_ACCEPT;

        String msg = "{ \"tipo\": \"duel_request\", \"target\": \"" + targetId + "\" }";
        client.enviarMensaje(msg);
        listener.onDuelUpdate("Desafío enviado a " + targetId + "...");
    }

    public void acceptDuel(String challengerId, Pokemon myPoke) {
        this.opponentId = challengerId;
        this.myPokemon = myPoke;
        this.currentState = DuelState.IN_PROGRESS;
        this.isMyTurn = false;

        String msg = "{ \"tipo\": \"duel_accept\", \"target\": \"" + challengerId + "\" }";
        client.enviarMensaje(msg);
        listener.onDuelAccepted();
    }

    public void attack() {
        if (!isMyTurn)
            return;

        int damage = 10;

        String msg = "{ \"tipo\": \"duel_attack\", \"target\": \"" + opponentId + "\", \"damage\": " + damage + " }";
        client.enviarMensaje(msg);

        isMyTurn = false;
        listener.onDuelUpdate("Has atacado! Esperando rival...");
    }

    public void processMessage(String type, String senderId, float damageValue) {
        if (type.equals("duel_request")) {
            listener.onDuelRequested(senderId);
        } else if (type.equals("duel_accept")) {
            currentState = DuelState.IN_PROGRESS;
            isMyTurn = true;
            listener.onDuelAccepted();
            listener.onDuelUpdate("¡Duelo iniciado! Tu turno.");
        } else if (type.equals("duel_attack")) {
            if (myPokemon != null) {
                // myPokemon.receivesDamage((int)damageValue);
            }
            listener.onDuelUpdate("Rival atacó con " + damageValue + " daño.");
            isMyTurn = true;
            // if (myPokemon.hp <= 0) sendDefeat();
        }
    }
}
