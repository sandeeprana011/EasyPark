package com.google.zxing.client.j2se;

import com.google.zxing.common.BitMatrix;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;

public final class MatrixToImageWriter {
    private static final MatrixToImageConfig DEFAULT_CONFIG;

    static {
        DEFAULT_CONFIG = new MatrixToImageConfig();
    }

    private MatrixToImageWriter() {
    }

    public static BufferedImage toBufferedImage(BitMatrix matrix) {
        return toBufferedImage(matrix, DEFAULT_CONFIG);
    }

    public static BufferedImage toBufferedImage(BitMatrix matrix, MatrixToImageConfig config) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, config.getBufferedImageColorModel());
        int onColor = config.getPixelOnColor();
        int offColor = config.getPixelOffColor();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int i;
                if (matrix.get(x, y)) {
                    i = onColor;
                } else {
                    i = offColor;
                }
                image.setRGB(x, y, i);
            }
        }
        return image;
    }

    public static void writeToFile(BitMatrix matrix, String format, File file) throws IOException {
        writeToFile(matrix, format, file, DEFAULT_CONFIG);
    }

    public static void writeToFile(BitMatrix matrix, String format, File file, MatrixToImageConfig config) throws IOException {
        if (!ImageIO.write(toBufferedImage(matrix, config), format, file)) {
            throw new IOException("Could not write an image of format " + format + " to " + file);
        }
    }

    public static void writeToStream(BitMatrix matrix, String format, OutputStream stream) throws IOException {
        writeToStream(matrix, format, stream, DEFAULT_CONFIG);
    }

    public static void writeToStream(BitMatrix matrix, String format, OutputStream stream, MatrixToImageConfig config) throws IOException {
        if (!ImageIO.write(toBufferedImage(matrix, config), format, stream)) {
            throw new IOException("Could not write an image of format " + format);
        }
    }
}
