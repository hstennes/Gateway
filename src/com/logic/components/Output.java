package com.logic.components;

public class Output extends Connection{

    /**
     * The signal being sent by this output connection
     */
    private boolean[] signal;

    public Output(LComponent lcomp, int x, int y, int index, int direction, int bitWidth){
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
        return 0;
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
