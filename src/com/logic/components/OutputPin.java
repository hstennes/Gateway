package com.logic.components;

public class OutputPin extends Connection{

    /**
     * The signal being sent by this output connection
     */
    private boolean[] signal;

    public OutputPin(LComponent lcomp, int x, int y, int index, int direction, int bitWidth){
        super(lcomp, x, y, index, direction);
        signal = new boolean[bitWidth];
    }

    @Override
    public boolean addWire(Wire wire) {
        initWire(wire);
        return true;
    }

    @Override
    public int getBitWidth() {
        return signal.length;
    }

    @Override
    public void changeBitWidth(int bitWidth) {
        signal = new boolean[bitWidth];
    }

    public boolean getSignal() {
        return signal[0];
    }

    public void setSignal(boolean signal) {
        this.signal[0] = signal;
    }
}
