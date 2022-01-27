package com.logic.custom;

import com.logic.components.CompType;
import com.logic.components.Constant;
import com.logic.files.FileNode;

import java.util.ArrayList;
import java.util.List;

public class ConstantNode implements Node{

    private int signal;

    public ConstantNode(CompType type){
        signal = type == CompType.ZERO ? 0 : 1;
    }

    @Override
    public void update(NodeBox nb, List<Integer> active) { }

    @Override
    public int getSignal(int n) {
        return signal;
    }

    @Override
    public FileNode serialize() {
        return new FileNode(signal == 0 ? CompType.ZERO : CompType.ONE, null, null);
    }

    @Override
    public Node duplicate() {
        return null;
    }
}
