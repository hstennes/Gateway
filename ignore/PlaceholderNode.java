package com.logic.custom;

import com.logic.components.CompType;
import com.logic.files.FileNode;

public class PlaceholderNode extends Node{

    private final CompType type;

    public PlaceholderNode(CompType type, int[] in, int[][] out){
        super(in, out);
        this.type = type;
    }

    @Override
    public int[] update(NodeBox nb) {
        return null;
    }

    @Override
    public FileNode serialize() {
        return new FileNode(type, in, out);
    }
}
