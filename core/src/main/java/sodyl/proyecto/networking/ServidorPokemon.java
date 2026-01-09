package sodyl.proyecto.networking;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServidorPokemon {
    private static final int PUERTO = 5000;
    private static final Set<ManejadorCliente> clientes = Collections.synchronizedSet(new HashSet<>());
    private static final ExecutorService pool = Executors.newCachedThreadPool();

    // Game State
    private static final long GAME_SEED = new java.util.Random().nextLong();
    private static final String CURRENT_MAP = "Mapa/MAPACOMPLETO.tmx";

    public static void main(String[] args) {
        System.out.println("Servidor Pokémon iniciado en el puerto " + PUERTO);
        System.out.println("Semilla de juego: " + GAME_SEED);

        try (ServerSocket listener = new ServerSocket(PUERTO)) {
            while (true) {
                Socket socket = listener.accept();
                System.out.println("Nuevo cliente conectado desde " + socket.getInetAddress());
                ManejadorCliente manejador = new ManejadorCliente(socket, clientes);
                clientes.add(manejador);
                pool.execute(manejador);

                // Enviar paquete INIT inmediatemente
                enviarInit(manejador);
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }

    private static void enviarInit(ManejadorCliente cliente) {
        // Simple JSON construction string to avoid dependencies if GDX utils not
        // present,
        // but assuming we might want to be consistent.
        // Since this is standard Java, we construct JSON manually string or use a lib
        // if available.
        // Using simple string format for minimal dependency in this file (which seems
        // to be pure Java)
        // Format: {"tipo":"INIT", "map":"...", "seed":...}
        String json = String.format("{\"tipo\":\"INIT\", \"map\":\"%s\", \"seed\":%d}", CURRENT_MAP, GAME_SEED);
        cliente.enviar(json);
    }

    public static void broadcast(String mensaje, ManejadorCliente remitente) {
        synchronized (clientes) {
            for (ManejadorCliente cliente : clientes) {
                if (cliente != remitente) {
                    cliente.enviar(mensaje);
                }
            }
        }
    }
}
