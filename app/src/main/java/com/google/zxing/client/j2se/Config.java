package com.google.zxing.client.j2se;

import com.google.zxing.DecodeHintType;
import java.util.Map;

final class Config {
    private boolean brief;
    private int[] crop;
    private boolean dumpBlackPoint;
    private boolean dumpResults;
    private Map<DecodeHintType, ?> hints;
    private boolean multi;
    private boolean productsOnly;
    private boolean pureBarcode;
    private boolean recursive;
    private int threads;
    private boolean tryHarder;

    Config() {
        this.threads = 1;
    }

    Map<DecodeHintType, ?> getHints() {
        return this.hints;
    }

    void setHints(Map<DecodeHintType, ?> hints) {
        this.hints = hints;
    }

    boolean isTryHarder() {
        return this.tryHarder;
    }

    void setTryHarder(boolean tryHarder) {
        this.tryHarder = tryHarder;
    }

    boolean isPureBarcode() {
        return this.pureBarcode;
    }

    void setPureBarcode(boolean pureBarcode) {
        this.pureBarcode = pureBarcode;
    }

    boolean isProductsOnly() {
        return this.productsOnly;
    }

    void setProductsOnly(boolean productsOnly) {
        this.productsOnly = productsOnly;
    }

    boolean isDumpResults() {
        return this.dumpResults;
    }

    void setDumpResults(boolean dumpResults) {
        this.dumpResults = dumpResults;
    }

    boolean isDumpBlackPoint() {
        return this.dumpBlackPoint;
    }

    void setDumpBlackPoint(boolean dumpBlackPoint) {
        this.dumpBlackPoint = dumpBlackPoint;
    }

    boolean isMulti() {
        return this.multi;
    }

    void setMulti(boolean multi) {
        this.multi = multi;
    }

    boolean isBrief() {
        return this.brief;
    }

    void setBrief(boolean brief) {
        this.brief = brief;
    }

    boolean isRecursive() {
        return this.recursive;
    }

    void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    int[] getCrop() {
        return this.crop;
    }

    void setCrop(int[] crop) {
        this.crop = crop;
    }

    int getThreads() {
        return this.threads;
    }

    void setThreads(int threads) {
        this.threads = threads;
    }
}
