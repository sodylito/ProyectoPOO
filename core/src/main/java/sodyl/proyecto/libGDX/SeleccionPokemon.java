package sodyl.proyecto.libGDX;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import sodyl.proyecto.clases.Inventario;

import com.badlogic.gdx.Input.Keys;

//Clase que implementa la pantalla de selección de Pokémon
public class SeleccionPokemon implements Screen, InputProcessor {

    private final Proyecto game;
    private Stage stage;
    private BitmapFont font;
    private BitmapFont titleFont;
    private BitmapFont labelFont;

    // Arreglo de Pokemones
    private String[] pokemonNames = { "Rowlet", "Cyndaquil", "Oshawott" };
    private Array<Texture> pokemonTextures;
    private int selectedIndex = 0;
    private Label selectedNameLabel;
    private Label instructionLabel;
    private Label selectionConfirmationLabel;
    private Table selectionTable;
    private Table mainTable;
    private Label titleLabel;

    // Contadores de animación
    private float pulseTimer = 0f;

    private ScreenMapaTiled previousScreen;

    public SeleccionPokemon(Proyecto game) {
        this.game = game;
    }

    public SeleccionPokemon(Proyecto game, ScreenMapaTiled previousScreen) {
        this.game = game;
        this.previousScreen = previousScreen;
    }

    @Override
    public void show() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("Mapa/ari-w9500-bold.ttf"));

        // Titulo
        FreeTypeFontParameter titleParameter = new FreeTypeFontParameter();
        titleParameter.size = 48;
        titleParameter.color = Color.WHITE;
        titleParameter.borderWidth = 2;
        titleParameter.borderColor = new Color(0.2f, 0.2f, 0.8f, 1f);
        titleParameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "áéíóúÁÉÍÓÚñÑ¿¡";
        titleFont = generator.generateFont(titleParameter);

        // Etiqueta
        FreeTypeFontParameter labelParameter = new FreeTypeFontParameter();
        labelParameter.size = 32;
        labelParameter.color = Color.WHITE;
        labelParameter.borderWidth = 1.5f;
        labelParameter.borderColor = new Color(0.3f, 0.3f, 0.3f, 1f);
        labelParameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "áéíóúÁÉÍÓÚñÑ¿¡";
        labelFont = generator.generateFont(labelParameter);

        // Regular font
        FreeTypeFontParameter fontParameter = new FreeTypeFontParameter();
        fontParameter.size = 24;
        fontParameter.color = Color.WHITE;
        fontParameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "áéíóúÁÉÍÓÚñÑ¿¡";
        font = generator.generateFont(fontParameter);

        generator.dispose();

        stage = new Stage(new FitViewport(Proyecto.PANTALLA_W, Proyecto.PANTALLA_H));
        Gdx.input.setInputProcessor(this);

        // Texturas de los Pokemones, las que se van a mostrar en la pantalla
        pokemonTextures = new Array<>();
        pokemonTextures.add(new Texture(Gdx.files.internal("imagenes/rowlet.png")));
        pokemonTextures.add(new Texture(Gdx.files.internal("imagenes/cyndaquil.png")));
        pokemonTextures.add(new Texture(Gdx.files.internal("imagenes/oshawott.png")));

        // Estilos de las etiquetas
        LabelStyle labelStyle = new LabelStyle(labelFont, Color.WHITE);
        LabelStyle titleStyle = new LabelStyle(titleFont, new Color(1f, 0.9f, 0.2f, 1f));
        LabelStyle instructionStyle = new LabelStyle(font, new Color(0.8f, 0.8f, 1f, 1f));

        // Tabla principal (Layout)
        mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        // Aparición con Fade in
        titleLabel = new Label("Elige tu Pokémon inicial", titleStyle);
        titleLabel.setColor(1f, 0.9f, 0.2f, 0f);
        titleLabel.addAction(Actions.fadeIn(1.0f));
        mainTable.add(titleLabel).padTop(50).padBottom(50).row();

        selectedNameLabel = new Label(pokemonNames[selectedIndex], labelStyle);
        selectedNameLabel.setColor(1f, 1f, 1f, 0f);
        selectedNameLabel.addAction(Actions.sequence(Actions.delay(0.3f), Actions.fadeIn(0.8f)));
        mainTable.add(selectedNameLabel).padBottom(20).row();

        selectionConfirmationLabel = new Label("Presiona ENTER para confirmar", instructionStyle);
        selectionConfirmationLabel.setColor(0.4f, 1f, 0.4f, 0f);
        selectionConfirmationLabel.addAction(Actions.sequence(Actions.delay(0.5f), Actions.fadeIn(0.6f)));
        mainTable.add(selectionConfirmationLabel).padBottom(50).row();

        selectionTable = new Table();

        for (int i = 0; i < pokemonTextures.size; i++) {
            final int index = i;
            TextureRegionDrawable drawable = new TextureRegionDrawable(pokemonTextures.get(i));
            ImageButton pokemonButton = new ImageButton(drawable);
            pokemonButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    setSelection(index);
                }
            });

            selectionTable.add(pokemonButton).size(150, 150).pad(20);
        }

        mainTable.add(selectionTable).padTop(20).row();

        instructionLabel = new Label("Usa las flechas ← → o el mouse para elegir", instructionStyle);
        instructionLabel.setColor(0.8f, 0.8f, 1f, 0f);
        instructionLabel.addAction(Actions.sequence(Actions.delay(0.7f), Actions.fadeIn(0.7f)));
        mainTable.add(instructionLabel).padTop(50).row();
        updateSelectionUI();
    }

    private void updateSelectionUI() { // Actualiza la UI para reflejar el índice seleccionado
        String name = pokemonNames[selectedIndex];
        selectedNameLabel.setText(name);
        Gdx.app.log("Selección", "Seleccionado: " + name);
        for (int i = 0; i < selectionTable.getChildren().size; i++) {
            Actor actor = selectionTable.getChildren().get(i);
            if (actor instanceof ImageButton) {
                ImageButton button = (ImageButton) actor;

                if (i == selectedIndex) {
                    button.getImage().setColor(new Color(0.4f, 1f, 0.4f, 1f));
                    button.clearActions();
                    button.addAction(Actions.sequence(
                            Actions.scaleTo(1.15f, 1.15f, 0.2f),
                            Actions.forever(Actions.sequence(
                                    Actions.scaleTo(1.2f, 1.2f, 0.8f),
                                    Actions.scaleTo(1.15f, 1.15f, 0.8f)))));
                } else {
                    button.getImage().setColor(Color.WHITE);
                    button.clearActions();
                    button.addAction(Actions.scaleTo(1.0f, 1.0f, 0.2f));
                }
            }
        }
    }

    private void setSelection(int index) { // Establece el nuevo índice de selección y actualiza la UI
        if (index < 0) {
            selectedIndex = pokemonNames.length - 1;
        } else if (index >= pokemonNames.length) {
            selectedIndex = 0;
        } else {
            selectedIndex = index;
        }
        updateSelectionUI();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.08f, 0.05f, 0.25f, 1);

        pulseTimer += delta;
        if (selectionConfirmationLabel != null && selectionConfirmationLabel.getColor().a > 0.5f) {
            float pulse = 0.7f + 0.3f * (float) Math.sin(pulseTimer * 3.0f);
            selectionConfirmationLabel.setColor(0.4f * pulse, 1f * pulse, 0.4f * pulse, 1f);
        }

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        font.dispose();
        titleFont.dispose();
        labelFont.dispose();
        for (Texture texture : pokemonTextures) {
            texture.dispose();
        }
    }

    @Override
    public boolean keyDown(int keycode) { // Gestiona las teclas presionadas
        if (keycode == Keys.LEFT) {
            setSelection(selectedIndex - 1);
            return true;
        }
        if (keycode == Keys.RIGHT) {
            setSelection(selectedIndex + 1);
            return true;
        }
        if (keycode == Keys.ENTER) {
            String selectedPokemonName = pokemonNames[selectedIndex];
            Gdx.app.log("INICIO JUEGO", "Comenzando juego con " + selectedPokemonName);
            Inventario inventory = (previousScreen != null) ? previousScreen.playerInventory : null;
            if (inventory == null) {
                inventory = new Inventario();
            }
            inventory.addObjeto(101, 5); // AQUI AÑADIMOS LAS 5 POKEBOLAS DE INICIO, ESTA PARTE SI ES DE LÓGICA

            // AQUI REGISTRAMOS EL POKEMÓN SELECCIONADO EN LA POKÉDEX Y LAS 5 POKEBOLAS EN
            // EL INV
            sodyl.proyecto.clases.Pokemon starter = sodyl.proyecto.clases.Pokemones.getPokemon(selectedPokemonName);
            if (starter != null) {
                starter.setNivel(0);
                starter.actualizarAtributos();
                starter.setActualHP(starter.getMaxHp());
                sodyl.proyecto.clases.Pokedex.addCollected(starter);
            }

            // Volvemos al mapa, ha terminado la selección pokemon
            game.setScreen(new ScreenMapaTiled(game, "Mapa/MAPACOMPLETO.tmx", inventory, null, selectedPokemonName,
                    ScreenMapaTiled.GameState.FREE_ROAMING));
            return true;
        }
        return false;
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return stage.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
