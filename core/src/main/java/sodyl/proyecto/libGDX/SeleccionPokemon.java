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

public class SeleccionPokemon implements Screen, InputProcessor {

    private final Proyecto game;
    private Stage stage;
    private BitmapFont font;
    private BitmapFont titleFont;
    private BitmapFont labelFont;

    // Data structures for Pokémon
    private String[] pokemonNames = { "Rowlet", "Cyndaquil", "Oshawott" };
    private Array<Texture> pokemonTextures;
    private int selectedIndex = 0; // Index of the currently selected Pokémon

    // UI elements
    private Label selectedNameLabel;
    private Label instructionLabel;
    private Label selectionConfirmationLabel;
    private Table selectionTable;
    private Table mainTable;
    private Label titleLabel;

    // Animation timers
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
        // Load custom font using FreeTypeFontGenerator
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("Mapa/ari-w9500-bold.ttf"));

        // Title font (large)
        FreeTypeFontParameter titleParameter = new FreeTypeFontParameter();
        titleParameter.size = 48;
        titleParameter.color = Color.WHITE;
        titleParameter.borderWidth = 2;
        titleParameter.borderColor = new Color(0.2f, 0.2f, 0.8f, 1f);
        titleParameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "áéíóúÁÉÍÓÚñÑ¿¡";
        titleFont = generator.generateFont(titleParameter);

        // Label font (medium)
        FreeTypeFontParameter labelParameter = new FreeTypeFontParameter();
        labelParameter.size = 32;
        labelParameter.color = Color.WHITE;
        labelParameter.borderWidth = 1.5f;
        labelParameter.borderColor = new Color(0.3f, 0.3f, 0.3f, 1f);
        labelParameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "áéíóúÁÉÍÓÚñÑ¿¡";
        labelFont = generator.generateFont(labelParameter);

        // Regular font (small)
        FreeTypeFontParameter fontParameter = new FreeTypeFontParameter();
        fontParameter.size = 24;
        fontParameter.color = Color.WHITE;
        fontParameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "áéíóúÁÉÍÓÚñÑ¿¡";
        font = generator.generateFont(fontParameter);

        generator.dispose();

        stage = new Stage(new FitViewport(Proyecto.PANTALLA_W, Proyecto.PANTALLA_H));
        Gdx.input.setInputProcessor(this); // Set this class to handle keyboard input

        // Load all Pokémon textures
        pokemonTextures = new Array<>();
        try {
            pokemonTextures.add(new Texture(Gdx.files.internal("imagenes/rowlet.png")));
            pokemonTextures.add(new Texture(Gdx.files.internal("imagenes/cyndaquil.png")));
            pokemonTextures.add(new Texture(Gdx.files.internal("imagenes/oshawott.png")));
        } catch (Exception e) {
            Gdx.app.error("SELECTION", "Error loading Pokemon textures: " + e.getMessage());
            // Add placeholders to avoid crash
            for (int i = 0; i < 3; i++) {
                com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(150, 150,
                        com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
                pixmap.setColor(Color.RED);
                pixmap.fill();
                pokemonTextures.add(new Texture(pixmap));
                pixmap.dispose();
            }
        }

        // Styles with custom fonts
        LabelStyle labelStyle = new LabelStyle(labelFont, Color.WHITE);
        LabelStyle titleStyle = new LabelStyle(titleFont, new Color(1f, 0.9f, 0.2f, 1f)); // Golden yellow
        LabelStyle instructionStyle = new LabelStyle(font, new Color(0.8f, 0.8f, 1f, 1f)); // Light blue

        // Main Table (Layout)
        mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        // --- UI Elements Creation ---

        // Title with fade-in animation
        titleLabel = new Label("Elige tu Pokémon inicial", titleStyle);
        titleLabel.setColor(1f, 0.9f, 0.2f, 0f); // Start transparent
        titleLabel.addAction(Actions.fadeIn(1.0f));
        mainTable.add(titleLabel).padTop(50).padBottom(50).row();

        // Selected Pokémon Name Label with fade-in
        selectedNameLabel = new Label(pokemonNames[selectedIndex], labelStyle);
        selectedNameLabel.setColor(1f, 1f, 1f, 0f); // Start transparent
        selectedNameLabel.addAction(Actions.sequence(
                Actions.delay(0.3f),
                Actions.fadeIn(0.8f)));
        mainTable.add(selectedNameLabel).padBottom(20).row();

        // Selection Confirmation Label with pulsing effect
        selectionConfirmationLabel = new Label("Presiona ENTER para confirmar", instructionStyle);
        selectionConfirmationLabel.setColor(0.4f, 1f, 0.4f, 0f); // Bright green, start transparent
        selectionConfirmationLabel.addAction(Actions.sequence(
                Actions.delay(0.5f),
                Actions.fadeIn(0.6f)));
        mainTable.add(selectionConfirmationLabel).padBottom(50).row();

        // Table for the 3 selection images/buttons
        selectionTable = new Table();

        for (int i = 0; i < pokemonTextures.size; i++) {
            final int index = i;
            TextureRegionDrawable drawable = new TextureRegionDrawable(pokemonTextures.get(i));

            // ImageButton using the Pokémon image
            ImageButton pokemonButton = new ImageButton(drawable);

            // Add mouse click listener
            pokemonButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    setSelection(index);
                }
            });

            selectionTable.add(pokemonButton).size(150, 150).pad(20);
        }

        mainTable.add(selectionTable).padTop(20).row();

        // Instructions for keyboard/mouse with fade-in
        instructionLabel = new Label("Usa las flechas ← → o el mouse para elegir", instructionStyle);
        instructionLabel.setColor(0.8f, 0.8f, 1f, 0f); // Start transparent
        instructionLabel.addAction(Actions.sequence(
                Actions.delay(0.7f),
                Actions.fadeIn(0.7f)));
        mainTable.add(instructionLabel).padTop(50).row();

        // Initial state update
        updateSelectionUI();
    }

    /**
     * Updates the UI to reflect the current selectedIndex.
     */
    private void updateSelectionUI() {
        String name = pokemonNames[selectedIndex];
        selectedNameLabel.setText(name);
        Gdx.app.log("Selection", "Selected: " + name);

        // Visual indicator with animations
        for (int i = 0; i < selectionTable.getChildren().size; i++) {
            Actor actor = selectionTable.getChildren().get(i);
            if (actor instanceof ImageButton) {
                ImageButton button = (ImageButton) actor;

                if (i == selectedIndex) {
                    // Selected: bright color with scale animation
                    button.getImage().setColor(new Color(0.4f, 1f, 0.4f, 1f)); // Bright green
                    button.clearActions();
                    button.addAction(Actions.sequence(
                            Actions.scaleTo(1.15f, 1.15f, 0.2f),
                            Actions.forever(Actions.sequence(
                                    Actions.scaleTo(1.2f, 1.2f, 0.8f),
                                    Actions.scaleTo(1.15f, 1.15f, 0.8f)))));
                } else {
                    // Not selected: normal color and scale
                    button.getImage().setColor(Color.WHITE);
                    button.clearActions();
                    button.addAction(Actions.scaleTo(1.0f, 1.0f, 0.2f));
                }
            }
        }
    }

    /**
     * Sets the new selection index and updates the UI.
     * 
     * @param index The new index to select.
     */
    private void setSelection(int index) {
        // Ensure index wraps around (0 to 2)
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
        // Gradient background (dark blue to purple)
        ScreenUtils.clear(0.08f, 0.05f, 0.25f, 1);

        // Update pulse animation for confirmation label
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

    // --- InputProcessor Implementation (for Keyboard) ---

    @Override
    public boolean keyDown(int keycode) {
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
            Gdx.app.log("GAME START", "Starting game with " + selectedPokemonName);

            // Registrar en la Pokedex
            try {
                sodyl.proyecto.clases.Pokedex.addSeen(selectedPokemonName);
            } catch (Exception ignored) {
            }

            // Transition back to Map 1
            // Use the constructor that allows setting the state to FREE_ROAMING to avoid
            // intro loop
            // Preserve inventory if available
            Inventario inventory = (previousScreen != null) ? previousScreen.playerInventory : null;

            game.setScreen(new ScreenMapaTiled(game, "Mapa/mapa11.tmx", inventory, null, selectedPokemonName,
                    ScreenMapaTiled.GameState.FREE_ROAMING));
            return true;
        }
        return false;
    }

    // Unused methods from Screen and InputProcessor
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
        // Since the buttons handle touchDown, this is mainly for the Stage
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
