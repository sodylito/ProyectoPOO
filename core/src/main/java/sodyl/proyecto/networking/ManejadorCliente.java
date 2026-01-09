package sodyl.proyecto.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Set;

public class ManejadorCliente implements Runnable {
    private Socket socket;
    private Set<ManejadorCliente> clientes;
    private PrintWriter out;
    private BufferedReader in;
    private String idCliente;

    public ManejadorCliente(Socket socket, Set<ManejadorCliente> clientes) {
        this.socket = socket;
        this.clientes = clientes;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String mensaje;
            while ((mensaje = in.readLine()) != null) {
                // El servidor simplemente retransmite lo que recibe a todos los demás
                ServidorPokemon.broadcast(mensaje, this);
            }
        } catch (IOException e) {
            System.err.println("Cliente desconectado: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            clientes.remove(this);
        }
    }

    public void enviar(String mensaje) {
        if (out != null) {
            out.println(mensaje);
        }
    }
}
