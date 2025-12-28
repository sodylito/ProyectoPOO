package sodyl.proyecto.clases;

import java.util.HashMap;
import java.util.Map;

public class TablaEficacia {

    // la clave del hashmap es el tipo de pokemón
    // el valor es otro mapa, que se inicializa con otro tipo y "double",
    // representando el multiplicador de daño
    private static final Map<TiposPokemon, Map<TiposPokemon, Double>> tablaEfectividad = new HashMap<>();

    public static void definirTipos() { // definimos los tipos en el mapap, tomados de TiposPokemon
        for (int i = 0; i < TiposPokemon.values().length; i++) {
            TiposPokemon tipo = TiposPokemon.values()[i];
            tablaEfectividad.put(tipo, new HashMap<>());
        }

        /*
         * Reglas Clave (2.0x):
         * Agua > Fuego, Roca
         * Fuego > Planta
         * Planta > Agua, Roca
         * Eléctrico > Agua, Volador
         * Lucha > Normal
         * Hada > Lucha
         * Roca > Volador, Fuego
         * Volador > Planta, Lucha
         * Psíquico > Lucha
         */

        Map<TiposPokemon, Double> agua = tablaEfectividad.get(TiposPokemon.AGUA);
        agua.put(TiposPokemon.FUEGO, 2.0);
        agua.put(TiposPokemon.ROCA, 2.0);
        agua.put(TiposPokemon.PLANTA, 0.5);
        agua.put(TiposPokemon.AGUA, 0.5);

        Map<TiposPokemon, Double> fuego = tablaEfectividad.get(TiposPokemon.FUEGO);
        fuego.put(TiposPokemon.PLANTA, 2.0);
        fuego.put(TiposPokemon.AGUA, 0.5);
        fuego.put(TiposPokemon.ROCA, 0.5);
        fuego.put(TiposPokemon.FUEGO, 0.5);

        Map<TiposPokemon, Double> planta = tablaEfectividad.get(TiposPokemon.PLANTA);
        planta.put(TiposPokemon.AGUA, 2.0);
        planta.put(TiposPokemon.ROCA, 2.0);
        planta.put(TiposPokemon.FUEGO, 0.5);
        planta.put(TiposPokemon.PLANTA, 0.5);
        planta.put(TiposPokemon.VOLADOR, 0.5);

        Map<TiposPokemon, Double> electrico = tablaEfectividad.get(TiposPokemon.ELECTRICO);
        electrico.put(TiposPokemon.AGUA, 2.0);
        electrico.put(TiposPokemon.VOLADOR, 2.0);
        electrico.put(TiposPokemon.PLANTA, 0.5);
        electrico.put(TiposPokemon.ELECTRICO, 0.5);

        Map<TiposPokemon, Double> lucha = tablaEfectividad.get(TiposPokemon.LUCHA);
        lucha.put(TiposPokemon.NORMAL, 2.0);
        lucha.put(TiposPokemon.ROCA, 2.0); // Standard
        lucha.put(TiposPokemon.HADA, 0.5);
        lucha.put(TiposPokemon.VOLADOR, 0.5);
        lucha.put(TiposPokemon.PSÍQUICO, 0.5);

        Map<TiposPokemon, Double> hada = tablaEfectividad.get(TiposPokemon.HADA);
        hada.put(TiposPokemon.LUCHA, 2.0);
        hada.put(TiposPokemon.FUEGO, 0.5);

        Map<TiposPokemon, Double> roca = tablaEfectividad.get(TiposPokemon.ROCA);
        roca.put(TiposPokemon.VOLADOR, 2.0);
        roca.put(TiposPokemon.FUEGO, 2.0);
        roca.put(TiposPokemon.LUCHA, 0.5);

        Map<TiposPokemon, Double> volador = tablaEfectividad.get(TiposPokemon.VOLADOR);
        volador.put(TiposPokemon.PLANTA, 2.0);
        volador.put(TiposPokemon.LUCHA, 2.0);
        volador.put(TiposPokemon.ROCA, 0.5);
        volador.put(TiposPokemon.ELECTRICO, 0.5);

        Map<TiposPokemon, Double> psiquico = tablaEfectividad.get(TiposPokemon.PSÍQUICO);
        psiquico.put(TiposPokemon.LUCHA, 2.0);
        psiquico.put(TiposPokemon.PSÍQUICO, 0.5);
    }

    // esta función devuelve el multiplicador hecho por el tipo, y recibe como
    // parámetros
    // el tipo del pokemón que estás atacando y el del pokemón que está defendiendo
    public static double getMultiplicador(TiposPokemon tipoAtaque, TiposPokemon tipoDefensa) {
        Map<TiposPokemon, Double> ataqueMap = tablaEfectividad.get(tipoAtaque);
        return ataqueMap.getOrDefault(tipoDefensa, 1.0);
    }
}
