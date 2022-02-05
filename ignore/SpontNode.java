package com.logic.custom;

public abstract class SpontNode extends Node {

    public SpontNode(int[] in, int[][] out) {
        super(in, out);
    }

    public abstract void start(OpCustom callback);

    public abstract void stop();

}
