package com.logic.custom;

import sun.misc.Signal;

public class SignalProvider {

    private int[][] signals;

    private SignalProvider[] nested;

    public int getSignal(int node, int nodeOut){
        return signals[node][nodeOut];
    }

    public void setSignal(int node, int nodeOut, int signal){
        this.signals[node][nodeOut] = signal;
    }

    public SignalProvider getNestedSP(int i){
        return nested[i];
    }

    public SignalProvider duplicate(){
        return null;
    }
}
