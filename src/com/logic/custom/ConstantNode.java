package com.logic.custom;

import com.logic.components.CompType;

public class ConstantNode extends Node{

    public ConstantNode(CompType type) {
        super(new int[1]);
        if(type == CompType.ONE) out[0] = -1;
        else out[0] = 0;
    }

    @Override
    public void update(Node[] nodes) { }
}
