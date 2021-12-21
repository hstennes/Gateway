package com.logic.components;

public class Input extends Connection{

    private int bitWidth;

    public Input(LComponent lcomp, int x, int y, int index, int direction, int bitWidth){
        super(lcomp, x, y, index, direction);
        this.bitWidth = bitWidth;
    }

    public boolean addWire(Wire wire){
        if(wires.size() == 0) {
            initWire(wire);
            return true;
        }
        else {
            wire.delete();
            return false;
        }
    }

    @Override
    public int getBitWidth() {
        return bitWidth;
    }

    @Override
    public void changeBitWidth(int bitWidth) {
        this.bitWidth = bitWidth;
    }
}
