package com.aem.api;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.Log;
import java.io.FileNotFoundException;
import java.io.IOException;

class BitmapConvertor {
    private static final String TAG = "BitmapConvertor";
    private Context mContext;
    private byte[] mDataArray;
    int mDataWidth;
    private String mFileName;
    int mHeight;
    private ProgressDialog mPd;
    byte[] mRawBitmapData;
    private String mStatus;
    int mWidth;

    public BitmapConvertor(Context context) {
        this.mContext = context;
    }

    public String convertBitmap(Bitmap inputBitmap, String fileName) {
        this.mWidth = inputBitmap.getWidth();
        this.mHeight = inputBitmap.getHeight();
        this.mFileName = fileName;
        this.mDataWidth = (((this.mWidth + 31) / 32) * 4) * 8;
        this.mDataArray = new byte[(this.mDataWidth * this.mHeight)];
        this.mRawBitmapData = new byte[((this.mDataWidth * this.mHeight) / 8)];
        convertArgbToGrayscale(inputBitmap, this.mWidth, this.mHeight);
        createRawMonochromeData();
        this.mStatus = saveImage(this.mFileName, this.mDataWidth, this.mHeight);
        return this.mStatus;
    }

    private void convertArgbToGrayscale(Bitmap bmpOriginal, int width, int height) {
        int k = 0;
        for (int x = 0; x < height; x++) {
            int y = 0;
            while (y < width) {
                int pixel = bmpOriginal.getPixel(y, x);
                int B = (int) (((0.299d * ((double) Color.red(pixel))) + (0.587d * ((double) Color.green(pixel)))) + (0.114d * ((double) Color.blue(pixel))));
                int G = B;
                if (B < AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS) {
                    this.mDataArray[k] = (byte) 0;
                } else {
                    this.mDataArray[k] = (byte) 1;
                }
                y++;
                k++;
            }
            if (this.mDataWidth > width) {
                int p = width;
                while (p < this.mDataWidth) {
                    try {
                        this.mDataArray[k] = (byte) 1;
                        p++;
                        k++;
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                        return;
                    }
                }
                continue;
            }
        }
    }

    private void createRawMonochromeData() {
        int length = 0;
        for (int i = 0; i < this.mDataArray.length; i += 8) {
            byte first = this.mDataArray[i];
            for (int j = 0; j < 7; j++) {
                first = (byte) ((first << 1) | this.mDataArray[i + j]);
            }
            this.mRawBitmapData[length] = first;
            length++;
        }
    }

    private String saveImage(String fileName, int width, int height) {
        try {
            new BMPFile().saveBitmap(this.mContext.openFileOutput(new StringBuilder(String.valueOf(fileName)).append(".bmp").toString(), 0), this.mRawBitmapData, width, height);
            return "Success";
        } catch (FileNotFoundException e) {
            return "FileNotFound";
        } catch (IOException e2) {
            return "Memory Access Denied";
        }
    }
}
