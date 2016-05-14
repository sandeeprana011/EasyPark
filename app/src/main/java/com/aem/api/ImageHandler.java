package com.aem.api;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;

class ImageHandler {
    int Byte_Offset;
    short Filetype;
    public int HzPixelPerMeter;
    public int NoOfColorUsed;
    public int NoOfImptColor;
    int Reserved;
    int Size_infoheader;
    int Totalsize;
    public int VtPixelPerMeter;
    public int actNoOfBytes;
    public short bitplanes;
    public short bits_per_pixel;
    public int compressionType;
    Context context;
    public int nHorizontalLen;
    public int nVeritcalLen;

    public ImageHandler(Context context) {
        this.context = context;
    }

    byte[] getMonoChromeImagePacket(Bitmap bitmap, byte image_alignment) {
        BitmapConvertor convertor = new BitmapConvertor(this.context);
        if (!convertor.convertBitmap(bitmap, "my_monochrome_image").contentEquals("Success")) {
            return null;
        }
        byte[] imageBytes = getImageBytes();
        if (imageBytes != null) {
            return getImagePacket(imageBytes, convertor.mDataWidth, convertor.mHeight, image_alignment);
        }
        return null;
    }

    private byte[] getImagePacket(byte[] bytes, int width, int height, byte image_alignment) {
        byte mode = (byte) 100;
        if (height <= 255) {
            mode = (byte) 100;
        } else if (height <= 510) {
            mode = (byte) 101;
        } else if (height <= 756) {
            mode = (byte) 102;
        } else if (height <= 1020) {
            mode = (byte) 103;
        }
        CopyBufToStructBMP(bytes);
        long actualWidth = (long) width;
        long offset = (long) this.Byte_Offset;
        long actualHeight = (long) height;
        long totalSize = (long) this.Totalsize;
        int reWidth = (int) (((long) (bytes.length - this.Byte_Offset)) / actualHeight);
        long pad = ((long) (reWidth * 8)) - actualWidth;
        byte[] dataWithoutHeader = new byte[(bytes.length - 62)];
        int i = offset;
        while (true) {
            if (i >= ((long) bytes.length)) {
                break;
            }
            dataWithoutHeader[(int) (i - 62)] = bytes[(int) i];
            i++;
        }
        byte[] imagePacket = new byte[(dataWithoutHeader.length + 6)];
        imagePacket[0] = (byte) 27;
        imagePacket[1] = (byte) 42;
        imagePacket[2] = mode;
        imagePacket[3] = image_alignment;
        imagePacket[4] = (byte) reWidth;
        imagePacket[5] = (byte) ((int) actualHeight);
        int a = 0;
        int[] iArr = new int[]{(int) actualHeight, reWidth};
        byte[][] temp = (byte[][]) Array.newInstance(Byte.TYPE, iArr);
        i = 0;
        while (true) {
            if (((long) i) >= actualHeight) {
                return imagePacket;
            }
            boolean flag = true;
            for (int j = 0; j < reWidth; j++) {
                if (((long) j) < actualWidth / 8) {
                    temp[i][j] = (byte) (dataWithoutHeader[(dataWithoutHeader.length - ((i + 1) * reWidth)) + j] ^ MotionEventCompat.ACTION_MASK);
                } else {
                    int c = ((int) actualWidth) % 8;
                    if (c == 0 || !flag) {
                        temp[i][j] = dataWithoutHeader[(dataWithoutHeader.length - ((i + 1) * reWidth)) + j];
                    } else {
                        temp[i][j] = (byte) (dataWithoutHeader[(dataWithoutHeader.length - ((i + 1) * reWidth)) + j] ^ ((byte) (MotionEventCompat.ACTION_MASK << (8 - c))));
                        byte byt = temp[i][j];
                        flag = false;
                    }
                }
                imagePacket[a + 6] = temp[i][j];
                a++;
            }
            i++;
        }
    }

    private void CopyBufToStructBMP(byte[] RawBuf) {
        this.Filetype = (short) ((RawBuf[1] * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[0]);
        int idx = 0 + 2;
        this.Totalsize = (((((RawBuf[5] * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[4]) * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[3]) * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[idx];
        idx += 4;
        this.Reserved = (((((RawBuf[9] * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[8]) * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[7]) * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[idx];
        idx += 4;
        this.Byte_Offset = (((((RawBuf[13] * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[12]) * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[11]) * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[idx];
        idx += 4;
        this.Size_infoheader = (((((RawBuf[17] * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[16]) * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[15]) * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[idx];
        idx += 4;
        this.nHorizontalLen = (((((RawBuf[21] * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[20]) * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[19]) * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[idx];
        idx += 4;
        this.nVeritcalLen = ((((((RawBuf[25] * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[24]) * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[23]) * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[idx]) & MotionEventCompat.ACTION_MASK;
        idx += 4;
        this.bitplanes = (short) ((RawBuf[27] * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[idx]);
        idx += 2;
        this.bits_per_pixel = (short) ((RawBuf[29] * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[idx]);
        idx += 2;
        this.compressionType = (((((RawBuf[33] * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[32]) * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[31]) * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[idx];
        idx += 4;
        this.actNoOfBytes = (((((RawBuf[37] * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[36]) * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[35]) * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[idx];
        idx += 4;
        this.HzPixelPerMeter = (((((RawBuf[41] * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[40]) * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[39]) * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[idx];
        idx += 4;
        this.VtPixelPerMeter = (((((RawBuf[45] * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[44]) * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[43]) * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[idx];
        idx += 4;
        this.NoOfColorUsed = (((((RawBuf[49] * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[48]) * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[47]) * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[idx];
        idx += 4;
        this.NoOfImptColor = (((((RawBuf[53] * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[52]) * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[51]) * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) + RawBuf[idx];
        idx += 4;
    }

    private byte[] getImageBytes() {
        try {
            FileInputStream fin = this.context.openFileInput("my_monochrome_image.bmp");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT];
            while (true) {
                int readNum = fin.read(buf);
                if (readNum == -1) {
                    byte[] bytes = bos.toByteArray();
                    fin.close();
                    return bytes;
                }
                byte[] temp = new byte[readNum];
                for (int j = 0; j < readNum; j++) {
                    temp[j] = buf[j];
                }
                bos.write(temp, 0, readNum);
            }
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e2) {
            return null;
        }
    }

    private Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / ((float) width);
        float scaleHeight = ((float) newHeight) / ((float) height);
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
    }

    private Bitmap ConvertToBlackAndWhite(Bitmap sampleBitmap) {
        ColorMatrix bwMatrix = new ColorMatrix();
        bwMatrix.setSaturation(0.0f);
        ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(bwMatrix);
        Bitmap rBitmap = sampleBitmap.copy(Config.ARGB_8888, true);
        Paint paint = new Paint();
        paint.setColorFilter(colorFilter);
        new Canvas(rBitmap).drawBitmap(rBitmap, 0.0f, 0.0f, paint);
        return rBitmap;
    }
}
