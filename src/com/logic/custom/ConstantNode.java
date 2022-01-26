package com.logic.custom;

import com.logic.components.CompType;
import com.logic.components.Constant;

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
    public Node duplicate() {
        return null;
    }
}
