package com.logic.custom;

import com.logic.components.CompType;
import com.logic.files.FileNode;
import com.logic.util.CompUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SplitOutNode implements Node {

    private final int[] split;

    private final int in;

    private final int inOut;

    private final int[][] out;

    private final int[] signal;

    public SplitOutNode(int[] split, int in, int inOut, int[][] out, int[] signal){
        this.split = split;
        this.in = in;
        this.inOut = inOut;
        this.out = out;
        this.signal = signal;
    }

    @Override
    public void update(NodeBox nb, List<Integer> active) {
        int input = nb.get(in, inOut);
        for(int i = 0; i < split.length; i++){
            int newSignal = input & (1 << split[i]) - 1;
            input >>= split[i];
            if(newSignal == signal[i]) continue;
            signal[i] = newSignal;
            active.addAll(Arrays.stream(out[i]).boxed().collect(Collectors.toList()));
        }
    }

    @Override
    public int getSignal(int n) {
        return signal[n];
    }

    @Override
    public FileNode serialize() {
        FileNode fileNode = new FileNode(CompType.SPLIT_OUT, new int[] {in, inOut}, out);
        fileNode.setSplit(split);
        return fileNode;
    }

    @Override
    public Node duplicate() {
        int[] newSignal = new int[signal.length];
        System.arraycopy(signal, 0, newSignal, 0, signal.length);
        return new SplitOutNode(split, in, inOut, out, newSignal);
    }
}
