package com.logic.custom;

public interface SignalProvider {

    int getSignal(int node, int nodeOut);

    void setSignal(int node, int nodeOut, int signal);

    SignalProvider getNestedSP(int i);

}
