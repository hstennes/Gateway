package com.logic.custom;

import java.util.Arrays;

public class SignalProvider {

    /**
     * Holds all signal data for this level of the custom chip. Format signals[component id][output number]
     */
    private final int[][] signals;

    /**
     * Nested SignalProviders, which are identified by CustomNode.spIndex
     */
    private final SignalProvider[] nested;

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

    /**
     * Returns a signal at any layer in the SignalProvider. Uses the same format as CustomType.clocks to represent addresses.
     * @param address The component address
     * @return The signals for the component
     */
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

    public int[][] getRawSignals(){
        return signals;
    }

    public SignalProvider[] getAllNested(){
        return nested;
    }
}
