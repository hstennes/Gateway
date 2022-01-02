package com.logic.custom;

import com.logic.components.CompType;

import java.awt.*;
import java.util.ArrayList;

public class Node {

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

    private int[] in;

    private int[] inOutIndex;

    public int[] out;

    private CompType type;

    public Node(){

    }

    public void update(Node[] nodes){

    }
}
