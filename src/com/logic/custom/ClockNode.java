package com.logic.custom;

import java.util.ArrayList;

public class ClockNode extends Node{

    private int delay;

    public ClockNode() {
        super(new int[1]);
    }

    @Override
    public void update(Node[] nodes, ArrayList<Integer> active) {
        //this is really going to suck
    }
}
