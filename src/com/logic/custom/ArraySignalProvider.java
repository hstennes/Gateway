package com.logic.custom;

import com.logic.components.LComponent;

import java.util.ArrayList;

public class ArraySignalProvider implements SignalProvider{

    private int[][] signals;

    private ArraySignalProvider[] nested;

    public ArraySignalProvider(int[][] signals, ArraySignalProvider[] nested){
        this.signals = signals;
        this.nested = nested;
    }

    public int getSignal(int node, int nodeOut){
        return signals[node][nodeOut];
    }

    public void setSignal(int node, int nodeOut, int signal){
        this.signals[node][nodeOut] = signal;
    }

    public SignalProvider getNestedSP(int i){
        return nested[i];
    }

    public ArraySignalProvider duplicate(){
        return null;
    }

}
