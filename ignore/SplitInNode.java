package com.logic.custom;

import com.logic.components.CompType;
import com.logic.components.SplitIn;
import com.logic.files.FileNode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SplitInNode extends Node{

    private final int[] split;

    public SplitInNode(int[] split, int[] in, int[][] out){
        super(in, out);
        this.split = split;
    }

    @Override
    public int[] update(NodeBox nb) {
        int signal = 0;
        int shift = 0;
        for(int i = 0; i < split.length; i++){
            signal |= nb.get(in[i * 2], in[i * 2 + 1]) << shift;
            shift += split[i];
        }
        return new int[] {signal};
    }

    @Override
    public FileNode serialize() {
        FileNode fileNode = new FileNode(CompType.SPLIT_IN, in, new int[][] {out});
        fileNode.setSplit(split);
        return fileNode;
    }
}
