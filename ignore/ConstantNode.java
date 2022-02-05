package com.logic.custom;

import com.logic.components.CompType;
import com.logic.components.Constant;
import com.logic.files.FileNode;

import java.util.ArrayList;
import java.util.List;

public class ConstantNode extends Node{

    private final int value;

    public ConstantNode(CompType type){
        super(null, null);
        value = type == CompType.ZERO ? 0 : 1;
    }

    @Override
    public int[] update(NodeBox nb) {
        return new int[] {value};
    }

    @Override
    public FileNode serialize() {
        return new FileNode(value == 0 ? CompType.ZERO : CompType.ONE, null, null);
    }
}
