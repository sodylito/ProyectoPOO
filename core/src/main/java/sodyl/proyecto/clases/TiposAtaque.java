package sodyl.proyecto.clases;

public class TiposAtaque {

    public String nombre;
    public TiposPokemon tipo;
    public int danoBase;
    public int PP; // eso lo creo para que el usuario no spamee un mismo ataque, no tendría sentido
    // Nueva información para mecánicas tipo Pokémon
    public int precision; // 0-100 (porcentaje), por defecto 100
    public int prioridad; // prioridad del movimiento, por defecto 0

    public TiposAtaque() {
    }

    public TiposAtaque(String nombre, TiposPokemon tipo, int danoBase, int PP) {
        this(nombre, tipo, danoBase, PP, 100, 0);
    }

    public TiposAtaque(String nombre, TiposPokemon tipo, int danoBase, int PP, int precision, int prioridad) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.danoBase = danoBase;
        this.PP = PP;
        this.precision = precision;
        this.prioridad = prioridad;
    }
}
