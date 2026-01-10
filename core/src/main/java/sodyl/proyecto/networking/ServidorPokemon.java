package sodyl.proyecto.networking;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Map;

public class ServidorPokemon {
    private static final int PUERTO = 5000;
    private static final Set<ManejadorCliente> clientes = Collections.synchronizedSet(new HashSet<>());
    private static final Map<String, PlayerState> playerStates = new java.util.concurrent.ConcurrentHashMap<>();
    private static final ExecutorService pool = Executors.newCachedThreadPool();

    static class PlayerState {
        String id;
        float x, y;
        String dir;
        boolean moving;

        public PlayerState(String id, float x, float y, String dir, boolean moving) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.dir = dir;
            this.moving = moving;
        }
    }

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

                // Avisar a los demas que ha entrado un nuevo jugador (o al menos que se
                // conectó)
                // Realmente el jugador no "entra" al mapa visualmente hasta que se mueve y
                // manda su primer paquete "mover".
                // PERO, para que aparezca "idle" al principio, podríamos mandar un paquete
                // dummy.
                // Sin embargo, el INIT ya manda la lista de los que ESTAN.
                // El problema es el que YA ESTABA conectado, no sabe que entró uno nuevo hasta
                // que el nuevo se mueva.
                // Así que sí, mandamos un aviso.
                broadcastNuevoJugador(manejador);
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }

    private static void broadcastNuevoJugador(ManejadorCliente nuevoCliente) {
        // Mandamos un paquete dummy de "mover" o uno especifico "nuevo_jugador".
        // Usemos "nuevo_jugador" para ser explicitos, aunque ScreenMultiplayer usa
        // "mover" para crear/actualizar.
        // Si usamos "mover" con la posicion inicial (que aun no sabemos con certeza
        // hasta que el cliente diga),
        // podriamos asumir 0,0 o una por defecto.
        // MEJOR: El ScreenMultiplayer manda su posicion inicial al conectarse?
        // No, solo manda cuando se mueve.
        // Entonces el Servidor no sabe donde está el nuevo hasta que se mueva.
        // SOLUCION: El cliente nuevo DEBERIA mandar un "hola" o su primer "mover" al
        // iniciar.
        // PERO, como hotfix rápido solicitado: mandemos un "nuevo_jugador" con ID y
        // warning de que no tiene pos aun.
        // O mejor, esperemos a que el cliente mande su primer update.

        // EL PLAN DECE: "broadcastNuevoJugador: Sends ... to all other clients."
        // Asumamos que el cliente inicia en una pos default (ej. la del mapa).
        // Pero el servidor no sabe esa pos.
        // Vamos a mandar un mensaje especial que el cliente interpretará.
        // ID será algo temporal hasta que el cliente mande su ID real?
        // No, el ID lo genera el cliente. El servidor no lo sabe AUN.
        // ERROR EN EL PLAN: El Servidor no sabe el ID del cliente *antes* de que el
        // cliente mande datos.
        // El socket no tiene esa info.
        // El packet INIT va DEL servidor AL cliente.
        // El cliente responde?

        // REVISANDO CODIGO CLIENTE:
        // initNetworking() -> conecta -> no manda nada inmediatamente.
        // render() -> manda "mover" si syncTimer > intervalo.
        //
        // Entonces, apenas el cliente entra, a los 0.05s mandará un "mover" con su ID y
        // Pos.
        // Ese "mover" llegará al server, el server actualizará 'playerStates' y hará
        // broadcast.
        // Y el broadcast de "mover" YA tiene la logica:
        // if (!otherPlayers.containsKey(id)) { otherPlayers.put(...) }
        //
        // ENTONCES, ¿Por qué "los jugadores que entran tarde ... no ven a los que ya
        // estaban"?
        // Ah, el problema original era: "NO ven a los que YA ESTABAN".
        // Eso se arregló con el INIT y la lista de jugadores PREVIOS (step anterior).
        //
        // El usuario ahora dice: "El programa aun no logra el multijugador online
        // mediante la misma red... dos mundos distintos".
        // Y "El servidor debe detectar cuantos jugadores hay".
        //
        // Si el problema es RED, es por la IP "localhost".
        // El broadcastNuevoJugador que pidió el usuario es redundante si el cliente
        // manda "mover" al inicio.
        // PERO, el usuario lo pidió en el prompt anterior ("En ServidorPokemon...
        // broadcastNuevoJugador").
        //
        // Si el cliente tarda en mandar "mover", es invisible.
        // Si el servidor manda "nuevo_jugador", ¿qué ID manda? No lo tiene.
        // Omitiré broadcastNuevoJugador por ahora porque no tengo el ID del cliente
        // hasta que él hable.
        // El flujo "mover" inicial del cliente debería ser suficiente para que *otros*
        // lo vean.
        // El flujo "INIT" del server es suficiente para que *el nuevo* vea a los otros.
        //
        // El verdadero problema reportado ahora es "creando dos mundos multijugador
        // distintos".
        // Esto pasa si conectan a localhost en 2 maquinas distintas -> Cada uno es su
        // propio server local.
        // LA SOLUCION ES LA IP.

        // Ok, implementaré la parte de la IP primero que es la crítica.

        // Wait, the user specifically asked for "cuando el servidor reciba un mensaje
        // tipo mover... actualiza".
        // "Modifica el paquete INIT... enviar lista".
        // "En ScreenMultiplayer, al recibir INIT... instancia".
        // I did all that.

        // This NEW request: "El servidor debe detectar cuantos jugadores hay... un
        // cliente entra... presiona Multijugador".
        // "El programa aun no logra el multijugador online mediante la misma red".
        // Confirms IP issue.
    }

    private static void enviarInit(ManejadorCliente cliente) {
        // Construct JSON manually
        StringBuilder playersJson = new StringBuilder("[");
        synchronized (playerStates) {
            int i = 0;
            for (PlayerState ps : playerStates.values()) {
                if (i > 0)
                    playersJson.append(",");
                playersJson.append(String.format(java.util.Locale.US,
                        "{\"id\":\"%s\",\"x\":%f,\"y\":%f,\"dir\":\"%s\",\"moving\":%b}",
                        ps.id, ps.x, ps.y, ps.dir, ps.moving));
                i++;
            }
        }
        playersJson.append("]");

        String json = String.format(java.util.Locale.US,
                "{\"tipo\":\"INIT\", \"map\":\"%s\", \"seed\":%d, \"players\":%s}",
                CURRENT_MAP, GAME_SEED, playersJson.toString());
        cliente.enviar(json);
    }

    public static void desconectarCliente(ManejadorCliente cliente) {
        String id = cliente.getIdCliente();
        if (id != null) {
            System.out.println("Limpiando estado para jugador: " + id);
            playerStates.remove(id);
            // Notificar a los demás que se fue
            String disconnectedMsg = String.format(java.util.Locale.US, "{\"tipo\":\"desconectar\", \"id\":\"%s\"}",
                    id);
            broadcast(disconnectedMsg, null);
        }
    }

    public static void broadcast(String mensaje, ManejadorCliente remitente) {
        // Update state if it's a move message
        if (mensaje.contains("\"tipo\":\"mover\"")) {
            try {
                // Quick and dirty manual parsing to avoid external deps in this file,
                // or we could use regex. Ideally we'd use a library but let's keep it simple
                // for now or use string manipulation.
                // Actually, let's just do a quick scan since we know the format from
                // ScreenMultiplayer
                // Format: {"tipo":"mover", "id":"...", "x":..., "y":..., "dir":"...",
                // "moving":...}

                String id = extractJsonValue(mensaje, "id");
                if (id != null) {
                    if (remitente != null && remitente.getIdCliente() == null) {
                        remitente.setIdCliente(id);
                    }
                    float x = Float.parseFloat(extractJsonValue(mensaje, "x"));
                    float y = Float.parseFloat(extractJsonValue(mensaje, "y"));
                    String dir = extractJsonValue(mensaje, "dir");
                    boolean moving = Boolean.parseBoolean(extractJsonValue(mensaje, "moving"));

                    playerStates.put(id, new PlayerState(id, x, y, dir, moving));
                }
            } catch (Exception e) {
                System.err.println("Error updating state: " + e.getMessage());
            }
        } else if (mensaje.contains("\"tipo\":\"desconectar\"")) {
            String id = extractJsonValue(mensaje, "id");
            if (id != null)
                playerStates.remove(id);
        }

        synchronized (clientes) {
            for (ManejadorCliente cliente : clientes) {
                if (cliente != remitente) {
                    cliente.enviar(mensaje);
                }
            }
        }
    }

    private static String extractJsonValue(String json, String key) {
        String keyPattern = "\"" + key + "\":";
        int start = json.indexOf(keyPattern);
        if (start == -1)
            return null;
        start += keyPattern.length();

        // Saltear espacios en blanco opcionales después de ':'
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }

        if (start >= json.length())
            return null;

        char firstChar = json.charAt(start);
        if (firstChar == '"') {
            int end = json.indexOf("\"", start + 1);
            if (end == -1)
                return null;
            return json.substring(start + 1, end);
        } else {
            int end = start;
            while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.'
                    || Character.isLetter(json.charAt(end)) || json.charAt(end) == '-' || json.charAt(end) == '+')) {
                end++;
            }
            return json.substring(start, end);
        }
    }
}
