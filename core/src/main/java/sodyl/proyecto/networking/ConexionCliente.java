package sodyl.proyecto.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.Gdx;

public class ConexionCliente {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String serverIP;
    private int puerto;
    private NetworkListener decoder;
    private boolean conectado = false;

    public interface NetworkListener {
        void onMessageReceived(String message);
    }

    public ConexionCliente(String serverIP, int puerto, NetworkListener decoder) {
        this.serverIP = serverIP;
        this.puerto = puerto;
        this.decoder = decoder;
    }

    public void conectar() {
        new Thread(() -> {
            try {
                socket = new Socket(serverIP, puerto);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                conectado = true;
                Gdx.app.log("NETWORK", "Conectado al servidor en " + serverIP);

                String line;
                while ((line = in.readLine()) != null) {
                    if (decoder != null) {
                        decoder.onMessageReceived(line);
                    }
                }
            } catch (IOException e) {
                Gdx.app.error("NETWORK", "Error de conexión: " + e.getMessage());
                conectado = false;
            }
        }).start();
    }

    public void enviar(Object data) {
        if (conectado && out != null) {
            Json json = new Json();
            json.setOutputType(JsonWriter.OutputType.json);
            out.println(json.toJson(data));
        }
    }

    public boolean isConectado() {
        return conectado;
    }

    public void desconectar() {
        try {
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
