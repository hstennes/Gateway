package com.logic.custom;

import com.logic.components.CompType;

import java.util.ArrayList;
import java.util.List;

public class ConstantNode implements Node{

    @Override
    public void update(NodeBox nb, List<Integer> active) {

    }

    @Override
    public int getSignal(int n) {
        return 0;
    }
}
