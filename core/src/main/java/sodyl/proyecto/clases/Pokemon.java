package sodyl.proyecto.clases;

// UNIDAD 1 & 2: Definición de CLASE y ABSTRACCIÓN
// Esta clase representa la abstracción de un Pokémon en el juego.
// Aquí definimos los atributos (características) y métodos (comportamientos).
public class Pokemon {

    // UNIDAD 2: ENCAPSULAMIENTO
    // Usamos el modificador 'private' para proteger los datos de la clase.
    // Esto asegura que el estado interno solo sea modificado a través de métodos
    // controlados (Setters/Getters).
    private String especie;
    private TiposPokemon tipo;
    private int baseHP; // Atributo: HP base al capturarlo
    private int growthHP; // Atributo: Crecimiento por nivel
    private TiposAtaque movimiento1;
    private TiposAtaque movimiento2;

    private int nivel;
    private int actualHP;

    private int maxHp;
    private int danoAtaque;
    private int danoDefensa;
    private int velocidad;

    private int ppM1;
    private int ppM2;

    private String spriteFront;
    private String spriteBack;

    // UNIDAD 1: CONSTRUCTOR POR DEFECTO
    // Permite instanciar un objeto sin valores iniciales (útil para serialización).
    public Pokemon() {
    }

    // UNIDAD 1: CONSTRUCTOR PARAMETRIZADO
    // Permite inicializar el objeto con valores específicos al momento de su
    // creación.
    // Usamos 'this' para diferenciar los atributos de la clase de los parámetros
    // del constructor.
    public Pokemon(String especie, TiposPokemon tipo, int baseHP, int growthHP, TiposAtaque movimiento1,
            TiposAtaque movimiento2) {
        this.especie = especie;
        this.tipo = tipo;
        this.baseHP = baseHP;
        this.growthHP = growthHP;
        this.movimiento1 = movimiento1;
        this.movimiento2 = movimiento2;
        this.nivel = 1;

        // Lógica de inicialización: Cálculo de atributos basados en el nivel
        this.maxHp = baseHP + (nivel * growthHP);
        this.danoAtaque = movimiento1.danoBase + (nivel * 2);
        this.danoDefensa = movimiento2.danoBase + (nivel * 2);
        this.velocidad = 10 + (nivel * 2);

        this.actualHP = this.maxHp;

        this.ppM1 = movimiento1.PP;
        this.ppM2 = movimiento2.PP;
    }

    public void actualizarAtributos() {
        int effectiveLevel = Math.max(1, this.nivel);
        this.maxHp = this.baseHP + (effectiveLevel * this.growthHP);
        this.danoAtaque = this.movimiento1.danoBase + (effectiveLevel * 2);
        this.danoDefensa = this.movimiento2.danoBase + (effectiveLevel * 2);
        this.velocidad = 10 + (effectiveLevel * 2);
    }

    // UNIDAD 2: GETTERS Y SETTERS
    // Métodos públicos para acceder y modificar atributos privados de forma segura.
    public String getSpriteFront() {
        return spriteFront;
    }

    public void setSpriteFront(String spriteFront) {
        this.spriteFront = spriteFront;
    }

    public String getSpriteBack() {
        return spriteBack;
    }

    public void setSpriteBack(String spriteBack) {
        this.spriteBack = spriteBack;
    }

    public String getEspecie() {
        return especie;
    }

    public TiposPokemon getTipo() {
        return tipo;
    }

    public int getBaseHP() {
        return baseHP;
    }

    public int getGrowthHP() {
        return growthHP;
    }

    public TiposAtaque getMovimiento1() {
        return movimiento1;
    }

    public TiposAtaque getMovimiento2() {
        return movimiento2;
    }

    public int getNivel() {
        return nivel;
    }

    // Ejemplo de validación en un Setter (Encapsulamiento avanzado)
    public void setNivel(int nivel) {
        this.nivel = Math.max(0, Math.min(10, nivel));
    }

    public int getActualHP() {
        return actualHP;
    }

    public void setActualHP(int actualHP) {
        this.actualHP = actualHP;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public void setAttackValue(int danoAtaque) {
        this.danoAtaque = danoAtaque;
    }

    public int getAttackValue() {
        return this.danoAtaque;
    }

    public void setDefenseValue(int danoDefensa) {
        this.danoDefensa = danoDefensa;
    }

    public int getDefenseValue() {
        return this.danoDefensa;
    }

    public int getVelocidad() {
        return velocidad;
    }

    public void setVelocidad(int velocidad) {
        this.velocidad = velocidad;
    }

    public int getppM1() {
        return ppM1;
    }

    /**
     * Decrementa en 1 los PP del movimiento 1 si hay >0. Devuelve true si pudo
     * usarse.
     */
    public boolean usarppM1() {
        if (this.ppM1 > 0) {
            this.ppM1--;
            return true;
        }
        return false;
    }

    public int getppM2() {
        return ppM2;
    }

    /**
     * Decrementa en 1 los PP del movimiento 2 si hay >0. Devuelve true si pudo
     * usarse.
     */
    public boolean usatppM2() {
        if (this.ppM2 > 0) {
            this.ppM2--;
            return true;
        }
        return false;
    }

    /**
     * UNIDAD 1: PASE DE MENSAJES
     * Este método interactúa con otros métodos internos para realizar una acción.
     */
    public boolean usarPPMovimiento(int movimientoNumero) {
        if (movimientoNumero == 1)
            return usarppM1();
        if (movimientoNumero == 2)
            return usatppM2();
        return false;
    }

    /** Restaura HP y PP al máximo */
    public void restoreStatus() {
        this.actualHP = this.maxHp;
        if (this.movimiento1 != null) {
            this.ppM1 = this.movimiento1.PP;
        }
        if (this.movimiento2 != null) {
            this.ppM2 = this.movimiento2.PP;
        }
    }
}
