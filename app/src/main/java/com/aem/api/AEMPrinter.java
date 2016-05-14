package com.aem.api;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.support.v4.view.MotionEventCompat;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.IOException;

public class AEMPrinter {
    private static final byte CARRIAGE_RETURN = (byte) 13;
    public static final byte DOUBLE_HEIGHT = (byte) 8;
    public static final byte DOUBLE_WIDTH = (byte) 4;
    public static final byte FONT_001 = (byte) 3;
    public static final byte FONT_002 = (byte) 20;
    public static final byte FONT_003 = (byte) 22;
    public static final byte FONT_NORMAL = (byte) 6;
    public static final byte IMAGE_CENTER_ALIGNMENT = (byte) 99;
    public static final byte IMAGE_LEFT_ALIGNMENT = (byte) 108;
    public static final byte IMAGE_RIGHT_ALIGNMENT = (byte) 114;
    private static final byte LINE_FEED = (byte) 10;
    private static final byte NEGATIVE_CHAR = (byte) 14;
    public static final byte TEXT_ALIGNMENT_CENTER = (byte) 3;
    public static final byte TEXT_ALIGNMENT_LEFT = (byte) 1;
    public static final byte TEXT_ALIGNMENT_RIGHT = (byte) 2;
    private static final byte UNDERLINE = (byte) 21;
    private final byte BARCODE_TYPE_CODE39;
    private final byte BARCODE_TYPE_EAN13;
    private final byte BARCODE_TYPE_EAN8;
    private final byte BARCODE_TYPE_UPCA;
    BluetoothSocket bluetoothSocket;
    Context m_Context;

    public enum BARCODE_HEIGHT {
        DOUBLEDENSITY_FULLHEIGHT,
        TRIPLEDENSITY_FULLHEIGHT,
        DOUBLEDENSITY_HALFHEIGHT,
        TRIPLEDENSITY_HALFHEIGHT
    }

    public enum BARCODE_TYPE {
        UPCA,
        EAN13,
        EAN8,
        CODE39
    }

    public AEMPrinter(BluetoothSocket socket) {
        this.BARCODE_TYPE_UPCA = (byte) 65;
        this.BARCODE_TYPE_EAN13 = (byte) 67;
        this.BARCODE_TYPE_EAN8 = (byte) 68;
        this.BARCODE_TYPE_CODE39 = (byte) 69;
        this.bluetoothSocket = socket;
    }

    public void setFontType(byte FONT) throws IOException {
        this.bluetoothSocket.getOutputStream().write(FONT);
    }

    public void setFontSize(byte DOUBLE_DIMENSION) throws IOException {
        this.bluetoothSocket.getOutputStream().write(DOUBLE_DIMENSION);
    }

    public void printInNegative() throws IOException {
        this.bluetoothSocket.getOutputStream().write(14);
    }

    public void enableUnderline() throws IOException {
        this.bluetoothSocket.getOutputStream().write(21);
    }

    public void setLineFeed(int noOfFeeds) throws IOException {
        for (int i = 0; i < noOfFeeds; i++) {
            this.bluetoothSocket.getOutputStream().write(10);
        }
    }

    public void setCarriageReturn() throws IOException {
        this.bluetoothSocket.getOutputStream().write(13);
    }

    public void print(String text) throws IOException {
        if (this.bluetoothSocket != null) {
            this.bluetoothSocket.getOutputStream().write(text.getBytes(), 0, text.getBytes().length);
            setCarriageReturn();
        }
    }

    public Bitmap createQRCode(String text) throws WriterException {
        BitMatrix bm = new QRCodeWriter().encode(Uri.encode(text, "UTF-8"), BarcodeFormat.QR_CODE, 350, MotionEventCompat.ACTION_MASK);
        Bitmap bitmap = Bitmap.createBitmap(350, MotionEventCompat.ACTION_MASK, Config.ARGB_8888);
        for (int i = 0; i < 350; i++) {
            for (int j = 0; j < MotionEventCompat.ACTION_MASK; j++) {
                bitmap.setPixel(i, j, bm.get(i, j) ? MatrixToImageConfig.BLACK : -1);
            }
        }
        return bitmap;
    }

    public void printBarcode(String barcodeData, BARCODE_TYPE barcodetype, BARCODE_HEIGHT barcodeheight) throws IOException {
        if (this.bluetoothSocket != null) {
            byte[] barcodepacket = createBarcodePacket(barcodeData.getBytes(), barcodetype, barcodeheight);
            if (barcodepacket != null) {
                this.bluetoothSocket.getOutputStream().write(barcodepacket, 0, barcodepacket.length);
            }
        }
    }

    private byte[] createBarcodePacket(byte[] barcodeBytes, BARCODE_TYPE barcodetype, BARCODE_HEIGHT height) {
        byte[] barcodePacket;
        int i;
        if (barcodetype == BARCODE_TYPE.CODE39) {
            barcodePacket = new byte[(barcodeBytes.length + 7)];
            barcodePacket[0] = (byte) 29;
            barcodePacket[1] = (byte) 107;
            barcodePacket[2] = (byte) 69;
            barcodePacket[3] = (byte) (barcodeBytes.length + 2);
            barcodePacket[4] = getBarcodeHeight(height);
            barcodePacket[5] = (byte) 42;
            i = 0;
            while (i < barcodeBytes.length) {
                barcodePacket[i + 6] = barcodeBytes[i];
                i++;
            }
            barcodePacket[i + 6] = (byte) 42;
            return barcodePacket;
        } else if (barcodetype == BARCODE_TYPE.UPCA) {
            barcodePacket = new byte[(barcodeBytes.length + 5)];
            barcodePacket[0] = (byte) 29;
            barcodePacket[1] = (byte) 107;
            barcodePacket[2] = (byte) 65;
            barcodePacket[3] = (byte) barcodeBytes.length;
            barcodePacket[4] = getBarcodeHeight(height);
            for (i = 0; i < barcodeBytes.length; i++) {
                barcodePacket[i + 5] = barcodeBytes[i];
            }
            return barcodePacket;
        } else if (barcodetype == BARCODE_TYPE.EAN13) {
            barcodePacket = new byte[(barcodeBytes.length + 5)];
            barcodePacket[0] = (byte) 29;
            barcodePacket[1] = (byte) 107;
            barcodePacket[2] = (byte) 67;
            barcodePacket[3] = (byte) barcodeBytes.length;
            barcodePacket[4] = getBarcodeHeight(height);
            for (i = 0; i < barcodeBytes.length; i++) {
                barcodePacket[i + 5] = barcodeBytes[i];
            }
            return barcodePacket;
        } else if (barcodetype != BARCODE_TYPE.EAN8) {
            return null;
        } else {
            barcodePacket = new byte[(barcodeBytes.length + 5)];
            barcodePacket[0] = (byte) 29;
            barcodePacket[1] = (byte) 107;
            barcodePacket[2] = (byte) 68;
            barcodePacket[3] = (byte) barcodeBytes.length;
            barcodePacket[4] = getBarcodeHeight(height);
            for (i = 0; i < barcodeBytes.length; i++) {
                barcodePacket[i + 5] = barcodeBytes[i];
            }
            return barcodePacket;
        }
    }

    private byte getBarcodeHeight(BARCODE_HEIGHT height) {
        if (height == BARCODE_HEIGHT.DOUBLEDENSITY_FULLHEIGHT) {
            return (byte) 97;
        }
        if (height == BARCODE_HEIGHT.TRIPLEDENSITY_FULLHEIGHT) {
            return (byte) 98;
        }
        if (height == BARCODE_HEIGHT.DOUBLEDENSITY_HALFHEIGHT) {
            return IMAGE_CENTER_ALIGNMENT;
        }
        if (height == BARCODE_HEIGHT.TRIPLEDENSITY_HALFHEIGHT) {
            return (byte) 100;
        }
        return (byte) 97;
    }

    public void printImage(Bitmap originalBitmap, Context context, byte image_alignment) throws IOException {
        this.m_Context = context;
        byte[] imagePacket = new ImageHandler(context).getMonoChromeImagePacket(originalBitmap, image_alignment);
        if (imagePacket != null) {
            this.bluetoothSocket.getOutputStream().write(imagePacket, 0, imagePacket.length);
            if (deleteFile()) {
                System.out.print("b");
            } else {
                System.out.print("b");
            }
        }
    }

    private boolean deleteFile() {
        return this.m_Context.deleteFile("my_monochrome_image.bmp");
    }
}
