package sodyl.proyecto.libGDX; // Ubicado en el paquete raíz para mejor acceso

import com.badlogic.gdx.Gdx;
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

/**
 * Clase para manejar cuadros de diálogo con efecto de tipeo (typing effect).
 * Gestiona un Table que se posiciona en la parte inferior del Stage.
 */
public class DialogBox {
    private Table container;
    private Label textLabel;
    private String fullText;
    private int currentVisibleChars;
    private Texture backgroundTexture;

    // Velocidad de tipeo: caracteres por segundo
    private final float charsPerSecond = 30f;
    private float charTimer;

    // Banderas de estado
    private boolean isTyping;
    private boolean isFinished;

    // Dimensiones estáticas
    private final float BOX_HEIGHT = 150f;
    private final float PADDING = 20f;
    private final int BORDER_THICKNESS_HORIZONTAL = 3; // Bordes superior e inferior
    private final int BORDER_THICKNESS_VERTICAL = 8; // Bordes izquierdo y derecho (más gruesos)
    private final int CORNER_RADIUS = 15;

    public DialogBox(Stage stage, BitmapFont font) {
        // Usa el ancho actual del stage para el diseño inicial
        float initialWidth = stage.getWidth();

        // 1. Crear el estilo de fondo (caja blanca con borde verde curvado)
        // La textura se crea con el ancho actual del stage.
        this.backgroundTexture = createRoundedRectTexture(initialWidth, BOX_HEIGHT, Color.WHITE,
                Color.GREEN, BORDER_THICKNESS_HORIZONTAL, BORDER_THICKNESS_VERTICAL, CORNER_RADIUS);
        Drawable background = new TextureRegionDrawable(backgroundTexture);

        // 2. Crear el estilo de la etiqueta (Label)
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.BLACK);
        textLabel = new Label("", labelStyle);
        textLabel.setWrap(true);
        textLabel.setAlignment(Align.topLeft);

        // Aseguramos que el label no ocupe todo el espacio de la tabla si el texto es
        // corto
        textLabel.setHeight(BOX_HEIGHT - 2 * PADDING);

        // 3. Crear la tabla contenedora
        container = new Table();
        container.setBackground(background);
        container.setWidth(initialWidth);
        container.setHeight(BOX_HEIGHT);
        container.setPosition(0, 0); // Posicionarla en la parte inferior

        // 4. Añadir la etiqueta al contenedor con padding
        container.pad(PADDING);
        // Expandir y rellenar asegura que el texto use todo el espacio disponible
        // dentro del padding
        container.add(textLabel).expand().fill().align(Align.topLeft);

        // 5. Añadir la tabla al stage, pero ocultarla al principio
        stage.addActor(container);
        container.setVisible(false);
    }

    /**
     * Crea una textura de un solo color con un borde, liberando el Pixmap.
     * Es crucial para el redimensionamiento del fondo.
     */
    private Texture createColoredTexture(float width, float height, Color bgColor, Color borderColor,
            int borderThickness) {
        int textureWidth = Math.max(1, (int) width);
        int textureHeight = Math.max(1, (int) height);

        Pixmap pixmap = new Pixmap(textureWidth, textureHeight, Pixmap.Format.RGBA8888);

        // Dibujar borde
        pixmap.setColor(borderColor);
        pixmap.fill();

        // Dibujar fondo interior
        pixmap.setColor(bgColor);
        pixmap.fillRectangle(
                borderThickness,
                borderThickness,
                textureWidth - 2 * borderThickness,
                textureHeight - 2 * borderThickness);

        Texture texture = new Texture(pixmap);
        // CRÍTICO: Liberar el Pixmap después de crear la Texture
        pixmap.dispose();
        return texture;
    }

    /**
     * Crea una textura con bordes redondeados y un borde de color.
     * Soporta diferentes grosores para bordes horizontales (superior/inferior) y
     * verticales (izquierdo/derecho).
     */
    private Texture createRoundedRectTexture(float width, float height, Color bgColor, Color borderColor,
            int borderThicknessH, int borderThicknessV, int cornerRadius) {
        int textureWidth = Math.max(1, (int) width);
        int textureHeight = Math.max(1, (int) height);

        Pixmap pixmap = new Pixmap(textureWidth, textureHeight, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0, 0, 0, 0)); // Transparente
        pixmap.fill();

        // Dibujar el borde verde redondeado
        pixmap.setColor(borderColor);
        drawRoundedRect(pixmap, 0, 0, textureWidth, textureHeight, cornerRadius);

        // Dibujar el fondo blanco interior
        pixmap.setColor(bgColor);
        drawRoundedRect(pixmap, borderThicknessV, borderThicknessH,
                textureWidth - 2 * borderThicknessV, textureHeight - 2 * borderThicknessH,
                cornerRadius - Math.max(borderThicknessH, borderThicknessV));

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /**
     * Dibuja un rectángulo redondeado en el Pixmap.
     */
    private void drawRoundedRect(Pixmap pixmap, int x, int y, int width, int height, int radius) {
        // Limitar el radio a la mitad del lado más pequeño
        radius = Math.min(radius, Math.min(width, height) / 2);

        // Dibujar rectángulos para formar el cuerpo
        pixmap.fillRectangle(x + radius, y, width - 2 * radius, height); // Centro horizontal
        pixmap.fillRectangle(x, y + radius, width, height - 2 * radius); // Centro vertical

        // Dibujar círculos en las esquinas
        drawFilledCircle(pixmap, x + radius, y + radius, radius); // Esquina inferior izquierda
        drawFilledCircle(pixmap, x + width - radius - 1, y + radius, radius); // Esquina inferior derecha
        drawFilledCircle(pixmap, x + radius, y + height - radius - 1, radius); // Esquina superior izquierda
        drawFilledCircle(pixmap, x + width - radius - 1, y + height - radius - 1, radius); // Esquina superior derecha
    }

    /**
     * Dibuja un círculo relleno en el Pixmap usando el algoritmo de punto medio.
     */
    private void drawFilledCircle(Pixmap pixmap, int centerX, int centerY, int radius) {
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                if (x * x + y * y <= radius * radius) {
                    pixmap.drawPixel(centerX + x, centerY + y);
                }
            }
        }
    }

    // --- Métodos de ciclo de vida ---

    /**
     * Ajusta la caja de diálogo al nuevo tamaño de la pantalla.
     * Recrea la textura de fondo para un escalado correcto y liberar la anterior.
     */
    public void resize(float width, float height) {
        // 1. Liberar la textura vieja
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }

        // 2. Recrear la textura de fondo con el nuevo ancho
        this.backgroundTexture = createRoundedRectTexture(width, BOX_HEIGHT, Color.WHITE, Color.GREEN,
                BORDER_THICKNESS_HORIZONTAL, BORDER_THICKNESS_VERTICAL, CORNER_RADIUS);
        container.setBackground(new TextureRegionDrawable(backgroundTexture));

        // 3. Reajustar el tamaño y posición de la tabla contenedora
        container.setWidth(width);
        container.setHeight(BOX_HEIGHT);
        container.setPosition(0, 0);
        // Fuerza el re-layout de la tabla y sus hijos
        container.invalidateHierarchy();
    }

    /**
     * Libera la textura de fondo. Debe llamarse en el dispose() de la pantalla para
     * evitar fugas de memoria.
     */
    public void dispose() {
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
    }

    // --- Métodos de control de diálogo ---

    /**
     * Establece un nuevo texto para el diálogo y reinicia la animación de tipeo.
     * 
     * @param text El texto completo a mostrar.
     */
    public void setText(String text) {
        this.fullText = text;
        this.currentVisibleChars = 0;
        this.charTimer = 0;
        this.isTyping = true;
        this.isFinished = false;
        textLabel.setText("");
        container.setVisible(true);
    }

    /**
     * Oculta el cuadro de diálogo.
     */
    public void hide() {
        container.setVisible(false);
    }

    /**
     * Actualiza la lógica del diálogo, principalmente el efecto de tipeo.
     * 
     * @param delta Tiempo desde el último frame (Gdx.graphics.getDeltaTime()).
     */
    public void update(float delta) {
        if (isTyping) {
            charTimer += delta;

            // Calcular el número objetivo de caracteres basado en el tiempo transcurrido
            // Multiplicamos el tiempo acumulado por la velocidad
            int targetChars = (int) (charTimer * charsPerSecond);

            // Solo actualiza si hay nuevos caracteres visibles para evitar setText()
            // innecesarios
            if (targetChars > currentVisibleChars) {
                currentVisibleChars = Math.min(targetChars, fullText.length());

                // Muestra solo la parte visible del texto
                textLabel.setText(fullText.substring(0, currentVisibleChars));

                if (currentVisibleChars == fullText.length()) {
                    isTyping = false;
                    isFinished = true;
                }
            }
        }
        // Llamar a act en el contenedor es útil si hay animaciones o acciones en sus
        // hijos.
        container.act(delta);
    }

    /**
     * Fuerza el avance del diálogo: muestra el texto completo instantáneamente.
     */
    public void advance() {
        if (isTyping) {
            // Muestra texto completo instantáneamente
            textLabel.setText(fullText);
            currentVisibleChars = fullText.length();
            isTyping = false;
            isFinished = true;
        }
    }

    /**
     * Retorna true si el texto completo está visible y no se está tipeando (listo
     * para la siguiente acción).
     */
    public boolean isTextFinished() {
        return isFinished && !isTyping;
    }

    /**
     * Retorna true si actualmente se está animando el texto.
     */
    public boolean isTyping() {
        return isTyping;
    }

    /**
     * Trae el contenedor del diálogo al frente del escenario.
     */
    public void toFront() {
        container.toFront();
    }
}
