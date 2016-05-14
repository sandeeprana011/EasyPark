package com.google.zxing.client.j2se;

import java.util.ArrayList;
import java.util.List;

final class Inputs {
    private final List<String> inputs;
    private int position;

    Inputs() {
        this.inputs = new ArrayList(10);
        this.position = 0;
    }

    public synchronized void addInput(String pathOrUrl) {
        this.inputs.add(pathOrUrl);
    }

    public synchronized String getNextInput() {
        String result;
        if (this.position < this.inputs.size()) {
            result = (String) this.inputs.get(this.position);
            this.position++;
        } else {
            result = null;
        }
        return result;
    }

    public synchronized int getInputCount() {
        return this.inputs.size();
    }
}
