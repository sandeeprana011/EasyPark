package com.aem.api;

import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import java.io.FileOutputStream;

class BMPFile {
    private static final int BITMAPFILEHEADER_SIZE = 14;
    private static final int BITMAPINFOHEADER_SIZE = 40;
    private int bfOffBits;
    private int bfReserved1;
    private int bfReserved2;
    private int bfSize;
    private byte[] bfType;
    private int biBitCount;
    private int biClrImportant;
    private int biClrUsed;
    private int biCompression;
    private int biHeight;
    private int biPlanes;
    private int biSize;
    private int biSizeImage;
    private int biWidth;
    private int biXPelsPerMeter;
    private int biYPelsPerMeter;
    private byte[] bitmap;
    private byte[] colorPalette;
    int scanLineSize;

    public BMPFile() {
        this.bfType = new byte[]{(byte) 66, (byte) 77};
        this.bfSize = 0;
        this.bfReserved1 = 0;
        this.bfReserved2 = 0;
        this.bfOffBits = 62;
        this.biSize = BITMAPINFOHEADER_SIZE;
        this.biWidth = 0;
        this.biHeight = 0;
        this.biPlanes = 1;
        this.biBitCount = 1;
        this.biCompression = 0;
        this.biSizeImage = 0;
        this.biXPelsPerMeter = 0;
        this.biYPelsPerMeter = 0;
        this.biClrUsed = 2;
        this.biClrImportant = 2;
        this.scanLineSize = 0;
        byte[] bArr = new byte[8];
        bArr[3] = (byte) -1;
        bArr[4] = (byte) -1;
        bArr[5] = (byte) -1;
        bArr[6] = (byte) -1;
        bArr[7] = (byte) -1;
        this.colorPalette = bArr;
    }

    public void saveBitmap(FileOutputStream fos, byte[] imagePix, int parWidth, int parHeight) {
        try {
            save(fos, imagePix, parWidth, parHeight);
        } catch (Exception saveEx) {
            saveEx.printStackTrace();
        }
    }

    private void save(FileOutputStream fos, byte[] imagePix, int parWidth, int parHeight) {
        try {
            convertImage(imagePix, parWidth, parHeight);
            writeBitmapFileHeader(fos);
            writeBitmapInfoHeader(fos);
            writePixelArray(fos);
        } catch (Exception saveEx) {
            saveEx.printStackTrace();
        }
    }

    private boolean convertImage(byte[] imagePix, int parWidth, int parHeight) {
        this.bitmap = imagePix;
        this.bfSize = ((((parWidth + 31) / 32) * 4) * parHeight) + 62;
        this.biWidth = parWidth;
        this.biHeight = parHeight;
        this.scanLineSize = (((parWidth * 1) + 31) / 32) * 4;
        return true;
    }

    private void writeBitmapFileHeader(FileOutputStream fos) {
        try {
            fos.write(this.bfType);
            fos.write(intToDWord(this.bfSize));
            fos.write(intToWord(this.bfReserved1));
            fos.write(intToWord(this.bfReserved2));
            fos.write(intToDWord(this.bfOffBits));
        } catch (Exception wbfh) {
            wbfh.printStackTrace();
        }
    }

    private void writeBitmapInfoHeader(FileOutputStream fos) {
        try {
            fos.write(intToDWord(this.biSize));
            fos.write(intToDWord(this.biWidth));
            fos.write(intToDWord(this.biHeight));
            fos.write(intToWord(this.biPlanes));
            fos.write(intToWord(this.biBitCount));
            fos.write(intToDWord(this.biCompression));
            fos.write(intToDWord(this.biSizeImage));
            fos.write(intToDWord(this.biXPelsPerMeter));
            fos.write(intToDWord(this.biYPelsPerMeter));
            fos.write(intToDWord(this.biClrUsed));
            fos.write(intToDWord(this.biClrImportant));
            fos.write(this.colorPalette);
        } catch (Exception wbih) {
            wbih.printStackTrace();
        }
    }

    private void writePixelArray(FileOutputStream fos) {
        try {
            for (int i = this.biHeight; i > 0; i--) {
                for (int k = (i - 1) * this.scanLineSize; k < ((i - 1) * this.scanLineSize) + this.scanLineSize; k++) {
                    fos.write(this.bitmap[k] & MotionEventCompat.ACTION_MASK);
                }
            }
        } catch (Exception e) {
            Log.e("BMPFile", e.toString());
        }
    }

    private byte[] intToWord(int parValue) {
        return new byte[]{(byte) (parValue & MotionEventCompat.ACTION_MASK), (byte) ((parValue >> 8) & MotionEventCompat.ACTION_MASK)};
    }

    private byte[] intToDWord(int parValue) {
        return new byte[]{(byte) (parValue & MotionEventCompat.ACTION_MASK), (byte) ((parValue >> 8) & MotionEventCompat.ACTION_MASK), (byte) ((parValue >> 16) & MotionEventCompat.ACTION_MASK), (byte) ((parValue >> 24) & MotionEventCompat.ACTION_MASK)};
    }
}
