package com.logic.custom;

import com.logic.components.CompType;
import com.logic.engine.LogicFunctions;

public class BasicGateNode extends Node{

    private final byte function;

    public BasicGateNode(int[] in, int[][] mark, int address, CompType type) {
        super(in, mark, address);
        function = (byte) LogicFunctions.getFunctionIndex(type);
    }

    @Override
    public void update(int[] signals, int offset, ActiveStack active) {
        int newSignal = signals[in[0] + offset];
        switch(function){
            case 0:
                for(int i = 1; i < in.length; i++) newSignal &= signals[in[i] + offset];
                break;
            case 1:
                for(int i = 1; i < in.length; i++) newSignal |= signals[in[i] + offset];
                break;
            case 2:
                for(int i = 1; i < in.length; i++) newSignal ^= signals[in[i] + offset];
                break;
            case 3:
                for(int i = 1; i < in.length; i++) newSignal = ~(newSignal & signals[in[i] + offset]);
                break;
            case 4:
                for(int i = 1; i < in.length; i++) newSignal = ~(newSignal | signals[in[i] + offset]);
                break;
            case 5:
                for(int i = 1; i < in.length; i++) newSignal = ~(newSignal ^ signals[in[i] + offset]);
                break;
        }

        if(newSignal == signals[address + offset]) return;
        signals[address + offset] = newSignal;
        active.mark(mark[0]);
    }
}
