package sodyl.proyecto.clases;

//Clase para definir los atributos y comportamientos de cada pokemon de forma individual
public class Pokemon {

    private String especie;
    private TiposPokemon tipo;
    private int baseHP;
    private int growthHP;
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

    public Pokemon() {
    }

    public Pokemon(String especie, TiposPokemon tipo, int baseHP, int growthHP, TiposAtaque movimiento1,
            TiposAtaque movimiento2) {
        this.especie = especie;
        this.tipo = tipo;
        this.baseHP = baseHP;
        this.growthHP = growthHP;
        this.movimiento1 = movimiento1;
        this.movimiento2 = movimiento2;
        this.nivel = 1;

        // Este es el cálculo de los atributos que dependen del nivel del pokemón
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

    // Getters y Setters
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

    // Función que evalúa si se puede usar el movimiento 1
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

    public boolean usatppM2() {
        if (this.ppM2 > 0) {
            this.ppM2--;
            return true;
        }
        return false;
    }

    public boolean usarPPMovimiento(int movimientoNumero) {
        if (movimientoNumero == 1)
            return usarppM1();
        if (movimientoNumero == 2)
            return usatppM2();
        return false;
    }

    // Restaura HP y PP al máximo, útil en el centro Pokémon
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
