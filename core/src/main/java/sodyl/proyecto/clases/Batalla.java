package sodyl.proyecto.clases;

import java.util.Random;

/**
 * Clase que maneja la lógica de batalla entre Pokémon.
 */
// UNIDAD 2: COHESIÓN Y ACOPLAMIENTO
// Esta clase presenta una ALTA COHESIÓN porque se encarga únicamente de la
// lógica de combate.
// El acoplamiento se mantiene controlado al recibir las dependencias (Pokemon,
// Inventario)
// por constructor, facilitando la mantenibilidad.
public class Batalla {
    private Pokemon pokemonJugador;
    private Pokemon pokemonEnemigo;
    private Inventario inventario;
    private Random random;
    private boolean batallaTerminada;
    private String ganador; // "JUGADOR" o "ENEMIGO"

    private float playerDamageMultiplier = 1.0f;

    public Batalla(Pokemon pokemonJugador, Pokemon pokemonEnemigo, Inventario inventario) {
        this.pokemonJugador = pokemonJugador;
        this.pokemonEnemigo = pokemonEnemigo;
        this.inventario = inventario;
        this.random = new Random();
        this.batallaTerminada = false;
        this.ganador = null;
    }

    public void activatePowerUp() {
        this.playerDamageMultiplier = 1.5f;
    }

    /**
     * El jugador ataca al enemigo usando el movimiento especificado (1 o 2).
     * 
     * @param movimientoNumero 1 para movimiento1, 2 para movimiento2
     * @return Mensaje descriptivo del ataque
     */
    public void setPokemonJugador(Pokemon pokemonJugador) {
        this.pokemonJugador = pokemonJugador;
    }

    public void setPokemonEnemigo(Pokemon pokemonEnemigo) {
        this.pokemonEnemigo = pokemonEnemigo;
    }

    /**
     * Determina quién ataca primero.
     * 
     * @param movimientoJugadorNum 1 o 2
     * @param movimientoEnemigoNum 1 o 2
     * @return true si el jugador ataca primero
     */
    public boolean playerAttacksFirst(int movimientoJugadorNum, int movimientoEnemigoNum) {
        return pokemonJugador.getVelocidad() >= pokemonEnemigo.getVelocidad();
    }

    /**
     * Ejecuta un ataque individual.
     * 
     * @param atacante         Pokemon que ataca
     * @param defensor         Pokemon que defiende
     * @param movimientoNumero 1 o 2
     * @return Mensaje del resultado
     */
    /**
     * UNIDAD 1: PASE DE MENSAJES (Messaging)
     * En POO, los "mensajes" son llamadas a métodos. Aquí 'Batalla' envía un
     * mensaje
     * al objeto 'atacante' (usarPPMovimiento) y luego delega la ejecución al método
     * interno.
     */
    public String executeAttack(Pokemon atacante, Pokemon defensor, int movimientoNumero) {
        if (batallaTerminada)
            return "";

        TiposAtaque movimiento = (movimientoNumero == 1) ? atacante.getMovimiento1() : atacante.getMovimiento2();

        // Verificar PP
        if (!atacante.usarPPMovimiento(movimientoNumero)) {
            return atacante.getEspecie() + " no tiene PP para " + movimiento.nombre + ".";
        }

        return ejecutarMovimiento(atacante, defensor, movimiento);
    }

    /**
     * El jugador ataca al enemigo usando el movimiento especificado (1 o 2).
     * MANTENIDO POR COMPATIBILIDAD, PERO SE RECOMIENDA USAR executeAttack EN EL
     * SCREEN.
     * 
     * @param movimientoNumero 1 para movimiento1, 2 para movimiento2
     * @return Mensaje descriptivo del ataque
     */
    public String atacarEnemigo(int movimientoNumero) {
        if (batallaTerminada) {
            return "La batalla ya ha terminado.";
        }

        // Nuevo sistema: el jugador escoge un movimiento, el enemigo elige uno
        // aleatorio.
        TiposAtaque movimientoJugador = (movimientoNumero == 1) ? pokemonJugador.getMovimiento1()
                : pokemonJugador.getMovimiento2();
        int movimientoEnemigoNum = random.nextBoolean() ? 1 : 2;
        TiposAtaque movimientoEnemigo = (movimientoEnemigoNum == 1) ? pokemonEnemigo.getMovimiento1()
                : pokemonEnemigo.getMovimiento2();

        // Verificar PP y consumir (intento de uso)
        boolean jugadorPuedeUsar = pokemonJugador.usarPPMovimiento(movimientoNumero);
        boolean enemigoPuedeUsar = pokemonEnemigo.usarPPMovimiento(movimientoEnemigoNum);

        if (!jugadorPuedeUsar) {
            return pokemonJugador.getEspecie() + " no puede usar " + movimientoJugador.nombre + ". ¡No quedan PP!";
        }

        // Determinar orden por velocidad
        boolean jugadorAtacaPrimero = pokemonJugador.getVelocidad() >= pokemonEnemigo.getVelocidad();

        StringBuilder sb = new StringBuilder();

        // Ejecutar ataques en orden
        if (jugadorAtacaPrimero) {
            sb.append(ejecutarMovimiento(pokemonJugador, pokemonEnemigo, movimientoJugador));
            if (!batallaTerminada) {
                sb.append("\n");
                if (enemigoPuedeUsar)
                    sb.append(ejecutarMovimiento(pokemonEnemigo, pokemonJugador, movimientoEnemigo));
                else
                    sb.append(pokemonEnemigo.getEspecie()).append(" no tiene PP para atacar.");
            }
        } else {
            // enemigo primero
            if (enemigoPuedeUsar)
                sb.append(ejecutarMovimiento(pokemonEnemigo, pokemonJugador, movimientoEnemigo));
            else
                sb.append(pokemonEnemigo.getEspecie()).append(" no tiene PP para atacar.");

            if (!batallaTerminada) {
                sb.append("\n");
                sb.append(ejecutarMovimiento(pokemonJugador, pokemonEnemigo, movimientoJugador));
            }
        }

        return sb.toString();
    }

    /**
     * El enemigo ataca al jugador usando un movimiento aleatorio.
     * 
     * @return Mensaje descriptivo del ataque
     */
    public String ataqueEnemigo() {
        if (batallaTerminada) {
            return "";
        }

        // Mantener compatibilidad: elegir movimiento aleatorio y usar la nueva función
        int movimientoNumero = random.nextBoolean() ? 1 : 2;
        TiposAtaque movimiento = (movimientoNumero == 1) ? pokemonEnemigo.getMovimiento1()
                : pokemonEnemigo.getMovimiento2();
        boolean pudoUsar = pokemonEnemigo.usarPPMovimiento(movimientoNumero);
        if (!pudoUsar)
            return pokemonEnemigo.getEspecie() + " no puede atacar. ¡No quedan PP!";
        return ejecutarMovimiento(pokemonEnemigo, pokemonJugador, movimiento);
    }

    public Pokemon getPokemonJugador() {
        return pokemonJugador;
    }

    public Pokemon getPokemonEnemigo() {
        return pokemonEnemigo;
    }

    public boolean isBatallaTerminada() {
        return batallaTerminada;
    }

    public String getGanador() {
        return ganador;
    }

    /**
     * Intenta capturar al Pokémon enemigo usando un objeto de captura.
     * 
     * @param itemId ID del objeto a usar (101 para Pokéball, 106 para MasterBall)
     * @return true si la captura fue exitosa, false en caso contrario
     */
    public boolean intentarCaptura(int itemId) {
        // Permitir captura solo durante batalla activa (no terminada) y con Pokemon
        // vivo (HP > 0)
        if (!batallaTerminada && pokemonEnemigo.getActualHP() > 0) {
            // Verificar si hay el objeto solicitado
            if (inventario.getQuantity(itemId) <= 0) {
                return false;
            }

            // Consumir objeto
            inventario.removeObjeto(itemId, 1);

            boolean capturado = false;
            float hpPorcentaje = (float) pokemonEnemigo.getActualHP() / pokemonEnemigo.getMaxHp();
            boolean isArceus = pokemonEnemigo.getEspecie().equalsIgnoreCase("Arceus");

            if (itemId == 106) {
                // Lógica MasterBall
                if (isArceus) {
                    // Arceus: 100% si HP <= 25%
                    if (hpPorcentaje <= 0.25f) {
                        capturado = true;
                    }
                } else {
                    // General: 100% si HP <= 75%
                    if (hpPorcentaje <= 0.75f) {
                        capturado = true;
                    }
                }
            } else if (itemId == 101) {
                // Pokéball normal
                if (isArceus) {
                    // A Arceus no se le puede capturar con una pokebola
                    capturado = false;
                } else {
                    // Solo captura con 25% de vida o menos
                    if (hpPorcentaje <= 0.25f) {
                        capturado = true;
                    }
                }
            }

            return capturado;
        }
        return false;
    }

    // Rutina que ejecuta un movimiento de 'atacante' sobre 'defensor' y devuelve un
    // mensaje descriptivo.
    private String ejecutarMovimiento(Pokemon atacante, Pokemon defensor, TiposAtaque movimiento) {
        if (batallaTerminada)
            return "";

        StringBuilder salida = new StringBuilder();
        salida.append(atacante.getEspecie()).append(" usó ").append(movimiento.nombre).append("!");

        // 2) Fórmula de daño solicitada
        int nivel = atacante.getNivel();
        int poder = movimiento.danoBase;

        // Daño = (Poder_Ataque * (Nivel_Pokemon + 5)) / 10
        int danoBaseCalculado = (poder * (nivel + 5)) / 10;

        // Aplicar potenciador si es el jugador el que ataca
        if (atacante == pokemonJugador) {
            danoBaseCalculado = (int) (danoBaseCalculado * playerDamageMultiplier);
        }

        // Type effectiveness
        double efectividad = TablaEficacia.getMultiplicador(movimiento.tipo, defensor.getTipo());

        // Aplica el multiplicador de tipo AL FINAL de este cálculo
        int dano = (int) (danoBaseCalculado * efectividad);

        if (efectividad > 1.0)
            salida.append(" ¡Es súper efectivo!");
        else if (efectividad < 1.0 && efectividad > 0.0)
            salida.append(" No es muy efectivo...");
        else if (efectividad == 0.0)
            salida.append(" ¡No tuvo efecto!");

        // Aplicar daño

        if (efectividad == 0.0) {
            // no hace daño
        } else {
            int nuevoHP = Math.max(0, defensor.getActualHP() - dano);
            defensor.setActualHP(nuevoHP);
            salida.append("\n").append(defensor.getEspecie()).append(" recibió ").append(dano).append(" de daño.");
        }

        if (defensor.getActualHP() <= 0) {
            batallaTerminada = true;
            ganador = atacante == pokemonJugador ? "JUGADOR" : "ENEMIGO";
            salida.append("\n¡").append(defensor.getEspecie()).append(" se debilitó!");

            if (ganador.equals("JUGADOR")) {
                // Battle results and rewards are now handled in ScreenBatalla.java
            }
        }

        return salida.toString();
    }
}
