package com.logic.custom;

import com.logic.files.FileNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class StartNode extends Node{

    public StartNode(int[] in, int[][] out) {
        super(in, out);
    }

    @Override
    public void update(SignalProvider sp, ArrayList<Integer> active, int id) {
        active.addAll(Arrays.stream(out[0]).boxed().collect(Collectors.toList()));
    }

    @Override
    public FileNode serialize() {
        return null;
    }
}
