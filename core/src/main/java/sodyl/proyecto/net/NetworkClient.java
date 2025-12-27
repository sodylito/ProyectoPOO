package sodyl.proyecto.net;

import com.badlogic.gdx.Gdx;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NetworkClient {
    private static NetworkClient instance;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Thread listenerThread;
    private boolean connected = false;

    // IP Local por defecto (localhost). Cambiar si se prueba en red.
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 9999;

    private NetworkClient() {
    }

    public static NetworkClient getInstance() {
        if (instance == null) {
            instance = new NetworkClient();
        }
        return instance;
    }

    public void connect() {
        if (connected)
            return;

        new Thread(() -> {
            try {
                System.out.println("Intentando conectar al servidor " + SERVER_IP + ":" + SERVER_PORT + "...");
                socket = new Socket(SERVER_IP, SERVER_PORT);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                connected = true;
                System.out.println("¡Conectado!");

                // Iniciar hilo de escucha
                startListener();
            } catch (IOException e) {
                Gdx.app.error("NET", "Error al conectar: " + e.getMessage());
            }
        }).start();
    }

    private void startListener() {
        listenerThread = new Thread(() -> {
            try {
                String message;
                while (connected && (message = in.readLine()) != null) {
                    final String msg = message;
                    // IMPORTANTE: Volver al hilo principal de LibGDX para tocar gráficos/lógica
                    Gdx.app.postRunnable(() -> {
                        handleMessage(msg);
                    });
                }
            } catch (IOException e) {
                System.out.println("Desconectado del servidor.");
                connected = false;
            }
        });
        listenerThread.start();
    }

    private void handleMessage(String msg) {
        Gdx.app.log("NET_MSG", "Recibido: " + msg);
        // Aquí añadiremos lógica para mover otros jugadores, etc.
    }

    public void sendMessage(String msg) {
        if (connected && out != null) {
            // Enviar en un hilo aparte para no bloquear UI (aunque PrintWriter es rápido,
            // es buena práctica)
            new Thread(() -> out.println(msg)).start();
        }
    }
}
