package com.logic.custom;

import com.logic.components.CompType;
import com.logic.engine.LogicFunctions;
import com.logic.files.FileNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SingleInputGateNode implements Node {

    private final int mask;

    private final int in;

    private final int inOut;

    private int signal;

    private final int[] out;

    public SingleInputGateNode(int in, int inOut, int[] out, int signal, CompType type) {
        this.in = in;
        this.inOut = inOut;
        this.out = out;
        this.signal = signal;
        mask = type == CompType.NOT ? -1 : 0;
    }

    private SingleInputGateNode(int mask, int in, int inOut, int signal, int[] out){
        this.mask = mask;
        this.in = in;
        this.inOut = inOut;
        this.signal = signal;
        this.out = out;
    }

    @Override
    public void update(NodeBox nb, List<Integer> active) {
        int newSignal = nb.get(in, inOut) ^ mask;
        if(newSignal == signal) return;
        signal = newSignal;
        active.addAll(Arrays.stream(out).boxed().collect(Collectors.toList()));
    }

    @Override
    public int getSignal(int n) {
        return signal;
    }

    @Override
    public FileNode serialize() {
        return new FileNode(mask == -1 ? CompType.NOT : CompType.BUFFER, new int[] {in, inOut}, new int[][] {out});
    }

    @Override
    public Node duplicate() {
        return new SingleInputGateNode(mask, in, inOut, signal, out);
    }
}
