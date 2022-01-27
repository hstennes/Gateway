package com.logic.custom;

import com.logic.components.CompType;
import com.logic.engine.LogicFunctions;
import com.logic.engine.LogicWorker;
import com.logic.files.FileNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BasicGateNode implements Node{

    private final byte function;

    private final int[] in;

    private final int[] out;

    private int signal;

    public BasicGateNode(int[] in, int[] out, int signal, CompType type) {
        this.in = in;
        this.out = out;
        this.signal = signal;
        function = (byte) LogicFunctions.getFunctionIndex(type);
    }

    private BasicGateNode(byte function, int[] in, int[] out, int signal){
        this.function = function;
        this.in = in;
        this.out = out;
        this.signal = signal;
    }

    @Override
    public void update(NodeBox nb, List<Integer> active) {
        int[] inputs = new int[in.length / 2];
        for(int i = 0; i < inputs.length; i++) inputs[i] = nb.get(in[i * 2], in[i * 2 + 1]);
        int newSignal = LogicFunctions.basicLogic(inputs, function);
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
        return new FileNode(LogicFunctions.getCompType(function), in, new int[][] {out});
    }

    @Override
    public Node duplicate() {
        return new BasicGateNode(function, in, out, signal);
    }
}
