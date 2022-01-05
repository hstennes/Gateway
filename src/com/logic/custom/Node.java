package com.logic.custom;

import java.util.ArrayList;
import java.util.List;

public interface Node {

    /*
     * Array of nodes (fixed size)
     * Each node holds the indexes of all connected nodes, or -1 for nothing connected
     * On update: get input from input nodes, set outputs, mark (write down index) of affected output nodes
     */

    /*
     * BasicGate - type
     * SingleInput - type
     * Constant - type
     * Clock - delay
     * Splitter - type, split[]
     * Custom - lots of stuff!
     *
     * Button - does not exist
     * Display - does not exist
     * Light - does not exist
     * Switch - does not exist
     */

    /*
    BasicGate - function int, inputs xN, inOut xN, signal x1, output x1
    SingleInput - function boolean, input x1, inOut x1, signal x1, output x1
    Constant - type boolean, output x1
    Clock - delay int, signal x1, output x1
    SplitIn - inputs xN, inOut xN, signal x1, output x1
    Custom - inner Nodes, inputs xN, inOut xN, signal xM, output xM

    seems like a bit of a waste to have an output array when only custom component require an array

    Node interface: update(NodeBox nb), getOutput(int n)
    NodeBox: getInput(int component, int outIndex), mark(int node)

    Maximum memory efficiency, not great simulation speed because of tons of method calls
     */

    void update(NodeBox nb, List<Integer> active);

    int getSignal(int n);

    Node duplicate();
}
