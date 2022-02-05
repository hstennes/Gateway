package com.logic.custom;

import com.logic.components.CompType;
import com.logic.engine.LogicFunctions;
import com.logic.files.FileNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class BasicGateNode extends Node{

    private final byte function;

    public BasicGateNode(int[] in, int[][] out, CompType type) {
        super(in, out);
        function = (byte) LogicFunctions.getFunctionIndex(type);
    }

    @Override
    public void update(SignalProvider sp, ArrayList<Integer> active, int id) {
        int[] inputs = new int[in.length / 2];
        for(int i = 0; i < inputs.length; i++) inputs[i] = sp.getSignal(in[i * 2], in[i * 2 + 1]);
        int newSignal = LogicFunctions.basicLogic(inputs, function);
        if(newSignal == sp.getSignal(id, 0)) return;
        sp.setSignal(id, 0, newSignal);
        active.addAll(Arrays.stream(out[0]).boxed().collect(Collectors.toList()));
    }

    @Override
    public FileNode serialize() {
        return new FileNode(LogicFunctions.getCompType(function), in, out);
    }
}
