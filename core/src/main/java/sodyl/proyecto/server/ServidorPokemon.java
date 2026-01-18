package sodyl.proyecto.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ServidorPokemon {
    private static final int PORT = 5000;
    private static Set<PrintWriter> clientWriters = Collections.synchronizedSet(new HashSet<>());
    private static Map<String, PrintWriter> clientMap = new ConcurrentHashMap<>();
    private static Map<String, Long> depletedResources = new ConcurrentHashMap<>();
    private static boolean gameStarted = false;

    public static void main(String[] args) {
        System.out.println("Servidor Pokémon iniciado en puerto " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String clientName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                clientWriters.add(out);
                broadcast("{\"tipo\":\"jugadores_conectados\", \"count\":" + clientWriters.size() + "}");

                if (clientWriters.size() >= 2 && !gameStarted) {
                    gameStarted = true;
                    long seed = new Random().nextLong();
                    broadcast("{\"tipo\":\"START_GAME\", \"seed\":" + seed + "}");
                    System.out.println("Partida iniciada con 2 jugadores. Semilla: " + seed);
                }

                String message;
                while ((message = in.readLine()) != null) {
                    processMessage(message);
                }
            } catch (IOException e) {
                System.out.println("Error en cliente: " + e.getMessage());
            } finally {
                if (out != null)
                    clientWriters.remove(out);
                if (clientName != null) {
                    clientMap.remove(clientName);
                    broadcast("{\"tipo\":\"desconectar\", \"id\":\"" + clientName + "\"}");
                }
                broadcast("{\"tipo\":\"jugadores_conectados\", \"count\":" + clientWriters.size() + "}");
                if (clientWriters.isEmpty()) {
                    gameStarted = false;
                    System.out.println("Todos los jugadores desconectados. Reiniciando estado del servidor.");
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }

        private void processMessage(String jsonMsg) {
            if (clientName == null && jsonMsg.contains("\"id\"")) {
                int start = jsonMsg.indexOf("\"id\":\"") + 6;
                int end = jsonMsg.indexOf("\"", start);
                if (start > 5 && end > start) {
                    clientName = jsonMsg.substring(start, end);
                    clientMap.put(clientName, out);
                    System.out.println("Jugador identificado: " + clientName);
                }
            }

            if (jsonMsg.contains("\"tipo\": \"duel_request\"") || jsonMsg.contains("\"tipo\":\"duel_request\"")) {
                String target = extractValue(jsonMsg, "target");
                if (target != null && clientMap.containsKey(target)) {
                    if (!jsonMsg.contains("\"id\"")) {
                    }
                    clientMap.get(target).println(jsonMsg);
                    return;
                }
            }

            if (jsonMsg.contains("\"tipo\": \"duel_accept\"") || jsonMsg.contains("\"tipo\":\"duel_accept\"")) {
                String target = extractValue(jsonMsg, "target");
                if (target != null && clientMap.containsKey(target)) {
                    clientMap.get(target).println(jsonMsg);
                    return;
                }
            }

            if (jsonMsg.contains("\"tipo\":\"item_collected\"") || jsonMsg.contains("\"tipo\": \"item_collected\"")) {
                String itemId = extractValue(jsonMsg, "itemId");
                if (itemId != null) {
                    if (isDepleted(itemId)) {
                    } else {
                        depletedResources.put(itemId, System.currentTimeMillis() + 60000);
                        broadcast(jsonMsg);
                    }
                }
                return;
            }

            broadcast(jsonMsg);
        }

        private boolean isDepleted(String itemId) {
            Long time = depletedResources.get(itemId);
            return time != null && time > System.currentTimeMillis();
        }

        private String extractValue(String json, String key) {
            String search = "\"" + key + "\":\"";
            int start = json.indexOf(search);
            if (start == -1) {
                search = "\"" + key + "\": \"";
                start = json.indexOf(search);
            }
            if (start != -1) {
                start += search.length();
                int end = json.indexOf("\"", start);
                if (end != -1)
                    return json.substring(start, end);
            }
            return null;
        }

        private void broadcast(String message) {
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println(message);
                }
            }
        }
    }
}
