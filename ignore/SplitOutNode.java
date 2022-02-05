package com.logic.custom;

import com.logic.components.CompType;
import com.logic.files.FileNode;
import com.logic.util.CompUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SplitOutNode extends Node {

    private final int[] split;

    public SplitOutNode(int[] split, int[] in, int[][] out){
        super(in, out);
        this.split = split;
    }

    @Override
    public int[] update(NodeBox nb) {
        int input = nb.get(in[0], in[1]);
        int[] signal = new int[out.length];
        for(int i = 0; i < split.length; i++){
            int newSignal = input & (1 << split[i]) - 1;
            input >>= split[i];
            signal[i] = newSignal;
        }
        return signal;
    }

    @Override
    public FileNode serialize() {
        FileNode fileNode = new FileNode(CompType.SPLIT_OUT, in, out);
        fileNode.setSplit(split);
        return fileNode;
    }
}
