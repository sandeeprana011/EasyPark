package com.aem.api;

import com.aem.api.CardReader.CARD_TRACK;

public interface IAemCardScanner {
    void onScanDLCard(String str);

    void onScanMSR(String str, CARD_TRACK card_track);

    void onScanRCCard(String str);

    void onScanRFD(String str);
}
