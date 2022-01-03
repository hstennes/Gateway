package com.logic.custom;

import java.util.ArrayList;
import java.util.List;

public class SplitInNode implements Node{

    private int[] split;

    @Override
    public void update(NodeBox nb, List<Integer> active) {

    }

    @Override
    public int getSignal(int n) {
        return 0;
    }
}
