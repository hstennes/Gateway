package com.logic.components;

public class InputPin extends Connection{

    public InputPin(LComponent lcomp, int x, int y, int index, int direction, int bitWidth){
        super(lcomp, x, y, index, direction, bitWidth);
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
}
