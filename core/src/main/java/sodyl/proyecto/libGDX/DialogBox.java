package sodyl.proyecto.libGDX;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

// Clase para gestionar cuadros de diálogo.
public class DialogBox {
    private Table container;
    private Label textLabel;
    private String fullText;
    private int currentVisibleChars;
    private Texture backgroundTexture;
    private Color currentBorderColor = Color.GREEN;

    // Velocidad de type
    private final float charsPerSecond = 30f;
    private float charTimer;

    private boolean isTyping;
    private boolean isFinished;

    // Dimensiones
    private final float BOX_HEIGHT = 150f;
    private final float PADDING = 20f;
    private final int BORDER_THICKNESS_HORIZONTAL = 3;
    private final int BORDER_THICKNESS_VERTICAL = 8;
    private final int CORNER_RADIUS = 15;

    public DialogBox(Stage stage, BitmapFont font) {
        float initialWidth = stage.getWidth();

        this.backgroundTexture = createRoundedRectTexture(initialWidth, BOX_HEIGHT, Color.WHITE,
                Color.GREEN, BORDER_THICKNESS_HORIZONTAL, BORDER_THICKNESS_VERTICAL, CORNER_RADIUS);
        Drawable background = new TextureRegionDrawable(backgroundTexture);

        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.BLACK);
        textLabel = new Label("", labelStyle);
        textLabel.setWrap(true);
        textLabel.setAlignment(Align.topLeft);

        textLabel.setHeight(BOX_HEIGHT - 2 * PADDING);

        // Crear la tabla contenedora
        container = new Table();
        container.setBackground(background);
        container.setWidth(initialWidth);
        container.setHeight(BOX_HEIGHT);
        container.setPosition(0, 0);

        container.pad(PADDING);
        container.add(textLabel).expand().fill().align(Align.topLeft);
        stage.addActor(container);
        container.setVisible(false);
    }

    // Crea una textura con bordes redondeados y un borde de color
    private Texture createRoundedRectTexture(float width, float height, Color bgColor, Color borderColor,
            int borderThicknessH, int borderThicknessV, int cornerRadius) {
        int textureWidth = Math.max(1, (int) width);
        int textureHeight = Math.max(1, (int) height);

        Pixmap pixmap = new Pixmap(textureWidth, textureHeight, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0, 0, 0, 0));
        pixmap.fill();

        pixmap.setColor(borderColor);
        drawRoundedRect(pixmap, 0, 0, textureWidth, textureHeight, cornerRadius);

        pixmap.setColor(bgColor);
        drawRoundedRect(pixmap, borderThicknessV, borderThicknessH,
                textureWidth - 2 * borderThicknessV, textureHeight - 2 * borderThicknessH,
                cornerRadius - Math.max(borderThicknessH, borderThicknessV));

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void drawRoundedRect(Pixmap pixmap, int x, int y, int width, int height, int radius) {
        radius = Math.min(radius, Math.min(width, height) / 2);
        pixmap.fillRectangle(x + radius, y, width - 2 * radius, height);
        pixmap.fillRectangle(x, y + radius, width, height - 2 * radius);

        drawFilledCircle(pixmap, x + radius, y + radius, radius);
        drawFilledCircle(pixmap, x + width - radius - 1, y + radius, radius);
        drawFilledCircle(pixmap, x + radius, y + height - radius - 1, radius);
        drawFilledCircle(pixmap, x + width - radius - 1, y + height - radius - 1, radius);
    }

    // Método para dibujar un círculo relleno
    private void drawFilledCircle(Pixmap pixmap, int centerX, int centerY, int radius) {
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                if (x * x + y * y <= radius * radius) {
                    pixmap.drawPixel(centerX + x, centerY + y);
                }
            }
        }
    }

    // Resize hace que el dialog box se ajuste al nuevo tamaño de la pantalla
    public void resize(float width, float height) {
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
        this.backgroundTexture = createRoundedRectTexture(width, BOX_HEIGHT, Color.WHITE, currentBorderColor,
                BORDER_THICKNESS_HORIZONTAL, BORDER_THICKNESS_VERTICAL, CORNER_RADIUS);
        container.setBackground(new TextureRegionDrawable(backgroundTexture));
        container.setWidth(width);
        container.setHeight(BOX_HEIGHT);
        container.setPosition(0, 0);
        container.invalidateHierarchy();
    }

    public void dispose() {
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
    }

    // Cambia el color del borde del dialog box
    public void setBorderColor(Color color) {
        if (color == null || color.equals(currentBorderColor))
            return;

        this.currentBorderColor = color;

        if (container != null && container.getStage() != null) {
            if (backgroundTexture != null) {
                backgroundTexture.dispose();
            }

            float width = container.getStage().getWidth();
            this.backgroundTexture = createRoundedRectTexture(width, BOX_HEIGHT, Color.WHITE, currentBorderColor,
                    BORDER_THICKNESS_HORIZONTAL, BORDER_THICKNESS_VERTICAL, CORNER_RADIUS);
            container.setBackground(new TextureRegionDrawable(backgroundTexture));
        }
    }

    // Establece un nuevo texto para el diálogo y reinicia la animación de typing
    public void setText(String text) {
        this.fullText = text;
        this.currentVisibleChars = 0;
        this.charTimer = 0;
        this.isTyping = true;
        this.isFinished = false;
        textLabel.setText("");
        container.setVisible(true);
    }

    public void hide() {
        container.setVisible(false);
    }

    // Actualiza la lógica del diálogo, principalmente el efecto de typing
    public void update(float delta) {
        if (isTyping) {
            charTimer += delta;
            int targetChars = (int) (charTimer * charsPerSecond);
            if (targetChars > currentVisibleChars) {
                currentVisibleChars = Math.min(targetChars, fullText.length());
                textLabel.setText(fullText.substring(0, currentVisibleChars));

                if (currentVisibleChars == fullText.length()) {
                    isTyping = false;
                    isFinished = true;
                }
            }
        }
        container.act(delta);
    }

    // Cuando se presiona una tecla, se muestra el texto completo instantáneamente,
    // saltándose el typing
    public void advance() {
        if (isTyping) {
            textLabel.setText(fullText);
            currentVisibleChars = fullText.length();
            isTyping = false;
            isFinished = true;
        }
    }

    public boolean isTextFinished() {
        return isFinished && !isTyping;
    }

    public boolean isTyping() {
        return isTyping;
    }

    public void toFront() {
        container.toFront();
    }
}
