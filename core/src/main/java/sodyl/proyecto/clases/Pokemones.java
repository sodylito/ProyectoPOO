package sodyl.proyecto.clases;

import java.util.HashMap;
import java.util.Map;

public class Pokemones {

        // UNIDAD 10: COLECCIONES (Collections)
        // HashMap es una estructura de datos que asocia una clave única con un valor.
        // Se usa aquí por su alta eficiencia (O(1)) para buscar un Pokémon por su
        // nombre,
        // evitando tener que recorrer una lista completa (como en un ArrayList).
        private static final Map<String, Pokemon> misPokemones = new HashMap<>();
        private static final Map<String, TiposAtaque> misAtaques = new HashMap<>();

        public static void initialize() {
                definirAtaques();
                definirEspecies();
                TablaEficacia.definirTipos();
        }

        // definimos los ataques en el hashmap, para despuès asignarselos a los
        // pokemones
        private static void definirAtaques() { // vamos a decir que el pokemon màs debil tiene 200HP
                // 45-55 Poder
                misAtaques.put("Pistola Agua", new TiposAtaque("Pistola Agua", TiposPokemon.AGUA, 55, 25));
                misAtaques.put("Lanzallamas", new TiposAtaque("Lanzallamas", TiposPokemon.FUEGO, 55, 25));
                misAtaques.put("Bomba Seta", new TiposAtaque("Bomba Seta", TiposPokemon.PLANTA, 55, 25));
                misAtaques.put("Derribo", new TiposAtaque("Derribo", TiposPokemon.ROCA, 55, 25));

                // 65-70 Poder
                misAtaques.put("Bomba Agua", new TiposAtaque("Bomba Agua", TiposPokemon.AGUA, 70, 15));
                misAtaques.put("Rayo Fuego", new TiposAtaque("Rayo Fuego", TiposPokemon.FUEGO, 70, 15));
                misAtaques.put("Rayo Solar", new TiposAtaque("Rayo Solar", TiposPokemon.PLANTA, 70, 15));
                misAtaques.put("Chispazo", new TiposAtaque("Chispazo", TiposPokemon.ELECTRICO, 70, 20));
                misAtaques.put("Burbuja", new TiposAtaque("Burbuja", TiposPokemon.HADA, 70, 15));

                // 80-90 Poder
                misAtaques.put("Picotazo", new TiposAtaque("Picotazo", TiposPokemon.VOLADOR, 90, 10));
                misAtaques.put("Psicorrayo", new TiposAtaque("Psicorrayo", TiposPokemon.PSÍQUICO, 90, 15));
                misAtaques.put("Deslizamiento Herbáceo",
                                new TiposAtaque("Deslizamiento Herbáceo", TiposPokemon.PLANTA, 90, 10));

                // 100-105 Poder
                misAtaques.put("Cascada", new TiposAtaque("Cascada", TiposPokemon.AGUA, 105, 10));
                misAtaques.put("Impactrueno", new TiposAtaque("Impactrueno", TiposPokemon.ELECTRICO, 105, 10));
                misAtaques.put("Avalancha", new TiposAtaque("Avalancha", TiposPokemon.ROCA, 105, 10));
                misAtaques.put("Estallido", new TiposAtaque("Estallido", TiposPokemon.FUEGO, 105, 10));
                misAtaques.put("Rayo Hada", new TiposAtaque("Rayo Hada", TiposPokemon.HADA, 105, 10));

                // 110-120 Poder
                misAtaques.put("Canto Mortal", new TiposAtaque("Canto Mortal", TiposPokemon.PSÍQUICO, 120, 10));
                misAtaques.put("A Bocajarro", new TiposAtaque("A Bocajarro", TiposPokemon.LUCHA, 120, 5));
                misAtaques.put("Hiperrayo", new TiposAtaque("Hiperrayo", TiposPokemon.NORMAL, 120, 10));
                misAtaques.put("Tornado", new TiposAtaque("Tornado", TiposPokemon.VOLADOR, 120, 5));

                // 150 Poder
                misAtaques.put("Juicio", new TiposAtaque("Juicio", TiposPokemon.NORMAL, 150, 8));
        }

        // ahora instanciamos a los pokemones
        /*
         * Tier 1 (Básico): HP_Base=50, Crecimiento_HP=15
         * Tier 2 (Intermedio): HP_Base=70, Crecimiento_HP=20
         * Tier 3 (Avanzado): HP_Base=100, Crecimiento_HP=25
         * Tier 4 (Héroes): HP_Base=150, Crecimiento_HP=30
         * Tier 5 (BOSS FINAL): HP_Base=500, Crecimiento_HP=100
         */
        private static void definirEspecies() {
                // TIER 1: POKÉMON COMUNES (Base HP: 50, Growth: 15)
                Pokemon rowlet = new Pokemon("Rowlet", TiposPokemon.PLANTA, 50, 15,
                                misAtaques.get("Burbuja"), misAtaques.get("Bomba Seta")); // Modif: Burbuja (Hada) ->
                                                                                          // Planta? No, mantener
                                                                                          // asignacion
                rowlet.setSpriteFront("gifPokemones/rowletAM2.gif");
                rowlet.setSpriteBack("gifPokemones/rowletAM.gif");
                misPokemones.put("Rowlet", rowlet);

                Pokemon oshawott = new Pokemon("Oshawott", TiposPokemon.AGUA, 50, 15,
                                misAtaques.get("Bomba Agua"), misAtaques.get("Pistola Agua"));
                oshawott.setSpriteFront("gifPokemones/oshawottAM2.gif");
                oshawott.setSpriteBack("gifPokemones/oshawottAM.gif");
                misPokemones.put("Oshawott", oshawott);

                Pokemon cyndaquil = new Pokemon("Cyndaquil", TiposPokemon.FUEGO, 50, 15,
                                misAtaques.get("Rayo Fuego"), misAtaques.get("Lanzallamas"));
                cyndaquil.setSpriteFront("gifPokemones/cyndaquilAM2.gif");
                cyndaquil.setSpriteBack("gifPokemones/cyndaquilAM.gif");
                misPokemones.put("Cyndaquil", cyndaquil);

                // TIER 2: POKÉMON POCO COMUNES (Base HP: 70, Growth: 20)
                Pokemon ivysaur = new Pokemon("Ivysaur", TiposPokemon.PLANTA, 70, 20,
                                misAtaques.get("Impactrueno"), misAtaques.get("Rayo Solar"));
                ivysaur.setSpriteFront("gifPokemones/ivysaurAM.gif");
                ivysaur.setSpriteBack("gifPokemones/ivysaurAM2.gif");
                misPokemones.put("Ivysaur", ivysaur); // Nota: Ivysaur con Impactrueno? Se mantiene del original

                Pokemon pikachu = new Pokemon("Pikachu", TiposPokemon.ELECTRICO, 70, 20,
                                misAtaques.get("Impactrueno"), misAtaques.get("Picotazo"));
                pikachu.setSpriteFront("gifPokemones/pikachuAM.gif");
                pikachu.setSpriteBack("gifPokemones/pikachuAM2.gif");
                misPokemones.put("Pikachu", pikachu);

                Pokemon vaporeon = new Pokemon("Vaporeon", TiposPokemon.AGUA, 70, 20,
                                misAtaques.get("Picotazo"), misAtaques.get("Bomba Agua"));
                vaporeon.setSpriteFront("gifPokemones/vaporeonAM.gif");
                vaporeon.setSpriteBack("gifPokemones/vaporeonAM2.gif");
                misPokemones.put("Vaporeon", vaporeon);

                Pokemon jolteon = new Pokemon("Jolteon", TiposPokemon.ELECTRICO, 70, 20,
                                misAtaques.get("Impactrueno"), misAtaques.get("Chispazo"));
                jolteon.setSpriteFront("gifPokemones/jolteonAM.gif");
                jolteon.setSpriteBack("gifPokemones/jolteonAM2.gif");
                misPokemones.put("Jolteon", jolteon);

                Pokemon flareon = new Pokemon("Flareon", TiposPokemon.FUEGO, 70, 20,
                                misAtaques.get("Rayo Fuego"), misAtaques.get("Picotazo"));
                flareon.setSpriteFront("gifPokemones/flareonAM.gif");
                flareon.setSpriteBack("gifPokemones/flareonAM2.gif");
                misPokemones.put("Flareon", flareon);

                Pokemon sylveon = new Pokemon("Sylveon", TiposPokemon.HADA, 70, 20,
                                misAtaques.get("Psicorrayo"), misAtaques.get("Burbuja"));
                sylveon.setSpriteFront("gifPokemones/sylveonAM2.gif");
                sylveon.setSpriteBack("gifPokemones/sylveonAM.gif");
                misPokemones.put("Sylveon", sylveon);

                // TIER 3: POKÉMON AVANZADOS (Base HP: 100, Growth: 25)
                Pokemon serperior = new Pokemon("Serperior", TiposPokemon.PLANTA, 100, 25,
                                misAtaques.get("Avalancha"), misAtaques.get("Deslizamiento Herbáceo"));
                serperior.setSpriteFront("gifPokemones/serperiorAM.gif");
                serperior.setSpriteBack("gifPokemones/serperiorAM2.gif");
                misPokemones.put("Serperior", serperior);

                Pokemon blastoise = new Pokemon("Blastoise", TiposPokemon.AGUA, 100, 25,
                                misAtaques.get("Cascada"), misAtaques.get("Avalancha"));
                blastoise.setSpriteFront("gifPokemones/blastoiseAM.gif");
                blastoise.setSpriteBack("gifPokemones/blastoiseAM2.gif");
                misPokemones.put("Blastoise", blastoise);

                Pokemon charizard = new Pokemon("Charizard", TiposPokemon.FUEGO, 100, 25,
                                misAtaques.get("Tornado"), misAtaques.get("Estallido"));
                charizard.setSpriteFront("gifPokemones/charizardAM.gif");
                charizard.setSpriteBack("gifPokemones/charizardAM2.gif");
                misPokemones.put("Charizard", charizard);

                Pokemon gyarados = new Pokemon("Gyarados", TiposPokemon.VOLADOR, 100, 25,
                                misAtaques.get("Cascada"), misAtaques.get("Tornado"));
                gyarados.setSpriteFront("gifPokemones/gyaradosAM.gif");
                gyarados.setSpriteBack("gifPokemones/gyaradosAM2.gif");
                misPokemones.put("Gyarados", gyarados);

                // TIER 4: HÉROES (Base HP: 150, Growth: 30)
                Pokemon mewtwo = new Pokemon("Mewtwo", TiposPokemon.PSÍQUICO, 150, 30,
                                misAtaques.get("Canto Mortal"), misAtaques.get("Rayo Hada"));
                mewtwo.setSpriteFront("gifPokemones/mewtwoAM.gif");
                mewtwo.setSpriteBack("gifPokemones/mewtwoAM2.gif");
                misPokemones.put("Mewtwo", mewtwo);

                Pokemon lucario = new Pokemon("Lucario", TiposPokemon.LUCHA, 150, 30,
                                misAtaques.get("A Bocajarro"), misAtaques.get("Rayo Hada"));
                lucario.setSpriteFront("gifPokemones/lucarioAM.gif");
                lucario.setSpriteBack("gifPokemones/lucarioAM2.gif");
                misPokemones.put("Lucario", lucario);

                // TIER 5: BOSS FINAL (Base HP: 500, Growth: 100)
                Pokemon arceus = new Pokemon("Arceus", TiposPokemon.NORMAL, 500, 100,
                                misAtaques.get("Juicio"), misAtaques.get("Hiperrayo"));
                arceus.setSpriteFront("gifPokemones/arceusAM.gif");
                arceus.setSpriteBack("gifPokemones/arceusAM2.gif");
                misPokemones.put("Arceus", arceus);
        }

        // UNIDAD 9: PATRONES DE DISEÑO - PROTOTYPE / FACTORY
        // Este método actúa como una fábrica de objetos que utiliza el patrón
        // Prototype.
        // En lugar de crear un objeto desde cero, buscamos una "plantilla" (template)
        // y creamos una copia (instancia nueva) con los mismos valores base.
        public static Pokemon getPokemon(String nombre) {
                Pokemon plantilla = misPokemones.get(nombre);
                if (plantilla == null) {
                        return null;
                }

                // Creamos una nueva instancia basada en la plantilla (Clonación lógica)
                Pokemon newPokemon = new Pokemon(plantilla.getEspecie(), plantilla.getTipo(), plantilla.getBaseHP(),
                                plantilla.getGrowthHP(), plantilla.getMovimiento1(), plantilla.getMovimiento2());
                newPokemon.setSpriteFront(plantilla.getSpriteFront());
                newPokemon.setSpriteBack(plantilla.getSpriteBack());
                newPokemon.actualizarAtributos();

                return newPokemon;
        }

        public static java.util.List<TiposAtaque> getAllAttacks() {
                return new java.util.ArrayList<>(misAtaques.values());
        }

        public static java.util.List<String> getAllSpecies() {
                return new java.util.ArrayList<>(misPokemones.keySet());
        }
}
