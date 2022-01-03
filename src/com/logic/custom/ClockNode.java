package com.logic.custom;

import java.util.ArrayList;
import java.util.List;

public class ClockNode implements Node{

    private int delay;

    @Override
    public void update(NodeBox nb, List<Integer> active) {

    }

    @Override
    public int getSignal(int n) {
        return 0;
    }
}
