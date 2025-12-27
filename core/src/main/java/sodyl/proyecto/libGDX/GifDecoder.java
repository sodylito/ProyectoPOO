package sodyl.proyecto.libGDX;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * GIF Decoder for LibGDX.
 * Ported/Adapted from https://gist.github.com/devunwired/4479231
 * Uses Pixmap instead of Android Bitmap.
 */
public class GifDecoder {

    /**
     * File read status: No errors.
     */
    public static final int STATUS_OK = 0;
    /**
     * File read status: Error decoding file (may be partially decoded)
     */
    public static final int STATUS_FORMAT_ERROR = 1;
    /**
     * File read status: Unable to open source.
     */
    public static final int STATUS_OPEN_ERROR = 2;
    /**
     * max decoder pixel stack size
     */
    protected static final int MAX_STACK_SIZE = 4096;

    /**
     * GIF Disposal Method meaning take no action
     */
    private static final int DISPOSAL_UNSPECIFIED = 0;
    /**
     * GIF Disposal Method meaning leave canvas from previous frame
     */
    private static final int DISPOSAL_NONE = 1;
    /**
     * GIF Disposal Method meaning clear canvas to background color
     */
    private static final int DISPOSAL_BACKGROUND = 2;
    /**
     * GIF Disposal Method meaning clear canvas to frame before last
     */
    private static final int DISPOSAL_PREVIOUS = 3;

    /**
     * Global status code of GIF data parsing
     */
    protected int status;

    // Global File Header values and parsing flags
    protected int width; // full image width
    protected int height; // full image height
    protected boolean gctFlag; // global color table used
    protected int gctSize; // size of global color table
    protected int loopCount = 1; // iterations; 0 = repeat forever
    protected int[] gct; // global color table
    protected int[] act; // active color table
    protected int bgIndex; // background color index
    protected int bgColor; // background color
    protected int pixelAspect; // pixel aspect ratio
    protected boolean lctFlag; // local color table flag
    protected int lctSize; // local color table size

    // Raw GIF data from input source
    protected ByteBuffer rawData;

    // Raw data read working array
    protected byte[] block = new byte[256]; // current data block
    protected int blockSize = 0; // block size last graphic control extension info

    // LZW decoder working arrays
    protected short[] prefix;
    protected byte[] suffix;
    protected byte[] pixelStack;
    protected byte[] mainPixels;
    protected int[] mainScratch, copyScratch;

    protected ArrayList<GifFrame> frames; // frames read from current file
    protected GifFrame currentFrame;
    protected Pixmap previousImage, currentImage, renderImage;

    protected int framePointer;
    protected int frameCount;

    /**
     * Inner model class housing metadata for each frame
     */
    private static class GifFrame {
        public int ix, iy, iw, ih;
        /* Control Flags */
        public boolean interlace;
        public boolean transparency;
        /* Disposal Method */
        public int dispose;
        /* Transparency Index */
        public int transIndex;
        /* Delay, in ms, to next frame */
        public int delay;
        /* Index in the raw buffer where we need to start reading to decode */
        public int bufferFrameStart;
        /* Local Color Table */
        public int[] lct;
    }

    /**
     * Helper method to load a GIF file into a LibGDX Animation.
     * 
     * @param playMode The play mode for the animation.
     * @param is       Access to the file (e.g., Gdx.files.internal("...").read())
     * @return The created Animation or null if failed.
     */
    public static Animation<TextureRegion> loadGIFAnimation(Animation.PlayMode playMode, InputStream is) {
        GifDecoder decoder = new GifDecoder();
        decoder.read(is);
        if (decoder.err()) {
            return null;
        }

        Array<TextureRegion> keyFrames = new Array<TextureRegion>();
        // Advance and decode frames
        // getFrameCount actually returns the number of frames read in read()
        int n = decoder.getFrameCount();
        for (int i = 0; i < n; i++) {
            decoder.advance();
            Pixmap pm = decoder.getNextFrame();
            if (pm != null) {
                // Copy to texture. Texture constructor copies data to VRAM.
                Texture texture = new Texture(pm);
                keyFrames.add(new TextureRegion(texture));
                // Note: We do not dispose 'pm' here because it is a reference to 'currentImage'
                // inside decoder which is reused.
                // The decoder will eventually garbage collect, but 'currentImage' is native.
                // We should add a generic dispose to Decoder if we wanted to be strictly clean,
                // but since we reuse 'currentImage' instance, we only have 2 Pixmaps in memory
                // during load.
            }
        }

        // Calculate frame duration. If variable delays, this naive approach averages or
        // picks first?
        // Animation supports uniform frame duration.
        // If we want variable duration, we'd need Animation that supports it or
        // multiple frames.
        // For simplicity, we take the average delay or first frame delay.
        float frameDuration = 0.1f;
        if (n > 0) {
            int delayMs = decoder.getDelay(0);
            if (delayMs > 0)
                frameDuration = delayMs / 1000f;
        }

        Animation<TextureRegion> animation = new Animation<>(frameDuration, keyFrames);
        animation.setPlayMode(playMode);
        return animation;
    }

    /**
     * Move the animation frame counter forward
     */
    public void advance() {
        framePointer = (framePointer + 1) % frameCount;
    }

    /**
     * Gets display duration for specified frame.
     *
     * @param n int index of frame
     * @return delay in milliseconds
     */
    public int getDelay(int n) {
        int delay = -1;
        if ((n >= 0) && (n < frameCount)) {
            delay = frames.get(n).delay;
        }
        return delay;
    }

    /**
     * Gets the number of frames read from file.
     *
     * @return frame count
     */
    public int getFrameCount() {
        return frameCount;
    }

    /**
     * Get the next frame in the animation sequence.
     *
     * @return Pixmap representation of frame (reference to internal buffer, do not
     *         dispose)
     */
    public Pixmap getNextFrame() {
        if (frameCount <= 0 || framePointer < 0 || currentImage == null) {
            return null;
        }

        GifFrame frame = frames.get(framePointer);

        // Set the appropriate color table
        if (frame.lct == null) {
            act = gct;
        } else {
            act = frame.lct;
            if (bgIndex == frame.transIndex) {
                bgColor = 0;
            }
        }

        int save = 0;
        if (frame.transparency) {
            save = act[frame.transIndex];
            act[frame.transIndex] = 0; // set transparent color if specified
        }
        if (act == null) {
            status = STATUS_FORMAT_ERROR; // no color table defined
            return null;
        }

        setPixels(framePointer); // transfer pixel data to image

        // Reset the transparent pixel in the color table
        if (frame.transparency) {
            act[frame.transIndex] = save;
        }

        return currentImage;
    }

    /**
     * Reads GIF image from stream
     *
     * @param is containing GIF file.
     * @return read status code (0 = no errors)
     */
    public int read(InputStream is) {
        if (is != null) {
            try {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[16384];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                read(buffer.toByteArray());
            } catch (IOException e) {
                // Log error
            }
        } else {
            status = STATUS_OPEN_ERROR;
        }
        try {
            if (is != null)
                is.close();
        } catch (Exception e) {
        }
        return status;
    }

    /**
     * Reads GIF image from byte array
     *
     * @param data containing GIF file.
     * @return read status code (0 = no errors)
     */
    public int read(byte[] data) {
        init();
        if (data != null) {
            // Initiliaze the raw data buffer
            rawData = ByteBuffer.wrap(data);
            rawData.rewind();
            rawData.order(java.nio.ByteOrder.LITTLE_ENDIAN);

            readHeader();
            if (!err()) {
                readContents();
                if (frameCount < 0) {
                    status = STATUS_FORMAT_ERROR;
                }
            }
        } else {
            status = STATUS_OPEN_ERROR;
        }
        return status;
    }

    /**
     * Creates new frame image from current data (and previous frames as specified
     * by their disposition codes).
     */
    protected void setPixels(int frameIndex) {
        GifFrame currentFrame = frames.get(frameIndex);
        GifFrame previousFrame = null;
        int previousIndex = frameIndex - 1;
        if (previousIndex >= 0) {
            previousFrame = frames.get(previousIndex);
        }

        // final location of blended pixels
        final int[] dest = mainScratch;

        // fill in starting image contents based on last image's dispose code
        if (previousFrame != null && previousFrame.dispose > DISPOSAL_UNSPECIFIED) {
            if (previousFrame.dispose == DISPOSAL_NONE && currentImage != null) {
                // Start with the current image
                // COPY currentImage pixels to dest
                // We must read FROM currentImage because dest might be dirty from scratch
                // usage?
                // Ah, currentImage tracks the visual state.
                copyPixmapToValues(currentImage, dest);
            }
            if (previousFrame.dispose == DISPOSAL_BACKGROUND) {
                // Start with a canvas filled with the background color
                int c = 0;
                if (!currentFrame.transparency) {
                    c = bgColor;
                }
                for (int i = 0; i < previousFrame.ih; i++) {
                    int n1 = (previousFrame.iy + i) * width + previousFrame.ix;
                    int n2 = n1 + previousFrame.iw;
                    for (int k = n1; k < n2; k++) {
                        dest[k] = c;
                    }
                }
            }
            if (previousFrame.dispose == DISPOSAL_PREVIOUS && previousImage != null) {
                // Start with the previous frame
                copyPixmapToValues(previousImage, dest);
            }
        }

        // Decode pixels for this frame into the global pixels[] scratch
        decodeBitmapData(currentFrame, mainPixels); // decode pixel data

        // copy each source line to the appropriate place in the destination
        int pass = 1;
        int inc = 8;
        int iline = 0;
        for (int i = 0; i < currentFrame.ih; i++) {
            int line = i;
            if (currentFrame.interlace) {
                if (iline >= currentFrame.ih) {
                    pass++;
                    switch (pass) {
                        case 2:
                            iline = 4;
                            break;
                        case 3:
                            iline = 2;
                            inc = 4;
                            break;
                        case 4:
                            iline = 1;
                            inc = 2;
                            break;
                        default:
                            break;
                    }
                }
                line = iline;
                iline += inc;
            }
            line += currentFrame.iy;
            if (line < height) {
                int k = line * width;
                int dx = k + currentFrame.ix; // start of line in dest
                int dlim = dx + currentFrame.iw; // end of dest line
                if ((k + width) < dlim) {
                    dlim = k + width; // past dest edge
                }
                int sx = i * currentFrame.iw; // start of line in source
                while (dx < dlim) {
                    // map color and insert in destination
                    int index = ((int) mainPixels[sx++]) & 0xff;
                    int c = act[index];
                    if (c != 0) {
                        dest[dx] = c;
                    }
                    dx++;
                }
            }
        }

        // Copy pixels into previous image
        copyValuesToPixmap(dest, currentImage); // Actually currentImage should update based on dest.
        // Wait, the logic requires preserving previous state.
        // "Copy pixels into previous image" in original means saving current state to
        // previousImage before next mutation if needed?
        // No, the original logic was:
        // currentImage.getPixels(copyScratch...) -> copy to copyScratch
        // previousImage.setPixels(copyScratch) -> save to prev
        // currentImage.setPixels(dest) -> update current

        // My implementation:
        // copyPixmapToValues(currentImage, copyScratch);
        // copyValuesToPixmap(copyScratch, previousImage);
        // copyValuesToPixmap(dest, currentImage);

        // Optimized:
        copyPixmapToPixmap(currentImage, previousImage);
        copyValuesToPixmap(dest, currentImage);
    }

    private void copyPixmapToValues(Pixmap src, int[] dst) {
        ByteBuffer bb = src.getPixels();
        bb.rewind();
        // Since we are using RGBA8888, bytes are R G B A
        // We constructed ints as (R<<24)|(G<<16)|(B<<8)|A (Big Endian int)
        // If we read int from bb (Big Endian default), it matches.
        bb.asIntBuffer().get(dst);
    }

    private void copyValuesToPixmap(int[] src, Pixmap dst) {
        ByteBuffer bb = dst.getPixels();
        bb.rewind();
        bb.asIntBuffer().put(src);
    }

    private void copyPixmapToPixmap(Pixmap src, Pixmap dst) {
        // Simple blit clone
        // Or byte copy
        ByteBuffer srcBB = src.getPixels();
        ByteBuffer dstBB = dst.getPixels();
        srcBB.rewind();
        dstBB.rewind();
        dstBB.put(srcBB);
    }

    /**
     * Decodes LZW image data into pixel array. Adapted from John Cristy's
     * BitmapMagick.
     */
    protected void decodeBitmapData(GifFrame frame, byte[] dstPixels) {
        // Standard LZW decoder
        if (frame != null) {
            rawData.position(frame.bufferFrameStart);
        }

        int nullCode = -1;
        int npix = (frame == null) ? width * height : frame.iw * frame.ih;
        int available, clear, code_mask, code_size, end_of_information, in_code, old_code, bits, code, count, i, datum,
                data_size, first, top, bi, pi;

        if (dstPixels == null || dstPixels.length < npix) {
            dstPixels = new byte[npix]; // allocate new pixel array
        }
        if (prefix == null) {
            prefix = new short[MAX_STACK_SIZE];
        }
        if (suffix == null) {
            suffix = new byte[MAX_STACK_SIZE];
        }
        if (pixelStack == null) {
            pixelStack = new byte[MAX_STACK_SIZE + 1];
        }

        // Initialize GIF data stream decoder.
        data_size = read();
        clear = 1 << data_size;
        end_of_information = clear + 1;
        available = clear + 2;
        old_code = nullCode;
        code_size = data_size + 1;
        code_mask = (1 << code_size) - 1;
        for (code = 0; code < clear; code++) {
            prefix[code] = 0;
            suffix[code] = (byte) code;
        }

        // Decode GIF pixel stream.
        datum = bits = count = first = top = pi = bi = 0;
        for (i = 0; i < npix;) {
            if (top == 0) {
                if (bits < code_size) {
                    // Load bytes until there are enough bits for a code.
                    if (count == 0) {
                        // Read a new data block.
                        count = readBlock();
                        if (count <= 0) {
                            break;
                        }
                        bi = 0;
                    }
                    datum += (((int) block[bi]) & 0xff) << bits;
                    bits += 8;
                    bi++;
                    count--;
                    continue;
                }
                // Get the next code.
                code = datum & code_mask;
                datum >>= code_size;
                bits -= code_size;
                // Interpret the code
                if ((code > available) || (code == end_of_information)) {
                    break;
                }
                if (code == clear) {
                    // Reset decoder.
                    code_size = data_size + 1;
                    code_mask = (1 << code_size) - 1;
                    available = clear + 2;
                    old_code = nullCode;
                    continue;
                }
                if (old_code == nullCode) {
                    pixelStack[top++] = suffix[code];
                    old_code = code;
                    first = code;
                    continue;
                }
                in_code = code;
                if (code == available) {
                    pixelStack[top++] = (byte) first;
                    code = old_code;
                }
                while (code > clear) {
                    pixelStack[top++] = suffix[code];
                    code = prefix[code];
                }
                first = ((int) suffix[code]) & 0xff;
                // Add a new string to the string table,
                if (available >= MAX_STACK_SIZE) {
                    break;
                }
                pixelStack[top++] = (byte) first;
                prefix[available] = (short) old_code;
                suffix[available] = (byte) first;
                available++;
                if (((available & code_mask) == 0) && (available < MAX_STACK_SIZE)) {
                    code_size++;
                    code_mask += available;
                }
                old_code = in_code;
            }
            // Pop a pixel off the pixel stack.
            top--;
            dstPixels[pi++] = pixelStack[top];
            i++;
        }
        for (i = pi; i < npix; i++) {
            dstPixels[i] = 0; // clear missing pixels
        }
    }

    protected boolean err() {
        return status != STATUS_OK;
    }

    protected void init() {
        status = STATUS_OK;
        frameCount = 0;
        framePointer = -1;
        frames = new ArrayList<GifFrame>();
        gct = null;
    }

    protected int read() {
        int curByte = 0;
        try {
            curByte = (rawData.get() & 0xFF);
        } catch (Exception e) {
            status = STATUS_FORMAT_ERROR;
        }
        return curByte;
    }

    protected int readBlock() {
        blockSize = read();
        int n = 0;
        if (blockSize > 0) {
            try {
                int count;
                while (n < blockSize) {
                    count = blockSize - n;
                    rawData.get(block, n, count);
                    n += count;
                }
            } catch (Exception e) {
                status = STATUS_FORMAT_ERROR;
            }
        }
        return n;
    }

    protected int[] readColorTable(int ncolors) {
        int nbytes = 3 * ncolors;
        int[] tab = null;
        byte[] c = new byte[nbytes];

        try {
            rawData.get(c);

            tab = new int[256]; // max size to avoid bounds checks
            int i = 0;
            int j = 0;
            while (i < ncolors) {
                int r = ((int) c[j++]) & 0xff;
                int g = ((int) c[j++]) & 0xff;
                int b = ((int) c[j++]) & 0xff;
                // RGBA8888 packed int: R G B A
                tab[i++] = (r << 24) | (g << 16) | (b << 8) | 0xff;
            }
        } catch (Exception e) {
            status = STATUS_FORMAT_ERROR;
        }

        return tab;
    }

    protected void readContents() {
        // read GIF file content blocks
        boolean done = false;
        while (!(done || err())) {
            int code = read();
            switch (code) {
                case 0x2C: // image separator
                    readBitmap();
                    break;
                case 0x21: // extension
                    code = read();
                    switch (code) {
                        case 0xf9: // graphics control extension
                            // Start a new frame
                            currentFrame = new GifFrame();
                            readGraphicControlExt();
                            break;
                        case 0xff: // application extension
                            readBlock();
                            String app = "";
                            for (int i = 0; i < 11; i++) {
                                app += (char) block[i];
                            }
                            if (app.equals("NETSCAPE2.0")) {
                                readNetscapeExt();
                            } else {
                                skip(); // don't care
                            }
                            break;
                        case 0xfe:// comment extension
                            skip();
                            break;
                        case 0x01:// plain text extension
                            skip();
                            break;
                        default: // uninteresting extension
                            skip();
                    }
                    break;
                case 0x3b: // terminator
                    done = true;
                    break;
                case 0x00: // bad byte, but keep going and see what happens break;
                default:
                    status = STATUS_FORMAT_ERROR;
            }
        }
    }

    protected void readHeader() {
        String id = "";
        for (int i = 0; i < 6; i++) {
            id += (char) read();
        }
        if (!id.startsWith("GIF")) {
            status = STATUS_FORMAT_ERROR;
            return;
        }
        readLSD();
        if (gctFlag && !err()) {
            gct = readColorTable(gctSize);
            bgColor = gct[bgIndex];
        }
    }

    protected void readGraphicControlExt() {
        read(); // block size
        int packed = read(); // packed fields
        currentFrame.dispose = (packed & 0x1c) >> 2; // disposal method
        if (currentFrame.dispose == 0) {
            currentFrame.dispose = 1; // elect to keep old image if discretionary
        }
        currentFrame.transparency = (packed & 1) != 0;
        currentFrame.delay = readShort() * 10; // delay in milliseconds
        currentFrame.transIndex = read(); // transparent color index
        read(); // block terminator
    }

    protected void readBitmap() {
        currentFrame.ix = readShort(); // (sub)image position & size
        currentFrame.iy = readShort();
        currentFrame.iw = readShort();
        currentFrame.ih = readShort();

        int packed = read();
        lctFlag = (packed & 0x80) != 0; // 1 - local color table flag interlace
        lctSize = (int) Math.pow(2, (packed & 0x07) + 1);

        currentFrame.interlace = (packed & 0x40) != 0;
        if (lctFlag) {
            currentFrame.lct = readColorTable(lctSize); // read table
        } else {
            currentFrame.lct = null; // No local color table
        }

        currentFrame.bufferFrameStart = rawData.position(); // Save this as the decoding position pointer

        decodeBitmapData(null, mainPixels); // false decode pixel data to advance buffer
        skip();
        if (err()) {
            return;
        }

        frameCount++;
        frames.add(currentFrame); // add image to frame
    }

    protected void readLSD() {
        // logical screen size
        width = readShort();
        height = readShort();

        int packed = read();
        gctFlag = (packed & 0x80) != 0;

        gctSize = 2 << (packed & 7);
        bgIndex = read();
        pixelAspect = read();

        // Now that we know the size, init scratch arrays
        mainPixels = new byte[width * height];
        mainScratch = new int[width * height];
        copyScratch = new int[width * height];

        previousImage = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        currentImage = new Pixmap(width, height, Pixmap.Format.RGBA8888);
    }

    protected void readNetscapeExt() {
        do {
            readBlock();
            if (block[0] == 1) {
                // loop count sub-block
                int b1 = ((int) block[1]) & 0xff;
                int b2 = ((int) block[2]) & 0xff;
                loopCount = (b2 << 8) | b1;
            }
        } while ((blockSize > 0) && !err());
    }

    protected int readShort() {
        // read 16-bit value
        return rawData.getShort();
    }

    protected void skip() {
        do {
            readBlock();
        } while ((blockSize > 0) && !err());
    }
}
