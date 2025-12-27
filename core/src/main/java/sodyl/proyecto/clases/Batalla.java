package sodyl.proyecto.clases;

import java.util.Random;

/**
 * Clase que maneja la lógica de batalla entre Pokémon.
 */
public class Batalla {
    private Pokemon pokemonJugador;
    private Pokemon pokemonEnemigo;
    private Inventario inventario;
    private Random random;
    private boolean batallaTerminada;
    private String ganador; // "JUGADOR" o "ENEMIGO"

    public Batalla(Pokemon pokemonJugador, Pokemon pokemonEnemigo, Inventario inventario) {
        this.pokemonJugador = pokemonJugador;
        this.pokemonEnemigo = pokemonEnemigo;
        this.inventario = inventario;
        this.random = new Random();
        this.batallaTerminada = false;
        this.ganador = null;
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
        TiposAtaque mJ = (movimientoJugadorNum == 1) ? pokemonJugador.getMovimiento1()
                : pokemonJugador.getMovimiento2();
        TiposAtaque mE = (movimientoEnemigoNum == 1) ? pokemonEnemigo.getMovimiento1()
                : pokemonEnemigo.getMovimiento2();

        if (mJ.prioridad != mE.prioridad) {
            return mJ.prioridad > mE.prioridad;
        }
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

        // Determinar orden por prioridad y luego por velocidad
        int prioridadJugador = movimientoJugador.prioridad;
        int prioridadEnemigo = movimientoEnemigo.prioridad;

        boolean jugadorAtacaPrimero;
        if (prioridadJugador != prioridadEnemigo) {
            jugadorAtacaPrimero = prioridadJugador > prioridadEnemigo;
        } else {
            jugadorAtacaPrimero = pokemonJugador.getVelocidad() >= pokemonEnemigo.getVelocidad();
        }

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
     * Intenta capturar al Pokémon enemigo usando una Pokéball.
     * 
     * @return true si la captura fue exitosa, false en caso contrario
     */
    public boolean intentarCaptura() {
        // Permitir captura solo durante batalla activa (no terminada) y con Pokemon
        // vivo (HP > 0)
        if (!batallaTerminada && pokemonEnemigo.getActualHP() > 0) {
            // Verificar si hay pokeballs (ID 101)
            if (inventario.getQuantity(101) <= 0) {
                return false;
            }

            // Consumir pokeball
            inventario.removeObjeto(101, 1);

            // Fórmula simple de captura: más fácil si el HP es bajo
            float hpPorcentaje = (float) pokemonEnemigo.getActualHP() / pokemonEnemigo.getMaxHp();
            float probabilidadCaptura = (1.0f - hpPorcentaje) * 0.5f; // Máximo 50% si HP = 0

            boolean capturado = random.nextFloat() < probabilidadCaptura;

            if (capturado) {
                Pokedex.addCollected(pokemonEnemigo);
                Pokedex.addResearchPoints(pokemonEnemigo.getEspecie(), 2);
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

        // 1) Chequeo de precisión
        int precision = movimiento.precision; // 0-100
        int roll = random.nextInt(100) + 1; // 1..100
        if (roll > precision) {
            salida.append("\n¡El ataque falló!");
            return salida.toString();
        }

        // 2) Fórmula de daño inspirada en Pokémon (simplificada)
        int nivel = atacante.getNivel();
        int poder = movimiento.danoBase; // usar daño base como 'power'
        int ataque = Math.max(1, atacante.getAttackValue());
        int defensa = Math.max(1, defensor.getDefenseValue());

        double base = (((2.0 * nivel) / 5.0 + 2.0) * poder * ((double) ataque / (double) defensa)) / 50.0 + 2.0;

        // Critical hit
        boolean crit = random.nextDouble() < 0.0625; // ~6.25%
        double critMultiplier = crit ? 1.5 : 1.0;

        // STAB (same-type attack bonus)
        double stab = (movimiento.tipo == atacante.getTipo()) ? 1.5 : 1.0;

        // Type effectiveness
        double efectividad = TablaEficacia.getMultiplicador(movimiento.tipo, defensor.getTipo());

        // Random variation 85-100%
        double variacion = 0.85 + random.nextDouble() * 0.15;

        int dano = (int) Math.max(1, Math.floor(base * critMultiplier * stab * efectividad * variacion));

        if (crit)
            salida.append(" ¡Golpe crítico!");
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
                // Ganar batalla: +1 nivel de investigación
                Pokedex.addResearchPoints(defensor.getEspecie(), 1);

                // Recompensa aleatoria
                int rewardId = random.nextBoolean() ? 1 : 2; // 1: Bonguri, 2: Guijarro Rojo
                inventario.addObjeto(rewardId, 1);
                salida.append("\n¡Has obtenido una recompensa!");
            }
        }

        return salida.toString();
    }
}
