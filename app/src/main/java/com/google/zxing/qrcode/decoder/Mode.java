package com.google.zxing.qrcode.decoder;

import android.support.v4.app.DialogFragment;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.CursorAdapter;
import com.google.zxing.qrcode.encoder.QRCode;

public enum Mode {
    TERMINATOR(new int[]{0, 0, 0}, 0),
    NUMERIC(new int[]{10, 12, 14}, 1),
    ALPHANUMERIC(new int[]{9, 11, 13}, 2),
    STRUCTURED_APPEND(new int[]{0, 0, 0}, 3),
    BYTE(new int[]{8, 16, 16}, 4),
    ECI(new int[]{0, 0, 0}, 7),
    KANJI(new int[]{8, 10, 12}, 8),
    FNC1_FIRST_POSITION(new int[]{0, 0, 0}, 5),
    FNC1_SECOND_POSITION(new int[]{0, 0, 0}, 9),
    HANZI(new int[]{8, 10, 12}, 13);
    
    private final int bits;
    private final int[] characterCountBitsForVersions;

    private Mode(int[] characterCountBitsForVersions, int bits) {
        this.characterCountBitsForVersions = characterCountBitsForVersions;
        this.bits = bits;
    }

    public static Mode forBits(int bits) {
        switch (bits) {
            case DialogFragment.STYLE_NORMAL /*0*/:
                return TERMINATOR;
            case CursorAdapter.FLAG_AUTO_REQUERY /*1*/:
                return NUMERIC;
            case CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER /*2*/:
                return ALPHANUMERIC;
            case FragmentManagerImpl.ANIM_STYLE_CLOSE_ENTER /*3*/:
                return STRUCTURED_APPEND;
            case FragmentManagerImpl.ANIM_STYLE_CLOSE_EXIT /*4*/:
                return BYTE;
            case FragmentManagerImpl.ANIM_STYLE_FADE_ENTER /*5*/:
                return FNC1_FIRST_POSITION;
            case MotionEventCompat.ACTION_HOVER_MOVE /*7*/:
                return ECI;
            case QRCode.NUM_MASK_PATTERNS /*8*/:
                return KANJI;
            case MotionEventCompat.ACTION_HOVER_ENTER /*9*/:
                return FNC1_SECOND_POSITION;
            case 13:
                return HANZI;
            default:
                throw new IllegalArgumentException();
        }
    }

    public int getCharacterCountBits(Version version) {
        int offset;
        int number = version.getVersionNumber();
        if (number <= 9) {
            offset = 0;
        } else if (number <= 26) {
            offset = 1;
        } else {
            offset = 2;
        }
        return this.characterCountBitsForVersions[offset];
    }

    public int getBits() {
        return this.bits;
    }
}
