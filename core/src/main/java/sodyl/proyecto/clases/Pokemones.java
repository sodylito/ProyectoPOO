package sodyl.proyecto.clases;

import java.util.HashMap;
import java.util.Map;

public class Pokemones {
        // hashMap es una colecciòn. Se usa esto y no un ArrayList porque esta
        // estructura relaciona a una clave (el nombre del pokemòn)
        // con un valor (el pokemòn, como tipo). Esto es mucho màs eficiente que
        // recorrer el arreglo
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
                misAtaques.put("Pistola Agua", new TiposAtaque("Pistola Agua", TiposPokemon.AGUA, 45, 25));
                misAtaques.put("Bomba Agua", new TiposAtaque("Bomba Agua", TiposPokemon.AGUA, 70, 15));
                misAtaques.put("Cascada", new TiposAtaque("Cascada", TiposPokemon.AGUA, 100, 10));

                misAtaques.put("Lanzallamas", new TiposAtaque("Lanzallamas", TiposPokemon.FUEGO, 45, 25));
                misAtaques.put("Rayo Fuego", new TiposAtaque("Rayo Fuego", TiposPokemon.FUEGO, 65, 15));
                misAtaques.put("Estallido", new TiposAtaque("Estallido", TiposPokemon.FUEGO, 105, 10));

                misAtaques.put("Bomba Seta", new TiposAtaque("Bomba Seta", TiposPokemon.PLANTA, 55, 25));
                misAtaques.put("Rayo Solar", new TiposAtaque("Rayo Solar", TiposPokemon.PLANTA, 65, 15));
                misAtaques.put("Deslizamiento Herbáceo",
                                new TiposAtaque("Deslizamiento Herbáceo", TiposPokemon.PLANTA, 90, 10));

                misAtaques.put("Chispazo", new TiposAtaque("Chispazo", TiposPokemon.ELECTRICO, 65, 20));
                misAtaques.put("Impactrueno", new TiposAtaque("Impactrueno", TiposPokemon.ELECTRICO, 100, 10));

                misAtaques.put("A Bocajarro", new TiposAtaque("A Bocajarro", TiposPokemon.LUCHA, 120, 5));

                misAtaques.put("Picotazo", new TiposAtaque("Picotazo", TiposPokemon.VOLADOR, 80, 10));
                misAtaques.put("Tornado", new TiposAtaque("Tornado", TiposPokemon.VOLADOR, 115, 5));

                misAtaques.put("Psicorrayo", new TiposAtaque("Psicorrayo", TiposPokemon.PSÍQUICO, 80, 15));
                misAtaques.put("Canto Mortal", new TiposAtaque("Canto Mortal", TiposPokemon.PSÍQUICO, 110, 10));

                misAtaques.put("Burbuja", new TiposAtaque("Burbuja", TiposPokemon.HADA, 65, 15));
                misAtaques.put("Rayo Hada", new TiposAtaque("Rayo Hada", TiposPokemon.HADA, 105, 10));

                misAtaques.put("Derribo", new TiposAtaque("Derribo", TiposPokemon.ROCA, 50, 25));
                misAtaques.put("Avalancha", new TiposAtaque("Avalancha", TiposPokemon.ROCA, 100, 10));

                // Ataques Legendarios para Arceus
                misAtaques.put("Juicio", new TiposAtaque("Juicio", TiposPokemon.NORMAL, 150, 8));
                misAtaques.put("Hiperrayo", new TiposAtaque("Hiperrayo", TiposPokemon.NORMAL, 120, 10));
        }

        // ahora instanciamos a los pokemones
        private static void definirEspecies() {
                // TIER 1: POKÉMON COMUNES (Alta probabilidad de aparición)
                // HP aumentado para mejor balance en batallas tempranas
                Pokemon rowlet = new Pokemon("Rowlet", TiposPokemon.PLANTA, 120,
                                misAtaques.get("Burbuja"), misAtaques.get("Bomba Seta"));
                rowlet.setSpriteFront("gifPokemones/rowletAM2.gif");
                rowlet.setSpriteBack("gifPokemones/rowletAM.gif");
                misPokemones.put("Rowlet", rowlet);

                Pokemon serperior = new Pokemon("Serperior", TiposPokemon.PLANTA, 350,
                                misAtaques.get("Avalancha"), misAtaques.get("Deslizamiento Herbáceo"));
                serperior.setSpriteFront("gifPokemones/serperiorAM.gif");
                serperior.setSpriteBack("gifPokemones/serperiorAM2.gif");
                misPokemones.put("Serperior", serperior);

                Pokemon cyndaquil = new Pokemon("Cyndaquil", TiposPokemon.FUEGO, 115,
                                misAtaques.get("Rayo Fuego"), misAtaques.get("Lanzallamas"));
                cyndaquil.setSpriteFront("gifPokemones/cyndaquilAM2.gif");
                cyndaquil.setSpriteBack("gifPokemones/cyndaquilAM.gif");
                misPokemones.put("Cyndaquil", cyndaquil);

                Pokemon oshawott = new Pokemon("Oshawott", TiposPokemon.AGUA, 125,
                                misAtaques.get("Bomba Agua"), misAtaques.get("Pistola Agua"));
                oshawott.setSpriteFront("gifPokemones/oshawottAM2.gif");
                oshawott.setSpriteBack("gifPokemones/oshawottAM.gif");
                misPokemones.put("Oshawott", oshawott);

                // TIER 2: POKÉMON POCO COMUNES (Probabilidad media)
                Pokemon ivysaur = new Pokemon("Ivysaur", TiposPokemon.PLANTA, 220,
                                misAtaques.get("Impactrueno"), misAtaques.get("Rayo Solar"));
                ivysaur.setSpriteFront("gifPokemones/ivysaurAM.gif");
                ivysaur.setSpriteBack("gifPokemones/ivysaurAM2.gif");
                misPokemones.put("Ivysaur", ivysaur);

                Pokemon flareon = new Pokemon("Flareon", TiposPokemon.FUEGO, 210,
                                misAtaques.get("Rayo Fuego"), misAtaques.get("Picotazo"));
                flareon.setSpriteFront("gifPokemones/flareonAM.gif");
                flareon.setSpriteBack("gifPokemones/flareonAM2.gif");
                misPokemones.put("Flareon", flareon);

                Pokemon vaporeon = new Pokemon("Vaporeon", TiposPokemon.AGUA, 230,
                                misAtaques.get("Picotazo"), misAtaques.get("Bomba Agua"));
                vaporeon.setSpriteFront("gifPokemones/vaporeonAM.gif");
                vaporeon.setSpriteBack("gifPokemones/vaporeonAM2.gif");
                misPokemones.put("Vaporeon", vaporeon);

                Pokemon pikachu = new Pokemon("Pikachu", TiposPokemon.ELECTRICO, 200,
                                misAtaques.get("Impactrueno"), misAtaques.get("Picotazo"));
                pikachu.setSpriteFront("gifPokemones/pikachuAM.gif");
                pikachu.setSpriteBack("gifPokemones/pikachuAM2.gif");
                misPokemones.put("Pikachu", pikachu);

                // TIER 3: POKÉMON RAROS (Baja probabilidad)
                Pokemon jolteon = new Pokemon("Jolteon", TiposPokemon.ELECTRICO, 310,
                                misAtaques.get("Impactrueno"), misAtaques.get("Chispazo"));
                jolteon.setSpriteFront("gifPokemones/jolteonAM.gif");
                jolteon.setSpriteBack("gifPokemones/jolteonAM2.gif");
                misPokemones.put("Jolteon", jolteon);

                Pokemon blastoise = new Pokemon("Blastoise", TiposPokemon.AGUA, 340,
                                misAtaques.get("Cascada"), misAtaques.get("Avalancha"));
                blastoise.setSpriteFront("gifPokemones/blastoiseAM.gif");
                blastoise.setSpriteBack("gifPokemones/blastoiseAM2.gif");
                misPokemones.put("Blastoise", blastoise);

                Pokemon charizard = new Pokemon("Charizard", TiposPokemon.FUEGO, 330,
                                misAtaques.get("Tornado"), misAtaques.get("Estallido"));
                charizard.setSpriteFront("gifPokemones/charizardAM.gif");
                charizard.setSpriteBack("gifPokemones/charizardAM2.gif");
                misPokemones.put("Charizard", charizard);

                Pokemon lucario = new Pokemon("Lucario", TiposPokemon.LUCHA, 350,
                                misAtaques.get("A Bocajarro"), misAtaques.get("Rayo Hada"));
                lucario.setSpriteFront("gifPokemones/lucarioAM.gif");
                lucario.setSpriteBack("gifPokemones/lucarioAM2.gif");
                misPokemones.put("Lucario", lucario);

                Pokemon gyarados = new Pokemon("Gyarados", TiposPokemon.VOLADOR, 320,
                                misAtaques.get("Cascada"), misAtaques.get("Tornado"));
                gyarados.setSpriteFront("gifPokemones/gyaradosAM.gif");
                gyarados.setSpriteBack("gifPokemones/gyaradosAM2.gif");
                misPokemones.put("Gyarados", gyarados);

                Pokemon sylveon = new Pokemon("Sylveon", TiposPokemon.HADA, 360,
                                misAtaques.get("Psicorrayo"), misAtaques.get("Burbuja"));
                sylveon.setSpriteFront("gifPokemones/sylveonAM2.gif");
                sylveon.setSpriteBack("gifPokemones/sylveonAM.gif");
                misPokemones.put("Sylveon", sylveon);

                // TIER 4: POKÉMON LEGENDARIOS (NPCs/Especiales)
                Pokemon mewtwo = new Pokemon("Mewtwo", TiposPokemon.PSÍQUICO, 400,
                                misAtaques.get("Canto Mortal"), misAtaques.get("Rayo Hada"));
                mewtwo.setSpriteFront("gifPokemones/mewtwoAM.gif");
                mewtwo.setSpriteBack("gifPokemones/mewtwoAM2.gif");
                misPokemones.put("Mewtwo", mewtwo);

                // ARCEUS - Pokémon Legendario (Batalla Final)
                Pokemon arceus = new Pokemon("Arceus", TiposPokemon.NORMAL, 500,
                                misAtaques.get("Juicio"), misAtaques.get("Hiperrayo"));
                arceus.setSpriteFront("gifPokemones/arceusAM.gif");
                arceus.setSpriteBack("gifPokemones/arceusAM2.gif");
                misPokemones.put("Arceus", arceus);
        }

        // esta funciòn es para crear el pokemòn en el encuentro/captura
        public static Pokemon getPokemon(String nombre) {
                Pokemon plantilla = misPokemones.get(nombre);
                if (plantilla == null) {
                        return null;
                }

                Pokemon newPokemon = new Pokemon(plantilla.getEspecie(), plantilla.getTipo(), plantilla.getBaseHP(),
                                plantilla.getMovimiento1(), plantilla.getMovimiento2());
                newPokemon.setSpriteFront(plantilla.getSpriteFront());
                newPokemon.setSpriteBack(plantilla.getSpriteBack());
                newPokemon.actualizarAtributos();

                return newPokemon;
        }

        public static java.util.List<String> getAllSpecies() {
                return new java.util.ArrayList<>(misPokemones.keySet());
        }
}
