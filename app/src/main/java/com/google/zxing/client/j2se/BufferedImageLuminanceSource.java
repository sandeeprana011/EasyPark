package com.google.zxing.client.j2se;

import com.google.zxing.LuminanceSource;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public final class BufferedImageLuminanceSource extends LuminanceSource {
    private final BufferedImage image;
    private final int left;
    private final int top;

    public BufferedImageLuminanceSource(BufferedImage image) {
        this(image, 0, 0, image.getWidth(), image.getHeight());
    }

    public BufferedImageLuminanceSource(BufferedImage image, int left, int top, int width, int height) {
        super(width, height);
        int sourceWidth = image.getWidth();
        int sourceHeight = image.getHeight();
        if (left + width > sourceWidth || top + height > sourceHeight) {
            throw new IllegalArgumentException("Crop rectangle does not fit within image data.");
        }
        if (image.getAlphaRaster() != null) {
            int[] buffer = new int[width];
            for (int y = top; y < top + height; y++) {
                image.getRGB(left, y, width, 1, buffer, 0, sourceWidth);
                boolean rowChanged = false;
                for (int x = 0; x < width; x++) {
                    if ((buffer[x] & MatrixToImageConfig.BLACK) == 0) {
                        buffer[x] = -1;
                        rowChanged = true;
                    }
                }
                if (rowChanged) {
                    image.setRGB(left, y, width, 1, buffer, 0, sourceWidth);
                }
            }
        }
        this.image = new BufferedImage(sourceWidth, sourceHeight, 10);
        this.image.getGraphics().drawImage(image, 0, 0, null);
        this.left = left;
        this.top = top;
    }

    public byte[] getRow(int y, byte[] row) {
        if (y < 0 || y >= getHeight()) {
            throw new IllegalArgumentException("Requested row is outside the image: " + y);
        }
        Object row2;
        int width = getWidth();
        if (row == null || row.length < width) {
            row2 = new byte[width];
        }
        this.image.getRaster().getDataElements(this.left, this.top + y, width, 1, row2);
        return row2;
    }

    public byte[] getMatrix() {
        int width = getWidth();
        int height = getHeight();
        byte[] matrix = new byte[(width * height)];
        this.image.getRaster().getDataElements(this.left, this.top, width, height, matrix);
        return matrix;
    }

    public boolean isCropSupported() {
        return true;
    }

    public LuminanceSource crop(int left, int top, int width, int height) {
        return new BufferedImageLuminanceSource(this.image, this.left + left, this.top + top, width, height);
    }

    public boolean isRotateSupported() {
        return true;
    }

    public LuminanceSource rotateCounterClockwise() {
        int sourceWidth = this.image.getWidth();
        int sourceHeight = this.image.getHeight();
        AffineTransform transform = new AffineTransform(0.0d, -1.0d, 1.0d, 0.0d, 0.0d, (double) sourceWidth);
        BufferedImage rotatedImage = new BufferedImage(sourceHeight, sourceWidth, 10);
        Graphics2D g = rotatedImage.createGraphics();
        g.drawImage(this.image, transform, null);
        g.dispose();
        int width = getWidth();
        return new BufferedImageLuminanceSource(rotatedImage, this.top, sourceWidth - (this.left + width), getHeight(), width);
    }

    public LuminanceSource rotateCounterClockwise45() {
        int width = getWidth();
        int height = getHeight();
        int oldCenterX = this.left + (width / 2);
        int oldCenterY = this.top + (height / 2);
        AffineTransform transform = AffineTransform.getRotateInstance(Math.toRadians(-45.0d), (double) oldCenterX, (double) oldCenterY);
        int sourceDimension = Math.max(this.image.getWidth(), this.image.getHeight());
        BufferedImage rotatedImage = new BufferedImage(sourceDimension, sourceDimension, 10);
        Graphics2D g = rotatedImage.createGraphics();
        g.drawImage(this.image, transform, null);
        g.dispose();
        int halfDimension = Math.max(width, height) / 2;
        int newLeft = Math.max(0, oldCenterX - halfDimension);
        int newTop = Math.max(0, oldCenterY - halfDimension);
        return new BufferedImageLuminanceSource(rotatedImage, newLeft, newTop, Math.min(sourceDimension - 1, oldCenterX + halfDimension) - newLeft, Math.min(sourceDimension - 1, oldCenterY + halfDimension) - newTop);
    }
}
