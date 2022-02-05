package com.logic.custom;

import com.logic.components.LComponent;

import java.util.ArrayList;
import java.util.Arrays;

public class ArraySignalProvider implements SignalProvider{

    private int[][] signals;

    private ArraySignalProvider[] nested;

    public ArraySignalProvider(int[][] signals, ArraySignalProvider[] nested){
        this.signals = signals;
        this.nested = nested;
    }

    public int getSignal(int node, int nodeOut){
        if(node == -1) return 0;
        return signals[node][nodeOut];
    }

    public void setSignal(int node, int nodeOut, int signal){
        this.signals[node][nodeOut] = signal;
    }

    public SignalProvider getNestedSP(int i){
        return nested[i];
    }

    public ArraySignalProvider duplicate(){
        ArraySignalProvider[] newNested = new ArraySignalProvider[nested.length];
        for(int i = 0; i < newNested.length; i++) newNested[i] = nested[i].duplicate();

        int[][] newSignals = new int[signals.length][];
        for(int i = 0; i < newSignals.length; i++){
            newSignals[i] = Arrays.copyOf(signals[i], signals[i].length);
        }
        return new ArraySignalProvider(newSignals, newNested);
    }
}
