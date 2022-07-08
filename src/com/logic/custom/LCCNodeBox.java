package com.logic.custom;

public class LCCNodeBox extends NodeBox2{

    private final int[][] levels;

    public LCCNodeBox(Node[] nodes, int[] outNodes, int[][] levels) {
        super(nodes, outNodes);
        this.levels = levels;
    }

    @Override
    public int[] update(int[] signals, int[] in, int offset, ActiveStack active) {
        boolean change = false;
        for(int i = 0; i < in.length; i++){
            int address = nodes[i].address + offset;
            int currentSignal = signals[address];
            int newSignal = in[i];

            if(currentSignal != newSignal) {
                change = true;
                signals[address] = newSignal;
            }
        }
        if(!change) return getOutputs(signals, offset);

        for(int[] level : levels){
            for(int i : level){
                nodes[i].updateLCC(signals, offset, active);
            }
        }
        return getOutputs(signals, offset);
    }
}
