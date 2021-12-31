package com.logic.components;

public class OutputPin extends Connection{

    /**
     * The signal being sent by this output connection
     */
    private int signal;

    public OutputPin(LComponent lcomp, int x, int y, int index, int direction, int bitWidth){
        super(lcomp, x, y, index, direction, bitWidth);
    }

    @Override
    public boolean addWire(Wire wire) {
        initWire(wire);
        return true;
    }

    @Deprecated
    public boolean getSignalOld() {
        return (signal & 1) == 1;
    }

    @Deprecated
    public void setSignalOld(boolean signal) {
        if(signal) this.signal |= 1;
        else this.signal &= ~1;
    }

    public int getSignal(){
        return signal;
    }

    public void setSignal(int signal){
        this.signal = signal;
    }
}
