package com.google.zxing.qrcode.encoder;

import android.support.v4.app.DialogFragment;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.CursorAdapter;

final class MaskUtil {
    private static final int N1 = 3;
    private static final int N2 = 3;
    private static final int N3 = 40;
    private static final int N4 = 10;

    private MaskUtil() {
    }

    static int applyMaskPenaltyRule1(ByteMatrix matrix) {
        return applyMaskPenaltyRule1Internal(matrix, true) + applyMaskPenaltyRule1Internal(matrix, false);
    }

    static int applyMaskPenaltyRule2(ByteMatrix matrix) {
        int penalty = 0;
        byte[][] array = matrix.getArray();
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int y = 0;
        while (y < height - 1) {
            int x = 0;
            while (x < width - 1) {
                byte value = array[y][x];
                if (value == array[y][x + 1] && value == array[y + 1][x] && value == array[y + 1][x + 1]) {
                    penalty++;
                }
                x++;
            }
            y++;
        }
        return penalty * N2;
    }

    static int applyMaskPenaltyRule3(ByteMatrix matrix) {
        int penalty = 0;
        byte[][] array = matrix.getArray();
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int y = 0;
        while (y < height) {
            int x = 0;
            while (x < width) {
                if (x + 6 < width && array[y][x] == (byte) 1 && array[y][x + 1] == null && array[y][x + 2] == (byte) 1 && array[y][x + N2] == (byte) 1 && array[y][x + 4] == (byte) 1 && array[y][x + 5] == null && array[y][x + 6] == (byte) 1 && ((x + N4 < width && array[y][x + 7] == null && array[y][x + 8] == null && array[y][x + 9] == null && array[y][x + N4] == null) || (x - 4 >= 0 && array[y][x - 1] == null && array[y][x - 2] == null && array[y][x - 3] == null && array[y][x - 4] == null))) {
                    penalty += N3;
                }
                if (y + 6 < height && array[y][x] == (byte) 1 && array[y + 1][x] == null && array[y + 2][x] == (byte) 1 && array[y + N2][x] == (byte) 1 && array[y + 4][x] == (byte) 1 && array[y + 5][x] == null && array[y + 6][x] == (byte) 1 && ((y + N4 < height && array[y + 7][x] == null && array[y + 8][x] == null && array[y + 9][x] == null && array[y + N4][x] == null) || (y - 4 >= 0 && array[y - 1][x] == null && array[y - 2][x] == null && array[y - 3][x] == null && array[y - 4][x] == null))) {
                    penalty += N3;
                }
                x++;
            }
            y++;
        }
        return penalty;
    }

    static int applyMaskPenaltyRule4(ByteMatrix matrix) {
        int numDarkCells = 0;
        byte[][] array = matrix.getArray();
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        for (int y = 0; y < height; y++) {
            byte[] arrayY = array[y];
            for (int x = 0; x < width; x++) {
                if (arrayY[x] == 1) {
                    numDarkCells++;
                }
            }
        }
        return ((int) (Math.abs((((double) numDarkCells) / ((double) (matrix.getHeight() * matrix.getWidth()))) - 0.5d) * 20.0d)) * N4;
    }

    static boolean getDataMaskBit(int maskPattern, int x, int y) {
        int intermediate;
        int temp;
        switch (maskPattern) {
            case DialogFragment.STYLE_NORMAL /*0*/:
                intermediate = (y + x) & 1;
                break;
            case CursorAdapter.FLAG_AUTO_REQUERY /*1*/:
                intermediate = y & 1;
                break;
            case CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER /*2*/:
                intermediate = x % N2;
                break;
            case N2 /*3*/:
                intermediate = (y + x) % N2;
                break;
            case FragmentManagerImpl.ANIM_STYLE_CLOSE_EXIT /*4*/:
                intermediate = ((y >>> 1) + (x / N2)) & 1;
                break;
            case FragmentManagerImpl.ANIM_STYLE_FADE_ENTER /*5*/:
                temp = y * x;
                intermediate = (temp & 1) + (temp % N2);
                break;
            case FragmentManagerImpl.ANIM_STYLE_FADE_EXIT /*6*/:
                temp = y * x;
                intermediate = ((temp & 1) + (temp % N2)) & 1;
                break;
            case MotionEventCompat.ACTION_HOVER_MOVE /*7*/:
                intermediate = (((y * x) % N2) + ((y + x) & 1)) & 1;
                break;
            default:
                throw new IllegalArgumentException("Invalid mask pattern: " + maskPattern);
        }
        if (intermediate == 0) {
            return true;
        }
        return false;
    }

    private static int applyMaskPenaltyRule1Internal(ByteMatrix matrix, boolean isHorizontal) {
        int penalty = 0;
        int iLimit = isHorizontal ? matrix.getHeight() : matrix.getWidth();
        int jLimit = isHorizontal ? matrix.getWidth() : matrix.getHeight();
        byte[][] array = matrix.getArray();
        int i = 0;
        while (i < iLimit) {
            int numSameBitCells = 0;
            int prevBit = -1;
            int j = 0;
            while (j < jLimit) {
                int bit = isHorizontal ? array[i][j] : array[j][i];
                if (bit == prevBit) {
                    numSameBitCells++;
                } else {
                    if (numSameBitCells >= 5) {
                        penalty += (numSameBitCells - 5) + N2;
                    }
                    numSameBitCells = 1;
                    prevBit = bit;
                }
                j++;
            }
            if (numSameBitCells > 5) {
                penalty += (numSameBitCells - 5) + N2;
            }
            i++;
        }
        return penalty;
    }
}
