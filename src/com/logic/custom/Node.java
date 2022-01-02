package com.logic.custom;

public abstract class Node {

    /**
     * Array of nodes (fixed size)
     * Each node holds the indexes of all connected nodes, or -1 for nothing connected
     * On update: get input from input nodes, set outputs, mark (write down index) of affected output nodes
     */

    /**
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

    public int[] out;

    public Node(int[] out){
        this.out = out;
    }

    public abstract void update(Node[] nodes);
}
