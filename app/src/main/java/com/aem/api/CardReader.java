package com.aem.api;

import android.bluetooth.BluetoothSocket;
import android.support.v4.view.MotionEventCompat;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CardReader {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$aem$api$CardReader$CARD_TRACK = null;
    private static boolean DLTest = false;
    private static final int DL_NUM = 4;
    private static final int DOB = 3;
    private static final int DOI = 6;
    private static final int ISS_AUTH = 5;
    private static boolean MSRReadTest = false;
    private static final int NAME = 1;
    private static boolean RCTest = false;
    private static final int REG_NAME = 14;
    private static final int REG_NUM = 13;
    private static final int REG_UPTO = 15;
    private static final int SWD_OF = 2;
    private static final int VALID_NTP = 8;
    private static final int VALID_TP = 7;
    private static final int VEH_INFO_1 = 9;
    static String buffer;
    private static InputStream dis;
    private static OutputStream dos;
    private BluetoothSocket bluetoothSocket;
    private IAemCardScanner scannerImplementer;

    class AdditionalAndDiscritionaryData {
        String Cvv;
        String Pvki;
        String expiryDate;
        String pvv;
        String serviceCode;

        AdditionalAndDiscritionaryData() {
        }
    }

    public enum CARD_TRACK {
        TRACK1,
        TRACK2
    }

    public class DLCardData {
        public String DL_NUM;
        public String DOB;
        public String DOI;
        public String ISS_AUTH;
        public String NAME;
        public String SWD_OF;
        public String VALID_NTP;
        public String VALID_TP;
        public String VEH_INFO_1;
    }

    private class InputThread extends Thread {
        private InputThread() {
        }

        public void run() {
            try {
                byte[] buffer = new byte[CardReader.NAME];
                while (true) {
                    CardReader.dis.read(buffer);
                    CardReader.this.readPacketFromPrinter((char) buffer[0]);
                }
            } catch (Exception e) {
            }
        }
    }

    public class MSRCardData {
        public String m_AccoundHolderName;
        public String m_cardNumber;
        public String m_cvvNumber;
        public String m_expiryDate;
        public String m_pvkiNumber;
        public String m_pvvNumber;
        public String m_serviceCode;
    }

    public class RCCardData {
        public String REG_NAME;
        public String REG_NUM;
        public String REG_UPTO;
    }

    static /* synthetic */ int[] $SWITCH_TABLE$com$aem$api$CardReader$CARD_TRACK() {
        int[] iArr = $SWITCH_TABLE$com$aem$api$CardReader$CARD_TRACK;
        if (iArr == null) {
            iArr = new int[CARD_TRACK.values().length];
            try {
                iArr[CARD_TRACK.TRACK1.ordinal()] = NAME;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[CARD_TRACK.TRACK2.ordinal()] = SWD_OF;
            } catch (NoSuchFieldError e2) {
            }
            $SWITCH_TABLE$com$aem$api$CardReader$CARD_TRACK = iArr;
        }
        return iArr;
    }

    static {
        MSRReadTest = false;
        DLTest = false;
        RCTest = false;
        dis = null;
        dos = null;
        buffer = "";
    }

    public CardReader(BluetoothSocket socket, IAemCardScanner readerImpl) {
        this.bluetoothSocket = socket;
        this.scannerImplementer = readerImpl;
        if (dis != null) {
            dis = null;
        }
        if (dos != null) {
            dos = null;
        }
        try {
            dis = this.bluetoothSocket.getInputStream();
            dos = this.bluetoothSocket.getOutputStream();
            new InputThread().start();
        } catch (IOException e) {
        }
    }

    private void readPacketFromPrinter(char ch) {
        try {
            buffer += ch;
            String dataTrack1 = "";
            String dataTrack2 = "";
            if (buffer.contains("%")) {
                this.scannerImplementer.onScanMSR(readTrack(CARD_TRACK.TRACK1, buffer), CARD_TRACK.TRACK1);
            } else if (buffer.contains(";")) {
                dataTrack2 = readTrack(CARD_TRACK.TRACK2, buffer);
            }
            MSRReadTest = false;
            buffer = "";
        } catch (Exception e) {
        }
    }

    public RCCardData decodeRCData(String tempBuff) {
        RCCardData rcCardData = new RCCardData();
        char[] tempCharBuff = tempBuff.toCharArray();
        rcCardData.REG_NUM = getDataFromDL(tempCharBuff, REG_NUM);
        rcCardData.REG_NAME = getDataFromDL(tempCharBuff, REG_NAME);
        rcCardData.REG_UPTO = getDataFromDL(tempCharBuff, REG_UPTO);
        return rcCardData;
    }

    public DLCardData decodeDLData(String tempBuff) {
        DLCardData dlCardData = new DLCardData();
        char[] tempCharBuff = tempBuff.toCharArray();
        dlCardData.NAME = getDataFromDL(tempCharBuff, NAME);
        dlCardData.SWD_OF = getDataFromDL(tempCharBuff, SWD_OF);
        dlCardData.DOB = getDataFromDL(tempCharBuff, DOB);
        dlCardData.DL_NUM = getDataFromDL(tempCharBuff, DL_NUM);
        dlCardData.ISS_AUTH = getDataFromDL(tempCharBuff, ISS_AUTH);
        dlCardData.DOI = getDataFromDL(tempCharBuff, DOI);
        dlCardData.VALID_TP = "VALID_TP:" + getDataFromDL(tempCharBuff, VALID_TP);
        dlCardData.VALID_NTP = getDataFromDL(tempCharBuff, VALID_NTP);
        return dlCardData;
    }

    private String getDataFromDL(char[] src, int PacketType) {
        String result = "";
        String str = "";
        boolean found = false;
        int i;
        char c;
        switch (PacketType) {
            case NAME /*1*/:
                for (i = 0; i < src.length; i += NAME) {
                    if ((src[i] & MotionEventCompat.ACTION_MASK) == 193) {
                        found = true;
                        if (found) {
                            if ((src[i] & MotionEventCompat.ACTION_MASK) == 193) {
                                result = "NA";
                                break;
                            }
                            i += NAME;
                            result = new String(src, i + NAME, src[i]);
                            break;
                        }
                        return "NA";
                    }
                }
                if (found) {
                    return "NA";
                }
                if ((src[i] & MotionEventCompat.ACTION_MASK) == 193) {
                    result = "NA";
                } else {
                    i += NAME;
                    result = new String(src, i + NAME, src[i]);
                }
            case SWD_OF /*2*/:
                for (i = 0; i < src.length; i += NAME) {
                    if ((src[i] & MotionEventCompat.ACTION_MASK) == 194) {
                        found = true;
                        if (found) {
                            if ((src[i] & MotionEventCompat.ACTION_MASK) == 194) {
                                i += NAME;
                                result = new String(src, i + NAME, src[i]);
                                break;
                            }
                        }
                        return "NA";
                    }
                }
                if (found) {
                    return "NA";
                }
                if ((src[i] & MotionEventCompat.ACTION_MASK) == 194) {
                    i += NAME;
                    result = new String(src, i + NAME, src[i]);
                }
                break;
            case DOB /*3*/:
                for (i = 0; i < src.length; i += NAME) {
                    if ((src[i] & MotionEventCompat.ACTION_MASK) == 195) {
                        found = true;
                        if (found) {
                            if ((src[i] & MotionEventCompat.ACTION_MASK) == 195) {
                                i += NAME;
                                c = src[i];
                                i += NAME;
                                result = getHexValue(src[i]) + "-" + getHexValue(src[i + NAME]) + "-" + getHexValue(src[i + SWD_OF]) + getHexValue(src[i + DOB]);
                                break;
                            }
                        }
                        return "NA";
                    }
                }
                if (found) {
                    return "NA";
                }
                if ((src[i] & MotionEventCompat.ACTION_MASK) == 195) {
                    i += NAME;
                    c = src[i];
                    i += NAME;
                    result = getHexValue(src[i]) + "-" + getHexValue(src[i + NAME]) + "-" + getHexValue(src[i + SWD_OF]) + getHexValue(src[i + DOB]);
                }
                break;
            case DL_NUM /*4*/:
                for (i = 0; i < src.length; i += NAME) {
                    if ((src[i] & MotionEventCompat.ACTION_MASK) == 196) {
                        found = true;
                        if (found) {
                            if ((src[i] & MotionEventCompat.ACTION_MASK) == 196) {
                                i += NAME;
                                result = new String(src, i + NAME, src[i]);
                                break;
                            }
                        }
                        return "NA";
                    }
                }
                if (found) {
                    return "NA";
                }
                if ((src[i] & MotionEventCompat.ACTION_MASK) == 196) {
                    i += NAME;
                    result = new String(src, i + NAME, src[i]);
                }
                break;
            case ISS_AUTH /*5*/:
                for (i = 0; i < src.length; i += NAME) {
                    if ((src[i] & MotionEventCompat.ACTION_MASK) == 197) {
                        found = true;
                        if (found) {
                            if ((src[i] & MotionEventCompat.ACTION_MASK) == 197) {
                                i += NAME;
                                result = new String(src, i + NAME, src[i]);
                                break;
                            }
                        }
                        return "NA";
                    }
                }
                if (found) {
                    return "NA";
                }
                if ((src[i] & MotionEventCompat.ACTION_MASK) == 197) {
                    i += NAME;
                    result = new String(src, i + NAME, src[i]);
                }
                break;
            case DOI /*6*/:
                for (i = 0; i < src.length; i += NAME) {
                    if ((src[i] & MotionEventCompat.ACTION_MASK) == 202) {
                        found = true;
                        if (found) {
                            if ((src[i] & MotionEventCompat.ACTION_MASK) == 202) {
                                i += NAME;
                                c = src[i];
                                i += NAME;
                                result = getHexValue(src[i]) + "-" + getHexValue(src[i + NAME]) + "-" + getHexValue(src[i + SWD_OF]) + getHexValue(src[i + DOB]);
                                break;
                            }
                        }
                        return "NA";
                    }
                }
                if (found) {
                    return "NA";
                }
                if ((src[i] & MotionEventCompat.ACTION_MASK) == 202) {
                    i += NAME;
                    c = src[i];
                    i += NAME;
                    result = getHexValue(src[i]) + "-" + getHexValue(src[i + NAME]) + "-" + getHexValue(src[i + SWD_OF]) + getHexValue(src[i + DOB]);
                }
                break;
            case VALID_TP /*7*/:
                for (i = 0; i < src.length; i += NAME) {
                    if ((src[i] & MotionEventCompat.ACTION_MASK) == 198) {
                        found = true;
                        if (found) {
                            if ((src[i] & MotionEventCompat.ACTION_MASK) == 198) {
                                i += NAME;
                                c = src[i];
                                i += NAME;
                                result = getHexValue(src[i]) + "-" + getHexValue(src[i + NAME]) + "-" + getHexValue(src[i + SWD_OF]) + getHexValue(src[i + DOB]);
                                break;
                            }
                        }
                        return "NA";
                    }
                }
                if (found) {
                    return "NA";
                }
                if ((src[i] & MotionEventCompat.ACTION_MASK) == 198) {
                    i += NAME;
                    c = src[i];
                    i += NAME;
                    result = getHexValue(src[i]) + "-" + getHexValue(src[i + NAME]) + "-" + getHexValue(src[i + SWD_OF]) + getHexValue(src[i + DOB]);
                }
                break;
            case VALID_NTP /*8*/:
                for (i = 0; i < src.length; i += NAME) {
                    if ((src[i] & MotionEventCompat.ACTION_MASK) == 199) {
                        found = true;
                        if (found) {
                            if ((src[i] & MotionEventCompat.ACTION_MASK) == 199) {
                                i += NAME;
                                c = src[i];
                                i += NAME;
                                result = getHexValue(src[i]) + "-" + getHexValue(src[i + NAME]) + "-" + getHexValue(src[i + SWD_OF]) + getHexValue(src[i + DOB]);
                                break;
                            }
                        }
                        return "NA";
                    }
                }
                if (found) {
                    return "NA";
                }
                if ((src[i] & MotionEventCompat.ACTION_MASK) == 199) {
                    i += NAME;
                    c = src[i];
                    i += NAME;
                    result = getHexValue(src[i]) + "-" + getHexValue(src[i + NAME]) + "-" + getHexValue(src[i + SWD_OF]) + getHexValue(src[i + DOB]);
                }
                break;
            case VEH_INFO_1 /*9*/:
                i = 0;
                for (int j = 0; j < DL_NUM; j += NAME) {
                    while (i < src.length) {
                        if ((src[i] & MotionEventCompat.ACTION_MASK) == 200) {
                            found = true;
                            if (!found) {
                                return "NA";
                            }
                            if ((src[i] & MotionEventCompat.ACTION_MASK) == 200) {
                                i += NAME;
                                c = src[i];
                                i += NAME;
                                str = new String(src, i, 46);
                                i += 46;
                                result = new StringBuilder(String.valueOf(result)).append(" ").append(new StringBuilder(String.valueOf(str)).append(" ").append(getHexValue(src[i])).append("-").append(getHexValue(src[i + NAME])).append("-").append(getHexValue(src[i + SWD_OF])).append(getHexValue(src[i + DOB])).toString()).toString();
                            }
                        } else {
                            i += NAME;
                        }
                    }
                    if (!found) {
                        return "NA";
                    }
                    if ((src[i] & MotionEventCompat.ACTION_MASK) == 200) {
                        i += NAME;
                        c = src[i];
                        i += NAME;
                        str = new String(src, i, 46);
                        i += 46;
                        result = new StringBuilder(String.valueOf(result)).append(" ").append(new StringBuilder(String.valueOf(str)).append(" ").append(getHexValue(src[i])).append("-").append(getHexValue(src[i + NAME])).append("-").append(getHexValue(src[i + SWD_OF])).append(getHexValue(src[i + DOB])).toString()).toString();
                    }
                }
                break;
            case REG_NUM /*13*/:
                for (i = 0; i < src.length; i += NAME) {
                    if ((src[i] & MotionEventCompat.ACTION_MASK) == 192) {
                        found = true;
                        if (found) {
                            if ((src[i] & MotionEventCompat.ACTION_MASK) == 192) {
                                i += NAME;
                                result = new String(src, i + NAME, src[i]);
                                break;
                            }
                        }
                        return "NA";
                    }
                }
                if (found) {
                    return "NA";
                }
                if ((src[i] & MotionEventCompat.ACTION_MASK) == 192) {
                    i += NAME;
                    result = new String(src, i + NAME, src[i]);
                }
                break;
            case REG_NAME /*14*/:
                for (i = 0; i < src.length; i += NAME) {
                    if ((src[i] & MotionEventCompat.ACTION_MASK) == 193) {
                        found = true;
                        if (found) {
                            if ((src[i] & MotionEventCompat.ACTION_MASK) == 193) {
                                i += NAME;
                                result = new String(src, i + NAME, src[i]);
                                break;
                            }
                        }
                        return "NA";
                    }
                }
                if (found) {
                    return "NA";
                }
                if ((src[i] & MotionEventCompat.ACTION_MASK) == 193) {
                    i += NAME;
                    result = new String(src, i + NAME, src[i]);
                }
                break;
            case REG_UPTO /*15*/:
                for (i = 0; i < src.length; i += NAME) {
                    if ((src[i] & MotionEventCompat.ACTION_MASK) == 196) {
                        found = true;
                        if (found) {
                            if ((src[i] & MotionEventCompat.ACTION_MASK) == 196) {
                                i += NAME;
                                c = src[i];
                                i += NAME;
                                result = getHexValue(src[i]) + "-" + getHexValue(src[i + NAME]) + "-" + getHexValue(src[i + SWD_OF]) + getHexValue(src[i + DOB]);
                                break;
                            }
                        }
                        return "NA";
                    }
                }
                if (found) {
                    return "NA";
                }
                if ((src[i] & MotionEventCompat.ACTION_MASK) == 196) {
                    i += NAME;
                    c = src[i];
                    i += NAME;
                    result = getHexValue(src[i]) + "-" + getHexValue(src[i + NAME]) + "-" + getHexValue(src[i + SWD_OF]) + getHexValue(src[i + DOB]);
                }
                break;
        }
        return result;
    }

    private String getHexValue(char ch) {
        String str = "";
        str = Integer.toHexString(ch & MotionEventCompat.ACTION_MASK);
        if (str.length() == NAME) {
            return "0" + str;
        }
        return str;
    }

    private String readTrack(CARD_TRACK track, String src) {
        String result = "";
        String str = "";
        int index;
        switch ($SWITCH_TABLE$com$aem$api$CardReader$CARD_TRACK()[track.ordinal()]) {
            case NAME /*1*/:
                index = src.indexOf(37);
                return src.substring(index, index + 76);
            case SWD_OF /*2*/:
                index = src.indexOf(59);
                return src.substring(index, index + 37);
            default:
                return result;
        }
    }

    private int MSRvalidChar(char ch) {
        switch (ch) {
            case SWD_OF /*2*/:
            case DOB /*3*/:
            case REG_NUM /*13*/:
            case '\u001c':
                return 0;
            default:
                return NAME;
        }
    }

    public void readMSR() throws IOException {
        dos.write(new byte[]{(byte) 28, (byte) 77, (byte) 51});
        MSRReadTest = true;
    }

    public void readDL() throws IOException {
        byte[] initCmd = new byte[]{(byte) 27, (byte) 78};
        byte[] selectMainFile = new byte[REG_NAME];
        selectMainFile[0] = (byte) 126;
        selectMainFile[NAME] = (byte) 66;
        selectMainFile[DOB] = (byte) 8;
        selectMainFile[DL_NUM] = (byte) 21;
        selectMainFile[DOI] = (byte) -92;
        selectMainFile[VEH_INFO_1] = (byte) 2;
        selectMainFile[10] = (byte) 64;
        selectMainFile[12] = (byte) -71;
        selectMainFile[REG_NUM] = (byte) 126;
        byte[] selectDF1 = new byte[REG_NAME];
        selectDF1[0] = (byte) 126;
        selectDF1[NAME] = (byte) 66;
        selectDF1[DOB] = (byte) 8;
        selectDF1[DL_NUM] = (byte) 21;
        selectDF1[DOI] = (byte) -92;
        selectDF1[VEH_INFO_1] = (byte) 2;
        selectDF1[10] = (byte) 64;
        selectDF1[11] = (byte) 4;
        selectDF1[12] = (byte) -67;
        selectDF1[REG_NUM] = (byte) 126;
        byte[] selectDF2 = new byte[REG_NAME];
        selectDF2[0] = (byte) 126;
        selectDF2[NAME] = (byte) 66;
        selectDF2[DOB] = (byte) 8;
        selectDF2[DL_NUM] = (byte) 21;
        selectDF2[DOI] = (byte) -92;
        selectDF2[VEH_INFO_1] = (byte) 2;
        selectDF2[10] = (byte) 64;
        selectDF2[11] = (byte) 5;
        selectDF2[12] = (byte) -68;
        selectDF2[REG_NUM] = (byte) 126;
        readDataDF1 = new byte[12];
        byte[] readDataDF2 = new byte[12];
        readDataDF2[0] = (byte) 126;
        readDataDF2[NAME] = (byte) 66;
        readDataDF2[DOB] = (byte) 6;
        readDataDF2[DL_NUM] = (byte) 21;
        readDataDF2[DOI] = (byte) -80;
        readDataDF2[VEH_INFO_1] = (byte) -112;
        readDataDF2[10] = (byte) 113;
        readDataDF2[11] = (byte) 126;
        byte[] terminateICMode = new byte[]{(byte) 126, (byte) 4, (byte) 126};
        DLTest = true;
        try {
            dos.write(initCmd);
            Thread.sleep(500);
            dos.write(selectMainFile);
            Thread.sleep(500);
            dos.write(selectDF1);
            Thread.sleep(500);
            dos.write(readDataDF1);
            Thread.sleep(500);
            dos.write(selectDF2);
            Thread.sleep(500);
            dos.write(readDataDF2);
            Thread.sleep(500);
            dos.write(terminateICMode);
        } catch (InterruptedException e) {
        }
    }

    public void readRC() {
        byte[] initCmd = new byte[]{(byte) 27, (byte) 78};
        byte[] selectMainFile = new byte[REG_NAME];
        selectMainFile[0] = (byte) 126;
        selectMainFile[NAME] = (byte) 66;
        selectMainFile[DOB] = (byte) 8;
        selectMainFile[DL_NUM] = (byte) 21;
        selectMainFile[DOI] = (byte) -92;
        selectMainFile[VEH_INFO_1] = (byte) 2;
        selectMainFile[10] = (byte) 80;
        selectMainFile[12] = (byte) -87;
        selectMainFile[REG_NUM] = (byte) 126;
        byte[] selectDF1 = new byte[REG_NAME];
        selectDF1[0] = (byte) 126;
        selectDF1[NAME] = (byte) 66;
        selectDF1[DOB] = (byte) 8;
        selectDF1[DL_NUM] = (byte) 21;
        selectDF1[DOI] = (byte) -92;
        selectDF1[VEH_INFO_1] = (byte) 2;
        selectDF1[10] = (byte) 80;
        selectDF1[11] = (byte) 3;
        selectDF1[12] = (byte) -86;
        selectDF1[REG_NUM] = (byte) 126;
        byte[] readDataDF1 = new byte[12];
        readDataDF1[0] = (byte) 126;
        readDataDF1[NAME] = (byte) 66;
        readDataDF1[DOB] = (byte) 6;
        readDataDF1[DL_NUM] = (byte) 21;
        readDataDF1[DOI] = (byte) -80;
        readDataDF1[VEH_INFO_1] = (byte) -112;
        readDataDF1[10] = (byte) 113;
        readDataDF1[11] = (byte) 126;
        byte[] terminateICMode = new byte[]{(byte) 126, (byte) 4, (byte) 126};
        RCTest = true;
        try {
            dos.write(initCmd);
            Thread.sleep(500);
            dos.write(selectMainFile);
            Thread.sleep(500);
            dos.write(selectDF1);
            Thread.sleep(500);
            dos.write(readDataDF1);
            Thread.sleep(500);
            dos.write(terminateICMode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MSRCardData decodeCreditCard(String buffer, CARD_TRACK track) {
        byte[] bytesBuffer = new byte[buffer.length()];
        for (int i = 0; i < buffer.length(); i += NAME) {
            bytesBuffer[i] = (byte) buffer.charAt(i);
        }
        MSRCardData creditDetails = new MSRCardData();
        creditDetails.m_cardNumber = getAccountNumber(bytesBuffer, track);
        creditDetails.m_AccoundHolderName = getAccountHolderName(bytesBuffer, track);
        AdditionalAndDiscritionaryData additionalDetails = getAdditionalDetails(bytesBuffer, track);
        creditDetails.m_expiryDate = additionalDetails.expiryDate;
        creditDetails.m_serviceCode = additionalDetails.serviceCode;
        creditDetails.m_pvkiNumber = additionalDetails.Pvki;
        creditDetails.m_pvvNumber = additionalDetails.pvv;
        creditDetails.m_cvvNumber = additionalDetails.Cvv;
        return creditDetails;
    }

    String getAccountNumber(byte[] bytesBuffer, CARD_TRACK type) {
        StringBuffer accountnumber = new StringBuffer();
        byte FS_Value = (byte) 0;
        int startIndex = 0;
        if (type == CARD_TRACK.TRACK1) {
            FS_Value = (byte) 94;
            startIndex = SWD_OF;
        } else if (type == CARD_TRACK.TRACK2) {
            FS_Value = (byte) 61;
            startIndex = NAME;
        }
        int i = startIndex;
        while (i < bytesBuffer.length && bytesBuffer[i] != FS_Value) {
            accountnumber.append((char) bytesBuffer[i]);
            i += NAME;
        }
        return accountnumber.toString();
    }

    String getAccountHolderName(byte[] bytesBuffer, CARD_TRACK type) {
        if (type == CARD_TRACK.TRACK2) {
            return "NA";
        }
        int i;
        StringBuffer accountName = new StringBuffer();
        byte FS_Value = (byte) 0;
        int startIndex = 0;
        if (type == CARD_TRACK.TRACK1) {
            FS_Value = (byte) 94;
            startIndex = SWD_OF;
        }
        int startofNameOfIndex = 0;
        for (i = startIndex; i < bytesBuffer.length; i += NAME) {
            if (bytesBuffer[i] == FS_Value) {
                startofNameOfIndex = i;
                break;
            }
        }
        i = startofNameOfIndex + NAME;
        while (i < bytesBuffer.length && bytesBuffer[i] != FS_Value) {
            accountName.append((char) bytesBuffer[i]);
            i += NAME;
        }
        return accountName.toString();
    }

    AdditionalAndDiscritionaryData getAdditionalDetails(byte[] bytesBuffer, CARD_TRACK type) {
        int i;
        AdditionalAndDiscritionaryData additionalData = new AdditionalAndDiscritionaryData();
        StringBuffer expirationDateAndServiceCode = new StringBuffer();
        byte FS_Value = (byte) 0;
        int startIndex = 0;
        int FS_Count = NAME;
        if (type == CARD_TRACK.TRACK1) {
            FS_Value = (byte) 94;
            startIndex = SWD_OF;
            FS_Count = SWD_OF;
        } else if (type == CARD_TRACK.TRACK2) {
            FS_Value = (byte) 61;
            startIndex = NAME;
            FS_Count = NAME;
        }
        int startOfDataIndex = 0;
        int count = 0;
        for (i = startIndex; i < bytesBuffer.length; i += NAME) {
            if (bytesBuffer[i] == FS_Value) {
                count += NAME;
                if (count == FS_Count) {
                    startOfDataIndex = i + NAME;
                    break;
                }
            }
        }
        for (i = startOfDataIndex; i < startOfDataIndex + REG_UPTO; i += NAME) {
            expirationDateAndServiceCode.append((char) bytesBuffer[i]);
            if (i == startOfDataIndex + NAME) {
                expirationDateAndServiceCode.append("/");
            }
            if (i == startOfDataIndex + DOB) {
                expirationDateAndServiceCode.append("-");
            }
            if (i == startOfDataIndex + DOI) {
                expirationDateAndServiceCode.append("-");
            }
            if (i == startOfDataIndex + VALID_TP) {
                expirationDateAndServiceCode.append("-");
            }
            if (i == startOfDataIndex + 11) {
                expirationDateAndServiceCode.append("-");
            }
        }
        String[] additionalDataFields = expirationDateAndServiceCode.toString().split("-");
        additionalData.expiryDate = additionalDataFields[0];
        additionalData.serviceCode = additionalDataFields[NAME];
        additionalData.Pvki = additionalDataFields[SWD_OF];
        additionalData.pvv = additionalDataFields[DOB];
        additionalData.Cvv = additionalDataFields[DL_NUM];
        return additionalData;
    }
}
