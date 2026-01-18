package sodyl.proyecto.clases;

public class TiposAtaque {

    public String nombre;
    public TiposPokemon tipo;
    public int danoBase;
    public int PP; // entero que define el límite de ataques para un movimiento

    public TiposAtaque() {
    }

    // Constructor para crear un ataque con todos los parámetros, lo usamos en
    // Pokemones
    public TiposAtaque(String nombre, TiposPokemon tipo, int danoBase, int PP) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.danoBase = danoBase;
        this.PP = PP;
    }
}
