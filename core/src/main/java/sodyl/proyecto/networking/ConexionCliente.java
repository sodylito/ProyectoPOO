package sodyl.proyecto.networking;

import com.badlogic.gdx.Gdx;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.InetAddress;
import java.util.Map;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

public class ConexionCliente {
    private String servidorIP;
    private int puerto;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private MessageListener listener;
    private boolean conectado = false;
    private Thread listenerThread;

    public interface MessageListener {
        void onMessageReceived(String message);
    }

    public ConexionCliente(String servidorIP, int puerto, MessageListener listener) {
        this.servidorIP = servidorIP;
        this.puerto = puerto;
        this.listener = listener;
    }

    public void conectar() {
        if (conectado)
            return;

        new Thread(() -> {
            try {
                Gdx.app.log("NETWORK", "Intentando conectar a " + servidorIP + ":" + puerto);
                // Si es localhost, usamos la IP de loopback explícita para evitar problemas de
                // resolución
                if (servidorIP.equals("localhost")) {
                    socket = new Socket(InetAddress.getByName("127.0.0.1"), puerto);
                } else {
                    socket = new Socket(servidorIP, puerto);
                }

                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                conectado = true;
                Gdx.app.log("NETWORK", "Conectado al servidor!");

                // Iniciar hilo de escucha
                startListening();

            } catch (IOException e) {
                Gdx.app.error("NETWORK", "Error al conectar: " + e.getMessage());
                conectado = false;
            }
        }).start();
    }

    private void startListening() {
        listenerThread = new Thread(() -> {
            try {
                String inputLine;
                while (conectado && (inputLine = in.readLine()) != null) {
                    if (listener != null) {
                        listener.onMessageReceived(inputLine);
                    }
                }
            } catch (IOException e) {
                if (conectado) { // Solo loguear error si no fue una desconexión intencional
                    Gdx.app.error("NETWORK", "Error leyendo del servidor: " + e.getMessage());
                }
            } finally {
                desconectar();
            }
        });
        listenerThread.start();
    }

    public void enviarMensaje(String mensaje) {
        if (conectado && out != null) {
            new Thread(() -> {
                try {
                    out.println(mensaje);
                } catch (Exception e) {
                    Gdx.app.error("NETWORK", "Error enviando mensaje: " + e.getMessage());
                }
            }).start();
        }
    }

    public void enviar(Map<String, Object> data) {
        Json json = new Json();
        json.setOutputType(OutputType.json);
        enviarMensaje(json.toJson(data));
    }

    public void desconectar() {
        conectado = false;
        try {
            if (out != null)
                out.close();
            if (in != null)
                in.close();
            if (socket != null)
                socket.close();
            Gdx.app.log("NETWORK", "Desconectado.");
        } catch (IOException e) {
            Gdx.app.error("NETWORK", "Error al cerrar conexión: " + e.getMessage());
        }
    }

    public boolean isConectado() {
        return conectado;
    }
}
