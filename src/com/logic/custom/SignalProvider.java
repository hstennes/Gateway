package com.logic.custom;

import java.util.Arrays;

public class SignalProvider {

    private int[][] signals;

    private SignalProvider[] nested;

    public SignalProvider(int[][] signals, SignalProvider[] nested){
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

    public int[] getNestedSignal(int[] address){
        if(address.length == 1) return signals[address[0]];

        int[] innerAddress = new int[address.length - 1];
        System.arraycopy(address, 1, innerAddress, 0, innerAddress.length);
        return nested[address[0]].getNestedSignal(innerAddress);
    }

    public SignalProvider getNestedSP(int i){
        return nested[i];
    }

    public SignalProvider duplicate(){
        SignalProvider[] newNested = new SignalProvider[nested.length];
        for(int i = 0; i < newNested.length; i++) newNested[i] = nested[i].duplicate();

        int[][] newSignals = new int[signals.length][];
        for(int i = 0; i < newSignals.length; i++){
            newSignals[i] = Arrays.copyOf(signals[i], signals[i].length);
        }
        return new SignalProvider(newSignals, newNested);
    }
}
