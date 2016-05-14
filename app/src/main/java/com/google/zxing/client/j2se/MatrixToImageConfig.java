package com.google.zxing.client.j2se;

public final class MatrixToImageConfig {
    public static final int BLACK = -16777216;
    public static final int WHITE = -1;
    private final int offColor;
    private final int onColor;

    public MatrixToImageConfig() {
        this(BLACK, WHITE);
    }

    public MatrixToImageConfig(int onColor, int offColor) {
        this.onColor = onColor;
        this.offColor = offColor;
    }

    public int getPixelOnColor() {
        return this.onColor;
    }

    public int getPixelOffColor() {
        return this.offColor;
    }

    int getBufferedImageColorModel() {
        return (this.onColor == BLACK && this.offColor == WHITE) ? 12 : 1;
    }
}
