package com.logic.custom;

import com.logic.components.CompType;
import com.logic.files.FileNode;

import java.util.List;

public class PlaceholderNode implements Node{

    private CompType type;

    private final int[] in;

    private final int[][] out;

    public PlaceholderNode(CompType type, int[] in, int[][] out){
        this.type = type;
        this.in = in;
        this.out = out;
    }

    @Override
    public void update(NodeBox nb, List<Integer> active) { }

    @Override
    public int getSignal(int n) {
        return 0;
    }

    @Override
    public FileNode serialize() {
        return new FileNode(type, in, out);
    }

    @Override
    public Node duplicate() {
        return new PlaceholderNode(type, in, out);
    }
}
